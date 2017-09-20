package org.calm4.model

import CalmModel3.{ApplicantRecord, CourseData, CourseInfo, _}
import org.calm4.core.ApplicantOrd
import org.calm4.core.CalmImplicits._
import org.calm4.core.Utils._
import org.calm4.quotes.CachedWithFile
import org.json4s._

import scala.concurrent.Future
import scala.util.Try

trait Course {
  val cId: Int
  //override def toString: String = s"c$cId"

  implicit val ord: Ordering[ApplicantRecord] = ApplicantOrd

  def transform: PartialFunction[(String, JValue), (String, JValue)] = {
    case ("course_id", x) => "cId" -> x
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