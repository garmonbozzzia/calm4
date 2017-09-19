package org.calm4.model

import java.time.Instant

import akka.stream.scaladsl.Source
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.attr
import CalmModel3.{GetInbox, InboxRecord, MessageRecord}
import org.calm4.Parsers
import org.calm4.core.CalmImplicits._
import org.calm4.core.Utils._
import org.calm4.quotes.CachedWithFile
import org.json4s._

import scala.concurrent.Future

trait Inbox {
  def parseInboxRecord: PartialFunction[Seq[String], Option[InboxRecord]] = {
    case Seq(linkAndType, received, _, _, _, _, _, _, _) =>
      val html = browser.parseString(linkAndType)
      for {
        href <- html >?>[String] attr("href")("a")
        (cId, aId) <- Parsers.applicantParser.fastParse(href)//.traceWith(_ => linkAndType)
        mType = html >> text
      } yield InboxRecord(cId, aId, mType, received)
  }
  def list: Future[Seq[InboxRecord]] = for{
    json <- CachedWithFile.getJson(GetInbox(), _ => true)
    res = (json \ "data").extract[Seq[Seq[String]]].reverse.collect(parseInboxRecord).flatten
  } yield res

  def listReply: Future[Seq[InboxRecord]] = list.map(_.filter(_.mType == "Reply"))

  def listReplyMessages: Future[Seq[MessageRecord]] = Source.fromFuture(listReply)
    .mapConcat(_.to[scala.collection.immutable.Seq])
    .mapAsync(1)(_.messages)
    .map(_.filter(_.inbox))
    .runReduce(_ ++ _)

  def diff(a: Seq[InboxRecord], saved: Seq[MessageRecord]) =
    Source.fromIterator(() => a.iterator)
    .mapAsync(1)(_.messages.map(_.filter(_.inbox)))
}

object DateTimeApp extends App {
  import java.text.SimpleDateFormat
  val timezoneDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
  val a = "2017-08-30 17:14:30 UTC"
  val date = timezoneDateFormat.parse(a).trace
  import org.calm4.core.Utils._

  import scala.concurrent.duration._
  (-(System.currentTimeMillis() - date.getTime) millisecond).toDays.trace
  val b: Instant = date.toInstant.trace
  dateFormat.format(date).trace
}



