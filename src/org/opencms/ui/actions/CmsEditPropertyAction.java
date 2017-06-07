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

package org.opencms.ui.actions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsEditPropertyContext;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilitySingleOnly;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * The edit table property action.<p>
 */
public class CmsEditPropertyAction extends A_CmsWorkplaceAction {

    /** The action id. */
    public static final String ACTION_ID = "edit_";

    /** The action visibility. */
    public static final I_CmsHasMenuItemVisibility VISIBILITY = new CmsMenuItemVisibilitySingleOnly(
        CmsStandardVisibilityCheck.DEFAULT);

    /** The property id. */
    private CmsResourceTableProperty m_propertyId;

    /** The action title message key. */
    private String m_titleKey;

    /**
     * Constructor.<p>
     *
     * @param propertyId the property id
     * @param titleKey the title message key
     */
    public CmsEditPropertyAction(CmsResourceTableProperty propertyId, String titleKey) {
        m_propertyId = propertyId;
        m_titleKey = titleKey;
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#executeAction(org.opencms.ui.I_CmsDialogContext)
     */
    public void executeAction(I_CmsDialogContext context) {

        if (!hasBlockingLocks(context)) {
            ((I_CmsEditPropertyContext)context).editProperty(m_propertyId);
        }
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getId()
     */
    public String getId() {

        return ACTION_ID + m_propertyId.getId();
    }

    /**
     * @see org.opencms.ui.actions.I_CmsWorkplaceAction#getTitle()
     */
    public String getTitle() {

        return getWorkplaceMessage(m_titleKey);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        return VISIBILITY.getVisibility(cms, resources);
    }

    /**
     * @see org.opencms.ui.actions.A_CmsWorkplaceAction#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        if (!(context instanceof I_CmsEditPropertyContext)
            || !((I_CmsEditPropertyContext)context).isPropertyEditable(m_propertyId)) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        } else {
            return getVisibility(context.getCms(), context.getResources());
        }
    }
}
