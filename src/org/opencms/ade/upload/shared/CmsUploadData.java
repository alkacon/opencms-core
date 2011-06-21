/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.upload.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Runtime data bean for prefetching.<p>
 * 
 * @since 8.0.0
 */
public class CmsUploadData implements IsSerializable {

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_ade_upload";

    /** The maximum file size for the upload. */
    private long m_uploadFileSizeLimit;

    /**
     * Returns the uploadFileSizeLimit.<p>
     *
     * @return the uploadFileSizeLimit
     */
    public long getUploadFileSizeLimit() {

        return m_uploadFileSizeLimit;
    }

    /**
     * Default constructor.<p>
     */
    public CmsUploadData() {

        // noop
    }

    /**
     * Constructor.<p>
     * 
     * @param uploadFileSizeLimit the upload file size limit
     */
    public CmsUploadData(long uploadFileSizeLimit) {

        m_uploadFileSizeLimit = uploadFileSizeLimit;
    }

    /**
     * Clone constructor.<p>
     * 
     * @param clone the instance to clone 
     */
    public CmsUploadData(CmsUploadData clone) {

        this(clone.getUploadFileSizeLimit());
    }

}
