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

package org.opencms.jsp.search.config.parser;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 10.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MANDATORY_HIGHLIGHTING_FIELD_MISSING_0 = "ERR_MANDATORY_HIGHLIGHTING_FIELD_MISSING_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ADDITIONAL_PARAMETER_CONFIG_WRONG_0 = "ERR_ADDITIONAL_PARAMETER_CONFIG_WRONG_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADDITIONAL_PARAMETER_CONFIG_NOT_PARSED_0 = "LOG_ADDITIONAL_PARAMETER_CONFIG_NOT_PARSED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FIELD_FACET_MANDATORY_KEY_MISSING_1 = "ERR_FIELD_FACET_MANDATORY_KEY_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RANGE_FACET_MANDATORY_KEY_MISSING_1 = "ERR_RANGE_FACET_MANDATORY_KEY_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_QUERY_FACET_MANDATORY_KEY_MISSING_1 = "ERR_QUERY_FACET_MANDATORY_KEY_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_OTHER_OPTION_1 = "ERR_INVALID_OTHER_OPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_OPTIONAL_BOOLEAN_MISSING_1 = "LOG_OPTIONAL_BOOLEAN_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_OPTIONAL_INTEGER_MISSING_1 = "LOG_OPTIONAL_INTEGER_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_CORE_SPECIFIED_0 = "LOG_NO_CORE_SPECIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_INDEX_SPECIFIED_0 = "LOG_NO_INDEX_SPECIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SORT_OPTION_NOT_PARSABLE_1 = "ERR_SORT_OPTION_NOT_PARSABLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_FACET_CONFIG_0 = "LOG_NO_FACET_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_HIGHLIGHTING_CONFIG_0 = "LOG_NO_HIGHLIGHTING_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_OPTIONAL_STRING_MISSING_1 = "LOG_OPTIONAL_STRING_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_EXTRA_PARAMETERS_0 = "LOG_NO_EXTRA_PARAMETERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_PAGESIZE_SPECIFIED_0 = "LOG_NO_PAGESIZE_SPECIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_SORT_CONFIG_0 = "LOG_NO_SORT_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_OPTIONAL_STRING_LIST_MISSING_1 = "LOG_OPTIONAL_STRING_LIST_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_OPTIONAL_STRING_ENTRY_UNPARSABLE_1 = "LOG_OPTIONAL_STRING_ENTRY_UNPARSABLE_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.jsp.search.config.parser";

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
