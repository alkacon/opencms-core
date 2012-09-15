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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the Solr permission handling.<p>
 * 
 * @since 8.5.0
 */
public class TestSolrSearchPermissionHandling extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestSolrSearchPermissionHandling(String arg0) {

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
        suite.setName(TestSolrSearchPermissionHandling.class.getName());
        suite.addTest(new TestSolrSearchPermissionHandling("testPermissionHandling"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("solrtest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * @throws Throwable
     */
    public void testPermissionHandling() throws Throwable {

        echo("Testing search for permission check by comparing result counts");
        CmsSolrIndex index = OpenCms.getSearchManager().getIndexSolr(AllTests.SOLR_ONLINE);

        CmsSolrQuery squery = new CmsSolrQuery(getCmsObject(), null);
        squery.setSearchRoots("/sites/default/");
        squery.setRows(new Integer(100));
        CmsSolrResultList results = index.search(getCmsObject(), squery);
        AllTests.printResults(getCmsObject(), results, true);
        assertEquals(53, results.getNumFound());

        CmsObject cms = OpenCms.initCmsObject(getCmsObject(), new CmsContextInfo("test1"));
        results = index.search(cms, squery);
        AllTests.printResults(cms, results, false);
        assertEquals(47, results.getNumFound());

        cms = OpenCms.initCmsObject(getCmsObject(), new CmsContextInfo("test2"));
        results = index.search(cms, squery);
        AllTests.printResults(cms, results, true);
        assertEquals(49, results.getNumFound());
    }
}
