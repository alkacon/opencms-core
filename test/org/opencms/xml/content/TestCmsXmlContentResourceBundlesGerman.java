/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Test cases for resource bundles in schemas with a different configured default locale.<p>
 */
public class TestCmsXmlContentResourceBundlesGerman extends OpenCmsTestRunner {

    /**
     * Overrides the OpenCms test setup.
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests whether the bundle messages are correct.<p>
     *
     * @throws Exception
     */
    @Test
    public void testReadBundleMessages() throws Exception {

        CmsObject cms = getCmsObject();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
        String content;
        CmsXmlContentDefinition definition;
        I_CmsXmlContentHandler contentHandler;

        // unmarshal content definition with localization in properties and XML
        content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-1_localized2.xsd",
            CmsEncoder.ENCODING_UTF_8);
        definition = CmsXmlContentDefinition.unmarshal(
            content,
            TestCmsXmlContentWithVfs.SCHEMA_SYSTEM_ID_1L1,
            resolver);

        contentHandler = definition.getContentHandler();
        assertSame(definition.getContentHandler().getClass().getName(), CmsDefaultXmlContentHandler.class.getName());

        CmsMessages messagesDe = contentHandler.getMessages(Locale.GERMAN);
        String messageText = messagesDe.key("label.author");
        assertContains(messageText, "JETZT");

        CmsMessages messagesEn = contentHandler.getMessages(Locale.ENGLISH);
        messageText = messagesEn.key("label.author");
        assertContains(messageText, "NOW");
    }

}
