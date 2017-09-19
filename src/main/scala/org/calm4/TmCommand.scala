package org.calm4

import akka.http.scaladsl.model.Uri
import org.calm4.core.CalmImplicits._
import org.calm4.model.CalmModel3._

import scala.concurrent.Future
import org.calm4.core.Utils._

trait TmMessage {

  private def link(uri: Uri, link: String = "link") = s"[$link]($uri)"
  private def onsRender(ons: String, sat: Int, served: Int) = ons match {
    case "O" => s"⭐ Sit: $sat Served: $served"
    case "S" => s"⭐ Sit: $sat Served: $served"
    case "N" => ""
  }
  def tmText: String = this match {
    case ApplicantRecord(aId, cId, _, state, givenName, familyName, sat, served, age, ons, gender, pregnant) =>
      s"""|*$familyName $givenName* _Age:_ $age
         |${TmSymbolMap.toTm(state)} $state ${onsRender(ons, sat,served)}
         |/a${aId}m""".stripMargin
    case CourseData(courseInfo, applicants) => applicants
      .zipWithIndex.map(x => s"*${x._2+1}. *${x._1.tmText}").mkString("\n")
    case CourseDemon.ApplicationAdded(aId, cId) =>
      s"New ${link(CalmUri.applicationUri(aId, cId))}"
    case CourseDemon.StateChanged(oldState, newState, aId, cId) =>
      s"${TmSymbolMap.toTm(oldState)}=>${TmSymbolMap.toTm(newState)} ${link(CalmUri.applicationUri(aId, cId))}"
    case CourseRecord(id, start, end, cType, venue, status) =>
      s"""${link(CalmUri.courseUri(id), cType)} _${venue}_
         |    $status /c$id
         |    🗓$start 🗓 $end
         """.stripMargin
    case CourseList(courses) => courses.zipWithIndex.map(x => s"*${x._2+1}. *${x._1.tmText}").mkString("\n")
    case MessageRecord(mId, aId, date, _, _, _, _, _, title, mn, _, _) =>
      s"$date:\n*$title*\n/a${aId}${mn}$mId"
    case x => x.toString
  }
}

case class SeqTmMessage(seq: Seq[TmMessage]) extends TmMessage {
  seq.map(_.tmText).mkString("\n")
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
  }
  def execute: Future[TmMessage] = this match {
    case AllCoursesTm() => Courses.list
    case CourseTm(cId) => CourseId(cId).data
    case ApplicantTm(cId, aId) => CourseId(cId).data.map(_.applicants.find(_.aId == aId)).collect{
      case Some(x)  => x
    }

    case ReflistTm(aId) => ???
    case MessagesTm(aId) => ApplicantId(aId).messages.map(SeqTmMessage)
    case MessageTm(aId, mId) => MessageId(mId, aId).data
    case NoteTm(aId, nId) => ???
    case InboxTm() => Inbox.list.map(SeqTmMessage)
    case UndefinedTm(msg) => Future.successful(new TmMessage{
      override def tmText: String = msg.trace})
    //case Inbox =>
  }
}
