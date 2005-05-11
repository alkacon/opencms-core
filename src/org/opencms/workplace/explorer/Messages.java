/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/Messages.java,v $
 * Date   : $Date: 2005/05/11 15:24:21 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.explorer;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @since 5.9.1
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATE_FOLDER_0 = "ERR_CREATE_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATE_LINK_0 = "ERR_CREATE_LINK_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CSV_XML_TRANSFORMATION_FAILED_0 = "ERR_CSV_XML_TRANSFORMATION_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_TABLE_IMPORT_FAILED_0 = "ERR_TABLE_IMPORT_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_NOT_FOUND_0 = "ERR_UPLOAD_FILE_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1 = "ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_ACCESS_ENTRY_2 = "LOG_ADD_ACCESS_ENTRY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_MENU_ENTRY_2 = "LOG_ADD_MENU_ENTRY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_MENU_SEPARATOR_1 = "LOG_ADD_MENU_SEPARATOR_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_PROP_1 = "LOG_ADD_PROP_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_CONTEXT_MENU_1 = "LOG_CREATE_CONTEXT_MENU_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_READ_GROUPS_OF_USER_FAILED_1 = "LOG_READ_GROUPS_OF_USER_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_REDIRECT_XMLPAGE_FAILED_1 = "LOG_REDIRECT_XMLPAGE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_AUTO_NAV_1 = "LOG_SET_AUTO_NAV_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_AUTO_TITLE_1 = "LOG_SET_AUTO_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_ICON_1 = "LOG_SET_ICON_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_KEY_1 = "LOG_SET_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NAME_1 = "LOG_SET_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NEW_RESOURCE_ORDER_1 = "LOG_SET_NEW_RESOURCE_ORDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NEW_RESOURCE_URI_1 = "LOG_SET_NEW_RESOURCE_URI_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_PROP_DEFAULTS_2 = "LOG_SET_PROP_DEFAULTS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_REFERENCE_1 = "LOG_SET_REFERENCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WRONG_MENU_SEP_ORDER_0 = "LOG_WRONG_MENU_SEP_ORDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WRONG_ORDER_CONTEXT_MENU_1 = "LOG_WRONG_ORDER_CONTEXT_MENU_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.explorer.messages";

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
