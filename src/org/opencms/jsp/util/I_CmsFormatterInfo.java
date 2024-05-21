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
public interface I_CmsFormatterInfo extends I_CmsInfoWrapper {

    /**
     * Mode which controls whether / how localized strings should be resolved.
     */
    enum ResolveMode {
        /** Fully resolve localized string. */
        text,

        /** Tries to get the localization key itself. */
        key,

        /** Get the whole raw configured string including macro characters. */
        raw;
    }

    /**
     * Gets the description in the given locale.
     *
     * @param l the locale to use
     * @return the description
     */
    String description(Locale l);

    /**
     * Checks if this is active.
     *
     * @return true if this is active
     */
    boolean getIsActive();

    /**
     * Checks if this wraps a (non-function) formatter.
     *
     * @return true if this wraps a normal formatter
     */
    boolean getIsFormatter();

    /**
     * Checks if this wraps a dynamic function.
     *
     * @return true if this wraps a dynamic function
     */
    boolean getIsFunction();

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
     * Gets the localization key for the nice name, if one was used, or null otherwise.
     *
     * @return the localization key
     */
    String getNiceNameKey();

    /**
     * Gets the raw nice name, without resolving any macros.
     *
     * @return the raw nice name
     */
    String getNiceNameRaw();

    /**
     * Gets the nice name in the given locale.
     *
     * @param l the locale to use
     * @return the nice name for the locale
     */
    String niceName(Locale l);

}
