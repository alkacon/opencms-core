/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/sitemap/Attic/TestCmsXmlSitemap.java,v $
 * Date   : $Date: 2010/02/03 13:52:27 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML container pages.<p>
 *
 * @author Michael Moossen
 *  
 * @version $Revision: 1.1 $
 */
public class TestCmsXmlSitemap extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlSitemap(String arg0) {

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
        suite.setName(TestCmsXmlSitemap.class.getName());

        suite.addTest(new TestCmsXmlSitemap("testUnmarshall"));
        suite.addTest(new TestCmsXmlSitemap("testLink"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("ade-setup", "/");
                importData("adetest", "/sites/default/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests unmarshalling a sitemap.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUnmarshall() throws Exception {

        CmsObject cms = getCmsObject();

        String sitemapName = "/sitemap/sitemap";
        CmsResource sitemapResource = cms.readResource(sitemapName);

        // assert xsd relations
        String xsdName = "/system/workplace/editors/sitemap/schemas/sitemap.xsd";
        CmsResource xsdResource = cms.readResource(xsdName);
        List<CmsRelation> relations = cms.getRelationsForResource(
            sitemapResource,
            CmsRelationFilter.ALL.filterType(CmsRelationType.XSD));
        assertEquals(1, relations.size());
        assertRelation(new CmsRelation(sitemapResource, xsdResource, CmsRelationType.XSD), relations.get(0));

        // assert entry point relations
        String entrypointName = "/";
        CmsResource entrypointResource = cms.readResource(entrypointName);
        relations = cms.getRelationsForResource(
            sitemapResource,
            CmsRelationFilter.ALL.filterType(CmsRelationType.ENTRY_POINT));
        assertEquals(1, relations.size());
        assertRelation(
            new CmsRelation(sitemapResource, entrypointResource, CmsRelationType.ENTRY_POINT),
            relations.get(0));

        // assert xml-strong relations
        String cntPageName = "/containerpage/index.html";
        CmsResource cntPageResource = cms.readResource(cntPageName);
        relations = cms.getRelationsForResource(
            sitemapResource,
            CmsRelationFilter.ALL.filterType(CmsRelationType.XML_STRONG));
        assertEquals(1, relations.size());
        assertRelation(new CmsRelation(sitemapResource, cntPageResource, CmsRelationType.XML_STRONG), relations.get(0));

        // summarize all relations
        relations = cms.getRelationsForResource(sitemapResource, CmsRelationFilter.ALL);
        assertEquals(3, relations.size());
    }

    /**
     * Tests creating a link to a sitemap entry.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testLink() throws Exception {

        CmsObject cms = getCmsObject();
        String contentName = "/containerpage/content.html";
        CmsFile contentFile = cms.readFile(contentName);
        CmsXmlContent contentXml = CmsXmlContentFactory.unmarshal(cms, contentFile);
        I_CmsXmlContentValue contentValue = contentXml.getValue("Text", Locale.ENGLISH);
        contentValue.setStringValue(cms, "<a href='/'>sitemap link</a>");
        contentFile.setContents(contentXml.marshal());
    }
}