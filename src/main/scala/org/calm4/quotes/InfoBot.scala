package org.calm4.quotes

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.methods.EditMessageText
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup, Message}
import org.calm4.quotes.CalmBot.{cache, onCallbackWithTag, request}
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.CalmModel2.CourseData
import org.calm4.quotes.Utils._

import scala.concurrent.Future
import scala.util.Random

object InfoBot extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {

  case class PageAndId(id: Id, courseId: Id, page: Int)

  def token = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  val cache = scala.collection.mutable.Map[Long, Int]()
  val myReplyMarkup = Some(InlineKeyboardMarkup.singleRow(Seq(
    InlineKeyboardButton("prev", Some("prev")),
    InlineKeyboardButton("next", Some("next"))
  )))
  val numOfItems = 20
  val course = CachedWithFile.get[CourseData](GetCourse(2535))
  val students: Future[Seq[String]] = course.map(x => x.all.map{ x =>
    s"${x.applicant_family_name} ${x.applicant_given_name}"
  })


//  onCommand('hello) { implicit msg => splitThePage(text, 4).map(_.messageId.trace) }
  onCommand('hello) { implicit msg =>
    for(text <- students)
    reply(splitThePage(text, numOfItems, 0)._1,
      replyMarkup = myReplyMarkup)
      .map{x => cache(x.messageId) = 0 ; x}

  }

  onCallbackWithTag("next") { implicit cbq =>

    for (msg <- cbq.message) {
      val page = if(cache(msg.messageId) == splitThePage(text, 2, 0)._2 - 1) splitThePage(text, 2, 0)._2 - 1
      else cache(msg.messageId) + 1
      cache(msg.messageId) = page
      for(text <- students)
      request(
        EditMessageText(
          chatId = Some(msg.source),
          messageId = Some(msg.messageId),
          text = splitThePage(text, numOfItems, page)._1,
          replyMarkup = myReplyMarkup
        )
      )
    }
  }

  onCallbackWithTag("prev") { implicit cbq =>

    for (msg <- cbq.message) {
      val page = if(cache(msg.messageId) == 0) 0 else cache(msg.messageId) - 1
      cache(msg.messageId) = page
      for(text <- students)
      request(
        EditMessageText(
          chatId = Some(msg.source),
          messageId = Some(msg.messageId),
          text = splitThePage(text, numOfItems, page)._1,
          replyMarkup = myReplyMarkup
        )
      )
    }
  }

  def splitThePage(str: Seq[String], num: Int, pageNum: Int) = {
    val allPage = str.grouped(num).toList
    (allPage(pageNum).trace.fold("")((acc, x) => acc + "\n" + x), allPage.length)
  }




  var text = Seq("Lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "Nulla", "ut", "diam", "mi.", "Mauris", "diam", "leo", "feugiat")


}

object InfoBotRun extends App {
  InfoBot.run()
}







