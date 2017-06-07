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

package org.opencms.ade.upload;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_CREATING_0 = "ERR_UPLOAD_CREATING_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_CREATING_1 = "ERR_UPLOAD_CREATING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_SIZE_LIMIT_3 = "ERR_UPLOAD_FILE_SIZE_LIMIT_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FROZEN_1 = "ERR_UPLOAD_FROZEN_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_INTERRUPT_WATCH_DOG_1 = "ERR_UPLOAD_INTERRUPT_WATCH_DOG_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_JSON_0 = "ERR_UPLOAD_JSON_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_NO_FILEITEMS_0 = "ERR_UPLOAD_NO_FILEITEMS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_NO_MULTIPART_0 = "ERR_UPLOAD_NO_MULTIPART_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_REQUEST_SIZE_LIMIT_2 = "ERR_UPLOAD_REQUEST_SIZE_LIMIT_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_UNEXPECTED_0 = "ERR_UPLOAD_UNEXPECTED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_USER_CANCELED_0 = "ERR_UPLOAD_USER_CANCELED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_TITLE_0 = "GUI_UPLOAD_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UPLOAD_CREATE_WATCH_DOG_2 = "LOG_UPLOAD_CREATE_WATCH_DOG_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UPLOAD_FINISHED_WATCHER_1 = "LOG_UPLOAD_FINISHED_WATCHER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UPLOAD_FROZEN_WATCH_DOG_1 = "LOG_UPLOAD_FROZEN_WATCH_DOG_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UPLOAD_SUCCESS_0 = "LOG_UPLOAD_SUCCESS_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.upload.messages";

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
