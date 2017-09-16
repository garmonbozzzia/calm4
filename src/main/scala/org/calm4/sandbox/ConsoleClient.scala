package org.calm4.sandbox

import akka.stream.scaladsl.Source
import org.calm4.{CalmModel3, CommandParser}
import org.calm4.Utils._
import org.calm4.CalmModel3._

import scala.io.StdIn

/**
  * Created by yuri on 14.09.17.
  */
object Command {
  def apply(text: String) = CommandParser.parse(text)
}
import org.calm4.CalmImplicits._
object ConsoleClient extends App {
  //val input = Stream.continually(StdIn.readLine)
  //input.foreach(_.trace)
  Source.fromIterator(() => Iterator.continually(StdIn.readLine))
    .map(Command(_))
      .mapAsync(1)(_.execute)
      .map (CalmView.console)
    .runForeach(_.trace)
  //Command("/c2345") execute {_.trace}

}
