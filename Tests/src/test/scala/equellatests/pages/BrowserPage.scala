package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.pages
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

import scala.util.Try

trait BrowserPage {
  def ctx: PageContext
  def driver : WebDriver = ctx.getDriver
  def findElement(by: By): WebElement = driver.findElement(by)
  def findElementById(id: String): WebElement = findElement(By.id(id))
  def findElementO(by: By): Option[WebElement] = Try(driver.findElement(by)).toOption
  val waiter = new WebDriverWait(driver, 10, 50L)
  def waitFor[A](c: ExpectedCondition[A]) : A = waiter.until(c)

  def updatedBy(by: By): ExpectedCondition[_] = ExpectedConditions.and(ExpectedConditions.stalenessOf(findElement(by)),
    ExpectedConditions.visibilityOfElementLocated(by))

  def quoteXPath(input: String): String = {
    val txt = input
    if (txt.indexOf("'") > -1 && txt.indexOf("\"") > -1) "concat('" + txt.replace("'", "', \"'\", '") + "')"
    else if (txt.indexOf("\"") > -1) "'" + txt + "'"
    else "\"" + txt + "\""
  }
}