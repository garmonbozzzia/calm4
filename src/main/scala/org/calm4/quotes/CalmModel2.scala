package org.calm4.quotes

import org.calm4.quotes.CalmModel.{CalmResponse, Id}

import scala.concurrent.Future

/**
  * Created by yuri on 01.09.17.
  */
object CalmModel2{
  type Id = Int

  case object Inbox {
    def data: Future[CalmResponse] = ???
    def messages: Seq[MessageRecord] = ???
  }

  case class MessageRecord(data: List[String]){
    lazy val conversationData: Future[CalmResponse] = ???
    lazy val message: Future[Message] = ???
  }
  case class CourseList() {
    lazy val courses: List[Course] = ???
  }
  case class CourseRecord()
  case class Course(id: Id) {

  }
  case class Conversation(participant: Participant){
    lazy val messages: Future[Seq[Message]] = ???
  }
  case class ParticipantRecord()
  case class Participant(id: Id, courseId: Id )
  case class Message(id: Id, participant: Participant) {
    lazy val conversationData: Future[CalmResponse] = ???
    lazy val messageRecordData: Future[List[String]] = ???
    lazy val messageData: Future[CalmResponse] = ???
    lazy val text: Future[String] = ???
    lazy val details: Future[String] = ???
  }

  case class ApplicantRecord(id: Id,
                             display_id: String,
                             applicant_given_name: String,
                             applicant_family_name: String,
                             age: Option[Int],
                             sitting: Option[Boolean],
                             old: Option[Boolean],
                             conversation_locale: String,
                             language_native: String,
                             ad_hoc: String,
                             pregnant: Option[Boolean],
                             courses_sat: Option[Int],
                             courses_served: Option[Int],
                             room: String,
                             generated_hall_position: String,
                             hall_position: String,
                             confirmation_state_name: String
                      )

  case class OldNew(old: List[ApplicantRecord], `new`: List[ApplicantRecord])
  case class MaleFemaleSittings(male: OldNew, female: OldNew)
  case class MaleFemaleServing(male: List[ApplicantRecord], female: List[ApplicantRecord])
  case class CourseData(course_id: Id,
                        venue_name: String,
                        start_date: String,
                        end_date: String,
                        user_can_assign_hall_position: Boolean,
                        sitting: MaleFemaleSittings,
                        serving: MaleFemaleServing){
    def diff(other: CourseData) = ???
    lazy val all =
      serving.female ++
        serving.male ++
        sitting.male.old ++
        sitting.male.`new` ++
        sitting.female.`new`++
        sitting.female.old
  }

}