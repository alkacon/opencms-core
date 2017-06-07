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

package org.opencms.ugc;

import org.apache.commons.fileupload.FileItem;

/**
 * Implementation of the I_CmsFormDataItem which delegates its methods to a FileItem from Apache Commons Upload.<p>
 */
public class CmsUgcDataItem implements I_CmsFormDataItem {

    /** The wrapped file item. */
    private FileItem m_fileItem;

    /**
     * Creates a new instance.<p>
     *
     * @param item the file item to wrap
     */
    public CmsUgcDataItem(FileItem item) {

        m_fileItem = item;
    }

    /**
     * @see org.opencms.ugc.I_CmsFormDataItem#getData()
     */
    @Override
    public byte[] getData() {

        return m_fileItem.get();
    }

    /**
     * @see org.opencms.ugc.I_CmsFormDataItem#getFieldName()
     */
    @Override
    public String getFieldName() {

        return m_fileItem.getFieldName();
    }

    /**
     * @see org.opencms.ugc.I_CmsFormDataItem#getFileName()
     */
    @Override
    public String getFileName() {

        return m_fileItem.getName();
    }

}
