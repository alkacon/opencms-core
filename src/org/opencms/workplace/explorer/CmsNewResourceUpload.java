/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2005/05/23 12:38:35 $
 * Version: $Revision: 1.12 $
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

package org.opencms.workplace.explorer;

import org.opencms.db.CmsImportFolder;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceException;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsChtype;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;

/**
 * The new resource upload dialog handles the upload of single files or zipped files.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_upload.jsp
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.12 $
 * 
 * @since 5.3.3
 */
public class CmsNewResourceUpload extends CmsNewResource {
    
    /** The value for the resource upload applet action. */
    public static final int ACTION_APPLET = 140;

    /** The value for the resource name form action. */
    public static final int ACTION_NEWFORM2 = 120;

    /** The value for the resource upload applet action: error occured. */
    public static final int ACTION_SHOWERROR = 150;

    /** The value for the resource name form submission action. */
    public static final int ACTION_SUBMITFORM2 = 130;

    /** The name for the resource form submission action. */
    public static final String DIALOG_SHOWERROR = "showerror";

    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM2 = "submitform2";

    /** Request parameter name for the new resource file name. */
    public static final String PARAM_NEWRESOURCENAME = "newresourcename";

    /** Request parameter name for the redirect url. */
    public static final String PARAM_REDIRECTURL = "redirecturl";

    /** Request parameter name for the redirect target frame name. */
    public static final String PARAM_TARGETFRAME = "targetframe";

    /** Request parameter name for the upload file unzip flag. */
    public static final String PARAM_UNZIPFILE = "unzipfile";

    /** Request parameter name for the upload file name. */
    public static final String PARAM_UPLOADERROR = "uploaderror";

    /** Request parameter name for the upload file name. */
    public static final String PARAM_UPLOADFILE = "uploadfile";

    /** Request parameter name for the upload folder name. */
    public static final String PARAM_UPLOADFOLDER = "uploadfolder";

    private String m_paramNewResourceName;
    private String m_paramRedirectUrl;
    private String m_paramTargetFrame;
    private String m_paramUnzipFile;
    private String m_paramUploadError;
    private String m_paramUploadFile;
    private String m_paramUploadFolder;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceUpload(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceUpload(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Used to close the current JSP dialog.<p>
     * 
     * This method overwrites the close dialog method in the super class,
     * because in case a new file was uploaded and the cancel button pressed,
     * the uploaded file has to be deleted.<p>
     *  
     * It tries to include the URI stored in the workplace settings.
     * This URI is determined by the frame name, which has to be set 
     * in the framename parameter.<p>
     * 
     * @throws JspException if including an element fails
     */
    public void actionCloseDialog() throws JspException {

        if (getAction() == ACTION_CANCEL) {
            try {
                getCms().deleteResource(getParamResource(), I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            } catch (Exception e) {
                // file was not present
            }
        }
        super.actionCloseDialog();
    }

    /**
     * Updates the file type and renames the file if desired.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionUpdateFile() throws JspException {

        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            I_CmsResourceType oldType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            if (!oldType.getTypeName().equals(getParamNewResourceType())) {
                // change the type of the uploaded resource
                int newType = OpenCms.getResourceManager().getResourceType(getParamNewResourceType()).getTypeId();
                getCms().chtype(getParamResource(), newType);
            }
            if (getParamNewResourceName() != null && !getParamResource().endsWith(getParamNewResourceName())) {
                String newResourceName = CmsResource.getFolderPath(getParamResource()) + getParamNewResourceName();
                // rename the resource
                getCms().renameResource(getParamResource(), newResourceName);
                setParamResource(newResourceName);
            }
        } catch (CmsException e) {
            // error updating file, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_UPLOAD_FILE_0));
            includeErrorpage(this, e);
        }
    }

    /**
     * Uploads the specified file and unzips it, if selected.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionUpload() throws JspException {

        // determine the type of upload
        boolean unzipFile = Boolean.valueOf(getParamUnzipFile()).booleanValue();
        // Suffix for error messages (e.g. when exceeding the maximum file upload size)

        try {
            // get the file item from the multipart request
            Iterator i = getMultiPartFileItems().iterator();
            FileItem fi = null;
            while (i.hasNext()) {
                fi = (FileItem)i.next();
                if (fi.getName() != null) {
                    // found the file object, leave iteration
                    break;
                } else {
                    // this is no file object, check next item
                    continue;
                }
            }

            if (fi != null) {
                String fileName = fi.getName();
                long size = fi.getSize();
                long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());
                // check file size
                if (maxFileSizeBytes > 0 && size > maxFileSizeBytes) {
                    // file size is larger than maximum allowed file size, throw an error
                    throw new CmsWorkplaceException(Messages.get().container(
                        Messages.ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1, new Long(maxFileSizeBytes / 1024)));
                }
                byte[] content = fi.get();
                fi.delete();

                if (unzipFile) {
                    // zip file upload
                    String currentFolder = getParamUploadFolder();
                    if (CmsStringUtil.isEmpty(currentFolder)) {
                        // no upload folder parameter found, get current folder
                        currentFolder = getParamCurrentFolder();
                    }
                    if (CmsStringUtil.isEmpty(currentFolder) || !currentFolder.startsWith("/")) {
                        // no folder information found, guess upload folder
                        currentFolder = computeCurrentFolder();
                    }
                    // import the zip contents
                    new CmsImportFolder(content, currentFolder, getCms(), false);

                } else {
                    // single file upload
                    String newResname = getCms().getRequestContext().getFileTranslator().translateResource(
                        CmsResource.getName(fileName.replace('\\', '/')));
                    setParamNewResourceName(newResname);
                    setParamResource(newResname);
                    setParamResource(computeFullResourceName());
                    // determine the resource type id from the given information
                    int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();
                    try {
                        // create the resource
                        getCms().createResource(getParamResource(), resTypeId, content, Collections.EMPTY_LIST);
                    } catch (CmsException e) {
                        // resource was present, overwrite it
                        getCms().lockResource(getParamResource());
                        getCms().replaceResource(getParamResource(), resTypeId, content, null);
                    }
                }
            } else {
                throw new CmsWorkplaceException(Messages.get().container(Messages.ERR_UPLOAD_FILE_NOT_FOUND_0));
            }
        } catch (CmsException e) {
            // error uploading file, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_UPLOAD_FILE_0));
            includeErrorpage(this, e);   
        }
    }

    /**
     * Builds the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {

        return CmsChtype.buildTypeList(this, false);
    }

    /**
     * Creates the HTML code of the file upload applet with all required parameters.<p>
     * 
     * @return string containing the applet HTML code
     */
    public String createAppletCode() {

        StringBuffer applet = new StringBuffer(2048);

        // collect some required server data first
        String scheme = getJsp().getRequest().getScheme();
        String host = getJsp().getRequest().getServerName();
        String path = OpenCms.getSystemInfo().getContextPath() + OpenCms.getSystemInfo().getServletPath();
        int port = getJsp().getRequest().getServerPort();
        String webapp = scheme + "://" + host + ":" + port + OpenCms.getSystemInfo().getContextPath();

        // get all file extensions
        String fileExtensions = new String("");
        Map extensions = OpenCms.getResourceManager().getExtensionMapping();
        Iterator keys = extensions.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            String value = (String)extensions.get(key);
            fileExtensions += key + "=" + value + ",";
        }
        fileExtensions = fileExtensions.substring(0, fileExtensions.length() - 1);

        // get the file size upload limitation value (value is in bytes for the applet)
        long maxFileSize = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());

        // get the current folder
        String currentFolder = getParamCurrentFolder();

        // get the current session id
        HttpSession session = getJsp().getRequest().getSession(false);
        String sessionId = session.getId();

        // define the required colors.
        // currently this is hard coded here       
        String colors = "bgColor=#C0C0C0,outerBorderRightBottom=#333333,outerBorderLeftTop=#C0C0C0";
        colors += ",innerBorderRightBottom=#777777,innerBorderLeftTop=#F0F0F0";
        colors += ",bgHeadline=#000066,colorHeadline=#FFFFFF";
        colors += ",colorText=#000000,progessBar=#E10050";

        // create the upload applet html code
        applet.append("<applet code=\"org.opencms.applet.upload.FileUploadApplet.class\" archive=\"");
        applet.append(webapp);
        applet.append("/resources/components/upload_applet/upload.jar\" width=\"500\" height=\"100\">\n");
        applet.append("<param name=\"opencms\" value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(getSkinUri());
        applet.append("filetypes/\">\n");
        applet.append("<param name=\"target\" value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        applet.append("/system/workplace/commons/newresource_upload.jsp\">\n");
        applet.append("<param name=\"redirect\" value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        // check if the redirect url is given by request parameter. if not use the default
        if (CmsStringUtil.isEmpty(getParamRedirectUrl())) {
            applet.append(CmsWorkplace.C_FILE_EXPLORER_FILELIST);
        } else {
            applet.append(getParamRedirectUrl());
        }
        // append some parameters to prevent caching of URL by Applet
        applet.append("?time=" + System.currentTimeMillis());
        applet.append("\">\n");
        applet.append("<param name=\"targetframe\" value=\"");
        applet.append(getParamTargetFrame());
        applet.append("\">\n");
        applet.append("<param name=error value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        applet.append("/system/workplace/action/explorer_files_new_upload.html\">\n");
        applet.append("<param name=\"sessionId\" value=\"");
        applet.append(sessionId);
        applet.append("\">\n");
        applet.append("<param name=\"filelist\" value=\"");
        applet.append(currentFolder);
        applet.append("\">\n");
        applet.append("<param name=\"colors\" value=\"");
        applet.append(colors);
        applet.append("\">\n");
        applet.append("<param name=\"fileExtensions\" value=\"");
        applet.append(fileExtensions);
        applet.append("\">\n\n");
        applet.append("<param name=\"maxsize\" value=\"");
        applet.append(maxFileSize);
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputSelect\" value=\"");
        applet.append(key("uploadapplet.action.select"));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputCount\"value=\"");
        applet.append(key("uploadapplet.action.count"));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputCreate\" value=\"");
        applet.append(key("uploadapplet.action.create"));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputUpload\" value=\"");
        applet.append(key("uploadapplet.action.upload"));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputUpload\" value=\"");
        applet.append(key("uploadapplet.message.upload"));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputErrorZip\" value=\"");
        applet.append(key("uploadapplet.message.error.zip"));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputErrorSize\" value=\"");
        applet.append(key("uploadapplet.message.error.size"));
        applet.append("\">\n");
        applet.append("<param name=\"messageNoPreview\" value=\"");
        applet.append(key("uploadapplet.message.nopreview"));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputAdding\" value=\"");
        applet.append(key("uploadapplet.message.adding"));
        applet.append(" \">\n");
        applet.append("<param name=\"errorTitle\" value=\"");
        applet.append(key("uploadapplet.error.title"));
        applet.append(" \">\n");
        applet.append("<param name=\"errorLine1\" value=\"");
        applet.append(key("uploadapplet.error.line1"));
        applet.append(" \">\n");
        applet.append("</applet>\n");

        return applet.toString();

    }

    /**
     * Returns the new resource name of the uploaded file.<p>
     * 
     * @return the new resource name of the uploaded file
     */
    public String getParamNewResourceName() {

        return m_paramNewResourceName;
    }

    /**
     * Returns the paramRedirectUrl.<p>
     *
     * @return the paramRedirectUrl
     */
    public String getParamRedirectUrl() {

        return m_paramRedirectUrl;
    }

    /**
     * Returns the paramTargetFrame.<p>
     *
     * @return the paramTargetFrame
     */
    public String getParamTargetFrame() {

        if (CmsStringUtil.isEmpty(m_paramTargetFrame)) {
            return new String("explorer_files");
        }

        return m_paramTargetFrame;
    }

    /**
     * Returns true if the upload file should be unzipped, otherwise false.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public String getParamUnzipFile() {

        return m_paramUnzipFile;
    }

    /**
     * Returns the upload error message for the error dialog.<p>
     * 
     * @return the upload error message for the error dialog
     */
    public String getParamUploadError() {

        return m_paramUploadError;
    }

    /**
     * Returns the upload file name.<p>
     * 
     * @return the upload file name
     */
    public String getParamUploadFile() {

        return m_paramUploadFile;
    }

    /**
     * Returns the upload folder name.<p>
     * 
     * @return the upload folder name
     */
    public String getParamUploadFolder() {

        return m_paramUploadFolder;
    }

    /**
     * Sets the new resource name of the uploaded file.<p>
     * 
     * @param newResourceName the new resource name of the uploaded file
     */
    public void setParamNewResourceName(String newResourceName) {

        m_paramNewResourceName = newResourceName;
    }

    /**
     * Sets the paramRedirectUrl.<p>
     *
     * @param paramRedirectUrl the paramRedirectUrl to set
     */
    public void setParamRedirectUrl(String paramRedirectUrl) {

        m_paramRedirectUrl = paramRedirectUrl;
    }

    /**
     * Sets the paramTargetFrame.<p>
     *
     * @param paramTargetFrame the paramTargetFrame to set
     */
    public void setParamTargetFrame(String paramTargetFrame) {

        m_paramTargetFrame = paramTargetFrame;
    }

    /**
     * Sets if the upload file should be unzipped.<p>
     * 
     * @param unzipFile true if the upload file should be unzipped
     */
    public void setParamUnzipFile(String unzipFile) {

        m_paramUnzipFile = unzipFile;
    }

    /**
     * Sets the upload error message for the error dialog.<p>
     * 
     * @param uploadError the upload error message for the error dialog
     */
    public void setParamUploadError(String uploadError) {

        m_paramUploadError = uploadError;
    }

    /**
     * Sets the upload file name.<p>
     * 
     * @param uploadFile the upload file name
     */
    public void setParamUploadFile(String uploadFile) {

        m_paramUploadFile = uploadFile;
    }

    /**
     * Sets the upload folder name.<p>
     * 
     * @param uploadFolder the upload folder name
     */
    public void setParamUploadFolder(String uploadFolder) {

        m_paramUploadFolder = uploadFolder;
    }

    /**
     * Returns if the upload file should be unzipped.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public boolean unzipUpload() {

        return Boolean.valueOf(getParamUnzipFile()).booleanValue();
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
            setAction(ACTION_OK);
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);
        } else if (DIALOG_SUBMITFORM2.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM2);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            if (getSettings().getUserSettings().useUploadApplet()) {
                setAction(ACTION_APPLET);
            } else {
                setAction(ACTION_DEFAULT);
            }
            // build title for new resource dialog     
            setParamTitle(key("title.upload"));
        }
    }

}
