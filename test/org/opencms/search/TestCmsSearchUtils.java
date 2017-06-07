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

package org.opencms.search;

import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.test.OpenCmsTestCase;

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.DateTools;

/**
 * Tests some search utilities that don't require an OpenCms context.<p>
 */
public class TestCmsSearchUtils extends OpenCmsTestCase {

    /**
     * Prints a list of String to System.out.<p>
     *
     * @param strings the String to print
     */
    protected static void printStringList(List<String> strings) {

        Iterator<String> i = strings.iterator();
        System.out.println("\nSize: " + strings.size() + "\n");
        while (i.hasNext()) {
            System.out.println(i.next());
        }
        System.out.println("\n--------------------------------");
    }

    /**
     * Test for date range generation.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testDateRangeGeneration() throws Exception {

        long startDate = DateTools.stringToTime("20060101");
        long endDate = DateTools.stringToTime("20081231");
        List<String> range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 85, range.size());

        startDate = DateTools.stringToTime("20060101");
        endDate = DateTools.stringToTime("20071231");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 84, range.size());

        startDate = DateTools.stringToTime("20060101");
        endDate = DateTools.stringToTime("20061231");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 72, range.size());

        startDate = DateTools.stringToTime("20060101");
        endDate = DateTools.stringToTime("20060131");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 31, range.size());

        startDate = DateTools.stringToTime("20060131");
        endDate = DateTools.stringToTime("20060201");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 2, range.size());

        startDate = DateTools.stringToTime("20060201");
        endDate = DateTools.stringToTime("20060301");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 29, range.size());

        startDate = DateTools.stringToTime("20060201");
        endDate = DateTools.stringToTime("20060201");
        range = CmsSearchIndex.getDateRangeSpan(startDate, endDate);
        printStringList(range);
        assertEquals("Date range result list does not have required size", 1, range.size());
    }

    /**
     * Test parent folder path term splitting.<p>
     *
     * @throws Exception if the test fails
     */
    public void testParentFolderTokenizer() throws Exception {

        assertEquals("/", CmsSearchFieldConfiguration.getParentFolderTokens(null));
        assertEquals("/", CmsSearchFieldConfiguration.getParentFolderTokens(""));
        assertEquals("/", CmsSearchFieldConfiguration.getParentFolderTokens("/"));
        assertEquals("/ /sites/", CmsSearchFieldConfiguration.getParentFolderTokens("/sites/"));
        assertEquals("/", CmsSearchFieldConfiguration.getParentFolderTokens("/sites"));
        assertEquals("/ /sites/ /sites/default/", CmsSearchFieldConfiguration.getParentFolderTokens("/sites/default/"));
        assertEquals("/ /sites/", CmsSearchFieldConfiguration.getParentFolderTokens("/sites/default"));
    }
}