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

import org.opencms.editors.usergenerated.client.export.CmsClientFormSession;
import org.opencms.editors.usergenerated.client.export.CmsXmlContentFormApi;
import org.opencms.editors.usergenerated.client.export.I_CmsStringCallback;
import org.opencms.editors.usergenerated.shared.CmsFormConstants;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;

/**
 * Widget used to wrap and manage the state of forms for which the form editing API is used.<p>
 */
public class CmsFormWrapper extends FormPanel {

    /** Field containing the form session id. */
    private Hidden m_formSessionIdField;

    /** The client form session. */
    private CmsClientFormSession m_formSession;

    /**
     * Wraps an existing form element with this widget.<p>
     * 
     * @param element the form element to wrap 
     * @param formSessionId the form session  id 
     */
    public CmsFormWrapper(Element element, String formSessionId) {

        super(element, true);

        m_formSessionIdField = new Hidden(CmsFormConstants.FIELD_SESSION_ID, formSessionId);
        add(m_formSessionIdField);
        setEncoding(FormPanel.ENCODING_MULTIPART);
        onAttach();
    }

    /**
     * Checks if a form field is a file input field.<p>
     * 
     * @param elem the form field to check 
     * @return true if the given field is a file input field 
     */
    public static boolean isFileField(InputElement elem) {

        return "file".equalsIgnoreCase(elem.getType());
    }

    /**
     * Sets the form session.<p>
     * 
     * @param session the form session
     */
    public void setFormSession(CmsClientFormSession session) {

        m_formSession = session;
    }

    /**
     * Uploads only the given field.<p>
     * 
     * @param fieldName tbe name of the field
     * @param fileNameCallback the callback to call with the new path of the uploaded file 
     * @param errorCallback the callback to call with an error message 
     */
    public void uploadField(
        final String fieldName,
        final I_CmsStringCallback fileNameCallback,
        final I_CmsStringCallback errorCallback) {

        disableAllFileFieldsExcept(fieldName);
        final String id = CmsJsUtils.generateRandomId();
        updateFormAction(id);
        // Using an array here because we can only store the handler registration after it has been created , but  
        final HandlerRegistration[] registration = {null};
        registration[0] = addSubmitCompleteHandler(new SubmitCompleteHandler() {

            @SuppressWarnings("synthetic-access")
            public void onSubmitComplete(SubmitCompleteEvent event) {

                registration[0].removeHandler();
                CmsUUID sessionId = m_formSession.internalGetSessionId();
                RequestBuilder requestBuilder = CmsXmlContentFormApi.SERVICE.uploadFile(
                    sessionId,
                    fieldName,
                    id,
                    new AsyncCallback<String>() {

                        public void onFailure(Throwable caught) {

                            errorCallback.call(caught.getMessage());

                        }

                        public void onSuccess(String fileName) {

                            fileNameCallback.call(fileName);

                        }
                    });
                m_formSession.getContentFormApi().getRpcHelper().executeRpc(requestBuilder);
                m_formSession.getContentFormApi().getRequestCounter().decrement();
            }
        });
        m_formSession.getContentFormApi().getRequestCounter().increment();
        submit();
        enableAllFileFields();

    }

    /**
     * Disables all file input fields except the one with the given name.<p>
     * 
     * @param name the name of the field which should not be disabled 
     */
    void disableAllFileFieldsExcept(String name) {

        for (InputElement field : getAllFields()) {
            if (isFileField(field)) {
                field.setDisabled(!field.getName().equals(name));
            }
        }
    }

    /**
     * Enables all file input fields.<p>
     */
    void enableAllFileFields() {

        for (InputElement field : getAllFields()) {
            if (isFileField(field)) {
                field.setDisabled(false);
            }
        }
    }

    /**
     * Gets all form fields.<p>
     * 
     * @return the list of form fields 
     */
    List<InputElement> getAllFields() {

        NodeList<Element> fields = getElement().getElementsByTagName(InputElement.TAG);
        List<InputElement> result = Lists.newArrayList();
        for (int i = 0; i < fields.getLength(); i++) {
            InputElement field = InputElement.as(fields.getItem(i));
            result.add(field);
        }
        return result;
    }

    /**
     * Updates the form's action attribute.<p>
     * 
     * @param id the current form data id 
     */
    private void updateFormAction(String id) {

        setAction(CmsXmlContentFormApi.SERVICE_URL
            + "?"
            + CmsFormConstants.PARAM_FORM_DATA_ID
            + "="
            + id
            + "&"
            + CmsFormConstants.FIELD_SESSION_ID
            + "="
            + m_formSession.getSessionId());
    }
}
