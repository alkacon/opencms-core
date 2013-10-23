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

package org.opencms.ade.configuration.formatters;

import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsFormatterBean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Represents the currently cached collection of all formatter beans extracted from formatter configuration files.<p>
 * 
 * Objects of this class are immutable, but have a method to create an updated copy.<p>
 */
public class CmsFormatterConfigurationCacheState {

    /** Map of formatter beans by structure id. */
    private Map<CmsUUID, CmsFormatterBean> m_formatters = new HashMap<CmsUUID, CmsFormatterBean>();

    /** The map of formatters by resource type. */
    private Multimap<String, CmsFormatterBean> m_formattersByType;

    /**
     * Creates a new instance.<p>
     * 
     * @param formatters the initial map of formatters
     */
    public CmsFormatterConfigurationCacheState(Map<CmsUUID, CmsFormatterBean> formatters) {

        m_formatters = new HashMap<CmsUUID, CmsFormatterBean>(formatters);
    }

    /**
     * Creates a new copy of this state in which some entries are removed or replaced.<p>
     * 
     * This does not change the state object on which the method is called.
     * 
     * @param updateFormatters a map of formatters to change, where the key is the structure id and the value is either the replacement or null if the map entry should be removed
     *  
     * @return the updated copy 
     */
    public CmsFormatterConfigurationCacheState createUpdatedCopy(Map<CmsUUID, CmsFormatterBean> updateFormatters) {

        Map<CmsUUID, CmsFormatterBean> newFormatters = Maps.newHashMap(getFormatters());
        for (Map.Entry<CmsUUID, CmsFormatterBean> entry : updateFormatters.entrySet()) {
            CmsUUID key = entry.getKey();
            CmsFormatterBean value = entry.getValue();
            if (value != null) {
                newFormatters.put(key, value);
            } else {
                newFormatters.remove(key);
            }
        }
        return new CmsFormatterConfigurationCacheState(newFormatters);
    }

    /**
     * Gets the map of all formatters.<p>
     * 
     * @return the map of all formatters 
     */
    public Map<CmsUUID, CmsFormatterBean> getFormatters() {

        return Collections.unmodifiableMap(m_formatters);
    }

    /**
     * Gets the formatters for a specific resource types, and optionally only returns those which are automatically enabled.<p>
     * 
     * @param resourceType the resource type name 
     * @param filterAutoEnabled true if only the automatically enabled formatters should be returned
     *  
     * @return the formatters for the type 
     */
    public Collection<CmsFormatterBean> getFormattersForType(String resourceType, boolean filterAutoEnabled) {

        Collection<CmsFormatterBean> result = getFormattersByType().get(resourceType);
        if (filterAutoEnabled) {
            result = Collections2.filter(result, new Predicate<CmsFormatterBean>() {

                public boolean apply(CmsFormatterBean formatter) {

                    return formatter.isAutoEnabled();
                }
            });
        }
        return result;
    }

    /**
     * Gets the formatters as a multimap with the resource types as keys and caches this multimap if necessary.<p>
     * 
     * @return the multimap of formatters by resource type 
     */
    private Multimap<String, CmsFormatterBean> getFormattersByType() {

        if (m_formattersByType == null) {
            ArrayListMultimap<String, CmsFormatterBean> formattersByType = ArrayListMultimap.create();
            for (CmsFormatterBean formatter : m_formatters.values()) {
                formattersByType.put(formatter.getResourceTypeName(), formatter);
            }
            m_formattersByType = formattersByType;
        }
        Multimap<String, CmsFormatterBean> result = Multimaps.unmodifiableMultimap(m_formattersByType);
        return result;
    }

}
