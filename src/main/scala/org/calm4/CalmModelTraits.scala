package org.calm4

import org.calm4.CalmImplicits.browser
import org.calm4.CalmModel3.{ApplicantId, ApplicantIdF, CourseId, CourseList, CourseRecord, MessageId}
import org.calm4.quotes.CachedWithFile
import org.calm4.quotes.CalmModel.GetCourseList

import scala.concurrent.Future




trait CalmModelTraits {
  implicit def int2cid = CourseId(_)

  implicit def int2aid = ApplicantId(_)

  implicit def int2acid: ((Int, Int) => ApplicantIdF) = {
    case (aId, cId) => ApplicantIdF(aId, cId)
  }

  implicit def int2maid: (Int, Int) => MessageId = {
    case (mId, aId) => MessageId(mId, aId)
  }

  trait ApplicantIdT {
    this: ApplicantId =>
    override def toString: String = s"a$aId"
  }

  trait Applicant2IdT {
    this: ApplicantIdF =>
    override def toString: String = s"${cId}$aId"
  }

  trait MessageIdT

//  trait CourseListT {
//    this: CourseList =>
//    def actual = CourseList( courses
//      .filter(x => Seq("10-Day", "1-DayOSC", "3-DayOSC").contains(x.cType))
//      .filter(x => Seq("In Progress", "Tentative", "Scheduled" ).contains(x.status))
//      .filter(_.cId.cId != 0) )
//  }

}
