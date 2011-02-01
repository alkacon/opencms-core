/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2011/02/01 15:25:05 $
 * Version: $Revision: 1.17 $
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
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.gwt.client.ui.input.CmsLinkSelector;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.shared.CmsLinkBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;
import org.opencms.xml.sitemap.properties.CmsSourcedValue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.17 $
 *  
 *  @since 8.0.0
 */
public class CmsSitemapEntryEditor extends CmsFormDialog {

    /** The key for the default template. */
    private static final String DEFAULT_TEMPLATE_VALUE = "";

    /** The field id for the link selector widget. */
    private static final String FIELD_LINK = "field_link";

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

    /** The link selector widget. */
    protected CmsLinkSelector m_linkSelector = new CmsLinkSelector();

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
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map, java.util.Set)
             */
            public void onSubmitForm(Map<String, String> fieldValues, Set<String> editedFields) {

                Map<String, CmsSimplePropertyValue> simpleProps;

                final String titleValue = getAndRemoveValue(fieldValues, FIELD_TITLE);
                if (!m_handler.hasEditableName()) {
                    // The root element's name can't be edited 
                    hide();
                    simpleProps = convertProperties(fieldValues);
                    m_handler.handleSubmit(titleValue, "", null, simpleProps, editedFields.contains("field_urlname"));
                    return;
                }
                final String urlNameValue = getAndRemoveValue(fieldValues, FIELD_URLNAME);
                fieldValues.remove(FIELD_LINK);
                final CmsLinkBean link = m_linkSelector.getLinkBean();
                CmsClientSitemapEntry.setRedirect(fieldValues, link);
                simpleProps = convertProperties(fieldValues);
                m_handler.handleSubmit(
                    titleValue,
                    urlNameValue,
                    null,
                    simpleProps,
                    editedFields.contains(FIELD_URLNAME));
            }
        });
    }

    /**
     * Adds a form field for a given property.<p>
     * 
     * @param propDef the property definition 
     * @param entry the entry being edited 
     */
    public void addFieldForProperty(CmsXmlContentProperty propDef, CmsClientSitemapEntry entry) {

        String name = propDef.getPropertyName();

        String selectInherit = propDef.getSelectInherit();
        Map<String, CmsComputedPropertyValue> parentProps = entry.getParentInheritedProperties();
        CmsComputedPropertyValue parentProp = parentProps.get(propDef.getPropertyName());

        if (entry.isLeafType() || ((selectInherit != null) && !Boolean.parseBoolean(selectInherit))) {
            I_CmsFormField f1 = createField(propDef);
            f1.getWidget().setFormValueAsString(entry.getOwnProperty(name));
            f1.setId("#" + name);
            m_form.addField(f1);

        } else {
            Map<String, CmsSimplePropertyValue> props = entry.getProperties();
            CmsSimplePropertyValue prop = props.get(name);
            if (prop == null) {
                prop = new CmsSimplePropertyValue(null, null);
            }
            I_CmsFormField f1 = createField(propDef);
            f1.getWidget().setFormValueAsString(prop.getInheritValue());
            f1.setId("#" + name);
            I_CmsFormField f2 = createField(propDef);
            f2.getWidget().setFormValueAsString(prop.getOwnValue());
            CmsPair<CmsFormRow, CmsFormRow> rows = m_form.addDoubleField(f1, f2);

            if (parentProp != null) {
                setGhostValue(f1, parentProp.getInheritValue(), prop.getInheritValue() == null);
            }

            CmsFormRow row1 = rows.getFirst();

            CmsComputedPropertyValue propValue = entry.getParentInheritedProperties().get(name);
            if (propValue != null) {
                CmsSourcedValue sourcedProp = propValue.getInheritSourcedValue();
                if (sourcedProp != null) {
                    String val = sourcedProp.getValue();
                    String src = sourcedProp.getSource();
                    String message = Messages.get().key(Messages.GUI_INHERIT_PROPERTY_2, "" + val, "" + src);
                    row1.getOpener().setTitle(message);
                }
            }
        }
    }

    /**
     * Shows the sitemap entry editor to the user.<p>
     */
    public void start() {

        CmsForm form = getForm();

        form.addLabel(m_handler.getDescriptionText(), false);

        if (m_handler.hasEditableName()) {
            // the root entry name can't be edited 
            CmsBasicFormField urlNameField = createUrlNameField();
            form.addField(urlNameField);
        }

        CmsBasicFormField titleField = createTitleField();
        form.addField(titleField);

        CmsClientSitemapEntry entry = m_handler.getEntry();
        boolean isRedirect = entry.getProperties().containsKey(CmsSitemapManager.Property.isRedirect.getName());
        if (isRedirect) {
            CmsBasicFormField linkField = createLinkField();
            form.addField(linkField);
        }
        Collection<CmsXmlContentProperty> propertyDefs = m_propertyConfig.values();
        for (CmsXmlContentProperty propDef : propertyDefs) {
            if (propDef.getPropertyName().equals(CmsSitemapManager.Property.template.getName()) && isRedirect) {
                continue;
            }
            addFieldForProperty(propDef, entry);
        }

        form.setValidatorClass("org.opencms.ade.sitemap.CmsSitemapFormValidator");
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
     * Helper method for converting a map of strings to a map of {@link CmsSimplePropertyValue} instances.<p>
     * 
     * @param strProps the properties as a map of strings 
     * @return a map from strings to {@link CmsSimplePropertyValue} instances 
     */
    protected Map<String, CmsSimplePropertyValue> convertProperties(Map<String, String> strProps) {

        Map<String, CmsSimplePropertyValue> result = new LinkedHashMap<String, CmsSimplePropertyValue>();

        Set<String> baseNames = new LinkedHashSet<String>();
        for (String key : strProps.keySet()) {
            if (key.startsWith("#")) {
                baseNames.add(key.substring(1));
            } else {
                baseNames.add(key);
            }
        }
        for (String key : baseNames) {
            String own = strProps.get(key);
            String inherit = strProps.get("#" + key);
            if ((own == null) && (inherit == null)) {
                result.put(key, null);
                continue;
            }
            if (own == null) {
                own = inherit;
            }
            CmsSimplePropertyValue prop = new CmsSimplePropertyValue(own, inherit);
            result.put(key, prop);
        }
        return result;

    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#createForm()
     */
    @Override
    protected CmsForm createForm() {

        return new CmsForm() {

            @Override
            protected String createValidatorConfig() {

                List<String> forbiddenNames = m_handler.getForbiddenUrlNames();
                String forbiddenNamesStr = CmsStringUtil.listAsString(forbiddenNames, "#");
                CmsClientSitemapEntry entry = m_handler.getEntry();
                boolean isNew = entry.isNew() && !getEditedFields().contains("field_urlname");
                String config = "new:" + isNew;
                config += "|forbidden:" + forbiddenNamesStr;

                return config;
            }
        };
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
     * Sets the ghost value for a form field if its normal value is empty and the field's widget supports ghost values.<p>
     *  
     * @param field the form field 
     * @param value the ghost value to set
     * @param ghostMode if true, sets the widget to ghost mode  
     */
    protected void setGhostValue(I_CmsFormField field, String value, boolean ghostMode) {

        I_CmsFormWidget widget = field.getWidget();
        if ((widget instanceof I_CmsHasGhostValue) && (value != null)) {
            ((I_CmsHasGhostValue)widget).setGhostValue(value, ghostMode);
        }
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
     * Helper method for creating a form field from a property definition.<p>
     * 
     * @param propDef the property definition for which the field should be created 
     * @return a new field 
     */
    private I_CmsFormField createField(CmsXmlContentProperty propDef) {

        if (propDef.getPropertyName().equals("template")) {
            return createTemplateField();
        }
        return CmsBasicFormField.createField(propDef);
    }

    /**
     * Creates the field for editing the redirect target.<p>
     *  
     * @return the new field 
     */
    private CmsBasicFormField createLinkField() {

        CmsClientSitemapEntry entry = m_handler.getEntry();

        String description = Messages.get().key(Messages.GUI_REDIRECTION_FIELD_DESCRIPTION_0);
        String label = Messages.get().key(Messages.GUI_REDIRECTION_FIELD_LABEL_0);
        m_linkSelector.setLinkBean(entry.getRedirectInfo());
        CmsBasicFormField result = new CmsBasicFormField(FIELD_LINK, description, label, null, m_linkSelector, false);
        return result;
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
        return new CmsBasicFormField(FIELD_TEMPLATE, description, label, null, select, false);
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

        CmsBasicFormField result = new CmsBasicFormField(FIELD_TITLE, description, label, null, new CmsTextBox(), false);
        String title = m_handler.getTitle();
        if (title == null) {
            title = "";
        }
        result.getWidget().setFormValueAsString(title);
        //result.setValidator(new CmsNonEmptyValidator(Messages.get().key(Messages.GUI_TITLE_CANT_BE_EMPTY_0)));
        result.setValidator(new CmsTitleValidator());

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

        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, textbox, false);
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

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : propConfig.entrySet()) {
            if (!m_controller.isHiddenProperty(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

}
