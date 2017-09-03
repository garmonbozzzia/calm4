package org.calm4.quotes

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.calm4.quotes.CalmModel2.ApplicantJsonRecord
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import Utils._

/**
  * Created by yuri on 03.09.17.
  */
trait CalmImplicits {
  implicit val system = ActorSystem()
  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

  //implicit val materializer = ActorMaterializer()
  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x)
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val browser = JsoupBrowser()
  implicit val formats = DefaultFormats
}
