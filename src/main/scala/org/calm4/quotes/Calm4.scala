package org.calm4.quotes

import org.calm4.quotes.CalmModel.GetCourseList
import org.calm4.CalmImplicits._


/**
  * Created by yuri on 07.09.17.
  */
object Calm4 {
  def courseList = CachedWithFile.getDataJson(GetCourseList())
    .map(_.map(x => Parsers.CourseRecord(x)))
    .map(Parsers.CourseList(_).actual)
}
