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

import java.util.List;

/**
 * File uploader. Generates an asynchronous request to upload files and calls the dialog instance when done.<p>
 */
public interface I_CmsUploader {

    /**
     * Uploads the given files to the given URI.<p>
     *
     * @param uploadUri the upload URI
     * @param targetFolder the target folder
     * @param isRootPath true if the target folder is given as a root path
     * @param filesToUpload the files to upload
     * @param filesToUnzip the files to unzip
     * @param dialog the dialog instance
     */
    void uploadFiles(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        List<CmsFileInfo> filesToUpload,
        List<String> filesToUnzip,
        I_CmsUploadDialog dialog);

}
