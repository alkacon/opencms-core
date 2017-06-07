/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.Test;

/**
 * Tests for the ADE configuration mechanism which read the configuration data from multiple files in the VFS.<p>
 *
 */
public class TestLiveConfig extends OpenCmsTestCase {

    /** The current VFS prefix as added to internal links according to the configuration in opencms-importexport.xml. */
    String m_vfsPrefix;

    /**
     * Test constructor.<p>
     *
     * @param name the name of the test
     */
    public TestLiveConfig(String name) {

        super(name);
    }

    public static String generateSitemapConfigWithTypes(Map<String, String> types, String masterConfigId) {

        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<SitemapConfigurationsV2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\r\n"
            + "  <SitemapConfigurationV2 language=\"en\">\r\n"

            + " $masterConfigs:{masterConfig | <MasterConfig>"
            + "<link type='WEAK'>"
            + "  <target></target> "
            + " <uuid>$masterConfig$</uuid> "
            + " </link>"
            + "      </MasterConfig> }$"
            + "      $types.keys:{type |"
            + "      <ResourceType>\r\n"
            + "      <TypeName><![CDATA[$type$]]></TypeName>\n"
            + "      <Disabled>false</Disabled>\n"
            + "      <Folder>\n"
            + "        <Name><![CDATA[$types.(type)$]]></Name>\n"
            + "      </Folder>\n"
            + "      <NamePattern><![CDATA[asdf]]></NamePattern>\n"
            + "    </ResourceType>}$\n"
            + "  </SitemapConfigurationV2>\r\n"
            + "</SitemapConfigurationsV2>\r\n";

        StringTemplate st = new StringTemplate(template);
        st.setAttribute("types", types);
        st.setAttribute(
            "masterConfigs",
            masterConfigId != null ? Collections.singletonList(masterConfigId) : Collections.emptyList());
        return st.toString();
    }

    public static void main(String[] args) {

        Map<String, String> types = Maps.newHashMap();
        types.put("foo", "foo0");
        types.put("bar", "bar0");
        System.out.println(generateSitemapConfigWithTypes(types, "444444444444"));
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        CmsConfigurationCache.DEBUG = true;
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestLiveConfig.class, "ade-config", "/");
    }

    /**
     * Tests cross-site detail page links.<p>
     *
     * @throws Exception -
     */
    public void testCrossSiteDetailPageLinks1() throws Exception {

        // Link from site foo to site bar, where a detail page exists in foo

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/sites/foo");
        String rootPath = "/sites/bar/.content/blogentries/be_00001.xml";
        CmsResource res = rootCms().readResource(rootPath);
        String link = OpenCms.getLinkManager().getOnlineLink(cms, rootPath);
        assertEquals("http://foo.org" + getVfsPrefix() + "/main/blog/" + res.getStructureId() + "/", link);
        System.out.println(link);
    }

    /**
     * Tests cross-site detail page links.<p>
     *
     * @throws Exception -
     */
    public void testCrossSiteDetailPageLinks1a() throws Exception {

        // Link from site foo to site bar, where a detail page exists in foo
        // (using a site path that also exists in site foo)

        OpenCmsTestLogAppender.setBreakOnError(false);
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/sites/foo");
        String rootPath = "/sites/bar/.content/blogentries/be_00002.xml";
        CmsResource res = rootCms().readResource(rootPath);
        String link = OpenCms.getLinkManager().getOnlineLink(cms, rootPath);
        assertEquals("http://foo.org" + getVfsPrefix() + "/main/blog/" + res.getStructureId() + "/", link);
        System.out.println(link);
    }

    /**
     * Tests cross-site detail page links.<p>
     *
     * @throws Exception -
     */
    public void testCrossSiteDetailPageLinks2() throws Exception {

        // Link from site bar to site foo, where a detail page exists in foo

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/sites/bar");
        String rootPath = "/sites/foo/.content/blogentries/be_00001.xml";
        CmsResource res = rootCms().readResource(rootPath);
        String link = OpenCms.getLinkManager().getOnlineLink(cms, rootPath);
        assertEquals("http://foo.org" + getVfsPrefix() + "/main/blog/" + res.getStructureId() + "/", link);
        System.out.println(link);
    }

    /**
     * Tests deletion of configuration files.<p>
     *
     * @throws Exception -
     */
    public void testDeleted() throws Exception {

        try {

            waitForUpdate(false);
            delete(getCmsObject().readResource("/.content/.config"));
            CmsObject offlineCms = getCmsObject();
            waitForUpdate(false);
            //checkResourceTypes(offlineCms, "/sites/default/today/events", "foldername", "d2");
            checkResourceTypes(offlineCms, "/sites/default/today/events/foo", "foldername", "d2");
            checkResourceTypes(offlineCms, "/sites/default/today/news", "foldername", "c3", "e3");
            checkResourceTypes(offlineCms, "/sites/default/today/news/foo", "foldername", "c3", "e3");
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests finding detail pages.<p>
     *
     * @throws Exception -
     */
    public void testDetailPage1() throws Exception {

        // root site
        waitForUpdate(false);
        CmsObject cms = rootCms();
        String detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/sites/default/today/news",
            null);
        assertEquals("/sites/default/", detailPage);

        // default site

        cms = getCmsObject();
        detailPage = OpenCms.getADEManager().getDetailPageFinder().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/today/news",
            null);
        assertEquals("/sites/default/", detailPage);
    }

    /**
     * Tests the configuration in top-level sitemaps.<p>
     * @throws Exception -
     */
    public void testLevel1Configuration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = onlineCms();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/sites/default", "foldername", "a1", "b1", "c1");
        checkResourceTypes(onlineCms, "/sites/default", "foldername", "a1", "b1", "c1");
        checkResourceTypes(offlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
        checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
    }

    /**
     * Tests the configuration in level 2 subsitemaps.<p>
     * @throws Exception -
     */
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

    /**
     * Tests the master configuration feature.<p>
     *
     * @throws Exception -
     */
    public void testMasterConfiguration() throws Exception {

        CmsObject cms = getCmsObject();
        I_CmsResourceType folderType = OpenCms.getResourceManager().getResourceType("folder");
        cms.createResource("/system/mastertest", folderType);
        try {
            I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType("sitemap_config");
            I_CmsResourceType masterConfigType = OpenCms.getResourceManager().getResourceType("sitemap_master_config");

            cms.createResource("/system/mastertest/.content", folderType);
            cms.createResource("/system/mastertest/subfolder", folderType);
            cms.createResource("/system/mastertest/subfolder/.content", folderType);
            Map<String, String> types1 = Maps.newHashMap();
            types1.put("aa", "aa1");
            types1.put("bb", "bb1");
            types1.put("cc", "cc1");
            String config1 = generateSitemapConfigWithTypes(types1, null);
            cms.createResource(
                "/system/mastertest/.content/.config",
                configType,
                config1.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());

            Map<String, String> types2 = Maps.newHashMap();
            types2.put("bb", "bb2");
            types2.put("cc", "cc2");
            String config2 = generateSitemapConfigWithTypes(types2, null);
            CmsResource masterConfigResource = cms.createResource(
                "/system/.master",
                masterConfigType,
                config2.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());

            Map<String, String> types3 = Maps.newHashMap();
            types3.put("cc", "cc3");
            String config3 = generateSitemapConfigWithTypes(types3, "" + masterConfigResource.getStructureId());
            cms.createResource(
                "/system/mastertest/subfolder/.content/.config",
                configType,
                config3.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());
            OpenCms.getADEManager().waitForCacheUpdate(false);
            checkResourceTypesSet(cms, "/system/mastertest/subfolder", "foldername", "aa1", "bb2", "cc3");

            cms.deleteResource("/system/.master", CmsResource.DELETE_PRESERVE_SIBLINGS);
            OpenCms.getADEManager().waitForCacheUpdate(false);
            checkResourceTypesSet(cms, "/system/mastertest/subfolder", "foldername", "aa1", "bb1", "cc3");

        } finally {
            cms.deleteResource("/system/mastertest", CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

    }

    /**
     * Tests that newly created module configurations are reflected in the configuration objects.<p>
     *
     * @throws Exception -
     */
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
            waitForUpdate(false);
            checkResourceTypes(cms, "/sites/default", "foldername", "a1", "b1", "c1", "m0");
        } finally {
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
    }

    /**
     * Tests that when moving a configuration file, the configuration will be correct.<p>
     *
     * @throws Exception -
     */
    public void testMove1() throws Exception {

        try {
            CmsObject cms = rootCms();
            cms.lockResource("/sites/default/today/events");
            cms.moveResource("/sites/default/today/events", "/sites/default/today/news/events");
            OpenCms.getADEManager().getOfflineCache().getWaitHandleForUpdateTask().enter(0);
            checkResourceTypes(cms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(cms, "/sites/default/today/news/events/", "foldername", "d2", "c3", "e3", "a1");
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests that when detail pages are moved, the configuration will still return the correct URIs.<p>
     *
     * @throws Exception -
     */
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

    /**
     * Tests that the configuration is empty at paths where no configuration is defined.<p>
     * @throws Exception -
     */
    public void testNoConfiguration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/", "foldername");
        checkResourceTypes(offlineCms, "/sites", "foldername");
        checkResourceTypes(offlineCms, "/system", "foldername");
    }

    /**
     * Tests that publishing a changed configuration file updates the online configuration object.<p>
     * @throws Exception -
     */
    public void testPublish() throws Exception {

        CmsObject cms = rootCms();
        CmsObject onlineCms = onlineCms();
        try {
            waitForUpdate(false);
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            cms.copyResource("/sites/default/today/news/.content", "sites/default/today/.content");
            waitForUpdate(true);
            waitForUpdate(false);
            checkResourceTypes(cms, "/sites/default/today", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            publish();
            waitForUpdate(true);
            waitForUpdate(false);
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername", "c3", "e3", "a1", "b1");
            checkResourceTypes(onlineCms, "/sites/default/today/events", "foldername", "d2", "c3", "e3", "a1");
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests that publishing a deleted configuration file changes the online configuration.<p>
     *
     * @throws Exception -
     */
    public void testPublishDeleted() throws Exception {

        CmsObject cms = rootCms();
        CmsObject onlineCms = onlineCms();
        try {
            waitForUpdate(false);
            waitForUpdate(true);
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            cms.lockResource("/sites/default/.content");
            cms.deleteResource("/sites/default/.content", CmsResource.DELETE_PRESERVE_SIBLINGS);
            waitForUpdate(true);
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1");
            publish();
            waitForUpdate(true);
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername");
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests the saving of detail pages.<p>
     * @throws Exception -
     */
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
            waitForUpdate(false);
            CmsADEConfigData configData = manager.lookupConfiguration(cms, "/sites/default/today/");
            List<CmsDetailPageInfo> detailPages = configData.getAllDetailPages();
            assertTrue(detailPages.contains(info1));

            assertTrue(detailPages.contains(info2));
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests whether getSubsiteFolder works correctly with the shared folder.<p>
     */
    public void testSharedGetSubSite() throws Exception {

        CmsObject cms = rootCms();
        String filename = "/shared/.content/.config";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
            + "<SitemapConfigurationsV2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\r\n"
            + "  <SitemapConfigurationV2 language=\"en\">\r\n"
            + "  </SitemapConfigurationV2>\r\n"
            + "</SitemapConfigurationsV2>\r\n";
        cms.createResource("/shared/.content", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(
            filename,
            OpenCms.getADEManager().getSitemapConfigurationType().getTypeId(),
            data.getBytes(),
            Collections.<CmsProperty> emptyList());
        OpenCms.getADEManager().waitForCacheUpdate(false);
        assertEquals(
            "/shared",
            CmsFileUtil.removeTrailingSeparator(OpenCms.getADEManager().getSubSiteRoot(cms, "/shared")));

    }

    /**
     * Tests that sitmeap folder types override module folder types.<p>
     * @throws Exception -
     */
    public void testSitemapFolderTypesOverrideModuleFolderTypes() throws Exception {

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
                + "        <Name><![CDATA[a1]]></Name>\r\n"
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
            waitForUpdate(false);
            String parentFolderType = OpenCms.getADEManager().getOfflineCache().getState().getParentFolderType(
                "/sites/default/.content/a1/foo");
            assertEquals("a", parentFolderType);
        } finally {
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
    }

    /**
     * Waits until the configuration update task has been run.<p>
     *
     * @param online true if we should wait for the Online task, false for the Offline task
     */
    public void waitForUpdate(boolean online) {

        OpenCms.getADEManager().getCache(online).getWaitHandleForUpdateTask().enter(0);
    }

    /**
     * Helper method to compare attributes of configured resource types with a list of expected values.<p>
     *
     * @param cms the CMS context
     * @param path the path used to access the configuration
     * @param attr the attribute which should be retrieved from the configured resource types
     * @param expected the expected resource type names
     */
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

    /**
     * Helper method to compare attributes of configured resource types with a set  of expected values.<p>
     *
     * @param cms the CMS context
     * @param path the path used to access the configuration
     * @param attr the attribute which should be retrieved from the configured resource types
     * @param expected the expected resource type names
     */
    protected void checkResourceTypesSet(CmsObject cms, String path, String attr, String... expected) {

        CmsADEManager configManager = OpenCms.getADEManager();
        CmsADEConfigData data = configManager.lookupConfiguration(cms, path);
        List<CmsResourceTypeConfig> types = data.getResourceTypes();
        List<String> actualValues = new ArrayList<String>();
        for (CmsResourceTypeConfig typeConfig : types) {
            actualValues.add(getAttribute(typeConfig, attr));
        }
        assertEquals(Sets.newHashSet(expected), Sets.newHashSet(actualValues));
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
     * Gets an attribute from a resource type configuration object.<p>
     *
     * @param typeConfig the type configuration object
     * @param attr the attribute name
     * @return the attribute from the resource type configuration object
     */
    protected String getAttribute(CmsResourceTypeConfig typeConfig, String attr) {

        if ("type".equals(attr)) {
            return typeConfig.getTypeName();
        }

        if ("foldername".equals(attr)) {
            return typeConfig.getFolderOrName().getFolderName();
        }

        return null;
    }

    /**
     * Initializes m_vfsPrefix lazily, otherwise it does not work.
     * @return the VFS prefix as added to internal links
     */
    protected String getVfsPrefix() {

        if (null == m_vfsPrefix) {
            m_vfsPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        }
        return m_vfsPrefix;
    }

    /**
     * Helper method for creating a list of given elements.<p>
     *
     * @param elems the elements
     * @return a list containing the elements
     */
    protected <X> List<X> list(X... elems) {

        List<X> result = new ArrayList<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    /**
     * Helper method for creating a CMS context in the Online Project.<p>
     *
     * @return the CMS context
     * @throws Exception -
     */
    protected CmsObject onlineCms() throws Exception {

        CmsObject cms = getCmsObject();
        CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
        cms.getRequestContext().setCurrentProject(online);
        return cms;
    }

    /**
     * Helper method for publishing the current project.<p>
     * @throws Exception -
     */
    protected void publish() throws Exception {

        OpenCms.getPublishManager().publishProject(getCmsObject());
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Helper method to re-create the original test data in the VFS.<p>
     * @throws Exception -
     */
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

    /**
     * Helper method for getting a CMS object in the root site.<p>
     *
     * @return a CMS context in the root site
     *
     * @throws CmsException  -
     */
    protected CmsObject rootCms() throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }
}
