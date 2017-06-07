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

import org.opencms.ugc.shared.CmsUgcContent;
import org.opencms.util.CmsUUID;

import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * The asynchronous form edit service interface.<p>
 */
public interface I_CmsUgcEditServiceAsync {

    /**
     * Destroys a session.<p>
     *
     * @param sessionId the id of the session to destroy
     *
     * @param callback if something goes wrong
     */
    @SynchronizedRpcRequest
    void destroySession(CmsUUID sessionId, AsyncCallback<Void> callback);

    /**
     * Returns the form content for an existing session.<p>
     *
     * @param sessionId the id of the existing session
     * @param callback the callback for the result
     *
     * @return the request builder for this RPC call
     */
    RequestBuilder getContent(CmsUUID sessionId, AsyncCallback<CmsUgcContent> callback);

    /**
     * Gets the link corresponding to a given site path.<p>
     *
     * @param path the site path
     * @param resultCallback the callback for the result
     *
     * @return the request builder for the RPC call
     */
    RequestBuilder getLink(String path, AsyncCallback<String> resultCallback);

    /**
     * Saves the given content values to the edited content.<p>
     *
     * @param sessionId the session id
     * @param contentValues the content values
     * @param callback the callback
     *
     * @return the request builder for this  RPC call
     */
    RequestBuilder saveContent(
        CmsUUID sessionId,
        Map<String, String> contentValues,
        AsyncCallback<Map<String, String>> callback);

    /**
     * Uploads submitted file form fields to the VFS.<p>
     *
     * @param sessionId the session id
     * @param fieldNames the set of names of the form fields containing the uploads
     * @param formDataId the form data id
     * @param filenameCallback the callback to call with the resulting map from field names to file paths
     *
     * @return the request builder for this service method
     */
    RequestBuilder uploadFiles(
        CmsUUID sessionId,
        Set<String> fieldNames,
        String formDataId,
        AsyncCallback<Map<String, String>> filenameCallback);

    /**
     * Validates the new content values for a content loaded in the given session.<p>
     *
     * @param sessionId the id of the session containing the content
     * @param values the values to validate
     * @param resultCallback the callback to call with the result
     *
     * @return the request builder for the RPC call
     */
    RequestBuilder validateContent(
        CmsUUID sessionId,
        Map<String, String> values,
        AsyncCallback<Map<String, String>> resultCallback);
}
