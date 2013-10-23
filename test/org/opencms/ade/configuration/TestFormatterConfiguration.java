/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.configuration.formatters.CmsFormatterBeanParser;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import com.google.common.collect.Lists;

/**
 * Tests for formatter configurations.<p>
 */
public class TestFormatterConfiguration extends OpenCmsTestCase {

    /**
     * Test constructor.<p>
     * 
     * @param name the name of the test 
     */
    public TestFormatterConfiguration(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     * 
     * @return the test suite 
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestFormatterConfiguration.class, "simpletest", "/");
    }

    /**
     * Basic test for parsing a formatter configuration.<p>
     *  
     * @throws Exception
     */
    public void testParseConfiguration() throws Exception {

        CmsFormatterBeanParser parser = new CmsFormatterBeanParser(getCmsObject());
        CmsObject cms = getCmsObject();
        CmsResource fooJs = cms.createResource("/foo.js", 1);
        CmsResource barJs = cms.createResource("/bar.js", 1);
        CmsResource fooCss = cms.createResource("/foo.css", 1);
        CmsResource barCss = cms.createResource("/bar.css", 1);
        CmsResource formatter = cms.createResource("/xyz.jsp", 1);

        String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<NewFormatters xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"opencms://system/modules/org.opencms.ade.config/schemas/formatters/new_formatter.xsd\">\n"
            + "  <NewFormatter language=\"en\">\n"
            + "    <Type><![CDATA[jsp]]></Type>\n"
            + "    <Jsp>\n"
            + link(formatter)
            + "    </Jsp>\n"
            + "    <NiceName><![CDATA[1234]]></NiceName>\n"
            + "    <Rank><![CDATA[1001]]></Rank>\n"
            + "    <Match>\n"
            + "      <Types>\n"
            + "        <ContainerType><![CDATA[foo]]></ContainerType>\n"
            + "        <ContainerType><![CDATA[bar]]></ContainerType>\n"
            + "        <ContainerType><![CDATA[baz]]></ContainerType>\n"
            + "      </Types>\n"
            + "    </Match>\n"
            + "    <Preview>true</Preview>\n"
            + "    <SearchContent>true</SearchContent>\n"
            + "    <Setting>\n"
            + "      <PropertyName><![CDATA[13245]]></PropertyName>\n"
            + "      <Disabled>false</Disabled>\n"
            + "      <Widget/>\n"
            + "    </Setting>\n"
            + "    <HeadIncludeCss>\n"
            + "      <CssInline><![CDATA[123]]></CssInline>\n"
            + "      <CssInline><![CDATA[456]]></CssInline>\n"
            + "      <CssLink>\n"
            + link(fooCss)
            + "      </CssLink>\n"
            + "      <CssLink>\n"
            + link(barCss)
            + "      </CssLink>\n"
            + "    </HeadIncludeCss>\n"
            + "    <HeadIncludeJs>\n"
            + "      <JavascriptInline><![CDATA[234]]></JavascriptInline>\n"
            + "      <JavascriptInline><![CDATA[567]]></JavascriptInline>\n"
            + "      <JavascriptLink>\n"
            + link(fooJs)
            + "      </JavascriptLink>\n"
            + "      <JavascriptLink>\n"
            + link(barJs)
            + "      </JavascriptLink>\n"
            + "    </HeadIncludeJs>\n"
            + "  </NewFormatter>\n"
            + "</NewFormatters>\n"
            + "";
        byte[] configData = config.getBytes("UTF-8");
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(getCmsObject());
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCmsObject(), configData, "UTF-8", resolver);
        String dummyId = "dummyId";
        I_CmsFormatterBean formatterBean = parser.parse(content, "dummy location", dummyId);

        Set<String> expectedContainerTypes = new HashSet<String>(CmsStringUtil.splitAsList("foo,bar,baz", ","));
        assertEquals(expectedContainerTypes, formatterBean.getContainerTypes());
        assertEquals("rank not set", 1001, formatterBean.getRank());
        assertEquals(dummyId, formatterBean.getId());
        assertEquals("resource type isn't 'jsp'", "jsp", formatterBean.getResourceTypeName());
        assertEquals(formatter.getStructureId(), formatterBean.getJspStructureId());
        assertEquals("123456", formatterBean.getInlineCss());
        assertEquals("234567", formatterBean.getInlineJavascript());

        List<String> jsIncludes = formatterBean.getJavascriptHeadIncludes();
        assertEquals(Lists.newArrayList(Arrays.asList("/sites/default/foo.js", "/sites/default/bar.js")), jsIncludes);
        assertEquals(
            Lists.newArrayList(Arrays.asList("/sites/default/foo.css", "/sites/default/bar.css")),
            Lists.newArrayList(formatterBean.getCssHeadIncludes()));
        assertEquals("preview should be true", true, formatterBean.isPreviewFormatter());
        assertEquals("extract content should be true", true, formatterBean.isSearchContent());
        assertTrue("AutoEnabled should not be set", !formatterBean.isAutoEnabled());

        assertTrue("setting 13245 not found", formatterBean.getSettings().containsKey("13245"));
    }

    /** 
     * Creates the XML for a weak link to a resource.<p>
     * 
     * @param res the resource  
     * @return the XML for the link 
     */
    private String link(CmsResource res) {

        return "<link type=\"WEAK\">\n"
            + "<target><![CDATA["
            + res.getRootPath()
            + "]]></target>\n"
            + "<uuid>"
            + res.getStructureId()
            + "</uuid>\n"
            + "</link>\n";
    }

}
