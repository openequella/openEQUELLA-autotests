package equellatests.tests

import equellatests.ShotProperties
import equellatests.domain.RandomWord
import equellatests.instgen.fiveo.autoTestLogon
import equellatests.pages.{LoginNoticePage, LoginPage}
import org.openqa.selenium.By
import org.scalacheck.Prop
import org.scalacheck.Prop.forAll

object LoginNoticeMenuProperties extends ShotProperties("Login Notice Menu Properties") {

  property("pre login notice creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePreNoticeField(notice)
      page.clickPreApply()
      page.load()
      Prop(page.getPreNoticeFieldContents == notice).label("Notice: " + notice + ", NoticeField: " + page.getPreNoticeFieldContents)
    }
  }

  property("post login notice creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePostNoticeField(notice)
      page.clickPostApply()
      page.load()
      Prop(page.getPostNoticeFieldContents == notice).label("Notice: " + notice + ", NoticeField: " + page.getPostNoticeFieldContents)
    }
  }

  property("pre login notice deletion") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePreNoticeField(notice)
      page.clickPreApply()
      page.load()
      page.clickPreDelete()
      page.load()
      Prop(page.getPreNoticeFieldContents == "")
    }
  }

  property("post login notice deletion") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePostNoticeField(notice)
      page.clickPostApply()
      page.load()
      page.clickPostDelete()
      page.load()
      Prop(page.getPostNoticeFieldContents == "")
    }
  }

  property("prove existence on login page after creation") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePreNoticeField(notice)
      page.clickPreApply()
      page.load()
      val page2 = LoginPage(context).load()
      Prop(page2.findElementO(By.id("loginNotice")).get.getText == notice)
    }
  }

  property("prove non-existence on login page after deletion") = forAll { w1: RandomWord =>
    withLogon(autoTestLogon) { context =>
      val page = LoginNoticePage(context).load()
      val notice = s"${w1.word}"
      page.populatePreNoticeField(notice)
      page.clickPreApply()
      page.load()
      page.clickPreDelete()
      page.load()
      val page2 = LoginPage(context).load()
      Prop(page2.findElementO(By.id("loginNotice")).isEmpty)
    }
  }

}
