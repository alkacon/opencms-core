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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.i18n.CmsMessageContainer;

import java.util.Locale;

/**
 * Provides localized Exception handling based on the OpenCms default locale.<p>
 *
 * Instances of this class are assumed to have full localized exception messages.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsThrowable {

    /**
     * Returns a localized exception message based on the OpenCms default locale.<p>
     *
     * @return a localized exception message based on the OpenCms default locale
     * @see Throwable#getLocalizedMessage()
     */
    String getLocalizedMessage();

    /**
     * Returns a localized exception message based on the given Locale.<p>
     *
     * @param locale the Locale to get the message for
     *
     * @return a localized exception message based on the given Locale
     */
    String getLocalizedMessage(Locale locale);

    /**
     * Returns a localized exception message based on the OpenCms default locale.<p>
     *
     * @return a localized exception message based on the OpenCms default locale
     * @see Throwable#getMessage()
     */
    String getMessage();

    /**
     * Returns the localized message container used to build this localized exception.<p>
     *
     * @return the localized message container used to build this localized exception
     */
    CmsMessageContainer getMessageContainer();
}
