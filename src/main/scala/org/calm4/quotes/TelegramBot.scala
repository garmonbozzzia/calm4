package org.calm4.quotes

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import akka.stream.scaladsl.Source
import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative._
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup}
import org.calm4.quotes.CalmModel.{CalmJson, GetInbox, InboxRecord, load}

/**
  * Created by yuri on 25.08.17.
  */
import Utils._
import Calm4._
object CalmBot extends TelegramBot with Polling with Commands with Callbacks{
  // Use 'def' or 'lazy val' for the token, using a plain 'val' may/will
  // lead to initialization order issues.
  // Fetch the token from an environment variable or untracked file.

  def token = "418829147:AAHvnI1_RePHOrYSovqO7zMzOad2wENwwT4"

  val a = InlineKeyboardButton.callbackData("a", "ButtonTest")
  val b = InlineKeyboardButton.callbackData("b", "ButtonTest")
  val c = InlineKeyboardButton.callbackData("c", "ButtonTest")
  val d = InlineKeyboardButton.callbackData("d", "ButtonTest")
  val e = InlineKeyboardButton.callbackData("e", "ButtonTest")
  val f = InlineKeyboardButton.callbackData("f", "ButtonTest")
  val markup = InlineKeyboardMarkup(Seq(Seq(a, b), Seq(c,d,e)))

  onCommand('sessionId) { implicit msg =>
    withArgs{
      args =>
        Files.write(Paths.get(sessionIdFile), args(0).trace.getBytes(StandardCharsets.UTF_8))
        reply("Session Id saved")
    }
  }

  onCommand('apps) { implicit msg =>
    reply("10")
    Course.all.head.appRecords.map(_.trace).flatMap(x => reply(x.take(10).map(_.link).mkString("\n")))
  }

  onCallbackWithTag("ButtonTest") { implicit cbq =>
    for {
      data <- cbq.data
      //Extractors.Int(n) = data
      msg <- cbq.message
    } /* do */ {
      cbq.trace
      request(
        EditMessageText(
          Some(msg.source), // msg.chat.id
          Some(msg.messageId),
          parseMode = Some(ParseMode.HTML),
          text = s"""<b>${System.currentTimeMillis()}</b>, <strong>bold</strong>
            <i>italic</i>, <em>italic</em>
            <a href="http://www.example.com/">inline URL</a>

            <code>inline fixed-width code</code>
            <pre>pre-formatted fixed-width code block</pre>""",
          replyMarkup = Some(markup)
        ))
//        EditMessageReplyMarkup(
//          Some(msg.source), // msg.chat.id
//          Some(msg.messageId),
//          replyMarkup = Some(markup)))
    }


  }

  onCommand('appExample) { implicit msg =>
    TestData.app1TelegramViewExample.flatMap(x => reply(text = x, parseMode = Some(ParseMode.Markdown)))
  }

  onCommand('buttons) { implicit msg =>

    reply("msg", replyMarkup = Some(markup))
  }

  onCommand('inbox) { implicit msg =>
    def toTelegram(ir: InboxRecord): String = {
      ir.name.get
    }

    load(GetInbox()).map{
        case CalmJson(json) => json.extract[Inbox.InboxJson].data
          .traceWith(_.map(_.mkString("\n")).mkString("\n\n"))
          .map(new InboxRecord(_))
          .map(toTelegram)
      }.foreach(x => x.foreach(y => reply(y)))
  }

}

object CalmBotApp extends App {
  CalmBot.run()
}

