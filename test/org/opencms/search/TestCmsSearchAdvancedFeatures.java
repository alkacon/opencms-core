/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/search/TestCmsSearchAdvancedFeatures.java,v $
 * Date   : $Date: 2005/06/23 10:47:32 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for advanced search features.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.7 $
 */
public class TestCmsSearchAdvancedFeatures extends OpenCmsTestCase {

    /** Name of the index used for testing. */
    public static final String INDEX_OFFLINE = "Offline project (VFS)";

    /** The index used for testing. */
    public static final String INDEX_ONLINE = "Online project (VFS)";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsSearchAdvancedFeatures(String arg0) {

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
        suite.setName(TestCmsSearchAdvancedFeatures.class.getName());

        suite.addTest(new TestCmsSearchAdvancedFeatures("testSortSearchResults"));
        suite.addTest(new TestCmsSearchAdvancedFeatures("testSearchCategories"));
        suite.addTest(new TestCmsSearchAdvancedFeatures("testMultipleSearchRoots"));
        suite.addTest(new TestCmsSearchAdvancedFeatures("testSearchRestriction"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests searching with multiple search roots.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testMultipleSearchRoots() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching with multiple search roots");

        CmsSearch searchBean = new CmsSearch();
        List searchResult;
        String query = "OpenCms";

        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setMatchesPerPage(1000);
        searchBean.setQuery(query);

        String[][] roots = new String[][] {
            new String[] {"/folder1/"},
            new String[] {"/folder2/"},
            new String[] {"/types/"},
            new String[] {"/folder2/", "/types/"},
            new String[] {"/folder1/", "/types/"},
            new String[] {"/folder1/", "/folder2/"},
            new String[] {"/folder1/", "/folder2/", "/types/"}};

        int[] expected = new int[] {7, 4, 1, 5, 8, 11, 12};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }
    }

    /**
     * Tests search category grouping.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchCategories() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching for categories");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;
        String query = "OpenCms";

        // apply search categories to some folders

        CmsProperty cat1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_1", null, true);
        CmsProperty cat2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_2", null, true);
        CmsProperty cat3 = new CmsProperty(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, "category_3", null, true);

        cms.lockResource("/folder1/");
        cms.writePropertyObject("/folder1/", cat1);
        cms.unlockResource("/folder1/");
        cms.lockResource("/folder2/");
        cms.writePropertyObject("/folder2/", cat2);
        cms.unlockResource("/folder2/");
        cms.lockResource("/types/");
        cms.writePropertyObject("/types/", cat3);
        cms.unlockResource("/types/");

        // update the search index used
        OpenCms.getSearchManager().updateIndex(INDEX_OFFLINE, new CmsShellReport());

        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setQuery(query);
        searchBean.setMatchesPerPage(1000);
        searchBean.setCalculateCategories(true);

        // first run is default sort order
        searchResult = searchBean.getSearchResult();
        Iterator i = searchResult.iterator();
        System.out.println("Result sorted by relevance:");
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
            System.out.println("  score: " + res.getScore());
        }

        Map categories = searchBean.getSearchResultCategories();
        // make sure categories where found
        assertNotNull(categories);
        // print the categories 
        System.out.println(CmsSearchCategoryCollector.formatCategoryMap(categories));
        // make sure the results are as expected
        assertTrue(categories.containsKey(cat1.getValue()));
        assertTrue(categories.containsKey(cat2.getValue()));
        assertTrue(categories.containsKey(cat3.getValue()));
        assertTrue(categories.containsKey(CmsSearchCategoryCollector.UNKNOWN_CATEGORY));
        // result must be all 3 categories plus 1 for "unknown"
        assertEquals(4, categories.size());
        // assert result count
        assertEquals(new Integer(7), categories.get(cat1.getValue()));
        assertEquals(new Integer(4), categories.get(cat2.getValue()));
        assertEquals(new Integer(1), categories.get(cat3.getValue()));
        assertEquals(new Integer(1), categories.get(CmsSearchCategoryCollector.UNKNOWN_CATEGORY));

        // count the category results
        searchBean.setCalculateCategories(false);

        String[][] cats = new String[][] {
            new String[] {cat1.getValue()},
            new String[] {cat2.getValue()},
            new String[] {cat3.getValue()},
            new String[] {cat1.getValue(), cat3.getValue()},
            new String[] {cat2.getValue(), cat3.getValue()},
            new String[] {cat1.getValue(), cat2.getValue()},
            new String[] {cat1.getValue(), cat2.getValue(), cat3.getValue()}};

        int[] expected = new int[] {7, 4, 1, 8, 5, 11, 12};

        for (int k = 0; k < expected.length; k++) {
            int expect = expected[k];
            String[] catList = cats[k];
            searchBean.setCategories(catList);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + k
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }
    }

    /**
     * Tests searching with restrictions.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSearchRestriction() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing searching in search results");

        CmsSearch searchBean = new CmsSearch();
        List searchResult;
        String query = "OpenCms";

        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setMatchesPerPage(1000);
        searchBean.setQuery(query);

        // first part of the rest is identical to "testMultipleSearchRoots()"
        String[][] roots = new String[][] {
            new String[] {"/folder1/"},
            new String[] {"/folder2/"},
            new String[] {"/types/"},
            new String[] {"/folder2/", "/types/"},
            new String[] {"/folder1/", "/types/"},
            new String[] {"/folder1/", "/folder2/"},
            new String[] {"/folder1/", "/folder2/", "/types/"}};

        int[] expected = new int[] {7, 4, 1, 5, 8, 11, 12};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }

        // now create a restriction to search for an additional "Alkacon" (effectivly searching for "OpenCms Alkacon")
        CmsSearchParameters restriction;
        restriction = new CmsSearchParameters("Alkacon", null, null, null, false, null);

        expected = new int[] {3, 2, 1, 3, 4, 5, 6};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }

        // another run of tests using searching only in the "meta" field
        restriction = new CmsSearchParameters("Alkacon", Arrays.asList(new String[] {"meta"}), null, null, false, null);

        expected = new int[] {0, 0, 1, 1, 1, 0, 1};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }

        // another run of tests using categories that have been defined in "testSearchCategories()"
        restriction = new CmsSearchParameters(
            null,
            null,
            null,
            Arrays.asList(new String[] {"category_1", "category_3"}),
            false,
            null);

        expected = new int[] {7, 0, 1, 1, 8, 7, 8};

        for (int i = 0; i < expected.length; i++) {
            int expect = expected[i];
            String[] rootList = roots[i];
            searchBean.setSearchRoots(rootList);
            searchBean.setResultRestriction(restriction);
            searchResult = searchBean.getSearchResult();
            Iterator j = searchResult.iterator();
            System.out.println("Result for search "
                + i
                + " (found "
                + searchResult.size()
                + ", expected "
                + expect
                + ")");
            while (j.hasNext()) {
                CmsSearchResult res = (CmsSearchResult)j.next();
                System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
                System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
                System.out.println("  score: " + res.getScore());
            }
            assertEquals(expect, searchResult.size());
        }
    }

    /**
     * Tests sorting of search results.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testSortSearchResults() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing sorting of search results");

        // perform a search on the newly generated index
        CmsSearch searchBean = new CmsSearch();
        List searchResult;
        String query = "OpenCms";

        // update the search index used
        OpenCms.getSearchManager().updateIndex(INDEX_OFFLINE, new CmsShellReport());

        searchBean.init(cms);
        searchBean.setIndex(INDEX_OFFLINE);
        searchBean.setQuery(query);

        // first run is default sort order
        searchResult = searchBean.getSearchResult();
        Iterator i = searchResult.iterator();
        System.out.println("Result sorted by relevance:");
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));
            System.out.println("  score: " + res.getScore());
        }

        // second run use Title sort order
        String lastTitle = null;
        searchBean.setSortOrder(CmsSearch.SORT_TITLE);
        searchResult = searchBean.getSearchResult();
        i = searchResult.iterator();
        System.out.println("Result sorted by title:");
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));
            System.out.println("  score: " + res.getScore());
            if (lastTitle != null) {
                // make sure result is sorted correctly
                assertTrue(lastTitle.compareTo(res.getTitle()) <= 0);
            }
            lastTitle = res.getTitle();
        }

        // third run use date last modified
        long lastTime = 0;
        searchBean.setSortOrder(CmsSearch.SORT_DATE_LASTMODIFIED);
        searchResult = searchBean.getSearchResult();
        i = searchResult.iterator();
        System.out.println("Result sorted by date last modified:");
        while (i.hasNext()) {
            CmsSearchResult res = (CmsSearchResult)i.next();
            System.out.print(CmsStringUtil.padRight(cms.getRequestContext().removeSiteRoot(res.getPath()), 50));
            System.out.print(CmsStringUtil.padRight(res.getTitle(), 40));
            System.out.print(CmsDateUtil.getHeaderDate(res.getDateLastModified().getTime()));
            System.out.println("  score: " + res.getScore());
            if (lastTime > 0) {
                // make sure result is sorted correctly
                assertTrue(lastTime >= res.getDateLastModified().getTime());
                assertTrue(res.getScore() <= 100);
            }
            lastTime = res.getDateLastModified().getTime();
        }

        assertNull(searchBean.getSearchResultCategories());
    }

}