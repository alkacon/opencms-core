/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2004/03/19 14:15:16 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsResourceTypeBinary;
import org.opencms.file.CmsResourceTypeImage;
import org.opencms.file.CmsResourceTypeJsp;
import org.opencms.file.CmsResourceTypePlain;
import org.opencms.file.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @version $Revision: 1.2 $
 * 
 * @since 5.3.3
 */
public class CmsNewResourceUpload extends CmsNewResource {
    
    
    /** The value for the resource name form action */
    public static final int ACTION_NEWFORM2 = 120;
    /** The value for the resource name form submission action */
    public static final int ACTION_SUBMITFORM2 = 130;  
    
    /** The name for the resource form submission action */
    public static final String DIALOG_SUBMITFORM2 = "submitform2";
    
    /** Request parameter name for the upload file unzip flag */
    public static final String PARAM_UNZIPFILE = "unzipfile";
    /** Request parameter name for the upload file name */
    public static final String PARAM_UPLOADFILE = "uploadfile";
    /** Request parameter name for the new resource file name */
    public static final String PARAM_NEWRESOURCENAME = "newresourcename";
    
    /** All allowed resource types for upload, used in form "suggested file type" */
    private static String[] ALLOWED_RESOURCETYPES = new String[] {
            CmsResourceTypeBinary.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypePlain.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypeImage.C_RESOURCE_TYPE_NAME, 
            CmsResourceTypeJsp.C_RESOURCE_TYPE_NAME
    };
   
    private String m_paramUploadFile;
    private String m_paramUnzipFile;
    private String m_paramNewResourceName;
    
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
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.upload"));
        }   
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
     * Uploads the specified file and unzips it, if selected.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionUpload() throws JspException {
        // determine the type of upload
        boolean unzipFile = Boolean.valueOf(getParamUnzipFile()).booleanValue();
        
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
                if (maxFileSizeBytes > 0 && size > maxFileSizeBytes) {
                    throw new CmsException("File size larger than maximum allowed upload size");
                }
                byte[] content = fi.get();
                fi.delete();
                
                if (unzipFile) {
                    // zip file upload
                    String currentFolder = getSettings().getExplorerResource();
                    if (currentFolder == null) {
                        // set current folder to root folder
                        try {
                            currentFolder = getCms().readAbsolutePath(getCms().rootFolder());
                        } catch (CmsException e) {
                            currentFolder = I_CmsConstants.C_ROOT;
                        }
                    }           
                    if (!currentFolder.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                        // add folder separator to currentFolder
                        currentFolder += I_CmsConstants.C_FOLDER_SEPARATOR;
                    }
                    // import the zip contents
                    new CmsImportFolder(content, currentFolder, getCms(), false);             
                   
                } else {
                    // single file upload
                    String newResname = fileName;
                    if (newResname.indexOf(File.separator) != -1) {
                        // remove folder structure from new resource name
                        newResname = newResname.substring(newResname.lastIndexOf(File.separator) + 1);
                    }
                    setParamNewResourceName(newResname);
                    setParamResource(newResname);
                    setParamResource(computeFullResourceName());
                    // determine the resource type id from the given information
                    int resTypeId = computeFileType(newResname, contentType);
                    // create the resource
                    getCms().createResource(getParamResource(), resTypeId, new Hashtable(), content, null);
                }
                fi.delete();
            } else {
                throw new CmsException("Upload file not found");
            }
        } catch (CmsException e) {
            // error uploading file, show error dialog
            getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.upload"));
            setParamReasonSuggestion(key("error.reason.upload") + "<br>\n" + key("error.suggestion.upload") + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
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
     * Updates the file type and renames the file if desired
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionUpdateFile() throws JspException {
        try {
            CmsResource res = getCms().readFileHeader(getParamResource());
            I_CmsResourceType oldType = getCms().getResourceType(res.getType());
            if (!oldType.getResourceTypeName().equals(getParamNewResourceType())) {
                // change the type of the uploaded resource
                int newType = getCms().getResourceTypeId(getParamNewResourceType());
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
     * Builds the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {
        StringBuffer result = new StringBuffer(512);        
        int currentResTypeId = -1;
        try {
            CmsResource res = getCms().readFileHeader(getParamResource());
            currentResTypeId = res.getType();
         
            for (int i=0; i<ALLOWED_RESOURCETYPES.length; i++) {
                String resTypeName = ALLOWED_RESOURCETYPES[i];
                int resTypeId = getCms().getResourceTypeId(resTypeName);
                
                // get explorer type settings for current resource type
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getEplorerTypeSetting(resTypeName);
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
     * Returns the upload file name.<p>
     * 
     * @return the upload file name
     */
    public String getParamUploadFile() {
        return m_paramUploadFile;
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
     * Returns true if the upload file should be unzipped, otherwise false.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public String getParamUnzipFile() {
        return m_paramUnzipFile;
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
     * Returns if the upload file should be unzipped.<p>
     * 
     * @return true if the upload file should be unzipped, otherwise false
     */
    public boolean unzipUpload() {
        return Boolean.valueOf(getParamUnzipFile()).booleanValue();
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
     * Sets the new resource name of the uploaded file.<p>
     * 
     * @param newResourceName the new resource name of the uploaded file
     */
    public void setParamNewResourceName(String newResourceName) {
        m_paramNewResourceName = newResourceName;
    }

}
