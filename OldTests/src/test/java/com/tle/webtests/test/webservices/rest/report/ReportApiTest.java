package com.tle.webtests.test.webservices.rest.report;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.reporting.NoParamsReportWindow;
import com.tle.webtests.pageobject.reporting.ReportingPage;
import com.tle.webtests.test.files.Attachments;
import com.tle.webtests.test.reporting.UsersReportPage;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;
import com.tle.webtests.test.webservices.rest.OAuthClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.tle.webtests.framework.Assert.assertEquals;
import static com.tle.webtests.framework.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Requires an admin user "AutoTest", and assumes no REPORT ACLs are globally granted (such as to LOGGED_IN_USER)
 */
public class ReportApiTest extends AbstractRestApiTest {
	private static final String OAUTH_CLIENT_ID_BASE = "ReportApiTest";
	private static final String OAUTH_CLIENT_ID_ADMIN = OAUTH_CLIENT_ID_BASE + "Admin";
	private static final String OAUTH_CLIENT_ID_NO_ACCESS = OAUTH_CLIENT_ID_BASE + "NoAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_LIST_ACCESS = OAUTH_CLIENT_ID_BASE + "ListAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_CREATE_ACCESS = OAUTH_CLIENT_ID_BASE + "CreateAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_DELETE_ACCESS = OAUTH_CLIENT_ID_BASE + "DeleteAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_VIEW_ACCESS = OAUTH_CLIENT_ID_BASE + "ViewAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_EDIT_ACCESS = OAUTH_CLIENT_ID_BASE + "EditAccess" + System.currentTimeMillis();
	private static final String OAUTH_CLIENT_ID_ALL_ACCESS = OAUTH_CLIENT_ID_BASE + "AllAccess" + System.currentTimeMillis();

	private static final String USER_ADMIN = "AutoTest";
	private static final String USER_BASE = "ReportApiTestUser";
	private static final String USER_NO_ACCESS = USER_BASE + "NoAccess" + System.currentTimeMillis();
	private static final String USER_LIST_ACCESS = USER_BASE + "ListAccess" + System.currentTimeMillis();
	private static final String USER_CREATE_ACCESS = USER_BASE + "CreateAccess" + System.currentTimeMillis();
	private static final String USER_EDIT_ACCESS = USER_BASE + "EditAccess" + System.currentTimeMillis();
	private static final String USER_VIEW_ACCESS = USER_BASE + "ViewAccess" + System.currentTimeMillis();
	private static final String USER_DELETE_ACCESS = USER_BASE + "DeleteAccess" + System.currentTimeMillis();
	private static final String USER_ALL_ACCESS = USER_BASE + "AllAccess" + System.currentTimeMillis();

	private static final String API_PATH_REPORT = "api/report";
	private static final String API_PATH_REPORT_ACL = "api/report/acl";
	private static final String API_PATH_USER = "api/usermanagement/local/user";
	private static final String API_PATH_OAUTH = "api/oauth";
	private static final String API_PATH_STAGING = "api/staging";

	private static final String ACL_LIST_REPORT = "LIST_REPORT";
	private static final String ACL_CREATE_REPORT = "CREATE_REPORT";
	private static final String ACL_VIEW_REPORT = "VIEW_REPORT";
	private static final String ACL_DELETE_REPORT = "DELETE_REPORT";
	private static final String ACL_EDIT_REPORT = "EDIT_REPORT";
	private static final String ACL_EXECUTE_REPORT = "EXECUTE_REPORT";

	@Test
	public void testCreateReportNoAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_NO_ACCESS, OAUTH_CLIENT_ID_NO_ACCESS);

		ObjectNode rpt = mapper.createObjectNode();
		rpt.put("name", "Report " + System.currentTimeMillis());
		rpt.put("description", "desc");
		rpt.put("hideReport", "false");

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		HttpResponse postResponse = postEntity(rpt.toString(), uri.toString(), userUnderTestDetails.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		debug("Just called [%s] and received status [%s]", uri.toString(), postStatus);
		assertEquals(postStatus, 403);
		JsonNode resp = mapper.readTree(postResponse.getEntity().getContent());
		debug("Just called [%s] and received json [%s]", uri.toString(), resp);
		assertEquals(403, resp.get("code").asInt());
		assertEquals("Forbidden", resp.get("error").asText());
		assertEquals("You do not have the required privileges to access this object [CREATE_REPORT]", resp.get("error_description").asText());
	}

	@Test
	public void testReportEnd2End() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		grantUserReportPrivilege(adminDetails, adminDetails, ACL_EXECUTE_REPORT);

		// Setup creator
		AuthenticatedUserDetails creator = resolveUserDetails(adminDetails, USER_CREATE_ACCESS, OAUTH_CLIENT_ID_CREATE_ACCESS);
		grantUserReportPrivilege(adminDetails, creator, ACL_CREATE_REPORT);

		// Setup editor
		AuthenticatedUserDetails editor = resolveUserDetails(adminDetails, USER_EDIT_ACCESS, OAUTH_CLIENT_ID_EDIT_ACCESS);
		grantUserReportPrivilege(adminDetails, editor, ACL_EDIT_REPORT);

		// Setup viewer
		AuthenticatedUserDetails viewer = resolveUserDetails(adminDetails, USER_VIEW_ACCESS, OAUTH_CLIENT_ID_VIEW_ACCESS);
		grantUserReportPrivilege(adminDetails, viewer, ACL_VIEW_REPORT);

		// Setup lister
		AuthenticatedUserDetails lister = resolveUserDetails(adminDetails, USER_LIST_ACCESS, OAUTH_CLIENT_ID_LIST_ACCESS);
		grantUserReportPrivilege(adminDetails, lister, ACL_LIST_REPORT);

		// Setup deleter
		AuthenticatedUserDetails deleter  = resolveUserDetails(adminDetails, USER_DELETE_ACCESS, OAUTH_CLIENT_ID_DELETE_ACCESS);
		grantUserReportPrivilege(adminDetails, deleter, ACL_DELETE_REPORT);

		// Setup manager - for prep options
		AuthenticatedUserDetails reportManager = resolveUserDetails(adminDetails, USER_ALL_ACCESS, OAUTH_CLIENT_ID_ALL_ACCESS);

		grantUserReportPrivilege(adminDetails, reportManager, ACL_LIST_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_DELETE_REPORT);

		//Delete all existing reports
		resetEntities(reportManager, API_PATH_REPORT);

		//Upload test .rptdesign file
		String stagingUuid = provisionStagingArea(creator);
		String reportFilenameInEquella = "renamed-Users.rptdesign";
		uploadFile(creator, stagingUuid, "Users.rptdesign", reportFilenameInEquella);

		final String rptName = "Report E2E AutoTest " + System.currentTimeMillis();

		//Create report
		ObjectNode rpt = mapper.createObjectNode();
		rpt.put("name", rptName);
		rpt.put("description", "desc");
		rpt.put("hideReport", "false");
		rpt.put("filename", reportFilenameInEquella);
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"?staginguuid="+stagingUuid);
		HttpResponse postResponse = postEntity(rpt.toString(), uri.toString(), creator.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		debug("Just called [%s] to create a report and received [%s]", uri.toString(), postStatus);
		assertEquals(postStatus, 201);
		String location = postResponse.getFirstHeader("Location").getValue();
		debug("The Location header from the call [%s] is %s", uri.toString(), location);
		assertTrue(location.contains(API_PATH_REPORT));
		String reportUuid = location.substring((context.getBaseUrl() + API_PATH_REPORT).length()+1);

		confirmSingularReport(lister, viewer, reportUuid, rptName, "desc", "false", "reportFiles/"+reportFilenameInEquella);
		confirmDesignFile(viewer, reportUuid, null, "Users.rptdesign",reportFilenameInEquella);
		confirmExecuteReportAsAdmin(rptName);

		// Edit this report with a different file (zipped)

		// Upload a zipped report
		String stagingUuid2 = provisionStagingArea(creator);
		String reportFilenameInEquella2 = "rpts.zip";
		uploadFile(editor, stagingUuid2, "UsersReport.zip", reportFilenameInEquella2);

		ObjectNode rptUpdate = mapper.createObjectNode();
		rptUpdate.put("name", "Report E2E AutoTest EDITED!" + System.currentTimeMillis());
		rptUpdate.put("description", "an edited desc");
		rptUpdate.put("hideReport", "false");
		rptUpdate.put("filename", "Users_in_zip.rptdesign");
		uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid+"?staginguuid="+stagingUuid2+"&packagename="+reportFilenameInEquella2);
		HttpPut putReq = getPut(uri.toString(), rptUpdate.toString());
		HttpResponse putResponse = execute(putReq, false, editor.getToken());
		int status = putResponse.getStatusLine().getStatusCode();
		String respStr = superSerialResponse(putResponse);
		debug("Just called [%s] to edit a report and received [%s] - %s", uri.toString(), status, respStr);
		assertEquals(status, 200);

		confirmSingularReport(lister, viewer, reportUuid, rptUpdate.get("name").asText(), "an edited desc", "false", "reportFiles/Users_in_zip.rptdesign");
		confirmDesignFile(viewer, reportUuid, "UsersReport.zip", "Users_in_zip.rptdesign", "Users_in_zip.rptdesign");

		deleteReport(deleter, reportUuid);

		// check there's no available reports
		confirmNoReport(lister.getToken());
	}

	@Test
	public void testCreateReportAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_CREATE_ACCESS, OAUTH_CLIENT_ID_CREATE_ACCESS);

		grantUserReportPrivilege(adminDetails, userUnderTestDetails, ACL_CREATE_REPORT);

		ObjectNode rpt = mapper.createObjectNode();
		rpt.put("name", "Report " + System.currentTimeMillis());
		rpt.put("description", "desc");
		rpt.put("hideReport", "false");

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		HttpResponse postResponse = postEntity(rpt.toString(), uri.toString(), userUnderTestDetails.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		debug("Just called [%s] and received [%s]", uri.toString(), postStatus);
		assertEquals(postStatus, 201);
		// Returns the location of the report
		assertTrue(postResponse.getFirstHeader("Location").getValue().contains(API_PATH_REPORT));
	}

	@Test
	public void testReportLocks() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails user1Details = resolveUserDetails(adminDetails, USER_CREATE_ACCESS, OAUTH_CLIENT_ID_CREATE_ACCESS);
		AuthenticatedUserDetails user2Details = resolveUserDetails(adminDetails, USER_EDIT_ACCESS, OAUTH_CLIENT_ID_EDIT_ACCESS);

		grantUserReportPrivilege(adminDetails, user1Details, ACL_CREATE_REPORT);
		grantUserReportPrivilege(adminDetails, user1Details, ACL_EDIT_REPORT);

		grantUserReportPrivilege(adminDetails, user2Details, ACL_EDIT_REPORT);

		// Setup manager - for prep options
		AuthenticatedUserDetails reportManager = resolveUserDetails(adminDetails, USER_ALL_ACCESS, OAUTH_CLIENT_ID_ALL_ACCESS);

		grantUserReportPrivilege(adminDetails, reportManager, ACL_VIEW_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_LIST_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_DELETE_REPORT);

		//Delete all existing reports
		resetEntities(reportManager, API_PATH_REPORT);

		// Create a basic report
		long random = System.currentTimeMillis();
		String reportUuid = createArbitraryReportNoFile(adminDetails, user1Details, random);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);

		// Lock the report as user1
		String lock = obtainReportLock(user1Details, reportUuid, 201);
		assertNotNull(lock, "Lock uuid should not be null");

		// Try to lock the report as user2 - confirm failure
		String secondLock = obtainReportLock(user2Details, reportUuid, 409);
		assertNull(secondLock, "Second try of locking reporting should not have worked.");

		// Try to edit the report as user2 - confirm failure
		final String updatedName = "Report edited: " + System.currentTimeMillis();
		final String updatedDesc = "an edited desc";
		final String updatedHideReport = "true";

		ObjectNode rptUpdate = mapper.createObjectNode();
		rptUpdate.put("name", updatedName);
		rptUpdate.put("description", updatedDesc);
		rptUpdate.put("hideReport", updatedHideReport);

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid);
		HttpPut putReq = getPut(uri.toString(), rptUpdate.toString());
		HttpResponse putResponse = execute(putReq, false, user2Details.getToken());
		int status = putResponse.getStatusLine().getStatusCode();
		String respStr = superSerialResponse(putResponse);
		debug("Just called [%s] as user2 to edit a report and received [%s] - %s", uri.toString(), status, respStr);
		assertEquals(status, 409);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);

		// Try to edit the report as user1 - confirm success
		uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid + "?keeplocked=true&lock="+lock);
		putReq = getPut(uri.toString(), rptUpdate.toString());
		putResponse = execute(putReq, false, user1Details.getToken());
		status = putResponse.getStatusLine().getStatusCode();
		respStr = superSerialResponse(putResponse);
		debug("Just called ItemCount[%s] as user1 to edit a report and received [%s] - %s", uri.toString(), status, respStr);
		assertEquals(status, 200);
		confirmSingularReport(reportManager, reportManager, reportUuid, updatedName, updatedDesc, updatedHideReport, null);

		// Read the lock for the report as user1
		JsonNode readLockTest = readReportLock(user1Details, reportUuid);
		assertTrue(readLockTest.has("uuid"));
		assertEquals(readLockTest.get("uuid").getTextValue(), lock);

		// Read the lock for the report as user2
		readLockTest = readReportLock(user2Details, reportUuid);
		assertTrue(readLockTest.has("uuid"));
		assertEquals(readLockTest.get("uuid").getTextValue(), lock);

		// unlock the report as user1
		assertEquals(deleteReportLock(user1Details, reportUuid), 204);

		// Try to edit the report as user2 - confirm success
		final String updatedName2 = "Report edited again: " + System.currentTimeMillis();
		final String updatedDesc2 = "an edited desc again";
		final String updatedHideReport2 = "false";

		rptUpdate = mapper.createObjectNode();
		rptUpdate.put("name", updatedName2);
		rptUpdate.put("description", updatedDesc2);
		rptUpdate.put("hideReport", updatedHideReport2);

		uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid);
		putReq = getPut(uri.toString(), rptUpdate.toString());
		putResponse = execute(putReq, false, user1Details.getToken());
		status = putResponse.getStatusLine().getStatusCode();
		respStr = superSerialResponse(putResponse);
		debug("Just called [%s] as user1 to edit a report and received [%s] - %s", uri.toString(), status, respStr);
		assertEquals(status, 200);
		confirmSingularReport(reportManager, reportManager, reportUuid, updatedName2, updatedDesc2, updatedHideReport2, null);

		// Read the lock for the report as user1 - confirm no lock
		readLockTest = readReportLock(user1Details, reportUuid);
		assertTrue(readLockTest.has("code"));
		assertEquals(readLockTest.get("code").asInt(), 404);
	}

	@Test
	public void testListReportNoAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_NO_ACCESS, OAUTH_CLIENT_ID_NO_ACCESS);

		createArbitraryReportNoFile(adminDetails, adminDetails);

		// Confirm no reports are listed.
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		JsonNode result = getEntity(uri.toString(), userUnderTestDetails.getToken());
		assertNotNull(result);
		assertTrue(result.has("available"));
		assertEquals(result.get("available").asInt(), 0, "List reports should not have returned any results");
	}

	@Test
	public void testDeleteReportNoAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_NO_ACCESS, OAUTH_CLIENT_ID_NO_ACCESS);

		// Setup manager - for prep options
		AuthenticatedUserDetails reportManager = resolveUserDetails(adminDetails, USER_ALL_ACCESS, OAUTH_CLIENT_ID_ALL_ACCESS);

		grantUserReportPrivilege(adminDetails, reportManager, ACL_VIEW_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_LIST_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_DELETE_REPORT);

		//Delete all existing reports
		resetEntities(reportManager, API_PATH_REPORT);

		// Create a basic report
		long random = System.currentTimeMillis();
		String reportUuid = createArbitraryReportNoFile(adminDetails, adminDetails, random);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);

		// Confirm delete is blocked
		int deleteStatus = deleteReport(userUnderTestDetails, reportUuid);
		assertEquals(deleteStatus, 403);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);
	}

	@Test
	public void testEditReportNoAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_NO_ACCESS, OAUTH_CLIENT_ID_NO_ACCESS);

		// Setup manager - for prep options
		AuthenticatedUserDetails reportManager = resolveUserDetails(adminDetails, USER_ALL_ACCESS, OAUTH_CLIENT_ID_ALL_ACCESS);

		grantUserReportPrivilege(adminDetails, reportManager, ACL_VIEW_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_LIST_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_DELETE_REPORT);

		//Delete all existing reports
		resetEntities(reportManager, API_PATH_REPORT);

		// Create a basic report
		long random = System.currentTimeMillis();
		String reportUuid = createArbitraryReportNoFile(adminDetails, adminDetails, random);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);

		// Confirm edit is blocked
		ObjectNode rptUpdate = mapper.createObjectNode();
		rptUpdate.put("name", "Report edit - " + System.currentTimeMillis());
		rptUpdate.put("description", "Updated Desc");
		rptUpdate.put("hideReport", "true");

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid);
		HttpPut putReq = getPut(uri.toString(), rptUpdate.toString());
		HttpResponse putResponse = execute(putReq, false, userUnderTestDetails.getToken());
		int status = putResponse.getStatusLine().getStatusCode();
		String respStr = superSerialResponse(putResponse);
		debug("Just called [%s] to edit a report (should not work) and received [%s] - %s", uri.toString(), status, respStr);
		assertEquals(status, 403);

		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);
	}

	@Test
	public void testViewReportNoAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_NO_ACCESS, OAUTH_CLIENT_ID_NO_ACCESS);

		// Setup manager - for prep options
		AuthenticatedUserDetails reportManager = resolveUserDetails(adminDetails, USER_ALL_ACCESS, OAUTH_CLIENT_ID_ALL_ACCESS);

		grantUserReportPrivilege(adminDetails, reportManager, ACL_VIEW_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_LIST_REPORT);
		grantUserReportPrivilege(adminDetails, reportManager, ACL_DELETE_REPORT);

		//Delete all existing reports
		resetEntities(reportManager, API_PATH_REPORT);

		// Create a basic report
		long random = System.currentTimeMillis();
		String reportUuid = createArbitraryReportNoFile(adminDetails, adminDetails, random);
		confirmSingularReport(reportManager, reportManager, reportUuid, "Report "+random, "desc", "false", null);

		// Confirm unable to view
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+reportUuid);
		JsonNode report = getEntityOkIfNull(uri.toString(), userUnderTestDetails.getToken());
		assertEquals(report.get("code").asInt(), 403);
	}

	@Test
	public void testListReportAccess() throws Exception {
		AuthenticatedUserDetails adminDetails = resolveUserDetails(null, USER_ADMIN, OAUTH_CLIENT_ID_ADMIN);
		AuthenticatedUserDetails userUnderTestDetails = resolveUserDetails(adminDetails, USER_LIST_ACCESS, OAUTH_CLIENT_ID_LIST_ACCESS);

		grantUserReportPrivilege(adminDetails, userUnderTestDetails, ACL_LIST_REPORT);

		createArbitraryReportNoFile(adminDetails, adminDetails);

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		JsonNode result = getEntity(uri.toString(), userUnderTestDetails.getToken());
		assertNotNull(result);
		assertTrue(result.has("available"));
		assertTrue(result.get("available").asInt() > 0);
	}

    /**
     * @see AbstractRestApiTest#addOAuthClients(List)
     */
    @Override
    protected void addOAuthClients(List<Pair<String, String>> clients) {
        clients.add(new Pair<String, String>(OAUTH_CLIENT_ID_ADMIN, USER_ADMIN));
    }

    private void confirmExecuteReportAsAdmin(String reportName) {
		logon("AutoTest", "automated");
		ReportingPage reports = new ReportingPage(context).load();
		NoParamsReportWindow<UsersReportPage> report = reports.getReport(reportName, new UsersReportPage(context));
		UsersReportPage usersReport = report.getReport();
		Assert.assertEquals(usersReport.getUserName(0), "AutoLogin");
		report.close();
		logout();
	}

	// Returns the staging UUID
	protected String provisionStagingArea(AuthenticatedUserDetails details) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_STAGING);
		HttpResponse postResponse = postEntity("{}", uri.toString(), details.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		String location = postResponse.getFirstHeader("Location").getValue().substring(uri.toString().length()+1);
		debug("Just called [%s] and received [%s] with a Location header of %s", uri.toString(), postStatus, location);
		assertEquals(postStatus, 201);
		return location;
	}

	// Returns the lock UUID
	private String obtainReportLock(AuthenticatedUserDetails details, String reportUuid, int expectedStatus) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + reportUuid + "/lock");
		HttpResponse postResponse = postEntity("{}", uri.toString(), details.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		JsonNode resp = mapper.readTree(postResponse.getEntity().getContent());
		debug("Just called [%s] as [%s] and received status [%s] and response [%s]", uri.toString(), details.getUsername(), postStatus, resp);
		assertEquals(postStatus, expectedStatus);
		if(expectedStatus == 201) {
			return resp.get("uuid").getTextValue();
		}
		return null;
	}

	private JsonNode readReportLock(AuthenticatedUserDetails details, String reportUuid) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + reportUuid + "/lock");
		JsonNode resp = getEntityOkIfNull(uri.toString(), details.getToken());
		debug("Just called [%s] as [%s] and received [%s]", uri.toString(), details.getUsername(), resp);
		return resp;
	}

	private int deleteReportLock(AuthenticatedUserDetails details, String reportUuid) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + reportUuid + "/lock");
		HttpResponse delResp = deleteResource(uri.toString(), details.getToken());
		int status = delResp.getStatusLine().getStatusCode();
		debug("Just called [%s] to delete a report and received [%s]", uri.toString(), status);
		return status;
	}

	protected void uploadFile(AuthenticatedUserDetails details, String stagingUUID, String filename, String filenameInEquella) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_STAGING + "/" + stagingUUID + "/" + filenameInEquella);
		final File file = new File(AbstractPage.getPathFromUrl(Attachments.get(filename)));
		debug("Disk location of file to upload: %s", file.getAbsolutePath());
		HttpResponse response = execute(getPut(uri.toString(), file), true);
		int code = response.getStatusLine().getStatusCode();
		debug("uploadFile response code: %s", code);
		assertEquals(code, 200, "200 was not returned from PUT file");
		debug("Uploaded file to: %s", uri.toString());
	}

	protected String createArbitraryReportNoFile(AuthenticatedUserDetails admin, AuthenticatedUserDetails details) throws Exception {
		return createArbitraryReportNoFile(admin, details, System.currentTimeMillis());
	}

	protected String createArbitraryReportNoFile(AuthenticatedUserDetails admin, AuthenticatedUserDetails details, long rand) throws Exception {
		ObjectNode rpt = mapper.createObjectNode();
		rpt.put("name", "Report " + System.currentTimeMillis());
		rpt.put("description", "desc");
		rpt.put("hideReport", "false");

		// Create the report so there is at least 1 result
		grantUserReportPrivilege(admin, details, ACL_CREATE_REPORT);
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		HttpResponse postResponse = postEntity(rpt.toString(), uri.toString(), details.getToken(), false);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		debug("Just called [%s] and received [%s]", uri.toString(), postStatus);
		assertEquals(postStatus, 201);
		String locationLink = postResponse.getFirstHeader("Location").getValue();
		return locationLink.substring((context.getBaseUrl() + API_PATH_REPORT).length()+1);
	}

	protected AuthenticatedUserDetails resolveUserDetails(AuthenticatedUserDetails adminDetails, String username, String oauthId) throws Exception {
		AuthenticatedUserDetails aud = new AuthenticatedUserDetails(username);

		// Try to obtain token
		aud.setToken(requestToken(oauthId));
		if((aud.getToken() == null) && (adminDetails == null)) {
			// Not enough information to build out user details.
			assertTrue(false, "Trying to resolve user details for "+username+" and need admin details.");
		}

		// Assume if adminDetails == null, the user is the admin user
		String token = (adminDetails == null) ? aud.getToken() : adminDetails.getToken();

		// Try to obtain ID
		aud.setId(getUserId(token, username));
		if(aud.getId() == null) {
			// Assume user doesn't exist.  Create and retry for ID
			addUser(token, username);
			aud.setId(getUserId(token, username));
			assertNotNull(aud.getId(), "Trying to resolve user details for "+username+" and could not create user / find ID.");
		}

		if(aud.getToken() == null) {
			// OAuth client doesn't exist.  Create and retry for token
			addOAuth(token, oauthId, aud.getId(), username);
			aud.setToken(requestToken(oauthId));
			assertNotNull(aud.getToken(), "Trying to resolve user details for "+username+" and could not obtain a token.");
		}
		return aud;
	}

	protected void addUser(String token, String user) throws Exception {
		ObjectNode addUserNode = mapper.createObjectNode();
		addUserNode.put("username", user);
		addUserNode.put("firstName", user);
		addUserNode.put("lastName", user);
		ObjectNode exportNode = mapper.createObjectNode();
		exportNode.put("passwordHash", "asdf");
		addUserNode.put("_export", exportNode);

		URI uri = new URI(context.getBaseUrl() + API_PATH_USER);
		HttpResponse postResponse = postEntity(addUserNode.toString(), uri.toString(), token, true);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		assertTrue(postStatus == 200 || postStatus == 201);
	}

	/**
	 *
	 * @param token
	 * @param user
	 * @return the Equella ID for the user; null if that username cant' be found.
	 * @throws Exception
	 */
	protected String getUserId(String token, String user) throws Exception {
		URI getUsernameUri = new URI(context.getBaseUrl() + API_PATH_USER+"/username/"+user);
		JsonNode userJson = getEntityOkIfNull(getUsernameUri.toString(), token);
		if((userJson != null) && userJson.has("id")) {
			String userId = userJson.get("id").asText();
			debug("getUserId:  username=[%s], id=[%s]", user, userId);
			return userId;
		}
		return null;
	}


	protected void addOAuth(String token, String id, String userId, String username) throws Exception {
		ObjectNode node = mapper.createObjectNode();
		node.put("name", id);
		node.put("clientId", id);
		node.put("clientSecret", id);
		node.put("redirectUrl", "default");
		node.put("userId", userId);
		URI uri = new URI(context.getBaseUrl() + API_PATH_OAUTH);
		debug("addOAuth calling=[%s] with [%s]", uri.toString(), node.toString());
		HttpResponse postResponse = postEntity(node.toString(), uri.toString(), token, true);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		assertTrue(postStatus == 200 || postStatus == 201);

		//Cache it
		OAuthClient oClient = new OAuthClient();
		oClient.setName(id);
		oClient.setClientId(id);
		oClient.setSecret(id);
		oClient.setUsername(username);
		clients.add(oClient);
	}

	/**
	 *
	 * @param user
	 * @param reportUuid
	 * @param originalZip null if original upload was a single rptdesign file
	 * @param originalFilename if zipped, the name of the rptdesign file in the zip
	 * @param equellaFilename the name of the rptdesign file in Equella
	 * @throws Exception
	 */
	protected void confirmDesignFile(AuthenticatedUserDetails user,
									 String reportUuid,
									 String originalZip,
									 String originalFilename,
									 String equellaFilename) throws Exception {
		// Create staging area
		String stagingUuid = provisionStagingArea(user);

		// Package design file(s)
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + reportUuid +"/package");
		HttpResponse postResponse = postEntity("{}", uri.toString(), user.getToken(), true);
		int postStatus = postResponse.getStatusLine().getStatusCode();
		assertTrue(postStatus == 201);
		String location = postResponse.getFirstHeader("Location").getValue();
		debug("Called [%s].  The download location for the report design file is %s", uri.toString(), location);

		// Download packaged file from staging area
		final File downloadedFile = File.createTempFile("ReportApiTest",".zip");
		downloadedFile.deleteOnExit();
		HttpResponse downloadResp = download(location, downloadedFile, user.getToken());
		int downloadStatus = downloadResp.getStatusLine().getStatusCode();
		debug("Downloaded report file to %s.  Status is %s", downloadedFile.getAbsolutePath(), downloadStatus);
		assertEquals(downloadStatus, 200);

		File originalFile = null;
		if(originalZip != null) {
			File originalZipFile = new File(AbstractPage.getPathFromUrl(Attachments.get(originalZip)));
			File originalUnzippedDir = unzipFile(originalZipFile);
			originalFile = new File(originalUnzippedDir.getAbsolutePath() + "/" + originalFilename);
		} else {
			originalFile = new File(AbstractPage.getPathFromUrl(Attachments.get(originalFilename)));
		}

		// Confirm download and original files are the same (hashed)
		File unzippedDirectory = unzipFile(downloadedFile);
		File targetDesignFile = new File(unzippedDirectory.getAbsolutePath()+"/reportFiles/"+equellaFilename);
		debug("Comparing the original [%s] and downloaded [%s] report files.", originalFile.getAbsolutePath(), targetDesignFile.getAbsolutePath());
		final String origMd5 = md5(originalFile);
		final String echoedMd5 = md5(targetDesignFile);
		assertEquals(origMd5, echoedMd5, "Original and downloaded>unzipped files do not match.");
	}

	protected void grantUserReportPrivilege(AuthenticatedUserDetails adminUser, AuthenticatedUserDetails targetUser, String reportPriv) throws Exception {
		ObjectNode existingRule = checkUserReportPrivilege(adminUser.getToken(), targetUser, reportPriv);
		if(existingRule != null) {
			// Rule exists.   Could be enchanced to confirm GRANT vs REVOKE
			debug("grantUserReportPrivilege (%s):  Rule already in place: %s - %s", targetUser.getUsername(), existingRule.get("privilege"), existingRule.get("who"));
			return;
		}

		ObjectNode rule = mapper.createObjectNode();
		rule.put("granted", true);
		rule.put("override", false);
		rule.put("privilege", reportPriv);
		rule.put("who", "U:"+targetUser.getId());

		ArrayNode rulesArray = mapper.createArrayNode();
		rulesArray.add(rule);
		ObjectNode topLevel = mapper.createObjectNode();
		topLevel.put("rules", rulesArray);

		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT_ACL);
		HttpPut putReq = getPut(uri.toString(), topLevel.toString());
		HttpResponse putResponse = execute(putReq, true, adminUser.getToken());
		int status = putResponse.getStatusLine().getStatusCode();
		assertTrue(status == 200 || status == 201);
		debug("grantUserReportPrivilege (%s):  Rule created: %s - %s", targetUser.getUsername(), rule.get("privilege"), rule.get("who"));
	}

	protected int deleteReport(AuthenticatedUserDetails userDetails, String uuid) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + uuid);
		HttpResponse delResp = deleteResource(uri.toString(), userDetails.getToken());
		int status = delResp.getStatusLine().getStatusCode();
		debug("Just called [%s] to delete a report and received [%s]", uri.toString(), status);
		return status;
	}

	// Important to create this so the Admin user will be default have necessary privs.
	protected ObjectNode checkUserReportPrivilege(String token, AuthenticatedUserDetails targetUser, String reportPriv) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT_ACL);
		JsonNode results = getEntity(uri.toString(), token);
		if(results.has("rules")) {
			ArrayNode rules = (ArrayNode) results.get("rules");
			for(JsonNode rule : rules) {
				if(rule.get("who").toString().contains("U:"+targetUser.getId()) &&
						rule.get("who").toString().equals(reportPriv)) {
					debug("checkUserReportPrivilege (%s):  Rule matched: %s - %s", targetUser.getUsername(), rule.get("privilege"), rule.get("who"));
					return (ObjectNode) rule;
				}
			}
		} else {
			debug("checkUserReportPrivilege (%s):  No rules found for %s", targetUser.getUsername(), API_PATH_REPORT_ACL);
			return null;
		}
		debug("checkUserReportPrivilege (%s):  No rules matched for %s - %s", targetUser.getUsername(), reportPriv, targetUser.getId());

		return null;
	}

	// Reset entities (ie reports), but should be the same for all entities
	protected void resetEntities(AuthenticatedUserDetails user, String apiPath) throws Exception {
		URI uri = new URI(context.getBaseUrl() + apiPath);

		JsonNode results = getEntity(uri.toString(), user.getToken());
		debug("resetEntities:  listed entities via user [" + user.getUsername()  + "]: "+results.toString());
		while(results.has("results") && (results.get("length").asInt() > 0)) {
			debug("resetEntities [%s]: deleting a batch of %s", apiPath, results.get("length").asInt());
			ArrayNode entities = (ArrayNode) results.get("results");
			for(JsonNode entity : entities) {
				String uuid = entity.get("uuid").asText();
				debug("resetEntities:  deleting %s of type %s", uuid, apiPath);
				int reportDelStatus = deleteReport(user, uuid);
				if(reportDelStatus == 409) {
					// Report failed to be deleted.  Try to unlock it and retry
					URI lockUri = new URI(context.getBaseUrl() + API_PATH_REPORT + "/" + uuid + "/lock");
					HttpResponse delResp = deleteResource(lockUri.toString(), user.getToken());
					int lockStatus = delResp.getStatusLine().getStatusCode();
					debug("Report locked!  Just called [%s] to delete a report lock and received [%s]", lockUri.toString(), lockStatus);
					assertEquals(lockStatus, 204);
					// Try to delete the report again
					assertEquals(deleteReport(user, uuid), 204);
				} else {
					// Fail 'nicely'.
					assertEquals(reportDelStatus, 204);
				}
			}
			results = getEntity(uri.toString(), user.getToken());
		}
		debug("resetEntities:  Reset Complete");
	}


	protected void confirmSingularReport(AuthenticatedUserDetails lister,
										 AuthenticatedUserDetails viewer,
										 String expectedUuid,
										 String expectedName,
										 String expectedDescription,
										 String expectedHideReport,
										 String expectedFilename) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		JsonNode results = getEntity(uri.toString(), lister.getToken());
		assertNotNull(results);
		debug("confirmSingularReport:  listed entities via user [" + lister.getUsername()  + "]: "+results.toString());
		assertTrue(results.has("available"));
		assertEquals(results.get("available").asInt(), 1);
		JsonNode report = ((ArrayNode) results.get("results")).get(0);
		assertEquals(report.get("uuid").asText(), expectedUuid);
		assertEquals(report.get("name").asText(), expectedName);
		assertFalse(report.has("description"));
		assertEquals(report.get("hideReport").asText(), expectedHideReport);
		if(expectedFilename == null) {
			assertEquals(report.get("filename").asText(), "reportFiles/null");
		} else {
			assertEquals(report.get("filename").asText(), expectedFilename);
		}
		uri = new URI(context.getBaseUrl() + API_PATH_REPORT+"/"+expectedUuid);
		report = getEntity(uri.toString(), viewer.getToken());
		assertEquals(report.get("uuid").asText(), expectedUuid);
		assertEquals(report.get("name").asText(), expectedName);
		assertEquals(report.get("description").asText(), expectedDescription);
		assertEquals(report.get("hideReport").asText(), expectedHideReport);
		if(expectedFilename == null) {
			assertEquals(report.get("filename").asText(), "reportFiles/null");
		} else {
			assertEquals(report.get("filename").asText(), expectedFilename);
		}
	}

	protected void confirmNoReport(String token) throws Exception {
		URI uri = new URI(context.getBaseUrl() + API_PATH_REPORT);
		JsonNode results = getEntity(uri.toString(), token);
		assertNotNull(results);
		assertTrue(results.has("available"));
		assertTrue(results.get("available").asInt() == 0);
		assertTrue(results.has("length"));
		assertTrue(results.get("length").asInt() == 0);
	}

	private String md5(File file) throws IOException
	{
		InputStream inp = new FileInputStream(file);
		try
		{
			return DigestUtils.md5Hex(inp);
		}
		finally
		{
			Closeables.closeQuietly(inp);
		}
	}

	private File unzipFile(File zipFile) throws IOException {
		File outdir = Files.createTempDir();
		outdir.deleteOnExit();
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry zipEntry = zis.getNextEntry();
		while(zipEntry != null){
			String zipFileName = zipEntry.getName();
			String baseWithSubDirectories = outdir.getAbsolutePath() + "/";
			String filename = baseWithSubDirectories + zipFileName;
			int realFileNameIndex = zipFileName.lastIndexOf("/");
			if(realFileNameIndex != -1) {
				//There's a directory structure
				baseWithSubDirectories = baseWithSubDirectories + zipFileName.substring(0, realFileNameIndex);
				filename = baseWithSubDirectories + zipFileName.substring(realFileNameIndex);
			}
			File newDir = new File(baseWithSubDirectories);
			newDir.mkdirs();
			newDir.deleteOnExit();
			File newFile = new File(filename);
			newFile.deleteOnExit();
			debug("Unzipping: %s",newFile.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
		return outdir;
	}
}


