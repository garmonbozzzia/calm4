package org.calm4.quotes

import org.calm4.quotes.CalmModel.GetCourseList
import CalmImplicits._


/**
  * Created by yuri on 07.09.17.
  */
object Calm4 {
  def courseList = CachedWithFile.getJson(GetCourseList())
    .map(_.map(x => Parsers.CourseRecord(x)))
    .map(Parsers.CourseList(_).actual)
}
