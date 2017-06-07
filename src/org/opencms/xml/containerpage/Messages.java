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

package org.opencms.xml.containerpage;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIG_MALFORMED_TYPE_0 = "ERR_CONFIG_MALFORMED_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIG_NOT_FOUND_3 = "ERR_CONFIG_NOT_FOUND_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIG_NOT_SET_2 = "ERR_CONFIG_NOT_SET_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIG_WRONG_TYPE_2 = "ERR_CONFIG_WRONG_TYPE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DUPLICATE_NAME_1 = "ERR_DUPLICATE_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ELEMENT_GROUP_REFERENCES_ANOTHER_GROUP_2 = "ERR_ELEMENT_GROUP_REFERENCES_ANOTHER_GROUP_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FORMATTER_JSP_DONT_EXIST_1 = "ERR_FORMATTER_JSP_DONT_EXIST_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_ID_1 = "ERR_INVALID_ID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_JSON_EXCEPTION_1 = "ERR_JSON_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_TYPE_CONFIG_1 = "ERR_NO_TYPE_CONFIG_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READ_ELEMENT_PROPERTY_CONFIGURATION_1 = "ERR_READ_ELEMENT_PROPERTY_CONFIGURATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READING_ELEMENT_FROM_REQUEST_0 = "ERR_READING_ELEMENT_FROM_REQUEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READING_FORMATTER_INFO_FROM_REQUEST_0 = "ERR_READING_FORMATTER_INFO_FROM_REQUEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2 = "LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CONTAINER_PAGE_NOT_FOUND_1 = "LOG_CONTAINER_PAGE_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CONTENT_DEFINITION_DUPLICATE_FORMATTER_4 = "LOG_CONTENT_DEFINITION_DUPLICATE_FORMATTER_4";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_MATCHED_OFFLINE_2 = "LOG_DEBUG_CACHE_MATCHED_OFFLINE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_MATCHED_ONLINE_2 = "LOG_DEBUG_CACHE_MATCHED_ONLINE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_MISSED_OFFLINE_1 = "LOG_DEBUG_CACHE_MISSED_OFFLINE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_MISSED_ONLINE_1 = "LOG_DEBUG_CACHE_MISSED_ONLINE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_SET_OFFLINE_2 = "LOG_DEBUG_CACHE_SET_OFFLINE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_CACHE_SET_ONLINE_2 = "LOG_DEBUG_CACHE_SET_ONLINE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WARN_ELEMENT_GROUP_INSIDE_ELEMENT_GROUP_0 = "LOG_WARN_ELEMENT_GROUP_INSIDE_ELEMENT_GROUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WARN_NO_FORMATTERS_DEFINED_1 = "LOG_WARN_NO_FORMATTERS_DEFINED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WARN_UNCACHE_NULL_0 = "LOG_WARN_UNCACHE_NULL_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.xml.containerpage.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /** Message constant for key in the resource bundle .*/
    public static final String GUI_REDIRECT_TITLE_1 = "GUI_REDIRECT_TITLE_1";

    /** Message constant for key in the resource bundle .*/
    public static final String GUI_REDIRECT_SUBLEVEL_TITLE_0 = "GUI_REDIRECT_SUBLEVEL_TITLE_0";

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}