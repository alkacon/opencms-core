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
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.List;

/**
 * Abstract superclass for menu item visibility checks.<p>
 *
 * This class automatically handles the case where multiple resources are passed to the getVisibilityMethod.
 * You just need to implement the getSingleVisibility method in subclasses.
 */
public abstract class A_CmsSimpleVisibilityCheck implements I_CmsHasMenuItemVisibility {

    /** Flag to indicate that the check should not match multiple resources. */
    protected boolean m_singleResourceOnly;

    /**
     * Computes visibility of the menu item for a single resource.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource to check
     *
     * @return the visibility for the given resource
     */
    public abstract CmsMenuItemVisibilityMode getSingleVisibility(CmsObject cms, CmsResource resource);

    /**
     * @see org.opencms.ui.contextmenu.I_CmsHasMenuItemVisibility#getVisibility(org.opencms.file.CmsObject, java.util.List)
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, List<CmsResource> resources) {

        if (resources.size() <= 1) {
            // Single-selection case where we just delegate to getSingleVisibility
            // this applies also to main menu items
            return getSingleVisibility(cms, resources.size() == 1 ? resources.get(0) : null);
        } else {
            if (m_singleResourceOnly) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            CmsMenuItemVisibilityMode currentVisibility = null;
            for (CmsResource resource : resources) {
                CmsMenuItemVisibilityMode visibilityForResource = getSingleVisibility(cms, resource);
                if ((currentVisibility == null)
                    || (getPriority(visibilityForResource) > getPriority(currentVisibility))) {
                    currentVisibility = visibilityForResource;
                }
            }
            if (currentVisibility == null) {
                currentVisibility = CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
            if (currentVisibility.isInActive()) {
                // In the multi-selection case, different resources may cause the menu item to be inactive for different reasons,
                // which would make it hard to give a user-readable message. So we set the status to invisible.
                currentVisibility = CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            return currentVisibility;
        }
    }

    /**
     * Gets the priority of a menu item visibility mode.<p>
     *
     * Higher priority modes override the ones with lower priorities.<p>
     *
     *
     * @param mode the mode
     * @return the priority for the mode
     */
    private int getPriority(CmsMenuItemVisibilityMode mode) {

        if (mode.isPrioritized()) {
            return 4;
        }
        if (mode.isInActive()) {
            return 0;
        }
        if (mode.isActive()) {
            return 1;
        }
        // invisible
        return 2;
    }
}
