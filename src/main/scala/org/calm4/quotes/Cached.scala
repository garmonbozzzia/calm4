package org.calm4.quotes

import java.nio.charset.StandardCharsets

import org.calm4.quotes.Calm4.sessionIdFile
import org.calm4.quotes.CalmModel.CalmRequest

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by yuri on 01.09.17.
  */

class Cached[K,T]() {
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val cache = scala.collection.mutable.Map.empty[K,(Long, T)]

  def get(req: K, factory: => Future[T]): Future[T] = cache.get(req).fold{
    factory.map { x => cache(req) = (System.currentTimeMillis, x); x }
  }(x => Future(x._2))
}