/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/rfsfile/Messages.java,v $
 * Date   : $Date: 2005/07/15 10:34:03 $
 * Version: $Revision: 1.11 $
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

package org.opencms.workplace.tools.workplace.rfsfile;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author  Achim Westermann 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_NO_FILE_SELECTED_0 = "GUI_WORKPLACE_LOGVIEW_NO_FILE_SELECTED_0";
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_DOWNLOAD_SERVLET_FILE_ARG_0 = "ERR_DOWNLOAD_SERVLET_FILE_ARG_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FILE_ACCESS_0 = "ERR_FILE_ACCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FLMOD_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FLMOD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FNAME_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FPATH_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FPATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FSIZE_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_FSIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_MSG_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_MSG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_TITLE_0 = "GUI_WORKPLACE_LOGVIEW_DOWNLOAD_START_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_DEF_HELP_0 = "GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_DEF_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_HELP_0 = "GUI_WORKPLACE_LOGVIEW_FILE_CHARSET_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_FILE_CONF_HELP_0 = "GUI_WORKPLACE_LOGVIEW_FILE_CONF_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_FILE_LOG_HELP_0 = "GUI_WORKPLACE_LOGVIEW_FILE_LOG_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_FILE_XMLCONF_HELP_0 = "GUI_WORKPLACE_LOGVIEW_FILE_XMLCONF_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_GROUP_0 = "GUI_WORKPLACE_LOGVIEW_GROUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_SETTINGS_HELP_0 = "GUI_WORKPLACE_LOGVIEW_SETTINGS_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORKPLACE_LOGVIEW_SETTINGS_NAME_0 = "GUI_WORKPLACE_LOGVIEW_SETTINGS_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_HEADER_1 = "GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_HEADER_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_LINKTXT_0 = "GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_LINKTXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_MESSAGE_0 = "GUI_WORLKPLACE_LOGVIEW_DODOWNLOAD_MESSAGE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.workplace.rfsfile.messages";

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