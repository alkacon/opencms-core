/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/Messages.java,v $
 * Date   : $Date: 2005/09/16 08:51:27 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.notification;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITE_0 = "GUI_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_NOTIFICATIONS_SENT_0 = "LOG_NO_NOTIFICATIONS_SENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NOTIFICATIONS_SENT_TO_0 = "LOG_NOTIFICATIONS_SENT_TO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ISSUE_0 = "GUI_ISSUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPDATE_REQUIRED_1 = "GUI_UPDATE_REQUIRED_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNCHANGED_SINCE_1 = "GUI_UNCHANGED_SINCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RELEASE_AT_1 = "GUI_RELEASE_AT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPIRES_AT_1 = "GUI_EXPIRES_AT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIRM_RESOURCE_1 = "ERR_CONFIRM_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONFIRM_0 = "GUI_CONFIRM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DATE_0 = "GUI_DATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DAYS_NOT_MODIFIED_0 = "GUI_DAYS_NOT_MODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DISPLAY_0 = "GUI_DISPLAY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDIT_0 = "GUI_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EVENT_0 = "GUI_EVENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FILES_NOT_UPDATED_1 = "GUI_FILES_NOT_UPDATED_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODIFY_0 = "GUI_MODIFY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_0 = "GUI_RESOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WITHIN_NEXT_DAY_0 = "GUI_WITHIN_NEXT_DAY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_WITHIN_NEXT_WEEK_0 = "GUI_WITHIN_NEXT_WEEK_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PUBLISH_FAILED_2 = "LOG_PUBLISH_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PUBLISH_FINISHED_1 = "LOG_PUBLISH_FINISHED_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.notification.messages";

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
