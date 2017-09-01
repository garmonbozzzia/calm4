package org.calm4.quotes

import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.Uri
import org.calm4.quotes.CachedResponses.uri
import org.calm4.quotes.Calm4.loadJson
import org.calm4.quotes.CalmModel._
import org.json4s.jackson.Serialization._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by yuri on 01.09.17.
  */
object CachedResponses extends Cached[CalmRequest,String] {
  import CalmUri._
  implicit val formats = DefaultFormats
  def uri: CalmRequest => Uri = {
    case GetCourseList() => coursesUri()
    case GetInbox() => inboxUri
    case GetCourse(id) => courseUri(id)
    //case GetParticipant(id, courseId) => applicationUri(id,courseId)
    case GetConversation(participantId) => conversationUri(participantId)
    case GetMessage(id, participantId ) => messageUri(id, participantId)
    case GetReflist(participantId) => reflistUri(participantId)
    case GetSearchResult(s) => searchUri(s)
  }
  def getJson(req: CalmRequest) = get(req, loadJson(uri(req)))

  def getData[T](calmRequest: CalmRequest)(implicit m: Manifest[T]) =
    get(calmRequest, loadJson(uri(calmRequest))).map(x => parse(x).extract[T])
}

import Utils._
object CachedWithFile {
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val formats = DefaultFormats
  import java.nio.file.{Paths, Files}
  def path(req: Any) = s"data/cache/${req.hashCode()}.json"
  def load(req: Any) = Try(scala.io.Source.fromFile(path(req)).mkString).toOption
  def save(req: Any, data: String) =
    Files.write(Paths.get(path(req)).trace, data.getBytes(StandardCharsets.UTF_8))

  def get[T](req: Any, factory: => Future[String])(implicit m: Manifest[T]): Future[T] =
    load(req).fold{ factory.map { x => save(req, x); x} }(x => Future(x))
    .map(x => parse(x).extract[T])

  def get[T](req: CalmRequest)(implicit m: Manifest[T]): Future[T] = get(req, loadJson(CachedResponses.uri(req.trace)))
}