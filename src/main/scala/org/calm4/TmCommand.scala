package org.calm4

import org.calm4.CalmModel3._
import org.calm4.CommandParser._
import org.calm4.quotes.CachedWithFile

import scala.concurrent.Future

/**
  * Created by yuri on 14.09.17.
  */
trait TmCommand {
  override def toString: String = this match {
    case AllCoursesTm() => "/courses"
    case CourseTm(cId) => s"/c$cId"
    //case FilteredCourseTm() =>
    case ApplicantTm(aId, cId) => s"/c${cId}a${aId}"
    case ReflistTm(aId) => s"/a${aId}r"
    case MessagesTm(aId) => s"/a${aId}m"
    case MessageTm(aId, mId) => s"/a${aId}m$mId"
    case NoteTm(aId, nId) => s"/a${aId}n$nId"
    case UndefinedTm(msg) => s"/Error: $msg"
    //case Inbox =>
  }

  def execute: Future[Any] = this match {
    case AllCoursesTm() => Courses.list
    case CourseTm(cId) => CourseId(cId).data
    case ApplicantTm(cId, aId) => ???
    case ReflistTm(aId) => ???
    case MessagesTm(aId) => ???
    case MessageTm(aId, mId) => ???
    case NoteTm(aId, nId) => ???
    case UndefinedTm(msg) => ???
    //case Inbox =>
  }
}
