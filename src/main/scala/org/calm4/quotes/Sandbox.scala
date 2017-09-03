package org.calm4.quotes

import java.nio.file.{Files, Paths}
import akka.stream.scaladsl.{Sink, Source}
import org.calm4.quotes.Calm4._
import org.calm4.quotes.Utils._
import scala.concurrent.Future

object CalmApps {
  def loadAndSaveApps(courseUrl: String) =
    Source.fromFuture(getAppUrls(courseUrl).map(_.traceWith(_.length))).mapConcat[String](x => x)
      .map(x => (host + x, s"data/test/${getAppId(x).get}.html"))

      .mapAsync(4)(x =>
        if (!Files.exists(Paths.get(x._2))) savePage(x._1, x._2)
        else Future.successful(x._2)
          .map(browser.parseFile)
          .map(doc => parseApplicant(doc).traceWith(x =>
            s"${x.id}, ${x.occupation.replace(",", "|")}, ${x.town.replace(",", "|")}, ${x.province.replace(",", "|")}"))
      )
      .runWith(Sink.ignore)

  def app1 = {
    val appListPath = "data/test/apps.html"
    val appFile = "data/test/appExample.html"
    //val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
    val appsUrl = "https://calm.dhamma.org/en/courses/2481/course_applications"

    loadAndSaveApps(appsUrl).onComplete(_ => system.terminate().traceWith(_ => "Done!"))
  }
}


import org.calm4.quotes.Calm4._
import org.calm4.quotes.Utils._
object TestData {
  lazy val app1ExampleUri = "https://calm.dhamma.org/en/courses/2528/course_applications/166012/edit"
  lazy val courseExampleUri1 = "https://calm.dhamma.org/en/courses/2528/course_applications"
  lazy val app1ExamplePage = Calm4.loadPage(app1ExampleUri).map(_.trace)
  lazy val app1Example: Future[Applicant_] = app1ExamplePage.map(x => Calm4.parseApplicant(x)).map(_.trace)

  def telegramView(app: Applicant_): String = s"*${app.familyName} ${app.name}*\n${app.town} _${app.province}_\n${app.occupation}"
  lazy val app1TelegramViewExample = app1Example.map(telegramView)
}

object Sandbox extends App {
  def testAppList = ???

  val appListPath = "data/test/apps.html"
  val appFile = "data/test/appExample.html"
  //val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
  val appsUrl = "https://calm.dhamma.org/en/courses/2481/course_applications"


  //Course.all.head.appRecords.map(_.trace)



  Calm4.saveJson(CalmUri.inboxUri, "data/inbox2.json")

  //Course.all(0).appRecords.map(_.mkString("\n").trace)

  //Source(Course.all).mapAsync(1)(_.appRecords).runForeach(_.length.trace)

  //  val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
  //  format.parse("2013-07-06")
  //
  //
  //
  //
  //  //parseCourseList(coursesFile).map(_.trace)
  //  val a = browser.parseFile(coursesFile) >> elementList(".colour-event-datatable-row")
  //    .map(x => (x >> attr("href")("a")).zip(x >> elementList("td") >> allText))
  //    //, x >> elementList("td") >> allText)
  //    //.map(_.transpose.map(_.distinct).mkString("\n").trace)
  //    //.map(_.transpose.trace)
  //    .map(_.filter(x => validate(x._2)))
  //    .map(_.map(x => Course(x._1, x._2)))
  //    .map(_.mkString("\n").trace)
  //  //.map(_.trace)


  //a.map(x => x.trace)

  //  savePage(appsUrl, appListPath )
  //    .onComplete(_ => system.terminate().traceWith(_ => "Done!"))

  //  parseApplicantList("data/test/apps.html").traceWith(_.mkString("\n"))
  //  .map(getAppId(_).trace)

  //CalmApps.app1
  //loadAndSaveApps(appsUrl).onComplete(_ => system.terminate().traceWith(_ => "Done!"))


  //system.terminate()


}



//
//
//
//object InboxQueryApp extends App{
//  InboxUri.map.trace
//  Uri("").withQuery(Uri.Query(InboxUri.map)).toString().trace
//}
//

