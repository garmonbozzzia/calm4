package org.calm4

import akka.http.scaladsl.model.Uri
import org.calm4.InboxDemon.NewMessage
import org.calm4.model.CalmModel3.{ApplicantRecord, CourseRecord, MessageRecord}

/**
  * Created by yuri on 20.09.17.
  */
trait TmMessage {

  private def link(uri: Uri, link: String = "link") = s"[$link]($uri)"
  private def onsRender(ons: String, sat: Int, served: Int) = ons match {
    case "O" => s"🎓 Sit: $sat Served: $served"
    case "S" => s"⭐ Sit: $sat Served: $served"
    case "N" => ""
  }

  def tmMessages: Iterable[String] = tmText :: Nil

  def tmText: String = this match {
    case NewMessage(MessageRecord(mId, aId, date, _, _, sender, emailOrForm, _, title, msgType, _, _)) =>
      s"""|${sender}: *${title}* ${link(CalmUri.messageOrNoteUri(mId, aId, msgType))}""".stripMargin
    case ApplicantRecord(aId, cId, _, state, givenName, familyName, sat, served, age, ons, gender, pregnant) =>
      s"""|*$familyName $givenName* _Age:_ $age
          |    ${TmSymbolMap.toTm(state)} $state ${onsRender(ons, sat,served)}
          |    /a${aId}m""".stripMargin
//    case CourseData(courseInfo, applicants) => applicants
//      .zipWithIndex.map(x => s"*${x._2+1}. *${x._1.tmText}").mkString("\n")
    case CourseDemon.ApplicationAdded(aId, cId) =>
      s"New ${link(CalmUri.applicationUri(aId, cId))}"
    case CourseDemon.StateChanged(oldState, newState, aId, cId) =>
      s"${TmSymbolMap.toTm(oldState)}=>${TmSymbolMap.toTm(newState)} ${link(CalmUri.applicationUri(aId, cId))}"
    case CourseRecord(id, start, end, cType, venue, status) =>
      s"""${link(CalmUri.courseUri(id), cType)} _${venue}_
         |    $status /c$id
         |    🗓$start 🗓 $end""".stripMargin
    case MessageRecord(mId, aId, date, _, _, sender, _, _, title, mn, _, _) =>
      s"$date:\n$sender\n*$title*\n/a${aId}${mn}$mId\n"
    case x => x.toString
  }
}
