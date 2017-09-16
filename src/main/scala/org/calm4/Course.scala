package org.calm4

import org.calm4.CalmModel3.{ApplicantRecord, CourseData, CourseId, CourseInfo}
import org.calm4.CalmModel3._
import org.calm4.quotes.CachedWithFile
import org.json4s._
import CalmImplicits._
import Utils._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.attr
import org.calm4.Parsers.messageParser

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object ApplicantOrd extends Ordering[ApplicantRecord] {
  private val priorities = TmSymbolMap.toTmSeq.map(_._1)
  override def compare(x: ApplicantRecord, y: ApplicantRecord): Int = {
    if (x.state == y.state)
      x.familyName.compare(y.familyName)
    else priorities.indexOf(x.state) - priorities.indexOf(y.state)
  }
}

trait Course {
  this: CourseId =>
  override def toString: String = s"c$cId"

  implicit val ord: Ordering[ApplicantRecord] = ApplicantOrd

  def transform: PartialFunction[(String, JValue), (String, JValue)] = {
    case ("courses_sat", JNull) => "sat" -> JInt(0)
    case ("id", x) => "aId" -> x
    case ("courses_sat", JInt(x)) => "sat" -> JInt(x)
    case ("courses_served", JNull) => "served" -> JInt(0)
    case ("courses_served", JInt(x)) => "served" -> JInt(x)
    case ("applicant_given_name", x) => "givenName" -> x
    case ("applicant_family_name", x) => "familyName" -> x
    case ("age", JNull) => ("age", JInt(0))
    case ("venue_name", x) => "venue" -> x
    case ("confirmation_state_name", x) => ("state", x)
  }

  def extract(json: JValue): Seq[ApplicantRecord] = Try(json.extract[Seq[ApplicantRecord]].sorted).fold(
    ex => {s"$json\n$ex".trace; throw new Exception(ex) }, identity
  )

  def addGender(gender: String, ons: String): PartialFunction[JValue, JValue] = {
    case JObject(a) => JObject(("cId", JInt(cId)) :: ("gender" -> JString(gender)) :: ("ons" -> JString(ons)) :: a)
  }

  def data: Future[CourseData] = for {
    json <- CachedWithFile.getJson(GetCourse(cId))
    formattedJson = json.transformField(transform).camelizeKeys
  } yield {
    val info =  formattedJson.extract[CourseInfo]
      val mo = extract((formattedJson \ "sitting" \ "male" \ "old").transform(addGender("M", "O")))
      val mn = extract((formattedJson \ "sitting" \ "male" \ "new").transform(addGender("M", "N")))
      val fo = extract((formattedJson \ "sitting" \ "female" \ "old").transform(addGender("F", "O")))
      val fn = extract((formattedJson \ "sitting" \ "female" \ "new").transform(addGender("F", "N")))
      val ms = extract((formattedJson \ "serving" \ "male").transform(addGender("M", "S")))
      val fs = extract((formattedJson \ "serving" \ "female").transform(addGender("F", "S")))
    CourseData(info, mo ++ mn ++ fo ++ fn ++ ms ++ fs)
  }
}


trait Messages {
  val aId: Int

  import FastParse._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL._

  def parseMessageRecord: Seq[String] => Option[MessageRecord] = {
    case Seq(u0, date, d1, d2, applicant, email, received, _, _) =>
      val html = browser.parseString(u0)
      for {
        href <- html >?> attr("href")("a")
        Some((aId, msgType, mId)) = messageParser.fastParse(href)
      } yield MessageRecord(aId, mId, date, d1.toInt, d2.toInt, applicant, email, received, html >> text, msgType)
    case x => x.trace; throw new Exception("error")
  }

  def messages: Future[Seq[MessageRecord]] = {
    CachedWithFile.getJson(GetConversation(aId))
      .map(x => (x \ "data").extract[Seq[Seq[String]]].trace.map(parseMessageRecord).flatten)

  }
}

object A extends App{
  ApplicantId(173401).messages
}