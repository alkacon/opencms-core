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

import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceInstance;
import org.opencms.ade.configuration.CmsConfigurationReader.DiscardPropertiesMode;
import org.opencms.ade.containerpage.CmsSettingTranslator;
import org.opencms.ade.detailpage.CmsDetailPageFilter;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import junit.framework.ComparisonFailure;
import junit.framework.Test;

/**
 * Lightweight tests for the ADE configuration mechanism which mostly do not read the configuration data from the VFS.<p>
 */
public class TestConfig extends OpenCmsTestCase {

    /** Empty detail page list. **/
    public static final List<CmsDetailPageInfo> NO_DETAILPAGES = Collections.<CmsDetailPageInfo> emptyList();

    /** Empty model page list. **/
    public static final List<CmsModelPageConfig> NO_MODEL_PAGES = Collections.<CmsModelPageConfig> emptyList();

    /** Empty property definition list. **/
    public static final List<CmsPropertyConfig> NO_PROPERTIES = Collections.<CmsPropertyConfig> emptyList();

    /** Empty resource type list. **/
    public static final List<CmsResourceTypeConfig> NO_TYPES = Collections.<CmsResourceTypeConfig> emptyList();

    /**
     * Test constructor.<p>
     *
     * @param name the name
     */
    public TestConfig(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestConfig.class, "ade-config", "/");
    }

    /**
     * Helper method for creating a folder.<p>
     *
     * @param rootPath the root path of the folder
     * @param deep if true, creates all parent folders
     * @param unlock true if the created folder should be unlocked
     *
     * @throws CmsException if something goes wrong
     */
    public void createFolder(String rootPath, boolean deep, boolean unlock) throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        if (!deep) {
            CmsResource res = cms.createResource(rootPath, CmsResourceTypeFolder.getStaticTypeId());
            if (unlock) {
                cms.unlockResource(res);

            }
        } else {
            List<String> parents = new ArrayList<String>();
            String currentPath = rootPath;
            while (currentPath != null) {
                parents.add(currentPath);
                currentPath = CmsResource.getParentFolder(currentPath);
            }
            parents = Lists.reverse(parents);
            System.out.println("Creating folders up to " + rootPath + "(" + parents.size() + ")");

            for (String parent : parents) {
                System.out.println("Creating folder: " + parent);
                try {
                    CmsResource res = cms.createResource(parent, CmsResourceTypeFolder.getStaticTypeId());
                    if (unlock) {
                        cms.unlockResource(res);

                    }
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    // nop
                }

            }
        }
    }

    /**
     * Tests the 'creatable' check of the CmsResourceTypeConfig class.<p>
     *
     * @throws Exception -
     */
    public void testCreatable() throws Exception {

        String baseDirectory = "/sites/default/testCreatable";
        String contentDirectory = baseDirectory + "/.content";
        String plain = "plain";
        String binary = "binary";
        String plainDir = contentDirectory + "/" + plain;
        String binaryDir = contentDirectory + "/" + binary;

        createFolder(plainDir, true, false);
        createFolder(binaryDir, true, false);

        CmsObject cms = rootCms();
        String username = "User_testCreatable";
        cms.createUser(username, "password", "description", Collections.<String, Object> emptyMap());
        cms.chacc(plainDir, I_CmsPrincipal.PRINCIPAL_USER, username, "+r+v-w");
        cms.chacc(binaryDir, I_CmsPrincipal.PRINCIPAL_USER, username, "+r+v+w");

        CmsObject dummyUserCms = rootCms();
        dummyUserCms.getRequestContext().setSiteRoot("/sites/default");
        cms.addUserToGroup(username, OpenCms.getDefaultUsers().getGroupUsers());
        dummyUserCms.loginUser(username, "password");
        dummyUserCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(baseDirectory + "/.content", "plain");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("plain", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "binary",
            false,
            new CmsContentFolderDescriptor(baseDirectory + "/.content", "binary"),
            "binary_%(number).html");

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1, typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        List<CmsResourceTypeConfig> creatableTypes = config1.getCreatableTypes(dummyUserCms, null);
        assertEquals(1, creatableTypes.size());
        assertEquals(binary, creatableTypes.get(0).getTypeName());

        cms.getRequestContext().setSiteRoot("/sites/default");
        creatableTypes = config1.getCreatableTypes(cms, null);

        assertEquals(
            set("plain", "binary"),
            set(creatableTypes.get(0).getTypeName(), creatableTypes.get(1).getTypeName()));

    }

    /**
     * Tests the option to create contents locally.<p>
     *
     * @throws Exception -
     */
    public void testCreateContentsLocally() throws Exception {

        String typename = "plain";
        String baseDirectory = "/sites/default";
        String contentDirectory = baseDirectory + "/.content";

        String baseDirectory2 = "/sites/default/foo";

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(contentDirectory, typename);
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, folder, "file_%(number)");

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            baseDirectory2,
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setCreateContentsLocally(true);
        config2.setParent(config1);
        config2.initialize(rootCms());
        String folderPath = config2.getResourceType("plain").getFolderPath(rootCms(), null);
        assertPathEquals("/sites/default/foo/.content/plain", folderPath);
    }

    /**
     * Another test for the option to create contents locally.<p>
     *
     * @throws Exception -
     */
    public void testCreateContentsLocally2() throws Exception {

        String typename = "plain";
        String baseDirectory = "/sites/default";
        String contentDirectory = baseDirectory + "/.content";

        String baseDirectory2 = "/sites/default/foo";
        String contentDirectory2 = baseDirectory2 + "/.content";

        String baseDirectory3 = "/sites/default/foo/bar";

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(contentDirectory, typename);
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, folder, "file_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "foo",
            false,
            new CmsContentFolderDescriptor(contentDirectory2, "foo"),
            "foo_%(number)");

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            baseDirectory2,
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setParent(config1);
        config2.initialize(rootCms());

        CmsTestConfigData config3 = new CmsTestConfigData(
            baseDirectory3,
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config3.setCreateContentsLocally(true);
        config3.setParent(config2);
        config3.initialize(rootCms());

        String folderPath1 = "/sites/default/foo/bar/.content/foo";
        String folderPath2 = "/sites/default/foo/bar/.content/plain";

        List<CmsResourceTypeConfig> typeConfigs = config3.getResourceTypes();
        assertEquals(2, typeConfigs.size());
        assertEquals(
            set(folderPath1, folderPath2),
            set(typeConfigs.get(0).getFolderPath(rootCms(), null), typeConfigs.get(1).getFolderPath(rootCms(), null)));

        assertEquals(2, config2.getResourceTypes().size());
        assertEquals(
            set("/sites/default/foo/.content/foo", "/sites/default/.content/plain"),
            set(
                config2.getResourceTypes().get(0).getFolderPath(rootCms(), null),
                config2.getResourceTypes().get(1).getFolderPath(rootCms(), null)));
    }

    /**
     * Another test for the option to create contents locally.<p>
     *
     * @throws Exception -
     */
    public void testCreateContentsLocally3() throws Exception {

        String typename = "plain";
        String baseDirectory = "/sites/default";
        String contentDirectory = baseDirectory + "/.content";

        String baseDirectory2 = "/sites/default/foo";
        String contentDirectory2 = baseDirectory2 + "/.content";

        String baseDirectory3 = "/sites/default/foo/bar";

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(contentDirectory, typename);
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, folder, "file_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "foo",
            false,
            new CmsContentFolderDescriptor(contentDirectory2, "foo"),
            "foo_%(number)");

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            baseDirectory2,
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setParent(config1);
        config2.setCreateContentsLocally(true);
        config2.initialize(rootCms());
        CmsTestConfigData config3 = new CmsTestConfigData(
            baseDirectory3,
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config3.setParent(config2);
        config3.initialize(rootCms());
        assertPathEquals(
            "/sites/default/foo/.content/plain",
            config3.getResourceType("plain").getFolderPath(rootCms(), null));
    }

    /**
     * Tests the creation of local contents when no folder name has been defined anywhere.<p>
     *
     * @throws Exception -
     */
    public void testCreateContentsLocally4() throws Exception {

        String typename = "plain";
        String baseDirectory = "/sites/default";
        String baseDirectory2 = "/sites/default/foo";
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, null, "file_%(number)");

        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            baseDirectory2,
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setCreateContentsLocally(true);
        config2.setParent(config1);
        config2.initialize(rootCms());
        String folderPath = config2.getResourceType("plain").getFolderPath(getCmsObject(), null);
        assertPathEquals("/sites/default/foo/.content/plain", folderPath);
    }

    /**
     * Tests the creation of new contents by the CmsResourceTypeConfig class.<p>
     *
     * @throws Exception -
     */
    public void testCreateElements() throws Exception {

        String typename = "plain";

        String baseDirectory = "/sites/default/testCreateElements";
        String contentDirectory = baseDirectory + "/.content";
        String articleDirectory = contentDirectory + "/" + typename;
        try {
            createFolder(articleDirectory, true, true);
        } catch (CmsVfsResourceAlreadyExistsException e) {
            System.out.println("***" + e);
        }
        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(baseDirectory + "/.content", typename);
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig(typename, false, folder, "file_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            baseDirectory,
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        assertSame(config1.getResourceType(typename), typeConf1);
        typeConf1.createNewElement(getCmsObject(), null);
        CmsObject cms = rootCms();
        List<CmsResource> files = cms.getFilesInFolder(articleDirectory);
        assertEquals(1, files.size());
        assertTrue(files.get(0).getName().startsWith("file_"));

        typeConf1.createNewElement(getCmsObject(), null);
        files = cms.getFilesInFolder(articleDirectory);
        assertEquals(2, files.size());
        assertTrue(files.get(0).getName().startsWith("file_"));
        assertTrue(files.get(1).getName().startsWith("file_"));
    }

    /**
     * Tests the generation of the default content folder name.<p>
     *
     * @throws Exception -
     */
    public void testDefaultFolderName() throws Exception {

        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, null, "pattern_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.setIsModuleConfig(true);
        config1.initialize(rootCms());
        typeConf1 = config1.getResourceType("foo");
        String folderPath = typeConf1.getFolderPath(getCmsObject(), null);
        assertPathEquals("/sites/default/.content/foo", folderPath);
    }

    /**
     * Another test for the the generation of the content folder name.<p>
     *
     * @throws Exception -
     */
    public void testDefaultFolderName2() throws Exception {

        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, null, "pattern_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.setIsModuleConfig(true);
        config1.initialize(rootCms());

        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, null, "patternx_%(number)");
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/blah",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.setParent(config1);
        config2.initialize(rootCms());

        typeConf2 = config2.getResourceType("foo");
        String folderPath = typeConf2.getFolderPath(getCmsObject(), null);
        assertPathEquals("/sites/default/.content/foo", folderPath);
    }

    /**
     * Tests filtering of qualified detail pages.
     *
     * @throws Exception if something goes wrong
     */
    public void testDetailPageFiltering() throws Exception {

        CmsDetailPageInfo foo1 = new CmsDetailPageInfo(new CmsUUID(), "/sites/default/a1", "a", "foo", "");
        CmsDetailPageInfo foo2 = new CmsDetailPageInfo(new CmsUUID(), "/sites/default/a2", "a", "foo", "");
        CmsDetailPageInfo bar = new CmsDetailPageInfo(new CmsUUID(), "/sites/default/a3", "a", "bar", "");
        CmsDetailPageInfo unqualified1 = new CmsDetailPageInfo(new CmsUUID(), "/sites/default/a4", "a", null, "");
        CmsDetailPageInfo unqualified2 = new CmsDetailPageInfo(new CmsUUID(), "/sites/default/a5", "a", null, "");
        CmsDetailPageInfo unqualifiedDefault = new CmsDetailPageInfo(
            new CmsUUID(),
            "/sites/default/a6",
            "##DEFAULT##",
            null,
            "");
        CmsDetailPageInfo fooDefault = new CmsDetailPageInfo(
            new CmsUUID(),
            "/sites/default/a7",
            "##DEFAULT##",
            "foo",
            "");
        final Set<String> qualifiersToMatch = new HashSet<>();
        // we use a dummy that doesn't check categories and just uses the set qualifierToMatch for testing qualifiers
        CmsDetailPageFilter filter = new CmsDetailPageFilter(getCmsObject(), (CmsResource)null) {

            @Override
            protected boolean checkQualifier(String qualifier) {

                return qualifiersToMatch.contains(qualifier);
            }
        };

        List<CmsDetailPageInfo> infos2 = filter.filterDetailPages(
            Arrays.asList(fooDefault, unqualifiedDefault, unqualified1, unqualified2, bar, foo1, foo2)).collect(
                Collectors.toList());
        assertEquals(Arrays.asList(unqualified1, unqualified2, unqualifiedDefault), infos2);

        infos2 = filter.filterDetailPages(
            Arrays.asList(foo1, foo2, bar, unqualified1, unqualified2, unqualifiedDefault, fooDefault)).collect(
                Collectors.toList());
        assertEquals(Arrays.asList(unqualified1, unqualified2, unqualifiedDefault), infos2);

        qualifiersToMatch.add("foo");
        infos2 = filter.filterDetailPages(
            Arrays.asList(fooDefault, unqualifiedDefault, unqualified1, unqualified2, bar, foo1, foo2)).collect(
                Collectors.toList());
        assertEquals(Arrays.asList(foo1, foo2, unqualified1, unqualified2, fooDefault, unqualifiedDefault), infos2);

        infos2 = filter.filterDetailPages(
            Arrays.asList(foo1, foo2, bar, unqualified1, unqualified2, unqualifiedDefault, fooDefault)).collect(
                Collectors.toList());
        assertEquals(Arrays.asList(foo1, foo2, unqualified1, unqualified2, fooDefault, unqualifiedDefault), infos2);

        qualifiersToMatch.add("bar");
        infos2 = filter.filterDetailPages(
            Arrays.asList(fooDefault, unqualifiedDefault, unqualified1, unqualified2, bar, foo1, foo2)).collect(
                Collectors.toList());
        assertEquals(
            Arrays.asList(bar, foo1, foo2, unqualified1, unqualified2, fooDefault, unqualifiedDefault),
            infos2);

        infos2 = filter.filterDetailPages(
            Arrays.asList(foo1, foo2, bar, unqualified1, unqualified2, unqualifiedDefault, fooDefault)).collect(
                Collectors.toList());
        assertEquals(
            Arrays.asList(foo1, foo2, bar, unqualified1, unqualified2, fooDefault, unqualifiedDefault),
            infos2);

    }

    /**
     * Tests inheritance of detail page configurations.<p>
     *
     * @throws Exception -
     */
    public void testDetailPages2() throws Exception {

        CmsDetailPageInfo a1 = new CmsDetailPageInfo(getId("/sites/default/a1"), "/sites/default/a1", "a", null, "");
        CmsDetailPageInfo a2 = new CmsDetailPageInfo(getId("/sites/default/a2"), "/sites/default/a2", "a", null, "");
        CmsDetailPageInfo a3 = new CmsDetailPageInfo(getId("/sites/default/a3"), "/sites/default/a3", "a", null, "");
        CmsDetailPageInfo a4 = new CmsDetailPageInfo(getId("/sites/default/a4"), "/sites/default/a4", "a", null, "");

        CmsDetailPageInfo b1 = new CmsDetailPageInfo(getId("/sites/default/b1"), "/sites/default/b1", "b", null, "");
        CmsDetailPageInfo b2 = new CmsDetailPageInfo(getId("/sites/default/b2"), "/sites/default/b2", "b", null, "");

        List<CmsDetailPageInfo> parentDetailPages = list(a1, a2, b1, b2);
        List<CmsDetailPageInfo> childDetailPages = list(a3, a4);

        List<CmsResourceTypeConfig> types = new ArrayList<CmsResourceTypeConfig>();
        types.add(
            new CmsResourceTypeConfig(
                "a",
                false,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                null,
                true,
                false,
                1,
                null,
                null));

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            types,
            NO_PROPERTIES,
            parentDetailPages,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            childDetailPages,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsDetailPageInfo> pages = config2.getAllDetailPages(false);
        //assertEquals(4, pages.size());
        assertEquals(
            set(a3.getUri(), a4.getUri(), b1.getUri(), b2.getUri()),
            set(pages.get(0).getUri(), pages.get(1).getUri(), pages.get(2).getUri(), pages.get(3).getUri()));
        assertEquals(
            list(a3.getUri(), a4.getUri()),
            list(
                config2.getDetailPagesForType("a").get(0).getUri(),
                config2.getDetailPagesForType("a").get(1).getUri()));
    }

    /**
     * Tests the 'Disable all' option for model pages.<p>
     *
     * @throws Exception -
     */
    public void testDiscardInheritedModelPages() throws Exception {

        CmsModelPageConfig m1 = new CmsModelPageConfig(rootCms().readResource("/sites/default/a1"), true, false);
        CmsModelPageConfig m2 = new CmsModelPageConfig(rootCms().readResource("/sites/default/a2"), true, false);
        CmsModelPageConfig m3 = new CmsModelPageConfig(rootCms().readResource("/sites/default/a3"), true, false);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(m1, m2, m3));
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/blah",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        config2.setDiscardInheritedModelPages(true);
        assertEquals(0, config2.getModelPages().size());

    }

    /**
     * Tests the 'Disable all' option for properties.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testDiscardInheritedProperties() throws Exception {

        CmsPropertyConfig foo = createPropertyConfig("foo", "foo1");
        CmsPropertyConfig bar = createPropertyConfig("bar", "bar1");
        CmsPropertyConfig baz = createPropertyConfig("baz", "baz1");

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            NO_TYPES,
            list(foo, bar),
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        CmsTestConfigData config2 = new CmsTestConfigData("/blah", NO_TYPES, list(baz), NO_DETAILPAGES, NO_MODEL_PAGES);
        config2.setDiscardPropertiesMode(DiscardPropertiesMode.discard);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        Map<String, CmsXmlContentProperty> result = config2.getPropertyConfigurationAsMap();
        assertEquals(1, result.size());
        assertNotNull(result.get("baz"));
    }

    /**
     * Tests the 'Disable all' option for resource types.<p>
     *
     * @throws Exception  -
     */
    public void testDiscardInheritedTypes() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "bar",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf3.copy()),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config2.setIsDiscardInheritedTypes(true);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(1, resourceTypeConfig.size());
        assertEquals("baz", resourceTypeConfig.get(0).getTypeName());
    }

    /**
     * Tests that the 'disable all' option in an intermediate sitemap level does not prevent resource type configurations from being inherited.<p>
     *
     * @throws Exception -
     */
    public void testDiscardInheritedTypesMultilevel() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "bar",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsTestConfigData config2 = new CmsTestConfigData("/", NO_TYPES, NO_PROPERTIES, NO_DETAILPAGES, NO_MODEL_PAGES);
        config2.setIsDiscardInheritedTypes(true);

        CmsResourceTypeConfig typeConf4 = new CmsResourceTypeConfig("baz", false, null, null);
        CmsTestConfigData config3 = new CmsTestConfigData(
            "/",
            list(typeConf4),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES

        );

        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config3.initialize(rootCms());
        config2.setParent(config1);
        config3.setParent(config2);
        List<CmsResourceTypeConfig> resourceTypeConfig = config3.getResourceTypes();
        assertEquals(1, resourceTypeConfig.size());
        assertEquals("baz", resourceTypeConfig.get(0).getTypeName());
        assertEquals(folder, resourceTypeConfig.get(0).getFolderOrName());
        assertEquals(typeConf3.getNamePattern(false), resourceTypeConfig.get(0).getNamePattern(false));
    }

    /**
     * Tests inheritance of folder names for resource types.<p>
     * @throws Exception -
     */
    public void testInheritedFolderName1() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/somefolder/.content", "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config2.initialize(rootCms());
        config2.setParent(config1);

        assertPathEquals(
            "/somefolder/.content/blah",
            config2.getResourceType(typeConf1.getTypeName()).getFolderPath(getCmsObject(), null));
    }

    /**
     * Tests inheritance of folder names for resource types.<p>
     *
     * @throws Exception -
     */
    public void testInheritedFolderName2() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/somefolder/.content", "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, null, "blah");
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        CmsResourceTypeConfig rtc = config2.getResourceType(typeConf1.getTypeName());
        String folderPath = rtc.getFolderPath(getCmsObject(), null);
        assertPathEquals("/somefolder/.content/blah", folderPath);
    }

    /**
     * Tests inheritance of name patterns for resource types.<p>
     *
     * @throws Exception -
     */
    public void testInheritNamePattern() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        String pattern1 = "pattern1";
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, pattern1);
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig("foo", false, folder, null);
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        assertEquals(pattern1, config2.getResourceType("foo").getNamePattern());

    }

    /**
     * Tests inheritance of property definitions.<p>
     * @throws Exception -
     */
    public void testInheritProperties() throws Exception {

        List<CmsPropertyConfig> propConf1 = list(
            createPropertyConfig("A", "A1"),
            createPropertyConfig("B", "B1"),
            createPropertyConfig("C", "C1"),
            createPropertyConfig("D", "D1"));
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder",
            NO_TYPES,
            propConf1,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        List<CmsPropertyConfig> propConf2 = list(
            createDisabledPropertyConfig("C"),
            createPropertyConfig("D", "D2"),
            createPropertyConfig("A", "A2"));
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            NO_TYPES,
            propConf2,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsPropertyConfig> resultConf = config2.getPropertyConfiguration();
        assertEquals(3, resultConf.size());

        assertEquals("A", resultConf.get(0).getName());
        assertEquals("A2", resultConf.get(0).getPropertyData().getDescription());
        assertEquals("B", resultConf.get(1).getName());
        assertEquals("B1", resultConf.get(1).getPropertyData().getDescription());

        assertEquals("D", resultConf.get(2).getName());
        assertEquals("D2", resultConf.get(2).getPropertyData().getDescription());

    }

    /**
     * Tests inheritance of resource types.<p>
     *
     * @throws Exception -
     */
    public void testInheritResourceTypes1() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "bar",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(1).getTypeName());
        assertEquals(typeConf2.getTypeName(), resourceTypeConfig.get(0).getTypeName());
    }

    /**
     * Tests inheritance of model pages.<p>
     *
     * @throws Exception -
     */
    public void testModelPages1() throws Exception {

        CmsObject cms = getCmsObject();

        CmsModelPageConfig a1 = new CmsModelPageConfig(cms.readResource("/a1"), false, false);
        CmsModelPageConfig a2 = new CmsModelPageConfig(cms.readResource("/a2"), false, false);

        CmsModelPageConfig b1 = new CmsModelPageConfig(cms.readResource("/b1"), false, false);
        CmsModelPageConfig b2 = new CmsModelPageConfig(cms.readResource("/b2"), false, false);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(a1, a2));
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(b1, b2));
        config2.initialize(rootCms());
        config2.setParent(config1);

        List<CmsModelPageConfig> modelpages = config2.getModelPages();
        assertEquals(b1.getResource().getStructureId(), modelpages.get(0).getResource().getStructureId());
        assertEquals(b2.getResource().getStructureId(), modelpages.get(1).getResource().getStructureId());
        assertEquals(a1.getResource().getStructureId(), modelpages.get(2).getResource().getStructureId());
        assertEquals(a2.getResource().getStructureId(), modelpages.get(3).getResource().getStructureId());
        assertEquals(b1.getResource().getStructureId(), config2.getDefaultModelPage().getResource().getStructureId());
    }

    /**
     * Tests inheritance of model pages.<p>
     *
     * @throws Exception -
     */
    public void testModelPages2() throws Exception {

        CmsObject cms = getCmsObject();

        CmsModelPageConfig a1 = new CmsModelPageConfig(cms.readResource("/a1"), false, false);
        CmsModelPageConfig a2 = new CmsModelPageConfig(cms.readResource("/a2"), false, false);

        CmsModelPageConfig b1 = new CmsModelPageConfig(cms.readResource("/b1"), false, false);
        CmsModelPageConfig b2 = new CmsModelPageConfig(cms.readResource("/b2"), true, false);

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(a1, a2));
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            NO_TYPES,
            NO_PROPERTIES,
            NO_DETAILPAGES,
            list(b1, b2));
        config2.initialize(rootCms());
        config2.setParent(config1);

        assertEquals(b2.getResource().getStructureId(), config2.getDefaultModelPage().getResource().getStructureId());
    }

    /**
     * Tests overriding of resource types.<p>
     *
     * @throws Exception -
     */
    public void testOverrideResourceType() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "foo",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf2),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(1, resourceTypeConfig.size());
        assertEquals(typeConf2.getNamePattern(), resourceTypeConfig.get(0).getNamePattern());
    }

    /**
     * Tests the configuration parser.<p>
     *
     * @throws Exception -
     */
    public void testParseConfiguration() throws Exception {

        CmsObject cms = rootCms();
        CmsConfigurationReader configReader = new CmsConfigurationReader(rootCms());
        CmsADEConfigDataInternal configDataInternal = configReader.parseSitemapConfiguration(
            "/",
            cms.readResource("/sites/default/test.config"));
        CmsADEConfigData configData = new CmsADEConfigData(
            configDataInternal,
            new CmsADEConfigCacheState(
                cms,
                new HashMap<CmsUUID, CmsADEConfigDataInternal>(),
                new ArrayList<CmsADEConfigDataInternal>(),
                new HashMap<CmsUUID, CmsElementView>(),
                new HashMap<>(),
                new HashMap<>()),
            new CmsADEConfigurationSequence(
                Collections.singletonList(new ConfigReferenceInstance(configDataInternal))));
        assertFalse(configData.isModuleConfiguration());
        assertEquals(1, configData.getResourceTypes().size());
        CmsResourceTypeConfig v8article = configData.getResourceType("v8article");
        assertNotNull(v8article);
        assertEquals("asdf", v8article.getNamePattern());
        CmsContentFolderDescriptor folder = v8article.getFolderOrName();
        assertTrue(folder.isName());
        assertEquals("asdf", folder.getFolderName());
        assertEquals(1, configData.getAllDetailPages().size());
        assertEquals(1, configData.getModelPages().size());

        List<CmsPropertyConfig> props = configData.getPropertyConfiguration();
        assertEquals(1, props.size());
        CmsPropertyConfig prop1 = configData.getPropertyConfiguration().get(0);
        assertEquals("prop1", prop1.getName());
        assertEquals(false, prop1.isDisabled());
        assertEquals("displayname", prop1.getPropertyData().getNiceName());
        assertEquals("description", prop1.getPropertyData().getDescription());
        assertEquals("string", prop1.getPropertyData().getWidget());
        assertEquals("default", prop1.getPropertyData().getDefault());
        assertEquals("widgetconfig", prop1.getPropertyData().getWidgetConfiguration());
        assertEquals("ruleregex", prop1.getPropertyData().getRuleRegex());
        assertEquals("string", prop1.getPropertyData().getType());
        assertEquals("ruletype", prop1.getPropertyData().getRuleType());
        assertEquals("error", prop1.getPropertyData().getError());
        assertEquals(true, prop1.getPropertyData().isPreferFolder());
    }

    /**
     * Tests the parsing of module configurations.<p>
     * @throws Exception -
     */
    public void testParseModuleConfiguration() throws Exception {

        CmsObject cms = rootCms();
        CmsConfigurationReader configReader = new CmsConfigurationReader(rootCms());
        CmsADEConfigDataInternal configDataInternal = configReader.parseSitemapConfiguration(
            "/",
            cms.readResource("/sites/default/testmod.config"));
        CmsADEConfigData configData = new CmsADEConfigData(
            configDataInternal,
            new CmsADEConfigCacheState(
                cms,
                new HashMap<CmsUUID, CmsADEConfigDataInternal>(),
                new ArrayList<CmsADEConfigDataInternal>(),
                new HashMap<CmsUUID, CmsElementView>(),
                new HashMap<>(),
                new HashMap<>()),
            new CmsADEConfigurationSequence(
                Collections.singletonList(new ConfigReferenceInstance(configDataInternal))));
        assertTrue(configData.isModuleConfiguration());
        assertEquals(1, configData.getResourceTypes().size());
        CmsResourceTypeConfig anothertype = configData.getResourceType("anothertype");
        assertNotNull(anothertype);
        assertEquals("abc_%(number)", anothertype.getNamePattern());
        CmsContentFolderDescriptor folder = anothertype.getFolderOrName();
        assertTrue(folder.isFolder());
        assertPathEquals("/sites/default", folder.getFolder().getRootPath());
        assertEquals(0, configData.getAllDetailPages().size());
        assertEquals(0, configData.getModelPages().size());
        List<CmsPropertyConfig> props = configData.getPropertyConfiguration();
        assertEquals(1, props.size());
        CmsPropertyConfig prop1 = configData.getPropertyConfiguration().get(0);
        assertEquals("propertyname1", prop1.getName());
        assertEquals(false, prop1.isDisabled());
        assertEquals("displayname1", prop1.getPropertyData().getNiceName());
        assertEquals("description1", prop1.getPropertyData().getDescription());
        assertEquals("string", prop1.getPropertyData().getWidget());
        assertEquals("default1", prop1.getPropertyData().getDefault());
        assertEquals("widgetconfig1", prop1.getPropertyData().getWidgetConfiguration());
        assertEquals("ruleregex1", prop1.getPropertyData().getRuleRegex());
        assertEquals("string", prop1.getPropertyData().getType());
        assertEquals("ruletype1", prop1.getPropertyData().getRuleType());
        assertEquals("error1", prop1.getPropertyData().getError());
        assertEquals(true, prop1.getPropertyData().isPreferFolder());
    }

    /**
     * Tests setting translation parsing.
     */
    public void testParseSettingTranslation() {

        Map<String, String> map = CmsSettingTranslator.parseSettingTranslationMap(
            "  foo:bar  |\nbaz:qux\n|qoo  :  xyzzy");
        assertEquals(3, map.size());
        assertEquals("foo", map.get("bar"));
        assertEquals("baz", map.get("qux"));
        assertEquals("qoo", map.get("xyzzy"));
    }

    /**
     * Tests site plugin inheritance in the sitemap configuration.
     *
     * @throws Exception if something goes wrong
     */
    public void testPluginInheritance() throws Exception {

        CmsUUID a = new CmsUUID(CmsUUID.getNullUUID().toString().replaceAll("0", "a"));
        CmsUUID b = new CmsUUID(CmsUUID.getNullUUID().toString().replaceAll("0", "b"));
        CmsUUID c = new CmsUUID(CmsUUID.getNullUUID().toString().replaceAll("0", "c"));
        CmsUUID d = new CmsUUID(CmsUUID.getNullUUID().toString().replaceAll("0", "d"));
        CmsTestConfigData c0 = CmsTestConfigData.buildTestDataForPlugins(
            getCmsObject(),
            "/",
            false,
            new HashSet<>(Arrays.asList(d)),
            new HashSet<>());
        CmsTestConfigData c1 = CmsTestConfigData.buildTestDataForPlugins(
            getCmsObject(),
            "/foo",
            true,
            new HashSet<>(Arrays.asList(a, b)),
            new HashSet<>());
        CmsTestConfigData c2 = CmsTestConfigData.buildTestDataForPlugins(
            getCmsObject(),
            "/foo/bar",
            false,
            new HashSet<>(Arrays.asList(c)),
            new HashSet<>(Arrays.asList(b)));
        c1.setParent(c0);
        c2.setParent(c1);
        Set<CmsUUID> c2Plugins = c2.getSitePluginIds();
        assertEquals(new HashSet<>(Arrays.asList(a, c)), c2Plugins);

    }

    /**
     * Test for disabling single resource types.<p>
     *
     * @throws Exception -
     */
    public void testRemoveResourceType() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "bar",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsResourceTypeConfig removeType = new CmsResourceTypeConfig("bar", true, null, null);
        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(removeType),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(2, resourceTypeConfig.size());
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(0).getTypeName());
        assertEquals(typeConf3.getTypeName(), resourceTypeConfig.get(1).getTypeName());
    }

    /**
     * Test for reordering resource types.<p>
     *
     * @throws Exception -
     */
    public void testReorderResourceTypes() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor("/.content", "foldername");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsResourceTypeConfig typeConf2 = new CmsResourceTypeConfig(
            "bar",
            false,
            new CmsContentFolderDescriptor("/.content", "foldername2"),
            "pattern2_%(number)");
        CmsResourceTypeConfig typeConf3 = new CmsResourceTypeConfig("baz", false, folder, "blah");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/",
            list(typeConf1, typeConf2, typeConf3),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/",
            list(typeConf3.copy(), typeConf1.copy()),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);

        config1.initialize(rootCms());
        config2.initialize(rootCms());
        config2.setParent(config1);
        List<CmsResourceTypeConfig> resourceTypeConfig = config2.getResourceTypes();
        assertEquals(3, resourceTypeConfig.size());
        assertEquals(typeConf3.getTypeName(), resourceTypeConfig.get(0).getTypeName());
        assertEquals(typeConf1.getTypeName(), resourceTypeConfig.get(1).getTypeName());
        assertEquals(typeConf2.getTypeName(), resourceTypeConfig.get(2).getTypeName());
    }

    /**
     * Tests folder name generation.<p>
     *
     * @throws Exception -
     */
    public void testResolveFolderName1() throws Exception {

        CmsContentFolderDescriptor folder = new CmsContentFolderDescriptor(
            "/somefolder/somesubfolder/.content",
            "blah");
        CmsResourceTypeConfig typeConf1 = new CmsResourceTypeConfig("foo", false, folder, "pattern_%(number)");
        CmsTestConfigData config1 = new CmsTestConfigData(
            "/somefolder/somesubfolder",
            list(typeConf1),
            NO_PROPERTIES,
            NO_DETAILPAGES,
            NO_MODEL_PAGES);
        config1.initialize(rootCms());
        assertPathEquals(
            "/somefolder/somesubfolder/.content/blah",
            config1.getResourceType(typeConf1.getTypeName()).getFolderPath(getCmsObject(), null));
    }

    /**
     * Tests that for each configuration file there are distinct resource type configuration objects.<p>
     *
     * @throws Exception -
     */
    public void testResourceTypeConfigObjectsNotSame() throws Exception {

        CmsResourceTypeConfig c1 = new CmsResourceTypeConfig("c", false, null, "foo");
        CmsTestConfigData t1 = new CmsTestConfigData("/", list(c1), NO_PROPERTIES, NO_DETAILPAGES, NO_MODEL_PAGES);
        CmsTestConfigData t2 = new CmsTestConfigData("/", NO_TYPES, NO_PROPERTIES, NO_DETAILPAGES, NO_MODEL_PAGES);
        t1.initialize(rootCms());
        t2.initialize(rootCms());
        t2.setParent(t1);
        assertNotNull(t2.getResourceType("c"));
        assertNotSame(t1.getResourceType("c"), t2.getResourceType("c"));
    }

    /**
     * Tests inheritance of detail page configurations.<p>
     *
     * @throws Exception -
     */
    public void testUnsetTypeAvailability() throws Exception {

        List<CmsResourceTypeConfig> types = new ArrayList<CmsResourceTypeConfig>();
        types.add(
            new CmsResourceTypeConfig(
                "a",
                false,
                null,
                null,
                false,
                true,
                true,
                false,
                false,
                false,
                null,
                null,
                true,
                false,
                1,
                null,
                null));

        List<CmsResourceTypeConfig> childTypes = new ArrayList<CmsResourceTypeConfig>();
        childTypes.add(
            new CmsResourceTypeConfig(
                "a",
                false,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                true,
                null,
                null,
                true,
                false,
                1,
                null,
                null));

        CmsTestConfigData config1 = new CmsTestConfigData(
            "/sites/default",
            types,
            NO_PROPERTIES,
            new ArrayList<>(),
            NO_MODEL_PAGES);
        config1.initialize(rootCms());

        CmsTestConfigData config2 = new CmsTestConfigData(
            "/sites/default/foo",
            childTypes,
            NO_PROPERTIES,
            new ArrayList<>(),
            NO_MODEL_PAGES);
        config2.initialize(rootCms());
        config2.setParent(config1);

        assertTrue(config2.getResourceType("a").isAddDisabled());
        assertTrue(config2.getResourceType("a").isCreateDisabled());

    }

    /**
     * Helper method for comparing paths which ignores leading/trailing slashes.<p>
     *
     * @param path1 the first path
     * @param path2 the second path
     */
    protected void assertPathEquals(String path1, String path2) {

        if ((path1 == null) && (path2 == null)) {
            return;
        }
        if ((path1 == null) || (path2 == null)) {
            throw new ComparisonFailure("comparison failure", path1, path2);
        }
        assertEquals(CmsStringUtil.joinPaths("/", path1, "/"), CmsStringUtil.joinPaths("/", path2, "/"));
    }

    /**
     * Helper method for creating a disabled property configuration.<p>
     *
     * @param name the property name
     *
     * @return the property configuration object
     */
    protected CmsPropertyConfig createDisabledPropertyConfig(String name) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, null);
        return new CmsPropertyConfig(prop, true);
    }

    /**
     * Helper method for creating a property configuration object.<p>
     *
     * @param name the property name
     * @param description the property description
     * @return the property configuration object
     */
    protected CmsPropertyConfig createPropertyConfig(String name, String description) {

        CmsXmlContentProperty prop = createXmlContentProperty(name, description);
        return new CmsPropertyConfig(prop, false);
    }

    /**
     * Helper method for creating an XML content property with only a name and description.<p>
     *
     * @param name the property name
     * @param description the property description
     * @return the content property description bean
     */
    protected CmsXmlContentProperty createXmlContentProperty(String name, String description) {

        return new CmsXmlContentProperty(name, "string", "string", "", "", "", "", "", description, null, null);
    }

    /**
     * Helper method for dumping the whole VFS tree to the console.<p>
     *
     * @throws CmsException -
     */
    protected void dumpTree() throws CmsException {

        CmsObject cms = rootCms();
        CmsResource root = cms.readResource("/");
        dumpTree(cms, root, 0);
    }

    /**
     * Helper method for dumping a VFS tree to the console.<p>
     *
     * @param cms the CMS context
     * @param res the root resource
     * @param indentation the initial indentation level
     * @throws CmsException if something goes wrong
     */
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

    /**
     * Helper method for getting the structure id of a file given by root path.<p>
     *
     * @param rootPath the file root path
     * @return the structure id of the file
     * @throws CmsException -
     */
    protected CmsUUID getId(String rootPath) throws CmsException {

        CmsObject cms = rootCms();
        return cms.readResource(rootPath).getStructureId();
    }

    /**
     * Helper method for creating a list of elements.<p>
     *
     * @param elems the list elements
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
     * Helper method for getting a CMS context in the root site.<p>
     *
     * @return a CMS context in the root site
     *
     * @throws CmsException -
     */
    protected CmsObject rootCms() throws CmsException {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        return cms;
    }

    /**
     * Helper method for creating a set of elements.<p>
     *
     * @param elems the elements
     * @return a set of the elements
     */
    protected <X> Set<X> set(X... elems) {

        Set<X> result = new HashSet<X>();
        for (X x : elems) {
            result.add(x);
        }
        return result;
    }

    /**
     * Helper method for writing a number of spaces.<p>
     *
     * @param indent the number of spaces to write
     */
    protected void writeIndentation(int indent) {

        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
    }

}
