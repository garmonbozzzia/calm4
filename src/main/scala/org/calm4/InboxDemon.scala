package org.calm4

import akka.actor.{Actor, Cancellable, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import org.calm4.CalmImplicits._
import org.calm4.CalmModel3._
import org.calm4.Utils._

import scala.concurrent.duration._
import scala.io.StdIn



import org.calm4.TickSource._





object InboxDemon {

  trait InboxChange
  case class NewMessage(messageRecord: MessageRecord) extends InboxChange
  case class RepliedMessage(messageRecord: MessageRecord) extends InboxChange

  val callbacks = scala.collection.mutable.ListBuffer.empty[InboxChange => Any]

  def diff(x0: Seq[MessageRecord], x1: Seq[MessageRecord]): Seq[InboxChange] = x1.filter(!x0.contains(_))
    .map(NewMessage) ++ x0.filter(!x1.contains(_)).map(RepliedMessage)

  def run(duration: FiniteDuration = 10 minute) = {
    val s = Source.queue[Any](1, OverflowStrategy.dropNew)
      .mapAsync(1)(_ => Inbox.listReplyMessages)
      .sliding(2)
      .map {
        case Seq(x, y) => diff(x, y)
      }
      .map(_.foreach(x => callbacks.foreach(_ (x))))
      .to(Sink.ignore)
      .run()

    val ts = TickSource(duration)(s.offer(""))
    //ts ! Restart
    //ts ! Restart
    ts
  }
}

object InboxNewApp extends App {
  val ts = InboxDemon.run()
  InboxDemon.callbacks.append(PartialFunction[InboxDemon.InboxChange, Any] {
    case InboxDemon.NewMessage(x) => s"New: ${CalmUri.messageUri(x.mId, x.aId)}".trace
    case InboxDemon.RepliedMessage(x) => s"Replied: ${CalmUri.messageUri(x.mId, x.aId)}".trace
  })

  val cs = CourseDemon.run(Seq(CourseId(2330)))
  CourseDemon.callbacks.append(PartialFunction[CourseDemon.CourseDiff, Any] {
    case CourseDemon.ApplicationAdded(aId, cId) =>
      s"New ${CalmUri.applicationUri(aId, cId)}".trace
    case CourseDemon.StateChanged(oldState, newState, aId, cId) =>
      s"$oldState => $newState ${CalmUri.applicationUri(aId, cId)}".trace
  })

  Source.fromIterator(() => Iterator.continually(StdIn.readLine))
    .map {
      case "inbox" => ts ! TickSource.Restart
      case "courses" => cs ! TickSource.Restart
    }
    .runWith(Sink.ignore)
}

object AAAA extends App {
  f"${1}%.2f".trace
  val coll = scala.collection.mutable.Seq.empty[Int]
}