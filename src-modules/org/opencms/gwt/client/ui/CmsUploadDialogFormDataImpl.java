/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsUploadDialogFormDataImpl.java,v $
 * Date   : $Date: 2011/02/22 16:34:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Provides the upload dialog for form data support.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsUploadDialogFormDataImpl extends A_CmsUploadDialog {

    /**
     * @see org.opencms.gwt.client.ui.A_CmsUploadDialog#createInfoBean(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public CmsListInfoBean createInfoBean(CmsFileInfo file) {

        return new CmsListInfoBean(file.getFileName(), formatBytes(file.getFileSize()), null);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsUploadDialog#isTooLarge(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public boolean isTooLarge(CmsFileInfo cmsFileInfo) {

        long maxFileSize = CmsCoreProvider.get().getUploadFileSizeLimit();
        if (maxFileSize < 0) {
            return false;
        }
        return cmsFileInfo.getFileSize() > maxFileSize;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsUploadDialog#submit()
     */
    @Override
    public void submit() {

        List<String> orderedFilenamesToUpload = new ArrayList<String>(getFilesToUpload().keySet());
        Collections.sort(orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);

        // create a JsArray containing the files to upload
        JsArray<CmsFileInfo> filesToUpload = JavaScriptObject.createArray().cast();
        for (String filename : orderedFilenamesToUpload) {
            filesToUpload.push(getFilesToUpload().get(filename));
        }
        upload(
            CmsCoreProvider.get().getUploadUri(),
            CmsCoreData.UPLOAD_TARGET_FOLDER_FIELD_NAME,
            getTargetFolder(),
            filesToUpload,
            this);
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        setContentLength(calculateContentLength());
        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<p class=\"").append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessage()).append("\">");
        buffer.append("<b>" + Messages.get().key(Messages.GUI_UPLOAD_SUMMARY_FILES_0) + "</b> ");
        buffer.append(Messages.get().key(
            Messages.GUI_UPLOAD_SUMMARY_FILES_VALUE_3,
            new Integer(getFilesToUpload().size()),
            getFileText(),
            formatBytes(new Long(getContentLength()).intValue())));
        buffer.append("</p>");
        setSummaryHTML(buffer.toString());
    }

    /**
     * Returns the content length.<p>
     * 
     * @return the content length
     */
    protected long calculateContentLength() {

        int result = 0;
        for (CmsFileInfo file : getFilesToUpload().values()) {
            result += file.getFileSize();
        }
        return new Long(result).longValue();
    }

    /**
     * Sends a post request to the upload JSP.<p>
     * 
     * @param uploadUri the URI of the JSP that performs the upload
     * @param targetFolder the target folder to upload
     * @param dialog this dialog
     * @param jsForm the form
     */
    private native void upload(
        String uploadUri,
        String targetFolderFieldName,
        String targetFolder,
        JsArray<CmsFileInfo> filesToUpload,
        CmsUploadDialogFormDataImpl dialog) /*-{

		var data = new FormData();

		for (i = 0; i < filesToUpload.length; i++) {
			data.append("file_" + i, filesToUpload[i]);
		}
		data.append(targetFolderFieldName, targetFolder);

		var xhr = new XMLHttpRequest();
		xhr.open("POST", uploadUri, true);
		xhr.onreadystatechange = function() {
			if (xhr.readyState == 4) {
				if (xhr.status == 200) {
					dialog.@org.opencms.gwt.client.ui.CmsUploadDialogFormDataImpl::parseResponse(Ljava/lang/String;)(xhr.responseText);
				} else {
					dialog.@org.opencms.gwt.client.ui.CmsUploadDialogFormDataImpl::showErrorReport(Ljava/lang/String;Ljava/lang/String;)(xhr.statusText, null);
				}
			}
		}
		xhr.send(data);

    }-*/;

}
