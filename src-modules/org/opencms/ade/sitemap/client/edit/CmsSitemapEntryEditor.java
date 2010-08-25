/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2010/08/25 15:24:41 $
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectBox;
import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectCell;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsNonEmptyValidator;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.util.CmsPair;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.9 $
 *  
 *  @since 8.0.0
 */
public class CmsSitemapEntryEditor extends CmsFormDialog {

    /**
     * The editor mode.<p>
     */
    public enum Mode {

        /** Collision resolution while drag'n drop. */
        DND,

        /** Edition of an existing entry. */
        EDIT,

        /** Creation of a new entry. */
        NEW;
    }

    /** The key for the default template. */
    private static final String DEFAULT_TEMPLATE_VALUE = "";

    /** The field id of the 'template' property. */
    private static final String FIELD_TEMPLATE = "template";

    /** The field id of the 'template-inherited' property. */
    private static final String FIELD_TEMPLATE_INHERIT_CHECKBOX = "field_template_inherited";

    /** The field id of the "title" form field. */
    private static final String FIELD_TITLE = "field_title";

    /** The field id of the "url name" form field. */
    private static final String FIELD_URLNAME = "field_urlname";

    /** The sitemap controller which changes the actual entry data when the user clicks OK in this dialog. */
    protected CmsSitemapController m_controller;

    /** The handler for this sitemap entry editor. */
    protected I_CmsSitemapEntryEditorHandler m_handler;

    /** The configuration of the properties. */
    private Map<String, CmsXmlContentProperty> m_propertyConfig;

    /**
     * Creates a new sitemap entry editor.<p>
     * 
     * @param handler the handler
     */
    public CmsSitemapEntryEditor(I_CmsSitemapEntryEditorHandler handler) {

        super(handler.getDialogTitle());

        m_controller = handler.getController();
        m_propertyConfig = removeHiddenProperties(m_controller.getData().getProperties());
        m_handler = handler;
        setFormHandler(new I_CmsFormHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map)
             */
            public void onSubmitForm(Map<String, String> fieldValues) {

                final String titleValue = getAndRemoveValue(fieldValues, FIELD_TITLE);
                CmsPair<String, String> templateProps = getTemplateProperties(fieldValues);
                fieldValues.put(CmsSitemapManager.Property.template.toString(), templateProps.getFirst());
                fieldValues.put(CmsSitemapManager.Property.templateInherited.toString(), templateProps.getSecond());
                if (!m_handler.hasEditableName()) {
                    // The root element's name can't be edited 
                    hide();
                    m_handler.handleSubmit(titleValue, "", null, fieldValues);
                    return;
                }
                final String urlNameValue = getAndRemoveValue(fieldValues, FIELD_URLNAME);
                m_handler.handleSubmit(titleValue, urlNameValue, null, fieldValues);
            }
        });
    }

    /**
     * Shows the sitemap entry editor to the user.<p>
     */
    public void start() {

        CmsForm form = getForm();

        form.addLabel(m_handler.getDescriptionText());

        if (m_handler.hasEditableName()) {
            // the root entry name can't be edited 
            CmsBasicFormField urlNameField = createUrlNameField();
            form.addField(urlNameField);
        }

        CmsBasicFormField titleField = createTitleField();
        form.addField(titleField);

        Map<String, String> properties = m_handler.getEntry().getProperties();
        String propTemplate = properties.get(CmsSitemapManager.Property.template.toString());
        String propTemplateInherited = properties.get(CmsSitemapManager.Property.templateInherited.toString());
        boolean inheritTemplate = (propTemplate != null) && propTemplate.equals(propTemplateInherited);
        CmsBasicFormField templateField = createTemplateField();
        String initialTemplate = propTemplate != null ? propTemplate : "";
        form.addField(templateField, initialTemplate);

        CmsBasicFormField templateInheritField = createTemplateInheritField();
        form.addField(templateInheritField, "" + inheritTemplate);

        form.addSeparator();
        Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(m_propertyConfig.values());
        for (I_CmsFormField field : formFields.values()) {
            String currentValue = properties.get(field.getId());
            form.addField(field, currentValue);
        }
        center();
    }

    /**
     * Starts the sitemap entry editor and validates all form fields.<p>
     */
    public void startAndValidate() {

        start();
        getForm().doInitialValidation();
    }

    /** 
     * Helper method which retrieves a value for a given key from a map and then deletes the entry for the key.<p>
     * 
     * @param map the map from which to retrieve the value 
     * @param key the key
     * 
     * @return the removed value  
     */
    protected String getAndRemoveValue(Map<String, String> map, String key) {

        String value = map.get(key);
        if (value != null) {
            map.remove(key);
        }
        return value;
    }

    /**
     * Helper method for extracting new values for the 'template' and 'template-inherited' properties from the
     * raw form data.<p>
     * 
     * @param fieldValues the string map produced by the form 
     * 
     * @return a pair containing the 'template' and 'template-inherit' property, in that order
     */
    protected CmsPair<String, String> getTemplateProperties(Map<String, String> fieldValues) {

        String shouldInheritTemplateStr = getAndRemoveValue(fieldValues, FIELD_TEMPLATE_INHERIT_CHECKBOX);
        String template = fieldValues.get(CmsSitemapManager.Property.template.toString());
        if (template.equals(DEFAULT_TEMPLATE_VALUE)) {
            // return nulls to cause the properties to be deleted  
            return new CmsPair<String, String>(null, null);
        }

        // only inherit the template if checkbox is checked 
        String templateInherited = Boolean.parseBoolean(shouldInheritTemplateStr) ? template : null;
        return new CmsPair<String, String>(template, templateInherited);
    }

    /**
     * Returns a localized message from the message bundle.<p>
     * 
     * @param key the message key
     * @param args the message parameters
     *  
     * @return the localized message 
     */
    protected String message(String key, Object... args) {

        return Messages.get().key(key, args);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickCancel()
     */
    @Override
    protected void onClickCancel() {

        super.onClickCancel();
        m_handler.handleCancel();
    }

    /**
     * Sets the contents of the URL name field in the form.<p>
     * 
     * @param urlName the new URL name
     */
    protected void setUrlNameField(String urlName) {

        getForm().getField(FIELD_URLNAME).getWidget().setFormValueAsString(urlName);
    }

    /**
     * Shows an error message next to the URL name input field.<p>
     * 
     * @param message the message which should be displayed, or null if no message should be displayed 
     */
    protected void showUrlNameError(String message) {

        getForm().getField(FIELD_URLNAME).getWidget().setErrorMessage(message);
    }

    /**
     * Helper method for creating the form field for selecting a template.<p>
     * 
     * @return the template form field 
     */
    private CmsBasicFormField createTemplateField() {

        String description = message(Messages.GUI_TEMPLATE_PROPERTY_DESC_0);
        String label = message(Messages.GUI_TEMPLATE_PROPERTY_TITLE_0);
        CmsTemplateSelectBox select = createTemplateSelector(m_controller.getData().getTemplates());
        return new CmsBasicFormField(FIELD_TEMPLATE, description, label, null, select);
    }

    /** 
     * Helper method for creating the form field for selecting whether the template should be inherited or not.<p>
     * 
     * @return the new form field 
     */
    private CmsBasicFormField createTemplateInheritField() {

        String description = "";
        String label = "";
        CmsCheckBox checkbox = new CmsCheckBox(message(Messages.GUI_TEMPLATE_INHERIT_0));
        CmsBasicFormField result = new CmsBasicFormField(
            FIELD_TEMPLATE_INHERIT_CHECKBOX,
            description,
            label,
            null,
            checkbox);
        return result;
    }

    /**
     * Helper method for creating the template selection widget.<p>
     * 
     * @param templates the map of available templates
     * 
     * @return the template selector widget 
     */
    private CmsTemplateSelectBox createTemplateSelector(Map<String, CmsSitemapTemplate> templates) {

        CmsTemplateSelectBox result = new CmsTemplateSelectBox();
        for (Map.Entry<String, CmsSitemapTemplate> templateEntry : templates.entrySet()) {
            CmsSitemapTemplate template = templateEntry.getValue();
            CmsTemplateSelectCell selectCell = new CmsTemplateSelectCell();
            selectCell.setTemplate(template);
            result.addOption(selectCell);
        }
        CmsTemplateSelectCell defaultCell = new CmsTemplateSelectCell();
        defaultCell.setTemplate(getDefaultTemplate());
        result.addOption(defaultCell);
        return result;
    }

    /**
     * Creates the text field for editing the title.<p>
     * 
     * @return the newly created form field 
     */
    private CmsBasicFormField createTitleField() {

        String description = message(Messages.GUI_TITLE_PROPERTY_DESC_0);
        String label = message(Messages.GUI_TITLE_PROPERTY_0);

        CmsBasicFormField result = new CmsBasicFormField(FIELD_TITLE, description, label, null, new CmsTextBox());
        String title = m_handler.getTitle();
        if (title == null) {
            title = "";
        }
        result.getWidget().setFormValueAsString(title);
        result.setValidator(new CmsNonEmptyValidator(Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0)));
        return result;
    }

    /**
     * Creates the text field for editing the URL name.<p>
     * 
     * @return the newly created form field 
     */
    private CmsBasicFormField createUrlNameField() {

        String description = message(Messages.GUI_URLNAME_PROPERTY_DESC_0);
        String label = message(Messages.GUI_URLNAME_PROPERTY_0);
        final CmsTextBox textbox = new CmsTextBox();

        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, textbox);
        String urlName = m_handler.getName();
        if (urlName == null) {
            urlName = "";
        }
        result.getWidget().setFormValueAsString(urlName);
        List<String> forbiddenUrlNames = m_handler.getForbiddenUrlNames();
        result.setValidator(new CmsUrlNameValidator(forbiddenUrlNames));
        return result;
    }

    /**
     * Returns the template which should be used as the "use default" option in the template selector.<p>
     * 
     * @return the default template 
     */
    private CmsSitemapTemplate getDefaultTemplate() {

        CmsSitemapTemplate template = m_controller.getDefaultTemplate(m_handler.getEntry().getSitePath());
        // replace site path with empty string and title with "default" 
        String defaultTitle = message(Messages.GUI_DEFAULT_TEMPLATE_TITLE_0);
        return new CmsSitemapTemplate(
            defaultTitle,
            template.getDescription(),
            DEFAULT_TEMPLATE_VALUE,
            template.getImgPath());
    }

    /**
     * Helper method for removing hidden properties from a map of property configurations.<p>
     * 
     * The map passed into the method is not changed; a map which only contains the non-hidden
     * property definitions is returned.<p>
     * 
     * @param propConfig the property configuration 
     * 
     * @return the filtered property configuration 
     */
    private Map<String, CmsXmlContentProperty> removeHiddenProperties(Map<String, CmsXmlContentProperty> propConfig) {

        Map<String, CmsXmlContentProperty> result = new HashMap<String, CmsXmlContentProperty>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : propConfig.entrySet()) {
            if (!m_controller.isHiddenProperty(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
