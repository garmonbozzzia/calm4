package org.calm4.quotes

import akka.stream.scaladsl.Source
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{CourseData, Id}

import scala.concurrent.Future


object DiffChecker {
  import Utils._
  import scala.concurrent.duration._
  import Calm4._

  trait Diff
  case class StateChanged(oldState: String, newState: String, appId: Id, courseId: Id) extends Diff
  case class ApplicationAdded(appId: Id, courseId: Id) extends Diff
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

  class DiffChecker(ids: Seq[Id], timeout: FiniteDuration ) {
    import CachedWithFile._
    def reqsNew = Source.fromIterator(() => ids.map(GetCourse).iterator)
      .mapAsync(2)(get[CourseData](_, true)).runFold(Seq.empty[CourseData])(_ :+ _ )
    def reqsOld = Source.fromIterator(() => ids.map(GetCourse).iterator)
      .mapAsync(2)(get[CourseData](_)).runFold(Seq.empty[CourseData])(_ :+ _ )
    def changeSource = Source.tick(0 seconds, timeout, ids )
      .map(_.traceWith(_ => "Start"))
      .mapAsync(1)(_ =>
        for(os <- reqsOld; ns <- reqsNew) yield
          for((o,n) <- os zip ns ; res <- diff(o,n)) yield res
      ).map(_.trace)
      //.runForeach(_.trace)
  }

  def source(ids: Seq[Id], timeout: FiniteDuration = 1 minute) = new DiffChecker(ids, timeout).changeSource
}
