package org.calm4.quotes

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData}
import org.calm4.quotes.DiffChecker._

object CalmBot2 extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  def replyMarkup(courseData: CourseData) = None

  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd


  implicit class ToTm(data: Diff){
    def view: String = data match {
      case ApplicationAdded(appId, courseId) => s"New: /a_${courseId}_$appId"
      case StateChanged(oldState, newState, appId, courseId) => ???
    }
  }

  def text(courseData: CourseData) =
  courseData.sitting.female.`new`.sorted
  //courseData.all.sorted
    .map(x=>ApplicantJsonRecordTm(x).view1).mkString("\n")
  import scala.concurrent.duration._
  onCommand('inbox) { implicit msg =>
    DiffChecker.source(Seq(2526,2532,2481,2537,2534,2330), 10 minutes)
      .runForeach(x => if(x.nonEmpty) reply(x.map(_.view)mkString("\n")))
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
      x => reply(ApplicantJsonRecordTm(x).view2, parseMode = Some(ParseMode.Markdown))
    })
  }
}

object CalmBot2App extends App {
  CalmBot2.run()
}
