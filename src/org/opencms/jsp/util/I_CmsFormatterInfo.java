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

package org.opencms.jsp.util;

import java.util.Locale;

/**
 * Common interface for accessing formatter / resource type infos in JSPs.
 */
public interface I_CmsFormatterInfo {

    /**
     * Gets the description in the given locale.
     *
     * @param l the locale to use
     * @return the description
     */
    String description(Locale l);

    /**
     * Gets the description in the current locale.
     *
     * @return the locale to use
     */
    String getDescription();

    /**
     * Checks if this wraps a resource type.
     *
     * @return true if this is a resource type
     */
    boolean getIsResourceType();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the nice name for the current locale.
     *
     * @return the nice name for the current locale
     */
    String getNiceName();

    /**
     * Checks if this is active.
     *
     * @return true if this is active
     */
    boolean isActive();

    /**
     * Checks if this wraps a (non-function) formatter.
     *
     * @return true if this wraps a normal formatter
     */
    boolean isFormatter();

    /**
     * Checks if this wraps a dynamic function.
     *
     * @return true if this wraps a dynamic function
     */
    boolean isFunction();

    /**
     * Gets the nice name in the given locale.
     *
     * @param l the locale to use
     * @return the nice name for the locale
     */
    String niceName(Locale l);

}
