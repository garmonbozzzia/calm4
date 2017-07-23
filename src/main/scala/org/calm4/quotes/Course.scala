package org.calm4.quotes

/**
  * Created by yuri on 23.07.17.
  */
case class Course(startsAt: String, endsAt: String, link: String){
  def id = link.split("/")(6)
}

/**
  * Created by yuri on 22.07.17.
  */

object Course {
  def apply(link: String, data: List[String]) = new Course(data(0), data(1), link)
}