package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("fiveo")
public class WizardConfigTest extends AbstractSessionTest
{
	// DTEC 14461
	@Test
	public void wizardConfigTest()
	{
		final ArrayList<String> pages = Lists.newArrayList("Name", "Description", "Attachments");

		// Logon
		logon("AutoTest", "automated");

		// Load basic contribution wizard
		ContributePage contribPage = new ContributePage(context).load();
		WizardPageTab wizardPage = contribPage.openWizard("Wizard Config - None").get();
		wizardPage.editbox(1, "Wizard Config Test");

		// Check pages are text
		assertTrue(wizardPage.hasPage("Name *", false));
		assertTrue(wizardPage.hasPage("Description", false));
		assertTrue(wizardPage.hasPage("Attachments", false));

		// Check button text Next and Prev
		assertEquals(wizardPage.getNextButtonText(), "Next");
		wizardPage.next();
		assertEquals(wizardPage.getPrevButtonText(), "Prev");
		assertEquals(wizardPage.getNextButtonText(), "Next");
		wizardPage.next();
		assertEquals(wizardPage.getPrevButtonText(), "Prev");

		assertTrue(wizardPage.hasPage("Name", true) && wizardPage.hasPage("Description", true)
			&& wizardPage.hasPage("Attachments", false));

		// Click through pages check active page is text
		assertPages(wizardPage, pages, false);

		// Load advanced contribution wizard
		contribPage = wizardPage.cancel(contribPage);
		wizardPage = contribPage.openWizard("Wizard Config - All").get();
		wizardPage.editbox(1, "Wizard Config Test");

		// Check button links and navigate using them
		assertTrue(wizardPage.hasPage("Name *", false), "Name page is disabled");
		assertTrue(wizardPage.hasPage("Description", true), "Description page is enabled");
		assertTrue(wizardPage.hasPage("Attachments", true), "Attachment page is enabled");

		// Click through pages check active page is text
		assertPages(wizardPage, pages, true);

		// Check button text
		assertEquals(wizardPage.getNextButtonText(), "Description");
		wizardPage.next();
		assertEquals(wizardPage.getPrevButtonText(), "Name");
		assertEquals(wizardPage.getNextButtonText(), "Attachments");
		wizardPage.next();
		assertEquals(wizardPage.getPrevButtonText(), "Description");

		wizardPage.cancel(contribPage);
	}

	private void assertPages(WizardPageTab wizardPage, List<String> pages, boolean reverse)
	{
		if( reverse )
			Collections.reverse(pages);
		for( String page : pages )
		{
			wizardPage.clickPage(page);
			assertEquals(wizardPage.getCurrentPageName(), page);
		}
	}
}