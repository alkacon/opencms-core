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

package org.opencms.editors.usergenerated.shared.rpc;

import org.opencms.editors.usergenerated.shared.CmsFormContent;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The form edit service interface.<p>
 */
public interface I_CmsFormEditService extends RemoteService {

    /**
     * Returns the form content for an existing content.<p>
     * 
     * @param configPath the configuration path
     * @param sitepath the resource path
     * 
     * @return the form content information 
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsFormContent getExistingContent(String configPath, String sitepath) throws CmsRpcException;

    /**
     * Returns the form content for new content.<p>
     * 
     * @param configPath the configuration path
     * 
     * @return the form content information 
     * 
     * @throws CmsRpcException if something goes wrong
     */
    CmsFormContent getNewContent(String configPath) throws CmsRpcException;

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

}
