package org.calm4.model

import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import CalmModel3.{GetConversation, MessageRecord}
import org.calm4.Parsers.messageParser
import org.calm4.core.CalmImplicits.{browser, _}
import org.calm4.core.Utils._
import org.calm4.quotes.CachedWithFile

import scala.concurrent.Future

trait Applicant {

  val aId: Int

  def parseMessageRecord: Seq[String] => Option[MessageRecord] = {
    case Seq(u0, date, d1, d2, sender, emailOrWeb, received, a, _) =>
      val html = browser.parseString(u0)
      for {
        inboxC <- (html >?> attr("class")("a"))
        inbox = inboxC.contains("inbox_transmission")
        href <- html >?> attr("href")("a")
        Some((aId, msgType, mId)) = messageParser.fastParse(href)
      } yield MessageRecord(mId, aId, date, d1.toInt, d2.toInt, sender,
        emailOrWeb, received, html >> text, msgType, a, inbox)
    case x => x.trace; throw new Exception("error")
  }

  def messages: Future[Seq[MessageRecord]] = {
    CachedWithFile.getJson(GetConversation(aId))
      .map(x => (x \ "data").extract[Seq[Seq[String]]].map(parseMessageRecord).flatten)
  }
}
