package org.calm4.quotes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.ExecutionContext

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

  def parseApplicant(html: String) = ???
  def parseApplicantList(html: String) = ???
  def sessionId = ???
  def cookie = RawHeader("cookie", s"_session_id=${sessionId}")
  def savePage(url: String, filePath: String) = {
    val request = HttpRequest(uri = url).addHeader(cookie)
    Http().singleRequest(request)
    .map(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
  }//.map(FileIO.toPath(filePath))
}

object Sandbox {
  val appListFile = "data/test/apps"
  val appFile = "data/test/appExample"
}
