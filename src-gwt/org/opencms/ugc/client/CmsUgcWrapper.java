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

import org.opencms.ugc.client.export.CmsClientUgcSession;
import org.opencms.ugc.client.export.CmsXmlContentUgcApi;
import org.opencms.ugc.client.export.I_CmsErrorCallback;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;

/**
 * Widget used to wrap and manage the state of forms for which the form editing API is used.<p>
 */
public class CmsUgcWrapper extends FormPanel {

    /** The client form session. */
    private CmsClientUgcSession m_formSession;

    /**
     * Wraps an existing form element with this widget.<p>
     *
     * @param element the form element to wrap
     * @param formSessionId the form session  id
     */
    public CmsUgcWrapper(Element element, String formSessionId) {

        super(element, true);
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
    public void setFormSession(CmsClientUgcSession session) {

        m_formSession = session;
    }

    /**
     * Uploads files from the given file input fields.<p<
     *
     * @param fields the set of names of fields containing the files to upload
     * @param filenameCallback the callback to call with the resulting map from field names to file paths
     * @param errorCallback the callback to call with an error message
     */
    public void uploadFields(
        final Set<String> fields,
        final Function<Map<String, String>, Void> filenameCallback,
        final I_CmsErrorCallback errorCallback) {

        disableAllFileFieldsExcept(fields);
        final String id = CmsJsUtils.generateRandomId();
        updateFormAction(id);
        // Using an array here because we can only store the handler registration after it has been created , but
        final HandlerRegistration[] registration = {null};
        registration[0] = addSubmitCompleteHandler(new SubmitCompleteHandler() {

            @SuppressWarnings("synthetic-access")
            public void onSubmitComplete(SubmitCompleteEvent event) {

                enableAllFileFields();
                registration[0].removeHandler();
                CmsUUID sessionId = m_formSession.internalGetSessionId();
                RequestBuilder requestBuilder = CmsXmlContentUgcApi.SERVICE.uploadFiles(
                    sessionId,
                    fields,
                    id,
                    new AsyncCallback<Map<String, String>>() {

                    public void onFailure(Throwable caught) {

                        m_formSession.getContentFormApi().handleError(caught, errorCallback);

                    }

                    public void onSuccess(Map<String, String> fileNames) {

                        filenameCallback.apply(fileNames);

                    }
                });
                m_formSession.getContentFormApi().getRpcHelper().executeRpc(requestBuilder);
                m_formSession.getContentFormApi().getRequestCounter().decrement();
            }
        });
        m_formSession.getContentFormApi().getRequestCounter().increment();
        submit();
    }

    /**
     * Disables all file input fields except the one with the given name.<p>
     *
     * @param fieldNames the set of names of fields that should not be disabled
     */
    void disableAllFileFieldsExcept(Set<String> fieldNames) {

        for (InputElement field : getAllFields()) {
            if (isFileField(field)) {
                boolean shouldDisable = !fieldNames.contains(field.getName());
                field.setDisabled(shouldDisable);
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

        setAction(
            CmsXmlContentUgcApi.SERVICE_URL
                + "?"
                + CmsUgcConstants.PARAM_FORM_DATA_ID
                + "="
                + id
                + "&"
                + CmsUgcConstants.PARAM_SESSION_ID
                + "="
                + m_formSession.getSessionId());
    }
}
