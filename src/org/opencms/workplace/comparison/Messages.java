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

package org.opencms.workplace.comparison;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ADDED_0 = "GUI_COMPARE_ADDED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ATTRIBUTES_0 = "GUI_COMPARE_ATTRIBUTES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_CHANGED_0 = "GUI_COMPARE_CHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_ICON_0 = "GUI_COMPARE_COLS_ICON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_LOCALE_0 = "GUI_COMPARE_COLS_LOCALE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_NAME_0 = "GUI_COMPARE_COLS_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_PROPERTY_NAME_0 = "GUI_COMPARE_COLS_PROPERTY_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_STATUS_0 = "GUI_COMPARE_COLS_STATUS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COLS_TYPE_0 = "GUI_COMPARE_COLS_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_COMPARE_ALL_0 = "GUI_COMPARE_COMPARE_ALL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_CONTENT_0 = "GUI_COMPARE_CONTENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ELEM_ADDED_0 = "GUI_COMPARE_ELEM_ADDED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ELEM_CHANGED_0 = "GUI_COMPARE_ELEM_CHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ELEM_REMOVED_0 = "GUI_COMPARE_ELEM_REMOVED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_ELEM_UNCHANGED_0 = "GUI_COMPARE_ELEM_UNCHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_HIDE_IDENTICAL_ATTRIBUTES_0 = "GUI_COMPARE_HIDE_IDENTICAL_ATTRIBUTES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_HIDE_IDENTICAL_ELEMENTS_0 = "GUI_COMPARE_HIDE_IDENTICAL_ELEMENTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_HIDE_IDENTICAL_PROPERTIES_0 = "GUI_COMPARE_HIDE_IDENTICAL_PROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_IDENTICAL_TEXT_DIFFERENT_HTML_0 = "GUI_COMPARE_IDENTICAL_TEXT_DIFFERENT_HTML_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_PROPERTIES_0 = "GUI_COMPARE_PROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_REMOVED_0 = "GUI_COMPARE_REMOVED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_SHOW_ALL_ATTRIBUTES_0 = "GUI_COMPARE_SHOW_ALL_ATTRIBUTES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_SHOW_ALL_ELEMENTS_0 = "GUI_COMPARE_SHOW_ALL_ELEMENTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_SHOW_ALL_PROPERTIES_0 = "GUI_COMPARE_SHOW_ALL_PROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_UNCHANGED_0 = "GUI_COMPARE_UNCHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_VERSION_1 = "GUI_COMPARE_VERSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMPARE_VIEW_VERSION_1 = "GUI_COMPARE_VIEW_VERSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIFF_MODE_ALL_NAME_0 = "GUI_DIFF_MODE_ALL_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIFF_MODE_DIFFONLY_NAME_0 = "GUI_DIFF_MODE_DIFFONLY_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIFF_MODE_HTML_0 = "GUI_DIFF_MODE_HTML_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIFF_MODE_TEXT_0 = "GUI_DIFF_MODE_TEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIFF_SKIP_LINES_1 = "GUI_DIFF_SKIP_LINES_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.comparison.messages";

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
