package org.calm4.quotes

import java.lang.Exception

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Sink, Source}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands, InlineQueries}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.ParseMode
import info.mukel.telegrambot4s.models._
import org.calm4.quotes.CalmModel._
import org.calm4.quotes.CalmModel2._
import org.calm4.quotes.DiffChecker._
import org.calm4.quotes.Parsers._
import org.calm4.CommandParser._
import org.calm4.Utils._
import CachedWithFile._
import Calm4._
import CalmModel._
import org.calm4.{CalmModel3, CalmUri, CommandParser, TmSymbolMap}
import org.calm4.CalmModel3._

import scala.concurrent.Future

//object CalmModel3 {
//  case class Course(cId: Int){
//    def courseData: Future[CourseData] = get[CourseData](GetCourse(cId))
//  }
//  //Course(cId).data todo
//
//  //Applicant(aId).conversation todo
//  def conversation(aId: Int) = get[ConversationJsonData](GetConversation(aId))
//    .map(ConversationData.json2data)
//
//  def messageData(mId: Int, aId: Int): Future[MessageJsonData] = get[MessageJsonData](GetMessage(mId, aId))
//}

object CalmBot2 extends TelegramBot
  with Polling
  with Commands
  with Callbacks
  with InlineQueries {
  def token: String = scala.io.Source.fromFile("data/BotToken").getLines().mkString

  import CalmModel2.CourseData
  import Parsers.CourseRecord
  def replyMarkup(courseData: CourseData) = None

  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

  implicit class TmView[T](val obj: T) extends AnyVal {
    def tmView: String = obj match {
      case ApplicationAdded(appId, courseId) => s"New: /c${courseId}a$appId"
      case StateChanged(oldState, newState, appId, courseId) =>
        s"/c${courseId}a$appId\n${TmSymbolMap.toTm(oldState)} => ${TmSymbolMap.toTm(newState)}"
      case ConversationData(aId, mId, date, _, _, _, _, _, msgType, mn) =>
        s"$date:\n*$msgType*\n/a${aId}${mn}$mId"
      case CourseRecord(id, start, end, cType, venue, status) =>
        s"""[$cType](${CalmUri.courseUri(id)}) _${venue}_
           |    $status /c$id
           |    🗓$start 🗓 $end
         """.stripMargin
      case Parsers.CourseList(records) =>
        records.zipWithIndex.map(x => s"*${x._2+1}. *${x._1.tmView}").mkString("\n")
    }

    def tmReply(implicit message: Message) = reply(tmView, parseMode = Some(ParseMode.Markdown))
  }

  onInlineQuery { implicit cbq =>
    cbq.trace
    load(GetSearchResult(cbq.query)).map { case CalmJson(json) =>
      json.data.map(x => SearchRecord(x))
    }
      .flatMap { result =>
        answerInlineQuery(
          result.zipWithIndex.map(record => InlineQueryResultArticle(
            record._2.toString,
            record._1.tmSearchHeader,
            inputMessageContent = InputTextMessageContent(record._1.tmMessage),
            description = Some(record._1.tmDescripiton))), switchPmText = Some("text"), switchPmParameter = Some("param")
        )
      }
  }


  def text(courseData: CourseData, cId: Int): String =
    courseData.sitting.female.`new`.sorted
      .map(x => ApplicantJsonRecordTm(x, cId).view1).mkString("\n")

  import scala.concurrent.duration._

  onCommand('sandbox) { implicit msg =>
    reply(Example.text, parseMode = Some(ParseMode.Markdown), replyMarkup = Some(Example.markup))
  }

  onCommand('start) { implicit msg =>
    msg.trace
    reply("Hello!", replyMarkup = Some(Example.startMarkup))
  }

  onCommand('inbox) { implicit msg =>
    for{ courses <- Calm4.courseList.map(_.courses.map(_.id))}
    //DiffChecker.source(Seq(2526, 2532, 2481, 2537, 2534, 2330, 2528, 2484), 10 minutes)
    DiffChecker.source(courses, 10 minutes)
      .map(_.to[scala.collection.immutable.Seq])
      .mapConcat[Diff](x => x)
      .throttle(1, 1.5 seconds, 1, ThrottleMode.Shaping)
      .mapAsync(1)(x => reply(x.tmView))
      .throttle(1, 1.5 seconds, 1, ThrottleMode.Shaping)
      .runWith(Sink.ignore)
  }

  onCallbackQuery{ implicit cbq => cbq.data.trace
    ackCallback()
  }

  onMessage { implicit msg =>
    for (msgText <- msg.text)
      CommandParser.parse(msgText).trace match {
        case AllCoursesTm() => val text = Calm4.courseList.map(_.tmReply)
        case CourseTm(cId) =>
          Source.fromFuture(get[CourseData](GetCourse(cId.trace)).map(_.traceWith(_.start_date)))
            //Course(cId).data todo
            .map(_.all.grouped(25).to[scala.collection.immutable.Seq])
            .mapConcat[Seq[ApplicantJsonRecord]](x => x)
            .map(_.map(x => ApplicantJsonRecordTm(x, cId).view1.trace).mkString("\n")     )
            .mapAsync(1)(reply(_, parseMode = Some(ParseMode.Markdown)))
            .throttle(1, 1.5 seconds, 1, ThrottleMode.Shaping)
            .runWith(Sink.ignore)
        //        for{
        //          courseData <- get[CourseData](GetCourse(cId))
        //        res <- courseMessage(courseData)} yield res
        case ApplicantTm(cId, aId) => get[CourseData](GetCourse(cId))
          //Course(cId).data todo
          .map(_.all.find(_.id == aId).trace
            .map(x => ApplicantJsonRecordTm(x, cId).view2.trace))
          .map(x => x.map(reply(_, parseMode = Some(ParseMode.Markdown))))

        case ReflistTm(aId) => ???
        case MessagesTm(aId) => get[ConversationJsonData](GetConversation(aId))
          //Applicant(aId).conversation todo
          .map(ConversationData.json2data)
          .flatMap(x => reply(x.map(_.tmView).mkString("\n\n"), parseMode = Some(ParseMode.Markdown)))
        case MessageTm(aId, mId) =>
          //Message(mId,aId).data todo
          get[MessageJsonData](GetMessage(mId, aId))
            .map(MessageData.json2data)
            .map(x => reply(x))
        case NoteTm(aId, nId) => get[MessageJsonData](GetNote(nId, aId))
          .map(MessageData.json2data)
          .map(x => reply(x))
        case UndefinedTm(cmd) =>
      }
  }
}



object CalmBot2App extends App {
  CalmBot2.run()
}

object Example {
  def text = "example message"
  def markup = InlineKeyboardMarkup.singleColumn(Seq(
    InlineKeyboardButton.switchInlineQueryCurrentChat("switchInlineCC", "2"),
    InlineKeyboardButton.switchInlineQuery("switchInline", "2"),
    InlineKeyboardButton.url("Google", "http://google.com"),
    InlineKeyboardButton.callbackData("pressMe", "exampleTag")))
  //def markup2 = InlineKeyboardMarkup.singleButton(InlineKeyboardButton.callbackData("pressMe", "exampleTag"))
  def markup1 = ReplyKeyboardMarkup.singleButton(KeyboardButton("a"), resizeKeyboard = Some(true), selective = Some(false))
  def startMarkup = ReplyKeyboardMarkup.singleRow( Seq(KeyboardButton("/courses"), KeyboardButton("/help"), KeyboardButton("/sandbox"), KeyboardButton("/start")), resizeKeyboard = Some(true))
}

