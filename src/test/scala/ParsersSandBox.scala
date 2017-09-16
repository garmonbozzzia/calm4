import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import org.calm4.Authentication
import org.calm4.quotes.CachedWithFile
import org.calm4.quotes.Parsers.{CourseList, CourseRecord}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FeatureSpec, Matchers}
import org.calm4.CalmImplicits._
import org.calm4.CalmModel3.GetCourseList
import org.calm4.Utils._

trait CalmTest extends FeatureSpec with Matchers with ScalaFutures {
  implicit val patience = PatienceConfig(timeout = Span(15,Seconds), interval = Span(500, Millis))
}

class TestModel extends CalmTest {
  import org.calm4.CalmModel3.{CourseList, _}
  whenReady( for {
    CourseList(courses) <- Courses.list
    a <- CourseId(courses(0).cId).data
  } yield {
    a.applicants.mkString("\n").trace
  }) {x => x}
}

object TestModel {

}

class SignInTest extends FeatureSpec with Matchers with ScalaFutures {
  for{
    auth <- Authentication.cookie.trace
    result <- Http().singleRequest(Get("https://calm.dhamma.org/en/courses").addHeader(auth))
  } yield result.trace
  //loadPage("https://calm.dhamma.org/en/courses").map(_.trace)
}

class ParsersSandBox extends FeatureSpec with Matchers with ScalaFutures {
  //2
  implicit val patience = PatienceConfig(timeout = Span(15,Seconds), interval = Span(500, Millis))
  whenReady(
    CachedWithFile.getDataJson(GetCourseList(), _ => false)
      .map(_.trace.map(CourseRecord(_).trace))
      .map(CourseList(_).traceWith(x => s"Total: ${x.courses.length}"))
      .map(_.actual.traceWith(x => s"Total: ${x.courses.length}"))
      .map(_.traceWith(_ => "\nStatuses:"))
      .map(_.traceWith(_.courses.map(_.status).distinct.mkString("\n")))
      .map(_.traceWith(_ => "\nCourse Types:"))
      .map(_.traceWith(_.courses.map(_.cType).distinct.mkString("\n")))
  ){_.trace}
  //Thread.sleep(10000L)
//  10-Day
//  ServicePeriod
//  3-DayOSC
//  Satipatthana
//  1-DayOSC
//  20-DayOSC
//  TSC
//  Child
//  Teen

//  Finished
//  Cancelled
//  In Progress
//  Tentative
//  Scheduled
//  Scheduled (not open)
}
