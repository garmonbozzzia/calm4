package org.calm4.quotes

import org.calm4.quotes.CalmModel2.ApplicantJsonRecord
import scalaz.Scalaz._

object ApplicantJsonRecordTm{
  def apply: ApplicantJsonRecord => ApplicantJsonRecordTm = {
    case ApplicantJsonRecord(id, _, gName, fName, age, sit, old, _, _, ad_hoc, pregnant, sat, served, _, _, _, state ) =>
      new ApplicantJsonRecordTm(id, fName, gName,
        sit ? (old ? "🎓" | "") | "⭐",
        state, TmSymbolMap.toTm.getOrElse(state,"❓"),
        age.getOrElse(0),
        old ? Option(sat.getOrElse(0) -> served.getOrElse(0)) | None
      )
  }
}

case class ApplicantJsonRecordTm(id: Int, familyName: String, givenName: String, nos: String, longStatus: String,
                                 shortStatus: String, age: Int, sitAndServed: Option[(Int,Int)] ) {
  val view1 = s"/a$id $shortStatus *$familyName $givenName*"
  val view2 =
    s"*$familyName $givenName* _Age:_ $age\n$shortStatus $longStatus${sitAndServed.fold(""){case (x,y) => s"\n$nos Sit: $x Served: $y"}}"
}