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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.quota;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Information about a folder's size.
 */
public class CmsFolderReportEntry {

    /** The total sum of file sizes in the subtree starting at this folder. */
    long m_treeSize;

    /** The total sum of file sizs in the subtree starting at this folder, but excluding the contents of any other folders requested. */
    long m_treeSizeExclusive;

    /**
     * Creates a new entry.
     *
     * @param treeSize the total tree size
     * @param treeSizeExclusive the total tree size without any subtrees corresponding to folders that have also been requested in the report
     */
    public CmsFolderReportEntry(long treeSize, long treeSizeExclusive) {

        m_treeSize = treeSize;
        m_treeSizeExclusive = treeSizeExclusive;
    }

    /**
     * Gets the total sum of file sizes in the subtree starting at this folder.
     *
     * @return the file size sum
     */
    public long getTreeSize() {

        return m_treeSize;
    }

    /**
     * Gets the total sum of file sizes in the subtree starting at this folder, but excluding the contents of other requested folders.
     *
     * @return the exclusive tree size
     */
    public long getTreeSizeExclusive() {

        return m_treeSizeExclusive;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
