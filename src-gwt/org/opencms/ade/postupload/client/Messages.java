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

package org.opencms.ade.postupload.client;

import org.opencms.gwt.client.util.CmsMessages;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.0.0
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_ADVANCED_0 = "GUI_DIALOG_BUTTON_ADVANCED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_BACK_0 = "GUI_DIALOG_BUTTON_BACK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_CLOSE_0 = "GUI_DIALOG_BUTTON_CLOSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_NEXT_0 = "GUI_DIALOG_BUTTON_NEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_INFO_FIRST_RESOURCE_0 = "GUI_DIALOG_INFO_FIRST_RESOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_INFO_LAST_RESOURCE_0 = "GUI_DIALOG_INFO_LAST_RESOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_TITLE_0 = "GUI_DIALOG_TITLE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.postupload.clientmessages";

    /** Static instance member. */
    private static CmsMessages INSTANCE;

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
    public static CmsMessages get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsMessages(BUNDLE_NAME);
        }
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
