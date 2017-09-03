package org.calm4.quotes

import fastparse.all._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.calm4.quotes.Calm4._
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData, Participant}
import org.calm4.quotes.Utils._

object Parsers{
  def fastParse[T](data: String, parser: Parser[T]): Option[T] = parser.parse(data) match {
    case Parsed.Success(x: T,_) => Some(x)
    case x => None.traceWith(_ => s"$x\n$data\n")
  }
  def course(data: String): Option[CalmModel2.Course] = {
    fastParse(browser.parseString(data) >> attr("href")("a"), courseParser)
  }
  val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
  def tmAppId(data: String): Option[Int] = fastParse[Int](data, P("/a") ~ id)
  private val host = "https://calm.dhamma.org".?
  private val courseParser = P(host ~ "/en/courses/" ~ id).map(CalmModel2.Course)
  private val participantParser = P(host ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
    .map(x => Participant(x._1, x._2))
  val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~ "/messages/" ~ id)
}
object ApplicantRecordOrd extends Ordering[ApplicantJsonRecord] {
  private val priorities = TmSymbolMap.toTmSeq.map(_._1)
  override def compare(x: ApplicantJsonRecord, y: ApplicantJsonRecord): Int = {
    if(x.confirmation_state_name == y.confirmation_state_name)
      x.applicant_family_name.compare(y.applicant_family_name)
    else priorities.indexOf(x.confirmation_state_name) - priorities.indexOf(y.confirmation_state_name)
  }
}
object ParsersTest extends App {
//  case class DataJson(data: List[List[String]])
//  CachedResponses.getData[DataJson](GetInbox()).map(_.trace) // inbox.json
//  //CachedResponses.getJson(GetCourseList()).map(_.trace) //courses.json
//
//  case class CourseRecord(data: List[String]){
//    lazy val course = Parsers.course(data(0))
//
//  }
//  CachedResponses.getData[DataJson](GetCourseList())
//    .map(_.trace.data.map(???)) //courses.json
  //CachedResponses.getData[CourseData](GetCourse(2535))
  //GetCourse(2535).hashCode().trace
//  CachedWithFile.get[CourseData](GetCourse(2535), ???)
//    .map(_.sitting.male.old.map(_.courses_served).mkString("\n").trace)
//  CachedWithFile.get[CourseData](GetCourse(2535), ???).map(_.all.map(_.confirmation_state_name).distinct.mkString("\n")trace
//    )
//

  CachedWithFile.get[CourseData](GetCourse(2377))
    .map(_.trace.all.sorted.map(x => s"${x.confirmation_state_name} - ${x.applicant_family_name}").mkString("\n").trace)
}
