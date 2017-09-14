package org.calm4.quotes

import org.calm4.TmSymbolMap
import org.calm4.quotes.CalmModel2.ApplicantJsonRecord

/**
  * Created by yuri on 01.09.17.
  */

object ApplicantRecordOrd extends Ordering[ApplicantJsonRecord] {
  private val priorities = TmSymbolMap.toTmSeq.map(_._1)
  override def compare(x: ApplicantJsonRecord, y: ApplicantJsonRecord): Int = {
    if(x.confirmation_state_name == y.confirmation_state_name)
      x.applicant_family_name.compare(y.applicant_family_name)
    else priorities.indexOf(x.confirmation_state_name) - priorities.indexOf(y.confirmation_state_name)
  }
}
