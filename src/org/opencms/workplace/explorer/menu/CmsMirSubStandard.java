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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer.menu;

import org.opencms.file.CmsObject;
import org.opencms.workplace.explorer.CmsResourceUtil;

/**
 * Defines a menu item rule for the sub menu generation that checks the visibility of the sub items to show.<p>
 *
 * @since 6.5.6
 */
public class CmsMirSubStandard extends A_CmsMenuItemRule {

    /** The name of the standard rule set used for sub context menu entries. */
    public static final String RULE_NAME = "substandard";

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, org.opencms.workplace.explorer.CmsResourceUtil[], org.opencms.workplace.explorer.menu.I_CmsMenuItemRule[])
     */
    @Override
    public CmsMenuItemVisibilityMode getVisibility(
        CmsObject cms,
        CmsResourceUtil[] resourceUtil,
        I_CmsMenuItemRule[] rule) {

        for (int i = 0; i < rule.length; i++) {
            CmsMenuItemVisibilityMode mode = rule[i].getVisibility(cms, resourceUtil);
            if (!mode.isInVisible()) {
                // found the first visible item, return mode
                return mode;
            }
        }
        // no visible item found, sub menu entry is not displayed
        return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#matches(org.opencms.file.CmsObject, CmsResourceUtil[])
     */
    public boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        // this rule always matches
        return true;
    }

}
