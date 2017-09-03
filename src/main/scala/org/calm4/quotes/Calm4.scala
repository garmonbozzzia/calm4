package org.calm4.quotes

import java.nio.file.Paths

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{Uri, _}
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import net.ruippeixotog.scalascraper.model.Document
import org.calm4.quotes.Utils._


import scala.concurrent.Future

//trait Calm4Http extends CalmImplicits

trait Calm4Http extends CalmImplicits {
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  //val xcsrf = RawHeader("X-CSRF-Token", "EjeyVBeVMKOsi2SQpBXIiiztkK4vhjkP9FpUIdTDRnQ=")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")




  //case class DataJson(draw: Int, data: List[List[String]], recordsTotal: Int, recordsFiltered: Int)

//  def loadJson(link: String): Future[DataJson] =
//    for {
//      auth <- Authentication.cookie
//      response <- Http().singleRequest(HttpRequest(uri = link).withHeaders(auth, xml, accept, referer))
//      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
//    } yield parse(data.utf8String.trace).extract[DataJson]
//
//  def loadJson_(uri: Uri): Future[DataJson] =
//    for {
//      auth <- Authentication.cookie
//      response <- Http().singleRequest(HttpRequest(uri = uri).withHeaders(auth, xml, accept, referer))
//      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
//    } yield parse(data.utf8String.trace).extract[DataJson]

  //
  //  def loadJson(uri: Uri, headers: Seq[HttpHeader]): Future[String] =  Http().singleRequest(
  //    headers.foldLeft(HttpRequest(uri = uri.traceWith(_.path)))(_ addHeader _).addHeader(cookie))
  //    .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _) )
  //    .map(x => x.utf8String)

}

object Calm4 extends Calm4Old with CalmImplicits with Calm4Http {
  def loadPage(uri: Uri): Future[Document] =
    for {
      auth <- Authentication.cookie
      request = Get(uri).addHeader(auth)
      response <- Http().singleRequest(request)
      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield browser.parseString(data.utf8String)

  def loadJson(uri: Uri): Future[String] =
    for {
      auth <- Authentication.cookie
      request = Get(uri).withHeaders(auth, xml, accept, referer)
      response <- Http().singleRequest(request)
      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield data.utf8String

  def savePage(uri: String, filePath: String): Future[String] = for {
    auth <- Authentication.cookie
    req = Get(uri).addHeader(auth)
    responce <- Http().singleRequest(req)
    _ <- responce.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath)))
  } yield filePath

  def saveJson(uri: Uri, filePath: String): Future[String] = for {
    auth <- Authentication.cookie
    req = Get(uri).withHeaders(auth, xml, accept, referer)
    responce <- Http().singleRequest(req)
    _ <- responce.entity.dataBytes.runWith(FileIO.toPath(Paths.get(filePath)))
  } yield filePath

  def getAppUrls(courseUrl: String): Future[List[String]] = loadPage(courseUrl).map(parseApplicantList)
}