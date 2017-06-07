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
 * Interface used to check menu item visibility for context menus.<p>
 */
public interface I_CmsHasMenuItemVisibility {

    /**
     * Gets the visibility for the current resource and CMS context.<p>
     *
     * @param cms the CMS context to use
     * @param resources the list of resources to check
     *
     * @return the visibility
     */
    CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources);

    /**
     * Gets the visibility for the current dialog context.<p>
     *
     * @param context the dialog context
     *
     * @return the visibility
     */
    CmsMenuItemVisibilityMode getVisibility(I_CmsDialogContext context);
}
