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

package org.opencms.jsp.util;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FILE_READ_1 = "ERR_FILE_READ_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_XML_CONTENT_UNMARSHAL_1 = "ERR_XML_CONTENT_UNMARSHAL_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CLOSE_0 = "GUI_CLOSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DETAILS_0 = "GUI_DETAILS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ERROR_0 = "GUI_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ERROR_UNKNOWN_0 = "GUI_ERROR_UNKNOWN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FORMATTER_RENDERING_ERROR_0 = "GUI_FORMATTER_RENDERING_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FORMATTER_RENDERING_MULTIPLE_ROOT_ELEMENTS_0 = "GUI_FORMATTER_RENDERING_MULTIPLE_ROOT_ELEMENTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FORMATTER_RENDERING_NOT_WELL_FORMED_0 = "GUI_FORMATTER_RENDERING_NOT_WELL_FORMED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FUNCTION_DEFAULT_HTML_0 = "GUI_FUNCTION_DEFAULT_HTML_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REASON_0 = "GUI_REASON_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.jsp.util.messages";

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