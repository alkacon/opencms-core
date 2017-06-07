/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.monitor;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CAUGHT_THROWABLE_1 = "LOG_CAUGHT_THROWABLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CLEAR_CACHE_MEM_CONS_0 = "LOG_CLEAR_CACHE_MEM_CONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_CONNECTIONS_3 = "LOG_MM_CONNECTIONS_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_CREATED_1 = "LOG_MM_CREATED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_EMAIL_DISABLED_0 = "LOG_MM_EMAIL_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_EMAIL_RECEIVER_2 = "LOG_MM_EMAIL_RECEIVER_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_EMAIL_SENDER_1 = "LOG_MM_EMAIL_SENDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_INTERVAL_EMAIL_1 = "LOG_MM_INTERVAL_EMAIL_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_INTERVAL_LOG_1 = "LOG_MM_INTERVAL_LOG_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_INTERVAL_MAX_USAGE_1 = "LOG_MM_INTERVAL_MAX_USAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_INTERVAL_WARNING_1 = "LOG_MM_INTERVAL_WARNING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_LOG_INFO_2 = "LOG_MM_LOG_INFO_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_NOWARN_STATUS_5 = "LOG_MM_NOWARN_STATUS_5";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_SESSION_STAT_3 = "LOG_MM_SESSION_STAT_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_STARTUP_TIME_2 = "LOG_MM_STARTUP_TIME_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_STATUS_EMAIL_SENT_0 = "LOG_MM_STATUS_EMAIL_SENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_WARNING_EMAIL_SENT_0 = "LOG_MM_WARNING_EMAIL_SENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_WARNING_MEM_CONSUME_2 = "LOG_MM_WARNING_MEM_CONSUME_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_WARNING_MEM_STATUS_6 = "LOG_MM_WARNING_MEM_STATUS_6";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_WARNING_MEM_STATUS_8 = "LOG_MM_WARNING_MEM_STATUS_8";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MM_WARNING_MEM_STATUS_AVG_6 = "LOG_MM_WARNING_MEM_STATUS_AVG_6";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.monitor.messages";

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
