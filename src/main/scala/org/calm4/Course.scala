package org.calm4

import org.calm4.CalmModel3.{ApplicantRecord, CourseData, CourseId, CourseInfo}
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CachedWithFile
import org.json4s.{JInt, JNull, JObject, JString, JValue}
import CalmImplicits._

import scala.concurrent.Future

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

  def addGender(gender: String, ons: String): PartialFunction[JValue, JValue] = {
    case JObject(a) => JObject(("cId", JInt(cId)) :: ("gender" -> JString(gender)) :: ("ons" -> JString(ons)) :: a)
  }

  def data: Future[CourseData] = for {
    json <- CachedWithFile.getJson(GetCourse(cId))
    formattedJson = json.transformField(transform).camelizeKeys
  } yield {
    val info =  formattedJson.extract[CourseInfo]
    val mo = (formattedJson \ "sitting" \ "male" \ "old").transform(addGender("M", "O")).extract[Seq[ApplicantRecord]].sorted
    val mn = (formattedJson \ "sitting" \ "male" \ "new").transform(addGender("M", "N")).extract[Seq[ApplicantRecord]].sorted
    val fo = (formattedJson \ "sitting" \ "female" \ "old").transform(addGender("F", "O")).extract[Seq[ApplicantRecord]].sorted
    val fn = (formattedJson \ "sitting" \ "female" \ "new").transform(addGender("F", "N")).extract[Seq[ApplicantRecord]].sorted
    val ms = (formattedJson \ "serving" \ "male").transform(addGender("M", "S")).extract[Seq[ApplicantRecord]].sorted
    val fs = (formattedJson \ "serving" \ "female").transform(addGender("F", "S")).extract[Seq[ApplicantRecord]].sorted
    CourseData(info, mo ++ mn ++ fo ++ fn ++ ms ++ fs)
  }
}
