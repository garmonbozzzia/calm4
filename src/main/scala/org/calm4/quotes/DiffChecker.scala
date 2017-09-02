package org.calm4.quotes

import akka.stream.scaladsl.Source
import org.calm4.quotes.CalmModel.GetCourse
import org.calm4.quotes.CalmModel2.{CourseData, Id}

import scala.concurrent.Future

/**
  * Created by yuri on 02.09.17.
  */

object DiffChecker {
  import Utils._
  import scala.concurrent.duration._
  import Calm4._
  case class StateChanged(appId: Id, oldState: String, newState: String)
  case class ApplicationAdded(id: Id)
  case object NoChanges

  def diff(oldData: CourseData, newData: CourseData) =
    newData.all.map{ x =>
      oldData.all.find(_.id == x.id)
        .map(_.confirmation_state_name)
        .fold[Any](ApplicationAdded(x.id)) { oldState =>
        if(x.confirmation_state_name == oldState) NoChanges
        else StateChanged(x.id, oldState, x.confirmation_state_name)
      }
    }.filter(_ != NoChanges)

  class DiffChecker(ids: Seq[Id], timeout: FiniteDuration ) {
    def reqsNew = Future.sequence(ids.map(GetCourse).map(CachedResponses.forceGetData[CourseData](_)))
    def reqsOld = Future.sequence(ids.map(GetCourse).map(CachedResponses.getData[CourseData](_)))
    def changeSource = Source.tick(0 seconds, timeout, ids )
      .map(_.traceWith(_ => "Start"))
      .mapAsync(1)(_ =>
        for(os <- reqsOld; ns <- reqsNew)
          yield for((o,n) <- os zip ns ; res <- diff(o,n)) yield res)
      .map(_.trace)
      //.runForeach(_.trace)
  }

  def source(ids: Seq[Id], timeout: FiniteDuration = 1 minute) = new DiffChecker(ids, timeout).changeSource
}
