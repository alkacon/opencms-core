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

package org.opencms.ui.apps.modules;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsWorkplaceAppManager;
import org.opencms.ui.apps.I_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;

import java.util.Locale;

import com.vaadin.server.Resource;

/**
 * App configuration for the module maanger app.<p>
 */
public class CmsModuleAppConfiguration extends A_CmsWorkplaceAppConfiguration {

    /** The app id. */
    public static final String APP_ID = "modules";

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    @Override
    public String getAppCategory() {

        return CmsWorkplaceAppManager.ADMINISTRATION_CATEGORY_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        return new CmsModuleApp();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getButtonStyle()
     */
    @Override
    public String getButtonStyle() {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return CmsModuleApp.Icons.APP;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    public String getId() {

        return APP_ID;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    @Override
    public String getName(Locale locale) {

        return org.opencms.ui.apps.Messages.get().getBundle(locale).key(Messages.GUI_MODULES_APP_NAME_0);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getRequiredRole()
     */
    @Override
    public CmsRole getRequiredRole() {

        return CmsRole.DATABASE_MANAGER;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    @Override
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        if (!OpenCms.getRoleManager().hasRole(cms, getRequiredRole())) {
            return CmsAppVisibilityStatus.INVISIBLE;
        }

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            return new CmsAppVisibilityStatus(
                true,
                false,
                org.opencms.ui.apps.Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                    Messages.GUI_MODULES_ONLINE_DISABLED_0));
        }
        return CmsAppVisibilityStatus.ACTIVE;
    }

}
