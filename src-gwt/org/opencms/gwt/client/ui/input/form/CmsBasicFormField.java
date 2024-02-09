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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.input.CmsRegexValidator;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Basic implementation of the I_CmsFormField class.<p>
 *
 * @since 8.0.0
 */
public class CmsBasicFormField implements I_CmsFormField {

    /** The default value of the form field. */
    private Object m_defaultValue;

    /** Description for the form field. */
    private String m_description;

    /** Handler registration of the ValueChangeHandler for the string model object, if available. */
    private HandlerRegistration m_handlerRegistration;

    /** Id of the form field.*/
    private String m_id;

    /** Label of the form field. */
    private String m_label;

    /** A map of strings containing layout information for rendering the field's widget. */
    private LayoutData m_layoutData = new LayoutData();

    /** The string model. */
    private I_CmsStringModel m_model;

    /** The current validation status (only used if the field has a validator). */
    private I_CmsFormField.ValidationStatus m_validationStatus = I_CmsFormField.ValidationStatus.unknown;

    /** Validator of the form field. */
    private I_CmsValidator m_validator;

    /** Widget of the form field. */
    private I_CmsFormWidget m_widget;

    /**
     * Constructs a new form field.<p>
     *
     * @param id the id of the form field
     * @param description the description of the form field
     * @param label the label of the form field
     * @param defaultValue the default value of the form field
     * @param widget the widget of the form field
     */
    public CmsBasicFormField(String id, String description, String label, Object defaultValue, I_CmsFormWidget widget) {

        super();
        m_id = id;
        m_description = description;
        m_label = label;
        m_widget = widget;
        m_defaultValue = defaultValue;
    }

    /**
     * Utility method for creating a single basic form field from an id and a property configuration.
     *
     * @param propertyConfig the configuration of the property
     *
     * @return the newly created form field
     */
    public static CmsBasicFormField createField(CmsXmlContentProperty propertyConfig) {

        return createField(propertyConfig, Collections.<String, String> emptyMap());
    }

    /**
     * Utility method for creating a basic form field.<p>
     *
     * @param propertyConfig the property configuration
     * @param additionalParams the additional parameters
     *
     * @return the newly created form fields
     */
    public static CmsBasicFormField createField(
        CmsXmlContentProperty propertyConfig,
        Map<String, String> additionalParams) {

        return createField(
            propertyConfig,
            propertyConfig.getName(),
            CmsWidgetFactoryRegistry.instance(),
            additionalParams,
            false);
    }

    /**
     * Utility method for creating a single basic form field from an id and a property configuration.
     *
     * @param propertyConfig the configuration of the property
     * @param fieldId the field id
     * @param factory a factory for creating form  widgets
     * @param additionalParams additional field parameters
     * @param alwaysAllowEmpty indicates an empty value is allowed
     *
     * @return the newly created form field
     */
    public static CmsBasicFormField createField(
        CmsXmlContentProperty propertyConfig,
        String fieldId,
        I_CmsFormWidgetMultiFactory factory,
        Map<String, String> additionalParams,
        boolean alwaysAllowEmpty) {

        String widgetConfigStr = propertyConfig.getWidgetConfiguration();
        if (widgetConfigStr == null) {
            widgetConfigStr = "";
        }

        String label = propertyConfig.getNiceName();
        if (label == null) {
            label = propertyConfig.getName();
        }

        String description = propertyConfig.getDescription();
        if (CmsStringUtil.isEmpty(description)) {
            description = "";
        }

        Map<String, String> widgetConfig = CmsStringUtil.splitOptions(widgetConfigStr);
        widgetConfig.putAll(additionalParams);
        String widgetType = propertyConfig.getWidget();
        I_CmsFormWidget widget = factory.createFormWidget(
            widgetType,
            widgetConfig,
            Optional.fromNullable(propertyConfig.getDefault()));
        CmsBasicFormField field = new CmsBasicFormField(
            fieldId,
            description,
            label,
            propertyConfig.getDefault(),
            widget);
        String ruleRegex = propertyConfig.getRuleRegex();
        if (!CmsStringUtil.isEmpty(ruleRegex)) {
            String error = propertyConfig.getError();
            if (error == null) {
                error = "??? validation error";
            }
            field.setValidator(new CmsRegexValidator(ruleRegex, error, alwaysAllowEmpty));
        }
        return field;
    }

    /**
     * Creates a map of fields from a map of field configurations.<p>
     *
     * @param propertyConfigurations the map of field configurations
     *
     * @return a map of form fields
     */
    public static Map<String, I_CmsFormField> createFields(Collection<CmsXmlContentProperty> propertyConfigurations) {

        // using LinkedHashMap to preserve the order
        Map<String, I_CmsFormField> result = new LinkedHashMap<String, I_CmsFormField>();
        for (CmsXmlContentProperty propConfig : propertyConfigurations) {
            CmsBasicFormField field = createField(propConfig);
            result.put(propConfig.getName(), field);
        }
        return result;

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#bind(org.opencms.gwt.client.ui.input.I_CmsStringModel)
     */
    public void bind(I_CmsStringModel model) {

        assert m_model == null;
        m_model = model;
        m_handlerRegistration = m_model.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                I_CmsFormWidget widget = getWidget();
                if ((widget instanceof I_CmsHasGhostValue)
                    && !CmsStringUtil.isEmptyOrWhitespaceOnly(event.getValue())) {
                    ((I_CmsHasGhostValue)widget).setGhostMode(false);
                }
                widget.setFormValueAsString(event.getValue());
            }
        });
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        return (o instanceof CmsBasicFormField) && ((CmsBasicFormField)o).getId().equals(m_id);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getDefaultValue()
     */
    public Object getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getLabel()
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getLayoutData()
     */
    public LayoutData getLayoutData() {

        return m_layoutData;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getModel()
     */
    public I_CmsStringModel getModel() {

        return m_model;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getModelId()
     */
    public String getModelId() {

        if (m_model != null) {
            return m_model.getId();
        }
        return getId();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getModelValue()
     */
    public String getModelValue() {

        if (m_model != null) {
            return m_model.getValue();
        }
        return getWidget().getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getValidationStatus()
     */
    public I_CmsFormField.ValidationStatus getValidationStatus() {

        return m_validator != null ? m_validationStatus : I_CmsFormField.ValidationStatus.valid;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getValidator()
     */
    public I_CmsValidator getValidator() {

        return m_validator;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getWidget()
     */
    public I_CmsFormWidget getWidget() {

        return m_widget;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id.hashCode();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setId(java.lang.String)
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setValidationStatus(org.opencms.gwt.client.ui.input.I_CmsFormField.ValidationStatus)
     */
    public void setValidationStatus(I_CmsFormField.ValidationStatus validationStatus) {

        if (m_validator != null) {
            m_validationStatus = validationStatus;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setValidator(org.opencms.gwt.client.validation.I_CmsValidator)
     */
    public void setValidator(I_CmsValidator validator) {

        m_validator = validator;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#unbind()
     */
    public void unbind() {

        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
            m_handlerRegistration = null;
        }
    }

}
