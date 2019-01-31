package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.{Keys, WebElement}

case class LoginNoticePage(ctx: PageContext) extends NewTitledPage("Login Notice Editor", "page/loginconfiguration") {

  def preNoticeApplyButton:WebElement = findElementById("preApplyButton")

  def preNoticeDeleteButton:WebElement = findElementById("preDeleteButton")

  def preNoticeField:WebElement = findElementById("preNoticeField")

  def postNoticeApplyButton:WebElement = findElementById("postApplyButton")

  def postNoticeDeleteButton:WebElement = findElementById("postDeleteButton")

  def postNoticeField:WebElement = findElementById("postNoticeField")
  
  def clickPreApply(): Unit = {
    this.preNoticeApplyButton.click()
  }

  def clickPreDelete(): Unit = {
    this.preNoticeDeleteButton.click()
  }

  def populatePreNoticeField(notice:String): Unit = {

    preNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    preNoticeField.sendKeys(Keys.DELETE)
    preNoticeField.sendKeys(notice)
  }

  def getPreNoticeFieldContents:String = {
    preNoticeField.getText
  }
  
  def clickPostApply(): Unit = {
    this.postNoticeApplyButton.click()
  }

  def clickPostDelete(): Unit = {
    this.postNoticeDeleteButton.click()
  }

  def populatePostNoticeField(notice:String): Unit = {
    postNoticeField.sendKeys(Keys.chord(Keys.CONTROL, "a"))
    postNoticeField.sendKeys(Keys.DELETE)
    postNoticeField.sendKeys(notice)
  }

  def getPostNoticeFieldContents:String = {
    postNoticeField.getText
  }
}
