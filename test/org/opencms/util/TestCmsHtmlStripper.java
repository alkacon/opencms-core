/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsHtmlStripper.java,v $
 * Date   : $Date: 2007/07/04 16:57:07 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;

import junit.framework.TestCase;

/** 
 * Test case for <code>{@link org.opencms.util.CmsHtmlStripper}</code>.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 6.9.2
 */
public class TestCmsHtmlStripper extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsHtmlStripper(String arg0) {

        super(arg0);
    }

    /**
     * Tests <code>{@link CmsHtmlStripper#stripHtml(String)}</code> 
     * with a configuration that only keeps: "b,p,strong,i". <p>
     * 
     * @throws Exception in case the test fails
     */
    public void testStripHtml() throws Exception {

        CmsHtmlStripper stripper = new CmsHtmlStripper();
        stripper.addPreserveTags("b,p,strong,i", ',');

        String content1 = CmsFileUtil.readFile("org/opencms/util/testHtml_01.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result1 = stripper.stripHtml(content1);
        System.out.println(result1 + "\n\n");
        assertFalse(content1.equals(result1));
        result1 = result1.toLowerCase();
        assertTrue("Html must not contain h1 tag.", result1.indexOf("<h1") < 0);
        assertTrue("Html must not contain h2 tag.", result1.indexOf("<h2") < 0);
        assertTrue("Html must not contain head tag.", result1.indexOf("<head") < 0);

        String content2 = CmsFileUtil.readFile("org/opencms/util/testHtml_02.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result2 = stripper.stripHtml(content2);
        System.out.println(result2 + "\n\n");
        assertFalse(content2.equals(result2));
        result1 = result1.toLowerCase();
        assertTrue("Html must not contain h1 tag.", result2.indexOf("<h1") < 0);
        assertTrue("Html must not contain h2 tag.", result2.indexOf("<h2") < 0);
        assertTrue("Html must not contain head tag.", result2.indexOf("<head") < 0);

        String emil = CmsFileUtil.readFile("org/opencms/util/testHtml_03.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result3 = stripper.stripHtml(emil);
        System.out.println(result3 + "\n\n");
        assertFalse(emil.equals(result3));
        assertTrue("Html must not contain h1 tag.", result3.indexOf("<h1") < 0);
        assertTrue("Html must not contain h2 tag.", result3.indexOf("<h2") < 0);
        assertTrue("Html must not contain head tag.", result3.indexOf("<head") < 0);

    }

    /**
     * Tests <code>{@link CmsHtmlStripper#stripHtml(String)}</code> 
     * with no configuration (bypass-mode).<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testStripHtmlBypass() throws Exception {

        CmsHtmlStripper stripper = new CmsHtmlStripper(false);

        String content1 = CmsFileUtil.readFile("org/opencms/util/testHtml_01.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result1 = stripper.stripHtml(content1);
        System.out.println(result1 + "\n\n");
        // This fails as htmlparser adds a missing closing tag:
        //assertEquals(content1, result1);

        String content2 = CmsFileUtil.readFile("org/opencms/util/testHtml_02.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result2 = stripper.stripHtml(content2);
        System.out.println(result2 + "\n\n");
        assertEquals(content2, result2);

        String emil = CmsFileUtil.readFile("org/opencms/util/testHtml_03.html", CmsEncoder.ENCODING_ISO_8859_1);
        String result3 = stripper.stripHtml(emil);
        System.out.println(result3 + "\n\n");
        assertEquals(emil, result3);

    }

    /**
     * Tests <code>{@link CmsHtmlStripper#stripHtml(String)}</code> 
     * if it strips newline separators. <p>
     * 
     * @throws Exception in case the test fails
     */
    public void testStripHtmlNewline() throws Exception {

        String test = "<p>Dies ist eine Paragrahph\r hier die 2. Zeile \r\n und die dritte.</p> Nach dem Paragrahp";
        CmsHtmlStripper stripper = new CmsHtmlStripper();
        stripper.addPreserveTag("p");
        String result = stripper.stripHtml(test);
        assertEquals(test, result);
    }
}