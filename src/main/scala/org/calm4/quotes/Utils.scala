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

  implicit class SideEffectable[A] (val obj: A) extends AnyVal {
    def *>[B](f: A => B) = {
      f(obj);
      obj
    }
  }

  implicit class SideEffectable2[A,B] (val f: A=>B) extends AnyVal {
    def >* : A => A = {x: A => f(x); x}
  }

  implicit class Mapable (val cc: Any) extends AnyVal {
    def ccToMap =
      (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }
  }
}
