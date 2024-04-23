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

package org.opencms.ade.containerpage;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MISSING_CACHED_ELEMENT_0 = "ERR_MISSING_CACHED_ELEMENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ADDINFO_FORMATTER_0 = "GUI_ADDINFO_FORMATTER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ADDINFO_FORMATTER_KEY_0 = "GUI_ADDINFO_FORMATTER_KEY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ADDINFO_FORMATTER_LOCATION_0 = "GUI_ADDINFO_FORMATTER_LOCATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ADDINFO_SCHEMA_0 = "GUI_ADDINFO_SCHEMA_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTAINERPAGE_EDIT_DISABLED_BY_SITEMAP_CONFIG_0 = "GUI_CONTAINERPAGE_EDIT_DISABLED_BY_SITEMAP_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTAINERPAGE_TYPE_NOT_CREATABLE_1 = "GUI_CONTAINERPAGE_TYPE_NOT_CREATABLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DESCRIPTION_DEFAULT_RESOURCE_CONTENT_0 = "GUI_DESCRIPTION_DEFAULT_RESOURCE_CONTENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DETAIL_CONTENT_PAGE_TITLE_1 = "GUI_DETAIL_CONTENT_PAGE_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0 = "GUI_ELEMENT_RESOURCE_CAN_NOT_BE_EDITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_PATH_0 = "GUI_LABEL_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LISTADD_CAPTION_0 = "GUI_LISTADD_CAPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LISTADD_NO_TYPES_0 = "GUI_LISTADD_NO_TYPES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCKED_BY_1 = "GUI_LOCKED_BY_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCKED_FOR_PUBLISH_0 = "GUI_LOCKED_FOR_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REUSE_CHECK_ONLY_SHOW_N_1 = "GUI_REUSE_CHECK_ONLY_SHOW_N_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REUSE_CHECK_TITLE_0 = "GUI_REUSE_CHECK_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REUSE_CHECK_WARNING_TEXT_1 = "GUI_REUSE_CHECK_WARNING_TEXT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SCHEMA_FORMATTER_LABEL_0 = "GUI_SCHEMA_FORMATTER_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SETTING_TEMPLATE_CONTEXTS_DESCRIPTION_0 = "GUI_SETTING_TEMPLATE_CONTEXTS_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SETTING_TEMPLATE_CONTEXTS_NAME_0 = "GUI_SETTING_TEMPLATE_CONTEXTS_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHOWLOCALE_WRONG_SITE_0 = "GUI_SHOWLOCALE_WRONG_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_DEFAULT_RESOURCE_CONTENT_0 = "GUI_TITLE_DEFAULT_RESOURCE_CONTENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_MODEL_0 = "GUI_TITLE_MODEL_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.containerpage.messages";

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