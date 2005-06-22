/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/Messages.java,v $
 * Date   : $Date: 2005/06/22 10:38:24 $
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

package org.opencms.workplace.tools;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Moossen 
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message contant for key in the resource bundle. */
    public static final String LOG_MISSING_ADMIN_TOOL_1 = "LOG_MISSING_ADMIN_TOOL_1";
    
    /** Message contant for key in the resource bundle. */
    public static final String LOG_MISSING_TOOL_HANDLER_2 = "LOG_MISSING_TOOL_HANDLER_2";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_ADMIN_VIEW_LOADING_0 = "GUI_ADMIN_VIEW_LOADING_0";

    /** Static instance member. */
    public static final String GUI_ADMIN_VIEW_ROOT_HELP_0 = "GUI_ADMIN_VIEW_ROOT_HELP_0";

    /** Static instance member. */
    public static final String GUI_ADMIN_VIEW_ROOT_NAME_0 = "GUI_ADMIN_VIEW_ROOT_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_ADMIN_VIEW_UPLEVEL_0 = "GUI_ADMIN_VIEW_UPLEVEL_0";

    /** Static instance member. */
    public static final String GUI_EMPTY_MESSAGE_0 = "GUI_EMPTY_MESSAGE_0";

    /** Static instance member. */
    public static final String GUI_TOOLS_DEFAULT_GROUP_0 = "GUI_TOOLS_DEFAULT_GROUP_0";

    /** Static instance member. */
    public static final String GUI_TOOLS_DEFAULT_HELP_0 = "GUI_TOOLS_DEFAULT_HELP_0";

    /** Static instance member. */
    public static final String GUI_TOOLS_DEFAULT_NAME_0 = "GUI_TOOLS_DEFAULT_NAME_0";

    /** Static instance member. */
    public static final String GUI_TOOLS_DISABLED_HELP_0 = "GUI_TOOLS_DISABLED_HELP_0";

    /** Static instance member. */
    public static final String GUI_TOOLS_DISABLED_ONLINE_HELP_0 = "GUI_TOOLS_DISABLED_ONLINE_HELP_0";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_CREATED_0 = "INIT_TOOLMANAGER_CREATED_0";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_INCONSISTENT_PATH_1 = "INIT_TOOLMANAGER_INCONSISTENT_PATH_1";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_NEWTOOL_FOUND_1 = "INIT_TOOLMANAGER_NEWTOOL_FOUND_1";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_NOT_CREATED_0 = "INIT_TOOLMANAGER_NOT_CREATED_0";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_SETUP_ERROR_0 = "INIT_TOOLMANAGER_SETUP_ERROR_0";

    /** Static instance member. */
    public static final String INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1 = "INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.messages";

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