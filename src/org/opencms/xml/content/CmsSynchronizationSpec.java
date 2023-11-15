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

package org.opencms.xml.content;

import org.opencms.xml.content.I_CmsXmlContentHandler.SynchronizationMode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

/**
 * A class representing the value synchronization configuration from a schema (or set of schemas).
 */
public class CmsSynchronizationSpec {

    /** Map from xpaths to synchronization modes. */
    private LinkedHashMap<String, SynchronizationMode> m_synchMap;

    /**
     * Creates a new instance based on a map of synchronization modes.
     * <p>Note the map given as argument is used as-is and not copied, so modifying it later may lead to problems.
     *
     * @param synchMap the map of synchronization modes
     */
    public CmsSynchronizationSpec(LinkedHashMap<String, SynchronizationMode> synchMap) {

        m_synchMap = synchMap;
    }

    /**
     * Gets an immutable map view of the synchronizations mode by xpath.
     *
     * @return a map from xpaths to synchronization modes
     */
    public Map<String, SynchronizationMode> asMap() {

        return Collections.unmodifiableMap(m_synchMap);
    }

    /**
     * Gets the set of paths for which synchronization is turned on in this configuration.
     *
     * @return the set of synchronization paths
     */
    public Set<String> getSynchronizationPaths() {

        return Collections.unmodifiableSet(
            Maps.filterEntries(m_synchMap, entry -> entry.getValue() != SynchronizationMode.none).keySet());

    }

}
