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

package org.opencms.search.extractors;

import org.opencms.test.OpenCmsTestCase;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Tests the text extraction form a Pdf file.<p>
 */
public class TestPdfExtraction extends OpenCmsTestCase {

    /**
     * Tests the basic Pdf extraction.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBasicPdfExtraction() throws Exception {

        // open an input stream for the test file
        InputStream in = getClass().getClassLoader().getResourceAsStream("org/opencms/search/extractors/test1.pdf");

        // extract the content
        I_CmsExtractionResult extractionResult = CmsExtractorPdf.getExtractor().extractText(in);
        Map<String, String> items = extractionResult.getContentItems();

        System.out.println("\n\n---------------------------------------------------------------");
        System.out.println("Extracted from PDF:");
        Iterator<Map.Entry<String, String>> i = items.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> e = i.next();
            System.out.println("\nKey: " + e.getKey());
            System.out.println("Value: " + e.getValue());
        }

        assertEquals(8, items.size());
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_CONTENT));
        assertTrue(items.containsKey(I_CmsExtractionResult.ITEM_RAW));
        String result = extractionResult.getContent();
        assertEquals(result, items.get(I_CmsExtractionResult.ITEM_CONTENT));

        assertTrue(result.indexOf("Alkacon Software") > -1);
        assertTrue(result.indexOf("The OpenCms experts") > -1);
        assertTrue(result.indexOf("Some content here.") > -1);
        assertTrue(result.indexOf("Some content there.") > -1);
        assertTrue(result.indexOf("Some content on a second sheet.") > -1);
        assertTrue(result.indexOf("Some content on the third sheet.") > -1);
        assertTrue(result.indexOf("\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df\u20ac") > -1);

        assertEquals("Alkacon Software - The OpenCms experts", items.get(I_CmsExtractionResult.ITEM_TITLE));
        assertEquals("This is the subject", items.get(I_CmsExtractionResult.ITEM_SUBJECT));
        assertEquals("Alexander Kandzior", items.get(I_CmsExtractionResult.ITEM_AUTHOR));
        assertEquals("Key1, Key2", items.get(I_CmsExtractionResult.ITEM_KEYWORDS));
        assertEquals("Microsoft Excel", items.get(I_CmsExtractionResult.ITEM_CREATOR));
        assertEquals("Jaws PDF Creator v4.0.24", items.get(I_CmsExtractionResult.ITEM_PRODUCER));
    }
}
