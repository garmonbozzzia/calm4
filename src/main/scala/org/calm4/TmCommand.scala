package org.calm4

import akka.http.scaladsl.model.Uri
import org.calm4.InboxDemon.NewMessage
import org.calm4.core.CalmImplicits._
import org.calm4.model.CalmModel3._

import scala.concurrent.Future
import org.calm4.core.Utils._



class SeqTmMessage(seq: Seq[TmMessage], groupSize: Int = 10) extends TmMessage {
  override def tmText = seq.map(_.tmText).mkString("\n")
  override def tmMessages: Iterable[String] = seq.zipWithIndex.map(x => s"*${x._2+1}. *${x._1.tmText}\n")
    .grouped(groupSize.trace).trace
    .map(_.mkString).toIterable
}

trait TmCommand extends TmMessage {
  override def toString: String = tmText
  override def tmText: String = this match {
    case AllCoursesTm() => "/courses"
    case CourseTm(cId) => s"/c$cId"
    //case FilteredCourseTm() =>
    case ApplicantTm(aId, cId) => s"/c${cId}a${aId}"
    case ReflistTm(aId) => s"/a${aId}r"
    case MessagesTm(aId) => s"/a${aId}m"
    case MessageTm(aId, mId) => s"/a${aId}m$mId"
    case NoteTm(aId, nId) => s"/a${aId}n$nId"
    case InboxTm() => "/inbox"
    case UndefinedTm(msg) => s"Error: $msg"
    case CourseListTm(cId) => s"/c${cId}l"
    case WatchTm(cId) => s"/w$cId"
  }
  def execute: Future[TmMessage] = this match {
    case AllCoursesTm() => Courses.list.map(x => new SeqTmMessage(x.courses))
    case CourseTm(cId) => CourseId(cId).data.map(x => new SeqTmMessage(x.applicants))
    case ApplicantTm(cId, aId) => CourseId(cId).data.map(_.applicants.find(_.aId == aId)).collect{
      case Some(x)  => x
    }
    case ReflistTm(aId) => ???
    case MessagesTm(aId) => ApplicantId(aId).messages.map(new SeqTmMessage(_))
    case MessageTm(aId, mId) => MessageId(mId, aId).data
    case NoteTm(aId, nId) => ???
    case InboxTm() => Inbox.all.map(new SeqTmMessage(_))
    case UndefinedTm(msg) => Future.successful(new TmMessage{
      override def tmText: String = msg.trace})
    case WatchTm(cId) => Future.successful(new TmMessage{
      override def tmText: String = s"Watch $cId"})
  }
}