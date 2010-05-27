/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsForm.java,v $
 * Date   : $Date: 2010/05/27 08:06:13 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsValidationHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * This class acts as a container for form fields.<p>
 * 
 * It is also responsible for collecting and validating the values of the form fields.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsForm extends Composite {

    /** The CSS bundle used for this form. **/
    private static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** A map from field ids to the corresponding widgets. */
    private Map<String, I_CmsFormField> m_fields = new LinkedHashMap<String, I_CmsFormField>();

    /** The initial values of the form fields. */
    private Map<String, String> m_initialValues = new HashMap<String, String>();

    /** The main panel for this widget. */
    private FlowPanel m_panel = new FlowPanel();

    /** The list of form reset handlers. */
    private List<I_CmsFormResetHandler> m_resetHandlers = new ArrayList<I_CmsFormResetHandler>();

    /** The internal list of validation handlers. */
    private List<I_CmsValidationHandler> m_validationHandlers = new ArrayList<I_CmsValidationHandler>();

    /**
     * The default constructor.<p>
     * 
     */
    public CmsForm() {

        initWidget(m_panel);

    }

    /**
     * Adds a form field to the form.<p>
     * 
     * @param formField the form field which should be added
     */
    public void addField(final I_CmsFormField formField) {

        String initialValue = formField.getWidget().getFormValueAsString();
        m_initialValues.put(formField.getId(), initialValue);
        String description = formField.getDescription();
        String labelText = formField.getLabel();
        I_CmsFormWidget widget = formField.getWidget();
        if (widget instanceof HasBlurHandlers) {
            ((HasBlurHandlers)widget).addBlurHandler(new BlurHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
                 */
                public void onBlur(BlurEvent event) {

                    formField.validate();
                }
            });
        }
        m_fields.put(formField.getId(), formField);
        addRow(labelText, description, (Widget)widget);

    }

    /**
     * Adds a form field to the form and sets its initial value.<p>
     * 
     * @param formField the form field which should be added 
     * @param initialValue the initial value of the form field, or null if the field shouldn't have an initial value 
     */
    public void addField(I_CmsFormField formField, String initialValue) {

        if (initialValue != null) {
            formField.getWidget().setFormValueAsString(initialValue);
        }
        addField(formField);
    }

    /**
     * Adds a text label.<p>
     * 
     * @param labelText the text for the label
     * @return a label with the given text
     */
    public Label addLabel(String labelText) {

        Label label = new Label(labelText);
        label.setStyleName(CSS.formDescriptionLabel());
        m_panel.add(label);
        return label;
    }

    /** 
     * Adds a new form reset handler to the form.<p>
     * 
     * @param handler the new form reset handler 
     */
    public void addResetHandler(I_CmsFormResetHandler handler) {

        m_resetHandlers.add(handler);
    }

    /**
     * Adds a new row with a given label and input widget to the form.<p>
     * 
     * @param labelText the label text for the form field
     * @param description the description of the form field 
     * @param widget the widget for the form field 
     *  
     * @return the newly added form row 
     */
    public CmsFormRow addRow(String labelText, String description, Widget widget) {

        CmsFormRow row = new CmsFormRow();
        Label label = row.getLabel();
        label.setText(labelText);
        label.setTitle(description);
        row.getWidgetContainer().add(widget);
        m_panel.add(row);
        return row;
    }

    /**
     * Adds a separator below the last added form field.<p>
     * 
     */
    public void addSeparator() {

        m_panel.add(new CmsSeparator());
    }

    /**
     * Adds a new validation handler to the form.<p>
     * 
     * @param handler the validation handler that should be added 
     */
    public void addValidationHandler(I_CmsValidationHandler handler) {

        m_validationHandlers.add(handler);
    }

    /**
     * Collects all values from the form fields.<p>
     * 
     * This method omits form fields whose values are null.
     * 
     * @return a map of the form field values 
     */
    public Map<String, String> collectValues() {

        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, I_CmsFormField> entry : m_fields.entrySet()) {
            String key = entry.getKey();
            String value = null;
            I_CmsFormField field = entry.getValue();
            I_CmsFormWidget widget = field.getWidget();
            value = widget.getFormValueAsString();
            result.put(key, value);
        }
        return result;
    }

    /**
     * Returns the form field with a given id.<p>
     * 
     * @param id the id of the form field 
     * 
     * @return the form field with the given id, or null if no field was found 
     */
    public I_CmsFormField getField(String id) {

        return m_fields.get(id);
    }

    /**
     * Resets all form fields to their initial values.<p>
     */
    public void reset() {

        for (Map.Entry<String, I_CmsFormField> entry : m_fields.entrySet()) {
            String id = entry.getKey();
            I_CmsFormField field = entry.getValue();
            field.getWidget().setFormValueAsString(m_initialValues.get(id));
        }
        validateFields();
        for (I_CmsFormResetHandler resetHandler : m_resetHandlers) {
            resetHandler.onResetForm();
        }
    }

    /**
     * Validates all fields in the form.<p>
     * 
     * When the validation is completed, the validation handler passed as a parameter
     * is notified, unless it's null.
     * 
     * @param handler the object which should handle the result of the validation
     */
    public void validate(I_CmsValidationHandler handler) {

        boolean validationSucceeded = validateFields();
        if (handler != null) {
            handler.onValidationComplete(validationSucceeded);
        }
    }

    /**
     * Validates all fields in the form and returns the result.<p>
     * 
     * @return true if the validation was successful 
     */
    protected boolean validateFields() {

        boolean validationSucceeded = true;
        for (Map.Entry<String, I_CmsFormField> entry : m_fields.entrySet()) {
            I_CmsFormField field = entry.getValue();
            if (!field.validate()) {
                validationSucceeded = false;
            }
        }
        return validationSucceeded;
    }
}
