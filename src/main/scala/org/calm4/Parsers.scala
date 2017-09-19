package org.calm4

import fastparse.all._
import org.calm4.core.Utils._

trait ParsersUtils {
  val id = P(CharIn('0'to'9').rep(1).!.map(_.toInt))
  val host = "https://calm.dhamma.org".?
  val courseIdParser = P(host ~ "/en/courses/" ~ id)
  val applicantParser = P(host ~ "/en/courses/" ~ id ~ "/course_applications/" ~ id)
  val messageParser = P("https://calm.dhamma.org".? ~ "/en/course_applications/" ~ id ~
    ("/notes/".!.map(_ => "n") | "/messages/".!.map(_ => "m")) ~ id)

}

object Parsers extends ParsersUtils{
}
