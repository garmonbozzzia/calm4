package org.calm4.quotes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.FileIO
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

  case class Applicant(name: String, familyName: String, occupation: String)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = ExecutionContext.global

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._
  import java.nio.file.Paths

  val host = "https://calm.dhamma.org"
  val browser = JsoupBrowser()

  def parseApplicant(html: Document) = ???
  def parseApplicantList(path: String) =
    (browser.parseFile(path) >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

  def parseApplicantList(path: Document) =
    (path >> elementList("a[id=edit-app-link]"))
      .map(_ >> attr("href") )

  def sessionId = scala.io.Source.fromFile("data/sessionId").mkString
  def cookie = RawHeader("cookie", s"_session_id=${sessionId}")
  def getAppId(url: String) = url.split("/").lift(5)
  def savePage(url: String, filePath: String): Future[IOResult] = {
    val request = HttpRequest(uri = url).addHeader(cookie)
    Http().singleRequest(request.addHeader(cookie))
    .flatMap(
      _.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath))))
      //_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))

  }//.map(FileIO.toPath(filePath))

  def getAppUrls(courseUrl: String) = Http().singleRequest(
    HttpRequest(uri = courseUrl).addHeader(cookie)
  ).flatMap(responce => responce.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
  .map(x => parseApplicantList(browser.parseString(x.utf8String)))

//  def loadAndSaveApps(courseUrl: String) =
//    val a = getAppUrls(courseUrl)

}

object Sandbox extends App{
  def testAppList = ???
  val appListPath = "data/test/apps.html"
  val appFile = "data/test/appExample.html"
  val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
  import Calm4._
  import Utils._


  //Success(1)
  //Failure("")
  //Some(1).flatMap(x => x * 2)
  List[Int]().flatMap(x => List(x,x*2,x*3) )
  None.flatMap(x => Some(2) )
  Future.successful(100).map(x => 200)
  Future.successful(100).flatMap(x => Future.never)
  .onComplete(_ => println(""))

  Success(1).toOption.toList
  Failure(new Exception("Error")).toOption.toList

//  savePage(appsUrl, appListPath )
//    .onComplete(_ => system.terminate().traceWith(_ => "Done!"))

  parseApplicantList("data/test/apps.html").traceWith(_.mkString("\n"))
  .map(getAppId(_).trace)

  system.terminate()


}
