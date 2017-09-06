package org.calm4.quotes

import org.calm4.quotes.CalmModel.CalmResponse
import scala.concurrent.Future
import Utils._
import CalmImplicits._

/**
  * Created by yuri on 01.09.17.
  */
object CalmModel2 {
  type Id = Int

  case object Inbox {
    def data: Future[CalmResponse] = ???

    def messages: Seq[MessageRecord] = ???
  }

  case class MessageRecord(data: List[String]) {
    lazy val conversationData: Future[CalmResponse] = ???
    lazy val message: Future[Message] = ???
  }

  case class CourseList() {
    lazy val courses: List[Course] = ???
  }


  case class Course(id: Id) {

  }

  case class Conversation(participant: Participant) {
    lazy val messages: Future[Seq[Message]] = ???
  }

  case class ParticipantRecord()

  case class Participant(id: Id, courseId: Id)

  case class Message(id: Id, participant: Participant) {
    lazy val conversationData: Future[CalmResponse] = ???
    lazy val messageRecordData: Future[List[String]] = ???
    lazy val messageData: Future[CalmResponse] = ???
    lazy val text: Future[String] = ???
    lazy val details: Future[String] = ???
  }


  object ApplicantRecord {
    def apply: ApplicantJsonRecord => ApplicantRecord = {
      case ApplicantJsonRecord(id, display_id, applicant_given_name, applicant_family_name,
      age, sitting, old, _, _, ad_hoc, pregnant, courses_sat, courses_served, _, _, _, state) => ???
    }
  }

  case class ApplicantRecord(id: Id, displayId: String, givenName: String, familyName: String,
                             age: Int, old: Boolean, server: Boolean, pregnant: Boolean)

  case class ApplicantJsonRecord(id: Id, display_id: String, applicant_given_name: String, applicant_family_name: String,
                                 age: Option[Int], sitting: Boolean, old: Boolean, conversation_locale: String,
                                 language_native: String, ad_hoc: String, pregnant: Boolean, courses_sat: Option[Int],
                                 courses_served: Option[Int], room: String, generated_hall_position: String,
                                 hall_position: String, confirmation_state_name: String)

  case class OldNew(old: Seq[ApplicantJsonRecord], `new`: Seq[ApplicantJsonRecord])

  case class MaleFemaleSittings(male: OldNew, female: OldNew)

  case class MaleFemaleServing(male: Seq[ApplicantJsonRecord], female: Seq[ApplicantJsonRecord])

  case class CourseData(course_id: Id, venue_name: String, start_date: String, end_date: String,
                        user_can_assign_hall_position: Boolean, sitting: MaleFemaleSittings,
                        serving: MaleFemaleServing) {
    //добавить сортировку TODO
    implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd
    lazy val all = sitting.male.`new`.sorted ++
      sitting.male.old.sorted ++
      sitting.female.`new`.sorted ++
      sitting.female.old.sorted ++
      serving.male.sorted ++
      serving.female.sorted
  }

}

object DiffTest extends App {
  DiffChecker.source(Seq(2535)).runForeach(_.trace)
}

