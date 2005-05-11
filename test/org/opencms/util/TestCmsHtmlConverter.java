/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsHtmlConverter.java,v $
 * Date   : $Date: 2005/05/11 17:54:20 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.util;

import org.opencms.test.OpenCmsTestCase;

import java.io.File;

/** 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class TestCmsHtmlConverter extends OpenCmsTestCase  {
    
   // some test Strings    
    private static final String C_STRING_1 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;";     
    private static final String C_STRING_2 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364;";
    private static final String C_STRING_1_UTF8_RESULT = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df";     
    private static final String C_STRING_2_UTF8_RESULT = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac";
  
    
    private static final String C_CR_FF = "\r\n"; 
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsHtmlConverter(String arg0) {
        super(arg0);
    }

    /** 
     * Tests converstion of ISO-encoded entities.<p>     *
     */
    public void testISO() {
        System.out.println("Testing US-ASCII conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter("US-ASCII", CmsHtmlConverter.C_PARAM_WORD);        
        String convertedHtml1 = converter.convertToStringSilent(C_STRING_1);
        String convertedHtml2 = converter.convertToStringSilent(C_STRING_2);
        
        // the converted code will end with a cr ff (\r\n)         
        assertEquals(C_STRING_1 + C_CR_FF, convertedHtml1);
        assertEquals(C_STRING_2 + C_CR_FF, convertedHtml2);  
    }

    /** 
     * Tests converstion of UTF8-encoded entities.<p>     *
     */
    public void testUTF8() {
        System.out.println("Testing UTF-8 conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter("UTF-8", CmsHtmlConverter.C_PARAM_WORD);        
        String convertedHtml1 = converter.convertToStringSilent(C_STRING_1);
        String convertedHtml2 = converter.convertToStringSilent(C_STRING_2);
        
        // the converted code will end with a cr ff (\r\n)         
        assertEquals(C_STRING_1_UTF8_RESULT + C_CR_FF, convertedHtml1);
        assertEquals(C_STRING_2_UTF8_RESULT + C_CR_FF, convertedHtml2);  
    }
    
    /**
     * Tests if all word tags are removed.<p>
     */
    public void testremoveWordTags() {
        System.out.println("Testing Word conversion");
        CmsHtmlConverter converter = new CmsHtmlConverter("UTF-8", CmsHtmlConverter.C_PARAM_XHTML);        
        
        // read a file and convert it
        File inputfile = new File (getTestDataPath("test2.html"));
        try {
            byte[] htmlInput = converter.getFileBytes(inputfile);
            String inputContent = new String(htmlInput, converter.m_encoding);
            inputContent = converter.adjustHtml(inputContent);            
            byte[] htmlOutput = converter.convertToByte(inputContent);          
            String outputContent = new String(htmlOutput, converter.m_encoding);
            System.out.println(outputContent);
            // now check if all word specific tags are removed
            assertContainsNot(outputContent, "<o:p>");
            assertContainsNot(outputContent, "<o:smarttagtype");
            assertContainsNot(outputContent, "<?xml:namespace ");
                       
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    
    
}

