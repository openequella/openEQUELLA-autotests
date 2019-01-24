package equellatests.tests

import equellatests.ShotProperties
import equellatests.instgen.fiveo.autoTestLogon
import equellatests.pages.LoginNoticePage
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

object LoginNoticeMenuProperties extends ShotProperties ("Login Notice Menu Properties"){

  property("plaintext notice creation") = forAll { (w1: String) =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1}"
      page.populateNoticeField(notice)
      page.clickApply()
      page.load()
      Prop(page.getNoticeField==notice).label("Notice: " + notice + ", NoticeField: " + page.getNoticeField)
    }
  }

}
