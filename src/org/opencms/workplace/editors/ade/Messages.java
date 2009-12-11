/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/Messages.java,v $
 * Date   : $Date: 2009/12/11 08:27:48 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GENERATE_FORMATTED_ELEMENT_3 = "ERR_GENERATE_FORMATTED_ELEMENT_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_ID_1 = "ERR_INVALID_ID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_JSON_MISSING_PARAMETER_1 = "ERR_JSON_MISSING_PARAMETER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_JSON_WRONG_PARAMETER_VALUE_2 = "ERR_JSON_WRONG_PARAMETER_VALUE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SERVER_EXCEPTION_1 = "ERR_SERVER_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BROKEN_LINK_ONLINE_0 = "GUI_BROKEN_LINK_ONLINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_FILE_0 = "GUI_DEFAULT_FORMATTER_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_FILE_1 = "GUI_DEFAULT_FORMATTER_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_NEWNAME_0 = "GUI_DEFAULT_FORMATTER_NEWNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_NEWNAME_1 = "GUI_DEFAULT_FORMATTER_NEWNAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_TEXT_0 = "GUI_DEFAULT_FORMATTER_TEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_TYPE_0 = "GUI_DEFAULT_FORMATTER_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DEFAULT_FORMATTER_TYPE_1 = "GUI_DEFAULT_FORMATTER_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPNAME_DAY_1 = "GUI_GROUPNAME_DAY_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPNAME_EVERYTHING_ELSE_0 = "GUI_GROUPNAME_EVERYTHING_ELSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPNAME_SESSION_1 = "GUI_GROUPNAME_SESSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RELATED_RESOURCE_CAN_NOT_BE_PUBLISHED_0 = "GUI_RELATED_RESOURCE_CAN_NOT_BE_PUBLISHED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_LOCKED_BY_2 = "GUI_RESOURCE_LOCKED_BY_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_MISSING_ONLINE_0 = "GUI_RESOURCE_MISSING_ONLINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_NOT_ENOUGH_PERMISSIONS_0 = "GUI_RESOURCE_NOT_ENOUGH_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_PUBLISHED_BY_2 = "GUI_RESOURCE_PUBLISHED_BY_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TITLE_0 = "GUI_LABEL_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TYPE_0 = "GUI_LABEL_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_PATH_0 = "GUI_LABEL_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LAST_MODIFIED_0 = "GUI_LABEL_LAST_MODIFIED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.editors.ade.messages";

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