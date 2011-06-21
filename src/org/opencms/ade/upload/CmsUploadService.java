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

package org.opencms.ade.upload;

import org.opencms.ade.upload.shared.CmsUploadData;
import org.opencms.ade.upload.shared.CmsUploadFileBean;
import org.opencms.ade.upload.shared.CmsUploadProgessInfo;
import org.opencms.ade.upload.shared.CmsUploadProgessInfo.UPLOAD_STATE;
import org.opencms.ade.upload.shared.rpc.I_CmsUploadService;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.InvalidFileNameException;
import org.apache.commons.fileupload.util.Streams;

/**
 * Handles all RPC services related to the upload dialog.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.upload.CmsUploadService
 * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadService
 * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadServiceAsync
 */
public class CmsUploadService extends CmsGwtService implements I_CmsUploadService {

    /** The serial version UID. */
    private static final long serialVersionUID = -2235662141861687012L;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsUploadService newInstance(HttpServletRequest request) {

        CmsUploadService srv = new CmsUploadService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadService#cancelUpload()
     */
    public Boolean cancelUpload() {

        if (getRequest().getSession().getAttribute(CmsUploadBean.SESSION_ATTRIBUTE_LISTENER_ID) != null) {
            CmsUUID listenerId = (CmsUUID)getRequest().getSession().getAttribute(
                CmsUploadBean.SESSION_ATTRIBUTE_LISTENER_ID);
            CmsUploadListener listener = CmsUploadBean.getCurrentListener(listenerId);
            if ((listener != null) && !listener.isCanceled()) {
                listener.cancelUpload(new CmsUploadException(Messages.get().getBundle().key(
                    Messages.ERR_UPLOAD_USER_CANCELED_0)));
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadService#checkUploadFiles(java.util.List, java.lang.String)
     */
    public CmsUploadFileBean checkUploadFiles(List<String> fileNames, String targetFolder) {

        List<String> existingResourceNames = new ArrayList<String>();
        List<String> invalidFileNames = new ArrayList<String>();
        boolean isActive = false;

        // check if there is an active upload
        if (getRequest().getSession().getAttribute(CmsUploadBean.SESSION_ATTRIBUTE_LISTENER_ID) == null) {

            // check for existing files
            for (String fileName : fileNames) {

                try {
                    Streams.checkFileName(fileName);
                    String newResName = CmsUploadBean.getNewResourceName(getCmsObject(), fileName, targetFolder);
                    if (getCmsObject().existsResource(newResName, CmsResourceFilter.ALL)) {
                        existingResourceNames.add(fileName);
                    }
                } catch (InvalidFileNameException e) {
                    invalidFileNames.add(fileName);
                }
            }
        } else {
            isActive = true;
        }
        return new CmsUploadFileBean(existingResourceNames, invalidFileNames, isActive);
    }

    /**
     * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadService#getUploadProgressInfo()
     */
    public CmsUploadProgessInfo getUploadProgressInfo() {

        CmsUploadProgessInfo info = new CmsUploadProgessInfo(0, 0, UPLOAD_STATE.notStarted, 0, 0);
        if (getRequest().getSession().getAttribute(CmsUploadBean.SESSION_ATTRIBUTE_LISTENER_ID) != null) {
            CmsUUID listenerId = (CmsUUID)getRequest().getSession().getAttribute(
                CmsUploadBean.SESSION_ATTRIBUTE_LISTENER_ID);
            CmsUploadListener listener = CmsUploadBean.getCurrentListener(listenerId);
            if (listener != null) {
                info = listener.getInfo();
            }
        }
        return info;
    }

    /**
     * @see org.opencms.ade.upload.shared.rpc.I_CmsUploadService#prefetch()
     */
    public CmsUploadData prefetch() {

        long uploadFileSizeLimit = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCmsObject());
        CmsUploadData data = new CmsUploadData(uploadFileSizeLimit);
        return data;
    }
}
