/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronTable.java,v $
 * Date   : $Date: 2003/10/29 13:00:42 $
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

package org.opencms.cron;

import com.opencms.core.CmsException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a complete crontable with cronentries.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 13:00:42 $
 * @since 5.1.12
 */
public class CmsCronTable extends Object {

    /** Contains all valid Cms cron entries.<p> */
    private List m_cronEntries;

    /**
     * Creates a cron table from the specified string representing the cron table.<p>
     * 
     * @param table a String representing the cron table
     * @throws IOException if the string couldn't be read
     * @throws CmsException if the string contains an invalid line
     */
    public CmsCronTable(String table) throws IOException, CmsException {
        m_cronEntries = (List) new ArrayList();
        update(new StringReader(table));
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if (m_cronEntries != null) {
            m_cronEntries.clear();
        }
    }

    /**
     * Updates the table with the new values.<p>
     * 
     * @param reader - the Reader to get the new values from.
     * @throws IOException if the reader couldn't be read
     * @throws CmsException if the string contains a invalid parameterline.
     */
    public void update(Reader reader) throws IOException, CmsException {
        m_cronEntries.clear();
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line = lineReader.readLine();
        
        while (line != null) {
            line = line.trim();
            if (!"".equals(line)) {
                try {
                    m_cronEntries.add(new CmsCronEntry(line));
                } catch (CmsException e) {
                    if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                        org.opencms.main.OpenCms.getLog(this).error("Error parsing cron tab in line: " + line, e);
                    }                      
                }
            }
            
            line = lineReader.readLine();
        }
        
        reader.close();
    }

    /**
     * Updates the table with the new values.<p>
     * 
     * @param table - the String to get the new values from.
     * @throws IOException if the reader couldn't be read
     * @throws CmsException if the string contains a invalid parameterline.
     */
    public void update(String table) throws IOException, CmsException {
        update(new StringReader(table));
    }

    /**
     * Returns the size of thos table.<p>
     * 
     * @return the size of thos table.
     */
    public int size() {
        return m_cronEntries.size();
    }

    /**
     * Returns one entry of this table.<p>
     * 
     * @param i the id of the etnry to return
     * @return one CmsCronEntry.
     */
    public CmsCronEntry get(int i) {
        return (CmsCronEntry) m_cronEntries.get(i);
    }

    /**
     * Adds a new CmsCronEntry.<p>
     * 
     * @param entry the entry to add.
     */
    public void add(CmsCronEntry entry) {
        m_cronEntries.add(entry);
    }

    /**
     * Removes one entry from this table.<p>
     * 
     * @param entry the entry to remove.
     */
    public void remove(CmsCronEntry entry) {
        m_cronEntries.remove(entry);
    }

    /**
     * Returns this table as string.<p>
     * 
     * @return this table as string.
     */
    public String getTable() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            result.append(get(i).getParamstring() + "\n");
        }
        return result.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getClass().getName() + "[\n");
        for (int i = 0; i < size(); i++) {
            result.append("\t" + get(i).toString() + "\n");
        }
        result.append("]\n");
        return result.toString();
    }
}
