/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/Messages.java,v $
 * Date   : $Date: 2006/03/23 08:44:36 $
 * Version: $Revision: 1.8.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * @version $Revision: 1.8.2.3 $ 
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
    public static final String LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1 = "LOG_CREATE_HTMLWIDGET_INSTANCE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_WIDGETCOLLECTOR_ADD_1 = "LOG_DEBUG_WIDGETCOLLECTOR_ADD_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.widgets.messages";

    /**Prefix to create button message key. */
    private static final String GUI_BUTTON_PREF = "GUI_EDITOR_BUTTON_";

    /**postfix to create button message key. */
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