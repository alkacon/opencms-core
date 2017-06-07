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

package org.opencms.jsp.decorator;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.1.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_DEFINITION_CREATE_BUNDLE_2 = "LOG_DECORATION_DEFINITION_CREATE_BUNDLE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_DEFINITION_CREATE_MAP_2 = "LOG_DECORATION_DEFINITION_CREATE_MAP_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_DEFINITION_MAP_FILES_2 = "LOG_DECORATION_DEFINITION_MAP_FILES_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_DEFINITION_MAPS_2 = "LOG_DECORATION_DEFINITION_MAPS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_MAP_ADD_DECORATION_OBJECT_2 = "LOG_DECORATION_MAP_ADD_DECORATION_OBJECT_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_MAP_FILL_2 = "LOG_DECORATION_MAP_FILL_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_MAP_FILL_MAP_2 = "LOG_DECORATION_MAP_FILL_MAP_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_APPEND_TEXT_2 = "LOG_HTML_DECORATOR_APPEND_TEXT_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECODED_WORD_1 = "LOG_HTML_DECORATOR_DECODED_WORD_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECORATION_APPEND_DECORATION_1 = "LOG_HTML_DECORATOR_DECORATION_APPEND_DECORATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECORATION_APPEND_ORIGINALTEXT_1 = "LOG_HTML_DECORATOR_DECORATION_APPEND_ORIGINALTEXT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECORATION_APPEND_WORD_1 = "LOG_HTML_DECORATOR_DECORATION_APPEND_WORD_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECORATION_FOUND_2 = "LOG_HTML_DECORATOR_DECORATION_FOUND_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_DECORATION_FOUND_FWL_3 = "LOG_HTML_DECORATOR_DECORATION_FOUND_FWL_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_HTML_DECORATOR_PROCESS_WORD_2 = "LOG_HTML_DECORATOR_PROCESS_WORD_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_MAP_FILL_MAP_DELIMITER_2 = "LOG_DECORATION_MAP_FILL_MAP_DELIMITER_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DECORATION_MAP_FILL_MAP_SPLIT_LIST_2 = "LOG_DECORATION_MAP_FILL_MAP_SPLIT_LIST_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_CONFIG_MISSING_0 = "LOG_ERROR_CONFIG_MISSING_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.jsp.decorator.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

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
