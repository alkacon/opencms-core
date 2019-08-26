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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the string template resolver.<p>
 */
public class TestCmsStringTemplateResolver extends OpenCmsTestCase {

    /** Schema id 3. */
    private static final String SCHEMA_SYSTEM_ID_3 = "http://www.opencms.org/test3.xsd";

    /** Schema id 4. */
    private static final String SCHEMA_SYSTEM_ID_4 = "http://www.opencms.org/test4.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsStringTemplateResolver(String arg0) {

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
        suite.setName(TestCmsStringTemplateResolver.class.getName());

        suite.addTest(new TestCmsStringTemplateResolver("testRenderContentValue"));
        suite.addTest(new TestCmsStringTemplateResolver("testContentAccessNestedSchema"));
        suite.addTest(new TestCmsStringTemplateResolver("testRenderObjectFuntions"));
        suite.addTest(new TestCmsStringTemplateResolver("testRenderDate"));
        suite.addTest(new TestCmsStringTemplateResolver("testRenderLink"));
        suite.addTest(new TestCmsStringTemplateResolver("testRenderSettings"));
        suite.addTest(new TestCmsStringTemplateResolver("testRenderStructure"));

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
     * Test using a nested XML content schema.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testContentAccessNestedSchema() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing for nested XML content schemas");

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-3.xsd",
            CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_3, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_3, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-4.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        System.out.println(xmlcontent.toString());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, xmlcontent);

        // positive tests
        assertEquals(
            "/index.html",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Cascade/VfsLink%", bean, null));
        assertEquals(
            "/test.html",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.(\"Cascade[1]/VfsLink[2]\")%", bean, null));
        assertEquals(
            "/index.jsp",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.(\"Cascade[2]/VfsLink\")%", bean, null));
        assertEquals(
            "\n"
                + "<a href=\"http://www.alkacon.com\">Alkacon</a>\n"
                + "<a href=\"/noexist/index.html\">Index page</a>\n"
                + "<a href=\"/noexist/folder1/index.html?a=b&c=d#anchor\">Index page</a>\n"
                + "Please note: The internal link targets must not exist,\n"
                + "because otherwise the link management will add <uuid> nodes which are unknown in the test case.\n"
                + "",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Cascade/Html%", bean, null));

        assertEquals(
            "<a href='/index.html' /><a href='/test.html' />",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.valueList.Cascade/VfsLink :{link |<a href='%link%' />} %",
                bean,
                null));
        assertEquals(
            "<a href='/index.html' /><a href='/test.html' />",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.valueList.(\"Cascade[1]/VfsLink\") :{link |<a href='%link%' />} %",
                bean,
                null));

        assertEquals(
            "<div><a href='/index.html' /><a href='/test.html' /></div><div><a href='/index.jsp' /></div>",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.valueList.Cascade :{cascade |<div>%cascade.valueList.VfsLink :{link |<a href='%link%' />}%</div>} %",
                bean,
                null));
    }

    /**
     * Tests content value access and the {@link CmsJspVfsAccessBean#getExistsXml()} method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRenderContentValue() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0001.html");
        assertEquals(
            "Sample article 1  (>>SearchEgg1<<)",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Title%", article, null));
        assertEquals(
            "true",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Title.exists%", article, null));
        assertEquals(
            "Foo",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if(content.value.Title.exists)%Foo%else%Bar%endif%",
                article,
                null));
        assertEquals(
            "Bar",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if(content.value.Title.isEmpty)%Foo%else%Bar%endif%",
                article,
                null));
        assertEquals(
            "Bar",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if(content.value.Missing.exists)%Foo%else%Bar%endif%",
                article,
                null));
        assertEquals(
            "Foo",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if(content.value.Missing.isEmpty)%Foo%else%Bar%endif%",
                article,
                null));
        assertEquals(
            "Foo",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if(content.value.Missing.isEmpty)%Foo%else%Bar%endif%",
                article,
                null));
    }

    /**
     * Tests date formatting.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testRenderDate() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0004.html");
        assertEquals(
            "This is article number 4",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Title%", article, null));
        assertEquals(
            "7/18/17 8:40 AM",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.value.Release.toDate; format=\"M/d/yy h:mm a\"%",
                article,
                null));
        assertEquals(
            "July 18, 2017 8:40:00 AM CEST",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.value.Release.toDate; format=\"MMMMM d, yyyy h:mm:ss a z\"%",
                article,
                null));

        assertEquals(
            "8:40 AM",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.value.Release.toDate; format=\"h:mm a\"%",
                article,
                null));
        assertEquals(
            "7/18/17",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.value.Release.toDate; format=\"M/d/yy\"%",
                article,
                null));
        assertEquals(
            "18/07/2017",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.value.Release.toDate; format=\"dd/MM/yyyy\"%",
                article,
                null));
    }

    /**
     * Tests the link rendering.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testRenderLink() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0004.html");
        assertEquals(
            "This is article number 4",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Title%", article, null));
        assertEquals(
            "/xmlcontent/article_0003.html",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Homepage%", article, null));
        assertEquals(
            "/data/opencms/xmlcontent/article_0003.html",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Homepage.toLink%", article, null));
        assertEquals(
            "/data/opencms/xmlcontent/article_0004.html",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.fileLink%", article, null));
    }

    /**
     * Tests the object function wrapper.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testRenderObjectFuntions() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0004.html");
        assertEquals(
            "This is article number 4",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.value.Title%", article, null));

        assertEquals(
            "EQUAL",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if (fn.(content.value.Title.toString).isEqual.(\"This is article number 4\"))%EQUAL%else%NOT equal%endif%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));
        assertEquals(
            "EQUAL",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if (fn.(content.value.Title).isEqual.(\"This is article number 4\"))%EQUAL%else%NOT equal%endif%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));
        long rt = cms.getRequestContext().getRequestTime();
        assertEquals(
            String.valueOf(rt),
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%content.vfs.context.requestTime%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));
        assertEquals(
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(rt)),
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%fn.(content.vfs.context.requestTime).toDate; format=\"long\"%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));
        assertEquals(
            (new SimpleDateFormat("dd.MM.yyyy")).format(new Date(rt)),
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%fn.(content.vfs.context.requestTime).toDate; format=\"dd.MM.yyyy\"%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));

        assertEquals(
            "This is the article 4 text\n"
                + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.\n"
                + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.\n"
                + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.\n"
                + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.\n"
                + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.",

            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%trim(fn.(content.value.Text).stripHtml)%",
                article,
                Collections.<String, Object> singletonMap(
                    CmsStringTemplateRenderer.KEY_FUNCTIONS,
                    CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms)))));

        String trimmed = CmsStringTemplateRenderer.renderTemplate(
            cms,
            "%fn.(content.value.Teaser).trimToSize.(\"30\")%",
            article,
            Collections.<String, Object> singletonMap(
                CmsStringTemplateRenderer.KEY_FUNCTIONS,
                CmsCollectionsGenericWrapper.createLazyMap(new CmsObjectFunctionTransformer(cms))));

        assertTrue(trimmed.length() < 30);
    }

    /**
     * Tests settings access in the string template.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testRenderSettings() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0004.html");

        Map<String, String> settings = new HashMap<String, String>(4);
        Date settingDate = new Date();
        settings.put("settingKey", "settingValue");
        settings.put("dateKey", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(settingDate));
        Map<String, CmsJspObjectValueWrapper> wrappedSettings = CmsStringTemplateRenderer.wrapSettings(cms, settings);
        Map<String, Object> objects = Collections.<String, Object> singletonMap("settings", wrappedSettings);

        assertEquals(
            "settingValue",
            CmsStringTemplateRenderer.renderTemplate(cms, "%settings.settingKey%", article, objects));

        assertEquals(
            "TRUE",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if (settings.settingKey.isSet)%TRUE%else%FALSE%endif%",
                article,
                objects));

        assertEquals(
            "FALSE",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if (settings.settingKey.isEmpty)%TRUE%else%FALSE%endif%",
                article,
                objects));

        assertEquals(
            (new SimpleDateFormat("dd.MM.yyyy")).format(settingDate),
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%settings.dateKey.toDate; format=\"dd.MM.yyyy\"%",
                article,
                objects));

        assertEquals(
            "FALSE",
            CmsStringTemplateRenderer.renderTemplate(
                cms,
                "%if (settings.noneExistingKey.isSet)%TRUE%else%FALSE%endif%",
                article,
                objects));

    }

    /**
     * Test the printStructure utility method.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testRenderStructure() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource article = cms.readResource("/xmlcontent/article_0004.html");
        assertEquals(
            "<ul>\n"
                + "<li>Author[1]</li>\n"
                + "<li>Homepage[1]</li>\n"
                + "<li>Release[1]</li>\n"
                + "<li>Teaser[1]</li>\n"
                + "<li>Text[1]</li>\n"
                + "<li>Title[1]</li>\n"
                + "</ul>",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.printStructure%", article, null));

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-3.xsd",
            CmsEncoder.ENCODING_UTF_8);
        CmsXmlContentDefinition.unmarshal(content, SCHEMA_SYSTEM_ID_3, resolver);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_3, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-4.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_4, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // now create the XML content
        content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-4.xml", CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent = CmsXmlContentFactory.unmarshal(content, CmsEncoder.ENCODING_UTF_8, resolver);
        System.out.println(xmlcontent.toString());

        // validate the XML structure
        xmlcontent.validateXmlStructure(resolver);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, xmlcontent);
        assertEquals(
            "<ul>\n"
                + "<li>Cascade[1]</li>\n"
                + "<li>Cascade[1]/Html[1]</li>\n"
                + "<li>Cascade[1]/VfsLink[1]</li>\n"
                + "<li>Cascade[1]/VfsLink[2]</li>\n"
                + "<li>Cascade[2]</li>\n"
                + "<li>Cascade[2]/Html[1]</li>\n"
                + "<li>Cascade[2]/VfsLink[1]</li>\n"
                + "<li>Title[1]</li>\n"
                + "</ul>",
            CmsStringTemplateRenderer.renderTemplate(cms, "%content.printStructure%", bean, null));
    }

}
