package org.calm4

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.{ReplyKeyboardMarkup, User}
import org.calm4.model.CalmModel3._
import org.calm4.core.TickSource
import org.calm4.core.TickSource.Restart
import org.calm4.core.Utils._

import scala.concurrent.duration._
import scalaz.Scalaz._

object CalmBot extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token: String = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  val inboxDemon = InboxDemon.run()

  object ReplyActor {
    case class Reply[T](text: T)
    case object Flush
  }

  def toLink(uri: String) = s"[ссылка]($uri)"

  def render: Any => String = {
    case InboxDemon.NewMessage(x) => s"New: ${CalmUri.messageUri(x.mId, x.aId).toString |> toLink }"
    case InboxDemon.RepliedMessage(x) =>
      s"Replied: ${CalmUri.messageUri(x.mId, x.aId)}".toString |> toLink
    case CourseDemon.ApplicationAdded(aId, cId) =>
      s"New ${CalmUri.applicationUri(aId, cId).toString |> toLink }"
    case CourseDemon.StateChanged(oldState, newState, aId, cId) =>
      s"${TmSymbolMap.toTm(oldState)}=>${TmSymbolMap.toTm(newState)} ${CalmUri.applicationUri(aId, cId).toString |> toLink }"
  }

  class ReplyActor(implicit msg: info.mukel.telegrambot4s.models.Message) extends Actor {
    import ReplyActor._
    var buffer = ""
    var c: Option[Cancellable] = None
    def restartFlush = {
      c.foreach(_.cancel())
      c = Some(system.scheduler.scheduleOnce(1 second, self, Flush))
    }

    override def receive: Receive = {
      case Flush =>
        reply(buffer, parseMode = Some(ParseMode.Markdown)).trace("flush")
        buffer = "\n"
      case obj =>
        buffer = s"$buffer\n$obj".trace
        restartFlush
    }
  }

  val replyActors = scala.collection.mutable.Map.empty[User, ActorRef]

  onCommand('inbox) { implicit msg =>
    inboxDemon ! Restart
  }

  onCommand('courses) { implicit msg =>
    inboxDemon ! Restart
  }

  val courseRefs = scala.collection.mutable.Map.empty[Int, ActorRef]
  onCommand('course) { implicit msg =>
    withArgs { args =>
      args.lift(0).foreach { x =>
        val cr = courseRefs.getOrElseUpdate(x.toInt, CourseDemon.run(Seq(CourseId(x.toInt))))
        cr ! TickSource.Restart
      }
    }
  }

  onCommand('start) { implicit msg =>
    msg.from.foreach { user =>
      user.trace
      if (!replyActors.contains(user)) {
        val replyActor = system.actorOf(Props(new ReplyActor))
        CourseDemon.callbacks.append(x => replyActor ! x)
        InboxDemon.callbacks.append(x => replyActor ! x)
      }
    }
  }

  onMessage{ implicit msg =>
    for {
      msgText <- msg.text
      user <- msg.from
      ra = replyActors.getOrElseUpdate(user, system.actorOf(Props(new ReplyActor)))
      cmd = CommandParser.parse(msgText)
    } cmd.execute.foreach( ra.trace ! _.tmText)
  }
}

object CalmBotApp extends App {
  CalmBot.run()
}