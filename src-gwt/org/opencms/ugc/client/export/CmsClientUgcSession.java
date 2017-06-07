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

import org.opencms.ugc.client.CmsJsUtils;
import org.opencms.ugc.client.CmsUgcWrapper;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.ugc.shared.CmsUgcContent;
import org.opencms.ugc.shared.CmsUgcException;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Client side object representing a form editing session.<p>
 */
@Export
@ExportPackage("opencmsugc")
public class CmsClientUgcSession implements Exportable {

    /** The CmsXmlContentFormApi which was used to create this session object. */
    private CmsXmlContentUgcApi m_apiRoot;

    /** The form content returned from the server. */
    private CmsUgcContent m_content;

    /** The form wrapper widget. */
    private CmsUgcWrapper m_formWrapper;

    /**
     * Creates a new instance.<p>
     *
     * @param apiRoot the CmsXmlContentFormApi instance which was used to create this session object
     * @param content the form data returned from the server
     */
    public CmsClientUgcSession(CmsXmlContentUgcApi apiRoot, CmsUgcContent content) {

        m_content = content;
        m_apiRoot = apiRoot;
    }

    /**
     * Destroys the session.<p>
     */
    public void destroy() {

        CmsXmlContentUgcApi.SERVICE.destroySession(m_content.getSessionId(), new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {

                throw new RuntimeException(caught);

            }

            public void onSuccess(Void result) {

                // do nothing

            }
        });

    }

    /**
     * Gets the content form API instance.<p>
     *
     * @return the content form API instance
     */
    @NoExport
    public CmsXmlContentUgcApi getContentFormApi() {

        return m_apiRoot;

    }

    /**
     * Fetches the link for a given path from the server.<p>
     *
     * @param path the path for which we want the link
     *
     * @param callback the callback to call with the result
     */
    public void getLink(String path, final I_CmsStringCallback callback) {

        m_apiRoot.getRpcHelper().executeRpc(CmsXmlContentUgcApi.SERVICE.getLink(path, new AsyncCallback<String>() {

            @SuppressWarnings("synthetic-access")
            public void onFailure(Throwable caught) {

                m_apiRoot.handleError(caught, null);
            }

            public void onSuccess(String result) {

                callback.call(result);
            }
        }));
    }

    /**
     * Gets the session id.<p>
     *
     * @return the session id
     */
    public String getSessionId() {

        return "" + m_content.getSessionId();
    }

    /**
     * Gets the site path of the edited content.<p>
     *
     * @return the site path of the edited content
     */
    public String getSitePath() {

        return m_content.getSitePath();
    }

    /**
     * Gets the old content values as a Javascript object.<p>
     *
     * @return a Javascript object whose properties are the xpaths of the existing content values
     */
    public JavaScriptObject getValues() {

        return CmsJsUtils.convertMapToJsObject(m_content.getContentValues());
    }

    /**
     * Initializes the form belonging to this session.<p>
     *
     * @param formElement the form element
     */
    @NoExport
    public void initFormElement(Element formElement) {

        m_formWrapper = new CmsUgcWrapper(formElement, getSessionId());
        m_formWrapper.setFormSession(this);
    }

    /**
     * Gets the session id as a UUID.<p>
     *
     * @return the session id
     */
    @NoExport
    public CmsUUID internalGetSessionId() {

        return m_content.getSessionId();
    }

    /**
     * Asks the server to save the values set via setNewValue in the XML content.<p>
     *
     * @param newValues the new values to set
     * @param onSuccess the callback to be called in case of success
     * @param onFailure the callback to be called in case of failure
     */
    public void saveContent(
        JavaScriptObject newValues,
        final I_CmsStringCallback onSuccess,
        final I_CmsErrorCallback onFailure) {

        m_apiRoot.getRpcHelper().executeRpc(
            CmsXmlContentUgcApi.SERVICE.saveContent(
                m_content.getSessionId(),
                CmsJsUtils.convertJsObjectToMap(newValues),
                new AsyncCallback<Map<String, String>>() {

                    @SuppressWarnings("synthetic-access")
                    public void onFailure(Throwable caught) {

                        CmsUgcException formException = (CmsUgcException)caught;
                        m_apiRoot.handleError(formException, onFailure);
                    }

                    public void onSuccess(Map<String, String> result) {

                        if ((result == null) || result.isEmpty()) {
                            onSuccess.call("");
                        } else {
                            JavaScriptObject validationErrorsJso = CmsJsUtils.convertMapToJsObject(result);
                            onFailure.call(CmsUgcConstants.ErrorCode.errValidation.toString(), "", validationErrorsJso);
                        }
                    }
                }));

    }

    /**
     * Uploads multiple files.<p>
     *
     * @param fieldNames the array of form field names containing files to upload
     * @param fileCallback the callback for the results
     * @param errorCallback the error handling callback
     */
    public void uploadFiles(
        String[] fieldNames,
        final I_CmsJavaScriptObjectCallback fileCallback,
        I_CmsErrorCallback errorCallback) {

        Set<String> fieldSet = new HashSet<String>();
        for (String field : fieldNames) {
            fieldSet.add(field);
        }

        m_formWrapper.uploadFields(fieldSet, new Function<Map<String, String>, Void>() {

            public Void apply(Map<String, String> input) {

                fileCallback.call(CmsJsUtils.convertMapToJsObject(input));
                return null;
            }
        }, errorCallback);
    }

    /**
     * Validates the new content values.<p>
     *
     * @param newValues a Javascript object with the value xpaths as keys and the corresponding content values as values.<p>
     *
     * @param onSuccess the callback to call with the validation results
     */
    public void validate(JavaScriptObject newValues, final I_CmsJavaScriptObjectCallback onSuccess) {

        AsyncCallback<Map<String, String>> rpcCallback = new AsyncCallback<Map<String, String>>() {

            @SuppressWarnings("synthetic-access")
            public void onFailure(Throwable caught) {

                m_apiRoot.handleError(caught, null);
            }

            public void onSuccess(Map<String, String> result) {

                if (result == null) {
                    result = Maps.newHashMap();
                }
                onSuccess.call(CmsJsUtils.convertMapToJsObject(result));
            }
        };
        m_apiRoot.getRpcHelper().executeRpc(
            CmsXmlContentUgcApi.SERVICE.validateContent(
                m_content.getSessionId(),
                CmsJsUtils.convertJsObjectToMap(newValues),
                rpcCallback));

    }
}
