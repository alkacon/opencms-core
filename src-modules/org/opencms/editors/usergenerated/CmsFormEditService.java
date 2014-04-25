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

package org.opencms.editors.usergenerated;

import org.opencms.editors.usergenerated.shared.CmsFormConstants;
import org.opencms.editors.usergenerated.shared.CmsFormContent;
import org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentErrorHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * The form editing service.<p>
 */
public class CmsFormEditService extends CmsGwtService implements I_CmsFormEditService {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFormEditService.class);

    /** The serial version id. */
    private static final long serialVersionUID = 5479252081304867604L;

    /**
     * @see org.opencms.gwt.CmsGwtService#checkPermissions(org.opencms.file.CmsObject)
     */
    @Override
    public void checkPermissions(CmsObject cms) {

        // don't do permission checks, since this service should be available to "Guest" users from the frontend 
    }

    /**
     * @see org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService#getExistingContent(java.lang.String, java.lang.String)
     */
    public CmsFormContent getExistingContent(String configPath, String sitepath) throws CmsRpcException {

        CmsFormContent formContent = null;
        try {
            CmsFormSession session = CmsFormSessionFactory.getInstance().createSession(
                getCmsObject(),
                getRequest(),
                configPath);
            formContent = readContent(session, session.loadXmlContent(sitepath));
        } catch (Exception e) {
            error(e);
        }

        return formContent;
    }

    /**
     * @see org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService#getNewContent(java.lang.String)
     */
    public CmsFormContent getNewContent(String configPath) throws CmsRpcException {

        CmsFormContent formContent = null;
        try {
            CmsFormSession session = CmsFormSessionFactory.getInstance().createSession(
                getCmsObject(),
                getRequest(),
                configPath);
            formContent = readContent(session, session.createXmlContent());

        } catch (Exception e) {
            error(e);
        }

        return formContent;
    }

    /**
     * @see org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService#saveContent(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, String> saveContent(CmsUUID sessionId, Map<String, String> contentValues) throws CmsRpcException {

        Map<String, String> result = null;
        try {
            CmsFormSession session = getFormSession(sessionId);
            if ((session != null) && sessionId.equals(session.getId())) {

                CmsXmlContentErrorHandler errorHandler = session.saveContent(contentValues);
                if (errorHandler.hasErrors()) {
                    result = errorHandler.getErrors(getCmsObject().getRequestContext().getLocale());
                } else {
                    result = Collections.emptyMap();
                }

            } else {
                // invalid session 

            }
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService#uploadFiles(org.opencms.util.CmsUUID, java.util.Set, java.lang.String)
     */
    public Map<String, String> uploadFiles(
        final CmsUUID sessionId,
        final Set<String> fieldNames,
        final String formDataId) throws CmsRpcException {

        try {
            final CmsFormSession session = getFormSession(sessionId);
            final Map<String, String> result = Maps.newHashMap();

            session.getFormUploadHelper().consumeFormData(formDataId, new I_CmsFormDataHandler() {

                @SuppressWarnings("synthetic-access")
                public void handleFormData(Map<String, I_CmsFormDataItem> items) throws Exception {

                    for (String fieldName : fieldNames) {
                        I_CmsFormDataItem item = items.get(fieldName);
                        if (item != null) {
                            CmsResource createdResource = session.createUploadResource(
                                item.getFieldName(),
                                item.getFileName(),
                                item.getData());
                            String sitePath = session.getCmsObject().getSitePath(createdResource);
                            result.put(fieldName, sitePath);
                        } else {
                            LOG.debug(formDataId + ": requested upload for field " + fieldName + " which was empty.");
                        }
                    }
                }
            });
            return result;
        } catch (Exception e) {
            error(e);
            return null; // will never be reached 

        }
    }

    /**
     * Handles all multipart requests.<p>
     * 
     * @param request the request
     * @param response the response
     */
    protected void handleUpload(HttpServletRequest request, HttpServletResponse response) {

        String sessionIdStr = request.getParameter(CmsFormConstants.PARAM_SESSION_ID);
        CmsUUID sessionId = new CmsUUID(sessionIdStr);
        CmsFormSession session = CmsFormSessionFactory.getInstance().getSession(request, sessionId);
        session.getFormUploadHelper().processFormSubmitRequest(request);
    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        boolean isMultiPart = FileUploadBase.isMultipartContent(new ServletRequestContext(request));

        if (isMultiPart) {
            try {
                handleUpload(request, response);
            } finally {
                clearThreadStorage();
            }
        } else {
            super.service(request, response);
        }
    }

    /**
     * Gets the form session  with the given id.<p>
     * 
     * @param sessionId the session id 
     * 
     * @return the form session 
     */
    private CmsFormSession getFormSession(CmsUUID sessionId) {

        return CmsFormSessionFactory.getInstance().getSession(getRequest(), sessionId);
    }

    /**
     * Reads the form content information.<p>
     * 
     * @param session the editing session
     * @param resource the edited resource
     * 
     * @return the form content
     * 
     * @throws CmsException if reading the info fails
     */
    private CmsFormContent readContent(CmsFormSession session, CmsResource resource) throws CmsException {

        CmsFormContent formContent = new CmsFormContent();
        Map<String, String> contentValues = session.getValues();
        formContent.setContentValues(contentValues);
        formContent.setSessionId(session.getId());
        formContent.setResourceType(OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        formContent.setSitePath(getCmsObject().getSitePath(resource));
        formContent.setStrucureId(resource.getStructureId());
        return formContent;
    }
}
