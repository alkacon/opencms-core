/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2004/07/05 16:32:42 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace;

import org.opencms.db.CmsImportFolder;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

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
 * <li>/jsp/dialogs/newresource_upload.html
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.14 $
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
    
    /** All allowed resource types for upload, used in form "suggested file type". */
    private static String[] ALLOWED_RESOURCETYPES = new String[] {
            // TODO: This must be made configurable in opencms-workplace.xml
            CmsResourceTypeBinary.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypePlain.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypeImage.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypeJsp.C_RESOURCE_TYPE_NAME
    };
    
    /** The name for the resource form submission action. */
    public static final String DIALOG_SHOWERROR = "showerror";   
    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM2 = "submitform2";  
    
    /** Request parameter name for the new resource file name. */
    public static final String PARAM_NEWRESOURCENAME = "newresourcename";
    /** Request parameter name for the upload file unzip flag. */
    public static final String PARAM_UNZIPFILE = "unzipfile";
    /** Request parameter name for the upload file name. */
    public static final String PARAM_UPLOADERROR = "uploaderror";
    /** Request parameter name for the upload file name. */
    public static final String PARAM_UPLOADFILE = "uploadfile";
    /** Request parameter name for the upload folder name. */
    public static final String PARAM_UPLOADFOLDER = "uploadfolder";
    
    private String m_paramNewResourceName;
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
                // rename the resource
                getCms().renameResource(getParamResource(), getParamNewResourceName());
                // determine new full resource name
                String newResName = getParamResource().substring(0, getParamResource().lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR) + 1);
                newResName += getParamNewResourceName();
                setParamResource(newResName);
            }
        } catch (CmsException e) {
            // error updating file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.upload"));
            setParamReasonSuggestion(key("error.reason.upload") + "<br>\n" + key("error.suggestion.upload") + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
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
        String errorMsgSuffix = "";
        
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
                String contentType = fi.getContentType();
                long size = fi.getSize();
                long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms());
                // check file size
                if (maxFileSizeBytes > 0 && size > maxFileSizeBytes) {
                    // file size is larger than maximum allowed file size, throw an error
                    errorMsgSuffix = "size";
                    throw new CmsException("File size larger than maximum allowed upload size, currently set to " + (maxFileSizeBytes / 1024) + " kb");
                }
                byte[] content = fi.get();
                fi.delete();
                
                if (unzipFile) {
                    // zip file upload
                    String currentFolder = getParamUploadFolder();
                    if (currentFolder == null || !currentFolder.startsWith("/")) {
                        currentFolder = computeCurrentFolder();
                    }
                    // import the zip contents
                    new CmsImportFolder(content, currentFolder, getCms(), false);             
                   
                } else {
                    // single file upload
                    String newResname = getCms().getRequestContext().getFileTranslator().translateResource(CmsResource.getName(fileName.replace('\\', '/')));
                    setParamNewResourceName(newResname);
                    setParamResource(newResname);
                    setParamResource(computeFullResourceName());
                    // determine the resource type id from the given information
                    int resTypeId = computeFileType(newResname, contentType);
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
                throw new CmsException("Upload file not found");
            }
        } catch (CmsException e) {
            // error uploading file, show error dialog
            setAction(ACTION_SHOWERROR);
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.upload"));
            setParamReasonSuggestion(key("error.reason.upload" + errorMsgSuffix) + "<br>\n" + key("error.suggestion.upload" + errorMsgSuffix) + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Includes the error dialog if the upload applet has an error.<p>
     */
    private void actionUploadError() {
        // error uploading file, show error dialog
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        setParamErrorstack(getParamUploadError());
        setParamMessage(key("error.message.upload"));
        setParamReasonSuggestion(key("error.reason.upload") + "<br>\n" + key("error.suggestion.upload") + "\n");
        try {
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        } catch (JspException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error including error dialog " + C_FILE_DIALOG_SCREEN_ERROR);
            }
        }
    }
    
    /**
     * Builds the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {
        StringBuffer result = new StringBuffer(512);        
        int currentResTypeId = -1;
        try {
            CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            currentResTypeId = res.getTypeId();
         
            for (int i=0; i<ALLOWED_RESOURCETYPES.length; i++) {
                int resTypeId = OpenCms.getResourceManager().getResourceType(ALLOWED_RESOURCETYPES[i]).getTypeId();                
                // get explorer type settings for current resource type
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(ALLOWED_RESOURCETYPES[i]);
                if (settings != null) {
                    result.append("<tr><td>");
                    result.append("<input type=\"radio\" name=\"" + PARAM_NEWRESOURCETYPE + "\" value=\"" + settings.getName() + "\"");
                    if (resTypeId == currentResTypeId) {
                        result.append(" checked=\"checked\"");
                    }
                    result.append("></td>");
                    result.append("\t<td><img src=\"" + getSkinUri() + "filetypes/" + settings.getIcon() + "\" border=\"0\" title=\"" + key(settings.getKey()) + "\"></td>\n");
                    result.append("<td>" + key(settings.getKey()));
                    result.append("</td></tr>\n");
                }
            }
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error building resource type list for " + getParamResource());
            }
        }      
        return result.toString();
    }
    
    /**
     * Returns the resource type id of the given file.<p>
     * 
     * @param fileName the name of the file (needed for JSP recognition)
     * @param contentType the mime type String of the file
     * @return the resource type id of the given file
     */
    protected int computeFileType(String fileName, String contentType) {
        fileName = fileName.toLowerCase();
        contentType = contentType.toLowerCase();
        if (fileName.endsWith(".jsp")) {
            return CmsResourceTypeJsp.C_RESOURCE_TYPE_ID;
        } else if (contentType.indexOf("image") != -1) {
            return CmsResourceTypeImage.C_RESOURCE_TYPE_ID;
        } else if (contentType.indexOf("text") != -1) {
            return CmsResourceTypePlain.C_RESOURCE_TYPE_ID;
        } else {
            return CmsResourceTypeBinary.C_RESOURCE_TYPE_ID;
        }
    }
    
    /**
     * Creates the HTML code of the file upload applet with all required parameters.<p>
     * 
     * @return string containing the applet HTML code
     * @throws CmsException if reading file extensions goes wrong
     */
    public String createAppletCode() throws CmsException {
        
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
        fileExtensions=fileExtensions.substring(0, fileExtensions.length()-1);
        
        // get the file size upload limitation value (value is in bytes for the applet)
        long maxFileSize = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCms()); 
        
        // get the current folder
        String currentFolder = computeCurrentFolder();
        
        // get the current session id
        HttpSession session = getJsp().getRequest().getSession(false);
        String sessionId = session.getId();
        
        
        // define the required colors.
        // currently this is hard coded here       
        String colors="bgColor=#C0C0C0,outerBorderRightBottom=#333333,outerBorderLeftTop=#C0C0C0";
        colors += ",innerBorderRightBottom=#777777,innerBorderLeftTop=#F0F0F0";  
        colors += ",bgHeadline=#000066,colorHeadline=#FFFFFF";
        colors += ",colorText=#000000,progessBar=#E10050";
        
        // create the upload applet html code
        applet.append("<applet code=\"org.opencms.applet.upload.FileUploadApplet.class\" archive=\"");
        applet.append(webapp);
        applet.append("/skins/components/upload/applet/upload.jar\" width=\"500\" height=\"100\">\n");                
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
        applet.append("/system/workplace/jsp/dialogs/newresource_upload.html\">\n");
        applet.append("<param name=\"redirect\" value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        applet.append("/system/workplace/jsp/explorer_files.html\">\n");
        applet.append("<param name=error value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        applet.append("/system/workplace/action/explorer_files_new_upload.html\">\n");
        applet.append("<param name=\"browserCookie\" value=\"JSESSIONID=");
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
        } else if (DIALOG_SHOWERROR.equals(getParamAction())) {
            setAction(ACTION_SHOWERROR);
            actionUploadError();
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

    /**
     * Sets the new resource name of the uploaded file.<p>
     * 
     * @param newResourceName the new resource name of the uploaded file
     */
    public void setParamNewResourceName(String newResourceName) {
        m_paramNewResourceName = newResourceName;
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

}
