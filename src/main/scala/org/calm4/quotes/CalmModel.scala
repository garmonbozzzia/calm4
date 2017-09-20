package org.calm4.quotes

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, text}
import org.calm4.quotes.Calm4Http._
import org.calm4.core.CalmImplicits._
import org.calm4.{CalmUri}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.calm4.model.CalmModel3._

import scala.concurrent.Future



object CalmModel {

  case class Participant(id: Int, courseId: Int)
  case class Course(id: Int)


  trait CalmResponse
  case class CalmHtml(document: Document) extends CalmResponse
  case class CalmJson(json: DataJson) extends CalmResponse
  case class CalmRawJson(json: String) extends CalmResponse
  case class SearchResult(data: List[SearchRecord]) extends CalmResponse

  case class ParseError(msg: String)

  object SearchRecord{
    def apply(implicit data: List[String]) = new SearchRecord(data(1), data(2), data(4), data(8))
  }
  case class SearchRecord(givenName: String, familyName: String, birthDate: String, status: String )(implicit data: List[String]) {
    lazy val tmSearchHeader = s"${givenName} ${familyName}"
    lazy val tmDescripiton = s"$birthDate\n$status"
    lazy val tmMessage = browser.parseString(data(0)) >> attr("href")("a")
  }

  object InboxRecord{
    import fastparse.all._
    val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
    val participantParser = P("https://calm.dhamma.org".? ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
      .map(x => Participant(x._1, x._2))
    val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~ "/messages/" ~ id)
    def extractId(data: String): Option[Participant] = {
      participantParser.parse(data) match {
        case Parsed.Success(r, _) => Some(r)
        case _ => None
      }
    }
  }

  class InboxRecord(data: List[String]) {
    private lazy val d0 = data.lift(0).map(browser.parseString)
    lazy val link: Option[String] = d0 >> attr("href")("a")
    lazy val participant = link.flatMap(InboxRecord.extractId)
    lazy val messageType = d0 >> text("a")
    lazy val received = data.lift(1)
    lazy val name = data.lift(2)
    lazy val gender = data.lift(3)
    lazy val participationType = data.lift(4).map(browser.parseString) >> text
    lazy val courseStart = data.lift(5)
    lazy val courseEnd = data.lift(6)
    lazy val venue = data.lift(7)
    lazy val language = data.lift(8)
  }

  case class DataJson(data: List[List[String]])
  def load: CalmRequest => Future[CalmResponse] = {
    case GetCourse(id) => loadPage(s"https://calm.dhamma.org/en/courses/$id/course_applications").map(CalmHtml)
    case GetParticipant(id, courseId) =>
      loadPage(s"https://calm.dhamma.org/en/courses/$courseId/course_applications/$id/edit").map(CalmHtml)
    case GetInbox(s) => loadJson(CalmUri.inboxUri(s)).map(parse(_).extract[DataJson]).map(CalmJson)
    case GetSearchResult(s) =>
      loadJson(Uri("https://calm.dhamma.org/en/course_applications/search").withQuery(Query(Map("typeahead" -> s))))
        .map(parse(_).extract[DataJson]).map(CalmJson)
  }
}

import org.calm4.quotes.CalmModel._
import org.calm4.core.Utils._
object CalmSearchTest extends App {
  load(GetSearchResult("B"))
    .map(_.trace)
  //load(GetInbox()).map(_.trace)
  //  loadJson(
  //    Uri("https://calm.dhamma.org/en/course_applications/search").withQuery(Query(Map("typeahead" -> "Bra"))),
  //    TestSearchUri.hs
  //  ).map(_.trace)
}


object CalmModelTest extends App {
  //load(GetCourse(2479.toString)).onComplete(_.trace)

  val l1 = browser.parseString("<a class='category-link' href='/en/courses/2527/course_applications/169275/edit#ref_list'>New</a>")
  val res: String = l1 >> attr("href")("a")
  val res2 = l1 >> text("a")
  res.trace
  res2.trace

  val p1 = List(
    "<a class='category-link' href='/en/courses/2535/course_applications/168127/edit#ref_list'>New</a>",
    "2017-08-19",
    "Артем Микрюков",
    "M",
    "<span title='new student'>NEW<span>",
    "2017-10-11",
    "2017-10-22",
    "Saint Petersburg",
    "Russian"
  )
}

