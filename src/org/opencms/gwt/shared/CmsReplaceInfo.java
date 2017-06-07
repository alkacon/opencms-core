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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * File replace dialog info.<p>
 */
public class CmsReplaceInfo implements IsSerializable {

    /** The file info. */
    private CmsListInfoBean m_fileInfo;

    /** If the file is lockable. */
    private boolean m_isLockable;

    /** The maximum file upload size. */
    private long m_maxFileSize;

    /** The resource site path. */
    private String m_sitepath;

    /**
     * Constructor.<p>
     *
     * @param fileInfo the file info
     * @param sitepath the resource site path
     * @param isLockable if the file is lockable
     * @param maxFileSize the maximum file upload size
     */
    public CmsReplaceInfo(CmsListInfoBean fileInfo, String sitepath, boolean isLockable, long maxFileSize) {

        m_fileInfo = fileInfo;
        m_sitepath = sitepath;
        m_isLockable = isLockable;
        m_maxFileSize = maxFileSize;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsReplaceInfo() {

        // nothing to do
    }

    /**
     * Returns the file info.<p>
     *
     * @return the file info
     */
    public CmsListInfoBean getFileInfo() {

        return m_fileInfo;
    }

    /**
     * Returns the maximum file upload size.<p>
     *
     * @return the maximum file upload size
     */
    public long getMaxFileSize() {

        return m_maxFileSize;
    }

    /**
     * Returns the resource site path.<p>
     *
     * @return the resource site path
     */
    public String getSitepath() {

        return m_sitepath;
    }

    /**
     * Returns if the file is lockable.<p>
     *
     * @return <code>true</code> if the file is lockable
     */
    public boolean isLockable() {

        return m_isLockable;
    }
}
