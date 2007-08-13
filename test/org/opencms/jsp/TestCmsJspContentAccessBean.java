/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/jsp/Attic/TestCmsJspContentAccessBean.java,v $
 * Date   : $Date: 2007/08/13 16:13:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
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

package org.opencms.jsp;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.jsp.util.CmsJspContentAccessBean;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>{@link CmsJspContentAccessBean}</code>.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.0.2
 */
public class TestCmsJspContentAccessBean extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsJspContentAccessBean(String arg0) {

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
        suite.setName(TestCmsJspContentAccessBean.class.getName());

        suite.addTest(new TestCmsJspContentAccessBean("testContentAccess"));

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
     * Tests general content access for XML content.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testContentAccess() throws Exception {

        CmsObject cms = getCmsObject();

        // first read the XML content 
        CmsFile file = cms.readFile("/xmlcontent/article_0002.html");
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

        // new create the content access bean
        CmsJspContentAccessBean bean = new CmsJspContentAccessBean(cms, Locale.ENGLISH, content);

        // now for some fun with the bean
        assertSame(file, bean.getFile());

        // some simple has / has not checks
        Map hasValue = bean.getHasValue();
        assertSame(Boolean.TRUE, hasValue.get("Title"));
        assertSame(Boolean.FALSE, hasValue.get("IdontExistHere"));

        // check which kind of locales we have (should be "en" and "de")
        Map hasLocale = bean.getHasLocale();
        assertSame(Boolean.TRUE, hasLocale.get("en"));
        assertSame(Boolean.TRUE, hasLocale.get("de"));
        assertSame(Boolean.FALSE, hasLocale.get("fr"));

        // access the content form the default locale
        Map enValue = bean.getValue();
        assertEquals("This is the article 2 sample", String.valueOf(enValue.get("Title")));

        // now access the content from a selected locale
        Map localeValue = bean.getLocaleValue();
        Map deValue = (Map)localeValue.get("de");
        assertEquals("Das ist Artikel 2", String.valueOf(deValue.get("Title")));
        enValue = (Map)localeValue.get("en");
        assertEquals("This is the article 2 sample", String.valueOf(enValue.get("Title")));
        Map frValue = (Map)localeValue.get("fr");
        assertNull(frValue.get("Title"));

        // check list access to default locale
        Map enValues = bean.getValueList();
        assertEquals(2, ((List)enValues.get("Teaser")).size());
        assertEquals("This is teaser 2 in sample article 2.", String.valueOf(((List)enValues.get("Teaser")).get(1)));

        // now check list access to selected locale
        Map localeValues = bean.getLocaleValueList();
        Map deValues = (Map)localeValues.get("de");
        assertEquals(3, ((List)deValues.get("Teaser")).size());
        assertEquals(
            "Das ist der Teaser 3 im zweiten Beispielartikel.",
            String.valueOf(((List)deValues.get("Teaser")).get(2)));
        enValues = (Map)localeValues.get("en");
        assertEquals(2, ((List)enValues.get("Teaser")).size());
        assertEquals("This is teaser 2 in sample article 2.", String.valueOf(((List)enValues.get("Teaser")).get(1)));
        Map frValues = (Map)localeValues.get("fr");
        assertNull(frValues.get("Title"));
    }
}