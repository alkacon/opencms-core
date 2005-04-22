/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListItem.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic list item.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsListItem {

    /** Associated list. */
    private final CmsHtmlList m_list;

    /** Item values. */
    private final Map m_values = new HashMap();

    /** Unique id for later recovery. */
    private final String m_id;

    /**
     * Default Constructor.<p>
     * 
     * @param id the id of the item has to be unique
     * @param list the corresponding list
     */
    public CmsListItem(CmsHtmlList list, String id) {

        m_list = list;
        m_id = id;
    }

    /**
     * Returns the value of the column for this item.<p>
     * 
     * @param columnId the column id
     * 
     * @return the content, may be <code>null</code>
     */
    public Object get(String columnId) {

        if (m_list.getMetadata().getColumnDefinition(columnId) == null) {
            throw new IllegalArgumentException(Messages.get().key(Messages.ERR_LIST_INVALID_COLUMN_1, columnId));
        }
        return m_values.get(columnId);
    }

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public CmsHtmlList getList() {

        return m_list;
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
     * Sets the object to display at the given column.<p>
     * 
     * @param columnName the column name
     * @param value the value to display
     * 
     * @return the previous value, or <code>null</code> if unset
     */
    public Object set(String columnName, Object value) {

        if (m_list.getMetadata().getColumnDefinition(columnName) == null) {
            throw new IllegalArgumentException("unknown column id");
        }
        return m_values.put(columnName, value);
    }
}