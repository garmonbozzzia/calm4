package org.calm4.quotes

import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{attr, elementList}
import org.calm4.quotes.Calm4.browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
/**
  * Created by yuri on 03.09.17.
  */
trait Calm4Old {
  val host = "https://calm.dhamma.org"
  val actualStates = List("Finished", "In Progress", "Scheduled")
  def validate(data: List[String]) =
    data(2) == "10-Day" && actualStates.exists( _ == data(5))
  val coursesFile = "data/Registration.html"
  //  val courses: List[Course] = browser.parseFile(coursesFile) >> elementList(".colour-event-datatable-row")
  //    .map(x => (x >> attr("href")("a"))
  //      .zip(x >> elementList("td") >> allText)
  //      .collect{ case (x,y) if validate(y) => Course(x, y)}
  //    )
  val courses: List[Course] = ???

  def occupationExtractor(implicit html: Document): String = html >> attr("value")("input[id=course_application_occupation]")

  def familyNameExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_applicant_family_name]")
  def givenNameExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_applicant_given_name]")
  //def provinceExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_contact_province]")
  def townExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_contact_town]")
  def idExtractor(implicit html: Document) = html >> attr("value")("input[id=course_application_display_id]")
  def parseApplicant(implicit html: Document) = Applicant_(
    name = givenNameExtractor,
    familyName = familyNameExtractor,
    occupation = occupationExtractor,
    province = "provinceExtractor",
    town = townExtractor,
    id = idExtractor)

  def parseApplicantList(path: String): List[String] = parseApplicantList(browser.parseFile(path))
  def parseApplicantList(path: Document) = (path >> elementList("a[id=edit-app-link]")).map(_ >> attr("href") )

  def parseCourseList(path: String) : List[String] = parseCourseList(browser.parseFile(path))

  def parseCourseList(doc: Document): List[String] =
    (doc >> elementList (".colour-event-datatable-row a[id=start-date]")).map(_ >> attr("href"))

  def getAppId(url: String): Option[String] = url.split("/").lift(5)
}
