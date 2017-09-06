package org.calm4.quotes

import java.nio.file.Paths
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers._
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import net.ruippeixotog.scalascraper.model.Document
import scala.concurrent.Future
import CalmImplicits._

object Calm4Http{
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  //val xcsrf = RawHeader("X-CSRF-Token", "EjeyVBeVMKOsi2SQpBXIiiztkK4vhjkP9FpUIdTDRnQ=")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")

  def loadPage(uri: Uri): Future[Document] =
    for {
      auth <- Authentication.cookie
      request = Get(uri).addHeader(auth)
      response <- Http().singleRequest(request)
      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield browser.parseString(data.utf8String)

  def loadPage_(uri: Uri): Future[String] =
    for {
      auth <- Authentication.cookie
      request = Get(uri).addHeader(auth)
      response <- Http().singleRequest(request)
      data <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield data.utf8String

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
}