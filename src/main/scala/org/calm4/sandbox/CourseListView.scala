package org.calm4.sandbox

import org.calm4.CalmModel3._

/**
  * Created by yuri on 14.09.17.
  */
trait CalmView {
  //this: CalmModel3.CourseList =>
  def console: String = this match {
    case CourseList(courses) =>
      courses.zipWithIndex
        .map{ case(x,i) => s"$i\t${x.cId} ${x.start} ${x.end} ${x.cType}\t ${x.status}\t ${x.venue}"}
        .mkString("\n")
    case CourseId(cId) => ???
  }
}
