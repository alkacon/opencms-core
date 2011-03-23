/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/editors/fckeditor/CmsFCKEditorFileBrowser.java,v $
 * Date   : $Date: 2011/03/23 14:53:09 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.editors.fckeditor;

import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.CmsXmlUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Implements the OpenCms Connector for integration of the FCKeditor file browser.<p>
 * 
 * Supports browsing the OpenCms virtual file system (VFS), creating folders and uploading files to the VFS.<br>
 * Details about the connector implementation of the FCKeditor file browser can be 
 * found at http://wiki.fckeditor.net/Developer%27s_Guide/Participating/Server_Side_Integration.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.1.7
 */
public class CmsFCKEditorFileBrowser extends CmsDialog {

    /** Value for the action: create folder. */
    public static final int ACTION_CREATEFOLDER = 502;

    /** Value for the action: upload file. */
    public static final int ACTION_FILEUPLOAD = 503;

    /** Value for the action: get folders. */
    public static final int ACTION_GETFOLDERS = 500;

    /** Value for the action: get folders and files. */
    public static final int ACTION_GETFOLDERS_FILES = 501;

    /** Attribute name for the command attribute. */
    public static final String ATTR_COMMAND = "command";

    /** Attribute name for the name attribute. */
    public static final String ATTR_NAME = "name";

    /** Attribute name for the number attribute. */
    public static final String ATTR_NUMBER = "number";

    /** Attribute name for the path attribute. */
    public static final String ATTR_PATH = "path";

    /** Attribute name for the resourceType attribute. */
    public static final String ATTR_RESOURCETYPE = "resourceType";

    /** Attribute name for the size attribute. */
    public static final String ATTR_SIZE = "size";

    /** Attribute name for the url attribute. */
    public static final String ATTR_URL = "url";

    /** Name for the create folder command. */
    public static final String COMMAND_CREATEFOLDER = "CreateFolder";

    /** Name for the file upload command. */
    public static final String COMMAND_FILEUPLOAD = "FileUpload";

    /** Name for the get folders command. */
    public static final String COMMAND_GETFOLDERS = "GetFolders";

    /** Name for the get folders and files command. */
    public static final String COMMAND_GETFOLDERS_FILES = "GetFoldersAndFiles";

    /** Content type setting HTML for the response. */
    public static final String CONTENTTYPE_HTML = "text/html";

    /** Content type setting XML for the response. */
    public static final String CONTENTTYPE_XML = "text/xml";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "FCKeditor_file_browser";

    /** Error code for creating folders: folder already exists. */
    public static final String ERROR_CREATEFOLDER_EXISTS = "101";

    /** Error code for creating folders: invalid folder name. */
    public static final String ERROR_CREATEFOLDER_INVALIDNAME = "102";

    /** Error code for creating folders: no permissions. */
    public static final String ERROR_CREATEFOLDER_NOPERMISSIONS = "103";

    /** Error code for creating folders: all ok. */
    public static final String ERROR_CREATEFOLDER_OK = "0";

    /** Error code for creating folders: unknown error. */
    public static final String ERROR_CREATEFOLDER_UNKNOWNERROR = "110";

    /** Error code for uploading files: invalid file. */
    public static final String ERROR_UPLOAD_INVALID = "202";

    /** Error code for uploading files: all ok. */
    public static final String ERROR_UPLOAD_OK = "0";

    /** Node name for the Connector node. */
    public static final String NODE_CONNECTOR = "Connector";

    /** Node name for the CurrentFolder node. */
    public static final String NODE_CURRENTFOLDER = "CurrentFolder";

    /** Node name for the Error node. */
    public static final String NODE_ERROR = "Error";

    /** Node name for the File node. */
    public static final String NODE_FILE = "File";

    /** Node name for the Files node. */
    public static final String NODE_FILES = "Files";

    /** Node name for the Folder node. */
    public static final String NODE_FOLDER = "Folder";

    /** Node name for the Folders node. */
    public static final String NODE_FOLDERS = "Folders";

    /** Request parameter name for the command. */
    public static final String PARAM_COMMAND = "Command";

    /** Request parameter name for the current folder. */
    public static final String PARAM_CURRENTFOLDER = "CurrentFolder";

    /** Request parameter name for the new folder name. */
    public static final String PARAM_NEWFOLDERNAME = "NewFolderName";

    /** Request parameter name for the server path. */
    public static final String PARAM_SERVERPATH = "ServerPath";

    /** Request parameter name for the type. */
    public static final String PARAM_TYPE = "Type";

    /** Name for the browser resource type "File". */
    public static final String TYPE_FILE = "File";

    /** Name for the browser resource type "Flash". */
    public static final String TYPE_FLASH = "Flash";

    /** Name for the browser resource type "Image". */
    public static final String TYPE_IMAGE = "Image";

    /** Name for the browser resource type "Media". */
    public static final String TYPE_MEDIA = "Media";

    /** The XML document that is returned in the response. */
    private Document m_document;

    /** The list of multi part file items (if available). */
    private List<FileItem> m_multiPartFileItems;

    /** The Command parameter. */
    private String m_paramCommand;

    /** The CurrentFolder parameter. */
    private String m_paramCurrentFolder;

    /** The NewFolderName parameter. */
    private String m_paramNewFolderName;

    /** The ServerPath parameter. */
    private String m_paramServerPath;

    /** The Type parameter. */
    private String m_paramType;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFCKEditorFileBrowser(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsFCKEditorFileBrowser(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the output for the file browser depending on the executed command.<p>
     * 
     * @return the output for the file browser depending on the executed command
     */
    public String displayDialog() {

        switch (getAction()) {
            case ACTION_CREATEFOLDER:
                return createFolder();
            case ACTION_FILEUPLOAD:
                return uploadFile();
            case ACTION_GETFOLDERS:
                return getFolders(false);
            case ACTION_GETFOLDERS_FILES:
            default:
                return getFolders(true);
        }
    }

    /**
     * Fills all class parameter values from the data provided in the current request.<p>
     * 
     * For this class, the parameters are filled manually from the request, because the needed parameter
     * names for the file browser are in mixed case and not lower case.<p>
     * 
     * @param request the current JSP request
     */
    @Override
    public void fillParamValues(HttpServletRequest request) {

        // ensure a multipart request is parsed only once (for "forward" scenarios with reports)
        if (null == request.getAttribute(REQUEST_ATTRIBUTE_MULTIPART)) {
            // check if this is a multipart request 
            m_multiPartFileItems = CmsRequestUtil.readMultipartFileItems(request);
            if (m_multiPartFileItems != null) {
                // this was indeed a multipart form request
                CmsRequestUtil.readParameterMapFromMultiPart(
                    getCms().getRequestContext().getEncoding(),
                    m_multiPartFileItems);
                request.setAttribute(REQUEST_ATTRIBUTE_MULTIPART, Boolean.TRUE);
            }
        }

        // manually fill the required request parameters in the members
        setParamCommand(decodeParamValue(PARAM_COMMAND, request.getParameter(PARAM_COMMAND)));
        setParamCurrentFolder(decodeParamValue(PARAM_CURRENTFOLDER, request.getParameter(PARAM_CURRENTFOLDER)));
        setParamNewFolderName(decodeParamValue(PARAM_NEWFOLDERNAME, request.getParameter(PARAM_NEWFOLDERNAME)));
        setParamServerPath(decodeParamValue(PARAM_SERVERPATH, request.getParameter(PARAM_SERVERPATH)));
        setParamType(decodeParamValue(PARAM_TYPE, request.getParameter(PARAM_TYPE)));
    }

    /**
     * Returns the Command parameter.<p>
     * 
     * @return the Command parameter
     */
    public String getParamCommand() {

        return m_paramCommand;
    }

    /**
     * Returns the CurrentFolder parameter.<p>
     * 
     * @return the CurrentFolder parameter
     */
    public String getParamCurrentFolder() {

        return m_paramCurrentFolder;
    }

    /**
     * Returns the NewFolderName parameter.<p>
     * 
     * @return the NewFolderName parameter
     */
    public String getParamNewFolderName() {

        return m_paramNewFolderName;
    }

    /**
     * Returns the ServerPath parameter.<p>
     * 
     * @return the ServerPath parameter
     */
    public String getParamServerPath() {

        return m_paramServerPath;
    }

    /**
     * Returns the Type parameter.<p>
     * 
     * @return the Type parameter
     */
    public String getParamType() {

        return m_paramType;
    }

    /**
     * Sets the Command parameter.<p>
     * 
     * @param paramCommand the Command parameter
     */
    public void setParamCommand(String paramCommand) {

        m_paramCommand = paramCommand;
    }

    /**
     * Sets the CurrentFolder parameter.<p>
     * 
     * @param paramCurrentFolder the CurrentFolder parameter
     */
    public void setParamCurrentFolder(String paramCurrentFolder) {

        if (CmsStringUtil.isEmpty(paramCurrentFolder)) {
            m_paramCurrentFolder = "/";
        } else {
            m_paramCurrentFolder = paramCurrentFolder;
        }
    }

    /**
     * Sets the NewFolderName parameter.<p>
     * 
     * @param paramNewFolderName the NewFolderName parameter
     */
    public void setParamNewFolderName(String paramNewFolderName) {

        m_paramNewFolderName = paramNewFolderName;
    }

    /**
     * Sets the ServerPath parameter.<p>
     * 
     * @param paramServerPath the ServerPath parameter
     */
    public void setParamServerPath(String paramServerPath) {

        if (CmsStringUtil.isEmpty(paramServerPath)) {
            m_paramServerPath = OpenCms.getSystemInfo().getOpenCmsContext() + getParamCurrentFolder();
        } else {
            m_paramServerPath = OpenCms.getSystemInfo().getOpenCmsContext() + paramServerPath;
        }
    }

    /**
     * Sets the Type parameter.<p>
     * 
     * @param paramType the Type parameter
     */
    public void setParamType(String paramType) {

        if (CmsStringUtil.isEmpty(paramType)) {
            m_paramType = "";
        } else {
            m_paramType = paramType;
        }
    }

    /**
     * Creates a folder in the file browser and returns the XML containing the error code.<p>
     * 
     * @return the XML containing the error code for the folder creation
     */
    protected String createFolder() {

        createXMLHead();
        Element error = getDocument().getRootElement().addElement(NODE_ERROR);
        try {
            getCms().createResource(
                getParamCurrentFolder() + getParamNewFolderName(),
                CmsResourceTypeFolder.RESOURCE_TYPE_ID);
            // no error occurred, return error code 0
            error.addAttribute(ATTR_NUMBER, ERROR_CREATEFOLDER_OK);
        } catch (Exception e) {
            // check cause of error to return a specific error code
            if (e instanceof CmsVfsResourceAlreadyExistsException) {
                // resource already exists
                error.addAttribute(ATTR_NUMBER, ERROR_CREATEFOLDER_EXISTS);
            } else if (e instanceof CmsIllegalArgumentException) {
                // invalid folder name
                error.addAttribute(ATTR_NUMBER, ERROR_CREATEFOLDER_INVALIDNAME);
            } else if (e instanceof CmsPermissionViolationException) {
                // no permissions to create the folder
                error.addAttribute(ATTR_NUMBER, ERROR_CREATEFOLDER_NOPERMISSIONS);
            } else {
                // unknown error
                error.addAttribute(ATTR_NUMBER, ERROR_CREATEFOLDER_UNKNOWNERROR);
            }

        }

        try {
            return CmsXmlUtils.marshal(getDocument(), CmsEncoder.ENCODING_UTF_8);
        } catch (CmsException e) {
            // should never happen
            return "";
        }
    }

    /**
     * Creates the XML head that is used for every XML file browser response except the upload response.<p>
     */
    protected void createXMLHead() {

        // add the connector node
        Element connector = getDocument().addElement(NODE_CONNECTOR);
        connector.addAttribute(ATTR_COMMAND, getParamCommand());
        connector.addAttribute(ATTR_RESOURCETYPE, getParamType());
        Element currFolder = connector.addElement(NODE_CURRENTFOLDER);
        currFolder.addAttribute(ATTR_PATH, getParamCurrentFolder());
        currFolder.addAttribute(ATTR_URL, getParamServerPath());

    }

    /**
     * Returns the XML document instance that is used to build the response XML.<p>
     * 
     * @return the XML document instance that is used to build the response XML
     */
    protected Document getDocument() {

        if (m_document == null) {
            m_document = DocumentHelper.createDocument();
        }
        return m_document;
    }

    /**
     * Returns the XML to list folders and/or files in the file browser window.<p>
     * 
     * @param includeFiles flag to indicate if files are included
     * @return the XML to list folders and/or files in the file browser window
     */
    protected String getFolders(boolean includeFiles) {

        createXMLHead();
        Element folders = getDocument().getRootElement().addElement(NODE_FOLDERS);
        Element files = null;

        // generate resource filter
        CmsResourceFilter filter;
        if (includeFiles) {
            // create filter to get folders and files
            filter = CmsResourceFilter.DEFAULT.addRequireVisible();
            files = getDocument().getRootElement().addElement(NODE_FILES);
        } else {
            // create filter to get only folders
            filter = CmsResourceFilter.DEFAULT_FOLDERS.addRequireVisible();
        }

        try {
            List<CmsResource> resources = getCms().readResources(getParamCurrentFolder(), filter, false);
            Iterator<CmsResource> i = resources.iterator();
            while (i.hasNext()) {
                CmsResource res = i.next();
                if (res.isFolder()) {
                    // resource is a folder, create folder node
                    Element folder = folders.addElement(NODE_FOLDER);
                    String folderName = CmsResource.getName(res.getRootPath());
                    folderName = CmsStringUtil.substitute(folderName, "/", "");
                    folder.addAttribute(ATTR_NAME, folderName);
                } else {
                    // resource is a file
                    boolean showFile = true;
                    // check if required file type is an image and filter found resources if set
                    if (TYPE_IMAGE.equals(getParamType())) {
                        showFile = (res.getTypeId() == CmsResourceTypeImage.getStaticTypeId());
                    }
                    if ((showFile) && (files != null)) {
                        // create file node
                        Element file = files.addElement(NODE_FILE);
                        file.addAttribute(ATTR_NAME, CmsResource.getName(res.getRootPath()));
                        file.addAttribute(ATTR_SIZE, "" + (res.getLength() / 1024));
                    }
                }

            }
            return CmsXmlUtils.marshal(getDocument(), CmsEncoder.ENCODING_UTF_8);
        } catch (CmsException e) {
            // error getting resource list, return empty String
            return "";
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods and check for multipart file items
        fillParamValues(request);

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (COMMAND_FILEUPLOAD.equals(getParamCommand())) {
            // upload file
            setAction(ACTION_FILEUPLOAD);
        } else if (COMMAND_CREATEFOLDER.equals(getParamCommand())) {
            // create folder
            setAction(ACTION_CREATEFOLDER);
        } else if (COMMAND_GETFOLDERS.equals(getParamCommand())) {
            // get folders
            setAction(ACTION_GETFOLDERS);
        } else {
            // default: get files and folders
            setAction(ACTION_GETFOLDERS_FILES);
        }

        // get the top response
        CmsFlexController controller = CmsFlexController.getController(getJsp().getRequest());
        HttpServletResponse res = controller.getTopResponse();
        // set the response headers depending on the command to execute
        CmsRequestUtil.setNoCacheHeaders(res);
        String contentType = CONTENTTYPE_XML;
        if (getAction() == ACTION_FILEUPLOAD) {
            contentType = CONTENTTYPE_HTML;
        }
        res.setContentType(contentType);
    }

    /**
     * Uploads a file to the OpenCms VFS and returns the necessary JavaScript for the file browser.<p>
     * 
     * @return the necessary JavaScript for the file browser
     */
    protected String uploadFile() {

        String errorCode = ERROR_UPLOAD_OK;
        try {
            // get the file item from the multipart request
            Iterator<FileItem> i = m_multiPartFileItems.iterator();
            FileItem fi = null;
            while (i.hasNext()) {
                fi = i.next();
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
                if ((maxFileSizeBytes > 0) && (size > maxFileSizeBytes)) {
                    // file size is larger than maximum allowed file size, throw an error
                    throw new Exception();
                }
                byte[] content = fi.get();
                fi.delete();

                // single file upload
                String newResname = CmsResource.getName(fileName.replace('\\', '/'));
                // determine Title property value to set on new resource
                String title = newResname;
                if (title.lastIndexOf('.') != -1) {
                    title = title.substring(0, title.lastIndexOf('.'));
                }
                List<CmsProperty> properties = new ArrayList<CmsProperty>(1);
                CmsProperty titleProp = new CmsProperty();
                titleProp.setName(CmsPropertyDefinition.PROPERTY_TITLE);
                if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                    titleProp.setStructureValue(title);
                } else {
                    titleProp.setResourceValue(title);
                }
                properties.add(titleProp);

                // determine the resource type id from the given information
                int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();

                // calculate absolute path of uploaded resource
                newResname = getParamCurrentFolder() + newResname;

                if (!getCms().existsResource(newResname, CmsResourceFilter.IGNORE_EXPIRATION)) {
                    try {
                        // create the resource
                        getCms().createResource(newResname, resTypeId, content, properties);
                    } catch (CmsDbSqlException sqlExc) {
                        // SQL error, probably the file is too large for the database settings, delete file
                        getCms().lockResource(newResname);
                        getCms().deleteResource(newResname, CmsResource.DELETE_PRESERVE_SIBLINGS);
                        throw sqlExc;
                    }
                } else {
                    // resource exists, overwrite existing resource
                    checkLock(newResname);
                    CmsFile file = getCms().readFile(newResname, CmsResourceFilter.IGNORE_EXPIRATION);
                    byte[] contents = file.getContents();
                    try {
                        getCms().replaceResource(newResname, resTypeId, content, null);
                    } catch (CmsDbSqlException sqlExc) {
                        // SQL error, probably the file is too large for the database settings, restore content
                        file.setContents(contents);
                        getCms().writeFile(file);
                        throw sqlExc;
                    }
                }
            } else {
                // no upload file found
                throw new Exception();
            }
        } catch (Throwable e) {
            // something went wrong, change error code
            errorCode = ERROR_UPLOAD_INVALID;
        }

        // create JavaScript to return to file browser
        StringBuffer result = new StringBuffer(256);
        result.append("<html><head><script type=\"text/javascript\">\n");
        result.append("window.parent.frames[\"frmUpload\"].OnUploadCompleted(");
        result.append(errorCode);
        result.append(");\n");
        result.append("</script></head></html>");
        return result.toString();
    }

}