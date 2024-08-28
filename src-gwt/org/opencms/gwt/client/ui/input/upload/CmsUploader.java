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

package org.opencms.gwt.client.ui.input.upload;

import org.opencms.gwt.client.ui.input.upload.impl.CmsUploaderFormData;

import java.util.List;

/**
 * File uploader utility class. Takes care of the browser specifics when uploading files asynchronously.<p>
 */
public class CmsUploader implements I_CmsUploader {

    /** The uploader implementation. */
    private I_CmsUploader m_impl;

    /**
     * Constructor.<p>
     */
    public CmsUploader() {

        m_impl = new CmsUploaderFormData();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploader#uploadFiles(java.lang.String, java.lang.String, boolean, java.lang.String, java.util.List, java.util.List, org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog)
     */
    public void uploadFiles(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        String postCreateHandler,
        List<CmsFileInfo> filesToUpload,
        List<String> filesToUnzip,
        boolean keepFileNames,
        I_CmsUploadDialog dialog) {

        m_impl.uploadFiles(
            uploadUri,
            targetFolder,
            isRootPath,
            postCreateHandler,
            filesToUpload,
            filesToUnzip,
            keepFileNames,
            dialog);
    }

}
