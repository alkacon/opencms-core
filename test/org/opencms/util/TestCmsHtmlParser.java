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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;

/**
 * Test case for <code>{@link org.opencms.util.CmsHtmlParser}</code>.<p>
 *
 * @since 6.2.0
 */
public class TestCmsHtmlParser extends OpenCmsTestCase {

    /**
     * Tests the HTML extractor.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testHtmlExtractor() throws Exception {

        I_CmsHtmlNodeVisitor visitor1 = new CmsHtmlParser(true);
        String content1 = CmsFileUtil.readFile("org/opencms/util/testHtml_01.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result1 = visitor1.process(content1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(result1 + "\n\n");
        // assertEquals(content1, result1);

        I_CmsHtmlNodeVisitor visitor2 = new CmsHtmlParser(true);
        String content2 = CmsFileUtil.readFile("org/opencms/util/testHtml_02.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result2 = visitor2.process(content2, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(result2 + "\n\n");
        assertEquals(content2, result2);

        I_CmsHtmlNodeVisitor visitor3 = new CmsHtmlParser(true);
        String content3 = CmsFileUtil.readFile("org/opencms/util/testHtml_03.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result3 = visitor3.process(content3, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(result3 + "\n\n");
        assertEquals(content3, result3);

        // check with non-echo visitor, no output should be produced
        I_CmsHtmlNodeVisitor visitor4 = new CmsHtmlParser();
        result3 = visitor4.process(content3, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(result3 + "\n\n");
        assertEquals("", result3);
    }
}