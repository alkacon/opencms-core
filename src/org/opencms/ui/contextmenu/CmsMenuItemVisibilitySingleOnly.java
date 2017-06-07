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

package org.opencms.ui.contextmenu;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * Decorator for menu item visibility classes which always returns INVISIBLE if more than one resource
 * is passed, but otherwise delegates the decision to its wrapped instance.<p>
 */
public class CmsMenuItemVisibilitySingleOnly implements I_CmsHasMenuItemVisibility {

    /** The wrapped instance to delegate to. */
    private I_CmsHasMenuItemVisibility m_visibility;

    /**
     * Creates a new instance wrapping the given visibility handler.<p>
     *
     * @param visibility the wrapped visibility handler
     */
    public CmsMenuItemVisibilitySingleOnly(I_CmsHasMenuItemVisibility visibility) {
        m_visibility = visibility;
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if (resources.size() != 1) {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
        return m_visibility.getVisibility(cms, resources);
    }

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.ui.I_CmsDialogContext)
     */
    public CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context) {

        return getVisibility(context.getCms(), context.getResources());
    }
}
