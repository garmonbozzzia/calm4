package org.calm4.quotes

import akka.stream.scaladsl.Source
import org.calm4.quotes.CalmModel2.CourseData
import org.calm4.CalmImplicits._
import org.calm4.Utils._

import scala.concurrent.duration._
import CachedWithFile._
import akka.actor.Cancellable
import org.calm4.CalmModel3._

object DiffChecker {
  trait Diff
  case class StateChanged(oldState: String, newState: String, appId: Int, courseId: Int) extends Diff
  case class ApplicationAdded(appId: Int, courseId: Int) extends Diff
  case object NoChanges extends Diff

  def diff(oldData: CourseData, newData: CourseData): Seq[Diff] =
    newData.all.map{ x =>
      oldData.all.find(_.id == x.id)
        .map(_.confirmation_state_name)
        .fold[Diff](ApplicationAdded(x.id, oldData.course_id)) { oldState =>
        if(x.confirmation_state_name == oldState) NoChanges
        else StateChanged(oldState, x.confirmation_state_name, x.id, oldData.course_id)
      }
    }.filter(_ != NoChanges)

  class DiffChecker(ids: Seq[Int], timeout: FiniteDuration ) {
    private def reqsNew = Source.fromIterator(() => ids.map(GetCourse).iterator)
      .mapAsync(2)(get[CourseData](_, _ => true)).runFold(Seq.empty[CourseData])(_ :+ _ )
    private def reqsOld = Source.fromIterator(() => ids.map(GetCourse).iterator)
      .mapAsync(2)(get[CourseData](_, _ => false)).runFold(Seq.empty[CourseData])(_ :+ _ )
    def changeSource: Source[Seq[Diff], Cancellable] = Source.tick(0 seconds, timeout, ids )
      .map(_.traceWith(_ => "Start"))
      .mapAsync(1)(_ =>
        for(os <- reqsOld; ns <- reqsNew) yield
          for((o,n) <- os zip ns ; res <- diff(o,n)) yield res
      ).map(_.trace)
      //.runForeach(_.trace)
  }

  def source(ids: Seq[Int], timeout: FiniteDuration = 1 minute): Source[Seq[Diff], Cancellable] =
    new DiffChecker(ids, timeout).changeSource
}