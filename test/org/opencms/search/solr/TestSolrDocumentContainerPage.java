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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.test.OpenCmsTestRunner;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests the content extraction via {@link CmsSolrDocumentContainerPage}.<p>
 *
 * In particular, we test the feature to skip the extraction of the content of a container's elements
 * for the indexed content of the container page, based on the container's name or type.
 * The configuration is read from the sitemap attributes
 * <code>search.containers.exclude.names</code> and <code>search.containers.exclude.types</code>.
 *
 * The tests build on the container pages imported via the module
 * <code>test.contentextraction.containerpage</code>.
 * Essentially, the same page is placed in various different sub-sitemaps that have different attributes
 * for exclusion defined.
 *
 * See the module's data for details.
 *
 */
public class TestSolrDocumentContainerPage extends OpenCmsTestRunner {

    /** Content marker that only appears in the element of the top-level "main" container. */
    private static final String MARKER_MAIN = "MAIN";

    /** Content marker that only appears in the element of the nested container. */
    private static final String MARKER_NESTED = "NESTED";

    /** Content marker that only appears in the element of the "sidebar" container. */
    private static final String MARKER_ASIDE = "ASIDE";

    private static final String SUBSITEMAP_PATH = "/test.extraction/";

    private static final String INDEX_HTML = "index.html";

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "adetest", "/sites/default/", "ade-setup");
        try {
            CmsObject cms = getCmsObject();
            importCoreModule(cms, "org.opencms.base");
            importModule(cms, "test.contentextraction.containerpage");
            OpenCms.getADEManager().waitForCacheUpdate(false);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set up the container page exclusion test.", e);
        }
    }

    /**
     * Scenario C: a container whose type attribute lists several comma-separated types is excluded
     * if any of those types is configured for exclusion.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testExcludeByTypeForMultiTypeContainer() throws Exception {

        String content = extractPageContent(SUBSITEMAP_PATH + "by-type/" + INDEX_HTML);
        assertFalse(
            content.contains(MARKER_ASIDE),
            "A container must be excluded if one of its comma-separated types is configured for exclusion.");
        assertTrue(content.contains(MARKER_MAIN), "Content of a container with a non-excluded type must be extracted.");
        assertTrue(
            content.contains(MARKER_NESTED),
            "Content of a nested container with a non-excluded type must be extracted.");
    }

    /**
     * Scenario A: excluding a container (nested or not), without further nested containers below it,
     *  by its name skips only that container.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testExcludeNoCascadeByName() throws Exception {

        // We exclude aside and nested
        String content = extractPageContent(SUBSITEMAP_PATH + "by-name/" + INDEX_HTML);
        assertFalse(
            content.contains(MARKER_ASIDE),
            "Content of the excluded top-level aside container must be skipped.");
        assertTrue(content.contains(MARKER_MAIN), "Content of a non-excluded container main must still be extracted.");
        assertFalse(content.contains(MARKER_NESTED), "Content of a excluded nested container nested must be skipped.");
    }

    /**
     * Scenario B: excluding the top-level "main" container by name must also exclude the
     * container nested below it, while leaving unrelated containers untouched.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testExcludeParentCascadesToNested() throws Exception {

        String content = extractPageContent(SUBSITEMAP_PATH + "by-name-transitiv/" + INDEX_HTML);
        assertFalse(content.contains(MARKER_MAIN), "Content of the excluded top-level container must be skipped.");
        assertFalse(
            content.contains(MARKER_NESTED),
            "Content of a container nested below an excluded container must be skipped.");
        assertTrue(content.contains(MARKER_ASIDE), "Content of an unrelated container must still be extracted.");
    }

    /**
     * Baseline: without any exclusion attributes, the content of all containers is extracted.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testNoExclusion() throws Exception {

        String content = extractPageContent(SUBSITEMAP_PATH + INDEX_HTML);
        assertTrue(content.contains(MARKER_MAIN), "Content of the top-level main container should be extracted.");
        assertTrue(content.contains(MARKER_NESTED), "Content of the nested container should be extracted.");
        assertTrue(content.contains(MARKER_ASIDE), "Content of the top-level aside container should be extracted.");
    }

    /**
     * Extracts the English page content via {@link CmsSolrDocumentContainerPage}.<p>
     *
     * @param pagePath site path to the container page to extract
     * @return the combined extracted content for the English locale
     *
     * @throws Exception if something goes wrong
     */
    private String extractPageContent(String pagePath) throws Exception {

        CmsObject cms = getCmsObject();
        // We only need the project from the index, so we fake it here.
        CmsSolrIndex index = new CmsSolrIndex();
        index.setProject(CmsProject.ONLINE_PROJECT_NAME);
        CmsResource page = cms.readResource(pagePath);
        CmsSolrDocumentContainerPage factory = new CmsSolrDocumentContainerPage(
            CmsSolrDocumentContainerPage.TYPE_CONTAINERPAGE_SOLR);
        I_CmsExtractionResult result = factory.extractContent(cms, page, index, Locale.ENGLISH);
        String content = result.getContent(Locale.ENGLISH);
        return content == null ? "" : content;
    }

}
