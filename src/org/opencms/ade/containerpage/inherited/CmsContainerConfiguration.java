/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean representing a single configuration entry for the inherited container configuration.<p>
 *
 */
public class CmsContainerConfiguration {

    /** Node name. **/
    public static final String N_CONFIGURATION = "Configuration";

    /** Node name. **/
    public static final String N_ELEMENT = "Element";

    /** Node name. **/
    public static final String N_HIDDEN = "Hidden";

    /** Node name. **/
    public static final String N_KEY = "Key";

    /** Node name. **/
    public static final String N_NAME = "Name";

    /** Node name. **/
    public static final String N_NEWELEMENT = "NewElement";

    /** Node name. **/
    public static final String N_ORDERKEY = "OrderKey";

    /** Node name. **/
    public static final String N_URI = "Uri";

    /** Node name. **/
    public static final String N_VISIBLE = "Visible";

    /** A map containing the new elements. */
    private Map<String, CmsContainerElementBean> m_newElements;

    /** A list of keys for a new ordering of elements. **/
    private List<String> m_ordering;

    /** The path from which the configuration was fetched. */
    private String m_path;

    /** A map from element keys to Booleans, representing hidden/shown elements. */
    private Map<String, Boolean> m_visibility;

    /**
     * Creates a new instance.<p>
     *
     * @param ordering the new ordering list
     * @param visibility the visibility map
     * @param newElements the new elements
     */
    public CmsContainerConfiguration(
        List<String> ordering,
        Map<String, Boolean> visibility,
        Map<String, CmsContainerElementBean> newElements) {

        m_ordering = ordering != null ? Collections.unmodifiableList(ordering) : null;
        m_visibility = Collections.unmodifiableMap(visibility);
        m_newElements = Collections.unmodifiableMap(newElements);
    }

    /**
     * Generates an empty configuration object.<p>
     *
     * @return an empty configuration object
     */
    public static CmsContainerConfiguration emptyConfiguration() {

        return new CmsContainerConfiguration(
            null,
            new HashMap<String, Boolean>(),
            new HashMap<String, CmsContainerElementBean>());
    }

    /**
     * Gets the map of new elements.<p>
     *
     * @return the map of new elements
     */
    public Map<String, CmsContainerElementBean> getNewElements() {

        return m_newElements;
    }

    /**
     * Gets the new elements in the order in which they appear in the 'ordering' list.<p>
     *
     * @return an ordered map containing the new elements in the correct order
     */
    public LinkedHashMap<String, CmsContainerElementBean> getNewElementsInOrder() {

        LinkedHashMap<String, CmsContainerElementBean> result = new LinkedHashMap<String, CmsContainerElementBean>();
        if (m_ordering != null) {
            for (String orderKey : m_ordering) {
                CmsContainerElementBean element = m_newElements.get(orderKey);
                if (element != null) {
                    result.put(orderKey, element);
                }
            }
            return result;
        }
        return new LinkedHashMap<String, CmsContainerElementBean>();
    }

    /**
     * Gets the ordering list.<p>
     *
     * @return the ordering list
     */
    public List<String> getOrdering() {

        return m_ordering;
    }

    /**
     * Gets the path from which this configuration was read.<p>
     *
     * @return the path from which this configuration was read
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Gets the visibility map for this configuration.<p>
     *
     * @return the visibility map
     */
    public Map<String, Boolean> getVisibility() {

        return m_visibility;
    }

    /**
     * Sets the path for this configuration.<p>
     *
     * @param path the new path value
     */
    public void setPath(String path) {

        m_path = path;
    }

}
