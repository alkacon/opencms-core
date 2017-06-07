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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;

import java.util.Locale;

/**
 * Abstract superclass for workplace apps.<p>
 */
public abstract class A_CmsWorkplaceAppConfiguration implements I_CmsWorkplaceAppConfiguration {

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getAppCategory()
     */
    public String getAppCategory() {

        return CmsWorkplaceAppManager.MAIN_CATEGORY_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getButtonStyle()
     */
    public String getButtonStyle() {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getHelpText(java.util.Locale)
     */
    public String getHelpText(Locale locale) {

        return "";
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        return getId();
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getOrder()
     */
    public int getOrder() {

        return 0;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getPriority()
     */
    public int getPriority() {

        return I_CmsWorkplaceAppConfiguration.DEFAULT_PRIORIY;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration#getVisibility(org.opencms.file.CmsObject)
     */
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        CmsAppVisibilityStatus visibility;
        if (hasWorkplaceUserRole(cms)) {
            visibility = new CmsAppVisibilityStatus(true, true, "");
        } else {
            visibility = new CmsAppVisibilityStatus(
                false,
                false,
                Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
                    Messages.GUI_WORKPLACE_ACCESS_DENIED_MESSAGE_0));
        }
        return visibility;
    }

    /**
     * Checks whether the current user has the workplace user role.<p>
     *
     * @param cms the cms context
     *
     * @return <code>true</code> if the current user has the workplace user role
     */
    protected boolean hasWorkplaceUserRole(CmsObject cms) {

        return OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_USER);
    }

}
