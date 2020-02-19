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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import static org.junit.Assert.assertNotEquals;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.relations.CmsRelationType;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;

/**
 * Unit tests for the <code>{@link CmsJspResourceWrapper}</code>.<p>
 */
public class TestCmsJspResourceWrapper extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsJspResourceWrapper(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestCmsJspResourceWrapper.class, "simpletest", "/");
    }

    /**
     * Tests for basic wrapper methods.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBasics() throws Exception {

        CmsObject cms = getCmsObject();

        CmsJspResourceWrapper topFolderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/"));
        CmsJspResourceWrapper topFileRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/index.html"));
        CmsJspResourceWrapper folderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/"));
        CmsJspResourceWrapper fileRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/index.html"));
        CmsJspResourceWrapper subFolderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/subfolder12/"));
        CmsJspResourceWrapper subFileRes = CmsJspResourceWrapper.wrap(
            cms,
            cms.readResource("/folder1/subfolder12/index.html"));

        // Path levels
        assertEquals(0, topFolderRes.getSitePathLevel());
        assertEquals(0, topFileRes.getSitePathLevel());
        assertEquals(1, folderRes.getSitePathLevel());
        assertEquals(1, fileRes.getSitePathLevel());
        assertEquals(2, subFolderRes.getSitePathLevel());
        assertEquals(2, subFileRes.getSitePathLevel());

        assertEquals(2, topFolderRes.getRootPathLevel());
        assertEquals(2, topFileRes.getRootPathLevel());
        assertEquals(3, folderRes.getRootPathLevel());
        assertEquals(3, fileRes.getRootPathLevel());
        assertEquals(4, subFolderRes.getRootPathLevel());
        assertEquals(4, subFileRes.getRootPathLevel());

        // Parent folder resource operations
        assertEquals(topFolderRes, topFileRes.getFolder());
        assertEquals(topFolderRes, folderRes.getParentFolder());
        assertEquals(topFolderRes, fileRes.getParentFolder().getParentFolder());
        assertEquals(topFolderRes, subFolderRes.getParentFolder().getParentFolder());
        assertEquals(folderRes, fileRes.getFolder());
        assertEquals(subFolderRes, subFileRes.getFolder());

        // Site path access
        assertEquals("/", folderRes.getParentFolder().getSitePath());
        assertEquals("/folder1/", fileRes.getParentFolder().getSitePath());
        assertEquals("/folder1/", fileRes.getSitePathParentFolder());
        assertEquals("/folder1/subfolder12/", subFolderRes.getSitePath());
        assertEquals("/folder1/subfolder12/", subFileRes.getSitePathParentFolder());

        // Root path access
        assertEquals("/sites/default/", folderRes.getParentFolder().getRootPath());
        assertEquals("/sites/default/folder1/", fileRes.getParentFolder().getRootPath());
        assertEquals("/sites/default/folder1/", fileRes.getRootPathParentFolder());
        assertEquals("/sites/default/folder1/subfolder12/", subFolderRes.getRootPath());
        assertEquals("/sites/default/folder1/subfolder12/", subFileRes.getRootPathParentFolder());

        // Extensions
        assertEquals("html", topFileRes.getExtension());
        assertEquals(topFileRes.getExtension(), topFileRes.getResourceExtension());

        // File names
        assertEquals("index.html", subFileRes.getName());
        assertEquals(subFileRes.getName(), subFileRes.getResourceName());
    }

    /**
     * Tests for nav elements with wrapper.<p>
     *
     * @throws Exception if the test fails
     */
    public void testNavElements() throws Exception {

        CmsObject cms = getCmsObject();

        CmsJspResourceWrapper topFolderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/"));
        CmsJspResourceWrapper subFolderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/"));

        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);

        CmsJspNavElement navElemTop = topFolderRes.getNavigation();
        assertEquals(-1, navElemTop.getNavTreeLevel());

        CmsJspNavElement navElemSub = subFolderRes.getNavigation();
        assertEquals(0, navElemSub.getNavTreeLevel());

        CmsJspNavElement navElemBuildTop = navBuilder.getNavigationForResource("/");
        assertEquals(navElemBuildTop, navElemTop);
        assertEquals(-1, navElemBuildTop.getNavTreeLevel());
        assertEquals(navElemBuildTop.getNavTreeLevel(), navElemTop.getNavTreeLevel());

        assertEquals(navElemSub, subFolderRes.getNavBuilder().getNavigationForResource());
        assertEquals(navElemTop, topFolderRes.getNavBuilder().getNavigationForResource());

        CmsJspNavElement navElemBuildSub = navBuilder.getNavigationForResource("/folder1/");
        assertEquals(navElemBuildSub, navElemSub);
        assertEquals(0, navElemBuildSub.getNavTreeLevel());
        assertEquals(navElemBuildSub.getNavTreeLevel(), navElemSub.getNavTreeLevel());

        // default file access
        CmsJspResourceWrapper indexRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/index.html"));
        assertNotNull(indexRes);
        assertEquals(indexRes, subFolderRes.getNavigationDefaultFile());

        // navigation in folder access
        List<CmsJspNavElement> subFolderNav = subFolderRes.getNavigationForFolder();
        assertEquals(7, subFolderNav.size());
    }

    /**
     * Tests for parent folder access.<p>
     *
     * @throws Exception if the test fails
     */
    public void testParentFolders() throws Exception {

        CmsObject cms = getCmsObject();

        CmsJspResourceWrapper topFolderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/"));
        CmsJspResourceWrapper folder1Res = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/"));
        CmsJspResourceWrapper folderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/subfolder12/"));
        CmsJspResourceWrapper fileRes = CmsJspResourceWrapper.wrap(
            cms,
            cms.readResource("/folder1/subfolder12/index.html"));

        assertNull(topFolderRes.getParentFolder());
        assertEquals(2, folderRes.getParentFolders().size());
        assertEquals(3, fileRes.getParentFolders().size());
        assertEquals(folderRes, fileRes.getParentFolders().get(0));
        assertEquals(topFolderRes, fileRes.getParentFolders().get(2));

        assertTrue(topFolderRes.isParentFolderOf(fileRes.getSitePath()));
        assertTrue(topFolderRes.isParentFolderOf(folderRes.getSitePath()));
        assertTrue(folderRes.isParentFolderOf(fileRes.getSitePath()));
        assertTrue(folderRes.isParentFolderOf("/folder1/subfolder12/iDontExist.butWhoCares"));

        assertTrue(topFolderRes.isParentFolderOf(fileRes));
        assertTrue(topFolderRes.isParentFolderOf(folderRes));
        assertTrue(folderRes.isParentFolderOf(fileRes));

        assertFalse(folderRes.isParentFolderOf(topFolderRes));
        assertFalse(fileRes.isParentFolderOf(folderRes));
        assertFalse(folderRes.isParentFolderOf("/folder1/subfolder12/"));
        assertFalse(folderRes.isParentFolderOf((String)null));
        assertFalse(folderRes.isParentFolderOf((CmsResource)null));

        assertTrue(fileRes.isChildResourceOf(topFolderRes.getSitePath()));
        assertTrue(folderRes.isChildResourceOf(topFolderRes.getSitePath()));
        assertTrue(fileRes.isChildResourceOf(folderRes.getSitePath()));
        assertTrue(fileRes.isChildResourceOf("/folder1/subfolder12"));
        assertFalse(fileRes.isChildResourceOf((String)null));
        assertFalse(fileRes.isChildResourceOf((CmsResource)null));

        assertTrue(fileRes.isChildResourceOf(topFolderRes));
        assertTrue(folderRes.isChildResourceOf(topFolderRes));
        assertTrue(fileRes.isChildResourceOf(folderRes));

        assertEquals(folderRes, fileRes.getParentFolder());
        assertEquals(folder1Res, folderRes.getParentFolder());
        assertEquals(topFolderRes, folder1Res.getParentFolder());

        assertEquals("/folder1/subfolder12/", fileRes.getSitePathFolder());
        assertEquals("/folder1/subfolder12/", fileRes.getSitePathParentFolder());
        assertEquals("/folder1/", folderRes.getParentFolder().getSitePathFolder());
        assertEquals("/folder1/", folderRes.getSitePathParentFolder());
        assertEquals("/", folder1Res.getSitePathParentFolder());
    }

    /**
     * Tests for wrapper access to properties.<p>
     *
     * @throws Exception if the test fails
     */
    public void testProperties() throws Exception {

        CmsObject cms = getCmsObject();
        CmsJspResourceWrapper res1 = CmsJspResourceWrapper.wrap(cms, cms.readResource("/index.html"));

        // Property access
        Map<String, String> props = res1.getProperty();
        assertNotNull(props);
        String title = props.get(CmsPropertyDefinition.PROPERTY_TITLE);
        assertEquals("Index page", title);

        // Localized properties
        String[] testLocales = TestCmsJspVfsAccessBean.setupPropertyLocaleTest(cms);
        CmsJspResourceWrapper wrap1 = CmsJspResourceWrapper.wrap(
            cms,
            cms.readResource("/test_read_property_locale/test.txt"));
        String[] expectedPostfix = new String[] {"", "_de", "_de_DE", "_en", "_en", ""};
        String directPropertyName = "direct";
        String searchedPropertyName = "searched";
        for (int i = 0; i < testLocales.length; i++) {
            // without search
            assertEquals(
                directPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocale().get(testLocales[i]).get(directPropertyName));
            assertEquals(
                directPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocale().get(testLocales[i]).get(directPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                wrap1.getPropertyLocale().get(testLocales[i]).get(searchedPropertyName));
            assertEquals(
                CmsProperty.getNullProperty().getValue(),
                wrap1.getPropertyLocale().get(testLocales[i]).get(searchedPropertyName));
            // with search
            assertEquals(
                directPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocaleSearch().get(testLocales[i]).get(directPropertyName));
            assertEquals(
                directPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocaleSearch().get(testLocales[i]).get(directPropertyName));
            assertEquals(
                searchedPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocaleSearch().get(testLocales[i]).get(searchedPropertyName));
            assertEquals(
                searchedPropertyName + expectedPostfix[i],
                wrap1.getPropertyLocaleSearch().get(testLocales[i]).get(searchedPropertyName));
        }
    }

    /**
     * Test related resource accessors.
     *
     * @throws Exception -
     */
    public void testRelations() throws Exception {

        CmsObject cms = getCmsObject();
        String src = "/testRelations1.txt";
        String tgt = "/testRelations2.txt";
        CmsResource srcRes = cms.createResource(src, CmsResourceTypePlain.getStaticTypeId());
        CmsResource tgtRes = cms.createResource(tgt, CmsResourceTypePlain.getStaticTypeId());
        cms.addRelationToResource(src, tgt, CmsRelationType.CATEGORY.getName());
        CmsJspResourceWrapper srcWrapper = CmsJspResourceWrapper.wrap(cms, cms.readResource(src));
        assertEquals(
            Arrays.asList(tgtRes.getStructureId()),
            srcWrapper.getOutgoingRelations().stream().map(res -> res.getStructureId()).collect(Collectors.toList()));
        assertEquals(0, srcWrapper.getIncomingRelations().size());

        CmsJspResourceWrapper tgtWrapper = CmsJspResourceWrapper.wrap(cms, cms.readResource(tgt));
        assertEquals(
            Arrays.asList(srcRes.getStructureId()),
            tgtWrapper.getIncomingRelations().stream().map(res -> res.getStructureId()).collect(Collectors.toList()));
        assertEquals(0, tgtWrapper.getOutgoingRelations().size());

    }

    /**
     * Tests for resource wrapper identities.<p>
     *
     * @throws Exception if the test fails
     */
    public void testResourceIdentities() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource res1 = cms.readResource("/folder1/index.html");
        CmsJspResourceWrapper wrap1 = CmsJspResourceWrapper.wrap(cms, res1);

        // wrapping a wrapper returns the wrapper
        assertSame(wrap1, CmsJspResourceWrapper.wrap(cms, wrap1));
        // wrapping a resource returns a new wrapper
        assertNotSame(wrap1, CmsJspResourceWrapper.wrap(cms, res1));
        // new wrapper and exiting wrapper of same resource must be equal
        assertEquals(wrap1, CmsJspResourceWrapper.wrap(cms, res1));
        // also same hash code
        assertEquals(wrap1.hashCode(), CmsJspResourceWrapper.wrap(cms, res1).hashCode());

        // new context with same URI
        CmsObject cms2 = getCmsObject();
        CmsResource res2 = cms2.readResource("/folder1/index.html");
        CmsJspResourceWrapper wrap2 = CmsJspResourceWrapper.wrap(cms2, res2);
        // wrapper created from different contexts with same URI are not the same, but must be equal
        assertNotSame(wrap1, wrap2);
        assertEquals(wrap1, wrap2);
        assertEquals(wrap1.hashCode(), wrap2.hashCode());

        // new context with different URI
        CmsObject cms3 = getCmsObject();
        assertNotSame(cms, cms3);
        cms3.getRequestContext().setSiteRoot("/sites/default/folder1/");
        assertNotEquals(cms3.getRequestContext().getSiteRoot(), cms.getRequestContext().getSiteRoot());

        CmsResource res3 = cms3.readResource("/index.html");
        CmsJspResourceWrapper wrap3 = CmsJspResourceWrapper.wrap(cms3, res3);
        // wrapper created from different contexts with different URI are not the same, but must be equal
        assertNotSame(wrap1, wrap3);
        assertEquals(wrap1, wrap3);
        assertEquals(wrap1.hashCode(), wrap3.hashCode());

        // wrapping a wrapper with a different context URI must create a different wrapper
        CmsJspResourceWrapper wrap4 = CmsJspResourceWrapper.wrap(cms3, wrap1);
        assertNotSame(wrap1, wrap4);
    }

    /**
     * Tests for the {@link CmsJspVfsAccessBean#getReadXml()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testXml() throws Exception {

        CmsObject cms = getCmsObject();

        // access XML content
        CmsJspResourceWrapper res1 = CmsJspResourceWrapper.wrap(cms, cms.readResource("/xmlcontent/article_0001.html"));
        CmsJspContentAccessBean content = res1.getXml();
        assertTrue(res1.getIsXml());
        assertEquals("Alkacon Software", content.getValue().get("Author").toString());

        // access XML page
        CmsJspResourceWrapper res2 = CmsJspResourceWrapper.wrap(cms, cms.readResource("/index.html"));
        content = res2.getXml();
        assertTrue(res2.getIsXml());
        assertEquals(Boolean.TRUE, content.getHasValue().get("body"));
        assertEquals(Boolean.FALSE, content.getHasValue().get("element"));
        System.out.println(content.getValue().get("body"));

        // access folder as XML
        CmsJspResourceWrapper folderRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/"));
        assertFalse(folderRes.getIsXml());
        assertNull(folderRes.getXml());

        // access file as XML
        CmsJspResourceWrapper fileRes = CmsJspResourceWrapper.wrap(cms, cms.readResource("/folder1/image1.gif"));
        assertFalse(fileRes.getIsXml());
        assertNull(fileRes.getXml());
    }
}