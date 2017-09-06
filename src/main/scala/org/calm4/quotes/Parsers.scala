package org.calm4.quotes

import fastparse.all._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Element, ElementQuery}
import org.calm4.quotes.CalmImplicits._
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData, Participant}
import org.calm4.quotes.Utils._

object Parsers{
  def fastParse[T](data: String, parser: Parser[T]): Option[T] = parser.parse(data) match {
    case Parsed.Success(x: T,_) => Some(x)
    case x => None//.traceWith(_ => s"$x\n$data\n")
  }
  def course(data: String): Option[CalmModel2.Course] = {
    fastParse(browser.parseString(data) >> attr("href")("a"), courseParser)
  }
  val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
  def tmAppId(data: String): Option[Int] = fastParse[Int](data, P("/a") ~ id)
  private val host = "https://calm.dhamma.org".?
  private val courseParser = P(host ~ "/en/courses/" ~ id).map(CalmModel2.Course)
  private val courseIdParser = P(host ~ "/en/courses/" ~ id)
  private val participantParser = P(host ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
    .map(x => Participant(x._1, x._2))
  val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~ "/messages/" ~ id)

  type Id = Int
  case class ConversationJsonData(draw: Int, data: Seq[Seq[String]], recordsTotal: Option[String],
    recordsFiltered: Int, pending_letter_state_name: String, all_attachments_onclick_fn: Option[String])
  case class ConversationData(aId: Id, mId: Id, date: String, d1: Int, d2: Int, applicant: String,
                              email: String, received: String, msgType: String )
  object ConversationData {
    def json2data(data: ConversationJsonData) = data.data.map{
      case Seq(u0,date,d1,d2,applicant,email,received,_,_) =>
        val html = browser.parseString(u0)
        val href = html >> attr("href")("a")
        val Some((aId, mId)) = fastParse(html >> attr("href")("a"), messageParser)
        ConversationData(aId, mId, date, d1.toInt, d2.toInt, applicant, email, received, html >> text)
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
        val id = link.fold(0)(x => fastParse(x, courseIdParser).getOrElse(0))
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

object ApplicantRecordOrd extends Ordering[ApplicantJsonRecord] {
  private val priorities = TmSymbolMap.toTmSeq.map(_._1)
  override def compare(x: ApplicantJsonRecord, y: ApplicantJsonRecord): Int = {
    if(x.confirmation_state_name == y.confirmation_state_name)
      x.applicant_family_name.compare(y.applicant_family_name)
    else priorities.indexOf(x.confirmation_state_name) - priorities.indexOf(y.confirmation_state_name)
  }
}


import Parsers._
import CalmModel._
import Utils._
object ParsersSandBox extends App {
  //2
  CachedWithFile.getJson(GetCourseList(), _ => true)
    .map(_.trace.map(CourseRecord(_).trace))
    .map(CourseList(_).traceWith(x => s"Total: ${x.courses.length}"))
    .map(_.actual.traceWith(x => s"Total: ${x.courses.length}"))
    .map(_.traceWith(_ => "\nStatuses:"))
    .map(_.traceWith(_.courses.map(_.status).distinct.mkString("\n")))
    .map(_.traceWith(_ => "\nCourse Types:"))
    .map(_.traceWith(_.courses.map(_.cType).distinct.mkString("\n")))

//  10-Day
//  ServicePeriod
//  3-DayOSC
//  Satipatthana
//  1-DayOSC
//  20-DayOSC
//  TSC
//  Child
//  Teen

//  Finished
//  Cancelled
//  In Progress
//  Tentative
//  Scheduled
//  Scheduled (not open)
}
