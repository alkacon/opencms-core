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
import org.opencms.file.collectors.CmsSolrCollector;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.test.OpenCmsTestRunner;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the priority resource collectors.<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsSolrCollector extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "solrtest", "/", "/../org/opencms/search/solr");
        // disable all lucene indexes
        for (String indexName : OpenCms.getSearchManager().getIndexNames()) {
            if (!indexName.equalsIgnoreCase(CmsTestSolrHelper.SOLR_ONLINE)) {
                I_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
                if (index != null) {
                    index.setEnabled(false);
                }
            }
        }
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Order(2)
    @Test
    public void testByContext() throws Throwable {

        echo("Testing testByContext resource collector");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        I_CmsResourceCollector collector = new CmsSolrCollector();
        List<CmsResource> resources = collector.getResults(cms, "byContext", null);
        // assert that 10 files are returned
        assertEquals(10, resources.size());
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Order(3)
    @Test
    public void testByContextWithQuery() throws Throwable {

        echo("Testing testByContextWithQuery resource collector");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        I_CmsResourceCollector collector = new CmsSolrCollector();
        StringBuffer q = new StringBuffer(128);
        q.append("q=");
        q.append("+type:article");
        q.append("&rows=" + 3);
        q.append("&sort=" + CmsSearchField.FIELD_DATE_LASTMODIFIED + " desc");
        List<CmsResource> resources = collector.getResults(cms, "byContext", q.toString());

        // assert that 3 files are returned
        assertEquals(3, resources.size());

        CmsResource res;
        res = resources.get(0);
        assertEquals("/sites/default/xmlcontent/article_0004.html", res.getRootPath());
        res = resources.get(1);
        assertEquals("/sites/default/xmlcontent/article_0003.html", res.getRootPath());
        res = resources.get(2);
        assertEquals("/sites/default/xmlcontent/article_0002.html", res.getRootPath());
    }

    /**
     * Tests the "allInFolderPriorityDesc" resource collector.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Order(1)
    @Test
    public void testByQuery() throws Throwable {

        echo("Testing if Solr is able to do the same as: allInFolderPriorityDateDesc resource collector");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        I_CmsResourceCollector collector = new CmsSolrCollector();
        StringBuffer q = new StringBuffer(128);
        q.append("&fq=parent-folders:\"/sites/default/xmlcontent/\"");
        q.append("&fq=type:article");
        q.append("&rows=" + 3);
        q.append("&sort=" + CmsSearchField.FIELD_DATE_LASTMODIFIED + " desc");
        List<CmsResource> resources = collector.getResults(cms, "byQuery", q.toString());

        // assert that 3 files are returned
        assertEquals(3, resources.size());

        CmsResource res;
        res = resources.get(0);
        assertEquals("/sites/default/xmlcontent/article_0004.html", res.getRootPath());
        res = resources.get(1);
        assertEquals("/sites/default/xmlcontent/article_0003.html", res.getRootPath());
        res = resources.get(2);
        assertEquals("/sites/default/xmlcontent/article_0002.html", res.getRootPath());
    }
}
