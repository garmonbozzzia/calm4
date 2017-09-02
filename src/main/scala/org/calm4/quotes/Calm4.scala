package org.calm4.quotes

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, Uri, _}
import akka.stream.scaladsl.FileIO
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import org.calm4.quotes.CalmModel2.ApplicantJsonRecord
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Implicits {

}

object Calm4 {
  import Utils._

  implicit val system = ActorSystem()
  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

  //implicit val materializer = ActorMaterializer()
  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x)
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global


  val host = "https://calm.dhamma.org"
  val browser = JsoupBrowser()

  val actualStates = List("Finished", "In Progress", "Scheduled")
  def validate(data: List[String]) =
    data(2) == "10-Day" && actualStates.exists( _ == data(5))

  val coursesFile = "data/Registration.html"
  val courses: List[Course] = browser.parseFile(coursesFile) >> elementList(".colour-event-datatable-row")
    .map(x => (x >> attr("href")("a"))
      .zip(x >> elementList("td") >> allText)
      .collect{ case (x,y) if validate(y) => Course(x, y)}
    )

  def occupationExtractor(implicit html: Document): String = html >> attr("value")("input[id=course_application_occupation]")

  def familyNameExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_applicant_family_name]")
  def givenNameExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_applicant_given_name]")
  //def provinceExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_contact_province]")
  def townExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_contact_town]")
  def idExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_display_id]")
  def parseApplicant(implicit html: Document) = Applicant_(
    name = givenNameExtractor,
    familyName = familyNameExtractor,
    occupation = occupationExtractor,
    province = "provinceExtractor",
    town = townExtractor,
    id = idExtractor)

  def parseApplicantList(path: String): List[String] = parseApplicantList(browser.parseFile(path))
  def parseApplicantList(path: Document) = (path >> elementList("a[id=edit-app-link]")).map(_ >> attr("href") )

  def parseCourseList(path: String) : List[String] = parseCourseList(browser.parseFile(path))

  def parseCourseList(doc: Document): List[String] =
    (doc >> elementList (".colour-event-datatable-row a[id=start-date]")).map(_ >> attr("href"))

  val sessionIdFile = "data/sessionId"
  val sessionId = scala.io.Source.fromFile(sessionIdFile).mkString
  val cookie = RawHeader("cookie", s"_sso_session=$sessionId")
  def getAppId(url: String): Option[String] = url.split("/").lift(5)
  def savePage(url: String, filePath: String): Future[String] = for{
      responce <- Http().singleRequest(HttpRequest(uri = url).addHeader(cookie))
      _ <- responce.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath)))
    } yield filePath

  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  //val xcsrf = RawHeader("X-CSRF-Token", "EjeyVBeVMKOsi2SQpBXIiiztkK4vhjkP9FpUIdTDRnQ=")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer","")
  def savePage2(url: Uri, filePath: String): Future[String] = for{
    responce <- Http().singleRequest(HttpRequest(uri = url)
        .addHeader(accept)
        .addHeader(xml)
        .addHeader(referer)
      .addHeader(cookie))
    _ <- responce.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath)))
  } yield filePath

  def loadPage(link: String): Future[Document] = Http().singleRequest(
    HttpRequest(uri = link).addHeader(cookie)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => x.utf8String)
    .map(browser.parseString)

  implicit val formats = DefaultFormats
  //case class DataJson(draw: Int, data: List[List[String]], recordsTotal: Int, recordsFiltered: Int)
  case class DataJson(data: List[List[String]])

  def loadJson(link: String): Future[DataJson] = Http().singleRequest(
    HttpRequest(uri = link).addHeader(cookie).addHeader(xml).addHeader(accept).addHeader(referer)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => parse(x.utf8String.trace).extract[DataJson])

  import akka.http.scaladsl.model.Uri
  def loadJson_(uri: Uri, headers: Seq[HttpHeader]): Future[DataJson] =  Http().singleRequest(
    headers.foldLeft(HttpRequest(uri = uri.trace))(_ addHeader _).addHeader(cookie))
      .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
      .map(x => parse(x.utf8String.trace).extract[DataJson])

  def loadJson_(uri: Uri): Future[DataJson] = Http().singleRequest(
    HttpRequest(uri = uri)
      //.addHeaders(TestSearchUri.hs.toArray)
      .addHeader(cookie).addHeader(xml).addHeader(accept).addHeader(referer)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => parse(x.utf8String.trace).extract[DataJson])

  def loadJson(uri: Uri, headers: Seq[HttpHeader]): Future[String] =  Http().singleRequest(
    headers.foldLeft(HttpRequest(uri = uri.traceWith(_.path)))(_ addHeader _).addHeader(cookie))
    .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => x.utf8String)

  def loadJson(uri: Uri): Future[String] = Http().singleRequest(
    HttpRequest(uri = uri.traceWith(_.path))
      //.addHeaders(TestSearchUri.hs.toArray)
      .addHeader(cookie).addHeader(xml).addHeader(accept).addHeader(referer)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => x.utf8String)


  def getAppUrls(courseUrl: String): Future[List[String]] = loadPage(courseUrl).map(parseApplicantList)

  //.map(x => getAppId(x).map( id => savePage(host + x, id) ))
}