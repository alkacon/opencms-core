/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsForm.java,v $
 * Date   : $Date: 2010/05/10 06:54:24 $
 * Version: $Revision: 1.3 $
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

import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsValidationHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsForm extends Composite {

    /** A map from field ids to the corresponding widgets. */
    private Map<String, I_CmsFormField> m_fields = new LinkedHashMap<String, I_CmsFormField>();

    /** The initial values of the form fields. */
    private Map<String, String> m_initialValues = new HashMap<String, String>();

    /** The main panel for this widget. */
    private FlowPanel m_panel = new FlowPanel();

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
     * @param initialValue the initial value of the form field, or null if the field shouldn't have an initial value 
     */
    public void addField(I_CmsFormField formField, String initialValue) {

        String description = formField.getDescription();
        String labelText = formField.getLabel();
        I_CmsFormWidget widget = formField.getWidget();
        m_fields.put(formField.getId(), formField);
        if (initialValue != null) {
            formField.getWidget().setFormValueAsString(initialValue);
        }
        m_initialValues.put(formField.getId(), initialValue);
        addRow(labelText, description, (Widget)widget);

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
            if (value != null) {
                result.put(key, value);
            }
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
