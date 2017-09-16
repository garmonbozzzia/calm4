package org.calm4.quotes

import akka.stream.scaladsl.{Sink, Source}
import fastparse.all._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.calm4.quotes.CachedWithFile.get
import org.calm4.CalmImplicits._
import org.calm4.CalmModel3._
import org.calm4.Utils._

import scala.concurrent.Future

object Parsers{
  implicit class FastParse[T](val parser: Parser[T] ) extends AnyVal {
    def fastParse(data: String): Option[T] = parser.parse(data) match {
      case Parsed.Success(x,_) => Some(x)
      case x => None.traceWith(_ => s"$x\n$data\n")
    }
  }

  val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
  private val host = "https://calm.dhamma.org".?
  private val courseParser = P(host ~ "/en/courses/" ~ id).map(CalmModel2.Course)
  private val courseIdParser = P(host ~ "/en/courses/" ~ id)
  val applicantParser = P(host ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
  val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~
    ("/notes/".!.map(_ => "n") | "/messages/".!.map(_ => "m")) ~ id)


  type Id = Int
  case class ConversationJsonData(draw: Int, data: Seq[Seq[String]], recordsTotal: Option[String],
    recordsFiltered: Int, pending_letter_state_name: String, all_attachments_onclick_fn: Option[String])
  case class ConversationData(aId: Id, mId: Id, date: String, d1: Int, d2: Int, applicant: String,
                              email: String, received: String, msgType: String, urlType: String = "messages" )
  object ConversationData {
    def json2data(data: ConversationJsonData) = data.data.map{
      case Seq(u0,date,d1,d2,applicant,email,received,_,_) =>
        val html = browser.parseString(u0)
        val href = html >> attr("href")("a")
        val Some((aId, msgType, mId)) = messageParser.fastParse(html >> attr("href")("a"))
        ConversationData(aId, mId, date, d1.toInt, d2.toInt, applicant, email, received, html >> text, msgType)
    }
  }
  case class MessageJsonDataSegment(value: String)
  case class MessageJsonData(segments: Seq[MessageJsonDataSegment])
  object MessageData {
    def json2data(data: MessageJsonData): String = data.segments match {
      case Seq(x) => browser.parseString(x.value) >> allText
    }
  }

  case class CourseRecord(id: Int, start: String, end: String, cType: String, venue: String, status: String)
  object CourseRecord {
    def apply: Seq[String] => CourseRecord = {
      case Seq(htmlStart, end, cType, venue, _, status, registrars, _, _, _, _) =>
        val html = browser.parseString(htmlStart)
        val link = html >?> attr("href")("a")
        val id = link.fold(0)(x => courseIdParser.fastParse(x).getOrElse(0))
        CourseRecord( id, html >> text, end, cType, venue, status)
      case x => x.trace; throw new Exception("error")
    }
  }
  case class CourseList(courses: Seq[CourseRecord]) {
    def actual = CourseList(
      courses.filter(_.cType match {
        case "10-Day" => true
        //case "1-DayOSC" => true
        case "3-DayOSC" => true
        case _ => false
      }).filter(_.status match {
        case "In Progress" => true
        case "Tentative" => true
        case "Scheduled" => true
        case _ => false
      }).filter(_.id != 0)
    )
  }
}

import org.calm4.quotes.CalmModel._
import org.calm4.quotes.Parsers._
import org.calm4.Utils._

object ConversationTest extends App {
  def parse: Seq[String] => ConversationData = {
    case Seq(u0, date, d1, d2, applicant, email, received, _, _) =>
      val html = browser.parseString(u0)
      val href: String = (html >?> attr("href")("a")).getOrElse(u0.trace)
      val Some((aId, isNote, mId)) = messageParser.fastParse(href).trace
      ConversationData(aId, mId, date, d1.toInt, d2.toInt, applicant, email, received, html >> text)
  }

  //Conversation(171471).data
  CachedWithFile.getDataJson(GetConversation(171471), _ => false)
    .map(_.map(parse).mkString("\n").trace)
}

//class Inbox() {
//  class Inbox()
//  object Inbox {
//    //var old: Option[Inbox] = None
//    def apply: Future[Option[Inbox]] = ???
//    def inbox: Future[Option[Inbox]] = ???
//    def message(mId: Int, cId: Int ) = ???
//    def applicant(aId: Int, cId: Int ) = ???
//    def course(cId: Int) = ???
//    def courses = ???
//  }
//
//  object Message {
//    import scala.collection.mutable.Map
//    val messages = Map[Int, Message]()
//    def apply(mId: Int, aId: Int) = messages.getOrElseUpdate(mId, new Message(mId, aId))
//  }
//
//  class InboxDiff
//  def diff: (Option[Inbox], Option[Inbox]) => InboxDiff = ???
//  //List(1,2,3).
//  Source.fromIterator(() => Iterator.continually(Inbox.inbox) )
//      .prepend(Source.single(Future(None)))
//      .mapAsync(1)(x => x)
//    .sliding(2)
//    .map{ case Seq(x,y) => diff(x,y) }
//    .runWith(Sink.ignore)
//
//  case class Course(cId: Int)
//  case class Applicant(aId: Int, cId: Int)
//  case class Note(nId: Int, aId: Int)
//  case class Message(mId: Int, aId: Int) {
//  }
//
//  def messages = ???
//}

object Inbox extends App{
  import CachedWithFile._
  type Id = Int
  case class InboxRecord(cId: Id, aId: Id, mType: String)
  def parseInbox: PartialFunction[Seq[String], Option[InboxRecord]] = {
    case Seq(linkAndType, _, _, _, _, _, _, _, _) =>
      val html = browser.parseString(linkAndType.trace)
      for{
        href <-  html >?>[String] attr("href")("a")
        (cId, aId) <- Parsers.applicantParser.fastParse(href).traceWith(_ => linkAndType)
        mType = html >> text
      } yield InboxRecord(cId,aId,mType)
  }
  def parseReplyInbox: Seq[String] => Option[CalmRequest] = {
    case Seq(msgInfo, date, c2, c3, sender, email, received, _, _) =>
      val html = browser.parseString(msgInfo)
    for {
      cl <- html >?> attr("class")("a")
      if cl.contains("inbox_transmission")
      href <- html >?> attr("href")("a")
      (aId, mType, mId) <- messageParser.fastParse(href)
    } yield if(mType == "m") GetMessage(mId, aId) else GetNote(mId, aId)
  }

  def inbox: Future[Seq[InboxRecord]] =
    getDataJson(GetInbox(), _ => false).map(_.collect(parseInbox).flatten.filter(_.mType == "Reply"))

  val a = for{
    messages <- getDataJson(GetInbox(), _ => false)
    replyMessages = messages.collect(parseInbox).flatten.filter(_.mType == "Reply")
      .map(x => GetConversation(x.aId))
  } Source.fromIterator(() => replyMessages.iterator)
    .mapAsync(1){getDataJson(_, _ => false)}
    .map(_.map(parseReplyInbox).flatten)
      .mapConcat(_.to[scala.collection.immutable.Seq])
      .filter(_.isInstanceOf[GetMessage])
      .take(20)
      .mapAsync(1) {x => get[MessageJsonData](x)
          .map(x => MessageData.json2data(x))
      }
    .runForeach(_.take(200).trace)
}

//object A extends App {
//  //CachedWithFile.getJson(GetNote(1409106,180157), _ => true).map(_.trace)
//  for{
//    (a,b) <- List(1,2,3)
//  } yield a
//}



//Запрос(курсы...) Ответ( Json | Html ) КалмОбъект() ТмКоманда ТмСообщение
//(входящее сообщение)(новая анкета, сообщение о доставке, ответ)
//case class InboxRecord()
//case class Inbox(records: Seq[InboxRecord])
//case class NewApplication()
//case class DeliveryNote()
//case class Note()
//case class Message()
//case class Applicant()
//case class ReplyMessage(){
//  def applicant: Applicant = ???
//  def message: Message = ???
//}

