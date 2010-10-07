/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/CmsBasicFormField.java,v $
 * Date   : $Date: 2010/10/07 07:56:34 $
 * Version: $Revision: 1.4 $
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

import org.opencms.gwt.client.ui.input.CmsRegexValidator;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.validation.I_CmsValidator;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic implementation of the I_CmsFormField class.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0 
 */
public class CmsBasicFormField implements I_CmsFormField {

    /** Indicates whether this is an advanced or basic form field. */
    private boolean m_advanced;

    /** The default value of the form field. */
    private Object m_defaultValue;

    /** Description for the form field. */
    private String m_description;

    /** Id of the form field.*/
    private String m_id;

    /** The "ignore" flag. */
    private boolean m_ignore;

    /** Label of the form field. */
    private String m_label;

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
     * @param advanced if true, this is an advanced, else a basic form field 
     */
    public CmsBasicFormField(
        String id,
        String description,
        String label,
        Object defaultValue,
        I_CmsFormWidget widget,
        boolean advanced) {

        super();
        m_id = id;
        m_description = description;
        m_label = label;
        m_widget = widget;
        m_defaultValue = defaultValue;
        m_advanced = advanced;
    }

    /**
     * Utility method for creating a single basic form field from an id and a property configuration. 
     * 
     * @param propertyConfig the configuration of the property
     *   
     * @return the newly created form field 
     */
    public static CmsBasicFormField createField(CmsXmlContentProperty propertyConfig) {

        String id = propertyConfig.getPropertyName();
        String widgetConfigStr = propertyConfig.getWidgetConfiguration();
        if (widgetConfigStr == null) {
            widgetConfigStr = "";
        }

        String label = propertyConfig.getNiceName();
        if (label == null) {
            label = id;
        }

        Map<String, String> widgetConfig = CmsStringUtil.splitAsMap(widgetConfigStr, "|", ":");
        String widgetType = propertyConfig.getWidget();
        I_CmsFormWidget widget = CmsWidgetFactoryRegistry.instance().createFormWidget(widgetType, widgetConfig);
        CmsBasicFormField field = new CmsBasicFormField(
            id,
            propertyConfig.getDescription(),
            label,
            propertyConfig.getDefault(),
            widget,
            "true".equals(propertyConfig.getAdvanced()));
        String ruleRegex = propertyConfig.getRuleRegex();
        if (!CmsStringUtil.isEmpty(ruleRegex)) {
            field.setValidator(new CmsRegexValidator(ruleRegex, propertyConfig.getError()));
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
            result.put(propConfig.getPropertyName(), field);
        }
        return result;

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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#isAdvanced()
     */
    public boolean isAdvanced() {

        return m_advanced;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#isIgnored()
     */
    public boolean isIgnored() {

        return m_ignore;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setId(java.lang.String)
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setIgnore(boolean)
     */
    public void setIgnore(boolean ignore) {

        m_ignore = ignore;
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

}
