/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/Messages.java,v $
 * Date   : $Date: 2005/06/23 10:47:32 $
 * Version: $Revision: 1.9 $
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

package org.opencms.workplace.tools.widgetdemo;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_1_HELP_0 = "GUI_WIDGETDEMO_1_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_1_NAME_0 = "GUI_WIDGETDEMO_1_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_2_HELP_0 = "GUI_WIDGETDEMO_2_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_2_NAME_0 = "GUI_WIDGETDEMO_2_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_3_HELP_0 = "GUI_WIDGETDEMO_3_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_3_NAME_0 = "GUI_WIDGETDEMO_3_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_4_HELP_0 = "GUI_WIDGETDEMO_4_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_4_NAME_0 = "GUI_WIDGETDEMO_4_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_5_HELP_0 = "GUI_WIDGETDEMO_5_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_5_NAME_0 = "GUI_WIDGETDEMO_5_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_6_HELP_0 = "GUI_WIDGETDEMO_6_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_6_NAME_0 = "GUI_WIDGETDEMO_6_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_7_HELP_0 = "GUI_WIDGETDEMO_7_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_7_NAME_0 = "GUI_WIDGETDEMO_7_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_8_HELP_0 = "GUI_WIDGETDEMO_8_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_8_NAME_0 = "GUI_WIDGETDEMO_8_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_9_HELP_0 = "GUI_WIDGETDEMO_9_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_9_NAME_0 = "GUI_WIDGETDEMO_9_NAME_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_ADVANCED_GROUP_0 = "GUI_WIDGETDEMO_ADVANCED_GROUP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_BASIC_GROUP_0 = "GUI_WIDGETDEMO_BASIC_GROUP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_DEMOHELP_1 = "GUI_WIDGETDEMO_DEMOHELP_1";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_DEMOHELP_VAL2_0 = "GUI_WIDGETDEMO_DEMOHELP_VAL2_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_DEMOVALUE_1 = "GUI_WIDGETDEMO_DEMOVALUE_1";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_TOOL_GROUP_0 = "GUI_WIDGETDEMO_TOOL_GROUP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_TOOL_HELP_0 = "GUI_WIDGETDEMO_TOOL_HELP_0";

    /** Message contant for key in the resource bundle. */
    public static final String GUI_WIDGETDEMO_TOOL_NAME_0 = "GUI_WIDGETDEMO_TOOL_NAME_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.widgetdemo.messages";

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