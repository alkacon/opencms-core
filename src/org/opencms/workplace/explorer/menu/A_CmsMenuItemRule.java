/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/menu/A_CmsMenuItemRule.java,v $
 * Date   : $Date: 2011/03/23 14:51:54 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * Base implementation of the menu item rule.<p>
 * 
 * This was introduced because of the possible sub-menus. 
 * Most rules are not sub-menu aware, therefore they do not need to implement 
 * {@link #getVisibility(CmsObject, CmsResourceUtil[], I_CmsMenuItemRule[])}. 
 * This default base implementation just calls the standard visibility method 
 * discarding the additional rules.<p>
 * 
 * @author Andreas Zahner  
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.7.2 
 */
public abstract class A_CmsMenuItemRule implements I_CmsMenuItemRule {

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, org.opencms.workplace.explorer.CmsResourceUtil[])
     */
    public CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil) {

        // default is "always active"
        return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
    }

    /**
     * @see org.opencms.workplace.explorer.menu.I_CmsMenuItemRule#getVisibility(org.opencms.file.CmsObject, org.opencms.workplace.explorer.CmsResourceUtil[], org.opencms.workplace.explorer.menu.I_CmsMenuItemRule[])
     */
    public CmsMenuItemVisibilityMode getVisibility(
        CmsObject cms,
        CmsResourceUtil[] resourceUtil,
        I_CmsMenuItemRule[] rule) {

        // by default ignore the additional item rules
        return getVisibility(cms, resourceUtil);
    }
}