/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/TestCmsEncoder.java,v $
 * Date   : $Date: 2004/07/07 18:44:19 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.i18n;

import junit.framework.TestCase;

/**
 * Tests for the CmsEncoder.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class TestCmsEncoder extends TestCase {

    // working around encoding issues (e.g. of CVS) by using unicode values 
    // the values of C_STRING_1 are: ae oe ue Ae Oe Ue scharfes-s euro-symbol
    private static final String C_STRING_1 = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac";     
    private static final String C_STRING_2 = "Test: \u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df&#8364;";
    private static final String C_STRING_3 = "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364;";
    private static final String C_STRING_4 = "\u00e4\u00f6\u00fc\u20ac#|#12|&#12|&#;\u00c4\u00d6\u00dctest";
    private static final String C_STRING_5 = "&#228;&#246;&#252;&#8364;#|#12|&#12|&#;&#196;&#214;&#220;test";
    
    private static final String C_ENC_ISO_8859_1 = "ISO-8859-1";
    private static final String C_ENC_ISO_8859_15 = "ISO-8859-15";
    private static final String C_ENC_UTF_8 = "UTF-8";
    private static final String C_ENC_US_ASCII = "US-ASCII";
    private static final String C_ENC_WINDOWS_1252 = "Cp1252";
    
    private static final String[][] TESTS_ENCODE = { 
        {C_STRING_1, C_STRING_2, C_ENC_ISO_8859_1},
        {C_STRING_1, C_STRING_1, C_ENC_ISO_8859_15},
        {C_STRING_1, C_STRING_1, C_ENC_UTF_8},
        {C_STRING_1, C_STRING_3, C_ENC_US_ASCII},
        {C_STRING_1, C_STRING_1, C_ENC_WINDOWS_1252},
        {C_STRING_4, C_STRING_5, C_ENC_US_ASCII},
    };    

    private static final String[][] TESTS_DECODE = { 
        {C_STRING_3, C_STRING_2, C_ENC_ISO_8859_1},
        {C_STRING_3, C_STRING_1, C_ENC_ISO_8859_15},
        {C_STRING_3, C_STRING_1, C_ENC_UTF_8},
        {C_STRING_3, C_STRING_3, C_ENC_US_ASCII},
        {C_STRING_3, C_STRING_1, C_ENC_WINDOWS_1252},
        {C_STRING_5, C_STRING_4, C_ENC_UTF_8},
    };      
    
    /**
     * @see CmsEncoder#encodeHtmlEntities(String, String)
     */
    public void testEncodeForHtml() {
        
        for (int i=0; i<TESTS_ENCODE.length; i++) {
            String source = TESTS_ENCODE[i][0];
            String dest = TESTS_ENCODE[i][1];
            String encoding = TESTS_ENCODE[i][2];
            
            String result = CmsEncoder.encodeHtmlEntities(source, encoding);
            assertEquals(result, dest);            
        }
    } 
    
    /**
     * @see CmsEncoder#decodeHtmlEntities(String, String) 
     */
    public void testDecodeHtmlEntities() {
        
        for (int i=0; i<TESTS_DECODE.length; i++) {
            String source = TESTS_DECODE[i][0];
            String dest = TESTS_DECODE[i][1];
            String encoding = TESTS_DECODE[i][2];
            
            String result = CmsEncoder.decodeHtmlEntities(source, encoding);
            assertEquals(result, dest);            
        }    
    }
    
    /**
     * @see CmsEncoder#lookupEncoding(String, String)
     */
    public void testLookupEncoding() {
        assertEquals(CmsEncoder.lookupEncoding("UTF-8", null), "UTF-8");
        assertEquals(CmsEncoder.lookupEncoding("utf-8", null), "UTF-8");
        assertEquals(CmsEncoder.lookupEncoding("UTF8", null), "UTF-8");
        assertEquals(CmsEncoder.lookupEncoding("utf8", null), "UTF-8");
        assertEquals(CmsEncoder.lookupEncoding("ISO-8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso-8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO_8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso_8859-1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("ISO_8859_1", null), "ISO-8859-1");
        assertEquals(CmsEncoder.lookupEncoding("iso_8859_1", null), "ISO-8859-1");        
        assertEquals(CmsEncoder.lookupEncoding("latin1", null), "ISO-8859-1");        
    }
}
