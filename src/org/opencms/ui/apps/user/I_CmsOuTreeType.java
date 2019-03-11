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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.ui.CmsCssIcon;

/**
 * Interface for OU tree types.<p>
 */
public interface I_CmsOuTreeType {

    /**
     * Returns the key for the empty-message.<p>
     *
     * @return key as string
     */
    public String getEmptyMessageKey();

    /**
     * Get the icon.<p>
     *
     * @return CmsCssIcon
     */
    public CmsCssIcon getIcon();

    /**
     * Gets the id of the type.<p>
     *
     * @return id string
     */
    public String getId();

    /**
     * Gets the name of the element.<p>
     *
     * @return name
     */
    public String getName();

    /**
     * Checks if type is expandable.<p>
     *
     * @return true if expandable
     */
    public boolean isExpandable();

    /**
     * Checks if the tree type is group-like.<p>
     *
     * @return true for group-like types
     */
    public boolean isGroup();

    /**
     * Checks if the tree type is OU-like.<p>
     *
     * @return true for OU-like types
     */
    public boolean isOrgUnit();

    /**
     * Checks if the tree type is role-like.<p>
     *
     * @return true for role-like OUs
     */
    public boolean isRole();

    /**
     * Checks if the tree type is user-like.<p>
     *
     * @return true for user-like OUs
     */
    public boolean isUser();

    /**
     * Checks if the tree type is available for a given OU.
     *
     * @param cms the CMS context
     * @param ou the OU FQN.
     *
     * @return true if the tree type is valid for the OU
     */
    public boolean isValidForOu(CmsObject cms, String ou);

    /**
     * True if type should be shown in OU overview table.<p>
     *
     * @return true if the type should be shown in the overview table for an OU
     */
    public boolean showInOuTable();

}
