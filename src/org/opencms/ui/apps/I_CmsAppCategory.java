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

import java.util.Locale;

/**
 * App category interface.<p>
 */
public interface I_CmsAppCategory {

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the user readable name for the category for a given locale.<p>
     *
     * @param locale the locale for which to get the name
     *
     * @return the localized name
     */
    String getName(Locale locale);

    /**
     * Returns the order.<p>
     *
     * App categories are sorted by the order, i.e., categories with higher order numbers
     * are shown below the ones with lower order numbers.<p>
     *
     * @return the order
     */
    int getOrder();

    /**
     * Returns the parentId.<p>
     *
     * App categories can be nested. The method must return the id of the parent category
     * if the category is nested. If it is on top level, <code>null</code> should be
     * returned.<p>
     *
     * @return the parentId
     */
    String getParentId();

    /**
     * Returns the priority.<p>
     *
     * If more than one category with the same id is present, the one with higher priority
     * is shown. The other categories with this id are discarded.<p>
     *
     * @return the priority
     */
    int getPriority();

}