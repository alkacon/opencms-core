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

package org.opencms.ui.apps;

import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.Locale;

/**
 * Bean representing a category for workplace apps.<p>
 *
 * App categories should have a unique id. Nesting of categories is defined by the parent category id of each category.
 * A category whose parent category id is null will be displayed at the root level, and similarly, a workplace app whose
 * category id is null will be displayed at the root level.
 *
 */
public class CmsAppCategory implements I_CmsAppCategory {

    /** Prefix for message bundle keys used to localize app categories. */
    private static final String MESSAGE_PREFIX = "appcategory.";

    /** Sort key for the category. */
    private int m_order;

    /** Category id. */
    private String m_id;

    /** Parent category id. */
    private String m_parentId;

    /** Priority number; categories with higher priorities can override those with lower priorities. */
    private int m_priority;

    /**
     * Creates a new instance.<p>
     *
     * @param id the category id
     * @param parentId the parent category id
     * @param order the order
     * @param priority the priority
     */
    public CmsAppCategory(String id, String parentId, int order, int priority) {

        super();
        m_id = id;
        m_parentId = parentId;
        m_order = order;
        m_priority = priority;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        CmsWorkplaceMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
        String niceName = messages.keyDefault(MESSAGE_PREFIX + m_id, m_id);
        return niceName;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getParentId()
     */
    public String getParentId() {

        return m_parentId;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getPriority()
     */
    public int getPriority() {

        return m_priority;
    }
}
