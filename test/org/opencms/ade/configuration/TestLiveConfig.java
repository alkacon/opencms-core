/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.ComparisonFailure;
import junit.framework.Test;

/**
 * Tests for the ADE configuration mechanism which read the configuration data from the VFS.
 */
public class TestLiveConfig extends OpenCmsTestCase {

    protected static final List<CmsPropertyConfig> NO_PROPERTIES = Collections.<CmsPropertyConfig> emptyList();

    protected static final List<CmsDetailPageInfo> NO_DETAILPAGES = Collections.<CmsDetailPageInfo> emptyList();

    protected static final List<CmsModelPageConfig> NO_MODEL_PAGES = Collections.<CmsModelPageConfig> emptyList();

    protected static final List<CmsResourceTypeConfig> NO_TYPES = Collections.<CmsResourceTypeConfig> emptyList();

    public TestLiveConfig(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     * 
     * @return the test suite 
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestLiveConfig.class, "ade-config", "/");
    }

    public void testDeleted() throws Exception {

        try {
            delete(getCmsObject().readResource("/.content/.config"));
            CmsObject offlineCms = getCmsObject();
            CmsObject onlineCms = onlineCms();
            checkResourceTypes(offlineCms, "/sites/default/today/events", "foldername", "d2");
            checkResourceTypes(offlineCms, "/sites/default/today/events/foo", "foldername", "d2");
            checkResourceTypes(offlineCms, "/sites/default/today/news", "foldername", "c3", "e3");
            checkResourceTypes(offlineCms, "/sites/default/today/news/foo", "foldername", "c3", "e3");
        } finally {
            restoreFiles();
        }
    }

    public void testDetailPage1() throws Exception {

        // root site 

        CmsObject cms = rootCms();
        String detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/sites/default/today/news");
        assertEquals("/sites/default/", detailPage);

        // default site

        cms = getCmsObject();
        detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/today/news");
        assertEquals("/sites/default/", detailPage);
    }

    public void testFormatters() throws Exception {

        CmsObject cms = rootCms();
        CmsADEManager manager = OpenCms.getADEManager();
        CmsADEConfigData config = manager.lookupConfiguration(cms, "/sites/default");
        CmsFormatterConfiguration formatterConfig = config.getFormatters("a");
        assertNotNull(formatterConfig);
        List<CmsFormatterBean> formatters = formatterConfig.getAllFormatters();
        assertEquals(1, formatters.size());
        assertEquals("blah", formatters.get(0).getContainerType());
    }

    public void testLevel1Configuration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = onlineCms();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/sites/default", "foldername", "a1", "b1", "c1");
        checkResourceTypes(onlineCms, "/sites/default", "foldername", "a1", "b1", "c1");
        checkResourceTypes(offlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
        checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
    }

    public void testLevel2Configuration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = onlineCms();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/sites/default/today/events", "foldername", "d2", "a1", "c1");
        checkResourceTypes(offlineCms, "/sites/default/today/events/foo", "foldername", "d2", "a1", "c1");
        checkResourceTypes(onlineCms, "/sites/default/today/events/", "foldername", "d2", "a1", "c1");
        checkResourceTypes(onlineCms, "/sites/default/today/events/foo", "foldername", "d2", "a1", "c1");

        checkResourceTypes(onlineCms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1");
        checkResourceTypes(onlineCms, "/sites/default/today/news/foo/", "foldername", "c3", "e3", "a1", "b1");
        checkResourceTypes(offlineCms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1");
        checkResourceTypes(offlineCms, "/sites/default/today/news/foo", "foldername", "c3", "e3", "a1", "b1");
    }

    public void testModuleConfig1() throws Exception {

        CmsObject cms = rootCms();
        String filename = "/system/modules/org.opencms.ade.config/.config";
        try {
            String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<SitemapConfigurationsV2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\r\n"
                + "  <SitemapConfigurationV2 language=\"en\">\r\n"
                + "      <ResourceType>\r\n"
                + "      <TypeName><![CDATA[m]]></TypeName>\r\n"
                + "      <Disabled>false</Disabled>\r\n"
                + "      <Folder>\r\n"
                + "        <Name><![CDATA[m0]]></Name>\r\n"
                + "      </Folder>\r\n"
                + "      <NamePattern><![CDATA[asdf]]></NamePattern>\r\n"
                + "    </ResourceType>\r\n"
                + "  </SitemapConfigurationV2>\r\n"
                + "</SitemapConfigurationsV2>\r\n";
            cms.createResource(
                filename,
                OpenCms.getADEManager().getModuleConfigurationType().getTypeId(),
                data.getBytes(),
                Collections.<CmsProperty> emptyList());
            checkResourceTypes(cms, "/sites/default", "foldername", "a1", "b1", "c1", "m0");
        } finally {
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
    }

    public void testMove1() throws Exception {

        try {
            CmsObject cms = rootCms();
            cms.lockResource("/sites/default/today/events");
            cms.moveResource("/sites/default/today/events", "/sites/default/today/news/events");
            checkResourceTypes(cms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(cms, "/sites/default/today/news/events/", "foldername", "d2", "c3", "e3", "a1");
        } finally {
            restoreFiles();
        }
    }

    public void testMoveDetailPages() throws Exception {

        CmsObject cms = rootCms();
        try {
            OpenCms.getADEManager().refresh();
            cms.lockResource("/sites/default/modelpage1.html");
            String newPath = "/sites/default/.content/blah.html";
            cms.moveResource("/sites/default/modelpage1.html", newPath);
            CmsADEConfigData configData = OpenCms.getADEManager().lookupConfiguration(cms, "/sites/default/today");
            List<CmsDetailPageInfo> detailPages = configData.getDetailPagesForType("foo");
            CmsDetailPageInfo page = detailPages.get(0);
            assertEquals("/sites/default/.content/blah.html", page.getUri());
        } finally {
            restoreFiles();
        }

    }

    public void testNoConfiguration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/", "foldername");
        checkResourceTypes(offlineCms, "/sites", "foldername");
        checkResourceTypes(offlineCms, "/system", "foldername");
    }

    public void testPublish() throws Exception {

        CmsObject cms = rootCms();
        CmsObject onlineCms = onlineCms();
        try {
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            cms.copyResource("/sites/default/today/news/.content", "sites/default/today/.content");
            checkResourceTypes(cms, "/sites/default/today", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            publish();
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(onlineCms, "/sites/default/today/events", "foldername", "d2", "c3", "e3", "a1");
        } finally {
            restoreFiles();
        }
    }

    public void testPublishDeleted() throws Exception {

        CmsObject cms = rootCms();
        CmsObject onlineCms = onlineCms();
        try {
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            cms.lockResource("/sites/default/.content");
            cms.deleteResource("/sites/default/.content", CmsResource.DELETE_PRESERVE_SIBLINGS);
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            publish();
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername");
        } finally {
            restoreFiles();
        }
    }

    public void testSaveDetailPages() throws Exception {

        try {

            CmsADEManager manager = OpenCms.getADEManager();
            CmsObject cms = rootCms();
            CmsResource page1 = cms.readResource("/sites/default/today");
            CmsResource page2 = cms.readResource("/sites/default/today/events");
            CmsDetailPageInfo info1 = new CmsDetailPageInfo(page1.getStructureId(), page1.getRootPath(), "foo");
            CmsDetailPageInfo info2 = new CmsDetailPageInfo(page2.getStructureId(), page2.getRootPath(), "bar");
            cms.lockResource("/sites/default/.content/.config");
            manager.saveDetailPages(cms, "/sites/default/today", list(info1, info2), new CmsUUID());

            CmsADEConfigData configData = manager.lookupConfiguration(cms, "/sites/default/today/");
            List<CmsDetailPageInfo> detailPages = configData.getAllDetailPages();
            assertEquals("/sites/default/today/", detailPages.get(0).getUri());
            assertEquals("foo", detailPages.get(0).getType());
            assertEquals(page1.getStructureId(), detailPages.get(0).getId());

            assertEquals("/sites/default/today/events/", detailPages.get(1).getUri());
            assertEquals("bar", detailPages.get(1).getType());
            assertEquals(page2.getStructureId(), detailPages.get(1).getId());
        } finally {
            restoreFiles();
        }
    }

    protected void assertPathEquals(String path1, String path2) {

        if ((path1 == null) && (path2 == null)) {
            return;
        }
        if ((path1 == null) || (path2 == null)) {
            throw new ComparisonFailure("comparison failure", path1, path2);
        }
        assertEquals(CmsStringUtil.joinPaths("/", path1, "/"), CmsStringUtil.joinPaths("/", path2, "/"));
    }

    protected void checkResourceTypes(CmsObject cms, String path, String attr, String... expected) {

        CmsADEManager configManager = OpenCms.getADEManager();
        CmsADEConfigData data = configManager.lookupConfiguration(cms, path);
        List<CmsResourceTypeConfig> types = data.getResourceTypes();
        List<String> actualValues = new ArrayList<String>();
        for (CmsResourceTypeConfig typeConfig : types) {
            actualValues.add(getAttribute(typeConfig, attr));
        }
        assertEquals(Arrays.asList(expected), actualValues);
    }

    protected CmsPropertyConfig createDisabledPropertyConfig(String name) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, null);
        return new CmsPropertyConfig(prop, true);
    }

    protected CmsPropertyConfig createPropertyConfig(String name, String description) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, description);
        return new CmsPropertyConfig(prop, false);
    }

    protected CmsXmlContentProperty createXmlContentProperty(String name, String description) {

        return new CmsXmlContentProperty(name, "string", "string", "", "", "", "", "", description, null, null);
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

    protected void dumpTree() throws CmsException {

        CmsObject cms = rootCms();
        CmsResource root = cms.readResource("/");
        dumpTree(cms, root, 0);
    }

    protected void dumpTree(CmsObject cms, CmsResource res, int indentation) throws CmsException {

        writeIndentation(indentation);
        System.out.println(res.getName());
        I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res);
        if (resType.isFolder()) {
            List<CmsResource> children = cms.getResourcesInFolder(res.getRootPath(), CmsResourceFilter.ALL);
            for (CmsResource child : children) {
                dumpTree(cms, child, indentation + 6);
            }
        }
    }

    protected String getAttribute(CmsResourceTypeConfig typeConfig, String attr) {

        if ("type".equals(attr)) {
            return typeConfig.getTypeName();
        }

        if ("foldername".equals(attr)) {
            return typeConfig.getFolderOrName().getFolderName();
        }

        return null;
    }

    protected <X> List<X> list(X... elems) {

        List<X> result = new ArrayList<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    protected CmsObject onlineCms() throws Exception {

        CmsObject cms = getCmsObject();
        CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
        cms.getRequestContext().setCurrentProject(online);
        return cms;
    }

    /**
     * Helper method for publishing the current project.<p>
     * @throws Exception
     */
    protected void publish() throws Exception {

        OpenCms.getPublishManager().publishProject(getCmsObject());
        OpenCms.getPublishManager().waitWhileRunning();
    }

    protected void restoreFiles() throws Exception {

        System.out.println("Restoring test data...");
        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            CmsObject cms = getCmsObject();
            cms.getRequestContext().setSiteRoot("");
            cms.lockResource("/sites/default");
            cms.deleteResource("/sites/default", CmsResource.DELETE_PRESERVE_SIBLINGS);
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
            importResources(cms, "ade-config", "/");
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);
        }
    }

    protected CmsObject rootCms() throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }

    protected <X> Set<X> set(X... elems) {

        Set<X> result = new HashSet<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    protected void writeIndentation(int indent) {

        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
    }
}
