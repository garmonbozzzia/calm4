package org.calm4.quotes

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import org.calm4.Utils._
import Calm4Http._

object UriTest extends App{
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  //167487
  //CachedResponses.getJson(GetInbox()).map(_.trace) // inbox.json

  //Calm4.loadJson(Uri("https://calm.dhamma.org/en/event_reports/2482/application_forms")).map(_.trace)
  loadPage(Uri("https://calm.dhamma.org/en/users/sign_in").withQuery(Query("user[login]" -> "ignatev-yury", "user[password]" -> "J3qqnh#8fd" ))).map(_.trace)

  //CachedResponses.getJson(GetCourse(2535)).map(_.trace) //course2535.json
  //CachedResponses.getJson(GetParticipant(175435,2535)).map(_.trace) //возвращает html
  //CachedResponses.getJson(GetConversation(163755)).map(_.trace)
  //CachedResponses.getJson(GetReflist(165687)).map(_.trace)
  //CachedResponses.getJson(GetMessage(1363066, 165687)).map(_.trace)
  //CachedResponses.getJson(GetMessage(1369694, 165687)).map(_.trace)
  "Start".trace
//  CachedResponses.getJson(GetMessage(1369694, 165687)).flatMap{x =>
//    "Done".trace
//    CachedResponses.getJson(GetMessage(1369694, 165687)).map(_.trace)}
}
