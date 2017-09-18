package org.calm4

import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.calm4.quotes.ApplicantRecordOrd
import org.calm4.quotes.CalmModel2.ApplicantJsonRecord
import Utils._
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object CalmImplicits {
  implicit val system = ActorSystem()
  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

  //implicit val materializer = ActorMaterializer()
  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x).traceWith(_ => x.getStackTrace.mkString("\n"))
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val browser = JsoupBrowser()
  implicit val formats = DefaultFormats

  val timezoneDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
}
