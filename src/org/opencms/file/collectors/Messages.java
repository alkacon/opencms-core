/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.collectors;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {
 
    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.file.collectors.messages";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_DEFAULTS_INVALID_2 = "ERR_COLLECTOR_DEFAULTS_INVALID_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_NAME_INVALID_1 = "ERR_COLLECTOR_NAME_INVALID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_DATE_FORMAT_SYNTAX_0 = "ERR_COLLECTOR_PARAM_DATE_FORMAT_SYNTAX_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_EMPTY_0 = "ERR_COLLECTOR_PARAM_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_INVALID_1 = "ERR_COLLECTOR_PARAM_INVALID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_KEY_VALUE_SYNTAX_1 = "ERR_COLLECTOR_PARAM_KEY_VALUE_SYNTAX_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_SINGLE_FILE_0 = "ERR_COLLECTOR_PARAM_SINGLE_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_USER_1 = "ERR_COLLECTOR_PARAM_USER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNKNOWN_RESTYPE_1 = "ERR_UNKNOWN_RESTYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COLLECTOR_PARAM_PROPERTY_NOT_FOUND_0 = "ERR_COLLECTOR_PARAM_PROPERTY_NOT_FOUND_0";
    
    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /** Message constant for key in the resource bundle. */
    public static final String LOG_RESOURCE_WITHOUT_NAVPROP_1 = "LOG_RESOURCE_WITHOUT_NAVPROP_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_RESTYPE_INTID_2 = "LOG_RESTYPE_INTID_2";

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