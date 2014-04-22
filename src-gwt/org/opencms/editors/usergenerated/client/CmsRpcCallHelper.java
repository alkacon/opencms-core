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

package org.opencms.editors.usergenerated.client;

import org.opencms.editors.usergenerated.client.export.I_CmsBooleanCallback;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;

/**
 * Helper class to execute an RPC call with a custom callback to enable/disable the wait animation.<p>
 */
public class CmsRpcCallHelper {

    /** Count of waiting RPC calls. */
    private static int WAIT_COUNT = 0;

    /** The callback to enable/disable the wait indicator. */
    private I_CmsBooleanCallback m_waitIndicatorCallback;

    /** 
     * Creates a new instance.<p>
     * 
     * @param waitIndicatorCallback the wait indicator callback to use
     */
    public CmsRpcCallHelper(I_CmsBooleanCallback waitIndicatorCallback) {

        m_waitIndicatorCallback = waitIndicatorCallback;
    }

    /**
     * Executes the RPC call.<p>
     * 
     * @param requestBuilder the request builder returned by the service interface 
     */
    public void executeRpc(RequestBuilder requestBuilder) {

        final RequestCallback callback = requestBuilder.getCallback();
        RequestCallback callbackWrapper = new RequestCallback() {

            public void onError(com.google.gwt.http.client.Request request, Throwable exception) {

                stopLoading();
                callback.onError(request, exception);
            }

            public void onResponseReceived(
                com.google.gwt.http.client.Request request,
                com.google.gwt.http.client.Response response) {

                stopLoading();
                callback.onResponseReceived(request, response);
            }
        };
        requestBuilder.setCallback(callbackWrapper);
        startLoading();
        try {
            requestBuilder.send();
        } catch (Exception e) {
            stopLoading();
        }
    }

    /** 
     * Called before  sending a  request.<p>
     */
    protected void startLoading() {

        WAIT_COUNT += 1;
        m_waitIndicatorCallback.call(true);
    }

    /**
     * Called after receiving a response.<p>
     */
    protected void stopLoading() {

        WAIT_COUNT -= 1;
        if (WAIT_COUNT == 0) {
            m_waitIndicatorCallback.call(false);
        }
    }

}
