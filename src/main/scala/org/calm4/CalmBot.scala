package org.calm4

import akka.actor.{Actor, ActorRef, Cancellable, Props, Stash}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.{Chat, User}
import org.calm4.core.TickSource
import org.calm4.core.TickSource.Restart
import org.calm4.core.Utils._
import org.calm4.model.CalmModel3._

import scala.concurrent.duration._

object CalmBot extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token: String = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  lazy val inboxDemon = InboxDemon.run()

  def toLink(uri: String) = s"[ссылка]($uri)"

  object ReplyActor {
    case object Resume
    case class Message(body: String)
  }
  class ReplyActor(timeout: FiniteDuration = 1 second)
                  (implicit msg: info.mukel.telegrambot4s.models.Message) extends Actor with Stash {
    import ReplyActor._
    override def receive: Receive = {
      case Resume => "wrong resume".trace
      case body: String =>
        reply(body.trace, parseMode = Some(ParseMode.Markdown))
        system.scheduler.scheduleOnce(timeout, self, Resume)
        context.become({
          case Resume => context.unbecome()
            unstashAll().trace("unstash")
          case x => stash()
        }, false)
    }
  }

  val replyActors = scala.collection.mutable.Map.empty[Chat, ActorRef]

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
    val ra = replyActors.getOrElseUpdate(msg.chat, system.actorOf(Props(new ReplyActor(1 second))))
    InboxDemon.callbacks.append(_.tmMessages.foreach(ra.trace ! _))
  }

  onCommand('inbox) { implicit msg =>
    inboxDemon ! Restart
  }

  onMessage{ implicit msg =>
    for {
      msgText <- msg.text
      chat = msg.chat
      ra = replyActors.getOrElseUpdate(chat, system.actorOf(Props(new ReplyActor(1 second))))
      cmd = CommandParser.parse(msgText)
    } cmd.execute.foreach{ _.tmMessages.foreach(ra.trace ! _) }
  }
}

object CalmBotApp extends App {
  CalmBot.run()
}