package org.calm4.quotes

import java.nio.file.Files

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, IOResult, Supervision}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Calm4 {
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

  import Utils._


  case class Applicant(id: String, name: String = "", familyName: String = "", occupation: String)

  implicit val system = ActorSystem()
  //implicit val materializer = ActorMaterializer()

  val decider: Supervision.Decider = {
    case x => Supervision.Resume.traceWith(_ => x)
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec = ExecutionContext.global

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._
  import java.nio.file.Paths

  val host = "https://calm.dhamma.org"
  val browser = JsoupBrowser()

  def parseApplicant(html: Document) = Applicant(
    occupation = html >> attr("value")("input[id=course_application_occupation]"),
    id = html >> attr("value")("input[id=course_application_display_id]"))

  def parseApplicantList(path: String) =
    (browser.parseFile(path) >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

  def parseApplicantList(path: Document) =
    (path >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

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

  def getAppUrls(courseUrl: String) = Http().singleRequest(
    HttpRequest(uri = courseUrl.trace).addHeader(cookie)
  ).flatMap(responce => responce.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
  .map(x => parseApplicantList(browser.parseString(x.utf8String)))

  def loadAndSaveApps(courseUrl: String) =
    Source.fromFuture( getAppUrls(courseUrl).map(_.traceWith(_.length)) ).mapConcat[String](x => x)
      .map( x => (host + x, s"data/test/${getAppId(x).get}.html" ) )

      .mapAsync(4)(x =>
        if(!Files.exists(Paths.get(x._2))) savePage(x._1, x._2 )
        else Future.successful(x._2)
        .map(browser.parseFile)
        .map(doc => parseApplicant(doc).traceWith(x => s"${x.id}, ${x.occupation}" ))
      )
  .runWith(Sink.ignore)
      //.map(x => getAppId(x).map( id => savePage(host + x, id) ))

}

object Sandbox extends App{
  def testAppList = ???
  val appListPath = "data/test/apps.html"
  val appFile = "data/test/appExample.html"
  //val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
  val appsUrl = "https://calm.dhamma.org/en/courses/2481/course_applications"
  import Calm4._
  import Utils._


//  savePage(appsUrl, appListPath )
//    .onComplete(_ => system.terminate().traceWith(_ => "Done!"))

//  parseApplicantList("data/test/apps.html").traceWith(_.mkString("\n"))
//  .map(getAppId(_).trace)

  loadAndSaveApps(appsUrl).onComplete(_ => system.terminate().traceWith(_ => "Done!"))

  //system.terminate()


}
