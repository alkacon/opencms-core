/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronTable.java,v $
 * Date   : $Date: 2003/11/13 10:29:27 $
 * Version: $Revision: 1.4 $
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

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsRegistry;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

/**
 * Describes a complete crontable with cronentries.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.4 $ $Date: 2003/11/13 10:29:27 $
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
     */
    public CmsCronTable(String table) throws IOException {
        m_cronEntries = (List) new ArrayList();
        update(new StringReader(table));        
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_cronEntries != null) {
                m_cronEntries.clear();
            }
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }

    /**
     * Updates the table with the new values.<p>
     * 
     * @param reader - the Reader to get the new values from.
     * @throws IOException if the reader couldn't be read
     */
    public void update(Reader reader) throws IOException {
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
        
        // read the cron tab configured in registry.xml
        readCronTab();
        
        reader.close();
    }
    
    /**
     * Reads the crontab from registry.xml.<p>
     */
    private void readCronTab() {
        Element cronjob = null;
        CmsCronEntry cronEntry = null;
        int min = I_CmsConstants.C_UNKNOWN_ID;
        int hour = I_CmsConstants.C_UNKNOWN_ID;
        int dayOfMonth = I_CmsConstants.C_UNKNOWN_ID;
        int month = I_CmsConstants.C_UNKNOWN_ID;
        int dayOfWeek = I_CmsConstants.C_UNKNOWN_ID;
        String userName = null;
        String groupName = null;
        String className = null;
        String textValue = null;
        String params = null;

        try {
            CmsRegistry registry = OpenCms.getRegistry();
            Element system = registry.getDom4jSystemElement();
            Element crontab = system.element("crontab");

            if (crontab != null) {
                List cronjobs = crontab.elements();

                Iterator i = cronjobs.iterator();
                while (i.hasNext()) {
                    cronjob = (Element) i.next();

                    try {
                        textValue = cronjob.element("min").getText().trim();
                        if ("*".equals(textValue)) {
                            min = I_CmsConstants.C_UNKNOWN_ID;
                        } else {
                            min = Integer.parseInt(textValue);
                        }

                        textValue = cronjob.element("hour").getText().trim();
                        if ("*".equals(textValue)) {
                            hour = I_CmsConstants.C_UNKNOWN_ID;
                        } else {
                            hour = Integer.parseInt(textValue);
                        }

                        textValue = cronjob.element("dayofmonth").getText().trim();
                        if ("*".equals(textValue)) {
                            dayOfMonth = I_CmsConstants.C_UNKNOWN_ID;
                        } else {
                            dayOfMonth = Integer.parseInt(textValue);
                        }

                        textValue = cronjob.element("month").getText().trim();
                        if ("*".equals(textValue)) {
                            month = I_CmsConstants.C_UNKNOWN_ID;
                        } else {
                            month = Integer.parseInt(textValue);
                        }

                        textValue = cronjob.element("dayofweek").getText().trim();
                        if ("*".equals(textValue)) {
                            dayOfWeek = I_CmsConstants.C_UNKNOWN_ID;
                        } else {
                            dayOfWeek = Integer.parseInt(textValue);
                        }

                        userName = cronjob.element("user").getText().trim();
                        groupName = cronjob.element("group").getText().trim();
                        className = cronjob.element("class").getText().trim();

                        params = cronjob.element("params").getText().trim();
                        if ("".equals(params)) {
                            params = null;
                        }

                        cronEntry = new CmsCronEntry(min, hour, dayOfWeek, month, dayOfMonth, userName, groupName, className, params);
                        m_cronEntries.add(cronEntry);
                    } catch (Exception e) {
                        if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                            org.opencms.main.OpenCms.getLog(this).error("Error reading cronjob in registry.xml: " + cronjob.toString(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (org.opencms.main.OpenCms.getLog(this).isErrorEnabled()) {
                org.opencms.main.OpenCms.getLog(this).error("Error reading crontab in registry.xml", e);
            }
        }
    }

    /**
     * Updates the table with the new values.<p>
     * 
     * @param table - the String to get the new values from.
     * @throws IOException if the reader couldn't be read
     */
    public void update(String table) throws IOException {
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
