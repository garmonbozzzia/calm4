package org.calm4.quotes

import akka.actor.{ActorSystem}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.actor.ActorPublisherMessage.Request
import akka.stream.scaladsl.FileIO
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import fastparse.all.P
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.calm4.quotes.Calm4.browser
import org.calm4.quotes.Inbox.InboxEntity
import org.json4s._
import org.json4s.jackson.JsonMethods._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Calm4 {
  import Utils._


  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x)
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  import java.nio.file.Paths

  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._

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
  def parseApplicant(implicit html: Document) = Applicant(
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
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  def savePage2(url: Uri, filePath: String): Future[String] = for{
    responce <- Http().singleRequest(HttpRequest(uri = url)
        .addHeader(accept)
        .addHeader(xml)
      .addHeader(cookie))
    _ <- responce.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath)))
  } yield filePath

  def loadPage(link: String): Future[Document] = Http().singleRequest(
    HttpRequest(uri = link).addHeader(cookie)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => x.utf8String)
    .map(browser.parseString)


  implicit val formats = DefaultFormats

  def loadJson(link: String): Future[JValue] = Http().singleRequest(
    HttpRequest(uri = link).addHeader(cookie).addHeader(xml).addHeader(accept)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => parse(x.utf8String.trace))

  import akka.http.scaladsl.model.Uri
  def loadJson(uri: Uri): Future[JValue] = Http().singleRequest(
    HttpRequest(uri = uri).addHeader(cookie).addHeader(xml).addHeader(accept)
  ).flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
    .map(x => parse(x.utf8String.trace))

  def getAppUrls(courseUrl: String): Future[List[String]] = loadPage(courseUrl).map(parseApplicantList)

  //.map(x => getAppId(x).map( id => savePage(host + x, id) ))
}

object CalmModel {

  type Id = Int
  case class Participant(id: Id, courseId: Id)

  trait CalmRequest
  case class GetCourseList() extends CalmRequest
  case class GetInbox() extends CalmRequest
  case class GetCourse(id: Id ) extends CalmRequest
  case class GetParticipant(id: Id , courseId: Id ) extends CalmRequest
  case class GetMessage(id: Id , participantId: Id ) extends CalmRequest
  case class GetReflist(participantId: Id) extends CalmRequest
  case class GetConversation(participantId: Id) extends CalmRequest
  case class GetSearchResult(search: String) extends CalmRequest

  trait CalmResponse
  case class CalmHtml(document: Document) extends CalmResponse
  case class CalmJson(json: JValue) extends CalmResponse
  case class InboxData(json: JValue) extends CalmResponse {
  }

  class InboxRecord(data: List[String]) {
    import fastparse.all._
    val id = P( CharIn('0'to'9').rep(1).!.map(_.toInt) )
    val participantParser = P("https://calm.dhamma.org".? ~ "/en/courses/" ~ id ~"/course_applications/" ~ id)
      .map(x => Participant(x._1, x._2))
    val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~ "/messages/" ~ id)
    def extractId(data: String): Option[Participant] = {
      participantParser.parse(data) match {
        case Parsed.Success(r, _) => Some(r)
        case _ => None
      }
    }

    private lazy val d0 = data.lift(0).map(browser.parseString)
    lazy val link: Option[String] = d0 >> attr("href")("a")
    lazy val participant = link.flatMap(extractId)
    lazy val messageType = d0 >> text("a")
    lazy val received = data.lift(1)
    lazy val name = data.lift(2)
    lazy val gender = data.lift(3)
    lazy val participationType = data.lift(4).map(browser.parseString) >> text
    lazy val courseStart = data.lift(5)
    lazy val courseEnd = data.lift(6)
    lazy val venue = data.lift(7)
    lazy val language = data.lift(8)
  }


  import Calm4._
  def load: CalmRequest => Future[CalmResponse] = {
    case GetCourse(id) => loadPage(s"https://calm.dhamma.org/en/courses/$id/course_applications").map(CalmHtml)
    case GetParticipant(id, courseId) =>
      loadPage(s"https://calm.dhamma.org/en/courses/$courseId/course_applications/$id/edit").map(CalmHtml)
    case GetInbox() => loadJson(InboxUri.uri).map(CalmJson)
  }
}

object CalmModelTest extends App {
  import CalmModel._
  import Utils._
  import Calm4._
  //load(GetCourse(2479.toString)).onComplete(_.trace)

  def extractLink(data: List[String]): Option[InboxEntity] =
    if (data.length != 9) None
    else {
      val d1 = browser.parseString(data(0))
      val d2 = browser.parseString(data(4))
      import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
      import net.ruippeixotog.scalascraper.dsl.DSL._
      Some(InboxEntity(link = d1 >> element("a") >> attr("href"), name = data(1), appType = "-", venue = data(7)))
    }



  val l1 = browser.parseString("<a class='category-link' href='/en/courses/2527/course_applications/169275/edit#ref_list'>New</a>")
  val res: String = l1 >> attr("href")("a")
  val res2 = l1 >> text("a")
  res.trace
  res2.trace

  import org.json4s.Xml.{toJson, toXml}




  val p1 = List(
    "<a class='category-link' href='/en/courses/2535/course_applications/168127/edit#ref_list'>New</a>",
    "2017-08-19",
    "Артем Микрюков",
    "M",
    "<span title='new student'>NEW<span>",
    "2017-10-11",
    "2017-10-22",
    "Saint Petersburg",
    "Russian"
  )
}
