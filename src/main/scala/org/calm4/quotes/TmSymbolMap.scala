package org.calm4.quotes

/**
  * Created by yuri on 01.09.17.
  */

object TmSymbolMap{
  //val sortMap =
  val toTmSeq: Seq[(String,String)] = Seq(
    "NewPendingForConfirmation" -> "→✅ ",
    "PendingForConfirmation" -> "?✅ ",
    "Confirmed" -> "✅",
    "RequestedReconfirm" -> "✅? ",
    "Reconfirmed" -> "✅✅ ",
    "Arrived" -> " 🚗 ",
    "Left" -> " 🏃 ",
    "Completed" -> "✅✅✅",
    "NewPendingForWaitlist" -> "→✔ ",
    "PendingForWaitlist" -> "?✔ ",
    "ConfirmableWaitlist" -> " ✔ ",
    "WaitListReqReconfirm" -> "✔? ",
    "WaitListReconfirmed" -> "✔✔ ",
    "NewApplication" -> " 🆕 ",
    "NewNoVacancy" -> " →✖",
    "NoVacancy" -> " ✖ ",
    "NoShow" -> " ⚫ ",
    "Cancelled" -> " ❎ ",
    "Discontinued" -> " ❌ ",
    "Refused" -> " ⛔ "
  )
  val toTm = toTmSeq.toMap
}
