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
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.workplace.explorer.CmsResourceUtil;

/**
 * Defines a menu item rule to check the visibility of a context menu item in the explorer view.<p>
 *
 * @since 6.5.6
 */
public interface I_CmsMenuItemRule {

    /** The request attribute name for the context information. */
    String ATTR_CONTEXT_INFO = "__contextInfo";

    /** A constant that signals that we are in the container page context. */
    String CONTEXT_CONTAINERPAGE = AdeContext.containerpage.toString();

    /** A constant that signals that we are in the edit provider context. */
    String CONTEXT_EDITPROVIDER = AdeContext.editprovider.toString();

    /** A constant that signals that we are in the sitemap context. */
    String CONTEXT_SITEMAP = AdeContext.sitemap.toString();

    /**
     * Returns the visibility for the menu item.<p>
     *
     * The possible visibilities are:
     * <ul>
     * <li>{@link CmsMenuItemVisibilityMode#VISIBILITY_ACTIVE} menu item is active</li>
     * <li>{@link CmsMenuItemVisibilityMode#VISIBILITY_INACTIVE} menu item is inactive (greyed out)</li>
     * <li>{@link CmsMenuItemVisibilityMode#VISIBILITY_INVISIBLE} menu item is invisible</li>
     * </ul>
     *
     * @param cms the current OpenCms user context
     * @param resourceUtil the initialized resource utilities for which the menu item visibility is checked
     * @return the visibility of the menu item
     */
    CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil);

    /**
     * Returns the visibility for the menu item, depending on the resource utilities and menu item rules.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceUtil the initialized resource utilities for which the menu item visibility is checked
     * @param rule the rules which are checked for visibility
     * @return the visibility for the menu item
     */
    CmsMenuItemVisibilityMode getVisibility(CmsObject cms, CmsResourceUtil[] resourceUtil, I_CmsMenuItemRule[] rule);

    /**
     * Returns if the rule for the menu item should be applied or not.<p>
     *
     * @param cms the current OpenCms user context
     * @param resourceUtil the initialized resource utilities for which the menu item rule is checked
     * @return true if the rule for the menu item should be applied, otherwise false
     */
    boolean matches(CmsObject cms, CmsResourceUtil[] resourceUtil);

}
