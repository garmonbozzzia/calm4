package org.calm4.core

import akka.actor.{Actor, Cancellable, Props}
import org.calm4.core.CalmImplicits.{system, _}
import org.calm4.core.Utils._

import scala.concurrent.duration._

object TickSource {
  case object Tick
  case object Restart
  def apply(duration: FiniteDuration)(f: => Unit) = system.actorOf(Props(new TickSource(duration)(f))).trace
}

class TickSource(duration: FiniteDuration)(f: => Unit) extends Actor {
  import TickSource._
  var cancellable: Option[Cancellable] = None
  override def receive: Receive = {
    case Restart => cancellable.foreach(_.cancel())
      cancellable = Some(system.scheduler.schedule(0 seconds, duration, self, Tick))
    case Tick => f
  }
}