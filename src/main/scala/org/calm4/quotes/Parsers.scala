package org.calm4.quotes

import fastparse.all._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.calm4.quotes.CalmImplicits._
import org.calm4.quotes.CalmModel._
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

object CommandParser {
  import fastparse._
  import Parsers._
  val cId = P(CharIn('0'to'9').rep(exactly = 4).!.map(_.toInt))
  val aId = P(CharIn('0'to'9').rep(exactly = 6).!.map(_.toInt))
  val mId = P(CharIn('0'to'9').rep(exactly = 7).!.map(_.toInt))
  //val courseFilter = P("_".!)
  //val courseFiltered = P("/c"~id~).map()

  trait TmCommand
  type Id = Int
  case class AllCoursesTm() extends TmCommand
  case class CourseTm(id: Id) extends TmCommand
  //case class FilteredCourseTm(id: Id, g: Some[Boolean],  )
  case class ApplicantTm(aId: Id, cId: Id ) extends TmCommand
  case class ReflistTm(aId: Id) extends TmCommand
  case class ConversationTm(aId: Id) extends TmCommand
  case class MessagesTm(aId: Id) extends TmCommand
  case class MessageTm(aId: Id, mId: Id) extends TmCommand
  case class UndefinedTm(cmd: String) extends TmCommand

  private val courses = P("/c"~End).map(_ => AllCoursesTm())
  private val course = P("/c"~cId~End).map(CourseTm)
  private val applicant = P("/c"~cId~"a"~aId~End).map(x => ApplicantTm(x._1,x._2))
  private val reflist = P("/a"~aId~"r"~End).map(ReflistTm)
  private val messages = P("/a"~aId~"m"~End).map(MessagesTm)
  private val message = P("/a"~aId~"m"~mId~End).map(x => MessageTm(x._1, x._2))

  val parsers = Seq(courses,course,applicant,reflist, messages, message ).toStream

  def parse(cmd: String): TmCommand  = {
    parsers.map(fastParse(cmd, _)).collectFirst{
      case Some(x) => x
    } getOrElse(UndefinedTm(cmd))
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
