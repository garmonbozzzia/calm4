package org.calm4.model

import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import CalmModel3.{GetMessage, MessageData}
import org.calm4.core.CalmImplicits.{browser, _}
import org.calm4.core.Utils._
import org.calm4.quotes.CachedWithFile
import org.json4s.JString

import scala.concurrent.Future
import scala.util.Try


trait Message {
  val aId: Int
  val mId: Int

  def data: Future[MessageData] = for{
    json <- CachedWithFile.getJson(GetMessage(mId.trace, aId))
  } yield Try(json.camelizeKeys.transformField{
    case ("type", x) => "messageType" -> x
    case ("id", x) => "mId" -> x
    case ("segments", x) =>
      val JString(txt) = x \\ "value"
      "text" -> JString(browser.parseString(txt) >> allText)
  }.extract[MessageData]).fold(x => {x.trace; throw x}, identity )
}
