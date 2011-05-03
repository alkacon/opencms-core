/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsFormatterConfiguration.java,v $
 * Date   : $Date: 2011/05/03 10:48:48 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Represents a formatter configuration.<p>
 * 
 * A formatter configuration can be either defined in the XML schema XSD of a XML content, 
 * or in a special sitemap configuration file.<p>
 * 
 * @author Georg Westenberger
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsFormatterConfiguration {

    /** The log instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsFormatterConfiguration.class);

    /** Indicates if this configuration has been frozen. */
    private boolean m_frozen;

    /** The formatters for different container types. */
    private Map<String, CmsFormatterBean> m_typeFormatters;

    /** The formatters for different widths. */
    private List<CmsFormatterBean> m_widthFormatters;

    /** 
     * Create a new formatter configuration.<p>
     */
    public CmsFormatterConfiguration() {

        m_typeFormatters = new HashMap<String, CmsFormatterBean>(4);
        m_widthFormatters = new ArrayList<CmsFormatterBean>(4);
    }

    /**
     * Adds a formatter to this configuration.<p>
     * 
     * @param formatter the formatter to add
     * 
     * @throws CmsConfigurationException in case this formatter configuration is already frozen
     */
    public void addFormatter(CmsFormatterBean formatter) throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_FORMATTER_CONFIG_FROZEN));
        }
        String oldUri = null;
        Object key;
        if (formatter.isTypeFormatter()) {

            String type = formatter.getType();
            key = type;
            CmsFormatterBean oldFormatter = m_typeFormatters.get(type);
            if (oldFormatter != null) {
                oldUri = oldFormatter.getJspRootPath();
            }
            m_typeFormatters.put(type, formatter);
        } else {

            Integer minWidth = Integer.valueOf(formatter.getMinWidth());
            key = minWidth;
            int old = m_widthFormatters.lastIndexOf(formatter);
            if (old >= 0) {
                oldUri = m_widthFormatters.remove(old).getJspRootPath();
            }
            m_widthFormatters.add(formatter);
        }
        if (oldUri != null) {
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_CONTENT_DEFINITION_DUPLICATE_FORMATTER_4,
                new Object[] {key, oldUri, formatter.getJspRootPath(), formatter.getLocation()}));
        }
    }

    /**
     * Freezes this configuration.<p>
     */
    public void freeze() {

        if (!m_frozen) {
            m_frozen = true;
            m_typeFormatters = Collections.unmodifiableMap(m_typeFormatters);
            m_widthFormatters = Collections.unmodifiableList(m_widthFormatters);
        }
    }

    /**
     * Selects the correct formatter based the provided type and width from this configuration.<p>
     * 
     * This method first tries to find the formatter for the container type. 
     * If this fails, it returns the formatter with the largest width less than the 
     * container width; if the container width is negative, the formatter with the
     * largest width will be returned.
     * 
     * @param containerType the container type 
     * @param containerWidth the container width
     *  
     * @return the correct formatter, or null if none was found 
     */
    public CmsFormatterBean getFormatter(String containerType, int containerWidth) {

        CmsFormatterBean result = m_typeFormatters.get(containerType);
        if ((result == null) && (containerWidth > 0)) {
            // in case we don't have found a type and width info is set, check for width formatters
            CmsFormatterBean candidate = null;
            for (CmsFormatterBean f : m_widthFormatters) {
                // iterate all width containers and see if we have a fit
                if ((f.getMinWidth() <= containerWidth) && (containerWidth <= f.getMaxWidth())) {
                    // found a match
                    if ((candidate == null) || (candidate.getMinWidth() < f.getMinWidth())) {
                        candidate = f;
                    }
                }
            }
            if (candidate != null) {
                result = candidate;
            }
        }
        return result;
    }

    /**
     * Returns the provided <code>true</code> in case this configuration has a formatter 
     * for the given type / width parameters.<p>
     * 
     * @param containerType the container type 
     * @param containerWidth the container width
     *  
     * @return the provided <code>true</code> in case this configuration has a formatter 
     *      for the given type / width parameters.
     */
    public boolean hasFormatter(String containerType, int containerWidth) {

        return getFormatter(containerType, containerWidth) != null;
    }

    /**
     * Returns <code>true</code> in case there is at least one formatter configured in this configuration.<p>
     * 
     * @return <code>true</code> in case there is at least one formatter configured in this configuration
     */
    public boolean hasFormatters() {

        return (m_typeFormatters.size() > 0) || (m_widthFormatters.size() > 0);
    }

    /**
     * Indicates if this configuration has been frozen.<p>
     * 
     * @return <code>true</code> if this configuration has been frozen
     */
    public boolean isFrozen() {

        return m_frozen;
    }
}