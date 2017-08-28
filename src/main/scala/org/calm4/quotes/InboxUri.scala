package org.calm4.quotes

import akka.http.scaladsl.model.Uri

/**
  * Created by yuri on 26.08.17.
  */
import Calm4._
object InboxUri {
  val columns = "column"
  val data = "data"

  val ks = Seq("[data]", "[name]", "[searchable]", "[orderable]", "[search][value]", "[search][regex]")
  val vs = Seq("", true, true, "", false)
  def map(n: Int) = (0 to n).toList.flatMap {
    i => ks.zip( i.toString +: vs ).map{
      x => s"columns[$i]${x._1}" -> x._2.toString
    }
  }

    lazy val query = (map(8) ++ Seq(
    "draw" -> "1",
    "order[0][column]" -> "1",
    "order[0][dir]" -> "asc",
    "start" -> "0",
    "length" -> "100",
    "search[value]" -> "",
    "  search[regex]" -> "false",
    "user_custom_search[filterOnMyApplicationsOnly]" -> "false",
    "user_custom_search[length]" -> "100",
    "user_custom_search[start]" -> "0"
    )).toMap

  lazy val uri = Uri("https://calm.dhamma.org/en/course_applications/inbox")
    .withQuery(Uri.Query(query))
}
