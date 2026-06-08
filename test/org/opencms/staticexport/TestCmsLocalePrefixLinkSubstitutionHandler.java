/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsPair;

import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/** Tests for the {@link CmsLocalePrefixLinkSubstitutionHandler}. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsLocalePrefixLinkSubstitutionHandler extends OpenCmsTestRunner {

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "multisite", "/");
        setUpLocalizationMode();
    }

    /**
     * Test path and parameter adjustment in the single tree scenario.
     * @throws CmsException thrown if getting the CmsObject fails
     */
    @Test
    @Order(1)
    public void testAddVfsPrefix() throws CmsException {

        CmsLocalePrefixLinkSubstitutionHandler handler = new CmsLocalePrefixLinkSubstitutionHandler();
        CmsObject cms = getCmsObject();
        String vfsName = "/folder1/subfolder11/";
        String defaultPrefix = "/data/opencms";
        CmsSite targetSite = OpenCms.getSiteManager().getSite("/sites/default/", null);
        CmsPair<String, String> result;

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=de");
        assertEquals(defaultPrefix + "/de" + vfsName, result.getFirst());
        assertEquals(null, result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=en_GB&other=test");
        assertEquals(defaultPrefix + "/en_GB" + vfsName, result.getFirst());
        assertEquals("?other=test", result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?some=value&__locale=en_DE&other=test");
        assertEquals(defaultPrefix + "/en_DE" + vfsName, result.getFirst());
        assertEquals("?some=value&other=test", result.getSecond());

        // cs is not available as locale, so it should be ignored.
        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?__locale=cs&other=test");
        assertEquals(defaultPrefix + "/en" + vfsName, result.getFirst());
        assertEquals("?__locale=cs&other=test", result.getSecond());

        result = handler.addVfsPrefix(cms, vfsName, targetSite, "?some=value&other=test");
        assertEquals(defaultPrefix + "/en" + vfsName, result.getFirst());
        assertEquals("?some=value&other=test", result.getSecond());

    }

    /**
     * Tests root path evaluation.<p>
     *
     * @throws Exception in case something goes wrong
     */
    @Test
    @Order(2)
    public void testGetRootPath() throws Exception {

        CmsObject cms = getCmsObject();
        //   echo("Testing symmetric link / root path substitution with a custom link handler");

        CmsLinkManager lm = OpenCms.getLinkManager();
        I_CmsLinkSubstitutionHandler lh = new CmsLocalePrefixLinkSubstitutionHandler();
        lm.setLinkSubstitutionHandler(cms, lh);
        testGetRootPath(cms, "/sites/default/index.html");
        testGetRootPath(cms, "/shared/sharedFile.txt");
    }

    /**
     * Preserves the legacy suite setup which switched the default site to single-tree localization.<p>
     */
    private void setUpLocalizationMode() {

        TreeMap<String, String> parameters = new TreeMap<String, String>();
        parameters.put("localizationMode", "singleTree");
        OpenCms.getSiteManager().getSite("/sites/default/", null).setParameters(parameters);
    }

    /**
     * Tests root path evaluation.<p>
     *
     * @param cms the current OpenCms context
     * @param path the resource path in the VFS to check the links for
     *
     * @throws Exception in case the test fails
     */
    private void testGetRootPath(CmsObject cms, String path) throws Exception {

        CmsLinkManager lm = OpenCms.getLinkManager();

        // first try: no server info
        String link = lm.substituteLinkForRootPath(cms, path);
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);

        // second try: with server and protocol
        link = lm.getServerLink(cms, path);
        rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);

        // test also server links that don't have the locale prefix
        link = cms.getRequestContext().removeSiteRoot(path);
        link = OpenCms.getStaticExportManager().getVfsPrefix().concat(link);
        link = OpenCms.getSiteManager().getSiteForSiteRoot(cms.getRequestContext().getSiteRoot()).getServerPrefix(
            cms,
            link).concat(link);
        rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);
    }
}
