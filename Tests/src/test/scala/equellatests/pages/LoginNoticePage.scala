package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.{Keys, WebElement}

case class LoginNoticePage(ctx: PageContext) extends NewTitledPage("Login Notice Editor", "page/loginconfiguration") {

  def preNoticeApplyButton:WebElement = findElementById("preApplyButton")

  def preNoticeClearButton:WebElement = findElementById("preClearButton")

  def preNoticeField:WebElement = findElementById("preNoticeField")

  def postNoticeApplyButton:WebElement = findElementById("postApplyButton")

  def postNoticeClearButton:WebElement = findElementById("postClearButton")

  def postNoticeField:WebElement = findElementById("postNoticeField")

  def preTab:WebElement = findElementById("preTab")

  def postTab:WebElement = findElementById("postTab")

  def clearOkButton:WebElement = findElementById("okToClear")

  def clickPreApply(): Unit = {
    this.preNoticeApplyButton.click()
  }

  def clickPreClear(): Unit = {
    this.preNoticeClearButton.click()
    this.clearOkButton.click()
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

  def clickPostClear(): Unit = {
    this.postNoticeClearButton.click()
    this.clearOkButton.click()
  }

  def clickPreTab(): Unit = {
    this.preTab.click()
  }

  def clickPostTab(): Unit = {
    this.postTab.click()
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
