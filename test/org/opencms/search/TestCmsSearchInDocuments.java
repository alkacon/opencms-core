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

package org.opencms.search;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for searching in extracted document text.<p>
 *
 */
public class TestCmsSearchInDocuments extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";

    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchInDocuments(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsSearchInDocuments.class.getName());

        suite.addTest(new TestCmsSearchInDocuments("testSearchIndexGeneration"));
        suite.addTest(new TestCmsSearchInDocuments("testSearchInDocuments"));
        suite.addTest(new TestCmsSearchInDocuments("testExceptGeneration"));
        suite.addTest(new TestCmsSearchInDocuments("testExceptHighlighting"));
        suite.addTest(new TestCmsSearchInDocuments("testExceptEscaping"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the excerpt escaping.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExceptEscaping() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt escaping");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        // count depend on the number of documents indexed
        int expected = 7;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/search/");

        searchBean.setQuery("alkacon");
        searchResult = searchBean.getSearchResult();

        System.out.println("\n\n----- Searching for '" + searchBean.getQuery() + "'");
        TestCmsSearch.printResults(searchResult, cms, true);
        assertEquals(expected, searchResult.size());

        // check if "sites", "default" and "alkacon" is contained in the excerpt
        Iterator<CmsSearchResult> i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult result = i.next();
            if (result.getExcerpt() == null) {
                continue;
            }
            String excerpt = result.getExcerpt().toLowerCase();
            excerpt = CmsStringUtil.substitute(excerpt, "<b>alkacon</b>", "alkacon");
            assertFalse(excerpt.toLowerCase().indexOf("<") > -1);
            assertFalse(excerpt.toLowerCase().indexOf(">") > -1);
        }
    }

    /**
     * Tests the excerpt generation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExceptGeneration() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt generation");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        // count depend on the number of documents indexed
        int expected = 6;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/search/");

        searchBean.setQuery("The OpenCms experts");
        searchResult = searchBean.getSearchResult();

        System.out.println("\n\n----- Searching for '" + searchBean.getQuery() + "'");
        TestCmsSearch.printResults(searchResult, cms, true);
        assertEquals(expected, searchResult.size());

        // check if "the" and "a" is contained in the excerpt
        // it may have been removed as term in the search, but it should be in the excerpt result anyway
        boolean foundThe = false;
        boolean foundA = false;
        Iterator<CmsSearchResult> i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult result = i.next();
            String excerpt = result.getExcerpt().toLowerCase();
            if (excerpt.indexOf(" the ") > -1) {
                foundThe = true;
            }
            if (excerpt.indexOf(" a ") > -1) {
                foundA = true;
            }
        }
        assertTrue(foundThe);
        assertTrue(foundA);

        searchBean.setQuery("Some content on the third sheet.");
        searchResult = searchBean.getSearchResult();

        System.out.println("\n\n----- Searching for '" + searchBean.getQuery() + "'");
        TestCmsSearch.printResults(searchResult, cms, true);
        assertEquals(expected, searchResult.size());

        // check if "the", "on" and "a" is contained in the excerpt
        foundThe = false;
        foundA = false;
        boolean foundOn = false;
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult result = i.next();
            String excerpt = result.getExcerpt().toLowerCase();
            if (excerpt.indexOf(" the ") > -1) {
                foundThe = true;
            }
            if (excerpt.indexOf(" a ") > -1) {
                foundA = true;
            }
            if (excerpt.indexOf(" on ") > -1) {
                foundOn = true;
            }
        }
        assertTrue(foundThe);
        assertTrue(foundOn);
        assertTrue(foundA);
    }

    /**
     * Tests the excerpt highlighting.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExceptHighlighting() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing excerpt highlighting");

        // create new text file
        String resname = "/search/highlightTest.txt";
        cms.createResource(resname, CmsResourceTypePlain.getStaticTypeId());
        CmsFile file = cms.readFile(resname);
        // that matches the search query as well as the path
        // add some html code for the testExceptEscaping method
        file.setContents("<b>sites<font> <html>alkacon<body> <i>default</h1>".getBytes());
        cms.writeFile(file);
        // publish it to update the search index
        OpenCms.getPublishManager().publishResource(cms, resname);
        OpenCms.getPublishManager().waitWhileRunning();

        // rebuild search index
        OpenCms.getSearchManager().rebuildIndex(INDEX_ONLINE, new CmsLogReport(Locale.ENGLISH, getClass()));

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        // count depend on the number of documents indexed
        int expected = 7;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/search/");
        searchBean.setExcerptOnlySearchedFields(false);

        searchBean.setQuery("alkacon");
        searchResult = searchBean.getSearchResult();

        System.out.println("\n\n----- Searching for '" + searchBean.getQuery() + "'");
        TestCmsSearch.printResults(searchResult, cms, true);
        assertEquals(expected, searchResult.size());

        // check if "sites", "default" and "alkacon" is contained in the excerpt
        Iterator<CmsSearchResult> i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult result = i.next();
            if (result.getExcerpt() == null) {
                continue;
            }
            String excerpt = result.getExcerpt().toLowerCase();
            assertTrue(excerpt.toLowerCase().indexOf("<b>alkacon</b>") > -1);
            assertFalse(excerpt.toLowerCase().indexOf("<b>sites</b>") > -1);
            assertFalse(excerpt.toLowerCase().indexOf("<b>default</b>") > -1);
        }

        // check again with the ExcerptOnlySearchedFields set
        searchBean.setExcerptOnlySearchedFields(true);
        searchResult = searchBean.getSearchResult();

        System.out.println("\n\n----- Searching for '" + searchBean.getQuery() + "'");
        TestCmsSearch.printResults(searchResult, cms, true);
        assertEquals(expected, searchResult.size());

        // check if "sites", "default" and "alkacon" is contained in the excerpt
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsSearchResult result = i.next();
            if (result.getExcerpt() == null) {
                continue;
            }
            String excerpt = result.getExcerpt().toLowerCase();
            assertTrue(excerpt.toLowerCase().indexOf("<b>alkacon</b>") > -1);
            assertFalse(excerpt.toLowerCase().indexOf("<b>sites</b>") > -1);
            assertFalse(excerpt.toLowerCase().indexOf("<b>default</b>") > -1);
        }
    }

    /**
     * Imports the documents for the test cases in the VFS an generates the index.<p>
     *
     * Please note: This method need to be called first in this test suite, the
     * other methods depend on the index generated here.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchIndexGeneration() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search index generation with different resource types");

        // create test folder
        cms.createResource("/search/", CmsResourceTypeFolder.RESOURCE_TYPE_ID, null, null);
        cms.unlockResource("/search/");

        // import the sample documents to the VFS
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.pdf",
            "/search/test1.pdf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.doc",
            "/search/test1.doc",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.rtf",
            "/search/test1.rtf",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.xls",
            "/search/test1.xls",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.ppt",
            "/search/test1.ppt",
            CmsResourceTypeBinary.getStaticTypeId(),
            Collections.<CmsProperty> emptyList());

        // HTML page is encoded using UTF-8
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(
            new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, CmsEncoder.ENCODING_UTF_8, null, true));
        importTestResource(
            cms,
            "org/opencms/search/extractors/test1.html",
            "/search/test1.html",
            CmsResourceTypePlain.getStaticTypeId(),
            properties);

        assertTrue(cms.existsResource("/search/test1.pdf"));
        assertTrue(cms.existsResource("/search/test1.html"));
        assertTrue(cms.existsResource("/search/test1.doc"));
        assertTrue(cms.existsResource("/search/test1.rtf"));
        assertTrue(cms.existsResource("/search/test1.xls"));
        assertTrue(cms.existsResource("/search/test1.ppt"));

        // publish the project
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // update the search indexes
        I_CmsReport report = new CmsShellReport(cms.getRequestContext().getLocale());
        // this call does not throws the rebuild index event
        OpenCms.getSearchManager().rebuildAllIndexes(report);

        // check the online project
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));

        assertTrue(cms.existsResource("/search/test1.pdf"));
        assertTrue(cms.existsResource("/search/test1.html"));
        assertTrue(cms.existsResource("/search/test1.doc"));
        assertTrue(cms.existsResource("/search/test1.rtf"));
        assertTrue(cms.existsResource("/search/test1.xls"));
        assertTrue(cms.existsResource("/search/test1.ppt"));
    }

    /**
     * Tests searching in the VFS for specific Strings that are placed in
     * various document formats.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSearchInDocuments() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching in different (complex) document types");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List<CmsSearchResult> searchResult;

        // count depend on the number of documents indexed
        int expected = 6;

        searchBean.init(cms);
        searchBean.setIndex(INDEX_ONLINE);
        searchBean.setSearchRoot("/search/");

        searchBean.setQuery("Alkacon Software");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("The OpenCms experts");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("Some content here.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("Some content there.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("Some content on a second sheet.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        searchBean.setQuery("Some content on the third sheet.");
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());

        String specialQuery = C_AUML_LOWER
            + C_OUML_LOWER
            + C_UUML_LOWER
            + C_AUML_UPPER
            + C_OUML_UPPER
            + C_UUML_UPPER
            + C_SHARP_S
            + C_EURO;
        searchBean.setQuery(specialQuery);
        searchResult = searchBean.getSearchResult();
        assertEquals(expected, searchResult.size());
    }
}
