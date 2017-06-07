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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.search.CmsSearchResource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Main test suite for the package <code>{@link org.opencms.search.solr}</code>.<p>
 *
 * @since 8.5.0
 */
public final class AllTests {

    /** Name of a search index created using API. */
    public static final String INDEX_TEST = "Test new index";

    /** Name of an index used for testing. */
    public static final String SOLR_OFFLINE = "Solr Offline";

    /** Name of an index used for testing. */
    public static final String SOLR_ONLINE = "Solr Online";

    /**
     * Hide constructor to prevent generation of class instances.<p>
     */
    private AllTests() {

        // empty
    }

    /**
     * Returns the search resource from the result by the given path.<p>
     *
     * @param results the results
     * @param path signals if to print the counts only
     *
     * @return the search resource or null
     */
    public static CmsSearchResource getByPath(CmsSolrResultList results, String path) {

        for (CmsSearchResource r : results) {
            if (r.getRootPath().equals(path)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Prints the results.<p>
     *
     * @param cms the current cms
     * @param results the results
     * @param countOnly signals if to print the counts only
     */
    public static void printResults(CmsObject cms, CmsSolrResultList results, boolean countOnly) {

        if (countOnly) {
            printResultCount(results);
        } else {
            printResultCount(results);
            printResults(results, cms);
        }
    }

    /**
     * Returns the JUnit test suite for this package.<p>
     *
     * @return the JUnit test suite for this package
     */
    public static Test suite() {

        TestSuite suite = new TestSuite("Tests for package " + AllTests.class.getPackage().getName());
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        //$JUnit-BEGIN$
        suite.addTest(TestSolrConfiguration.suite());
        suite.addTest(TestSolrFieldConfiguration.suite());
        suite.addTest(TestSolrSearch.suite());
        suite.addTest(TestCmsSolrCollector.suite());
        //$JUnit-END$
        return suite;
    }

    /**
     * Prints a Solr query response.<p>
     *
     * @param results the query result
     */
    private static void printResultCount(CmsSolrResultList results) {

        System.out.println("#################################");
        System.out.println("Found: " + results.getNumFound());
        System.out.println("Start: " + results.getStart());
        System.out.println("Max Score: " + results.getMaxScore());
        System.out.println("Query: " + CmsEncoder.decode(results.getQuery().toString()));
        System.out.println("#################################");
    }

    /**
     * Prints a Solr query response.<p>
     *
     * @param qr the query response
     */
    @SuppressWarnings("unused")
    private static void printResultDetails(QueryResponse qr) {

        SolrDocumentList sdl = qr.getResults();
        qr.getExplainMap();

        // System.out.println(sdl.toString());

        ArrayList<HashMap<String, Object>> hitsOnPage = new ArrayList<HashMap<String, Object>>();
        for (SolrDocument d : sdl) {
            HashMap<String, Object> values = new HashMap<String, Object>();
            Iterator<Map.Entry<String, Object>> i = d.iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> e2 = i.next();
                values.put(e2.getKey(), e2.getValue());
            }

            hitsOnPage.add(values);
            System.out.println(values.get("path") + " (" + values.get("Title") + ")");
        }
        List<FacetField> facets = qr.getFacetFields();

        if (facets != null) {
            for (FacetField facet : facets) {
                List<FacetField.Count> facetEntries = facet.getValues();

                if (facetEntries != null) {
                    for (FacetField.Count fcount : facetEntries) {
                        System.out.println(fcount.getName() + ": " + fcount.getCount());
                    }
                }
            }
        }
    }

    /**
     * Prints the result.<p>
     *
     * @param results the results to print
     * @param cms the cms object
     */
    private static void printResults(CmsSolrResultList results, CmsObject cms) {

        Iterator<CmsSearchResource> i = results.iterator();
        int count = 0;
        int colPath = 0;
        int colTitle = 0;
        while (i.hasNext()) {
            CmsSearchResource res = i.next();
            String path = res.getRootPath();
            colPath = Math.max(colPath, path.length() + 3);
            String title = res.getField(
                CmsSearchField.FIELD_TITLE + "_" + cms.getRequestContext().getLocale().toString());
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            colTitle = Math.max(colTitle, title.length() + 3);
        }

        for (CmsSearchResource res : results) {
            System.out.print(CmsStringUtil.padRight("" + ++count, 4));
            System.out.print(CmsStringUtil.padRight(res.getRootPath(), colPath));
            String title = res.getField(
                CmsSearchField.FIELD_TITLE + "_" + cms.getRequestContext().getLocale().toString());
            if (title == null) {
                title = "";
            } else {
                title = title.trim();
            }
            System.out.print(CmsStringUtil.padRight(title, colTitle));
            String type = res.getField(CmsSearchField.FIELD_TYPE);
            if (type == null) {
                type = "";
            }
            System.out.print(CmsStringUtil.padRight(type, 10));
            System.out.print(
                CmsStringUtil.padRight(
                    "" + CmsDateUtil.getDateTime(new Date(res.getDateLastModified()), DateFormat.SHORT, Locale.GERMAN),
                    17));

            System.out.println("score: " + res.getScore(results.getMaxScore().floatValue()));
        }
    }
}