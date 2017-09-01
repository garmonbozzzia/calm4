package org.calm4.quotes

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{ApplicantRecord, CourseData}



object CalmBot2 extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token = "418829147:AAHvnI1_RePHOrYSovqO7zMzOad2wENwwT4"

  def tmLink: ApplicantRecord => String = {
    case x => s"""[/a${x.id}](/a${x.id})"""
  }

  object ApplicantRecordTm{
    def apply: ApplicantRecord => ApplicantRecordTm = {
      case ApplicantRecord(id, _, gName, fName, age, sit, old, _, _, ad_hoc, pregnant, sat, served, _, _, _, state ) =>
        new ApplicantRecordTm(s"/a$id", fName, gName,
          sit.fold("❌")(if(_) old.fold("❌")(if(_) "🎓" else "") else "⭐"),
          state,
          TmSymbolMap.toTm.getOrElse(state,"❓"),
          age.getOrElse(0),
          old.map(_ => sat.getOrElse(0) -> served.getOrElse(0))
        )
    }
  }
  case class ApplicantRecordTm(id: String, familyName: String, givenName: String, nos: String, longStatus: String,
                               shortStatus: String, age: Int, sitAndServed: Option[(Int,Int)] ) {
    val view1 = s"$id $shortStatus *$familyName $givenName*"
    val view2 =
      s"*$familyName $givenName* _Age:_ $age\n$shortStatus $longStatus${sitAndServed.fold(""){case (x,y) => s"\n$nos Sit: $x Served: $y"}}"

  }

  def replyMarkup(courseData: CourseData) = None



  implicit val ord: Ordering[ApplicantRecord] = ApplicantRecordOrd
  def text(courseData: CourseData) = courseData.sitting.female.old.sorted
    .map(x=>ApplicantRecordTm(x).view1).mkString("\n")
  onCommand('c2535) { implicit msg =>
    for{
      courseData <- CachedWithFile.get[CourseData](GetCourse(2535), ???)
    } yield {
      reply(
        text(courseData),
        parseMode = Some(ParseMode.Markdown),
        replyMarkup = replyMarkup(courseData)
      )
    }
  }

  onMessage{ implicit msg =>
    val course = CachedWithFile.get[CourseData](GetCourse(2535), ???)
    msg.text.flatMap(Parsers.tmAppId).map{
      id => course.map(_.all.find(_.id == id).get)
    }.map(
    _.flatMap{
      x => reply(ApplicantRecordTm(x).view2, parseMode = Some(ParseMode.Markdown))
    })
  }
}

object CalmBot2App extends App {
  CalmBot2.run()
}
