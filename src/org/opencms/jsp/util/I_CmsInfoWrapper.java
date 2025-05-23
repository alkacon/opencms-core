/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.i18n.CmsLocaleManager;

import java.util.Locale;

/**
 * Contains common methods for various beans used to generate the template documentation.
 */
public interface I_CmsInfoWrapper {

    /**
     * Gets the description in the current locale.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the description in the given locale.
     *
     * @param locale the locale to use
     * @return the description
     */
    String getDescription(Locale locale);

    /**
     * Gets the description in the given locale.
     *
     * @param locale the locale to use
     * @return the description
     */
    default String getDescription(String locale) {

        return getDescription(CmsLocaleManager.getLocale(locale));
    }

    /**
     * Returns the localization key for the description if one was used, and null otherwise.
     *
     * @return the localization key
     */
    String getDescriptionKey();

    /**
     * Gets the raw description, without resolving any macros.
     *
     * @return the raw description
     */
    String getDescriptionRaw();

}