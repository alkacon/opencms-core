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

package org.opencms.widgets;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_EDITOR_MESSAGE_NOSELECTION_0 = "ERR_EDITOR_MESSAGE_NOSELECTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MALFORMED_SELECT_OPTIONS_1 = "ERR_MALFORMED_SELECT_OPTIONS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MAP_DUPLICATE_KEY_3 = "ERR_MAP_DUPLICATE_KEY_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MAP_PARAMETER_FORM_1 = "ERR_MAP_PARAMETER_FORM_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_PROPERTY_2 = "ERR_NO_PROPERTY_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_WIDGET_DIALOG_0 = "ERR_NO_WIDGET_DIALOG_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_DATETIME_1 = "ERR_PARSE_DATETIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROPERTY_READ_2 = "ERR_PROPERTY_READ_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROPERTY_WRITE_3 = "ERR_PROPERTY_WRITE_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WIDGETCOLLECTOR_ADD_1 = "ERR_WIDGETCOLLECTOR_ADD_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_ANCHOR_0 = "GUI_BUTTON_ANCHOR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_COLOR_0 = "GUI_BUTTON_COLOR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_ERASE_0 = "GUI_BUTTON_ERASE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_LINKTO_0 = "GUI_BUTTON_LINKTO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_PREVIEW_0 = "GUI_BUTTON_PREVIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CATEGORY_SELECT_0 = "GUI_CATEGORY_SELECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_COLOR_TITLE_0 = "GUI_DIALOG_COLOR_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_BUTTON_DOWNLOADLIST_0 = "GUI_EDITOR_BUTTON_DOWNLOADLIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_BUTTON_HTMLLIST_0 = "GUI_EDITOR_BUTTON_HTMLLIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_BUTTON_IMAGELIST_0 = "GUI_EDITOR_BUTTON_IMAGELIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_BUTTON_LINKLIST_0 = "GUI_EDITOR_BUTTON_LINKLIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_BUTTON_TABLELIST_0 = "GUI_EDITOR_BUTTON_TABLELIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_LABEL_IMAGE_DESC_0 = "GUI_EDITOR_LABEL_IMAGE_DESC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_LABEL_IMAGE_FORMAT_0 = "GUI_EDITOR_LABEL_IMAGE_FORMAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_LABEL_IMAGE_PATH_0 = "GUI_EDITOR_LABEL_IMAGE_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_FALSE_0 = "GUI_LABEL_FALSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TRUE_0 = "GUI_LABEL_TRUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MULTISELECT_ACTIVATE_0 = "GUI_MULTISELECT_ACTIVATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1 = "LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_WIDGETCOLLECTOR_ADD_1 = "LOG_DEBUG_WIDGETCOLLECTOR_ADD_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_WIDGET_PLAINTEXT_EXTRACT_HTML_1 = "LOG_ERR_WIDGET_PLAINTEXT_EXTRACT_HTML_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_WIDGET_SELECTGROUP_PATTERN_1 = "LOG_ERR_WIDGET_SELECTGROUP_PATTERN_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.widgets.messages";

    /** Prefix to create button message key. */
    private static final String GUI_BUTTON_PREF = "GUI_EDITOR_BUTTON_";

    /** Postfix to create button message key. */
    private static final String GUI_BUTTON_SUF = "LIST_0";

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
     * Create button message key.
     * 
     * @param gallery name 
     * @return Button  message key as String
     */
    public static String getButtonName(String gallery) {

        StringBuffer sb = new StringBuffer(GUI_BUTTON_PREF);
        sb.append(gallery.toUpperCase());
        sb.append(GUI_BUTTON_SUF);
        return sb.toString();
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