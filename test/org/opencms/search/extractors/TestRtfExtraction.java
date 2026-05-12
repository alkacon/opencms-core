/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.extractors;

import org.opencms.test.OpenCmsTestRunner;

import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests the text extraction form a RTF file.<p>
 */
public class TestRtfExtraction extends OpenCmsTestRunner {

    /**
     * Tests the basic RTF extraction.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testBasicRtfExtraction() throws Exception {

        // open an input stream for the test file
        @SuppressWarnings("resource")
        InputStream in = getClass().getClassLoader().getResourceAsStream("org/opencms/search/extractors/test1.rtf");

        // extract the content
        I_CmsExtractionResult extractionResult = CmsExtractorRtf.getExtractor().extractText(in);
        Map<String, String> items = extractionResult.getContentItems();
        //This should be 6, adding 5 for CmsTextExtractor.extractText, and adding one for new CmsExtractionResult.
        assertEquals(6, items.size());
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_CONTENT));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_RAW));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_AUTHOR));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_TITLE));
        assertEquals("Alkacon Software \u2013 The OpenCms experts", items.get(I_CmsExtractionResult.ITEM_TITLE));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_COMPANY));
        assertEquals("Alkacon Software", items.get(I_CmsExtractionResult.ITEM_COMPANY));
        String result = extractionResult.getContent();
        assertEquals(result, items.get(I_CmsExtractionResult.ITEM_CONTENT));

        System.out.println("---------------------------------------------------------------");
        System.out.println("Extracted from RTF:");
        System.out.println(result);

        assertTrue(result.indexOf("Alkacon Software") > -1);
        assertTrue(result.indexOf("The OpenCms experts") > -1);
        assertTrue(result.indexOf("Some content here.") > -1);
        assertTrue(result.indexOf("Some content there.") > -1);
        assertTrue(result.indexOf("Some content on a second sheet.") > -1);
        assertTrue(result.indexOf("Some content on the third sheet.") > -1);
        // NOTE: Euro symbol conversion fails - possible reason is that Swing classes handle only ISO
        assertTrue(result.indexOf("\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df") > -1);
    }

    /**
     * Tests the basic RTF extraction.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testBasicRtfExtractionWithoutMetaData() throws Exception {

        // open an input stream for the test file
        @SuppressWarnings("resource")
        InputStream in = getClass().getClassLoader().getResourceAsStream("org/opencms/search/extractors/test1.rtf");

        // extract the content
        I_CmsExtractionResult extractionResult = CmsExtractorRtf.getExtractor(false).extractText(in);
        Map<String, String> items = extractionResult.getContentItems();
        //This should be 2, adding 1 for CmsTextExtractor.extractText, and adding one for new CmsExtractionResult.
        assertEquals(2, items.size());
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_CONTENT));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_RAW));
        assertFalse(items.containsKey(I_CmsExtractionResult.ITEM_AUTHOR));
        assertFalse(items.containsKey(I_CmsExtractionResult.ITEM_TITLE));
        assertFalse(items.containsKey(I_CmsExtractionResult.ITEM_COMPANY));
        String result = extractionResult.getContent();
        assertEquals(result, items.get(I_CmsExtractionResult.ITEM_CONTENT));

        System.out.println("---------------------------------------------------------------");
        System.out.println("Extracted from RTF:");
        System.out.println(result);

        assertTrue(result.indexOf("Alkacon Software") > -1);
        assertTrue(result.indexOf("The OpenCms experts") > -1);
        assertTrue(result.indexOf("Some content here.") > -1);
        assertTrue(result.indexOf("Some content there.") > -1);
        assertTrue(result.indexOf("Some content on a second sheet.") > -1);
        assertTrue(result.indexOf("Some content on the third sheet.") > -1);
        // NOTE: Euro symbol conversion fails - possible reason is that Swing classes handle only ISO
        assertTrue(result.indexOf("\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df") > -1);
    }
}
