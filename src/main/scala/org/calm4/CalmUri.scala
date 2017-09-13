package org.calm4

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.{Path, Query}
import org.calm4.Utils._
import org.calm4.quotes.CalmModel._

/**
  * Created by yuri on 26.08.17.
  */
object CalmUri {
  type Id = Int
  private val ks = Seq("[data]", "[name]", "[searchable]", "[orderable]", "[search][value]", "[search][regex]")
  private val vs = Seq("", true, true, "", false).map(_.toString)

  def uri: CalmRequest => Uri = {
    case GetCourseList() => coursesUri()
    case GetInbox() => inboxUri
    case GetCourse(id) => courseUri(id)
    case GetParticipant(id, courseId) => applicationUri(id,courseId)
    case GetConversation(participantId) => conversationUri(participantId)
    case GetMessage(id, participantId ) => messageUri(id, participantId).trace
    case GetNote(id, participantId ) => noteUri(id, participantId).trace
    case GetReflist(participantId) => reflistUri(participantId)
    case GetSearchResult(s) => searchUri(s)
  }

  private def columnParams(n: Int) = for{
    i <- (0 to n).toList
    (x,y) <- ks.zip(i.toString +: vs)
  } yield s"columns[$i]$x" -> y

  val host = Uri("https://calm.dhamma.org")
  implicit def seq2query(seq: Seq[(String, String)]): Uri.Query = Uri.Query(seq.toMap)
  implicit def string2Path(str: String): Path = Path(str)

  def searchUri(s: String): Uri = host.withPath("/en/course_applications/search").withQuery(Query("typeahead" -> s))
  def messageUri(msgId: Int, appId: Id): Uri = host.withPath(s"/en/course_applications/$appId/messages/$msgId")
  def noteUri(msgId: Int, appId: Id): Uri = host.withPath(s"/en/course_applications/$appId/notes/$msgId")

  def applicationUri(appId: Id, courseId: Id): Uri =
    host.withPath(s"/en/courses/$courseId/course_applications/$appId/edit")

  def reflistUri(appId: Id): Uri = host
    .withPath(s"/en/course_application/$appId/course_application_load_rl")
    .withQuery(columnParams(9) ++ Seq(
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "100",
      "search[value]" -> "",
      "search[regex]" -> "false"
    ))

  def courseUri(id: Int): Uri = host.withPath(s"/en/courses/$id/course_applications")

  lazy val inboxUri: Uri =
    host.withPath("/en/course_applications/inbox").withQuery(columnParams(8) ++ Seq(
      "draw" -> "1",
      "order[0][column]" -> "1",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "100",
      "search[value]" -> "",
      "search[regex]" -> "false",
      "user_custom_search[filterOnMyApplicationsOnly]" -> "false",
      "user_custom_search[length]" -> "100",
      "user_custom_search[start]" -> "0"
    )
  )

  def coursesUri(startDate: String = "2017-8-01"): Uri =
    host.withPath("/en/courses").withQuery(columnParams(10) ++ Seq (
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "500",
      "search[value]" -> "",
      "search[regex]" -> "false",
      "user_custom_search[length]" -> "100",
      "user_custom_search[start]" -> "0",
      "user_custom_search[operator_start_date]" -> "gte_date",
    //  "user_custom_search[criterion_start_date]" -> startDate,
      "user_custom_search[operator_course_type_id]" -> "eq",
      "user_custom_search[filterOnMyCoursesOnly]" -> "false",
      "user_custom_search[defaultCurrentDate]" -> "true",
      "user_custom_search[context]" -> "all_courses"
      )).trace

  def conversationUri(appId: Id): Uri = host.withPath(s"/en/course_applications/$appId/conversation_datatable")
    .withQuery( columnParams(8) ++ Seq(
        "order[0][column]" -> "0",
        "order[0][dir]" -> "asc",
        "start" -> "0",
        "length" -> "-1",
        "search[value]" -> "",
        "search[regex]" -> "false"
      )
    )
}

