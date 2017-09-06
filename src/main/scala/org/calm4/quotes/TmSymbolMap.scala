package org.calm4.quotes

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
