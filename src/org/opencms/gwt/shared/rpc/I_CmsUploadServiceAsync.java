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

package org.opencms.gwt.shared.rpc;

import org.opencms.gwt.shared.CmsUploadFileBean;
import org.opencms.gwt.shared.CmsUploadProgessInfo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles all RPC services related to the upload dialog.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.ade.upload.CmsUploadService
 * @see org.opencms.gwt.shared.rpc.I_CmsUploadService
 * @see org.opencms.gwt.shared.rpc.I_CmsUploadServiceAsync
 */
public interface I_CmsUploadServiceAsync {

    /**
     * Cancels the upload.<p>
     *
     * @param callback the asynchronous callback
     */
    void cancelUpload(AsyncCallback<Boolean> callback);

    /**
     * Checks the availability of a resource in the VFS, using the
     * {@link org.opencms.file.CmsResourceFilter#IGNORE_EXPIRATION} filter.<p>
     *
     * Calculates the VFS path for each filename in the given list and checks its availability.<p>
     *
     * @param fileNames the filenames to check
     * @param targetFolder the folder to check
     * @param isRootPath <code>true</code> in case the target folder path is a root path
     * @param callback the async callback
     */
    void checkUploadFiles(
        List<String> fileNames,
        String targetFolder,
        boolean isRootPath,
        AsyncCallback<CmsUploadFileBean> callback);

    /**
     * Returns the upload progress information.<p>
     *
     * @param callback the asynchronous callback
     */
    void getUploadProgressInfo(AsyncCallback<CmsUploadProgessInfo> callback);
}
