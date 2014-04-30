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

package org.opencms.editors.usergenerated.client.export;

import org.opencms.editors.usergenerated.client.CmsFormWrapper;
import org.opencms.editors.usergenerated.client.CmsJsUtils;
import org.opencms.editors.usergenerated.shared.CmsFormConstants;
import org.opencms.editors.usergenerated.shared.CmsFormContent;
import org.opencms.editors.usergenerated.shared.CmsFormException;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.google.common.base.Function;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Client side object representing a form editing session.<p>
 */
@Export
@ExportPackage("opencms")
public class CmsClientFormSession implements Exportable {

    /** The CmsXmlContentFormApi which was used to create this session object. */
    private CmsXmlContentFormApi m_apiRoot;

    /** The form content returned from the server. */
    private CmsFormContent m_content;

    /** The map of new values to set in the content. */
    private Map<String, String> m_newValues = new HashMap<String, String>();

    /** The form wrapper widget. */
    private CmsFormWrapper m_formWrapper;

    /** 
     * Creates a new instance.<p>
     * 
     * @param apiRoot the CmsXmlContentFormApi instance which was used to create this session object 
     * @param content the form data returned from the server 
     */
    public CmsClientFormSession(CmsXmlContentFormApi apiRoot, CmsFormContent content) {

        m_content = content;
        m_apiRoot = apiRoot;
    }

    /**
     * Destroys the session.<p>
     */
    public void destroy() {

        CmsXmlContentFormApi.SERVICE.destroySession(m_content.getSessionId(), new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {

                // TODO Auto-generated method stub

            }

            public void onSuccess(Void result) {

                // TODO Auto-generated method stub

            }
        });

    }

    /**
     * Gets the content form API instance.<p>
     * 
     * @return the content form API instance
     */
    @NoExport
    public CmsXmlContentFormApi getContentFormApi() {

        return m_apiRoot;

    }

    /**
     * Gets the old content value originally returned from the server.<p>
     * 
     * @param xpath the xpath for which the value should be returned
     * @return the value for the xpath, or null if there is no value for the path 
     */
    public String getOldValue(String xpath) {

        return m_content.getContentValues().get(xpath);
    }

    /**
     * Gets the array of xpaths of the values which are set in the original content received from the server.<p>
     * 
     * @return the array of xpaths from the content 
     */
    public String[] getOldValueKeys() {

        String[] result = new String[m_content.getContentValues().size()];
        int i = 0;
        for (String key : m_content.getContentValues().keySet()) {
            result[i] = key;
            i += 1;
        }
        return result;
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
     * Initializes the form belonging to this session.<p>
     * 
     * @param formElement the form element
     */
    @NoExport
    public void initFormElement(Element formElement) {

        m_formWrapper = new CmsFormWrapper(formElement, getSessionId());
        m_formWrapper.setFormSession(this);
    }

    /**
     * Gets the session id as a UUID.<p>
     * 
     * @return the session id 
     */
    public CmsUUID internalGetSessionId() {

        return m_content.getSessionId();
    }

    /**
     * Asks the server to save the values set via setNewValue in the XML content.<p>
     * 
     * @param onSuccess the callback to be called in case of success 
     * @param onFailure the callback to be called in case of failure 
     */
    public void saveContent(final I_CmsStringCallback onSuccess, final I_CmsErrorCallback onFailure) {

        m_apiRoot.getRpcHelper().executeRpc(
            CmsXmlContentFormApi.SERVICE.saveContent(
                m_content.getSessionId(),
                m_newValues,
                new AsyncCallback<Map<String, String>>() {

                    @SuppressWarnings("synthetic-access")
                    public void onFailure(Throwable caught) {

                        CmsFormException formException = (CmsFormException)caught;
                        m_apiRoot.handleError(formException, onFailure);
                    }

                    public void onSuccess(Map<String, String> result) {

                        if ((result == null) || result.isEmpty()) {
                            onSuccess.call("");
                        } else {
                            JSONObject validationErrors = new JSONObject();
                            for (Map.Entry<String, String> entry : result.entrySet()) {
                                validationErrors.put(entry.getKey(), new JSONString(entry.getValue()));
                            }
                            onFailure.call(
                                CmsFormConstants.ErrorCode.errValidation.toString(),
                                "",
                                validationErrors.getJavaScriptObject());
                        }
                    }
                }));

    }

    /**
     * Sets a new content value to be saved later.<p>
     * 
     * @param xpath the path of the value to set 
     * @param value the actual value 
     */
    public void setNewValue(String xpath, String value) {

        m_newValues.put(xpath, value);
    }

    /**
     * Uploads a single file from a file input field.<p>
     * 
     * @param fieldName the name of the form field to upload 
     * @param fileCallback the callback for the result
     * @param errorCallback the error handling callback  
     */
    public void uploadFile(
        final String fieldName,
        final I_CmsStringCallback fileCallback,
        I_CmsErrorCallback errorCallback) {

        Set<String> fieldSet = new HashSet<String>();
        fieldSet.add(fieldName);
        m_formWrapper.uploadFields(fieldSet, new Function<Map<String, String>, Void>() {

            public Void apply(Map<String, String> input) {

                fileCallback.call(input.get(fieldName));
                return null;
            }
        }, errorCallback);

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
}
