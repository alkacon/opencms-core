/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ugc;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.ugc.CmsUgcSession.PathComparator;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Optional;

import junit.framework.Test;

/**
 * Tests the form session methods.<p>
 */
public class TestFormSession extends OpenCmsTestCase {

    /** Schema id. */
    private static final String SCHEMA_ID_IMAGE = "http://www.opencms.org/image.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_LINK = "http://www.opencms.org/link.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_PARAGRAPH = "http://www.opencms.org/paragraph.xsd";

    /** Schema id. */
    private static final String SCHEMA_ID_TEXTBLOCK = "http://www.opencms.org/textblock.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestFormSession(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        Test wrapper = generateSetupTestWrapper(TestFormSession.class, "simpletest", "/");
        return wrapper;
    }

    /**
     * Publishes the offline project.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public void publishAll() throws CmsException {

        OpenCms.getPublishManager().publishProject(getCmsObject(), new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests the add values method.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testAddValues() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        CmsXmlContentDefinition definition = unmarshalDefinition(resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_ID_TEXTBLOCK,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        String fileContent = CmsFileUtil.readFile("org/opencms/ugc/tb_00001.xml", CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(fileContent, CmsEncoder.ENCODING_UTF_8, resolver);
        CmsUgcSession session = new CmsUgcSession(getCmsObject());
        Locale editLocale = new Locale("en");
        Map<String, String> values = session.getContentValues(xmlContent, editLocale);

        xmlContent.removeLocale(editLocale);
        session.addContentValues(xmlContent, editLocale, values);

        // all content values should be restored
        assertEquals(fileContent, new String(xmlContent.marshal(), CmsEncoder.ENCODING_UTF_8));
    }

    /**
     * Tests that created resources are created in the correct project.<p>
     *
     * @throws Exception -
     */
    public void testAssignProjectToCreatedResources() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsResource uploadFolder = cms.createResource("/" + getName() + "Upload", 0);
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        CmsUgcSession session = new CmsUgcSession(cms, config);
        CmsResource contentRes = session.createXmlContent();
        CmsResource uploadRes = session.createUploadResource(
            randomFieldName(),
            "example.bin",
            new byte[] {1, 2, 3, 4, 5});
        CmsProject expectedProject = session.getProject();
        assertEquals(
            "Project id doesn't match session project",
            expectedProject.getUuid(),
            contentRes.getProjectLastModified());
        assertEquals(
            "Project id doesn't match session project",
            expectedProject.getUuid(),
            uploadRes.getProjectLastModified());
    }

    /**
     * Tests automatic publishing.<p>
     *
     * @throws Exception -
     */
    public void testAutoPublish() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        String newUserName = "user_" + getName();
        String password = "password";
        cms.createUser(newUserName, password, "test", new HashMap<String, Object>());
        cms.addUserToGroup(newUserName, "Users");
        CmsObject userCms = OpenCms.initCmsObject(cms);
        userCms.loginUser(newUserName, password);
        userCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsResource uploadFolder = cms.createResource("/" + getName() + "Upload", 0);
        publishAll();
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            true,
            Optional.<List<String>> absent());

        CmsUgcSession session = new CmsUgcSession(cms, userCms, config);
        CmsResource contentRes = session.createXmlContent();
        CmsResource uploadRes = session.createUploadResource(
            randomFieldName(),
            "example.bin",
            new byte[] {1, 2, 3, 4, 5});
        session.finish();
        OpenCms.getPublishManager().waitWhileRunning();
        contentRes = cms.readResource(contentRes.getStructureId());
        assertEquals(CmsResource.STATE_UNCHANGED, contentRes.getState());
        uploadRes = cms.readResource(uploadRes.getStructureId());
        assertEquals(CmsResource.STATE_UNCHANGED, uploadRes.getState());
    }

    /**
     * Tests creation of new contents.<p>
     *
     * @throws Exception -
     */
    public void testCreateContent() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        CmsUgcSession session = new CmsUgcSession(cms, config);
        CmsResource createdContent = session.createXmlContent();
        assertTrue(
            "The content should be created in the content folder",
            createdContent.getRootPath().startsWith(contentFolder.getRootPath()));
        assertEquals(
            createdContent.getTypeId(),
            OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId());

        /* Try with a different resource type. */
        config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "plain",
            contentFolder,
            "n1_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        session = new CmsUgcSession(cms, config);
        createdContent = session.createXmlContent();
        assertEquals(createdContent.getTypeId(), OpenCms.getResourceManager().getResourceType("plain").getTypeId());
    }

    /**
     * Tests the add values method.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testDeleteValues() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        CmsXmlContentDefinition definition = unmarshalDefinition(resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_ID_TEXTBLOCK,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        String fileContent = CmsFileUtil.readFile("org/opencms/ugc/tb_00002.xml", CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(fileContent, CmsEncoder.ENCODING_UTF_8, resolver);
        CmsUgcSession session = new CmsUgcSession(getCmsObject());
        Locale editLocale = new Locale("en");
        Map<String, String> values = new HashMap<String, String>();
        values.put("Paragraph/Link[1]", null);
        values.put("Paragraph/Link[2]", null);
        values.put("Paragraph/Link[3]", null);
        values.put("Paragraph/Link[4]", null);
        session.addContentValues(xmlContent, editLocale, values);

        String fileContentNoLinks = CmsFileUtil.readFile(
            "org/opencms/ugc/tb_00002_no_links.xml",
            CmsEncoder.ENCODING_UTF_8);
        // all content values should be restored
        assertEquals(fileContentNoLinks, new String(xmlContent.marshal(), CmsEncoder.ENCODING_UTF_8));
    }

    /**
     * Tests that editing multiple files in a single form session causes an error.<p>
     *
     * @throws Exception -
     */
    public void testFailEditMultipleContents() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.<CmsResource> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());

        cms.createResource("/" + getName() + "/file1.txt", 1);
        cms.createResource("/" + getName() + "/file2.txt", 1);

        CmsUgcSession session;

        // case create -> load
        session = new CmsUgcSession(cms, config);
        session.createXmlContent();
        try {

            session.loadXmlContent("file1.txt");
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsUgcException e) {
            // ok
        }

        // case create -> create
        session = new CmsUgcSession(cms, config);
        session.createXmlContent();
        try {
            session.createXmlContent();
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsUgcException e) {
            // ok
        }

        // case load -> create
        session = new CmsUgcSession(cms, config);
        session.loadXmlContent("file1.txt");
        try {
            session.createXmlContent();
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsUgcException e) {
            // ok
        }

        // case load -> load
        session = new CmsUgcSession(cms, config);
        session.loadXmlContent("file1.txt");
        try {
            session.loadXmlContent("file2.txt");
            fail("Should not be able to edit more than one file in a session.");
        } catch (CmsUgcException e) {
            // ok
        }

    }

    /**
     * Tests the get values method.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testGetValues() throws Exception {

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        CmsXmlContentDefinition definition = unmarshalDefinition(resolver);
        CmsXmlEntityResolver.cacheSystemId(
            SCHEMA_ID_TEXTBLOCK,
            definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
        String fileContent = CmsFileUtil.readFile("org/opencms/ugc/tb_00001.xml", CmsEncoder.ENCODING_UTF_8);
        // now create the XML content
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(fileContent, CmsEncoder.ENCODING_UTF_8, resolver);
        CmsUgcSession session = new CmsUgcSession(getCmsObject());
        Map<String, String> values = session.getContentValues(xmlContent, new Locale("en"));
        assertEquals("Full width example", values.get("Title[1]"));
    }

    /**
     * Tests the comparator used for xpath ordering.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testPathComparator() throws Exception {

        PathComparator comp = new PathComparator(true);
        assertEquals("Equal paths should be equal", 0, comp.compare("Title[1]", "Title[1]"));
        assertEquals(
            "Parent path should come before child path",
            -1,
            comp.compare("Paragraph[1]", "Paragraph[1]/Headline[1]"));
        // elements with the same name should be listed in reverse order to avoid deletion issues
        assertEquals(
            "Index 2 should come after index 11 as they are required to be in reverse order",
            1,
            comp.compare("Paragraph[2]", "Paragraph[11]"));
        assertEquals("A should come before B", -1, comp.compare("A[1]", "B[1]"));
        assertEquals("A should come before B at lower levels", -1, comp.compare("Foo[1]/A[1]", "Foo[1]/B[1]"));

    }

    /**
     * Tests that the session queue does not allow more than maxLength waiting entries.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testQueueMaxLength() throws Exception {

        int maxLength = 10;
        final CmsUgcSessionQueue queue = new CmsUgcSessionQueue(true, 100, maxLength);
        int numThreads = 30;
        final AtomicInteger okCount = new AtomicInteger();
        final CountDownLatch countdown = new CountDownLatch(numThreads);
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread() {

                /**
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    if (queue.waitForSlot()) {
                        okCount.incrementAndGet();
                    }
                    countdown.countDown();
                }
            };
            thread.start();
        }
        countdown.await(); // wait until all threads have finished
        assertEquals(1 + maxLength, okCount.get());

    }

    /**
     * Tests the wait time for queue is observed.<p>
     *
     * @throws Exception -
     */
    public void testQueueWaitTime() throws Exception {

        int waitTime = 500;
        final CmsUgcSessionQueue queue = new CmsUgcSessionQueue(true, waitTime, Integer.MAX_VALUE);
        int numThreads = 10;
        final CountDownLatch countdown = new CountDownLatch(numThreads);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread() {

                /**
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {

                    queue.waitForSlot();
                    countdown.countDown();

                }
            };

            thread.start();
        }
        countdown.await(); // wait until all threads have finished
        long t2 = System.currentTimeMillis();
        System.out.println("delta-t=" + (t2 - t1) + ", expected=" + (waitTime * (numThreads - 1)));
        assertTrue("The elapsed time is below expected wait time", (t2 - t1) >= ((numThreads - 1) * waitTime));
    }

    /**
     * Tests that the session is cleaned up after its parent session is destroyed.<p>
     *
     * @throws Exception -
     */
    public void testSessionCleanup() throws Exception {

        CmsObject cms = getCmsObject();
        CmsUser admin = cms.readUser("Admin");
        CmsResource contentFolder = cms.createResource("/" + getName(), 0);
        CmsResource uploadFolder = cms.createResource("/" + getName() + "Upload", 0);

        CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(cms, contentFolder, false);
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(Locale.ENGLISH), pubList);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsGroup administrators = cms.readGroup("Administrators");

        CmsUgcConfiguration config = new CmsUgcConfiguration(
            new CmsUUID(),
            Optional.of(admin),
            administrators,
            "xmlcontent",
            contentFolder,
            "n_%(number)",
            Locale.ENGLISH,
            Optional.of(uploadFolder),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            Optional.<Long> absent(),
            Optional.<Integer> absent(),
            false,
            Optional.<List<String>> absent());
        CmsUgcSession session = new CmsUgcSession(cms, config);
        CmsResource contentRes = session.createXmlContent();
        CmsResource uploadRes = session.createUploadResource(
            randomFieldName(),
            "example.bin",
            new byte[] {1, 2, 3, 4, 5});
        CmsProject project = session.getProject();

        session.onSessionDestroyed();

        try {
            cms.readResource(contentRes.getStructureId(), CmsResourceFilter.ALL);
            fail("Resource shouldn't exist after session cleanup");
        } catch (CmsVfsResourceNotFoundException e) {
            // ignore
        }

        try {
            cms.readResource(uploadRes.getStructureId(), CmsResourceFilter.ALL);
            fail("Resource shouldn't exist after session cleanup");
        } catch (CmsVfsResourceNotFoundException e) {
            // ignore
        }

        try {
            cms.readProject(project.getUuid());
            fail("Project shouldn't exist after session cleanup");
        } catch (CmsException e) {
            // ok
        }

        // Now check that the session isn't cleaned up if the project contains any non-new, non-unchanged resources

        session = new CmsUgcSession(cms, config);
        contentRes = session.createXmlContent();
        uploadRes = session.createUploadResource(randomFieldName(), "example.bin", new byte[] {1, 2, 3, 4, 5});
        project = session.getProject();
        CmsPublishList pubList2 = OpenCms.getPublishManager().getPublishList(cms, contentRes, false);
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(Locale.ENGLISH), pubList2);
        OpenCms.getPublishManager().waitWhileRunning();

        session.getCmsObject().lockResource(contentRes);
        session.getCmsObject().writeResource(session.getCmsObject().readResource(contentRes.getStructureId()));

        session.onSessionDestroyed();

        cms.readResource(contentRes.getStructureId(), CmsResourceFilter.ALL);
        cms.readProject(project.getUuid());

    }

    /**
     * Read the given file and cache it's contents as XML schema with the given system id.
     *
     * @param fileName the file name to read
     * @param systemId the XML schema system id to use
     *
     * @throws IOException in case of errors reading the file
     */
    private void cacheXmlSchema(String fileName, String systemId) throws IOException {

        // read the XML schema
        byte[] schema = CmsFileUtil.readFile(fileName);
        // store the XML schema in the resolver
        CmsXmlEntityResolver.cacheSystemId(systemId, schema);
    }

    /**
     * Generate random string to be used as field name.<p>
     *
     * @return a random field name
     */
    private String randomFieldName() {

        return "" + new CmsUUID();
    }

    /**
     * Unmarshals the content definition used within the tests.<p>
     *
     * @param resolver the entity resolver
     *
     * @return the content definition
     *
     * @throws IOException if reading the files fails
     * @throws CmsXmlException if parsing the schema fails
     */
    private CmsXmlContentDefinition unmarshalDefinition(CmsXmlEntityResolver resolver)
    throws IOException, CmsXmlException {

        // fire "clear cache" event to clear up previously cached schemas
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
        cacheXmlSchema("org/opencms/ugc/image.xsd", SCHEMA_ID_IMAGE);
        cacheXmlSchema("org/opencms/ugc/link.xsd", SCHEMA_ID_LINK);
        cacheXmlSchema("org/opencms/ugc/paragraph.xsd", SCHEMA_ID_PARAGRAPH);

        String schemaContent = CmsFileUtil.readFile("org/opencms/ugc/textblock.xsd", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(
            schemaContent,
            SCHEMA_ID_TEXTBLOCK,
            resolver);
        return definition;
    }

}
