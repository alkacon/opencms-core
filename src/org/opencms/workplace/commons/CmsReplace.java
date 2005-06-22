/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsReplace.java,v $
 * Date   : $Date: 2005/06/22 16:06:35 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;

/**
 * The replace resource dialog handles the replacement of a single VFS file.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/replace.jsp
 * </ul>
 * <p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsReplace extends CmsDialog {

    /** The dialog type.<p> */
    public static final String DIALOG_TYPE = "replace";

    /** Request parameter name for the upload file name.<p> */
    public static final String PARAM_UPLOADFILE = "uploadfile";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsReplace(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsReplace(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Uploads the specified file and replaces the VFS file.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionReplace() throws JspException {

        try {
            // get the file item from the multipart request
            Iterator i = getMultiPartFileItems().iterator();
            FileItem fi = null;
            while (i.hasNext()) {
                fi = (FileItem)i.next();
                if (fi.getName() != null) {
                    // found the file object, leave iteration
                    break;
                }
            }

            if (fi != null) {
                // get file object information
                long size = fi.getSize();
                long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());
                // check file size
                if (maxFileSizeBytes > 0 && size > maxFileSizeBytes) {
                    // file size is larger than maximum allowed file size, throw an error
                    throw new CmsException(Messages.get().container(
                        Messages.ERR_FILE_SIZE_TOO_LARGE_1,
                        new Long((maxFileSizeBytes / 1024))));
                }
                byte[] content = fi.get();
                fi.delete();

                // determine the resource type id from the resource to replace
                CmsResource res = getCms().readResource(getParamResource());
                int resTypeId = res.getTypeId();
                // check the lock state and replace resource
                checkLock(getParamResource());
                getCms().replaceResource(getParamResource(), resTypeId, content, null);
                // close dialog
                actionCloseDialog();
            } else {
                throw new CmsException(Messages.get().container(Messages.ERR_UPLOAD_FILE_NOT_FOUND_0));
            }
        } catch (Throwable e) {
            // error replacing file, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed, replace file
            setAction(ACTION_OK);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else {
            // first call of dialog
            setAction(ACTION_DEFAULT);
            // build title for replace resource dialog     
            setParamTitle(key("title.replace") + ": " + CmsResource.getName(getParamResource()));
        }
    }

}
