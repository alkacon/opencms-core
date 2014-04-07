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

import org.opencms.editors.usergenerated.shared.CmsFormContent;
import org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentErrorHandler;

import java.util.Collections;
import java.util.Map;

/**
 * The form editing service.<p>
 */
public class CmsFormEditService extends CmsGwtService implements I_CmsFormEditService {

    /** The serial version id. */
    private static final long serialVersionUID = 5479252081304867604L;

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
            CmsFormSession session = CmsFormSessionFactory.getInstance().getSession(getRequest());
            if ((session != null) && sessionId.equals(session.getProject().getUuid())) {

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
        formContent.setSessionId(session.getProject().getUuid());
        formContent.setResourceType(OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        formContent.setSitePath(getCmsObject().getSitePath(resource));
        formContent.setStrucureId(resource.getStructureId());
        return formContent;
    }

}
