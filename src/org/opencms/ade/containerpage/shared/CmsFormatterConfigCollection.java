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

package org.opencms.ade.containerpage.shared;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean which holds a list of formatter configurations (client-side) which can be retrieved by key or id.
 */
public class CmsFormatterConfigCollection implements Iterable<CmsFormatterConfig>, IsSerializable {

    /** The list of formatter beans. */
    private ArrayList<CmsFormatterConfig> m_formatters = new ArrayList<>();

    /**
     * Adds another formatter configuration.
     *
     * @param config the formatter configuration bean
     */
    public void add(CmsFormatterConfig config) {

        m_formatters.add(config);
    }

    /**
     * Gets the formatter configuration for the given key or id.
     *
     * @param keyOrId the formatter key or id
     * @return the first formatter configuration with that key or id, or null if none were found
     */
    public CmsFormatterConfig get(String keyOrId) {
        
        for (CmsFormatterConfig config : this) {
            if (keyOrId.equals(config.getKey()) || keyOrId.equals(config.getId())) {
                return config;
            }
        }
        return null;
    }

    /**
     * Gets the first formatter configuration bean.
     *
     * @return the first formatter configuration bean
     */
    public CmsFormatterConfig getFirstFormatter() {

        return m_formatters.iterator().next();
    }

    /**
     * Gets an iterator over the formatter beans.
     *
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<CmsFormatterConfig> iterator() {

        return m_formatters.iterator();
    }

    /**
     * Returns the number of formatters.
     *
     * @return the number of formatters
     */
    public int size() {

        return m_formatters.size();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        for (CmsFormatterConfig config : this) {
            result.append("[" + config.getKey() + "/" + config.getId() + "]\n");
        }
        return result.toString();
    }

}
