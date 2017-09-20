package org.calm4

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import org.calm4.core.CalmImplicits._
import org.calm4.core.TickSource
import org.calm4.core.Utils._
import org.calm4.model.CalmModel3._
import scala.concurrent.duration._
import scala.io.StdIn

object InboxDemon {

  trait InboxChange extends TmMessage
  case class NewMessage(messageRecord: MessageRecord) extends InboxChange
  case class RepliedMessage(messageRecord: MessageRecord) extends InboxChange

  val callbacks = scala.collection.mutable.ListBuffer.empty[TmMessage => Any]

  def diff(x0: Seq[MessageRecord], x1: Seq[MessageRecord]): Seq[InboxChange] =
    x1.trace("Diff:").trace.filter(!x0.contains(_))
    .map(NewMessage) ++ x0.filter(!x1.contains(_)).map(RepliedMessage)

  def run(duration: FiniteDuration = 10 minute) = {
    val s = Source.queue[Any](1, OverflowStrategy.dropNew)
      .mapAsync(1)(_ => Inbox.all)
      .prepend(Source.single(Seq()))
      .sliding(2)
      .map {
        case Seq(x, y) => new SeqTmMessage(diff(x.trace, y).trace)
      }
      .map(x => callbacks.foreach(_ (x)))
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
  InboxDemon.callbacks.append(PartialFunction[TmMessage, Any] {
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

