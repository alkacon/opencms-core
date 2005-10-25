/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/Messages.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.workplace.tools.content.check;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String CHECK_CONTAINS_FILENAME_2 = "CHECK_CONTAINS_FILENAME_2";

    /** Message constant for key in the resource bundle. */
    public static final String CHECK_MATCH_3 = "CHECK_MATCH_3";

    /** Message constant for key in the resource bundle. */
    public static final String CHECK_NO_PROPERTYNAME_1 = "CHECK_NO_PROPERTYNAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String CHECK_TOO_SHORT_3 = "CHECK_TOO_SHORT_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_TEST_0 = "ERR_NO_TEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_VFSPATH_0 = "ERR_NO_VFSPATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHECKCONTENT_CONFIGURATION_PROPERTY_HELP_0 = "GUI_CHECKCONTENT_CONFIGURATION_PROPERTY_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHECKCONTENT_LIST_NAME_0 = "GUI_CHECKCONTENT_LIST_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CANNOT_CREATE_PLUGIN_1 = "LOG_CANNOT_CREATE_PLUGIN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CANNOT_CREATE_PLUGIN_2 = "LOG_CANNOT_CREATE_PLUGIN_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_PLUGIN_1 = "LOG_CREATE_PLUGIN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_PROCESSING_PROPERTIES_2 = "LOG_ERROR_PROCESSING_PROPERTIES_2";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_CHECK_BEGIN_0 = "RPT_CONTENT_CHECK_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_CHECK_END_0 = "RPT_CONTENT_CHECK_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_COLLECT_BEGIN_0 = "RPT_CONTENT_COLLECT_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_COLLECT_END_1 = "RPT_CONTENT_COLLECT_END_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_2 = "RPT_CONTENT_PROCESS_2";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_BEGIN_0 = "RPT_CONTENT_PROCESS_BEGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_END_0 = "RPT_CONTENT_PROCESS_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_ERROR_0 = "RPT_CONTENT_PROCESS_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_ERROR_1 = "RPT_CONTENT_PROCESS_ERROR_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_ERROR_2 = "RPT_CONTENT_PROCESS_ERROR_2";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_RESOURCE_1 = "RPT_CONTENT_PROCESS_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_WARNING_0 = "RPT_CONTENT_PROCESS_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_CONTENT_PROCESS_WARNING_1 = "RPT_CONTENT_PROCESS_WARNING_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EMPTY_0 = "RPT_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXTRACT_FROM_PATH_1 = "RPT_EXTRACT_FROM_PATH_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXTRACT_FROM_PATH_BEGIN_1 = "RPT_EXTRACT_FROM_PATH_BEGIN_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXTRACT_FROM_PATH_END_0 = "RPT_EXTRACT_FROM_PATH_END_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXTRACT_FROM_PATH_ERROR_2 = "RPT_EXTRACT_FROM_PATH_ERROR_2";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.content.check.messages";

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