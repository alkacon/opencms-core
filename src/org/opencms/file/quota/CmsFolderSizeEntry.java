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

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Information about the total size of files in a folder (not including any subfolders).
 */
public class CmsFolderSizeEntry {

    /** The root path. */
    private String m_rootPath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The total size (sum of all file sizes). */
    private long m_size;

    /** The type of the folder. */
    private int m_typeId;

    /**
     * Creates a new entry.
     *
     * @param structureId the structure id of the folder
     * @param rootPath the root path of the folder
     * @param size the total file size
     * @param typeId the type of the folder
     */
    public CmsFolderSizeEntry(CmsUUID structureId, String rootPath, long size, int typeId) {

        super();
        m_structureId = structureId;
        m_rootPath = rootPath.endsWith("/") ? rootPath : rootPath + "/";
        m_size = size;
        m_typeId = typeId;
    }

    public static void main(String[] args) {

        for (int j = 0; j < 10; j++) {
            List<CmsFolderSizeEntry> list = new ArrayList<>();

            for (int i = 0; i < 200000; i++) {
                list.add(
                    new CmsFolderSizeEntry(new CmsUUID(), "/blah/foo/xyz/quorb/wuz/norg" + i, (371 * i) % 10000, 1));
            }
            long result = 0;
            long start = System.currentTimeMillis();
            for (CmsFolderSizeEntry entry : list) {
                result += entry.getSize();
                String path = entry.getRootPath();
                while (path != null) {
                    path = CmsResource.getParentFolder(path);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
    }

    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Gets the total file size.
     *
     * @return the total file size
     */
    public long getSize() {

        return m_size;
    }

    /**
     * Gets the structure id of the folder.
     *
     * @return the structure id of the folder
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the type of the folder.
     *
     * @return the type of the folder
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
