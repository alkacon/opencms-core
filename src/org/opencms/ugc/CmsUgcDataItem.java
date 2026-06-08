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

package org.opencms.ugc;

import java.io.IOException;

import org.apache.commons.fileupload2.core.DiskFileItem;

/**
 * Implementation of the I_CmsFormDataItem which delegates its methods to a DiskFileItem from Apache Commons Upload.<p>
 */
public class CmsUgcDataItem implements I_CmsFormDataItem {

    /** The wrapped file item. */
    private DiskFileItem m_fileItem;

    /**
     * Creates a new instance.<p>
     *
     * @param item the file item to wrap
     */
    public CmsUgcDataItem(DiskFileItem item) {

        m_fileItem = item;
    }

    /**
     * @see org.opencms.ugc.I_CmsFormDataItem#getData()
     */
    @Override
    public byte[] getData() {

        try {
            return m_fileItem.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
