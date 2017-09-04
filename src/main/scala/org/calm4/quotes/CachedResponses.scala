package org.calm4.quotes

import java.nio.charset.StandardCharsets

import org.calm4.quotes.Calm4Http._
import org.calm4.quotes.CalmModel._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future
import scala.util.Try
import scalaz.Scalaz._
import scala.concurrent.ExecutionContext.Implicits._
import org.calm4.quotes.Utils._
import CalmUri._
import java.nio.file.{Files, Paths}

import net.ruippeixotog.scalascraper.model.Document

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

object CachedWithFile {
  implicit val formats = DefaultFormats
  private def path(req: Any) = s"data/cache/${req.hashCode()}.json"
  private def load(req: Any) = Try(scala.io.Source.fromFile(path(req)).mkString).toOption
    //.map(_.traceWith(_ => s"Loaded: $req"))
  private def save(req: Any, data: String) =
    Files.write(Paths.get(path(req)).traceWith(x => s"Saved: $x"), data.getBytes(StandardCharsets.UTF_8))
  def get_[K](req: K, force: Boolean, factory: K => Future[String]): Future[String] =
    (!force ? load(req) | None).fold{ factory(req)
      .map { _ *> (save(req, _)) } }(Future(_))
  def get[T](req: CalmRequest, force: Boolean = false)(implicit m: Manifest[T]): Future[T] =
    get_(req, force, CalmUri.uri andThen loadJson).map(parse(_).extract[T])
  def getPage(req: CalmRequest, force: Boolean = false) = get_(req, force, CalmUri.uri andThen loadPage_)
}