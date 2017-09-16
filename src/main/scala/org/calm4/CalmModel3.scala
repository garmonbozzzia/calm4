package org.calm4

import org.calm4.CalmImplicits._
import org.calm4.Utils._
import org.calm4.sandbox.CalmView

// имеем исходный запрос => jnghfdkztv pfghjc => получаем строку json/html => из строки парсим структуру данных => получаем набор объектов модели
// => из структуры данных можем сформировать новые запросы
// команда телеграма формирует исходный объект например /c2453a165453
// из него можно получить объект /c2453 /a165453m /a165453r
// каждый объект может быть детализирован(как это реализовать?) в виде trait

object CalmModel3 {
  object Courses extends Courses
  case class CourseId(cId: Int) extends Course
  case class ApplicantId(aId: Int) extends Messages
  case class ApplicantIdF(aId: Int, cId: Int) extends Messages
  case class MessageId(mId: Int, aId: Int)

  case class CourseRecord(cId: Int, start: String, end: String,
                          cType: String, venue: String, status: String)
  case class CourseList(courses: Seq[CourseRecord])
  case class CourseInfo(courseId: Int, venue: String, startDate: String, endDate: String)
  case class CourseData(courseInfo: CourseInfo, applicants: Seq[ApplicantRecord])
  case class ApplicantRecord(aId: Int, cId: Int, displayId: String, state: String,
                             givenName: String,familyName: String, sat: Int, served: Int,
                             age: Int, ons: String, gender: String, pregnant: Boolean) extends Messages
  case class ConversationList(aId: Int, messages: List[MessageRecord])
  case class MessageRecord(mId: Int, aId: Int, date: String, d1: Int, d2: Int, applicant: String,
                           email: String, received: String, msgType: String,urlType: String = "messages")
  case class InboxRecord()

  trait CalmRequest
  case class GetCourseList() extends CalmRequest // /courses
  case class GetInbox() extends CalmRequest // inbox
  case class GetCourse(id: Int ) extends CalmRequest // c2545 c2545[_fm][_nos][_mwi]
  case class GetParticipant(id: Int, courseId: Int ) extends CalmRequest // /c2545a165453
  case class GetConversation(participantId: Int) extends CalmRequest // a165453m
  case class GetMessage(id: Int , participantId: Int ) extends CalmRequest // a165453m8767865
  case class GetNote(nId: Int, aId: Int) extends CalmRequest // a165453n8767865
  case class GetReflist(participantId: Int) extends CalmRequest //a165453r
  case class GetSearchResult(search: String) extends CalmRequest

  case class AllCoursesTm() extends TmCommand
  case class CourseTm(cId: Int) extends TmCommand
  //case class FilteredCourseTm(id: Int, g: Some[Boolean],  )
  case class ApplicantTm(cId: Int, aId: Int ) extends TmCommand
  case class ReflistTm(aId: Int) extends TmCommand
  case class MessagesTm(aId: Int) extends TmCommand
  case class MessageTm(aId: Int, mId: Int) extends TmCommand
  case class NoteTm(aId: Int, nId: Int) extends TmCommand
  case class InboxTm() extends TmCommand
  case class UndefinedTm(cmd: String) extends TmCommand
}

//

// Courses =F> List[CourseRecord]
// CourseRecord => CourseId
// CourseId =F> (Info, List[ApplicantRecord])
// ApplicantRecord => ApplicantCId
// ApplicantRecord =F> (Info, List[MessageRecord])
// MessageRecord => MessageId
// MessageId =F> (Message)
// Inbox =F> List[InboxRecord]
// InboxRecord => ApplicantCId

//object Ids {
//  // c
//  // c2455
//  // c2455a235613
//  // c2455a235613m
//  //      a235613m3246886
//  // i
//}