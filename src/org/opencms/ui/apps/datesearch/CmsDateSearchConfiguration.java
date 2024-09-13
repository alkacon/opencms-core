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

package org.opencms.ui.apps.datesearch;

import org.opencms.file.CmsObject;
import org.opencms.security.CmsRole;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.apps.A_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.I_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;

import java.util.Locale;

import com.vaadin.server.Resource;

/**
 * Date search app configuration.
 */
public class CmsDateSearchConfiguration extends A_CmsWorkplaceAppConfiguration {

    /** Id of the app. */
    public static final String APP_ID = "date-search";

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppInstance()
     */
    @Override
    public I_CmsWorkplaceApp getAppInstance() {

        return new CmsDateSearchApp();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    @Override
    public String getHelpText(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_DATE_SEARCH_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getIcon()
     */
    @Override
    public Resource getIcon() {

        return new CmsCssIcon("oc-icon-32-date-search");
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getId()
     */
    @Override
    public String getId() {

        return APP_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    @Override
    public String getName(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_DATE_SEARCH_TITLE_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    @Override
    public int getOrder() {

        return 13;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getPriority()
     */
    @Override
    public int getPriority() {

        return I_CmsWorkplaceAppConfiguration.DEFAULT_PRIORIY;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getRequiredRole()
     */
    @Override
    public CmsRole getRequiredRole() {

        return CmsRole.WORKPLACE_USER;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    @Override
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        return new CmsAppVisibilityStatus(true, true, null);
    }
}
