package org.calm4.quotes

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData}



object CalmBot2 extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token = scala.io.Source.fromFile("data/BotToken").getLines().mkString


  object ApplicantRecordTm{
    def apply: ApplicantJsonRecord => ApplicantRecordTm = {
      case ApplicantJsonRecord(id, _, gName, fName, age, sit, old, _, _, ad_hoc, pregnant, sat, served, _, _, _, state ) =>
        new ApplicantRecordTm(id,
          fName, gName,
          //sit.fold("❌")(if(_) old.fold("❌")(if(_) "🎓" else "") else "⭐"),
          if(sit) if(old) "🎓" else "" else "⭐",
          state,
          TmSymbolMap.toTm.getOrElse(state,"❓"),
          age.getOrElse(0),
          //old.flatMap(if(_) Some(sat.getOrElse(0) -> served.getOrElse(0)) else None)
          if(old) Some(sat.getOrElse(0) -> served.getOrElse(0)) else None
        )
    }
  }
  case class ApplicantRecordTm(id: Int, familyName: String, givenName: String, nos: String, longStatus: String,
                               shortStatus: String, age: Int, sitAndServed: Option[(Int,Int)] ) {
    val view1 = s"/a$id $shortStatus *$familyName $givenName*"
    val view2 =
      s"*$familyName $givenName* _Age:_ $age\n$shortStatus $longStatus${sitAndServed.fold(""){case (x,y) => s"\n$nos Sit: $x Served: $y"}}"

  }

  def replyMarkup(courseData: CourseData) = None



  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd
  def text(courseData: CourseData) =
  courseData.sitting.female.`new`.sorted
  //courseData.all.sorted
    .map(x=>ApplicantRecordTm(x).view1).mkString("\n")
  onCommand('inbox) { implicit msg =>
    DiffChecker.source(Seq(2526,2532,2481,2537,2534,2330)).runForeach(x => if(x.nonEmpty) reply(x.mkString("\n")))
  }
  onCommand('c2535) { implicit msg =>
    for{
      courseData <- CachedWithFile.get[CourseData](GetCourse(2535))
    } yield {
      reply(
        text(courseData),
        parseMode = Some(ParseMode.Markdown),
        replyMarkup = replyMarkup(courseData)
      )
    }
  }

  onMessage{ implicit msg =>
    val course = CachedWithFile.get[CourseData](GetCourse(2535))
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
