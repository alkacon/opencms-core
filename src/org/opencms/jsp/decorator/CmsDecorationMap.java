/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.decorator;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * The CmsDecorationMap is the object representation of a single decoartion file.<p>
 * 
 * The semicolon seperated elements of the decoartion file are stored in a map. <p>
 * The map uses the decoration as keys and CmsDecorationObjects as values.<p>
 * Multiple CmsDecorationMaps form a CmsDecorationBundle.
 * 
 * @since 6.1.3 
 */
public class CmsDecorationMap implements Comparable {

    /** The seperator for the CSV file. */
    public static final String CSV_SEPERATOR = "|";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDecorationMap.class);

    /** The map to store all elements in. */
    private Map m_decorationMap;

    /** The decorator defintion to be used for this decoration map. */
    private CmsDecorationDefintion m_decoratorDefinition;

    /**  The locale of this decoration map. */
    private Locale m_locale;

    /** The name of the decoration map. */
    private String m_name;

    /**
     * Constructor, creates a new, empty CmsDecorationMap.<p>
     * 
     * @param decDef the CmsDecorationDefintion to be used in this decoration map
     * @param name The name of the decoration map
     * @param locale the locale for this decoration map
     */
    public CmsDecorationMap(CmsDecorationDefintion decDef, String name, Locale locale) {

        m_decoratorDefinition = decDef;
        m_name = name;
        m_locale = locale;
        m_decorationMap = new HashMap();
    }

    /**
     * Constructor, creates a new CmsDecorationMap.<p>
     * 
     * @param cms the CmsObject
     * @param res the resource to extrace the decorations from
     * @param decDef the CmsDecorationDefintion to be used in this decoration map
     * @throws CmsException if something goes wrong
     */
    public CmsDecorationMap(CmsObject cms, CmsResource res, CmsDecorationDefintion decDef)
    throws CmsException {

        m_decoratorDefinition = decDef;
        m_name = res.getName();
        m_locale = extractLocale();
        m_decorationMap = fillMap(cms, res);

    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        int compValue = 0;
        if (o instanceof CmsDecorationMap) {
            compValue = m_name.compareTo(((CmsDecorationMap)o).getName());
        }
        return compValue;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsDecorationMap) {
            return ((CmsDecorationMap)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns the decorationMap.<p>
     *
     * @return the decorationMap
     */
    public Map getDecorationMap() {

        return m_decorationMap;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Sets the decorationMap.<p>
     *
     * @param decorationMap the decorationMap to set
     */
    public void setDecorationMap(Map decorationMap) {

        m_decorationMap = decorationMap;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getName());
        buf.append(" [name = '");
        buf.append(m_name);
        buf.append("' locale=");
        buf.append(m_locale);
        buf.append("' mapsize=");
        buf.append(m_decorationMap.size());
        buf.append("]");
        return buf.toString();
    }

    /**
     * Extracts the locale from the decoration filename.<p>
     * 
     *@return locale extraced form filename or null
     */
    private Locale extractLocale() {

        Locale loc = null;
        int underscore = m_name.lastIndexOf("_");
        if (underscore > -1) {
            String localeName = m_name.substring(underscore + 1);
            if (localeName.lastIndexOf(".") > -1) {
                localeName = localeName.substring(0, localeName.lastIndexOf("."));
            }
            loc = CmsLocaleManager.getLocale(localeName);
        }

        return loc;
    }

    /**
     *  Fills the decoration map with values from the decoation file.<p>
     *  
     * @param cms the CmsObject
     * @param res the decoration file
     * @return decoration map, using decoration as key and decoration description as value
     * @throws CmsException if something goes wrong
     */
    private Map fillMap(CmsObject cms, CmsResource res) throws CmsException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DECORATION_MAP_FILL_MAP_2,
                m_name,
                m_decoratorDefinition));
        }

        Map decMap = new HashMap();
        // upgrade the resource to get the file content
        CmsFile file = cms.readFile(res);
        // get all the entries
        String unparsedContent = new String(file.getContents());

        String delimiter = "\r\n";
        if (unparsedContent.indexOf(delimiter) == -1) {
            // there was no \r\n delimiter in the csv file, so check if the lines are seperated by
            // \n only
            if (unparsedContent.indexOf("\n") > -1) {
                delimiter = "\n";
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DECORATION_MAP_FILL_MAP_DELIMITER_2,
                res.getName(),
                CmsStringUtil.escapeJavaScript(delimiter)));
        }

        List entries = CmsStringUtil.splitAsList(unparsedContent, delimiter);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DECORATION_MAP_FILL_MAP_SPLIT_LIST_2,
                res.getName(),
                entries));
        }
        Iterator i = entries.iterator();
        while (i.hasNext()) {
            try {
                String entry = (String)i.next();
                // extract key and value
                if (CmsStringUtil.isNotEmpty(entry)) {
                    int speratator = entry.indexOf(CSV_SEPERATOR);
                    if (speratator > -1) {
                        String key = entry.substring(0, speratator).trim();
                        String value = entry.substring(speratator + 1).trim();
                        if (CmsStringUtil.isNotEmpty(key) && CmsStringUtil.isNotEmpty(value)) {
                            CmsDecorationObject decObj = new CmsDecorationObject(
                                key,
                                value,
                                m_decoratorDefinition,
                                m_locale);
                            decMap.put(key, decObj);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(Messages.get().getBundle().key(
                                    Messages.LOG_DECORATION_MAP_ADD_DECORATION_OBJECT_2,
                                    decObj,
                                    key));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_DECORATION_MAP_FILL_2, m_name, e));
                }
            }
        }

        return decMap;
    }
}