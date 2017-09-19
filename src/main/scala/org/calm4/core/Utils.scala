package org.calm4.core

import fastparse.all._
import org.calm4.model.CalmModel3.ApplicantRecord
import org.calm4.TmSymbolMap

object Utils {

  implicit class FastParseW[T](val parser: Parser[T]) extends AnyVal {
    def fastParse(data: String): Option[T] = parser.parse(data) match {
      case Parsed.Success(x, _) => Some(x)
      case x => None.traceWith(_ => s"$x\n$data\n")
    }
  }
  val printer = pprint.copy( additionalHandlers = {case x:String => pprint.Tree.Literal(x.toString)},
    defaultHeight = 1000, defaultWidth = 120)
  //val log = Logging(CalmImplicits.system, this)
  implicit class Tracable[A] (val obj: A) extends AnyVal {
    def trace: A = {
      //pprintln(obj)
      printer.pprintln(obj)
      obj
    }

    def trace[U](u: => U): A = {
      //pprintln(obj)
      printer.pprintln(u)
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

object ApplicantOrd extends Ordering[ApplicantRecord] {
  private val priorities = TmSymbolMap.toTmSeq.map(_._1)
  override def compare(x: ApplicantRecord, y: ApplicantRecord): Int = {
    if (x.state == y.state)
      x.familyName.compare(y.familyName)
    else priorities.indexOf(x.state) - priorities.indexOf(y.state)
  }
}

