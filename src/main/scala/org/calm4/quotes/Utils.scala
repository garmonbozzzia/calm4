package org.calm4.quotes

/**
  * Created by yuri on 16.07.17.
  */

object Utils {
  implicit class Tracable[A] (val obj: A) extends AnyVal {
    def trace = {
      println(obj)
      obj
    }

    def traceWith[B](f: A => B ) = {
      println(f(obj))
      obj
    }
  }
}
