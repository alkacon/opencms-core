/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/adeconfig/Attic/CmsTypeFormatterConfiguration.java,v $
 * Date   : $Date: 2011/04/05 06:41:19 $
 * Version: $Revision: 1.2 $
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

package org.opencms.adeconfig;

import org.opencms.util.CmsPair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A bean which represents the formatter configuration for a single resource type.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsTypeFormatterConfiguration {

    /** The formatters for different container types. */
    private Map<String, String> m_containerTypeFormatters;

    /** The formatters for different widths. */
    private Map<Integer, CmsPair<String, Integer>> m_widthFormatters;

    /**
     * Creates a new instance.<p>
     * 
     * @param containerTypeFormatters the formatters for different container types 
     * 
     * @param widthFormatters the formatters for different widths 
     */
    public CmsTypeFormatterConfiguration(
        Map<String, String> containerTypeFormatters,
        Map<Integer, CmsPair<String, Integer>> widthFormatters) {

        m_containerTypeFormatters = containerTypeFormatters;
        m_widthFormatters = widthFormatters;
    }

    /**
     * Returns the map of formatters for different container types.<p>
     * 
     * @return the map of formatters for different container types, with the types as keys
     */
    public Map<String, String> getContainerTypeFormatters() {

        return Collections.unmodifiableMap(m_containerTypeFormatters);
    }

    /**
     * Returns the map of formatters for different widths.<p>
     * 
     * @return the map of formatters for different widths, with the widths as key
     */
    public Map<Integer, CmsPair<String, Integer>> getWidthFormatters() {

        return Collections.unmodifiableMap(m_widthFormatters);
    }

    /**
     * Merges the type configuration with another one.<p>
     * 
     * @param otherConfig the other type formatter configuration 
     * 
     * @return the merged configuration 
     */
    public CmsTypeFormatterConfiguration merge(CmsTypeFormatterConfiguration otherConfig) {

        Map<String, String> containerTypeFormatters = new HashMap<String, String>();
        Map<Integer, CmsPair<String, Integer>> widthFormatters = Maps.newHashMap();
        containerTypeFormatters.putAll(m_containerTypeFormatters);
        containerTypeFormatters.putAll(otherConfig.m_containerTypeFormatters);

        widthFormatters.putAll(m_widthFormatters);
        widthFormatters.putAll(otherConfig.m_widthFormatters);
        return new CmsTypeFormatterConfiguration(containerTypeFormatters, widthFormatters);

    }

}
