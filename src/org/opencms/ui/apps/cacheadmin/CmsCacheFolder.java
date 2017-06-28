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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.apps.CmsAppVisibilityStatus;
import org.opencms.ui.apps.CmsWorkplaceAppManager;
import org.opencms.ui.apps.I_CmsFolderAppCategory;
import org.opencms.ui.apps.Messages;

import java.util.Locale;

import com.vaadin.server.Resource;

/**
 * Class for the Cache folder.<p>
 */
public class CmsCacheFolder implements I_CmsFolderAppCategory {

    /**Folder id.*/
    public static final String ID = "cache";

    /** The app icon resource (size 32x32). */
    public static final CmsCssIcon ICON = new CmsCssIcon("oc-icon-32-cache");

    /**
     * @see org.opencms.ui.apps.I_CmsFolderAppCategory#getButtonStyle()
     */
    public String getButtonStyle() {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsFolderAppCategory#getHelpText(java.util.Locale)
     */
    public String getHelpText(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_CACHE_ADMIN_TOOL_HELP_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsFolderAppCategory#getIcon()
     */
    public Resource getIcon() {

        return ICON;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getId()
     */
    public String getId() {

        return ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getName(java.util.Locale)
     */
    public String getName(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_CACHE_ADMIN_TOOL_NAME_0);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getOrder()
     */
    public int getOrder() {

        return 90;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getParentId()
     */
    public String getParentId() {

        return CmsWorkplaceAppManager.ADMINISTRATION_CATEGORY_ID;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppCategory#getPriority()
     */
    public int getPriority() {

        return 0;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsFolderAppCategory#getVisibility(org.opencms.file.CmsObject)
     */
    public CmsAppVisibilityStatus getVisibility(CmsObject cms) {

        CmsAppVisibilityStatus status = OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER)
        ? CmsAppVisibilityStatus.ACTIVE
        : CmsAppVisibilityStatus.INVISIBLE;
        return status;
    }
}
