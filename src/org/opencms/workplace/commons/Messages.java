/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Messages.java,v $
 * Date   : $Date: 2005/06/22 16:06:35 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.commons;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BUILDING_RESTYPE_LIST_1 = "ERR_BUILDING_RESTYPE_LIST_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHANGE_LINK_TARGET_0 = "ERR_CHANGE_LINK_TARGET_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COPY_ONTO_ITSELF_1 = "ERR_COPY_ONTO_ITSELF_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FILE_SIZE_TOO_LARGE_1 = "ERR_FILE_SIZE_TOO_LARGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_LINK_TARGET_1 = "ERR_GET_LINK_TARGET_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_RESTYPE_1 = "ERR_GET_RESTYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_NEW_PASS_0 = "ERR_INVALID_NEW_PASS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_OLD_PASS_0 = "ERR_INVALID_OLD_PASS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_PROP_0 = "ERR_INVALID_PROP_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_FAILED_TARGET_EXISTS_2 = "ERR_MOVE_FAILED_TARGET_EXISTS_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_ONTO_ITSELF_1 = "ERR_MOVE_ONTO_ITSELF_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_EXPIREDATE_1 = "ERR_PARSE_EXPIREDATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_RELEASEDATE_1 = "ERR_PARSE_RELEASEDATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_TIMESTAMP_1 = "ERR_PARSE_TIMESTAMP_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REDIRECT_XMLPAGE_DIALOG_1 = "ERR_REDIRECT_XMLPAGE_DIALOG_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1 = "ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_NOT_FOUND_0 = "ERR_UPLOAD_FILE_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_NOPART_1 = "GUI_COPYTOPROJECT_NOPART_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_PART_1 = "GUI_COPYTOPROJECT_PART_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2 = "GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_RESOURCES_0 = "GUI_COPYTOPROJECT_RESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_TITLE_0 = "GUI_COPYTOPROJECT_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_ADVANCED_NO_PROPDEFS_0 = "GUI_PROPERTY_ADVANCED_NO_PROPDEFS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_COMPUTING_PUBRES_FAILED_0 = "LOG_COMPUTING_PUBRES_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DISPLAY_UNLOCK_INF_FAILED_0 = "LOG_DISPLAY_UNLOCK_INF_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_INCLUDE_FAILED_1 = "LOG_ERROR_INCLUDE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_PROJECT_NAME_FAILED_0 = "LOG_SET_PROJECT_NAME_FAILED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.commons.messages";

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
