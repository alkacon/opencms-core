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
import org.opencms.ade.detailpage.I_CmsDetailPageHandler;
import org.opencms.ade.sitemap.CmsSitemapAttributeUpdater;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.ui.components.CmsExtendedSiteSelector;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsVfsUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;

import org.antlr.stringtemplate.StringTemplate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.Test;

/**
 * Tests for the ADE configuration mechanism which read the configuration data from multiple files in the VFS.<p>
 *
 */
public class TestLiveConfig extends OpenCmsTestCase {

    /** Pattern for matching path segment consisting of two characters from {a, b}. */
    private static final Pattern detailPageTestSubsitePattern = Pattern.compile("/([ab][ab])/");

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

    /**
     * Generates a sitemap config XML with the given types.<p>
     *
     * @param types the types to use
     * @param masterConfigIds the master configuration ids
     *
     * @return the XML string
     */
    public static String generateSitemapConfigWithTypes(Map<String, String> types, List<String> masterConfigIds) {

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
        st.setAttribute("masterConfigs", masterConfigIds != null ? masterConfigIds : Collections.emptyList());
        return st.toString();
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
     * Tests programmatic updating of sitemap attributes.
     *
     * @throws Exception
     */
    public void testAttributeUpdates() throws Exception {

        CmsObject cms = getCmsObject();
        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType("sitemap_config");
        String path = "/savetest1.xml";
        cms.createResource("/savetest1.xml", configType);
        Map<String, String> originalAttributes = new HashMap<>();
        originalAttributes.put("foo", "1");
        originalAttributes.put("bar", "2");

        {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(path));
            // make sure we have something else than attributes in the sitemap config
            content.addValue(cms, "DiscardTypes", Locale.ENGLISH, 0);
            CmsSitemapAttributeUpdater updater = new CmsSitemapAttributeUpdater(cms, content);
            updater.replaceAttributes(originalAttributes);
            CmsFile file = content.getFile();
            file.setContents(content.marshal());
            cms.lockResource(file);
            cms.writeFile(file);
        }
        {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(path));
            CmsSitemapAttributeUpdater updater = new CmsSitemapAttributeUpdater(cms, content);
            assertEquals(originalAttributes, updater.getAttributesFromContent());
            Map<String, String> updates = new HashMap<>();
            updates.put("foo", null);
            updates.put("baz", "3");
            updater.updateAttributes(updates);
            CmsFile file = content.getFile();
            file.setContents(content.marshal());
            cms.lockResource(file);
            cms.writeFile(file);
        }

        {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(path));
            CmsSitemapAttributeUpdater updater = new CmsSitemapAttributeUpdater(cms, content);
            assertEquals(CmsStringUtil.splitAsMap("bar:2|baz:3", "|", ":"), updater.getAttributesFromContent());
        }
    }

    /**
     * Tests category-based detail page selection.
     * @throws Exception
     */
    public void testCategoryDetailPages() throws Exception {

        CmsObject cms = getCmsObject();
        String base = "/testCategoryDetailPages";
        cms.createResource(base, 0);
        cms.createResource("/.categories", 0);
        cms.createResource("/.categories/foo", 0);
        if (!cms.existsResource("/system/categories")) {
            cms.createResource("/system/categories", 0);
        }

        boolean preferDetailPages;
        boolean excludeExternalContents;

        preferDetailPages = true;
        excludeExternalContents = false;
        createCategoryDetailPageTestSitemap(base + "/a", preferDetailPages, excludeExternalContents, false);
        OpenCms.getADEManager().waitForCacheUpdate(false);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.readResource(base + "/a").getRootPath());
        List<CmsDetailPageInfo> pages = config.getDetailPagesForType("article1");
        List<String> types = pages.stream().map(page -> page.getQualifiedType()).collect(Collectors.toList());
        String articlePath = base + "/a/.content/blogentries/article.xml";
        String link1 = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, articlePath);
        assertTrue(link1.contains("/detail/"));
        assertFalse(link1.contains("/detail-foo/"));
        CmsCategoryService.getInstance().addResourceToCategory(cms, articlePath, "foo");
        String link2 = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, articlePath);
        assertFalse(link2.contains("/detail/"));
        assertTrue(link2.contains("/detail-foo/"));

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
     * Tests cross-site detail page links.<p>
     *
     * @throws Exception -
     */
    public void testCrossSiteDetailPageLinkUtilityFunctions() throws Exception {

        // Link from site foo to site bar, where a detail page exists in foo

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/sites/foo");
        String rootPath = "/sites/bar/.content/blogentries/be_00001.xml";
        CmsResource res = rootCms().readResource(rootPath);
        String link = OpenCms.getLinkManager().getOnlineLink(cms, rootPath);
        assertEquals(null, CmsLinkManager.getLinkSubsite(cms , "https://www.dummy.invalid/foo/bar/baz"));
        assertEquals("/sites/foo/", CmsLinkManager.getLinkSubsite(cms, link));
        assertEquals("/sites/bar/", CmsLinkManager.getLinkSubsite(cms, OpenCms.getLinkManager().getOnlineLink(cms, "/sites/bar/")));
    }

    /**
     * Tests that multiple default detail pages can be returned CmsADEConfigData#getDetailPagesForType().
     *
     * @throws Exception
     */
    public void testDefaultDetailPagesPreserved() throws Exception {

        CmsObject cms = getCmsObject();
        String base = "/testDefaultDetailPagesPreserved";
        cms.createResource(base, 0);

        boolean preferDetailPages;
        boolean excludeExternalContents;

        preferDetailPages = false;
        excludeExternalContents = false;
        createCategoryDetailPageTestSitemap(base + "/a", preferDetailPages, excludeExternalContents, false);
        OpenCms.getADEManager().waitForCacheUpdate(false);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
            cms,
            cms.readResource(base + "/a").getRootPath());
        List<CmsDetailPageInfo> pages = config.getDetailPagesForType("article1");
        List<String> types = pages.stream().map(page -> page.getQualifiedType()).collect(Collectors.toList());
        assertEquals(
            Arrays.asList("article1", "article1|category:foo", "##DEFAULT##", "##DEFAULT##|category:foo"),
            types);
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
        String detailPage = OpenCms.getADEManager().getDetailPageHandler().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/sites/default/today/news",
            null);
        assertEquals("/sites/default/", detailPage);

        // default site

        cms = getCmsObject();
        detailPage = OpenCms.getADEManager().getDetailPageHandler().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/today/news",
            null);
        assertEquals("/sites/default/", detailPage);
    }

    /**
     * Tests whether the 'include in site selector' flag is processed correctly.
     *
     * @throws Exception -
     */
    public void testIncludeInSiteSelector() throws Exception {

        try {
            OpenCms.getSiteManager().getSiteForRootPath(
                getCmsObject().getRequestContext().getSiteRoot()).setSubsiteSelectionEnabled(true);

            createDetailPageTestSitemap("/includeInSiteSelector", false, false, true);
            createDetailPageTestSitemap("/includeInSiteSelector/alpha", false, false, false);
            createDetailPageTestSitemap("/includeInSiteSelector/beta", false, false, true);
            createDetailPageTestSitemap("/includeInSiteSelector/alpha/gamma", false, false, true);
            OpenCms.getADEManager().waitForCacheUpdate(false);

            List<String> subsites = OpenCms.getADEManager().getSubsitesForSiteSelector(false);
            Set<String> subsiteSet = new HashSet<>(subsites);
            Set<String> expected = new HashSet<>(
                list(
                    "/sites/default/includeInSiteSelector/",
                    "/sites/default/includeInSiteSelector/alpha/gamma/",
                    "/sites/default/includeInSiteSelector/beta/"));
            assertEquals(expected, subsiteSet);
            List<CmsExtendedSiteSelector.SiteSelectorOption> options = CmsExtendedSiteSelector.getExplorerSiteSelectorOptions(
                getCmsObject(),
                true);

            Set<String> actual = new HashSet<>();
            for (CmsExtendedSiteSelector.SiteSelectorOption option : options) {
                if (option.getPath() != null) {
                    actual.add(option.getPath());
                    assertTrue(
                        "Site does not match",
                        CmsStringUtil.comparePaths(option.getSite(), getCmsObject().getRequestContext().getSiteRoot()));
                }
            }
            expected = new HashSet<>(
                list("/includeInSiteSelector", "/includeInSiteSelector/alpha/gamma", "/includeInSiteSelector/beta"));
            assertEquals(expected, actual);
        } finally {
            OpenCms.getSiteManager().getSiteForRootPath(
                getCmsObject().getRequestContext().getSiteRoot()).setSubsiteSelectionEnabled(false);
        }
    }

    /**
     * Tests the configuration in top-level sitemaps.<p>
     * @throws Exception -
     */
    public void testLevel1Configuration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = onlineCms();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/sites/default", "foldername", "a1", "b1", "c1", "foo1");
        checkResourceTypes(onlineCms, "/sites/default", "foldername", "a1", "b1", "c1", "foo1");
        checkResourceTypes(offlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
        checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
    }

    /**
     * Tests the configuration in level 2 subsitemaps.<p>
     * @throws Exception -
     */
    public void testLevel2Configuration() throws Exception {

        CmsObject offlineCms = getCmsObject();
        CmsObject onlineCms = onlineCms();
        OpenCms.getADEManager().refresh();
        checkResourceTypes(offlineCms, "/sites/default/today/events", "foldername", "d2", "a1", "c1", "foo1");
        checkResourceTypes(offlineCms, "/sites/default/today/events/foo", "foldername", "d2", "a1", "c1", "foo1");
        checkResourceTypes(onlineCms, "/sites/default/today/events/", "foldername", "d2", "a1", "c1", "foo1");
        checkResourceTypes(onlineCms, "/sites/default/today/events/foo", "foldername", "d2", "a1", "c1", "foo1");

        checkResourceTypes(onlineCms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1", "foo1");
        checkResourceTypes(onlineCms, "/sites/default/today/news/foo/", "foldername", "c3", "e3", "a1", "b1", "foo1");
        checkResourceTypes(offlineCms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1", "foo1");
        checkResourceTypes(offlineCms, "/sites/default/today/news/foo", "foldername", "c3", "e3", "a1", "b1", "foo1");
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
            String config3 = generateSitemapConfigWithTypes(
                types3,
                Arrays.asList("" + masterConfigResource.getStructureId()));
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
     * Tests the master configuration chaining feature.<p>
     *
     * @throws Exception -
     */
    public void testMasterConfigurationChaining() throws Exception {

        CmsObject cms = getCmsObject();
        I_CmsResourceType folderType = OpenCms.getResourceManager().getResourceType("folder");
        cms.createResource("/system/chaintest", folderType);

        I_CmsResourceType configType = OpenCms.getResourceManager().getResourceType("sitemap_config");
        I_CmsResourceType masterConfigType = OpenCms.getResourceManager().getResourceType("sitemap_master_config");

        cms.createResource("/system/chaintest/.content", folderType);
        Map<String, String> types1 = Maps.newHashMap();

        String config2 = generateSitemapConfigWithTypes(types1, null);
        CmsResource masterConfigResource = cms.createResource(
            "/system/.chainmaster",
            masterConfigType,
            config2.getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());

        String config3 = generateSitemapConfigWithTypes(types1, null);
        CmsResource masterConfigResource2 = cms.createResource(
            "/system/.chainmaster2",
            masterConfigType,
            config3.getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());

        String config4 = generateSitemapConfigWithTypes(
            types1,
            Arrays.asList("" + masterConfigResource.getStructureId(), "" + masterConfigResource2.getStructureId()));
        CmsResource masterConfigResource3 = cms.createResource(
            "/system/.chainmaster3",
            masterConfigType,
            config4.getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());

        String config1 = generateSitemapConfigWithTypes(
            types1,
            Arrays.asList("" + masterConfigResource3.getStructureId()));
        cms.createResource(
            "/system/chaintest/.content/.config",
            configType,
            config1.getBytes("UTF-8"),
            Collections.<CmsProperty> emptyList());

        OpenCms.getADEManager().waitForCacheUpdate(false);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, "/system/chaintest");
        assertEquals(
            Arrays.asList(
                "/system/.chainmaster",
                "/system/.chainmaster2",
                "/system/.chainmaster3",
                "/system/chaintest/.content/.config").toString(),
            config.getConfigPaths().toString());
    }

    /**
     * Tests the master configuration feature.<p>
     *
     * @throws Exception -
     */
    public void testMasterConfigurationMultiple() throws Exception {

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
            types2.put("dd", "dd2");
            String config2 = generateSitemapConfigWithTypes(types2, null);
            CmsResource masterConfigResource = cms.createResource(
                "/system/.master",
                masterConfigType,
                config2.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());

            Map<String, String> types3 = new HashMap<>();
            types3.put("bb", "bb3");
            types3.put("cc", "cc3");
            String config3 = generateSitemapConfigWithTypes(types3, null);
            CmsResource masterConfigResource2 = cms.createResource(
                "/system/.master2",
                masterConfigType,
                config3.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());

            Map<String, String> types4 = Maps.newHashMap();
            types4.put("cc", "cc4");
            String config4 = generateSitemapConfigWithTypes(
                types4,
                Arrays.asList("" + masterConfigResource.getStructureId(), "" + masterConfigResource2.getStructureId()));

            cms.createResource(
                "/system/mastertest/subfolder/.content/.config",
                configType,
                config4.getBytes("UTF-8"),
                Collections.<CmsProperty> emptyList());
            OpenCms.getADEManager().waitForCacheUpdate(false);
            checkResourceTypesSet(cms, "/system/mastertest/subfolder", "foldername", "aa1", "bb3", "cc4", "dd2");

        } finally {
            cms.deleteResource("/system/mastertest", CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

    }

    /**
     * Tests merging of master configurations via the ?template=... parameter.
     *
     * @throws Exception if something goes wrong
     */
    public void testMergedMasterConfigurations() throws Exception {

        CmsObject cms = getCmsObject();
        String base = "/testMergedMasterConfigurations";
        CmsVfsUtil.createFolder(cms, "/sites/default" + base + "/.content");
        CmsResource reset = makeMasterConfig(
            cms,
            base + "/reset.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapMasterConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_master_config.xsd\">\n"
                + "  <SitemapMasterConfiguration language=\"en\">\n"
                + "    <DiscardTypes>true</DiscardTypes>\n"
                + "    <UseFormatterKeys>true</UseFormatterKeys>\n"
                + "    <RemoveAllFormatters>true</RemoveAllFormatters>\n"
                + "  </SitemapMasterConfiguration>\n"
                + "</SitemapMasterConfigurations>\n"
                + "");

        CmsResource master1a = makeMasterConfig(
            cms,
            base + "/master1a.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapMasterConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_master_config.xsd\">\n"
                + "  <SitemapMasterConfiguration language=\"en\">\n"
                + "    <DiscardTypes>true</DiscardTypes>\n"
                + "    <ResourceType>\n"
                + "      <TypeName><![CDATA[A]]></TypeName>\n"
                + "    </ResourceType>\n"
                + "    <ResourceType>\n"
                + "      <TypeName><![CDATA[B]]></TypeName>\n"
                + "    </ResourceType>\n"
                + "  </SitemapMasterConfiguration>\n"
                + "</SitemapMasterConfigurations>\n"
                + "");
        CmsResource master1 = makeMasterConfig(
            cms,
            base + "/master1.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapMasterConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_master_config.xsd\">\n"
                + "  <SitemapMasterConfiguration language=\"en\">\n"
                + "    <MasterConfig>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <uuid>"
                + master1a.getStructureId()
                + "</uuid>\n"
                + "      </link>\n"
                + "    </MasterConfig>\n"
                + "  </SitemapMasterConfiguration>\n"
                + "</SitemapMasterConfigurations>\n"
                + "");
        CmsResource master2 = makeMasterConfig(
            cms,
            base + "/master2.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapMasterConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_master_config.xsd\">\n"
                + "  <SitemapMasterConfiguration language=\"en\">\n"
                + "    <DiscardTypes>true</DiscardTypes>\n"
                + "    <ResourceType>\n"
                + "      <TypeName><![CDATA[B]]></TypeName>\n"
                + "    </ResourceType>\n"
                + "    <ResourceType>\n"
                + "      <TypeName><![CDATA[C]]></TypeName>\n"
                + "    </ResourceType>\n"
                + "  </SitemapMasterConfiguration>\n"
                + "</SitemapMasterConfigurations>\n"
                + "");
        CmsResource configRes = makeConfig(
            cms,
            base + "/.content/.config",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<SitemapConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\n"
                + "  <SitemapConfiguration language=\"en\">\n"
                + "    <MasterConfig>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <uuid>"
                + reset.getStructureId()
                + "</uuid>\n"
                + "      </link>\n"
                + "    </MasterConfig>\n"

                + "    <MasterConfig>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <uuid>"
                + master1.getStructureId()
                + "</uuid>\n"
                + "        <query><![CDATA[template=source]]></query>\n"
                + "      </link>\n"
                + "    </MasterConfig>\n"
                + "    <MasterConfig>\n"
                + "      <link type=\"WEAK\">\n"
                + "        <uuid>"
                + master2.getStructureId()
                + "</uuid>\n"
                + "        <query><![CDATA[template=target]]></query>\n"
                + "      </link>\n"
                + "    </MasterConfig>\n"
                + "  </SitemapConfiguration>\n"
                + "</SitemapConfigurations>\n"
                + "");
        OpenCms.getADEManager().waitForCacheUpdate(false);
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, "/sites/default" + base);
        assertEquals(
            new HashSet<>(Arrays.asList("A", "B", "C")),
            config.getResourceTypes().stream().map(t -> t.getTypeName()).collect(Collectors.toSet()));

        // only in source
        assertTrue(config.getTypesByName().get("A").isAvailableInTemplate("source"));
        assertFalse(config.getTypesByName().get("A").isAvailableInTemplate("target"));

        // only in target
        assertTrue(config.getTypesByName().get("C").isAvailableInTemplate("target"));
        assertFalse(config.getTypesByName().get("C").isAvailableInTemplate("source"));

        // in both source and target
        assertTrue(config.getTypesByName().get("B").isAvailableInTemplate("source"));
        assertTrue(config.getTypesByName().get("B").isAvailableInTemplate("target"));

        assertTrue(config.getTypesByName().get("A").isAvailableInTemplate(null));
        assertTrue(config.getTypesByName().get("B").isAvailableInTemplate(null));
        assertTrue(config.getTypesByName().get("C").isAvailableInTemplate(null));

    }

    /**
     * Tests that newly created module configurations are reflected in the configuration objects.<p>
     *
     * @throws Exception -
     */
    @SuppressWarnings("deprecation")
    public void testModuleConfig1() throws Exception {

        CmsObject cms = rootCms();
        String filename = "/system/modules/org.opencms.base/.config";
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
            checkResourceTypes(cms, "/sites/default", "foldername", "a1", "b1", "c1", "foo1", "m0");
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
            checkResourceTypes(cms, "/sites/default/today/news", "foldername", "c3", "e3", "a1", "b1", "foo1");
            checkResourceTypes(cms, "/sites/default/today/news/events/", "foldername", "d2", "c3", "e3", "a1", "foo1");
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
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
            cms.copyResource("/sites/default/today/news/.content", "sites/default/today/.content");
            waitForUpdate(true);
            waitForUpdate(false);
            checkResourceTypes(cms, "/sites/default/today", "foldername", "c3", "e3", "a1", "b1", "foo1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
            publish();
            waitForUpdate(true);
            waitForUpdate(false);
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername", "c3", "e3", "a1", "b1", "foo1");
            checkResourceTypes(onlineCms, "/sites/default/today/events", "foldername", "d2", "c3", "e3", "a1", "foo1");
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
            checkResourceTypes(cms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
            cms.lockResource("/sites/default/.content");
            cms.deleteResource("/sites/default/.content", CmsResource.DELETE_PRESERVE_SIBLINGS);
            waitForUpdate(true);
            checkResourceTypes(onlineCms, "/sites/default/today", "foldername", "a1", "b1", "c1", "foo1");
            publish();
            waitForUpdate(true);
            checkResourceTypes(onlineCms, "/sites/default/today/", "foldername");
        } finally {
            restoreFiles();
        }
    }

    /**
     * Tests whether getSubsiteFolder works correctly with the shared folder.<p>
     *
     * @throws Exception in case resource creation fails
     */
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
    public void testSitemapFolderTypesOverrideModuleFolderTypes() throws Exception {

        CmsObject cms = rootCms();
        String filename = "/system/modules/org.opencms.base/.config";
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
     * Tests new detail page options.
     *
     * @throws Exception -
     */
    public void testSpecialDetailPageOptions() throws Exception {

        CmsObject cms = getCmsObject();
        String base = "/testDetailPageOptions";
        cms.createResource(base, 0);

        boolean preferDetailPages;
        boolean excludeExternalContents;

        preferDetailPages = true;
        excludeExternalContents = true;
        createDetailPageTestSitemap(base + "/aa", preferDetailPages, excludeExternalContents, false);

        preferDetailPages = true;
        excludeExternalContents = false;
        createDetailPageTestSitemap(base + "/ab", preferDetailPages, excludeExternalContents, false);

        preferDetailPages = false;
        excludeExternalContents = true;
        createDetailPageTestSitemap(base + "/ba", preferDetailPages, excludeExternalContents, false);

        preferDetailPages = false;
        excludeExternalContents = false;
        createDetailPageTestSitemap(base + "/bb", preferDetailPages, excludeExternalContents, false);

        OpenCms.getADEManager().waitForCacheUpdate(false);
        String aa = base + "/aa";
        String ab = base + "/ab";
        String ba = base + "/ba";
        String bb = base + "/bb";
        String link;

        link = createLinkFromTo(bb, ab);
        assertTrue(link.contains("detail"));
        assertEquals("ab", getABSubsite(link));

        link = createLinkFromTo(bb, ba);
        assertTrue(link.contains("detail"));
        assertEquals("bb", getABSubsite(link));

        link = createLinkFromTo(ba, bb);
        assertFalse("Link should not be a detail link", link.contains("detail"));

        link = createLinkFromTo(bb, aa);
        assertTrue(link.contains("detail"));
        assertEquals("aa", getABSubsite(link));

        link = createLinkFromTo(aa, aa);
        assertTrue(link.contains("detail"));
        assertEquals("aa", getABSubsite(link));

        link = createLinkFromTo(ab, bb);
        assertTrue(link.contains("detail"));
        assertEquals("ab", getABSubsite(link));

        link = createLinkFromTo(bb, bb);
        assertTrue(link.contains("detail"));
        assertEquals("bb", getABSubsite(link));

        CmsResource page;
        CmsResource content;
        final String articlePath = "/.content/blogentries/article.xml";
        I_CmsDetailPageHandler handler = OpenCms.getADEManager().getDetailPageHandler();
        page = cms.readResource(bb + "/detail/index.html");
        content = cms.readResource(ab + articlePath);
        assertFalse("Should not be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ab + "/detail/index.html");
        content = cms.readResource(bb + articlePath);
        assertTrue("Should be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ab + "/detail/index.html");
        content = cms.readResource(ab + articlePath);
        assertTrue("Should be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ba + "/detail/index.html");
        content = cms.readResource(bb + articlePath);
        assertFalse("Should not be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ba + "/detail/index.html");
        content = cms.readResource(ab + articlePath);
        assertFalse("Should not be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ba + "/detail/index.html");
        content = cms.readResource(bb + articlePath);
        assertFalse("Should not be a valid detail page", handler.isValidDetailPage(cms, page, content));

        page = cms.readResource(ba + "/detail/index.html");
        content = cms.readResource(ba + articlePath);
        assertTrue("Should be a valid detail page", handler.isValidDetailPage(cms, page, content));

    }

    /**
     * Tests the parameter for specifying a detail page.
     *
     * @throws Exception -
     */
    public void testSpecifiedTargetDetailPage() throws Exception {

        waitForUpdate(false);
        CmsObject cms = getCmsObject();
        String detailPage = OpenCms.getADEManager().getDetailPageHandler().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/today/news",
            "/otherpage");
        assertEquals("/sites/default/otherpage/", detailPage);

        detailPage = OpenCms.getADEManager().getDetailPageHandler().getDetailPage(
            cms,
            "/sites/default/.content/a1/blarg.html",
            "/today/news",
            "/sites/default/otherpage/");
        assertEquals("/sites/default/otherpage/", detailPage);
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
    protected <X> List<X> list(@SuppressWarnings("unchecked") X... elems) {

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

    private void createCategoryDetailPageTestSitemap(
        String path,
        boolean preferDetailPages,
        boolean excludeExternalContents,
        boolean includeInSiteSelector)
    throws Exception {

        CmsObject cms = getCmsObject();
        cms.createResource(path, 0);
        cms.createResource(path + "/.content", 0);
        CmsResource detailFolder = cms.createResource(path + "/detail", 0);
        CmsResource fooFolder = cms.createResource(path + "/detail-foo", 0);
        cms.createResource(path + "/detail/index.html", OpenCms.getResourceManager().getResourceType("containerpage"));
        CmsResource special1 = cms.createResource(path + "/default-1", 0);
        cms.createResource(
            path + "/default-1/index.html",
            OpenCms.getResourceManager().getResourceType("containerpage"));

        CmsResource special2 = cms.createResource(path + "/default-2", 0);
        cms.createResource(
            path + "/default-2/index.html",
            OpenCms.getResourceManager().getResourceType("containerpage"));

        String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<SitemapConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\n"
            + "  <SitemapConfiguration language=\"en\">\n"
            + "    <CreateContentsLocally>true</CreateContentsLocally>\n"
            + "    <PreferDetailPagesForLocalContents>"
            + preferDetailPages
            + "</PreferDetailPagesForLocalContents>\n"
            + "    <ExcludeExternalDetailContents>"
            + excludeExternalContents
            + "</ExcludeExternalDetailContents>\n"
            + "<IncludeInSiteSelector>"
            + includeInSiteSelector
            + "</IncludeInSiteSelector>"
            + " <ResourceType>\n"
            + "      <TypeName><![CDATA[article1]]></TypeName>\n"
            + "      <Disabled><![CDATA[false]]></Disabled>\n"
            + "      <Folder>\n"
            + "        <Name><![CDATA[blogentries]]></Name>\n"
            + "      </Folder>\n"
            + "    </ResourceType>"
            + "    <DetailPage>\n"
            + "      <Type><![CDATA[article1]]></Type>\n"
            + "      <Page>\n"
            + "        <link type=\"WEAK\">\n"
            + "          <target><![CDATA["
            + detailFolder.getRootPath()
            + "]]></target>\n"
            + "          <uuid>"
            + detailFolder.getStructureId()
            + "</uuid>\n"
            + "        </link>\n"
            + "      </Page>\n"
            + "    </DetailPage>\n"
            + "    <DetailPage>\n"
            + "      <Type><![CDATA[article1|category:foo]]></Type>\n"
            + "      <Page>\n"
            + "        <link type=\"WEAK\">\n"
            + "          <target><![CDATA["
            + fooFolder.getRootPath()
            + "]]></target>\n"
            + "          <uuid>"
            + fooFolder.getStructureId()
            + "</uuid>\n"
            + "        </link>\n"
            + "      </Page>\n"
            + "    </DetailPage>\n"

            + "    <DetailPage>\n"
            + "      <Type><![CDATA[##DEFAULT##]]></Type>\n"
            + "      <Page>\n"
            + "        <link type=\"WEAK\">\n"
            + "          <target><![CDATA["
            + special1.getRootPath()
            + "]]></target>\n"
            + "          <uuid>"
            + special1.getStructureId()
            + "</uuid>\n"
            + "        </link>\n"
            + "      </Page>\n"
            + "    </DetailPage>\n"
            + "    <DetailPage>\n"
            + "      <Type><![CDATA[##DEFAULT##|category:foo]]></Type>\n"
            + "      <Page>\n"
            + "        <link type=\"WEAK\">\n"
            + "          <target><![CDATA["
            + special2.getRootPath()
            + "]]></target>\n"
            + "          <uuid>"
            + special2.getStructureId()
            + "</uuid>\n"
            + "        </link>\n"
            + "      </Page>\n"
            + "    </DetailPage>\n"

            + "  </SitemapConfiguration>\n"
            + "</SitemapConfigurations>\n"
            + "";
        cms.createResource(path + "/.content/.config", OpenCms.getResourceManager().getResourceType("sitemap_config"));
        CmsFile file = cms.readFile(path + "/.content/.config");
        file.setContents(config.getBytes("UTF-8"));
        cms.writeFile(file);

        cms.createResource(path + "/.content/blogentries", 0);
        cms.createResource(
            path + "/.content/blogentries/article.xml",
            OpenCms.getResourceManager().getResourceType("article1"));

    }

    private void createDetailPageTestSitemap(
        String path,
        boolean preferDetailPages,
        boolean excludeExternalContents,
        boolean includeInSiteSelector)
    throws Exception {

        CmsObject cms = getCmsObject();
        cms.createResource(path, 0);
        cms.createResource(path + "/.content", 0);
        CmsResource detailFolder = cms.createResource(path + "/detail", 0);
        cms.createResource(path + "/detail/index.html", OpenCms.getResourceManager().getResourceType("containerpage"));

        String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<SitemapConfigurations xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/sitemap_config.xsd\">\n"
            + "  <SitemapConfiguration language=\"en\">\n"
            + "    <CreateContentsLocally>true</CreateContentsLocally>\n"
            + "    <PreferDetailPagesForLocalContents>"
            + preferDetailPages
            + "</PreferDetailPagesForLocalContents>\n"
            + "    <ExcludeExternalDetailContents>"
            + excludeExternalContents
            + "</ExcludeExternalDetailContents>\n"
            + "<IncludeInSiteSelector>"
            + includeInSiteSelector
            + "</IncludeInSiteSelector>"
            + " <ResourceType>\n"
            + "      <TypeName><![CDATA[article1]]></TypeName>\n"
            + "      <Disabled><![CDATA[false]]></Disabled>\n"
            + "      <Folder>\n"
            + "        <Name><![CDATA[blogentries]]></Name>\n"
            + "      </Folder>\n"
            + "    </ResourceType>"
            + "    <DetailPage>\n"
            + "      <Type><![CDATA[article1]]></Type>\n"
            + "      <Page>\n"
            + "        <link type=\"WEAK\">\n"
            + "          <target><![CDATA["
            + detailFolder.getRootPath()
            + "]]></target>\n"
            + "          <uuid>"
            + detailFolder.getStructureId()
            + "</uuid>\n"
            + "        </link>\n"
            + "      </Page>\n"
            + "    </DetailPage>\n"
            + "  </SitemapConfiguration>\n"
            + "</SitemapConfigurations>\n"
            + "";
        cms.createResource(path + "/.content/.config", OpenCms.getResourceManager().getResourceType("sitemap_config"));
        CmsFile file = cms.readFile(path + "/.content/.config");
        file.setContents(config.getBytes("UTF-8"));
        cms.writeFile(file);

        cms.createResource(path + "/.content/blogentries", 0);
        cms.createResource(
            path + "/.content/blogentries/article.xml",
            OpenCms.getResourceManager().getResourceType("article1"));

    }

    private String createLinkFromTo(String subsite1, String subsite2) throws Exception {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        cms.getRequestContext().setUri(subsite1);
        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            cms,
            subsite2 + "/.content/blogentries/article.xml");

    }

    private String getABSubsite(String link) {

        Matcher matcher = detailPageTestSubsitePattern.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Creates a sitemap configuration with the specified content.
     *
     * @param cms
     * @param path
     * @param content
     * @return
     * @throws Exception
     */
    private CmsResource makeConfig(CmsObject cms, String path, String content) throws Exception {

        return cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType("sitemap_config"),
            content.getBytes(StandardCharsets.UTF_8),
            new ArrayList<>());
    }

    /**
     * Creates a master configuration with the specified content.
     *
     * @param cms
     * @param path
     * @param content
     * @return
     * @throws Exception
     */
    private CmsResource makeMasterConfig(CmsObject cms, String path, String content) throws Exception {

        return cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType("sitemap_master_config"),
            content.getBytes(StandardCharsets.UTF_8),
            new ArrayList<>());
    }
}
