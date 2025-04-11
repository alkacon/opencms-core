/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.gwt.client.seo;

import org.opencms.gwt.client.util.CmsMessages;

/**
 * The messages class for the SEO dialog.<p>
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ADD_ALIAS_0 = "GUI_ADD_ALIAS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_MOVED_0 = "GUI_ALIAS_MOVED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_MOVED_DESCRIPTION_0 = "GUI_ALIAS_MOVED_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_PAGE_0 = "GUI_ALIAS_PAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_PAGE_DESCRIPTION_0 = "GUI_ALIAS_PAGE_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_REDIRECT_0 = "GUI_ALIAS_REDIRECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIAS_REDIRECT_DESCRIPTION_0 = "GUI_ALIAS_REDIRECT_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ALIASES_0 = "GUI_ALIASES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ENTER_ALIAS_0 = "GUI_ENTER_ALIAS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXISTING_ALIASES_0 = "GUI_EXISTING_ALIASES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEW_ALIAS_0 = "GUI_NEW_ALIAS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REMOVE_ALIAS_0 = "GUI_REMOVE_ALIAS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SEO_OPTIONS_0 = "GUI_SEO_OPTIONS_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.gwt.seo.clientmessages";

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
