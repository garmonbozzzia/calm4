package org.calm4

import fastparse.all._
import org.calm4.FastParse._
import CalmModel3._

object CommandParser {
  private val cId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  private val aId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  private val mId = P(CharIn('0'to'9').rep.!.map(_.toInt))
  private val end = P("@Calm4Bot".? ~ End)
  private val courses = P("/courses"~end).map(_ => AllCoursesTm())
  private val course = P("/c"~cId~end).map(CourseTm)
  //private val course = P("/c"~cId~"w"~end).map(CourseTm)
  private val applicant = P("/c"~cId~"a"~aId~end).map(x => ApplicantTm(x._1,x._2))
  private val reflist = P("/a"~aId~"r"~end).map(ReflistTm)
  private val messages = P("/a"~aId~"m"~end).map(MessagesTm)
  private val message = P("/a"~aId~"m"~mId~end).map(x => MessageTm(x._1, x._2))
  private val note = P("/a"~aId~"n"~mId~end).map(x => NoteTm(x._1, x._2))
  private val commandParser = courses | course | applicant | reflist | messages | message | note

  def parse(cmd: String): TmCommand  = commandParser.fastParse(cmd).getOrElse(UndefinedTm(cmd))
}
