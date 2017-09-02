package org.calm4.quotes

import org.calm4.quotes.CalmModel.{GetCourse, GetCourseList, GetInbox}

/**
  * Created by yuri on 01.09.17.
  */
import Utils._
object UriTest extends App{
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  //167487
  //CachedResponses.getJson(GetInbox()).map(_.trace) // inbox.json


  CachedResponses.getJson(GetCourse(2535)).map(_.trace) //course2535.json
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
