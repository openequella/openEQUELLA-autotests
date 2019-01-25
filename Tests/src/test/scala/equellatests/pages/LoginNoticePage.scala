package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.{Keys, WebElement}

case class LoginNoticePage(ctx: PageContext) extends NewTitledPage("Login Notice Editor", "page/loginconfiguration") {

  def applyButton:WebElement = findElementById("applyButton")

  def noticeField:WebElement = findElementById("noticeField")

  def clickApply(): Unit = {
    this.applyButton.click()
  }

  def populateNoticeField(notice:String): Unit = {
    noticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    noticeField.sendKeys(Keys.DELETE)
    noticeField.sendKeys(notice)
  }

  def getNoticeFieldContents:String = {
    noticeField.getText
  }

}
