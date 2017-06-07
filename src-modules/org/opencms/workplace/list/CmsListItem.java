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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.main.CmsIllegalArgumentException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic list item.<p>
 *
 * @since 6.0.0
 */
public class CmsListItem {

    /** Unique id for later recovery. */
    private final String m_id;

    /** Associated list definition. */
    private final CmsListMetadata m_metadata;

    /** Item values. */
    private final Map<String, Object> m_values = new HashMap<String, Object>();

    /**
     * Default Constructor.<p>
     *
     * @param id the id of the item has to be unique
     * @param metadata the corresponding list definition
     */
    public CmsListItem(CmsListMetadata metadata, String id) {

        if (id == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_LIST_INVALID_NULL_ARG_1, "id"));
        }
        if (metadata == null) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_LIST_INVALID_NULL_ARG_1, "metadata"));
        }
        m_metadata = metadata;
        m_id = id;
    }

    /**
     * Returns the value of the column for this item.<p>
     *
     * @param columnId the column id
     *
     * @return the content, may be <code>null</code>
     *
     * @throws CmsIllegalArgumentException if the given <code>columnId</code> is invalid
     */
    public Object get(String columnId) throws CmsIllegalArgumentException {

        if ((getMetadata().getColumnDefinition(columnId) == null)
            && (getMetadata().getItemDetailDefinition(columnId) == null)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_LIST_INVALID_COLUMN_1, columnId));
        }
        return m_values.get(columnId);
    }

    /**
     * Returns the id of the item.<p>
     *
     * @return the id
     *
     * @see CmsHtmlList#getItem(String)
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the meta data.<p>
     *
     * @return the meta data
     */
    public CmsListMetadata getMetadata() {

        return m_metadata;
    }

    /**
     * Returns the value map of the list item.<p>
     *
     * @return the value map of the list item
     */
    public Map<String, Object> getValues() {

        return Collections.unmodifiableMap(m_values);
    }

    /**
     * Sets the object to display at the given column.<p>
     *
     * @param columnId the column id
     * @param value the value to display
     *
     * @return the previous value, or <code>null</code> if not set
     * @throws CmsIllegalArgumentException if the given <code>columnId</code> is invalid
     *
     */
    public Object set(String columnId, Object value) throws CmsIllegalArgumentException {

        if ((getMetadata().getColumnDefinition(columnId) == null)
            && (getMetadata().getItemDetailDefinition(columnId) == null)) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_LIST_INVALID_COLUMN_1, columnId));
        }
        return m_values.put(columnId, value);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_id + ":" + m_values.toString();
    }
}