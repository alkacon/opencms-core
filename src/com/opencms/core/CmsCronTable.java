/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsCronTable.java,v $
* Date   : $Date: 2004/01/06 13:36:14 $
* Version: $Revision: 1.3.2.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import com.opencms.boot.I_CmsLogChannels;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

/**
 * Describes a complete crontable with cronentries.
 */
class CmsCronTable {

    /** This vector contains all CronEntries for this table */
    private Vector m_cronEntries = new Vector();

    /**
     * Creates a new empty table.
     */
    CmsCronTable() {
        // noop
    }

    /**
     * Creates a new table based on the parameter lines in this String.
     * @param table a String with parameterlines.
     * @throws IOException if the string couldn't be read
     */
    CmsCronTable(String table) throws IOException {
        this(new StringReader(table));
    }

    /**
     * Creates a new table based on the parameter lines in this Reader.
     * @param reder a Reader with parameterlines.
     * @throws IOException if the reader couldn't be read
     */
    CmsCronTable(Reader reader) throws IOException {
        update(reader);
    }

    /**
     * Updates the table with the new values.
     * 
     * @param reader - the Reader to get the new values from.
     * @throws IOException if the reader couldn't be read
     */
    void update(Reader reader) throws IOException {
        m_cronEntries = new Vector();
        LineNumberReader lnreader = new LineNumberReader(reader);
        String line = lnreader.readLine();

        while (line != null) {
            line = line.trim();
            if (!"".equals(line)) {
                try {
                    m_cronEntries.add(new CmsCronEntry(line));
                } catch (CmsException e) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, "Error parsing cron tab in line: " + line + ": " + e.toString());
                    }
                }
            }

            line = lnreader.readLine();
        }

        reader.close();
    }

    /**
     * Updates the table with the new values.
     * @param table - the String to get the new values from.
     * @throws IOException if the reader couldn't be read
     */
    void update(String table) throws IOException {
        update(new StringReader(table));
    }

    /**
     * Returns the size of thos table.
     * @return the size of thos table.
     */
    public int size() {
        return m_cronEntries.size();
    }

    /**
     * Returns one entry of this table.
     * @param i the id of the etnry to return
     * @return one CmsCronEntry.
     */
    public CmsCronEntry get(int i) {
        return (CmsCronEntry)m_cronEntries.get(i);
    }

    /**
     * Adds a new CmsCronEntry.
     * @param entry the entry to add.
     */
    public void add(CmsCronEntry entry) {
        m_cronEntries.add(entry);
    }

    /**
     * Removes one entry from this table.
     * @param entry the entry to remove.
     */
    public void remove(CmsCronEntry entry) {
        m_cronEntries.removeElement(entry);
    }

    /**
     * Returns this table as string.
     * @return this table as string.
     */
    public String getTable() {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < size(); i++) {
            result.append(get(i).getParamstring() + "\n");
        }
        return result.toString();
    }

    /**
     * Returns a Stringrepresentation of this object.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getClass().getName() + "[\n");
        for(int i = 0; i < size(); i++) {
            result.append("\t" + get(i).toString() + "\n");
        }
        result.append("]\n");
        return result.toString();
    }
}
