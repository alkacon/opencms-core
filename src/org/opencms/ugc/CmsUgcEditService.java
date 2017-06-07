/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ugc;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.ugc.shared.CmsUgcContent;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.ugc.shared.rpc.I_CmsUgcEditService;
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
public class CmsUgcEditService extends CmsGwtService implements I_CmsUgcEditService {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUgcEditService.class);

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
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#destroySession(org.opencms.util.CmsUUID)
     */
    public void destroySession(CmsUUID sessionId) throws CmsUgcException {

        try {
            CmsUgcSession formSession = getFormSession(sessionId);
            getRequest().getSession().removeAttribute("" + sessionId);
            if (formSession != null) {
                formSession.onSessionDestroyed();
            }

        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.gwt.CmsGwtService#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable t) throws CmsUgcException {

        logError(t);
        CmsUgcException exception = t instanceof CmsUgcException ? (CmsUgcException)t : new CmsUgcException(t);
        throw exception;
    }

    /**
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#getContent(org.opencms.util.CmsUUID)
     */
    public CmsUgcContent getContent(CmsUUID sessionId) throws CmsUgcException {

        CmsUgcContent formContent = null;
        try {
            CmsUgcSession session = CmsUgcSessionFactory.getInstance().getSession(getRequest(), sessionId);
            formContent = readContent(session, session.getResource());
        } catch (Exception e) {
            error(e);
        }

        return formContent;
    }

    /**
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#getLink(java.lang.String)
     */
    public String getLink(String path) {

        return OpenCms.getLinkManager().substituteLink(getCmsObject(), path);
    }

    /**
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#saveContent(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, String> saveContent(CmsUUID sessionId, Map<String, String> contentValues)
    throws CmsUgcException {

        Map<String, String> result = null;
        try {
            CmsUgcSession session = getFormSession(sessionId);
            if ((session != null) && sessionId.equals(session.getId())) {

                CmsXmlContentErrorHandler errorHandler = session.saveContent(contentValues);
                if (errorHandler.hasErrors()) {
                    result = errorHandler.getErrors(session.getMessageLocale());
                } else {
                    session.finish();
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
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#uploadFiles(org.opencms.util.CmsUUID, java.util.Set, java.lang.String)
     */
    public Map<String, String> uploadFiles(
        final CmsUUID sessionId,
        final Set<String> fieldNames,
        final String formDataId) throws CmsRpcException {

        try {
            final CmsUgcSession session = getFormSession(sessionId);
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
     * @see org.opencms.ugc.shared.rpc.I_CmsUgcEditService#validateContent(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, String> validateContent(CmsUUID sessionId, Map<String, String> contentValues)
    throws CmsUgcException {

        Map<String, String> result = Maps.newHashMap();
        try {
            CmsUgcSession session = getFormSession(sessionId);
            if ((session != null) && sessionId.equals(session.getId())) {
                CmsXmlContentErrorHandler errorHandler = session.validateContent(contentValues);
                result = errorHandler.getErrors(session.getMessageLocale());
            } else {
                // invalid session

            }
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * Handles all multipart requests.<p>
     *
     * @param request the request
     * @param response the response
     */
    protected void handleUpload(HttpServletRequest request, HttpServletResponse response) {

        String sessionIdStr = request.getParameter(CmsUgcConstants.PARAM_SESSION_ID);
        CmsUUID sessionId = new CmsUUID(sessionIdStr);
        CmsUgcSession session = CmsUgcSessionFactory.getInstance().getSession(request, sessionId);
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
    private CmsUgcSession getFormSession(CmsUUID sessionId) {

        return CmsUgcSessionFactory.getInstance().getSession(getRequest(), sessionId);
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
    private CmsUgcContent readContent(CmsUgcSession session, CmsResource resource) throws CmsException {

        CmsUgcContent formContent = new CmsUgcContent();
        Map<String, String> contentValues = session.getValues();
        formContent.setContentValues(contentValues);
        formContent.setSessionId(session.getId());
        formContent.setResourceType(OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        formContent.setSitePath(getCmsObject().getSitePath(resource));
        formContent.setStrucureId(resource.getStructureId());
        return formContent;
    }

}
