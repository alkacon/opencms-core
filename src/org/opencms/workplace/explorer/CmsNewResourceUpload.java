/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2011/03/23 14:52:20 $
 * Version: $Revision: 1.37 $
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

package org.opencms.workplace.explorer;

import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsImportFolder;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceException;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

/**
 * The new resource upload dialog handles the upload of single files or zipped files.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_upload.jsp
 * </ul>
 * <p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.37 $ 
 * 
 * @since 6.0.0 
 */
public class CmsNewResourceUpload extends CmsNewResource {

    /** The value for the resource upload applet action. */
    public static final int ACTION_APPLET = 140;

    /** The value for the resource upload applet action. */
    public static final int ACTION_APPLET_CHECK_OVERWRITE = 141;

    /** The value for the resource name form action. */
    public static final int ACTION_NEWFORM2 = 120;

    /** The value for the resource upload applet action: error occurred. */
    public static final int ACTION_SHOWERROR = 150;

    /** The value for the resource name form submission action. */
    public static final int ACTION_SUBMITFORM2 = 130;

    /** Constant for pre selection of the file filter for web documents / snippets in the upload applet. */
    // Warning: keep in sync with org.opencms.applet.upload.ImageFilter.FILTER_ID.
    public static final String APPLET_FILEFILTER_IMAGES = "imagefilter";

    /** Constant for pre selection of the file filter for office documents in the upload applet. */
    // Warning: keep in sync with org.opencms.applet.upload.OfficeFilter.FILTER_ID.
    public static final String APPLET_FILEFILTER_OFFICE = "officefilter";

    /** Constant for pre selection of the file filter for web documents / snippets in the upload applet. */
    // Warning: keep in sync with org.opencms.applet.upload.WebFilter.FILTER_ID.
    public static final String APPLET_FILEFILTER_WEB = "webfilter";

    /** Default setting for the applet JSP page colors (windows style). */
    public static final Map DEFAULT_APPLET_WINDOW_COLORS = new HashMap();

    /** The value for the resource upload applet action. */
    // Warning: This constant has to be kept in sync with the same named constant in org.opencms.applet.FileUploadApplet!    
    public static final String DIALOG_CHECK_OVERWRITE = "checkoverwrite";

    /** The name for the resource form submission action. */
    public static final String DIALOG_SHOWERROR = "showerror";

    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM2 = "submitform2";

    /** Request parameter name for the upload folder name. */
    public static final String PARAM_CLIENTFOLDER = "clientfolder";

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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResourceUpload.class);

    /** The configurable colors for the applet window (content frame JSP). */
    private Map m_appletWindowColors = DEFAULT_APPLET_WINDOW_COLORS;
    private String m_paramClientFolder;
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

    static {
        DEFAULT_APPLET_WINDOW_COLORS.put("bgColor", "#C0C0C0");
        DEFAULT_APPLET_WINDOW_COLORS.put("outerBorderRightBottom", "#333333");
        DEFAULT_APPLET_WINDOW_COLORS.put("outerBorderLeftTop", "#C0C0C0");
        DEFAULT_APPLET_WINDOW_COLORS.put("innerBorderRightBottom", "#777777");
        DEFAULT_APPLET_WINDOW_COLORS.put("innerBorderLeftTop", "#F0F0F0");
        DEFAULT_APPLET_WINDOW_COLORS.put("colorText", "#000000");
        DEFAULT_APPLET_WINDOW_COLORS.put("progessBar", "#E10050");
    }

    /**
     * Builds the html for the list of possible types for the uploaded file.<p>
     * 
     * This method can be used by all workplace dialog classes to build 
     * radio input buttons to select a resource type.<p>
     * 
     * @param dialog the dialog class instance which creates the type list
     * @param useTypeId if true, the resource type ID will be used for value attributes, otherwise the resource type names 
     * @return the list of possible files for the uploaded resource
     */
    public static String buildTypeList(CmsDialog dialog, boolean useTypeId) {

        StringBuffer result = new StringBuffer(512);
        try {
            // get current Cms object
            CmsObject cms = dialog.getCms();
            // determine resource type id of resource to change
            CmsResource res = cms.readResource(dialog.getParamResource(), CmsResourceFilter.ALL);
            int currentResTypeId = res.getTypeId();
            // get all available explorer type settings
            List resTypes = OpenCms.getWorkplaceManager().getExplorerTypeSettings();
            boolean isFolder = res.isFolder();
            // loop through all visible resource types
            for (int i = 0; i < resTypes.size(); i++) {
                boolean changeable = false;
                // get explorer type settings for current resource type
                CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)resTypes.get(i);

                // only if settings is a real resourcetype
                boolean isResourceType;
                try {
                    OpenCms.getResourceManager().getResourceType(settings.getName());
                    isResourceType = true;
                } catch (CmsLoaderException e) {
                    isResourceType = false;
                }

                if (isResourceType) {
                    int resTypeId = OpenCms.getResourceManager().getResourceType(settings.getName()).getTypeId();
                    // determine if this resTypeId is changeable by currentResTypeId

                    // changeable is true if current resource is a folder and this resource type also
                    if (isFolder && OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {
                        changeable = true;
                    } else if (!isFolder && !OpenCms.getResourceManager().getResourceType(resTypeId).isFolder()) {
                        // changeable is true if current resource is NOT a folder and this resource type also NOT                    
                        changeable = true;
                    }

                    if (changeable) {
                        // determine if this resource type is editable for the current user
                        CmsPermissionSet permissions = settings.getAccess().getPermissions(cms, res);
                        if (!permissions.requiresWritePermission() || !permissions.requiresControlPermission()) {
                            // skip resource types without required write or create permissions
                            continue;
                        }

                        // create table row with input radio button
                        result.append("<tr><td>");
                        result.append("<input type=\"radio\" name=\"");
                        result.append(PARAM_NEWRESOURCETYPE);
                        result.append("\" value=\"");
                        if (useTypeId) {
                            // use resource type id as value
                            result.append(resTypeId);
                        } else {
                            // use resource type name as value
                            result.append(settings.getName());
                        }
                        result.append("\"");
                        if (resTypeId == currentResTypeId) {
                            result.append(" checked=\"checked\"");
                        }
                        result.append("></td>");
                        result.append("\t<td><img src=\"");
                        result.append(getSkinUri());
                        result.append("filetypes/");
                        result.append(settings.getIcon());
                        result.append("\" border=\"0\" title=\"");
                        result.append(dialog.key(settings.getKey()));
                        result.append("\"></td>\n");
                        result.append("<td>");
                        result.append(dialog.key(settings.getKey()));
                        result.append("</td></tr>\n");
                    }
                }
            }
        } catch (CmsException e) {
            // error reading the VFS resource, log error
            LOG.error(org.opencms.workplace.commons.Messages.get().getBundle().key(
                org.opencms.workplace.commons.Messages.ERR_BUILDING_RESTYPE_LIST_1,
                dialog.getParamResource()));
        }
        return result.toString();
    }

    /**
     * Creates the HTML code of the file upload applet with all required parameters.<p>
     * 
     * @param jsp an initialized action element
     * @param locale the locale to use for the applet
     * @param currentFolder the folder to upload the resources to
     * @param redirectUrl the URL to redirect to after uploading
     * @param targetFrame the name of the target frame to redirect to after uploading
     * @param appletWindowColors the colors to use for the applet, if not provided, the default colors will be used
     * @return string containing the applet HTML code
     */
    public static String createAppletCode(
        CmsJspActionElement jsp,
        Locale locale,
        String currentFolder,
        String redirectUrl,
        String targetFrame,
        Map appletWindowColors) {

        StringBuffer applet = new StringBuffer(2048);

        // collect some required server data first
        String scheme = jsp.getRequest().getScheme();
        String host = jsp.getRequest().getServerName();
        String path = OpenCms.getStaticExportManager().getVfsPrefix();
        int port = jsp.getRequest().getServerPort();
        String webapp = scheme + "://" + host + ":" + port + OpenCms.getSystemInfo().getContextPath();

        // get all file extensions
        String fileExtensions = "";
        Map extensions = OpenCms.getResourceManager().getExtensionMapping();
        Iterator keys = extensions.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry entry = (Map.Entry)keys.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            fileExtensions += key + "=" + value + ",";
        }
        fileExtensions = fileExtensions.substring(0, fileExtensions.length() - 1);

        // get the file size upload limitation value (value is in bytes for the applet)
        long maxFileSize = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(jsp.getCmsObject());

        // get the current session id
        HttpSession session = jsp.getRequest().getSession(false);
        // we assume we always have a session here, otherwise an unhandled NPE will occur
        String sessionId = ((CmsUUID)session.getAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID)).getStringValue();

        // define the required colors.
        // these are configurable via set
        StringBuffer colors = new StringBuffer();
        if ((appletWindowColors == null) || (appletWindowColors.size() == 0)) {
            appletWindowColors = DEFAULT_APPLET_WINDOW_COLORS;
        }
        Iterator it = appletWindowColors.entrySet().iterator();
        Map.Entry color;
        while (it.hasNext()) {
            color = (Map.Entry)it.next();
            colors.append(color.getKey()).append('=').append(color.getValue());
            if (it.hasNext()) {
                colors.append(',');
            }
        }

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
        if (CmsStringUtil.isEmpty(redirectUrl)) {
            applet.append(CmsWorkplace.FILE_EXPLORER_FILELIST);
        } else {
            applet.append(redirectUrl);
        }
        // append some parameters to prevent caching of URL by Applet
        applet.append("?time=").append(System.currentTimeMillis());
        applet.append("\">\n");
        applet.append("<param name=\"targetframe\" value=\"");
        applet.append(targetFrame);
        applet.append("\">\n");
        applet.append("<param name=\"error\" value=\"");
        applet.append(scheme);
        applet.append("://");
        applet.append(host);
        applet.append(":");
        applet.append(port);
        applet.append(path);
        applet.append("/system/workplace/views/explorer/explorer_files.jsp\">\n");
        applet.append("<param name=\"sessionId\" value=\"");
        applet.append(sessionId);
        applet.append("\">\n");
        applet.append("<param name=\"filelist\" value=\"");
        applet.append(currentFolder);
        applet.append("\">\n");
        applet.append("<param name=\"filefilterselection\" value=\"");
        applet.append(getAppletFileFilterPreselectionConstant(jsp.getCmsObject(), currentFolder));
        applet.append("\">\n");
        applet.append("<param name=\"colors\" value=\"");
        applet.append(colors.toString());
        applet.append("\">\n");
        applet.append("<param name=\"fileExtensions\" value=\"");
        applet.append(fileExtensions);
        applet.append("\">\n\n");
        applet.append("<param name=\"maxsize\" value=\"");
        applet.append(maxFileSize);
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputSelect\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ACTION_SELECT_0));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputCount\"value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ACTION_COUNT_0));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputCreate\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ACTION_CREATE_0));
        applet.append("\">\n");
        applet.append("<param name=\"actionOutputUpload\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ACTION_UPLOAD_0));
        applet.append("\">\n");
        applet.append("<param name=\"actionOverwriteCheck\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ACTION_OVERWRITECHECK_0));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputUpload\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_MESSAGE_UPLOAD_0));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputErrorZip\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_MESSAGE_ERROR_ZIP_0));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputErrorSize\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_MESSAGE_ERROR_SIZE_0));
        applet.append("\">\n");
        applet.append("<param name=\"messageNoPreview\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_MESSAGE_NOPREVIEW_0));
        applet.append("\">\n");
        applet.append("<param name=\"messageOutputAdding\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_MESSAGE_ADDING_0));
        applet.append(" \">\n");
        applet.append("<param name=\"errorTitle\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ERROR_TITLE_0));
        applet.append(" \">\n");
        applet.append("<param name=\"errorLine1\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ERROR_LINE1_0));
        applet.append(" \">\n");
        applet.append("<param name=\"certificateErrorTitle\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ERROR_CERT_TITLE_0));
        applet.append(" \">\n");
        applet.append("<param name=\"overwriteDialogTitle\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_OVERWRITE_DIALOG_TITLE_0));
        applet.append(" \">\n");
        applet.append("<param name=\"overwriteDialogIntro\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_OVERWRITE_DIALOG_INTRO_0));
        applet.append(" \">\n");
        applet.append("<param name=\"overwriteDialogOk\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_OVERWRITE_DIALOG_OK_0));
        applet.append(" \">\n");
        applet.append("<param name=\"overwriteDialogCancel\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_OVERWRITE_DIALOG_CANCEL_0));
        applet.append(" \">\n");
        applet.append("<param name=\"overwriteDialogLocale\" value=\"");
        applet.append(locale.toString());
        applet.append(" \">\n");
        applet.append("<param name=\"certificateErrorMessage\" value=\"");
        applet.append(Messages.get().getBundle(locale).key(Messages.GUI_UPLOADAPPLET_ERROR_CERT_MESSAGE_0));
        applet.append(" \">\n");
        applet.append("<param name=\"clientFolder\" value=\"");
        applet.append(new CmsUserSettings(jsp.getCmsObject()).getUploadAppletClientFolder());
        applet.append(" \">\n");
        applet.append("</applet>\n");

        return applet.toString();

    }

    /**
     * Returns the proper constant for preselection of a file filter of the upload applet depending on the current 
     * folder to upload to. <p>
     * 
     * @param cms the current users context
     * @param currentFolder the folder to upload to 
     * @return one of <code>{@link #APPLET_FILEFILTER_IMAGES}</code>, <code>{@link #APPLET_FILEFILTER_OFFICE}</code>, 
     *      <code>{@link #APPLET_FILEFILTER_WEB}</code>
     */
    private static String getAppletFileFilterPreselectionConstant(CmsObject cms, String currentFolder) {

        String result = "";
        try {
            CmsResource res = cms.readResource(currentFolder, CmsResourceFilter.IGNORE_EXPIRATION);
            result = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
            if ("imagegallery".equals(result)) {
                result = APPLET_FILEFILTER_IMAGES;
            } else if ("htmlgallery".equals(result)) {
                result = APPLET_FILEFILTER_WEB;
            } else if ("downloadgallery".equals(result)) {
                result = APPLET_FILEFILTER_OFFICE;
            }
        } catch (CmsException e) {
            System.err.println(e);
            // ignore this, gallery type will simply not be supported for pre selection of the file type selector in the upload applet 
        }
        return result;
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
    @Override
    public void actionCloseDialog() throws JspException {

        if (getAction() == ACTION_CANCEL) {
            try {
                CmsResource res = getCms().readResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                if (res.getState().isNew()) {
                    // only delete new resource
                    getCms().deleteResource(getParamResource(), CmsResource.DELETE_PRESERVE_SIBLINGS);
                }
                if (res.getState().isChanged()) {
                    // resource is changed, restore content of resource from online project
                    CmsProject currentProject = getCms().getRequestContext().currentProject();
                    byte[] onlineContents = null;
                    try {
                        // switch to online project and get online file contents
                        getCms().getRequestContext().setCurrentProject(
                            getCms().readProject(CmsProject.ONLINE_PROJECT_ID));
                        CmsFile onlineFile = getCms().readFile(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                        onlineContents = onlineFile.getContents();

                    } finally {
                        // switch back to current project
                        getCms().getRequestContext().setCurrentProject(currentProject);
                    }
                    if (onlineContents != null) {
                        // write online contents back to offline file
                        CmsFile modFile = getCms().readFile(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                        modFile.setContents(onlineContents);
                        getCms().writeFile(modFile);
                    }
                }
            } catch (RuntimeException e) {
                // assume file was not present
            } catch (Exception e) {
                // assume file was not present
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
            int newType = oldType.getTypeId();
            if (!oldType.getTypeName().equals(getParamNewResourceType())) {

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamNewResourceType())) {
                    // automatic resource type selection
                    newType = OpenCms.getResourceManager().getDefaultTypeForName(res.getName()).getTypeId();
                } else {
                    // change the type of the uploaded resource
                    newType = OpenCms.getResourceManager().getResourceType(getParamNewResourceType()).getTypeId();
                }
                getCms().chtype(getParamResource(), newType);
            }
            if ((getParamNewResourceName() != null) && !getParamResource().endsWith(getParamNewResourceName())) {
                String newResourceName = CmsResource.getFolderPath(getParamResource()) + getParamNewResourceName();
                // rename the resource
                getCms().renameResource(getParamResource(), newResourceName);
                setParamResource(newResourceName);
            }
        } catch (Throwable e) {
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
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamClientFolder())) {
            CmsUserSettings userSettings = new CmsUserSettings(getCms());
            userSettings.setUploadAppletClientFolder(getParamClientFolder());
            try {
                userSettings.save(getCms());
            } catch (CmsException e) {
                // it's not fatal if the client folder for the applet file chooser is not possible 
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle(getLocale()).key(
                        Messages.ERR_UPLOAD_STORE_CLIENT_FOLDER_1,
                        new Object[] {getCms().getRequestContext().currentUser().getName()}), e);
                }
            }
        }

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
                if ((maxFileSizeBytes > 0) && (size > maxFileSizeBytes)) {
                    // file size is larger than maximum allowed file size, throw an error
                    throw new CmsWorkplaceException(Messages.get().container(
                        Messages.ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1,
                        new Long(maxFileSizeBytes / 1024)));
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
                    String newResname = CmsResource.getName(fileName.replace('\\', '/'));
                    // determine Title property value to set on new resource
                    String title = newResname;
                    if (title.lastIndexOf('.') != -1) {
                        title = title.substring(0, title.lastIndexOf('.'));
                    }
                    List properties = new ArrayList(1);
                    CmsProperty titleProp = new CmsProperty();
                    titleProp.setName(CmsPropertyDefinition.PROPERTY_TITLE);
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        titleProp.setStructureValue(title);
                    } else {
                        titleProp.setResourceValue(title);
                    }
                    properties.add(titleProp);
                    newResname = getCms().getRequestContext().getFileTranslator().translateResource(newResname);
                    setParamNewResourceName(newResname);
                    setParamResource(newResname);
                    setParamResource(computeFullResourceName());
                    // determine the resource type id from the given information
                    int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();
                    if (!getCms().existsResource(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION)) {
                        try {
                            // create the resource
                            getCms().createResource(getParamResource(), resTypeId, content, properties);
                        } catch (CmsSecurityException e) {
                            // in case of not enough permissions, try to create a plain text file
                            getCms().createResource(
                                getParamResource(),
                                CmsResourceTypePlain.getStaticTypeId(),
                                content,
                                properties);
                        } catch (CmsDbSqlException sqlExc) {
                            // SQL error, probably the file is too large for the database settings, delete file
                            getCms().lockResource(getParamResource());
                            getCms().deleteResource(getParamResource(), CmsResource.DELETE_PRESERVE_SIBLINGS);
                            throw sqlExc;
                        }
                    } else {
                        checkLock(getParamResource());
                        CmsFile file = getCms().readFile(getParamResource(), CmsResourceFilter.IGNORE_EXPIRATION);
                        byte[] contents = file.getContents();
                        try {
                            getCms().replaceResource(getParamResource(), resTypeId, content, null);
                        } catch (CmsSecurityException e) {
                            // in case of not enough permissions, try to create a plain text file
                            getCms().replaceResource(
                                getParamResource(),
                                CmsResourceTypePlain.getStaticTypeId(),
                                content,
                                null);
                        } catch (CmsDbSqlException sqlExc) {
                            // SQL error, probably the file is too large for the database settings, restore content
                            file.setContents(contents);
                            getCms().writeFile(file);
                            throw sqlExc;
                        }
                    }
                }
            } else {
                throw new CmsWorkplaceException(Messages.get().container(Messages.ERR_UPLOAD_FILE_NOT_FOUND_0));
            }
        } catch (Throwable e) {
            // error uploading file, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_UPLOAD_FILE_0));
            setAction(ACTION_SHOWERROR);
            includeErrorpage(this, e);
        }
    }

    /**
     * Builds the list of possible types for the uploaded file.<p>
     * 
     * @return the list of possible files for the uploaded resource
     */
    public String buildTypeList() {

        return buildTypeList(this, false);
    }

    /**
     * Creates the HTML code of the file upload applet with all required parameters.<p>
     * 
     * @return string containing the applet HTML code
     */
    public String createAppletCode() {

        return createAppletCode(
            getJsp(),
            getLocale(),
            getParamCurrentFolder(),
            getParamRedirectUrl(),
            getParamTargetFrame(),
            m_appletWindowColors);
    }

    /**
     * Returns the paramClientFolder.<p>
     *
     * @return the paramClientFolder
     */
    public String getParamClientFolder() {

        return m_paramClientFolder;
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
            return "explorer_files";
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
     * Replies on the request of the upload applet for checking potential overwrites of VFS resources 
     * with the line based resources that do exist on the host. <p> 
     * 
     * @param request the request sent by the applet 
     * 
     * @return the line based resources that do exist on the host
     */
    public String handleUploadOverwriteCheckRequest(HttpServletRequest request) {

        StringBuffer result = new StringBuffer();
        String uploadFiles = CmsEncoder.decode(request.getHeader("uploadFiles"));
        String currentFolder = CmsEncoder.decode(request.getHeader("uploadFolder"));
        if (currentFolder.endsWith("/")) {
            currentFolder = currentFolder.substring(0, currentFolder.length() - 1);
        }
        List vfsfiles = CmsStringUtil.splitAsList(uploadFiles, '\n');
        Iterator it = vfsfiles.iterator();
        // apply directory translation only for server comparison
        String vfsfile;
        // return the clean file as know by the client
        String clientfile;

        while (it.hasNext()) {
            clientfile = (String)it.next();
            vfsfile = new StringBuffer(currentFolder).append(clientfile).toString();
            vfsfile = OpenCms.getResourceManager().getFileTranslator().translateResource(vfsfile);
            if (getCms().existsResource(vfsfile)) {
                result.append(CmsEncoder.encode(clientfile, CmsEncoder.ENCODING_UTF_8));
                result.append('\n');
            }
        }
        return result.toString();

    }

    /**
     * Sets the configurable colors for the applet window (content frame JSP).<p>
     *
     * @param appletWindowColors the configurable colors for the applet window (content frame JSP).
     */
    public final void setAppletWindowColors(final Map appletWindowColors) {

        m_appletWindowColors = appletWindowColors;
    }

    /**
     * Sets the client upload folder name.<p>
     * 
     * @param clientFolder the client upload folder name
     */
    public void setParamClientFolder(String clientFolder) {

        m_paramClientFolder = clientFolder;
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        String siteRoot = jsp.getRequestContext().getSiteRoot();
        // In case of the upload applet the site stored in the user preferences must NOT be made the current 
        // site even if we have a new session! Since the upload applet will create a new session for the upload itself, 
        // we must make sure to use the site of the request, NOT the site stored in the user preferences.
        // The default logic will erase the request site in case of a new session.
        // With this workaround the site from the request is made the current site as required.
        super.initWorkplaceMembers(jsp);
        if (!siteRoot.equals(getSettings().getSite())) {
            getSettings().setSite(siteRoot);
            jsp.getRequestContext().setSiteRoot(siteRoot);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
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
        } else if (DIALOG_CHECK_OVERWRITE.equals(getParamAction())) {
            setAction(ACTION_APPLET_CHECK_OVERWRITE);
        } else {
            if (getSettings().getUserSettings().useUploadApplet()) {
                setAction(ACTION_APPLET);
            } else {
                setAction(ACTION_DEFAULT);
            }
            // build title for new resource dialog     
            setParamTitle(key(Messages.GUI_NEWRESOURCE_UPLOAD_0));
        }
    }
}