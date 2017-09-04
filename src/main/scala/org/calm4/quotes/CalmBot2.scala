package org.calm4.quotes

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Sink, Source}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models.Message
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData}
import org.calm4.quotes.DiffChecker._

import scalaz.Scalaz._
import scala.concurrent.Future

object CalmBot2 extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token: String = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  def replyMarkup(courseData: CourseData) = None

  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd
  implicit class ToTm(data: Diff){
    def view: String = data match {
      case ApplicationAdded(appId, courseId) => s"New: /a_${courseId}_$appId"
      case StateChanged(oldState, newState, appId, courseId) => s"Changed: $oldState -> $newState /a$appId /c$courseId"
    }
  }

  def courseMessage(courseData: CourseData)(implicit msg: Message) = {
    val responces = courseData.all.sorted.toSeq
      .map(x => ApplicantJsonRecordTm(x).view1)
      .grouped(25)
      .map(_.mkString("\n"))
      .map(reply(_, parseMode = Some(ParseMode.Markdown), replyMarkup = replyMarkup(courseData)))
    Future.sequence(responces)
  }

  def text(courseData: CourseData): String =
    courseData.sitting.female.`new`.sorted
    .map(x=>ApplicantJsonRecordTm(x).view1).mkString("\n")

  import scala.concurrent.duration._
  onCommand('inbox) { implicit msg =>
    DiffChecker.source(Seq(2526,2532,2481,2537,2534,2330), 5 minutes)
      .runForeach(x => if(x.nonEmpty) reply(x.map(_.view).mkString("\n")))
  }

  import org.calm4.quotes.CommandParser._
  import Utils._
  import CachedWithFile._
  onMessage{ implicit msg =>
    for( msgText <- msg.text )
    CommandParser.parse(msgText).trace match {
      case AllCoursesTm() => reply(
        Seq(2526,2532,2481,2537,2534,2330).map(id => s"/c$id").mkString("\n"))
      case CourseTm(cId) =>
        Source.fromFuture(get[CourseData](GetCourse(cId.trace)).map(_.traceWith(_.start_date)))
          .map(_.all.trace.grouped(25).to[scala.collection.immutable.Seq])
          .mapConcat[Seq[ApplicantJsonRecord]](x => x)
          .map(_.map(x => ApplicantJsonRecordTm(x).view1.trace).mkString("\n"))
          .mapAsync(1)(reply(_, parseMode = Some(ParseMode.Markdown)))
          .throttle(1,1.5 seconds, 1, ThrottleMode.Shaping)
          .runWith(Sink.ignore)
//        for{
//          courseData <- get[CourseData](GetCourse(cId))
//        res <- courseMessage(courseData)} yield res
      case ApplicantTm(aId,cId) => ???
      case ReflistTm(aId) => ???
      case ConversationTm(aId) =>
      case MessagesTm(aId) => get(GetConversation(aId))
      case MessageTm(aId,mId) => get(GetMessage(mId,aId))
      case UndefinedTm(cmd) =>
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
