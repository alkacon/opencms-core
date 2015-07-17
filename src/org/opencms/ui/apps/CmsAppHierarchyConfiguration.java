/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;

import java.util.Locale;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

/**
 * The app hierarchy configuration.<p>
 */
public class CmsAppHierarchyConfiguration implements I_CmsWorkplaceAppConfiguration {

    /** The app id. */
    public static final String APP_ID = "apphierachy";

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    public String getAppCategory() {

        return "none";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        return new CmsAppHierachy();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getButtonStyle()
     */
    public String getButtonStyle() {

        return I_CmsAppButtonProvider.BUTTON_STYLE_GRAY;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    public String getHelpText(Locale locale) {

        return "Displays all available apps";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return FontAwesome.ROCKET;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    public String getId() {

        return APP_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        return "App launcher";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    public int getOrder() {

        return 0;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        return new CmsAppVisibilityStatus(true, true, null);
    }
}
