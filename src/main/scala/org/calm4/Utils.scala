package org.calm4

object Utils {
  implicit class Tracable[A] (val obj: A) extends AnyVal {
    def trace: A = {
      println(obj)
      obj
    }

    def traceWith[B](f: A => B ): A = {
      println(f(obj))
      obj
    }
  }

  implicit class SideEffectable[A] (val obj: A) extends AnyVal {
    def *>[B](f: A => B): A = {
      f(obj)
      obj
    }
//
//    def *>>[B](f: => B): A = {
//      f
//      obj
//    }
  }
//
//  implicit class SideEffectable2[A,B] (val f: A=>B) extends AnyVal {
//    def >* : A => A = {x: A => f(x); x}
//  }

  implicit class Mapable (val cc: Any) extends AnyVal {
    def ccToMap: Map[String, Any] =
      (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }
  }
}
