package org.calm4.model

import org.calm4.{TmCommand, TmMessage}



object CalmModel3 {
  object Courses extends Courses
  case class InboxRecord(cId: Int, aId: Int, mType: String, received: String) extends Applicant with TmMessage
  object Inbox extends Inbox
  case class CourseId(cId: Int) extends Course
  case class ApplicantId(aId: Int) extends Applicant
  case class ApplicantIdF(aId: Int, cId: Int) extends Applicant
  case class MessageId(mId: Int, aId: Int) extends Message

  case class CourseRecord(cId: Int, start: String, end: String,
                          cType: String, venue: String, status: String) extends Course with TmMessage
  case class CourseList(courses: Seq[CourseRecord]) extends TmMessage
  case class CourseInfo(cId: Int, venue: String, startDate: String, endDate: String) extends Course
  case class CourseData(courseInfo: CourseInfo, applicants: Seq[ApplicantRecord]) extends TmMessage
  case class ApplicantRecord(aId: Int, cId: Int, displayId: String, state: String,
                             givenName: String,familyName: String, sat: Int, served: Int,
                             age: Int, ons: String, gender: String, pregnant: Boolean) extends Applicant with TmMessage
  case class ConversationList(aId: Int, messages: List[MessageRecord])
  case class MessageRecord(mId: Int, aId: Int, date: String, d1: Int, d2: Int, sender: String,
                           emailOrForm: String, received: String, title: String, msgType: String,
                           unknown: String, inbox: Boolean) extends TmMessage
  case class MessageData( mId: Int, //"id": 1363066,
                          createdAt: String, //"created_at": "2017-08-29 11:36:13 UTC",
                          updatedAt: String, //"updated_at": "2017-08-30 14:47:12 UTC",
                          sentAt: String, //"sent_at": "2017-08-29 11:36:13 UTC",
                          operator: String, //"operator": "applicant",
                          deliveryMethod: String, //"delivery_method": "email",
                          status: String, //"status": "applicant_sent",
                          deliveryStatus: Option[String], //"delivery_status": null,
                          recipient: String, //"recipient": "registrars",
                          from: String, //"from": "frau.nata95@mail.ru",
                          replyTo: Option[String], //"reply_to": null,
                          subject: String, //"subject": "Re: Your Vipassana Course - b9LqGO",
                          inbox: Boolean, //"inbox": false,
                          name: String, //"name": "Email Reply",
                          description: String, //"description": "Applicant replied to message via email",
                          messageType: String, //"type": "Message",
                          removedFromInbox: String, //"removed_from_inbox": "2017-08-30 14:47:12 UTC",
                          important: Boolean,//"important": false,
                          text: String

                          //"language_code": null,
                          //"requires_student_response": false,
                          //"requires_internal_response": false,
                          //"letter_tag": null,
                          //"referral_action_on_send": null,
                          //"action_on_send": "",
                          //"conversation_key": "6a17edb5949a55b",
                          //"value": "",
                          //"incoming": null,
                          //"status_vo": {
                          //"value": "applicant_sent"
                          //},
                          //"delivery_status_vo": null,
                          //"deactivated_lts": [],
                          //"associated_lts": [],
                          //"activated_lts": [],
                          //"currently_active_lts": [],
                          //"sms_country": null,
                          //"sms_national_nbr": null,
                          //"attachments": [],
                        ) extends TmMessage

  trait CalmRequest
  case class GetCourseList() extends CalmRequest // /courses
  case class GetInbox(start: Int = 0) extends CalmRequest // inbox
  case class GetCourse(id: Int ) extends CalmRequest // c2545 c2545[_fm][_nos][_mwi]
  case class GetParticipant(id: Int, courseId: Int ) extends CalmRequest // /c2545a165453
  case class GetConversation(participantId: Int) extends CalmRequest // a165453m
  case class GetMessage(id: Int , participantId: Int ) extends CalmRequest // a165453m8767865
  case class GetNote(nId: Int, aId: Int) extends CalmRequest // a165453n8767865
  case class GetReflist(participantId: Int) extends CalmRequest //a165453r
  case class GetSearchResult(search: String) extends CalmRequest

  case class AllCoursesTm() extends TmCommand
  case class CourseTm(cId: Int) extends TmCommand
  case class CourseListTm(cId: Int) extends TmCommand
  //case class FilteredCourseTm(id: Int, g: Some[Boolean],  )
  case class ApplicantTm(cId: Int, aId: Int ) extends TmCommand
  case class ReflistTm(aId: Int) extends TmCommand
  case class MessagesTm(aId: Int) extends TmCommand
  case class MessageTm(aId: Int, mId: Int) extends TmCommand
  case class NoteTm(aId: Int, nId: Int) extends TmCommand
  case class InboxTm() extends TmCommand
  case class UndefinedTm(cmd: String) extends TmCommand
  case class WatchTm(cId: Int) extends TmCommand
}
