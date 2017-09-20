package org.calm4

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import org.calm4.model.CalmModel3._
import org.calm4.core.Utils._
import org.calm4.core.CalmImplicits._
import org.calm4.core.TickSource
import org.calm4.model.{Applicant, Course}

import scala.concurrent.duration._

/**
  * Created by yuri on 18.09.17.
  */
object CourseDemon {

  trait CourseDiff extends TmMessage
  case class StateChanged(oldState: String, newState: String, aId: Int, cId: Int)
    extends CourseDiff with Applicant
  case class ApplicationAdded(aId: Int, cId: Int) extends CourseDiff with Applicant
  case object NoChanges extends CourseDiff

  val callbacks = scala.collection.mutable.ListBuffer.empty[CourseDiff => Any]

  def diff(oldData: CourseData, newData: CourseData): Seq[CourseDiff] =
    newData.applicants.map { x =>
      oldData.applicants.find(_.aId == x.aId)
        .map(_.state)
        .fold[CourseDiff](ApplicationAdded(x.aId, oldData.courseInfo.cId)) { oldState =>
        if (x.state == oldState) NoChanges
        else StateChanged(oldState, x.state, x.aId, oldData.courseInfo.cId)
      }
    }.trace.filter(_ != NoChanges)

  def run(courses: Seq[Course], duration: FiniteDuration = 10 minute) = {
    val s = Source.queue[Any](1, OverflowStrategy.dropNew)
      .mapConcat(_ => courses.to)
      .mapAsync(1)(x => x.data)
        .map(_.trace)
      .sliding(2)
      .map {
        case Seq(x, y) => diff(x, y)
      }
      .map(_.foreach(x => callbacks.foreach(_ (x))))
      .to(Sink.ignore)
      .run()

    val ts = TickSource(duration)(s.offer(""))
    ts ! TickSource.Restart
    //ts ! TickSource.Restart
    ts
  }
}

