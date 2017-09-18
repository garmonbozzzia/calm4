package org.calm4

import org.calm4.CalmImplicits.browser
import org.calm4.CalmModel3.GetMessage
import org.calm4.quotes.CachedWithFile
import org.json4s.JString
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.util.Try
import Utils._
import CalmModel3._
import CalmImplicits._


trait Message {
  val aId: Int
  val mId: Int

  def data = for{
    json <- CachedWithFile.getJson(GetMessage(mId.trace, aId))
  } yield Try(json.camelizeKeys.transformField{
    case ("type", x) => "messageType" -> x
    case ("id", x) => "mId" -> x
    case ("segments", x) =>
      val JString(txt) = x \\ "value"
      "text" -> JString(browser.parseString(txt) >> allText)
  }.extract[MessageData]).fold(x => {x.trace; throw x}, identity )
}
