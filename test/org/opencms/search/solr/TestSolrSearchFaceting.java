/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the Solr faceting capabilities.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrSearchFaceting extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrSearchFaceting(String arg0) {

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
        suite.setName(TestSolrSearchFaceting.class.getName());
        suite.addTest(new TestSolrSearchFaceting("testFacetQueryCount"));

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
     * This test demonstrates how to get the count of documents 
     * that have a specific word (e.g. OpenCms) in a certain field.<p>
     * 
     * This will allow you to create 'facet-like' UI-Components that
     * show the count of documents inside the search result containing
     * a specific word.<p>
     * 
     * E.g. The word "OpenCms" was found '7' times in the field "text" and '5' times in the "title"
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFacetQueryCount() throws Throwable {

        echo("Testing facet query count");

        // creating the query: facet=true&facet.field=Title_exact&facet.mincount=1&facet.query=text:OpenCms&rows=0
        CmsSolrQuery query = new CmsSolrQuery(getCmsObject());
        // facet=true
        query.setFacet(true);
        // facet.field=Title_exact
        query.addFacetField("Title_exact");
        // facet.mincount=1
        query.add("facet.mincount", "1");
        // facet.query=text:OpenCms
        query.addFacetQuery("text:OpenCms");
        // facet.query=Title_prop:OpenCms
        query.addFacetQuery("Title_prop:OpenCms");
        // rows=0
        query.setRows(new Integer(0));

        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllSolrTests.SOLR_ONLINE);
        CmsSolrResultList results = index.search(getCmsObject(), query);
        long facetTextCount = results.getFacetQuery().get("text:OpenCms").intValue();
        long facetTitleCount = results.getFacetQuery().get("Title_prop:OpenCms").intValue();
        echo("Found '"
            + results.getFacetField("Title_exact").getValueCount()
            + "' facets for the field \"Title_exact\" and '"
            + facetTextCount
            + "' of them containing the word: \"OpenCms\" in the field 'text' and '"
            + facetTitleCount
            + "' of them containing the word \"OpenCms\" in the field 'Title_prop!'");

        query = new CmsSolrQuery(getCmsObject(), "text:OpenCms");
        results = index.search(getCmsObject(), query);
        long numExpected = results.getNumFound();

        assertEquals(numExpected, facetTextCount);
        echo("Great Solr works fine!");
    }
}
