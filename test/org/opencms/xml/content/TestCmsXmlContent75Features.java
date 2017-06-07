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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.jsp.util.CmsJspContentAccessValueWrapper;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.HashMap;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms XML contents with real VFS operations for features introduced in OpenCms 7.5.<p>
 *
 */
public class TestCmsXmlContent75Features extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlContent75Features(String arg0) {

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
        suite.setName(TestCmsXmlContent75Features.class.getName());

        suite.addTest(new TestCmsXmlContent75Features("testDirectXmlAccesss"));

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
     * Test accessing the XML through a value wrapper.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testDirectXmlAccesss() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the direct access of the XML through a value wrapper");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        cacheSchema(
            resolver,
            "http://www.opencms.org/test3.xsd",
            "org/opencms/xml/content/xmlcontent-definition-3.mod1.xsd");

        cacheSchema(
            resolver,
            "http://www.opencms.org/test4.xsd",
            "org/opencms/xml/content/xmlcontent-definition-4.xsd");

        String content;
        CmsXmlContent xmlcontent;

        // now read the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.mod1.xml", CmsEncoder.ENCODING_UTF_8);
        xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, xmlcontent);

        // access the content form the default locale
        CmsJspContentAccessValueWrapper cascade = bean.getValue().get("Cascade");

        CmsJspContentAccessValueWrapper link = cascade.getValue().get("VfsLink");
        assertEquals("/index.html", link.toString());
        System.out.println("\n\n-----------------------------");
        System.out.println("<target> : " + link.getXmlText().get("link/target"));
        System.out.println("<uuid>   : " + link.getXmlText().get("link/uuid"));
        System.out.println("-----------------------------\n");

        assertEquals("/sites/default/index.html", link.getXmlText().get("link/target"));
        assertEquals("7d6c22cd-4e3a-11db-9016-5bf59c6009b3", link.getXmlText().get("link/uuid"));
        assertEquals("/sites/default/index.html", link.getXmlText().get("link[1]/target"));
        assertEquals("7d6c22cd-4e3a-11db-9016-5bf59c6009b3", link.getXmlText().get("link[1]/uuid"));

        CmsJspContentAccessValueWrapper html = cascade.getValue().get("Html");
        assertEquals("a=b&c=d", html.getXmlText().get("links/link/query"));
        assertEquals("/sites/default/noexist/index.html", html.getXmlText().get("links/link[@name='link1']/target"));

        assertEquals("/sites/default/index.html", cascade.getXmlText().get("VfsLink[1]/link/target"));
        assertEquals("a=b&c=d", cascade.getXmlText().get("Html/links/link/query"));
    }

    /**
     * Updates the OpenCms XML entity resolver cache with a changed XML schema id.<p>
     *
     * @param resolver the OpenCms XML entity resolver to use
     * @param id the XML schema id to update in the resolver
     * @param filename the name of the file in the RFS where to read the new schema content from
     *
     * @throws Exception if something goes wrong
     */
    private void cacheSchema(CmsXmlEntityResolver resolver, String id, String filename) throws Exception {

        // fire "clear cache" event to clear up previously cached schemas
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>()));
        // read the XML from the given file and store it in the resolver
        String content = CmsFileUtil.readFile(filename, CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition definition = CmsXmlContentDefinition.unmarshal(content, id, resolver);
        System.out.println(definition.getSchema().asXML());
        CmsXmlEntityResolver.cacheSystemId(id, definition.getSchema().asXML().getBytes(CmsEncoder.ENCODING_UTF_8));
    }
}