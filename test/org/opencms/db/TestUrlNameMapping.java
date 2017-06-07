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

package org.opencms.db;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.Locale;

import junit.framework.Test;

/**
 * Tests the URL name mapping facilities of OpenCms.<p>
 *
 * @since 8.0.0
 */
public class TestUrlNameMapping extends OpenCmsTestCase {

    /** The counter used for generating new file names. */
    private static int m_fileCounter;

    /**
     * Test constructor.<p>
     *
     * @param arg0
     */
    public TestUrlNameMapping(String arg0) {

        super(arg0);
    }

    /**
     * Creates an XML content for testing URL name mappings.<p>
     *
     * @param title the title value
     * @param replace the replace value
     *
     * @return the XML content data
     */
    public static String createTestContent(String title, String replace) {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<UrlNameTests xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"internal://org/opencms/db/urlname.xsd\">\n"
            + "  <UrlNameTest language=\"en\">\n"
            + "<Title>"
            + title
            + "</Title>\n"
            + "<Replace>"
            + replace
            + "</Replace>\n"
            + "  </UrlNameTest>\n"
            + "</UrlNameTests>\n"
            + "";
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestUrlNameMapping.class, "systemtest", "/");
    }

    /**
     * Creates an XML content with the given string as file content.<p>
     *
     * @param content the file content
     * @return the created resource
     *
     * @throws Exception if something goes wrong
     */
    public CmsResource createContent(String content) throws Exception {

        byte[] contentBytes = content.getBytes("UTF-8");
        CmsResource created = getCmsObject().createResource(
            "/file" + m_fileCounter++,
            7,
            contentBytes,
            new ArrayList<CmsProperty>());
        CmsResource res = getCmsObject().readResource(created.getStructureId());
        CmsFile file = getCmsObject().readFile(res);
        file.setContents(contentBytes);
        getCmsObject().writeFile(file);
        return file;
    }

    /**
     * Creates a new file and returns it.<p>
     *
     * @return the new file
     * @throws Exception if something goes wrong
     */
    public CmsResource createFile() throws Exception {

        CmsResource result = getCmsObject().createResource(
            "/file" + m_fileCounter++,
            CmsResourceTypePlain.getStaticTypeId());
        return result;
    }

    /**
     * Helper method for getting a CMS context set to the Online project.<p>
     *
     * @return a {@link CmsObject} with the project set to the Online project
     *
     * @throws Exception if something goes wrong
     */
    public CmsObject getOnlineCmsObject() throws Exception {

        CmsObject result = OpenCms.initCmsObject(getCmsObject());
        CmsProject onlineProject = getCmsObject().readProject(CmsProject.ONLINE_PROJECT_ID);
        result.getRequestContext().setCurrentProject(onlineProject);
        return result;
    }

    /**
     * Tests adding normal mappings after having added 'replace' mappings.<p>
     *
     * @throws Exception
     */
    public void testChangeReplaceSetting() throws Exception {

        CmsResource resource = createFile();
        String basename = "testChangeReplaceSetting";
        String name1 = addReplaceMapping(basename + 1, resource);
        publish();
        String name2 = addMapping(basename + 2, resource);
        publish();
        String name3 = addMapping(basename + 3, resource);
        publish();
        CmsObject cms = getCmsObject();
        assertEquals(resource.getStructureId(), cms.readIdForUrlName(name1));
        assertEquals(resource.getStructureId(), cms.readIdForUrlName(name2));
        assertEquals(resource.getStructureId(), cms.readIdForUrlName(name3));
    }

    /**
     * Tests that when a not-new resource is deleted and published, its URL name mappings are also deleted.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testDeleteChanged() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCmsObject();
        String baseName = "testDeleteChanged";
        String changedName = "testDeleteChanged2";
        String otherName = "testDeleteChanged3";

        CmsResource res = createFile();
        CmsResource res2 = createFile();
        addMapping(changedName, res);
        addMapping(baseName, res);
        addMapping(otherName, res2);
        publish();
        delete(res);
        assertEquals(res.getStructureId(), onlineCms.readIdForUrlName(baseName));
        assertEquals(
            baseName,
            onlineCms.readBestUrlName(
                res.getStructureId(),
                onlineCms.getRequestContext().getLocale(),
                OpenCms.getLocaleManager().getDefaultLocales()));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(baseName));
        assertEquals(baseName, readBestUrlName(cms, res.getStructureId()));
        publish();
        assertNull(onlineCms.readIdForUrlName(baseName));
        assertNull(onlineCms.readIdForUrlName(changedName));
        assertNull(readBestUrlName(onlineCms, res.getStructureId()));
        assertNull(cms.readIdForUrlName(baseName));
        assertNull(readBestUrlName(cms, res.getStructureId()));
        assertEquals(otherName, readBestUrlName(onlineCms, res2.getStructureId()));
        assertEquals(res2.getStructureId(), onlineCms.readIdForUrlName(otherName));
    }

    /**
     * Tests that when a new resource is deleted, its URL name mapping is also deleted.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testDeleteNew() throws Exception {

        CmsObject cms = getCmsObject();
        String baseName = "testDeleteNew";
        CmsResource res = createFile();
        addMapping(baseName, res);
        delete(res);
        assertNull(cms.readIdForUrlName(baseName));
        assertNull(readBestUrlName(cms, res.getStructureId()));
    }

    /**
     * Tests mapping the same URL name to multiple ids.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testMultipleIdMapping() throws Exception {

        CmsObject cms = getCmsObject();
        String baseName = "testMultipleIdMapping";
        CmsResource res1 = createFile();
        CmsResource res2 = createFile();
        String name = baseName;
        CmsUUID id1 = res1.getStructureId();
        CmsUUID id2 = res2.getStructureId();
        String returnedName1 = addMapping(name, res1);
        String returnedName2 = addMapping(name, res2);
        publish();
        assertTrue(returnedName2.contains(returnedName1));
        assertFalse(returnedName2.equals(returnedName1));
        assertEquals(returnedName1, readBestUrlName(cms, id1));
        assertEquals(returnedName2, readBestUrlName(cms, id2));
    }

    /**
     * Tests mapping multiple names to the same resource.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testMultipleNameMapping() throws Exception {

        CmsObject cms = getCmsObject();
        String baseName = "testMultipleNameMapping";
        String name1 = baseName + 1;
        String name2 = baseName + 2;
        CmsResource res = createFile();
        String returnedName1 = addMapping(name1, res);
        publish();
        String returnedName2 = addMapping(name2, res);
        publish();
        assertEquals(name1, returnedName1);
        assertEquals(name2, returnedName2);
        assertEquals(name2, readBestUrlName(cms, res.getStructureId()));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name1));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name2));
    }

    /**
     * Tests that an URL name that has not been published will be overwritten by a new URL name.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testOverwrite() throws Exception {

        CmsObject cms = getCmsObject();
        String baseName = "testOverwrite";
        String changedName = baseName + "Foo";
        CmsResource res = createFile();
        addMapping(baseName, res);
        addMapping(changedName, res);
        assertEquals(changedName, readBestUrlName(cms, res.getStructureId()));
        assertNull(cms.readIdForUrlName(baseName));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(changedName));
    }

    /**
     * Tests publishing the URL name mappings of a resource.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testPublish() throws Exception {

        CmsObject onlineCms = getOnlineCmsObject();
        String baseName = "testPublish";
        CmsResource res = createFile();
        publish();
        addMapping(baseName, res);
        assertNull(onlineCms.readIdForUrlName(baseName));
        assertNull(readBestUrlName(onlineCms, res.getStructureId()));
        publish();
        assertEquals(baseName, readBestUrlName(onlineCms, res.getStructureId()));
        assertEquals(res.getStructureId(), onlineCms.readIdForUrlName(baseName));
    }

    /**
     * Tests that the urlname.replace property mapping is handled correctly, and that the urlname.replace property works the way it should.<p>
     *
     * @throws Exception
     */
    public void testReplaceProperty() throws Exception {

        CmsObject cms = getCmsObject();
        String basename = "testReplaceProperty";
        CmsResource res1 = createContent(createTestContent(basename + "A", "false"));
        CmsResource res2 = createContent(createTestContent(basename + "B", "false"));
        publish();

        CmsFile f1 = cms.readFile(cms.getSitePath(res1));
        CmsFile f2 = cms.readFile(cms.getSitePath(res2));
        CmsXmlContent c1 = CmsXmlContentFactory.unmarshal(cms, f1);
        CmsXmlContent c2 = CmsXmlContentFactory.unmarshal(cms, f2);
        c1.getValue("/Title", Locale.ENGLISH).setStringValue(cms, basename + "X");
        c2.getValue("/Title", Locale.ENGLISH).setStringValue(cms, basename + "Y");
        c2.getValue("/Replace", Locale.ENGLISH).setStringValue(cms, "true");
        f1.setContents(c1.marshal());
        f2.setContents(c2.marshal());
        cms.lockResourceTemporary(f1);
        cms.lockResourceTemporary(f2);
        cms.writeFile(f1);
        cms.writeFile(f2);
        publish();
        assertEquals(res1.getStructureId(), cms.readIdForUrlName(basename + "A"));
        assertEquals(res1.getStructureId(), cms.readIdForUrlName(basename + "X"));
        assertNull("Url name should have been removed", cms.readIdForUrlName(basename + "B"));
        assertEquals(res2.getStructureId(), cms.readIdForUrlName(basename + "Y"));
    }

    /**
     * Tests adding a single url name in the Offline project.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testSimpleMapping() throws Exception {

        CmsObject cms = getCmsObject();
        String name = "testSimpleMapping";
        CmsResource res = createFile();
        String returnedName = addMapping(name, res);
        publish();
        assertEquals(name, returnedName);
        String foundName = readBestUrlName(cms, res.getStructureId());
        assertEquals(name, foundName);
        CmsUUID foundId = cms.readIdForUrlName(name);
        assertEquals(res.getStructureId(), foundId);
    }

    /**
     * Tests that when the changes of a resource are undone, its unpublished URL name mapping will be removed.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testUndo() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject onlineCms = getOnlineCmsObject();
        String baseName = "testUndo";
        String changedName = baseName + "Foo";
        CmsResource res = createFile();
        addMapping(baseName, res);
        publish();
        addMapping(changedName, res);
        cms.lockResource(cms.getSitePath(res));
        cms.undoChanges(cms.getSitePath(res), CmsResourceUndoMode.MODE_UNDO_MOVE_CONTENT);
        cms.unlockResource(cms.getSitePath(res));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(baseName));
        assertEquals(baseName, readBestUrlName(cms, res.getStructureId()));
        assertNull(onlineCms.readIdForUrlName(changedName));
    }

    /**
    * Tests replace-on-publish functionality for URL names.<p>
    *
    * @throws Exception
    */
    public void testUrlNameReplace() throws Exception {

        CmsObject cms = getCmsObject();
        String baseName = "testUrlNameReplace";
        CmsResource res = createFile();
        String name1 = addMapping(baseName + "A", res);
        publish();
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name1));

        String name2 = addReplaceMapping(baseName + "C", res);
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name2));
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name1));
        publish();
        assertEquals(res.getStructureId(), cms.readIdForUrlName(name2));
        assertNull("Old URL name should not be found", cms.readIdForUrlName(name1));
        CmsObject onlineCms = getOnlineCmsObject();
        assertNull("Old URL name should not be found", onlineCms.readIdForUrlName(name1));

    }

    /**
     * Helper method for adding a resource mapping.<p>
     *
     * @param name the mapping name to be used
     * @param res the resource to which the
     *
     * @return the URL name which has actually been mapped
     * @throws Exception
     */
    protected String addMapping(String name, CmsResource res) throws Exception {

        CmsObject cms = getCmsObject();
        // touch the resource so that we can publish it and its URL name mappings later
        touch(res);
        String result = cms.writeUrlNameMapping(name, res.getStructureId(), "en", false);
        return result;
    }

    /**
     * Adds a 'replace' mapping to a resource.<p>
     *
     * @param name the URL name
     * @param res the resource to which to add the mapping
     *
     * @return the actual URL name which was used
     *
     * @throws Exception
     */
    protected String addReplaceMapping(String name, CmsResource res) throws Exception {

        CmsObject cms = getCmsObject();
        // touch the resource so that we can publish it and its URL name mappings later
        touch(res);
        String result = cms.writeUrlNameMapping(name, res.getStructureId(), "en", true);
        return result;
    }

    /**
     * Helper method for deleting a resource.<p>
     *
     * @param res the resource to delete
     * @throws Exception if something goes wrong
     */
    protected void delete(CmsResource res) throws Exception {

        CmsObject cms = getCmsObject();
        String sitePath = cms.getSitePath(res);
        cms.lockResource(sitePath);
        getCmsObject().deleteResource(sitePath, CmsResource.DELETE_PRESERVE_SIBLINGS);
        try {
            cms.unlockResource(sitePath);
        } catch (CmsException e) {
            // ignore
        }
    }

    /**
     * Helper method for publishing the current project.<p>
     * @throws Exception
     */
    protected void publish() throws Exception {

        OpenCms.getPublishManager().publishProject(getCmsObject());
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Changes a resource's "last modified" time to the current time.<p>
     *
     * @param res the resource which should be touched
     *
     * @throws Exception if something goes wrong
     */
    protected void touch(CmsResource res) throws Exception {

        CmsObject cms = getCmsObject();
        String path = cms.getSitePath(res);
        cms.lockResource(path);
        cms.setDateLastModified(path, System.currentTimeMillis(), false);
        cms.unlockResource(cms.getSitePath(res));
    }

    /**
     * Reads the best URL name for the given structure id.<p>
     *
     * @param cms the current CMS context
     * @param structureId the structure ID of the resource
     *
     * @return the best URL name for the resource
     *
     * @throws CmsException
     */
    String readBestUrlName(CmsObject cms, CmsUUID structureId) throws CmsException {

        return cms.readBestUrlName(
            structureId,
            cms.getRequestContext().getLocale(),
            OpenCms.getLocaleManager().getDefaultLocales());
    }

}
