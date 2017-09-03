package org.calm4.quotes

import info.mukel.telegrambot4s.api.declarative._
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.{EditMessageText, ParseMode}
import info.mukel.telegrambot4s.models._
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.Calm4._
import org.calm4.quotes.Utils._

object CalmBot extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  // Use 'def' or 'lazy val' for the token, using a plain 'val' may/will
  // lead to initialization order issues.
  // Fetch the token from an environment variable or untracked file.

  def token = "418829147:AAHvnI1_RePHOrYSovqO7zMzOad2wENwwT4"

  onInlineQuery { implicit cbq =>
    cbq.trace
    load(GetSearchResult(cbq.query)).map { case CalmJson(json) =>
      json.data.map(x => SearchRecord(x))
    }
      .flatMap { result =>
        answerInlineQuery(
          result.zipWithIndex.map(record => InlineQueryResultArticle(
            record._2.toString,
            record._1.tmSearchHeader,
            inputMessageContent = InputTextMessageContent(record._1.tmMessage),
            description = Some(record._1.tmDescripiton))), switchPmText = Some("text"), switchPmParameter = Some("param")
        )
      }
  }

  onCommand('apps) { implicit msg =>
    reply("10")
    Course.all.head.appRecords.map(_.trace).flatMap(x => reply(x.take(10).map(_.link).mkString("\n")))
  }

  onCommand('appExample) { implicit msg =>
    TestData.app1TelegramViewExample.flatMap(x => reply(text = x, parseMode = Some(ParseMode.Markdown)))
  }


  val cache = Map[Long, Participant]()
  val cache2 = Map[Participant, Long]()
  onCallbackWithTag("Details") { implicit cbq =>
    for (msg <- cbq.message) {
      val Participant(id, cId) = cache(msg.messageId)
      val newText = load(GetParticipant(id, cId))
      newText
        .map {
          case CalmHtml(html) => "TODO" //html >>
        }
        .flatMap(x => request(
          EditMessageText(
            chatId = Some(msg.source),
            messageId = Some(msg.messageId),
            text = x
          )
        ))
    }
  }


  onCommand('inbox) { implicit msg =>
    def toTelegram(ir: InboxRecord): String = {
      s"${
        ir.messageType.map {
          case "Reply" => "📧"
          case "New" => "" //"📨"
          case x => "❌" + x
        }.get
      } *${ir.name.get}* ${
        ir.participationType.map {
          case "Server FT" => "⭐"
          case "OFT" => "🎓"
          case "NEW" => "" //"⭕"
          case x => "❌" + x
        }.get
      }${
        ir.gender.map {
          case "M" => "🚹"
          case "F" => "🚺"
          case x => "❌" + x
        }.get
      } \nПолучено: ${ir.received.get}\n[Ссылка](https://calm.dhamma.org${ir.link.get})"
    }

    //:mens::womens::new::o2::recycle:

    load(GetInbox()).map {
      case CalmJson(json) => json.data
        //.traceWith(_.map(_.mkString("\n")).mkString("\n\n"))
        .map(new InboxRecord(_))
        .map(toTelegram)
    }.foreach(
      x => x.foreach(y =>
        reply(text = y,
          parseMode = Some(ParseMode.Markdown),
          replyMarkup = Some(InlineKeyboardMarkup.singleButton(InlineKeyboardButton("Details...", Some("Details"))))
        ).foreach(_.messageId.trace)
      )
    )
  }
}

object CalmBotApp extends App {
  CalmBot.run()
}

