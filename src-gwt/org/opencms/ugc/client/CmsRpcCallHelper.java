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

package org.opencms.ugc.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;

/**
 * Helper class to execute an RPC call with a custom callback to enable/disable the wait animation.<p>
 */
public class CmsRpcCallHelper {

    /** Counter to keep track of running requests. */
    private CmsRequestCounter m_requestCounter;

    /**
     * Creates a new instance.<p>
     *
     * @param counter the request counter to keep track of running requests
     */
    public CmsRpcCallHelper(CmsRequestCounter counter) {

        m_requestCounter = counter;

    }

    /**
     * Executes the RPC call.<p>
     *
     * @param requestBuilder the request builder returned by the service interface
     */
    @SuppressWarnings("synthetic-access")
    public void executeRpc(RequestBuilder requestBuilder) {

        final RequestCallback callback = requestBuilder.getCallback();
        RequestCallback callbackWrapper = new RequestCallback() {

            public void onError(com.google.gwt.http.client.Request request, Throwable exception) {

                m_requestCounter.decrement();
                callback.onError(request, exception);
            }

            public void onResponseReceived(
                com.google.gwt.http.client.Request request,
                com.google.gwt.http.client.Response response) {

                m_requestCounter.decrement();
                callback.onResponseReceived(request, response);
            }
        };
        requestBuilder.setCallback(callbackWrapper);
        m_requestCounter.increment();
        try {
            requestBuilder.send();
        } catch (Exception e) {
            m_requestCounter.decrement();
        }
    }

}
