package org.calm4.quotes

import java.nio.file.{Files, Paths}
import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


object CalmApps {


  import Calm4._
  import Utils._

  def loadAndSaveApps(courseUrl: String) =
    Source.fromFuture( getAppUrls(courseUrl).map(_.traceWith(_.length)) ).mapConcat[String](x => x)
      .map( x => (host + x, s"data/test/${getAppId(x).get}.html" ) )

      .mapAsync(4)(x =>
        if(!Files.exists(Paths.get(x._2))) savePage(x._1, x._2 )
        else Future.successful(x._2)
          .map(browser.parseFile)
          .map(doc => parseApplicant(doc).traceWith(x =>
            s"${x.id}, ${x.occupation.replace(",", "|")}, ${x.town.replace(",", "|")}, ${x.province.replace(",", "|")}" ))
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

object Sandbox extends App {
  def testAppList = ???


  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL._

  val appListPath = "data/test/apps.html"
  val appFile = "data/test/appExample.html"
  //val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
  val appsUrl = "https://calm.dhamma.org/en/courses/2481/course_applications"

  import Calm4._
  import Utils._

  import java.nio.file.Paths

  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._



  val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
  format.parse("2013-07-06")




  //parseCourseList(coursesFile).map(_.trace)
  val a = browser.parseFile(coursesFile) >> elementList(".colour-event-datatable-row")
    .map(x => (x >> attr("href")("a")).zip(x >> elementList("td") >> allText))
    //, x >> elementList("td") >> allText)
    //.map(_.transpose.map(_.distinct).mkString("\n").trace)
    //.map(_.transpose.trace)
    .map(_.filter(x => validate(x._2)))
    .map(_.map(x => Course(x._1, x._2)))
    .map(_.mkString("\n").trace)
  //.map(_.trace)


  //a.map(x => x.trace)

  //  savePage(appsUrl, appListPath )
  //    .onComplete(_ => system.terminate().traceWith(_ => "Done!"))

  //  parseApplicantList("data/test/apps.html").traceWith(_.mkString("\n"))
  //  .map(getAppId(_).trace)

  //CalmApps.app1
  //loadAndSaveApps(appsUrl).onComplete(_ => system.terminate().traceWith(_ => "Done!"))


  //system.terminate()


}
