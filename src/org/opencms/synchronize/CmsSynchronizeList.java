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

package org.opencms.synchronize;

import org.opencms.util.CmsDateUtil;

import java.io.Serializable;

/**
 * Defines the CmsSynchronizeList object, used to store synchronisation data
 * required to synchronize the VFS and the server FS.<p>
 *
 * @since 6.0.0
 */
public class CmsSynchronizeList implements Serializable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -4460686435282590290L;

    /**
     * Last modification data of this resource in the FS.
     */
    private long m_modifiedFs;

    /**
     * Last modification date of this resouce in the VFS.
     */
    private long m_modifiedVfs;

    /**
     * Name of the resource stored in the sync list.
     */
    private String m_resName;

    /**
     * Name of the translated resource stored in the sync list.
     * Its nescessary to translate the resource name, since the server FS does
     * allow different
     * naming conventions than the VFS.
     */
    private String m_transResName;

    /**
     * Constructor, creates a new CmsSynchronizeList object.
     *
     * @param resName The name of the resource
     * @param transResName The name of the resource
     * @param modifiedVfs last modification date in the Vfs
     * @param modifiedFs last modification date in the Fs
     */
    public CmsSynchronizeList(String resName, String transResName, long modifiedVfs, long modifiedFs) {

        m_resName = resName;
        m_transResName = transResName;
        m_modifiedVfs = modifiedVfs;
        m_modifiedFs = modifiedFs;
    }

    /**
     * Returns a format description of the sync-list file on the server FS.<p>
     *
     * @return format description
     */
    public static String getFormatDescription() {

        String output = "[original filename FS]:[translated filename VFS]";
        output += ":[timestamp VFS]:[timestamp  FS]";
        output += ":[VFS=readable timestamp VFS]:[FS=readable timestamp FS]";
        return output;
    }

    /**
     * Returns the last modification date in the Fs.
     * @return last modification date in the Fs
     */
    public long getModifiedFs() {

        return m_modifiedFs;
    }

    /**
     * Returns the last modification date in the Vfs.
     * @return last modification date in the Vfs
     */
    public long getModifiedVfs() {

        return m_modifiedVfs;
    }

    /**
     * Returns the name of the resource.
     * @return name of the resource
     */
    public String getResName() {

        return m_resName;
    }

    /**
     * Returns the translated name of the resource.
     * @return name of the resource
     */
    public String getTransResName() {

        return m_transResName;
    }

    /**
     * Returns a string-representation for this object. <p>
     *
     * This is used to create the sync list entries in the server FS
     *
     * @return string-representation for this object.
     */
    @Override
    public String toString() {

        String output = m_resName + ":" + m_transResName + ":" + m_modifiedVfs + ":" + m_modifiedFs;
        output += ":VFS=" + CmsDateUtil.getDateTimeShort(m_modifiedVfs);
        output += ":FS=" + CmsDateUtil.getDateTimeShort(m_modifiedFs);
        return output;
    }

}
