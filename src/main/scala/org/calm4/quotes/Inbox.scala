package org.calm4.quotes

import org.calm4.quotes.Calm4.{browser, loadJson}

/**
  * Created by yuri on 26.08.17.
  */
import Utils._
import Calm4._
object Inbox extends App {
  //val a = scala.io.Source.fromFile("data/inbox.json").mkString
  val aa = loadJson(InboxUri.uri.trace).map(_.trace)
  //val aa = loadJson(Sandbox.inbox.trace)//.map(_.trace)

  import org.json4s._

  implicit val formats = DefaultFormats

  case class InboxJson(draw: Int, data: List[List[String]], recordsTotal: Int, recordsFiltered: Int)

  case class InboxEntity(isReply: Boolean = false, link: String = "-", name: String, appType: String, venue: String)


  def extractLink(data: List[String]): Option[InboxEntity] =
    if (data.length != 9) None
    else {
      val d1 = browser.parseString(data(0))
      val d2 = browser.parseString(data(4))
      import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
      import net.ruippeixotog.scalascraper.dsl.DSL._
      Some(InboxEntity(link = d1 >> element("a") >> attr("href"), name = data(1), appType = "-", venue = data(7)))
    }

  //val b = parse(a).extract[InboxJson].data.map(extractLink).mkString("\n").trace
  aa.map(_.extract[InboxJson].data.map(extractLink)traceWith(_.mkString("\n")))
    .map(Calm4.host + _.last.get.link)
    .map(_.trace)

  //parse(a).trace
}
