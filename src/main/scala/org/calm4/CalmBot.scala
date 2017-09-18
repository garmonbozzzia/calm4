package org.calm4

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.User
import org.calm4.CalmModel3.CourseId
import org.calm4.TickSource.Restart
import org.calm4.Utils._

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
        buffer = ""
      case Reply(InboxDemon.NewMessage(x)) => self !
        Reply(s"New: ${CalmUri.messageUri(x.mId, x.aId).toString |> toLink }")
      case Reply(InboxDemon.RepliedMessage(x)) => self !
        Reply(s"Replied: ${CalmUri.messageUri(x.mId, x.aId)}".toString |> toLink)
      case Reply(CourseDemon.ApplicationAdded(aId, cId)) => self !
        Reply(s"New ${CalmUri.applicationUri(aId, cId).toString |> toLink }")
      case Reply(CourseDemon.StateChanged(oldState, newState, aId, cId)) => self !
        Reply(s"${TmSymbolMap.toTm(oldState)}=>${TmSymbolMap.toTm(newState)} ${CalmUri.applicationUri(aId, cId).toString |> toLink }")
      case Reply(text) =>
        buffer = s"$buffer$text\n"
        restartFlush
    }
  }

  val replyActors = scala.collection.mutable.Map.empty[User, ActorRef]

  //  val courseDemon = for {
  //    courses <- Courses.list
  //  } yield { CourseDemon.run(courses.courses) }

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
      if (!replyActors.contains(user)) {
        val replyActor = system.actorOf(Props(new ReplyActor))
        CourseDemon.callbacks.append(x => replyActor ! ReplyActor.Reply(x))
        InboxDemon.callbacks.append(x => replyActor ! ReplyActor.Reply(x))
      }
    }
  }
}


object CalmBotApp extends App {
  CalmBot.run()
}
