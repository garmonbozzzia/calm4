package org.calm4.quotes

import scala.concurrent.Future
import Calm4._

case class Applicant_(id: String, name: String = "", familyName: String = "",
                      occupation: String, town: String, province: String)

case class ApplicantRecord_(appId: String, link: String)

case class Course(startsAt: String, endsAt: String, link: String){
  def id = link.split("/")(6)
  def appRecords: Future[List[ApplicantRecord_]] =
    getAppUrls(link).map(_.map(x => ApplicantRecord_("", host + x)))
  val filename = s"data/courses/$id.html"
  val url = s"$host/ru/$id"
  def save = savePage(link, s"data/courses/$id.html")
  def read = browser.parseFile("")
  def load = loadPage(link)
}

object Course {
  def apply(link: String, data: List[String]): Course = new Course(data(0), data(1), link)
  val all: List[Course] = courses
}
