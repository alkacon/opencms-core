/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.contenteditor;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.5.1
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_EDITOR_RESTRICTED_0 = "ERR_EDITOR_RESTRICTED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERROR_CONFIGURATION_EDITOR_CHANGE_HANDLER_FORMATTER_SELECTION_1 = "ERROR_CONFIGURATION_EDITOR_CHANGE_HANDLER_FORMATTER_SELECTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERROR_FAILED_READING_CATEGORIES_1 = "ERROR_FAILED_READING_CATEGORIES_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTENT_TAB_LABEL_0 = "GUI_CONTENT_TAB_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SERIALDATE_EMPTY_EVENT_SERIES_0 = "GUI_SERIALDATE_EMPTY_EVENT_SERIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SERIALDATE_INVALID_SERIES_SPECIFICATION_0 = "GUI_SERIALDATE_INVALID_SERIES_SPECIFICATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SERIALDATE_MULTIPLE_EVENTS_3 = "GUI_SERIALDATE_MULTIPLE_EVENTS_3";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SERIALDATE_SINGLE_EVENT_1 = "GUI_SERIALDATE_SINGLE_EVENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SERIALDATE_STATUS_TOO_MANY_DATES_2 = "GUI_SERIALDATE_STATUS_TOO_MANY_DATES_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SETTINGS_TAB_DESCRIPTION_0 = "GUI_SETTINGS_TAB_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SETTINGS_TAB_LABEL_0 = "GUI_SETTINGS_TAB_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_TAKE_PREFETCHING_TIME_FOR_RESOURCE_2 = "LOG_TAKE_PREFETCHING_TIME_FOR_RESOURCE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_TAKE_READING_ENTITY_TIME_1 = "LOG_TAKE_READING_ENTITY_TIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_TAKE_READING_WIDGET_CONFIGURATION_TIME_2 = "LOG_TAKE_READING_WIDGET_CONFIGURATION_TIME_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_TAKE_UNMARSHALING_TIME_1 = "LOG_TAKE_UNMARSHALING_TIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_TAKE_VISITING_TYPES_TIME_1 = "LOG_TAKE_VISITING_TYPES_TIME_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.contenteditor.messages";

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
     * @see org.opencms.i18n.I_CmsMessageBundle#getBundleName()
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }

}
