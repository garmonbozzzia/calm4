package org.calm4.quotes

import scala.concurrent.Future

/**
  * Created by yuri on 23.07.17.
  */

import Calm4._

case class Applicant(id: String, name: String = "", familyName: String = "",
                     occupation: String, town: String, province: String)

case class ApplicantRecord(appId: String, link: String)

case class Course(startsAt: String, endsAt: String, link: String){
  def id = link.split("/")(6)
  def appRecords: Future[List[ApplicantRecord]] =
    getAppUrls(link).map(_.map(x => ApplicantRecord("", host + x)))
  val filename = s"data/courses/$id.html"
  val url = s"$host/ru/$id"
  def save = savePage(link, s"data/courses/$id.html")
  def read = browser.parseFile("")
  def load = loadPage(link)
}

/**
  * Created by yuri on 22.07.17.
  */

object Course {
  def apply(link: String, data: List[String]): Course = new Course(data(0), data(1), link)
  val all: List[Course] = courses
}

//AppSearchRequest =>
//
//CourseListRequest => List[CourseRecord(courseApplicationsRequest , id, startDate, endDate, courseType, venue, id, status, registrars)]
//courseApplicationsRequest => List[ApplicationRecord()]