/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the configuration of the additional information of users.<p>
 *
 * This class reads the settings from the "opencms-workplace.xml" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 *
 * @since 6.5.6
 */
public final class CmsWorkplaceUserInfoManager {

    /** The list of defined blocks. */
    private List<CmsWorkplaceUserInfoBlock> m_blocks;

    /**
     * Default constructor.<p>
     */
    public CmsWorkplaceUserInfoManager() {

        m_blocks = new ArrayList<CmsWorkplaceUserInfoBlock>();
    }

    /**
     * Adds the given block.<p>
     *
     * @param block the block to add
     */
    public void addBlock(CmsWorkplaceUserInfoBlock block) {

        m_blocks.add(block);
    }

    /**
     * Returns a list of all configured additional information blocks.<p>
     *
     * @return a list of {@link CmsWorkplaceUserInfoBlock} objects
     */
    public List<CmsWorkplaceUserInfoBlock> getBlocks() {

        return m_blocks;
    }
}