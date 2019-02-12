package equellatests.pages

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.ExpectedConditions2
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.{Keys, WebElement}

case class LoginNoticePage(ctx: PageContext) extends NewTitledPage("Login Notice Editor", "page/loginconfiguration") {

  private def preNoticeApplyButton:WebElement = findElementById("preApplyButton")

  private def preNoticeClearButton:WebElement = findElementById("preClearButton")

  private def preNoticeField:WebElement = findElementById("preNoticeField")

  private def postNoticeApplyButton:WebElement = findElementById("postApplyButton")

  private def postNoticeClearButton:WebElement = findElementById("postClearButton")

  private def postNoticeField:WebElement = findElementById("postNoticeField")

  private def preTab:WebElement = findElementById("preTab")

  private def postTab:WebElement = findElementById("postTab")

  private def clearOkButton:WebElement = findElementById("okToClear")

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
  def pageUpdateExpectation = ExpectedConditions2.presenceOfElement(preNoticeField)

  def waitForLoad(): Unit = {
    waitFor(pageUpdateExpectation)
  }
}
