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

import org.opencms.test.OpenCmsTestCase;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Tests the text extraction form a Pdf file.<p>
 */
public class TestMsPowerPointExtraction extends OpenCmsTestCase {

    /**
     * Tests the PowerPoint text extraction for old OLE2 documents.<p>
     *
     * Also checks special chars like "&auml; &ouml; &uuml; &Auml; &Ouml; &Uuml; &szlig;"<p>
     *
     * @throws Exception if the test fails
     */
    public void testPPtExtractionOLE2() throws Exception {

        ClassLoader classloader = org.apache.poi.poifs.filesystem.POIFSFileSystem.class.getClassLoader();
        URL res = classloader.getResource("org/apache/poi/poifs/filesystem/POIFSFileSystem.class");
        String path = res.getPath();
        System.out.println("Core POI came from " + path);

        // open an input stream for the test file
        @SuppressWarnings("resource")
        InputStream in = getClass().getClassLoader().getResourceAsStream("org/opencms/search/extractors/test1.ppt");

        // extract the content
        I_CmsExtractionResult extractionResult = CmsExtractorMsOfficeOLE2.getExtractor().extractText(in);
        Map<String, String> items = extractionResult.getContentItems();

        System.out.println("\n\n---------------------------------------------------------------");
        System.out.println("Extracted from MS PowerPoint OLE2:");
        Iterator<Map.Entry<String, String>> i = items.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> e = i.next();
            System.out.println("\nKey: " + e.getKey());
            System.out.println("Value: " + e.getValue());
        }
        //This should be 11, adding 10 for CmsTextExtractor.extractText, and adding one for new CmsExtractionResult.
        assertEquals(11, items.size());
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
        // NOTE: Euro symbol conversion fails - possible reason is that Extraction method class handles only ISO
        // this is "&auml; &ouml; &uuml; &Auml; &Ouml; &Uuml; &szlig;"
        assertTrue(result.indexOf("\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df") > -1);

        assertEquals("Alkacon Software - The OpenCms experts", items.get(I_CmsExtractionResult.ITEM_TITLE));
        assertEquals("This is the subject", items.get(I_CmsExtractionResult.ITEM_SUBJECT));
        assertEquals("Alexander Kandzior", items.get(I_CmsExtractionResult.ITEM_AUTHOR));
        assertEquals("Alkacon Software", items.get(I_CmsExtractionResult.ITEM_COMPANY));
        assertEquals("This is the comment", items.get(I_CmsExtractionResult.ITEM_COMMENTS));
        assertEquals("Key1, Key2", items.get(I_CmsExtractionResult.ITEM_KEYWORDS));
        assertEquals("M. Manager", items.get(I_CmsExtractionResult.ITEM_MANAGER));
        assertEquals("Category", items.get(I_CmsExtractionResult.ITEM_CATEGORY));
    }

    /**
     * Tests the PowerPoint text extraction for new (MS Office 2007) OOXML documents.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPPtExtractionOOXML() throws Exception {

        // open an input stream for the test file
        @SuppressWarnings("resource")
        InputStream in = getClass().getClassLoader().getResourceAsStream("org/opencms/search/extractors/test1.pptx");

        // extract the content
        I_CmsExtractionResult extractionResult = CmsExtractorMsOfficeOOXML.getExtractor().extractText(in);
        Map<String, String> items = extractionResult.getContentItems();

        System.out.println("\n\n---------------------------------------------------------------");
        System.out.println("Extracted from MS PowerPoint (Office 2007 OOXML):");
        Iterator<Map.Entry<String, String>> i = items.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> e = i.next();
            System.out.println("\nKey: " + e.getKey());
            System.out.println("Value: " + e.getValue());
        }
        //This should be 10, adding 9 for CmsTextExtractor.extractText, and adding one for new CmsExtractionResult.
        assertEquals(10, items.size());
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
        // NOTE: Euro symbol conversion fails - possible reason is that Extraction method class handles only ISO
        // this is "&auml; &ouml; &uuml; &Auml; &Ouml; &Uuml; &szlig;"
        assertTrue(result.indexOf("\u00e4\u00f6\u00fc\u00c4\u00d6\u00dc\u00df") > -1);

        assertEquals("Alkacon Software - The OpenCms experts", items.get(I_CmsExtractionResult.ITEM_TITLE));
        assertEquals("This is the subject", items.get(I_CmsExtractionResult.ITEM_SUBJECT));
        assertEquals("Alexander Kandzior", items.get(I_CmsExtractionResult.ITEM_AUTHOR));
        assertEquals("Category", items.get(I_CmsExtractionResult.ITEM_CATEGORY));
        assertEquals("Key1, Key2", items.get(I_CmsExtractionResult.ITEM_KEYWORDS));
        assertEquals("M. Manager", items.get(I_CmsExtractionResult.ITEM_MANAGER));

        // either I am doing something wrong or Tika 0.9 does not support the "company" and "comment" meta information
        // assertEquals("Alkacon Software", items.get(I_CmsExtractionResult.ITEM_COMPANY));
        // assertEquals("This is the comment", items.get(I_CmsExtractionResult.ITEM_COMMENTS));

        // Tika 0.9 extracts the "creator" information from OOXML but not OLE2
        assertEquals("Alexander Kandzior", items.get(I_CmsExtractionResult.ITEM_CREATOR));
    }
}