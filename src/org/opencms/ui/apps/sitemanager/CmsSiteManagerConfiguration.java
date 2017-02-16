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

package org.opencms.ui.apps.sitemanager;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.I_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Locale;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;

/**
 * Configuration class for the site management app.
 *
 */

public class CmsSiteManagerConfiguration extends A_CmsWorkplaceAppConfiguration {

    /** The app id. */
    public static final String APP_ID = "site-management";

    /**
     * empty constructor.
     */
    public CmsSiteManagerConfiguration() {
        // TODO Auto-generated constructor stub

    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    public I_CmsWorkplaceApp getAppInstance() {

        // TODO Auto-generated method stub
        return new CmsSiteManager();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    @Override
    public String getHelpText(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_SITE_MANAGER_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    public Resource getIcon() {

        return new ExternalResource(OpenCmsTheme.getImageLink("apps/sites.png"));
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
    @Override
    public String getName(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_SITE_MANAGER_TITLE_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    @Override
    public int getOrder() {

        return 4;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    @Override
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        CmsAppVisibilityStatus status = OpenCms.getRoleManager().hasRole(cms, CmsRole.ADMINISTRATOR)
        ? CmsAppVisibilityStatus.ACTIVE
        : CmsAppVisibilityStatus.INVISIBLE;
        return status;
    }
}
