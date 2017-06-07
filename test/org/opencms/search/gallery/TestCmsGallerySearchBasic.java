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

package org.opencms.search.gallery;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource.CmsResourceUndoMode;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.search.galleries.CmsGallerySearchParameters.CmsGallerySortParam;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.search.galleries.CmsGallerySearchResultList;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the basic OpenCms gallery search functions.<p>
 */
public class TestCmsGallerySearchBasic extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsGallerySearchBasic(String arg0) {

        super(arg0);
    }

    /**
     * Prints the given list of search results to STDOUT.<p>
     *
     * @param searchResult the list to print
     * @param cms the current OpenCms user context
     */
    public static void printResults(CmsGallerySearchResultList searchResult, CmsObject cms) {

        printResults(searchResult, cms, false);
    }

    /**
     * Prints the given list of search results to STDOUT.<p>
     *
     * @param searchResult the list to print
     * @param cms the current OpenCms user context
     * @param showExcerpt if <code>true</code>, the generated excerpt is also displayed
     */
    public static void printResults(CmsGallerySearchResultList searchResult, CmsObject cms, boolean showExcerpt) {

        Iterator<CmsGallerySearchResult> i = searchResult.iterator();
        int count = 0;
        int colPath = 0;
        int colTitle = 0;
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            String path = cms.getRequestContext().removeSiteRoot(res.getPath());
            colPath = Math.max(colPath, path.length() + 3);
            String title = res.getTitle();
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            colTitle = Math.max(colTitle, title.length() + 3);
        }
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            count++;
            System.out.print(CmsStringUtil.padRight("" + count, 4));
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), colPath));
            String title = res.getTitle();
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            System.out.print(CmsStringUtil.padRight(title, colTitle));
            String type = res.getResourceType();
            if (type == null) {
                type = "";
            }
            System.out.print(CmsStringUtil.padRight(type, 10));
            if (res.getDateLastModified() != null) {
                System.out.print(
                    CmsStringUtil.padRight(
                        "" + CmsDateUtil.getDateTime(res.getDateLastModified(), DateFormat.SHORT, Locale.GERMAN),
                        17));
            }
            System.out.println("score: " + res.getScore());
            if (showExcerpt) {
                System.out.println(res.getExcerpt());
            }
        }
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsGallerySearchBasic.class.getName());

        suite.addTest(new TestCmsGallerySearchBasic("testGallerySearchIndexCreation"));
        suite.addTest(new TestCmsGallerySearchBasic("testGallerySortSearchResults"));
        suite.addTest(new TestCmsGallerySearchBasic("testSearchById"));
        suite.addTest(new TestCmsGallerySearchBasic("testSearchForMovedFiles"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "", "/../org/opencms/search/gallery");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Creates the configured search indexes for all other test cases in this class.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testGallerySearchIndexCreation() throws Exception {

        echo("Testing dynamic creation of special index for galleries");

        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // rebuild all indexes
        OpenCms.getSearchManager().rebuildAllIndexes(report);

        // make sure the ADE index actually exists
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        assertNotNull("Index for galleries not initialized", index);
        assertEquals("Index for galleries not of required class", CmsSolrIndex.class, index.getClass());
    }

    /**
     * Tests sorting of search results.<p>
     *
     * @throws Exception if the test fails
     */
    public void testGallerySortSearchResults() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing sorting of search results for galleries");

        // perform a search on the newly generated index
        CmsGallerySearch searchBean = new CmsGallerySearch();
        CmsGallerySearchParameters searchParams = new CmsGallerySearchParameters();
        CmsGallerySearchResultList searchResult;
        String query = "OpenCms";

        searchBean.init(cms);
        searchBean.setIndex(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        searchParams.setSearchWords(query);
        searchParams.setMatchesPerPage(50);

        // first run is default sort order
        searchParams.setSortOrder(CmsGallerySortParam.score);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by score:");
        printResults(searchResult, cms);

        // second run use Title sort order
        String lastTitle = null;
        searchParams.setSortOrder(CmsGallerySortParam.title_asc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by title ascending:");
        printResults(searchResult, cms);

        Iterator<CmsGallerySearchResult> i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getTitle()) <= 0);
            }
            lastTitle = res.getTitle();
        }

        searchParams.setSortOrder(CmsGallerySortParam.title_desc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by title descending:");
        printResults(searchResult, cms);

        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getTitle()) >= 0);
            }
            lastTitle = res.getTitle();
        }

        // third run use date last modified
        long lastTime = 0;
        searchParams.setSortOrder(CmsGallerySortParam.dateLastModified_desc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by date last modified descending:");
        printResults(searchResult, cms);

        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime >= res.getDateLastModified().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateLastModified().getTime();
        }

        searchParams.setSortOrder(CmsGallerySortParam.dateLastModified_asc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by date last modified ascending:");
        printResults(searchResult, cms);

        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime <= res.getDateLastModified().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateLastModified().getTime();
        }

        // forth run date created
        searchParams.setSortOrder(CmsGallerySortParam.dateCreated_desc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by date created descending:");
        printResults(searchResult, cms);

        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime >= res.getDateCreated().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateCreated().getTime();
        }

        searchParams.setSortOrder(CmsGallerySortParam.dateCreated_asc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by date created ascending:");
        printResults(searchResult, cms);

        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime <= res.getDateCreated().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateCreated().getTime();
        }

        // content length (size)
        searchParams.setSortOrder(CmsGallerySortParam.length_asc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by content length (size) ascending:");
        printResults(searchResult, cms);

        int lastLength = -1;
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastLength > 0) {
                // make sure result is sorted correctly
                assertTrue(lastLength <= res.getLength());
                assertTrue(res.getScore() <= 100);
            }
            lastLength = res.getLength();
        }

        searchParams.setSortOrder(CmsGallerySortParam.length_desc);
        searchResult = searchBean.getResult(searchParams);
        System.out.println("Result sorted by content length (size) descending:");
        printResults(searchResult, cms);

        lastLength = -1;
        i = searchResult.iterator();
        while (i.hasNext()) {
            CmsGallerySearchResult res = i.next();
            if (lastLength > 0) {
                // make sure result is sorted correctly
                assertTrue(lastLength >= res.getLength());
                assertTrue(res.getScore() <= 100);
            }
            lastLength = res.getLength();
        }
    }

    /**
     * Tests searching documents by their structure ID.<p>
     *
     * @throws Exception
     */
    public void testSearchById() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing search by id");
        CmsGallerySearch search = new CmsGallerySearch();
        search.init(cms);
        search.setIndex(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        CmsGallerySearchResult result = search.searchById(
            new CmsUUID("7d6c22cd-4e3a-11db-9016-5bf59c6009b3"),
            new Locale("en"));
        assertTrue(result.getPath().endsWith("/index.html"));
    }

    /**
     * Test that search results don't get "duplicated" after moving a resource.
     *
     * @throws Exception
     */
    public void testSearchForMovedFiles() throws Exception {

        CmsObject cms = getCmsObject();
        // rebuild all indexes
        cms.createResource(
            "/foo1.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "foo1".getBytes(),
            Collections.singletonList(new CmsProperty("Title", "foo1", "foo1")));
        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        // rebuild all indexes
        OpenCms.getSearchManager().rebuildAllIndexes(report);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.lockResource("/foo1.txt");
        cms.moveResource("/foo1.txt", "/foo2.txt");
        OpenCms.getSearchManager().updateOfflineIndexes();
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        CmsSolrQuery query = new CmsSolrQuery();
        query.setQuery("foo1");
        CmsSolrResultList results = index.search(cms, query);
        assertEquals(1, results.size());
        System.out.println("######################");
        System.out.println(results);
        System.out.println("######################");
        assertTrue(results.get(0).getField(CmsSearchField.FIELD_PATH).contains("foo2"));
        cms.undoChanges("/foo2.txt", CmsResourceUndoMode.MODE_UNDO_MOVE_CONTENT);
        OpenCms.getSearchManager().updateOfflineIndexes(5000);
        results = index.search(cms, query);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getField(CmsSearchField.FIELD_PATH).contains("foo1"));
    }
}
