package org.calm4.quotes

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import org.calm4.quotes.Calm4Http._
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.CalmUri._
import org.calm4.quotes.Utils._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try
import scalaz.Scalaz._

class MapCache[K,T]() {
  val cache = scala.collection.mutable.Map.empty[K,(Long, T)]
  def get(req: K, force: Boolean, factory: K => Future[T]): Future[T] =
    (!force ? cache.get(req).map(_._2) | None ).fold{
    factory(req).map { x => cache(req) = (System.currentTimeMillis, x); x }
  }(Future(_))
}

object CachedResponses extends MapCache[CalmRequest,String] {
  implicit val formats = DefaultFormats
  def getJson(req: CalmRequest): Future[String] = get(req, force = false, uri andThen loadJson)
  def get[T](calmRequest: CalmRequest, forced: Boolean = false)(implicit m: Manifest[T]): Future[T] =
    get(calmRequest, forced, uri andThen loadJson).map(x => parse(x).extract[T])
}

import scala.concurrent.duration._

object CachedWithFile {
  implicit val formats = DefaultFormats
  private def duration(path: String) = (System.currentTimeMillis - new File(path).lastModified()).millis

  def update5minutes(req: Any) = duration(path(req)).gt(req match {
    case GetCourseList() => 10 minutes
    case GetCourse(_) => 5 minutes
    case GetParticipant(_, _) => 5 minutes
    case GetConversation(_) => (10 seconds).traceWith{_ => duration(path(req)).toUnit(TimeUnit.MINUTES)}
    case GetMessage(_, _) => 5 minutes
    case _ => Duration.Inf
  })

  private def path(req: Any) = s"data/cache/${req match {
    case GetCourseList() => "_courses"
    case GetCourse(id) => s"c$id"
    case GetParticipant(aId, cId) => s"c${cId}a$aId"
    case GetConversation(aId) => s"a${aId}m"
    case GetMessage(mId, aId) => s"a${aId}m$mId"
    case _ => req.hashCode()
  }}.json"
  private def load(req: Any) = Try(scala.io.Source.fromFile(path(req)).mkString).toOption
  private def save(req: Any, data: String) =
    Files.write(Paths.get(path(req))//.traceWith(x => s"Saved: $x")
      , data.getBytes(StandardCharsets.UTF_8))
  def get_[K](req: K, updateCondition: Any => Boolean, factory: K => Future[String]): Future[String] =
    (!updateCondition(req).trace ? load(req) | None).fold{ factory(req).traceWith(_ => "Update cache")
      .map { _ *> (save(req, _)) } }(Future(_))
  def get[T](req: CalmRequest, updateCondition: Any => Boolean = update5minutes)(implicit m: Manifest[T]): Future[T] =
    get_(req, updateCondition, CalmUri.uri andThen loadJson).map(parse(_).extract[T])
  def getJson(req: CalmRequest, updateCondition: Any => Boolean = update5minutes): Future[Seq[Seq[String]]] =
    get_(req, updateCondition, CalmUri.uri andThen loadJson)
      .map(x => (parse(x.trace).trace \ "data").extract[Seq[Seq[String]]])
  def getPage(req: CalmRequest, force: Boolean = false) = get_(req, _ => force, CalmUri.uri andThen loadPage_)
}
object CreationTime extends App {
  import Utils._
  (110 minute).toMillis.trace
  //FiniteDuration(10, )
  (System.currentTimeMillis - new File("data/sessionId").lastModified()).millis.toUnit(TimeUnit.HOURS).trace
}