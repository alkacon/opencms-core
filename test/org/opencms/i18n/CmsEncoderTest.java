/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/i18n/Attic/CmsEncoderTest.java,v $
 * Date   : $Date: 2004/05/05 21:25:09 $
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
 * Important: This file is encoded in UTF-8 Unicode!.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsEncoderTest extends TestCase {

    /**
     * @see CmsEncoder#encodeForHtml(String, String)
     */
    public void testEncodeForHtml() {
        
        // the usual euro symbol and german umlauts test
        String test = "Test: äöüÄÖÜß€";
        
        // ISO-8859-1 does not contain the euro symbol
        assertEquals(CmsEncoder.encodeForHtml(test, "ISO-8859-1"), "Test: äöüÄÖÜß&#8364;");
        // ISO-8859-15 does
        assertEquals(CmsEncoder.encodeForHtml(test, "ISO-8859-15"), test);
        // UTF-8 should be able to encode almost everything
        assertEquals(CmsEncoder.encodeForHtml(test, "UTF-8"), test);
        // US-ASCII contains only the first 128 chars
        assertEquals(CmsEncoder.encodeForHtml(test, "US-ASCII"), "Test: &#228;&#246;&#252;&#196;&#214;&#220;&#223;&#8364;");
        // cp1252 is used by MS Windows (for western european based languages)
        assertEquals(CmsEncoder.encodeForHtml(test, "Cp1252"), test);
    }    
}
