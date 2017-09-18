package org.calm4

import akka.actor.{Actor, Cancellable, Props}
import org.calm4.CalmImplicits.system

import scala.concurrent.duration._
import CalmImplicits._
import Utils._

/**
  * Created by yuri on 18.09.17.
  */
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