package org.calm4

import org.calm4.CalmImplicits.browser
import org.calm4.CalmModel3.{CourseList, CourseRecord}
import org.calm4.quotes.CachedWithFile
import org.calm4.CalmModel3._
import scala.concurrent.Future
import Parsers._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import CalmImplicits._
import Utils._
import FastParse._

trait Courses {
  def list: Future[CourseList] =
    CachedWithFile.getDataJson(GetCourseList())
      .map(_.map(parseCourseRecord).flatten)
      .map(actual)

  private def parseCourseRecord: Seq[String] => Option[CourseRecord] = {
    case Seq(htmlStart, end, cType, venue, _, status, registrars, _, _, _, _) =>
      val html = browser.parseString(htmlStart)
      for {
        href <- html >?> attr("href")("a")
        id <- courseIdParser.fastParse(href)
      } yield CourseRecord( id, html >> text, end, cType, venue, status)
    case x => x.trace; throw new Exception("error")
  }

  private def actual(courses: Seq[CourseRecord]) = CourseList( courses
    .filter(x => Seq("10-Day", "1-DayOSC", "3-DayOSC").contains(x.cType))
    .filter(x => Seq("In Progress", "Tentative", "Scheduled" ).contains(x.status))
    .filter(_.cId != 0) )

}
