/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapDumper.java,v $
 * Date   : $Date: 2010/10/07 07:56:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Helper class which can dump a sitemap structure into a text file for debugging purposes.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapDumper {

    /** The sitemap entry from which the dump should start. */
    private CmsInternalSitemapEntry m_entry;

    /** The name of the output file. */
    private String m_filename;

    /** The current indentation level. */
    private int m_indentation;

    /** The output file writer. */
    private PrintWriter m_writer;

    /**
     * Creates a sitemap dumper.<p>
     * 
     * @param entry the sitemap entry from which the dump should start 
     * @param filename the name of the file to which the dump should be written 
     */
    public CmsSitemapDumper(CmsInternalSitemapEntry entry, String filename) {

        m_entry = entry;
        m_filename = filename;
    }

    /**
     * Dumps the part of the sitemap starting with the entry given in the constructor to a text file.<p>
     * 
     * @throws IOException if writing to the file fails 
     */
    public void dumpSitemap() throws IOException {

        open();
        try {
            dumpSitemap(m_entry);
        } finally {
            close();
        }
    }

    /**
     * Closes the output file.<p>
     */
    protected void close() {

        m_writer.close();
    }

    /**
     * Dumps a sitemap entry and its descendants.<p>
     * 
     * @param entry the sitemap entry to dump 
     */
    protected void dumpSitemap(CmsInternalSitemapEntry entry) {

        dumpSitemapEntry(entry);
        indent(+8);
        for (CmsInternalSitemapEntry child : entry.getSubEntries()) {
            dumpSitemap(child);
        }
        indent(-8);

    }

    /**
     * Dumps a single sitemap entry.<p>
     *  
     * @param entry the sitemap entry to dump 
     */
    protected void dumpSitemapEntry(CmsInternalSitemapEntry entry) {

        printLine("*****************************************");
        printLine("ID    = " + entry.getId());
        printLine("PATH  = " + entry.getRootPath());
        printLine("TITLE = " + entry.getTitle());
        printLine("OP");
        Map<String, CmsSimplePropertyValue> ownProps = entry.getNewProperties();
        for (Map.Entry<String, CmsSimplePropertyValue> propEntry : ownProps.entrySet()) {
            printLine("    "
                + propEntry.getKey()
                + " := "
                + propEntry.getValue().getOwnValue()
                + " | "
                + propEntry.getValue().getInheritValue());
        }
        printLine("CP");
        Map<String, CmsComputedPropertyValue> computedProps = entry.getComputedProperties();
        for (Map.Entry<String, CmsComputedPropertyValue> propEntry : computedProps.entrySet()) {
            printLine("    "
                + propEntry.getKey()
                + " := "
                + propEntry.getValue().getOwnValue()
                + " | "
                + propEntry.getValue().getInheritValue());
        }
    }

    /**
     * Opens the output file.<p>
     * 
     * @throws IOException if opening the file fails 
     */
    protected void open() throws IOException {

        m_writer = new PrintWriter(new FileWriter(m_filename));
    }

    /**
     * Prints a number of spaces.<p>
     * 
     * @param n the number of spaces to print
     */
    protected void printIndentation(int n) {

        for (int i = 0; i < n; i++) {
            m_writer.print(" ");
        }
    }

    /**
     * Prints an indented line of text.<p>
     * 
     * @param line the line of text to print 
     */
    protected void printLine(String line) {

        printIndentation(m_indentation);
        m_writer.println(line);
    }

    /**
     * Changes the indentation level for printing to the output file.<p>
     * 
     * @param indentationDelta a number that should be added to the indentation level (can be negative)
     */
    private void indent(int indentationDelta) {

        m_indentation += indentationDelta;
    }

}
