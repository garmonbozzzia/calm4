package org.calm4

import fastparse.all._
import FastParse._

object CommandParser {
  val cId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  val aId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  val mId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  //val courseFilter = P("_".!)
  //val courseFiltered = P("/c"~id~).map()

  trait TmCommand
  type Id = Int
  case class AllCoursesTm() extends TmCommand
  case class CourseTm(id: Id) extends TmCommand
  //case class FilteredCourseTm(id: Id, g: Some[Boolean],  )
  case class ApplicantTm(cId: Id, aId: Id ) extends TmCommand
  case class ReflistTm(aId: Id) extends TmCommand
  case class MessagesTm(aId: Id) extends TmCommand
  case class MessageTm(aId: Id, mId: Id) extends TmCommand
  case class NoteTm(aId: Id, nId: Id) extends TmCommand
  case class UndefinedTm(cmd: String) extends TmCommand

  private val end = P("@Calm4Bot".? ~ End)
  private val courses = P("/courses"~end).map(_ => AllCoursesTm())
  private val course = P("/c"~cId~end).map(CourseTm)
  private val applicant = P("/c"~cId~"a"~aId~end).map(x => ApplicantTm(x._1,x._2))
  private val reflist = P("/a"~aId~"r"~end).map(ReflistTm)
  private val messages = P("/a"~aId~"m"~end).map(MessagesTm)
  private val message = P("/a"~aId~"m"~mId~end).map(x => MessageTm(x._1, x._2))
  private val note = P("/a"~aId~"n"~mId~end).map(x => NoteTm(x._1, x._2))

  private val commandParser = courses | course | applicant | reflist | messages | message | note

  def parse(cmd: String): TmCommand  = commandParser.fastParse(cmd).getOrElse(UndefinedTm(cmd))

  //  val parsers = Seq(courses,course,applicant,reflist, messages, message ).toStream
//  def parse(cmd: String): TmCommand  = {
//    parsers.map(fastParse(cmd, _)).collectFirst{
//      case Some(x) => x
//    } getOrElse(UndefinedTm(cmd))
//  }

//

}
