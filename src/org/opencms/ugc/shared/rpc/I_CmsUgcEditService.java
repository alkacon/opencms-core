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

package org.opencms.ugc.shared.rpc;

import org.opencms.gwt.CmsRpcException;
import org.opencms.ugc.shared.CmsUgcContent;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.util.CmsUUID;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The form edit service interface.<p>
 */
public interface I_CmsUgcEditService extends RemoteService {

    /**
     * Destroys the given session.<p>
     *
     * @param sessionId the id of the session to destroy
     *
     * @throws CmsRpcException if something goes wrong
     */
    void destroySession(CmsUUID sessionId) throws CmsRpcException;

    /**
     * Loads the form content for an existing session.<p>
     *
     * @param sessionId the id of the existing session
     *
     * @return the form content information
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsUgcContent getContent(CmsUUID sessionId) throws CmsRpcException;

    /**
     * Gets the link corresponding to a given site path.<p>
     *
     * @param path the site path
     *
     * @return the link for the path
     */
    String getLink(String path);

    /**
     * Saves the given content values to the edited content.<p>
     *
     * @param sessionId the session id
     * @param contentValues the content values
     *
     * @return the validation errors, if there are any
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<String, String> saveContent(CmsUUID sessionId, Map<String, String> contentValues) throws CmsRpcException;

    /**
     * Uploads multiple files.<p>
     *
     * @param sessionId the session id
     * @param fieldNames the set of field names of the form fields containing the uploaded files
     * @param formDataId the form data id of the form submit in which the form fields were posted
     *
     * @return the map from field names to the VFS paths of uploaded files
     * @throws CmsRpcException if something goes wrong
     */
    Map<String, String> uploadFiles(CmsUUID sessionId, Set<String> fieldNames, String formDataId)
    throws CmsRpcException;

    /**
     * Validates the new content values for a content loaded in the given session.<p>
     *
     * @param sessionId the id of the session containing the content
     * @param contentValues the values to validate
     *
     * @return the map of validation results
     *
     * @throws CmsUgcException if something goes wrong
     */
    Map<String, String> validateContent(CmsUUID sessionId, Map<String, String> contentValues) throws CmsUgcException;

}
