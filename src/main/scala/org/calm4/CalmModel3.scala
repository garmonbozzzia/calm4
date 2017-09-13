package org.calm4

import org.calm4.CalmImplicits._
import org.calm4.Utils._

// имеем исходный запрос => jnghfdkztv pfghjc => получаем строку json/html => из строки парсим структуру данных => получаем набор объектов модели
// => из структуры данных можем сформировать новые запросы
// команда телеграма формирует исходный объект например /c2453a165453
// из него можно получить объект /c2453 /a165453m /a165453r
// каждый объект может быть детализирован(как это реализовать?) в виде trait

object CalmModel3 extends CalmModelTraits {
  object Courses extends Courses
  case class CourseId(val cId: Int) extends Course
  case class ApplicantId(aId: Int) extends ApplicantIdT
  case class ApplicantIdF(aId: ApplicantId, cId: CourseId) extends Applicant2IdT
  case class MessageId(mId: Int, aId: ApplicantIdT) extends MessageIdT
  case class CourseRecord(cId: CourseId, start: String, end: String,
                          cType: String, venue: String, status: String)
  case class CourseList(courses: Seq[CourseRecord])
  case class CourseInfo(val courseId: Int, venue: String, startDate: String, endDate: String)
  case class CourseData(courseInfo: CourseInfo, applicants: Seq[ApplicantRecord])
  case class ApplicantRecord(aId: Int, cId: Int, displayId: String, state: String, givenName: String,
                             familyName: String, sat: Int, served: Int, age: Int, ons: String,
                             gender: String, pregnant: Boolean)
  case class ConversationList(applicant: ApplicantId, messages: List[MessageRecord])
  case class MessageRecord(mId: Int, aId: ApplicantId, date: String,
                           d1: Int, d2: Int, applicant: String,
                           email: String, received: String, msgType: String,
                           urlType: String = "messages") extends MessageIdT
  case class InboxRecord()
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