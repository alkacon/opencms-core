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

package org.opencms.ugc.client.export;

import org.opencms.ugc.client.CmsRequestCounter;
import org.opencms.ugc.client.CmsRpcCallHelper;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.ugc.shared.CmsUgcContent;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.ugc.shared.rpc.I_CmsUgcEditService;
import org.opencms.ugc.shared.rpc.I_CmsUgcEditServiceAsync;
import org.opencms.util.CmsUUID;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Root access point for the Javascript form editing API exported with gwt-exporter. Can be used
 * to acquire new form editing sessions.<p>
 */
@Export
@ExportPackage("opencmsugc")
public class CmsXmlContentUgcApi implements Exportable {

    /** The request counter. */
    private CmsRequestCounter m_requestCounter = new CmsRequestCounter();

    /** Service instance. */
    @NoExport
    public static final I_CmsUgcEditServiceAsync SERVICE = GWT.create(I_CmsUgcEditService.class);

    /** The service URL. */
    @NoExport
    public static final String SERVICE_URL;

    /**
     * Default constructor for gwt-exporter.<p>
     */
    public CmsXmlContentUgcApi() {

        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            public void onUncaughtException(final Throwable e) {

                handleError(e, new I_CmsErrorCallback() {

                    public void call(String errorType, String message, JavaScriptObject additionalData) {

                        throw new RuntimeException(e);
                    }
                });

            }
        });

    }

    static {
        String url = Window.Location.getHref();
        // cut off fragment, parameters, and trailing slash, then append service name
        url = url.replaceAll("#.*$", "").replaceAll("\\?.*$", "").replaceAll("/$", "")
            + "/org.opencms.ugc.CmsUgcEditService.gwt";
        SERVICE_URL = url;
        ((ServiceDefTarget)SERVICE).setServiceEntryPoint(SERVICE_URL);

    }

    /**
     * Gets the request counter.<p>
     *
     * @return the request counter
     */
    @NoExport
    public CmsRequestCounter getRequestCounter() {

        return m_requestCounter;
    }

    /**
     * Creates an RPC helper object.<p>
     *
     * @return the RPC helper
     */
    @NoExport
    public CmsRpcCallHelper getRpcHelper() {

        return new CmsRpcCallHelper(m_requestCounter);
    }

    /**
     * Passes an exception to the given error handling callback and optionally outputs some debug info.<p>
     *
     * @param e the exception
     * @param callback  the error handling callback
     */
    public void handleError(Throwable e, I_CmsErrorCallback callback) {

        String errorCode = CmsUgcConstants.ErrorCode.errMisc.toString();
        String message;
        if (e instanceof CmsUgcException) {
            CmsUgcException formException = (CmsUgcException)e;
            errorCode = formException.getErrorCode().toString();
            message = formException.getUserMessage();
        } else {
            message = e.getMessage();
        }
        if (callback != null) {
            callback.call(errorCode, message, JavaScriptObject.createObject());
        }
    }

    /**
     * Loads a pre-created session.<p>
     *
     * @param sessionId the session id
     * @param formElement the form element
     * @param onSuccess the callback to call in case of success
     * @param onError the callback to call in case of an error
     */
    public void initFormForSession(
        final String sessionId,
        final Element formElement,
        final I_CmsClientCmsUgcSessionCallback onSuccess,
        final I_CmsErrorCallback onError) {

        getRpcHelper().executeRpc(SERVICE.getContent(new CmsUUID(sessionId), new AsyncCallback<CmsUgcContent>() {

            public void onFailure(Throwable caught) {

                handleError(caught, onError);
            }

            public void onSuccess(CmsUgcContent result) {

                CmsClientUgcSession session = new CmsClientUgcSession(CmsXmlContentUgcApi.this, result);
                session.initFormElement(formElement);
                onSuccess.call(session);
            }
        }));
    }

    /**
     * Sets the error callback for all uncaught exceptions.<p>
     *
     * @param callback the error callback
     */
    public void setErrorCallback(final I_CmsStringArrayCallback callback) {

        if (callback != null) {
            GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                public void onUncaughtException(Throwable e) {

                    String[] stack = new String[e.getStackTrace().length];
                    for (int i = 0; i < e.getStackTrace().length; i++) {
                        StackTraceElement stackEl = e.getStackTrace()[i];
                        stack[i] = stackEl.toString();
                    }
                    callback.call(stack);
                }

            });
        } else {
            GWT.setUncaughtExceptionHandler(null);
        }
    }

    /**
     * Sets the wait indicator callback.<p>
     *
     * @param callback a callback used to switch the wait indicator off or on.<p>
     */
    public void setWaitIndicatorCallback(I_CmsBooleanCallback callback) {

        m_requestCounter.setCallback(callback);
    }
}
