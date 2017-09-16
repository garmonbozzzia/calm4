package org.calm4

object TmSymbolMap{
  //val sortMap =
  val toTmSeq: Seq[(String,String)] = Seq(
    "NewPendingForConfirmation" -> "👤→✅",
    "PendingForConfirmation" -> "?✅",
    "Confirmed" -> "✅",
    "RequestedReconfirm" -> "✅?",
    "Reconfirmed" -> "✅✅",
    "Arrived" -> "🚗",
    "Left" -> "🏃",
    "Completed" -> "✅✅✅",
    "NewPendingForWaitlist" -> "👤→✔",
    "PendingForWaitlist" -> "?✔",
    "ConfirmableWaitlist" -> "✔",
    "WaitListReqReconfirm" -> "✔?",
    "WaitListReconfirmed" -> "✔✔",
    "NewApplication" -> "👤",
    "NewNoVacancy" -> "👤→✖",
    "NoVacancy" -> "✖",
    "NoShow" -> "⚫",
    "Cancelled" -> "❎",
    "Discontinued" -> "❌",
    "Refused" -> "⛔"
  )
  val toTm: Map[String, String] = toTmSeq.toMap
}

object Symbols extends App {
  println((32 to 64*1024).grouped(64).map(_.map(_.toChar).mkString).mkString("\n"))
}

