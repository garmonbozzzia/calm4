package org.calm4.quotes

import java.nio.file.{Files, Paths}

import akka.http.scaladsl.model.Uri
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString


import scala.concurrent.Future


object CalmApps {


  import Calm4._
  import Utils._

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


import Calm4._
import Utils._

object A extends App {
  Source(List(ByteString("asd"))).runWith(FileIO.toPath(Paths.get("data/54.txt")))
}

import Utils._
object TestData {
  lazy val app1ExampleUri = "https://calm.dhamma.org/en/courses/2528/course_applications/166012/edit"
  lazy val courseExampleUri1 = "https://calm.dhamma.org/en/courses/2528/course_applications"
  lazy val app1ExamplePage = Calm4.loadPage(app1ExampleUri).map(_.trace)
  lazy val app1Example: Future[Applicant] = app1ExamplePage.map(x => Calm4.parseApplicant(x)).map(_.trace)

  def telegramView(app: Applicant): String = s"*${app.familyName} ${app.name}*\n${app.town} _${app.province}_\n${app.occupation}"
  lazy val app1TelegramViewExample = app1Example.map(telegramView)
}

object Sandbox extends App {
  def testAppList = ???

  val appListPath = "data/test/apps.html"
  val appFile = "data/test/appExample.html"
  //val appsUrl = "https://calm.dhamma.org/en/courses/2478/course_applications"
  val appsUrl = "https://calm.dhamma.org/en/courses/2481/course_applications"


  //Course.all.head.appRecords.map(_.trace)



  Calm4.savePage2(InboxUri.uri, "data/inbox2.json")

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
//  <select class="form-control subdivision optional" data-country-selector="#course_application_contact_country" data-subdivision-selector="1" id="course_application_contact_province" name="course_application[contact_province]"><option value=""></option> <option value="AD">Adygeya, Respublika</option> <option value="AL">Altay, Respublika</option> <option value="ALT">Altayskiy kray</option> <option value="AMU">Amurskaya oblast'</option> <option value="ARK">Arkhangel'skaya oblast'</option> <option value="AST">Astrakhanskaya oblast'</option> <option value="BA">Bashkortostan, Respublika</option> <option value="BEL">Belgorodskaya oblast'</option> <option value="BRY">Bryanskaya oblast'</option> <option value="BU">Buryatiya, Respublika</option> <option value="CE">Chechenskaya Respublika</option> <option value="CHE">Chelyabinskaya oblast'</option> <option value="CHU">Chukotskiy avtonomnyy okrug</option> <option value="CU">Chuvashskaya Respublika</option> <option value="DA">Dagestan, Respublika</option> <option value="IN">Ingushskaya Respublika [Respublika Ingushetiya]</option> <option value="IRK">Irkutskaya oblast'</option> <option value="IVA">Ivanovskaya oblast'</option> <option value="KAM">Kamchatskaya oblast'</option> <option value="KB">Kabardino-Balkarskaya Respublika</option> <option value="KC">Karachayevo-Cherkesskaya Respublika</option> <option value="KDA">Krasnodarskiy kray</option> <option value="KEM">Kemerovskaya oblast'</option> <option value="KGD">Kaliningradskaya oblast'</option> <option value="KGN">Kurganskaya oblast'</option> <option value="KHA">Khabarovskiy kray</option> <option value="KHM">Khanty-Mansiyskiy avtonomnyy okrug [Yugra]</option> <option value="KIR">Kirovskaya oblast'</option> <option value="KK">Khakasiya, Respublika</option> <option value="KL">Kalmykiya, Respublika</option> <option value="KLU">Kaluzhskaya oblast'</option> <option value="KO">Komi, Respublika</option> <option value="KOS">Kostromskaya oblast'</option> <option value="KR">Kareliya, Respublika</option> <option value="KRS">Kurskaya oblast'</option> <option value="KYA">Krasnoyarskiy kray</option> <option value="LEN">Leningradskaya oblast'</option> <option value="LIP">Lipetskaya oblast'</option> <option value="MAG">Magadanskaya oblast'</option> <option value="ME">Mariy El, Respublika</option> <option value="MO">Mordoviya, Respublika</option> <option value="MOS">Moskovskaya oblast'</option> <option value="MOW">Moskva</option> <option value="MUR">Murmanskaya oblast'</option> <option value="NEN">Nenetskiy avtonomnyy okrug</option> <option value="NGR">Novgorodskaya oblast'</option> <option value="NIZ">Nizhegorodskaya oblast'</option> <option value="NVS">Novosibirskaya oblast'</option> <option selected value="OMS">Omskaya oblast'</option> <option value="ORE">Orenburgskaya oblast'</option> <option value="ORL">Orlovskaya oblast'</option> <option value="PER">Perm</option> <option value="PNZ">Penzenskaya oblast'</option> <option value="PRI">Primorskiy kray</option> <option value="PSK">Pskovskaya oblast'</option> <option value="ROS">Rostovskaya oblast'</option> <option value="RYA">Ryazanskaya oblast'</option> <option value="SA">Sakha, Respublika [Yakutiya]</option> <option value="SAK">Sakhalinskaya oblast'</option> <option value="SAM">Samarskaya oblast'</option> <option value="SAR">Saratovskaya oblast'</option> <option value="SE">Severnaya Osetiya, Respublika [Alaniya] [Respublika Severnaya Osetiya-Alaniya]</option> <option value="SMO">Smolenskaya oblast'</option> <option value="SPE">Sankt-Peterburg</option> <option value="STA">Stavropol'skiy kray</option> <option value="SVE">Sverdlovskaya oblast'</option> <option value="TA">Tatarstan, Respublika</option> <option value="TAM">Tambovskaya oblast'</option> <option value="TOM">Tomskaya oblast'</option> <option value="TUL">Tul'skaya oblast'</option> <option value="TVE">Tverskaya oblast'</option> <option value="TY">Tyva, Respublika [Tuva]</option> <option value="TYU">Tyumenskaya oblast'</option> <option value="UD">Udmurtskaya Respublika</option> <option value="ULY">Ul'yanovskaya oblast'</option> <option value="VGG">Volgogradskaya oblast'</option> <option value="VLA">Vladimirskaya oblast'</option> <option value="VLG">Vologodskaya oblast'</option> <option value="VOR">Voronezhskaya oblast'</option> <option value="YAN">Yamalo-Nenetskiy avtonomnyy okrug</option> <option value="YAR">Yaroslavskaya oblast'</option> <option value="YEV">Yevreyskaya avtonomnaya oblast'</option> <option value="ZAB">Zabaykal'skij kray</option></select>


