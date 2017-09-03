package org.calm4.quotes

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.calm4.quotes.Calm4._
import org.calm4.quotes.Utils._
import scalaz.Scalaz._

import scala.concurrent.Future

/**
  * Created by yuri on 03.09.17.
  */
object Authentication {

  val Seq(login, password) = scala.io.Source.fromFile("data/login").getLines().toSeq
  var (sessionId, expireAt) = (Cookie("_sso_session", "am1La2dFQ3F2QjRtVUxQUlNEMmtRbW1SSitjRlJVTndFZ0Q5YmYwQlB3Y29pVExDTitNTEQ0M2lOOTB3eUFWSlFtTHpBMU9ZRGM1WXlUSTVmbGR6LzBaaVZDYkgzZjZJYm9RU0VGRVkyMjg0bU5IMGhGSkQ3REJJRE51N09YTWJTVkYzSlZxdGFQc05aOVNNZ1BNSm8yamVhMG83Zys5alZXU1pxWlZJalhxSVpaQ01DRm1WcnoxUGZCN3J4YjlqY2t4TlNHajhPenhmd0RqdkhBM2gzSUxaUUU4dU5QaWFsY1dUSTB5TDNCUUltNVFJait0YXpXUGFEMHFDdVpaZC9mK0NkZTNXWGxqZU4wTkZDTzJSMVgvNGFHaXVWcGVlTWVNcmFEQkZxSlE0ZldCdDBuVm9ZbHcyaW5QV3pZemM3cGppSTduRUJrbDBXTzVLdlhJVGUxSWFJc3VmN3h2L2Urd2QrZkZxTysvbTl1WTlzOUJsREx0QkpBVXFPUjlRdURZRFVCQ01vdjNvZmgzaGVBWG9RVFFzeGcrZTI1a1gzRHpKWGY0Vmloc0hhdmRISWt5UytwMnRFK09QMzYwNDBZM0ZiS3YyZytuVURqQnJqU0VNQlRqbHAyaDFCYVlTK1p3ZzNlenJvTUE9LS1xeTVYTzNLdkRyN1hwVVdvdFlLYWZRPT0%3D--81de5e26e59ee0e8c8534da5374e5a972d91278a"), 0L)
  val signInRequest =
    Post("https://calm.dhamma.org/en/users/sign_in", FormData(
      "user[login]" -> login,
      "user[password]" -> password,
      "commit" -> "Log In")).addHeader(sessionId)

  def set(id: String) = {
    sessionId = Cookie("_sso_session", id )
    expireAt = System.currentTimeMillis + 17000000
    id
  }

  //val cookie = RawHeader("cookie", s"_sso_session=$sessionId")
  def signIn: Future[Cookie] = Http().singleRequest(signInRequest)
    .map(_.trace.header[`Set-Cookie`].map(_.cookie.value))
    .flatMap(_.fold(Future.failed[String](new Exception("Login failed").trace)){Future.successful})
    .map(set)
    .map(Cookie("_sso_session", _ ).trace)

  def cookie = (System.currentTimeMillis > expireAt) ? signIn | Future(sessionId)
}

object SignInTest extends App {
  import Calm4._
  import Utils._
  for{
    auth <- Authentication.cookie.trace
    result <- Http().singleRequest(Get("https://calm.dhamma.org/en/courses").addHeader(auth))
  } yield result.trace
  //loadPage("https://calm.dhamma.org/en/courses").map(_.trace)
}