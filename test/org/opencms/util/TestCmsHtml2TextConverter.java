/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsHtml2TextConverter.java,v $
 * Date   : $Date: 2011/03/23 14:59:06 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;

import junit.framework.TestCase;

/** 
 * Test case for <code>{@link org.opencms.util.CmsHtml2TextConverter}</code>.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 6.2.0
 */
public class TestCmsHtml2TextConverter extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsHtml2TextConverter(String arg0) {

        super(arg0);
    }
       
    /**
     * Tests the HTML extractor.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testHtmlExtractor() throws Exception {

        String content1 = CmsFileUtil.readFile("org/opencms/util/testHtml_01.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result1 = CmsHtml2TextConverter.html2text(content1, CmsEncoder.ENCODING_ISO_8859_1);        
        System.out.println(result1 + "\n\n");
        
        String expected1 = CmsFileUtil.readFile("org/opencms/util/testHtml_01_result.html", CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals(expected1, result1);
        
        String content2 = CmsFileUtil.readFile("org/opencms/util/testHtml_02.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result2 = CmsHtml2TextConverter.html2text(content2, CmsEncoder.ENCODING_ISO_8859_1);        
        System.out.println(result2 + "\n\n");
        
        String content3 = CmsFileUtil.readFile("org/opencms/util/testHtml_03.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result3 = CmsHtml2TextConverter.html2text(content3, CmsEncoder.ENCODING_ISO_8859_1);        
        System.out.println(result3 + "\n\n");
        
//        String content3 = CmsFileUtil.readFile("org/opencms/util/testHtml_02.html", CmsEncoder.ENCODING_ISO_8859_1);
//        String result3 = CmsHtmlTextAuszeichner.machDieAuszeichnung(content3, CmsEncoder.ENCODING_ISO_8859_1);        
//        System.out.println(result3 + "\n\n");
    }
}