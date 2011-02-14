/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/upload/Attic/CmsUploadBean.java,v $
 * Date   : $Date: 2011/02/14 11:46:56 $
 * Version: $Revision: 1.2 $
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

package org.opencms.gwt.upload;

import org.opencms.db.CmsDbSqlException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.gwt.shared.CmsUploadFileBean.I_CmsUploadConstants;
import org.opencms.gwt.shared.CmsUploadProgessInfo;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspBean;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.Messages;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;

/**
 * Bean to be used in JSP scriptlet code that provides 
 * access to the upload functionality.<p>
 * 
 * @author  Ruediger Kurz 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0 
 */
public class CmsUploadBean extends CmsJspBean {

    /** The default delay for slow uploads. */
    public static final int DEFAULT_SLOW_DELAY_MILLIS = 50;

    /** The default upload timeout. */
    public static final int DEFAULT_UPLOAD_TIMEOUT = 20000;

    /** Key name for the session attribute that stores the id of the current listener. */
    public static final String SESSION_ATTRIBUTE_LISTENER_ID = "__CmsUploadBean.LISTENER";

    /** Upload JSP URI. */
    public static final String UPLOAD_JSP_URI = "/system/modules/org.opencms.gwt/upload.jsp";

    /** Key name for the request attribute to indicate a multipart request was already parsed. */
    protected static final String REQUEST_ATTRIBUTE_MULTIPART = "__CmsUploadBean.MULTIPART";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUploadBean.class);

    /** A static map of all listeners. */
    private static Map<CmsUUID, CmsUploadListener> m_listeners = new HashMap<CmsUUID, CmsUploadListener>();

    /** A list of the file items to upload. */
    private List<FileItem> m_multiPartFileItems;

    /** The map of parameters read from the current request. */
    private Map<String, String[]> m_parameterMap;

    /**
     * Constructor, with parameters.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsUploadBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the listener for given CmsUUID.<p>
     * 
     * @param listenerId the uuid
     * 
     * @return the according listener
     */
    public static CmsUploadListener getCurrentListener(CmsUUID listenerId) {

        return m_listeners.get(listenerId);
    }

    /**
     * Starts the upload.<p>
     */
    public void start() {

        CmsUploadListener listener = new CmsUploadListener(getRequest().getContentLength());
        addListener(listener);

        boolean errorOccurred = true;

        try {
            // parse the request: stores the file items and the parameters of the request locally
            parseRequest(listener);
            writeResponse(generateResponse(true, null, listener.getInfo()));
            errorOccurred = false;
        } catch (CmsUploadException e) {
            writeResponse(generateResponse(false, e, listener.getInfo()));
            LOG.debug(e.getMessage(), e);
        } finally {
            removeListener(listener.getId());
        }

        try {
            if (m_multiPartFileItems != null) {

                String targetFolder = getTargetFolder();

                // iterate over the list of files to upload and create each single resource
                for (FileItem fi : m_multiPartFileItems) {
                    if ((fi != null) && (!fi.isFormField())) {
                        // found the file object
                        String newResname = CmsResource.getName(fi.getName().replace('\\', '/'));
                        newResname = getNewResourceName(newResname, targetFolder);
                        byte[] content = fi.get();
                        fi.delete();
                        createSingleResource(newResname, content);
                    }
                }
            }
        } catch (CmsException e) {
            if (!errorOccurred) {
                CmsUploadException ex = new CmsUploadException("Error while creating resources on the VFS of OpenCms");
                writeResponse(generateResponse(false, ex, null));
            }
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Puts a upload listener into the static map.<p>
     * 
     * @param listener the listener to put in the map
     */
    private void addListener(CmsUploadListener listener) {

        m_listeners.put(listener.getId(), listener);
        getRequest().getSession().setAttribute(SESSION_ATTRIBUTE_LISTENER_ID, listener.getId());
    }

    /**
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     * 
     * @param resource the resource name which is checked
     * 
     * @throws CmsException if reading or locking the resource fails
     */
    private void checkLock(String resource) throws CmsException {

        checkLock(resource, CmsLockType.EXCLUSIVE);
    }

    /**
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     * 
     * @param resource the resource name which is checked
     * @param type indicates the mode {@link CmsLockType#EXCLUSIVE} or {@link CmsLockType#TEMPORARY}
     * 
     * @throws CmsException if reading or locking the resource fails
     */
    private void checkLock(String resource, CmsLockType type) throws CmsException {

        CmsResource res = getCmsObject().readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = getCmsObject().getLock(res);
        boolean lockable = lock.isLockableBy(getCmsObject().getRequestContext().getCurrentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                // resource is lockable, so lock it automatically
                if (type == CmsLockType.TEMPORARY) {
                    getCmsObject().lockResourceTemporary(resource);
                } else {
                    getCmsObject().lockResource(resource);
                }
            } else {
                throw new CmsException(Messages.get().container(Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        } else {
            if (!lockable) {
                throw new CmsException(Messages.get().container(Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        }
    }

    /**
     * Creates a single resource.<p>
     * 
     * @param newResname the name of the resource to create
     * @param content the content of the resource to create
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsLoaderException if something goes wrong
     * @throws CmsDbSqlException if something goes wrong
     */
    private void createSingleResource(String newResname, byte[] content)
    throws CmsException, CmsLoaderException, CmsDbSqlException {

        int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();
        int plainId = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()).getTypeId();

        // determine Title property value to set on new resource
        String title = CmsResource.getName(newResname);
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

        if (!getCmsObject().existsResource(newResname, CmsResourceFilter.IGNORE_EXPIRATION)) {
            // if the resource does not exist, create it
            try {
                // create the resource
                getCmsObject().createResource(newResname, resTypeId, content, properties);
            } catch (CmsSecurityException e) {
                // in case of not enough permissions, try to create a plain text file
                getCmsObject().createResource(newResname, plainId, content, properties);
            } catch (CmsDbSqlException sqlExc) {
                // SQL error, probably the file is too large for the database settings, delete file
                getCmsObject().lockResource(newResname);
                getCmsObject().deleteResource(newResname, CmsResource.DELETE_PRESERVE_SIBLINGS);
                throw sqlExc;
            }
        } else {
            // if the resource already exists, replace it
            checkLock(newResname);
            CmsFile file = getCmsObject().readFile(newResname, CmsResourceFilter.IGNORE_EXPIRATION);
            byte[] contents = file.getContents();
            try {
                getCmsObject().replaceResource(newResname, resTypeId, content, null);
            } catch (CmsSecurityException e) {
                // in case of not enough permissions, try to create a plain text file
                getCmsObject().replaceResource(newResname, plainId, content, null);
            } catch (CmsDbSqlException sqlExc) {
                // SQL error, probably the file is too large for the database settings, restore content
                file.setContents(contents);
                getCmsObject().writeFile(file);
                throw sqlExc;
            }
        }
    }

    private JSONObject generateResponse(boolean success, Exception ex, CmsUploadProgessInfo info) {

        JSONObject result = new JSONObject();

        try {
            if (success) {
                result.put(I_CmsUploadConstants.KEY_SUCCESS, success);
                result.put(I_CmsUploadConstants.KEY_MESSAGE, "Upload was scuccessful.");
                result.put(I_CmsUploadConstants.KEY_STACKTRACE, "");
            } else {
                result.put(I_CmsUploadConstants.KEY_SUCCESS, success);
                result.put(I_CmsUploadConstants.KEY_MESSAGE, ex.getMessage());
                result.put(I_CmsUploadConstants.KEY_STACKTRACE, CmsException.getStackTraceAsString(ex));
            }

            if (info == null) {
                info = new CmsUploadProgessInfo(0, 0, false, 0, 0);
            }
            result.put(I_CmsUploadConstants.KEY_CURRENT_FILE, info.getCurrentFile());
            result.put(I_CmsUploadConstants.KEY_BYTES_READ, info.getBytesRead());
            result.put(I_CmsUploadConstants.KEY_CONTENT_LENGTH, info.getContentLength());
            result.put(I_CmsUploadConstants.KEY_PERCENT, info.getPercent());
            result.put(I_CmsUploadConstants.KEY_RUNNING, info.isRunning());

        } catch (JSONException e) {
            LOG.error("Faild to fill the JSON object", e);
        }
        return result;
    }

    /**
     * Returns the VFS path of a resource for the given filename and the given folder.<p>
     * 
     * @param fileName the name of the file
     * @param folder the folder for the new resoruce
     * 
     * @return the VFS path of a resource for the given filename and the given folder
     */
    private String getNewResourceName(String fileName, String folder) {

        return folder + getCmsObject().getRequestContext().getFileTranslator().translateResource(fileName);
    }

    /**
     * Returns the target folder for the new resource, 
     * if the given folder does not exist root folder
     * of the current site is returned.<p>
     * 
     * @return the target folder for the new resource
     * 
     * @throws CmsException if something goes wrong
     */
    private String getTargetFolder() throws CmsException {

        // get the target folder on the vfs
        CmsResource target = getCmsObject().readResource("/", CmsResourceFilter.IGNORE_EXPIRATION);
        if (m_parameterMap.get("upload_target_folder") != null) {
            String targetFolder = m_parameterMap.get("upload_target_folder")[0];
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(targetFolder)) {
                if (getCmsObject().existsResource(targetFolder)) {
                    CmsResource tmpTarget = getCmsObject().readResource(
                        targetFolder,
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (tmpTarget.isFolder()) {
                        target = tmpTarget;
                    }
                }
            }
        }
        String targetFolder = getCmsObject().getRequestContext().removeSiteRoot(target.getRootPath());
        if (!targetFolder.endsWith("/")) {
            // add folder separator to currentFolder
            targetFolder += "/";
        }
        return targetFolder;
    }

    /**
     * Parses the request.<p>
     * 
     * Stores the file items and the request parameters in a local variable if present.<p>
     */
    @SuppressWarnings("unchecked")
    private void parseRequest(CmsUploadListener listener) {

        m_parameterMap = null;

        // ensure a multipart request is parsed only once (for "forward" scenarios with reports)
        if (null == getRequest().getAttribute(REQUEST_ATTRIBUTE_MULTIPART)) {
            // read the files
            m_multiPartFileItems = readMultipartFileItems(getRequest(), listener);
            if (m_multiPartFileItems != null) {
                // this was indeed a multipart form request
                m_parameterMap = CmsRequestUtil.readParameterMapFromMultiPart(
                    getCmsObject().getRequestContext().getEncoding(),
                    m_multiPartFileItems);
                getRequest().setAttribute(REQUEST_ATTRIBUTE_MULTIPART, Boolean.TRUE);
            }
        }
        if (m_parameterMap == null) {
            // the request was a "normal" request
            m_parameterMap = getRequest().getParameterMap();
        }
    }

    /**
     * Parses a request of the form <code>multipart/form-data</code>.
     * 
     * The result list will contain items of type <code>{@link FileItem}</code>.
     * If the request is not of type <code>multipart/form-data</code>, then <code>null</code> is returned.<p>
     * 
     * @param request the HTTP servlet request to parse
     * @param listener the upload listener
     * 
     * @return the list of <code>{@link FileItem}</code> extracted from the multipart request,
     *      or <code>null</code> if the request was not of type <code>multipart/form-data</code>
     */
    private List<FileItem> readMultipartFileItems(HttpServletRequest request, CmsUploadListener listener) {

        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        factory.setRepository(new File(OpenCms.getSystemInfo().getPackagesRfsPath()));
        ServletFileUpload fu = new ServletFileUpload(factory);
        // set encoding to correctly handle special chars (e.g. in filenames)
        fu.setHeaderEncoding(request.getCharacterEncoding());
        // fu.setFileSizeMax(100);
        fu.setProgressListener(listener);

        List<FileItem> result = new ArrayList<FileItem>();
        try {
            List<FileItem> items = CmsCollectionsGenericWrapper.list(fu.parseRequest(request));
            if (items != null) {
                result = items;
            }
        } catch (SizeLimitExceededException e) {
            int actualSize = (int)(e.getActualSize() / 1024);
            int maxSize = (int)(e.getPermittedSize() / 1024);
            CmsUploadException ex = new CmsUploadException("Size limit reached - actual size: "
                + actualSize
                + " maximum size: "
                + maxSize);
            listener.setException(ex);
            throw ex;
        } catch (CmsUploadException e) {
            listener.setException(e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            CmsUploadException ex = new CmsUploadException("Unexpected upload exception occurred");
            listener.setException(ex);
            throw ex;
        }
        return result;
    }

    /**
     * Remove the listener active in this session.
     * 
     * @param listenerId the id of the listener to remove
     */
    private void removeListener(CmsUUID listenerId) {

        getRequest().getSession().removeAttribute(SESSION_ATTRIBUTE_LISTENER_ID);
        m_listeners.remove(listenerId);
    }

    /**
     * Writes the response.<p>
     * 
     * @param responseObject the response content
     */
    private void writeResponse(JSONObject responseObject) {

        try {
            PrintWriter writer = getResponse().getWriter();
            writer.print(responseObject.toString());
        } catch (IOException e) {
            LOG.debug(e);
        }
    }

}
