package org.calm4.sandbox

import org.calm4.model.CalmModel3._
import org.calm4.TmSymbolMap

/**
  * Created by yuri on 14.09.17.
  */
object CalmView {
  //this: CalmModel3.CourseList =>
  def console: Any => Any = {
    case CourseRecord(cId, start, end, cType,venue,status) =>
      f"$cId $start $end $cType%8s $status%11s $venue"
    case CourseList(courses) =>
      courses.zipWithIndex
        .map{ case(x,i) => f"$i%3d ${console(x)}"}
        .mkString("\n")
    case ApplicantRecord(aId, cId, displayId, state, givenName, familyName, sat, served, age, ons, gender, pregnant) =>
      f"$ons$gender $aId $age%2s $familyName%12s $givenName ${TmSymbolMap.toTm(state)}"
    case CourseData(info, cs ) => cs.zipWithIndex.map{case(x,i) =>
      f"${i}%3d ${console(x)}"}.mkString("\n")
    case x => x
  }
}
