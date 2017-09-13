package org.calm4

import fastparse.all._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.calm4.CalmModel3._
import CalmImplicits._
import Utils._

object FastParse {
  implicit class FastParseW[T](val parser: Parser[T]) extends AnyVal {
    def fastParse(data: String): Option[T] = parser.parse(data) match {
      case Parsed.Success(x, _) => Some(x)
      case x => None.traceWith(_ => s"$x\n$data\n")
    }
  }
}
import FastParse._

trait ParsersUtils {
  val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
  val host = "https://calm.dhamma.org".?
  val courseIdParser = P(host ~ "/en/courses/" ~ id)
  val applicantParser = P(host ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
  val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~
    ("/notes/".!.map(_ => "n") | "/messages/".!.map(_ => "m")) ~ id)

}

object Parsers extends ParsersUtils{
  def parseMessageRecord: Seq[String] => Option[MessageRecord] = {
    case Seq(u0,date,d1,d2,applicant,email,received,_,_) =>
      val html = browser.parseString(u0)
      for {
        href <- html >?> attr("href")("a")
        Some((aId, msgType, mId)) = messageParser.fastParse(href)
      } yield MessageRecord(aId, mId, date, d1.toInt, d2.toInt, applicant, email, received, html >> text, msgType)
    case x => x.trace; throw new Exception("error")
  }

  def parseCourseRecord: Seq[String] => Option[CourseRecord] = {
    case Seq(htmlStart, end, cType, venue, _, status, registrars, _, _, _, _) =>
      val html = browser.parseString(htmlStart)
      for {
        href <- html >?> attr("href")("a")
        id <- courseIdParser.fastParse(href)
      } yield CourseRecord( id, html >> text, end, cType, venue, status)
    case x => x.trace; throw new Exception("error")
  }
}
