package com.tle.webtests.test.reporting;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class MultiValueReportPage extends AbstractReport<MultiValueReportPage>
{	
	public MultiValueReportPage(PageContext context)
	{
		super(context, By.xpath("id('__bookmark_1')//tbody//tr/th/div[text()='Some data']"));
	}
	
	public String getReportValue(int index)
	{
		return driver.findElement(By.xpath("id('__bookmark_1')//tbody//tr["+index+"]/td/div")).getText();
	}	
}
