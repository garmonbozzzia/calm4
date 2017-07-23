package org.calm4.quotes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.FileIO
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.{ExecutionContext, Future}





object Calm4 {
  import Utils._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL._


  case class Applicant(id: String, name: String = "", familyName: String = "",
                       occupation: String, town: String, province: String)

  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  val decider: Supervision.Decider = {
    case x => Supervision.Resume.traceWith(_ => x)
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec = ExecutionContext.global

  import java.nio.file.Paths

  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._

  val host = "https://calm.dhamma.org"
  val browser = JsoupBrowser()

  val coursesFile = "data/Registration.html"
  def validate(data: List[String]) =
    data(2) == "10-Day" && (data(5) == "Finished" | data(5) == "In Progress" | data(5) == "Scheduled")
  val courses = browser.parseFile(coursesFile) >> elementList(".colour-event-datatable-row")
    .map(x => (x >> attr("href")("a"))
      .zip(x >> elementList("td") >> allText)
      .collect{ case (x,y) if validate(y) => Course(x, y)}
    )

  def parseApplicant(html: Document) = Applicant(
    occupation = html >> attr("value")("input[id=course_application_occupation]"),
    province = html >> attr("value")("input[id=course_application_contact_province]"),
    town = html >> attr("value")("input[id=course_application_contact_town]"),
    id = html >> attr("value")("input[id=course_application_display_id]"))

  def parseApplicantList(path: String) =
    (browser.parseFile(path) >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

  def parseApplicantList(path: Document) =
    (path >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

  def parseCourseList(path: String) =
    (browser.parseFile(path) >> elementList (".colour-event-datatable-row a[id=start-date]"))
    .map(_ >> attr("href"))

  def sessionId = scala.io.Source.fromFile("data/sessionId").mkString
  def cookie = RawHeader("cookie", s"_session_id=${sessionId}")
  def getAppId(url: String): Option[String] = url.split("/").lift(5)
  def savePage(url: String, filePath: String): Future[String] = {
    val request = HttpRequest(uri = url).addHeader(cookie)
    Http().singleRequest(request.addHeader(cookie))
      .flatMap(
        _.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath))))
      .map(_ => filePath)
    //_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))

  }//.map(FileIO.toPath(filePath))

  def getAppUrls(courseUrl: String): Future[List[String]] = Http().singleRequest(
    HttpRequest(uri = courseUrl.trace).addHeader(cookie)
  ).flatMap(responce => responce.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => parseApplicantList(browser.parseString(x.utf8String)))

  //.map(x => getAppId(x).map( id => savePage(host + x, id) ))

}
