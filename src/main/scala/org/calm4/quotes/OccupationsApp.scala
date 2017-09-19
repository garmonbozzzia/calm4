package org.calm4.quotes

import akka.stream.scaladsl.Source
import org.calm4.model.CalmModel3._
import org.calm4.quotes.CalmModel2.{ApplicantJsonRecord, CourseData}
import org.calm4.core.CalmImplicits._
import org.calm4.core.Utils._

/**
  * Created by yuri on 04.09.17.
  */
object OccupationsApp extends App{
  import Calm4Old._
  val courseId = 2484
  Source.fromFuture(CachedWithFile.get[CourseData](GetCourse(courseId)))
      .map(_.all.to[scala.collection.immutable.Seq])
    .mapConcat[ApplicantJsonRecord](x => x)
    .map(x => GetParticipant(x.id, courseId))
    .mapAsync(2)(CachedWithFile.getPage(_))
    .map(x => Calm4Old.parseApplicant(browser.parseString(x)))
    .map(x => s"${x.id}\t ${x.occupation.replace(",", "|")}\t ${x.town.replace(",", "|")}\t ${x.province.replace(",", "|")}")
    .runForeach(_.trace)
}
