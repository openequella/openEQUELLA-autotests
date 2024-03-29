package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class RecentContributionsEditPage extends AbstractPortalEditPage<RecentContributionsEditPage>
{
	public static String LIVE = "live";
	public static String DRAFT = "draft";

	private EquellaSelect stausList;
	private EquellaSelect displayList;

	public RecentContributionsEditPage(PageContext context)
	{
		super(context);
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		stausList = new EquellaSelect(context, driver.findElement(By.id("rct_s")));
		displayList = new EquellaSelect(context, driver.findElement(By.id("rct_d")));
	}

	@Override
	public String getType()
	{
		return "Recent contributions";
	}

	@Override
	public String getId()
	{
		return "rct";
	}

	public void setStatus(String status)
	{
		stausList.selectByValue(status);
	}

	public void setQuery(String query)
	{
		driver.findElement(By.id("rct_q")).sendKeys(query);
	}

	public void setAge(String age)
	{
		driver.findElement(By.id("rct_a")).sendKeys(age);
	}

	public void setDisplayTitleOnly(boolean titleOnly)
	{
		if( titleOnly )
			displayList.selectByValue("titleOnly");
	}

}
