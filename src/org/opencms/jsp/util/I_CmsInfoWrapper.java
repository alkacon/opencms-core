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

/**
 * Contains common methods for various beans used to generate the template documentation.
 */
public interface I_CmsInfoWrapper {

    /**
     * Gets the description in the current locale.
     *
     * @return the locale to use
     */
    String getDescription();

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