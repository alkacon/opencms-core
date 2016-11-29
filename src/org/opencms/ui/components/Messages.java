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

package org.opencms.ui.components;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Message bundle class.<p>
 */
public class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_TO_CLIPBOARD_0 = "GUI_COPY_TO_CLIPBOARD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_TO_CLIPBOARD_DESCRIPTION_0 = "GUI_COPY_TO_CLIPBOARD_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ERROR_DIALOG_MESSAGE_0 = "GUI_ERROR_DIALOG_MESSAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_FILE_SELECT_CAPTION_0 = "GUI_FILE_SELECT_CAPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NOT_EDITABLE_ONLINE_0 = "GUI_NOT_EDITABLE_ONLINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_EDIT_0 = "GUI_USER_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_INFO_DELETE_IMAGE_0 = "GUI_USER_INFO_DELETE_IMAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_INFO_NO_UPLOAD_0 = "GUI_USER_INFO_NO_UPLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_INFO_ONLINE_SINCE_1 = "GUI_USER_INFO_ONLINE_SINCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_INFO_UPLOAD_IMAGE_0 = "GUI_USER_INFO_UPLOAD_IMAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USER_INFO_UPLOAD_IMAGE_DIALOG_TITLE_0 = "GUI_USER_INFO_UPLOAD_IMAGE_DIALOG_TITLE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ui.components.messages";

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
