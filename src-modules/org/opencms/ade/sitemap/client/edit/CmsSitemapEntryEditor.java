/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2011/02/14 16:24:21 $
 * Version: $Revision: 1.20 $
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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsPropertyModification;
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
import org.opencms.gwt.client.ui.input.form.CmsSimpleFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsTabbedFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.util.CmsPair;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.20 $
 *  
 *  @since 8.0.0
 */
public class CmsSitemapEntryEditor implements I_CmsFormWidgetMultiFactory {

    /** First tab id. */
    public static final String TAB_1 = "TAB1";

    /** Second tab id. */
    public static final String TAB_2 = "TAB2";

    /** Third tab id. */
    public static final String TAB_3 = "TAB3";

    /** The key for the default template. */
    private static final String DEFAULT_TEMPLATE_VALUE = "";

    /** The field id for the link selector widget. */
    private static final String FIELD_LINK = "field_link";

    /** The field id of the 'template-inherited' property. */
    private static final String FIELD_TEMPLATE_INHERIT_CHECKBOX = "field_template_inherited";

    /** The field id of the "url name" form field. */
    private static final String FIELD_URLNAME = "field_urlname";

    /** The sitemap controller which changes the actual entry data when the user clicks OK in this dialog. */
    protected CmsSitemapController m_controller;

    /** The form dialog. */
    protected CmsFormDialog m_dialog;

    /** The handler for this sitemap entry editor. */
    protected I_CmsSitemapEntryEditorHandler m_handler;

    /** The link selector widget. */
    protected CmsLinkSelector m_linkSelector = new CmsLinkSelector();

    /** The configuration of the properties. */
    protected Map<String, CmsXmlContentProperty> m_propertyConfig;

    /**
     * Creates a new sitemap entry editor.<p>
     * 
     * @param handler the handler
     */
    public CmsSitemapEntryEditor(I_CmsSitemapEntryEditorHandler handler) {

        CmsForm form = new CmsForm(null);
        m_dialog = new CmsFormDialog(handler.getDialogTitle(), form);

        m_controller = handler.getController();
        m_propertyConfig = removeHiddenProperties(m_controller.getData().getProperties());
        m_handler = handler;
        m_dialog.setFormHandler(new I_CmsFormHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map, java.util.Set)
             */
            public void onSubmitForm(Map<String, String> fieldValues, Set<String> editedFields) {

                Map<String, String> changedFieldValues = new HashMap<String, String>(fieldValues);
                changedFieldValues.keySet().retainAll(editedFields);
                List<CmsPropertyModification> propChanges = getPropertyChanges(changedFieldValues);

                String titleValue = getTitle(fieldValues);
                if (titleValue == null) {
                    titleValue = m_handler.getEntry().getTitle();
                }
                if (!m_handler.hasEditableName()) {
                    // The root element's name can't be edited 
                    m_dialog.hide();
                    m_handler.handleSubmit(titleValue, "", null, propChanges, editedFields.contains(FIELD_URLNAME));
                    return;
                }
                final String urlNameValue = getAndRemoveValue(fieldValues, FIELD_URLNAME);
                fieldValues.remove(FIELD_LINK);
                //final CmsLinkBean link = m_linkSelector.getLinkBean();
                //CmsClientSitemapEntry.setRedirect(fieldValues, link);
                m_handler.handleSubmit(
                    titleValue,
                    urlNameValue,
                    null,
                    propChanges,
                    editedFields.contains(FIELD_URLNAME));
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory#createFormWidget(java.lang.String, java.util.Map)
     */
    public I_CmsFormWidget createFormWidget(String key, Map<String, String> widgetParams) {

        if ("template".equals(key)) {
            return createTemplateSelector(m_controller.getData().getTemplates());
        } else {

            return CmsWidgetFactoryRegistry.instance().createFormWidget(key, widgetParams);
        }

    }

    /**
     * Shows the sitemap entry editor to the user.<p>
     */
    public void start() {

        CmsForm form = m_dialog.getForm();
        form.addLabel(m_handler.getDescriptionText(), false);
        if (m_handler.isSimpleMode()) {
            form.setWidget(new CmsSimpleFormFieldPanel());
        } else {
            CmsTabbedFormFieldPanel tabs = new CmsTabbedFormFieldPanel();
            tabs.addTab(TAB_1, "Simple");
            tabs.addTab(TAB_2, "Structure");
            tabs.addTab(TAB_3, "Resource");
            form.setWidget(tabs);
        }

        if (m_handler.hasEditableName()) {
            // the root entry name can't be edited 
            CmsBasicFormField urlNameField = createUrlNameField();
            form.addField(TAB_1, urlNameField);
        }

        //        CmsBasicFormField titleField = createTitleField();
        //        form.addField(TAB_1, titleField);

        CmsClientSitemapEntry entry = m_handler.getEntry();
        boolean isRedirect = false; // entry.getProperties().containsKey(CmsSitemapManager.Property.isRedirect.getName());
        if (isRedirect) {
            CmsBasicFormField linkField = createLinkField();
            form.addField(TAB_1, linkField);
        }

        A_CmsPropertyFormBuilder builder = createFormBuilder();
        builder.setWidgetFactory(this);

        builder.setForm(form);
        builder.setPropertyDefinitions(CmsSitemapView.getInstance().getController().getData().getProperties());
        builder.setAllPropertyNames(CmsSitemapView.getInstance().getController().getData().getAllPropertyNames());
        builder.buildFields(entry);

        //        if (m_handler.isSimpleMode()) {
        //            builder.buildSimpleFields(entry);
        //        } else {
        //            builder.buildAdvancedFields(entry);
        //        }
        //        Collection<CmsXmlContentProperty> propertyDefs = m_propertyConfig.values();
        //        for (CmsXmlContentProperty propDef : propertyDefs) {
        //            if (propDef.getPropertyName().equals(CmsSitemapManager.Property.template.getName()) && isRedirect) {
        //                continue;
        //            }
        //            addFieldForProperty(propDef, entry);
        //        }

        //form.setValidatorClass("org.opencms.ade.sitemap.CmsSitemapFormValidator");
        form.setValidatorClass("org.opencms.gwt.CmsDefaultFormValidator");

        m_dialog.center();
    }

    /**
     * Starts the sitemap entry editor and validates all form fields.<p>
     */
    public void startAndValidate() {

        start();
        m_dialog.getForm().doInitialValidation();
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
     * Converts a map of field values to a list of property changes.<p>
     * 
     * @param fieldValues the field values 
     * @return the property changes
     */
    protected List<CmsPropertyModification> getPropertyChanges(Map<String, String> fieldValues) {

        List<CmsPropertyModification> result = new ArrayList<CmsPropertyModification>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("/")) {
                CmsPropertyModification propChange = new CmsPropertyModification(key, value);
                result.add(propChange);
            }
        }
        return result;
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

    //    protected CmsForm createForm1() {
    //
    //        CmsForm result = new CmsForm() {
    //
    //            private String m_validatorConfig;
    //
    //            public void setValidatorConfig(String validatorConfig) {
    //
    //                m_validatorConfig = validatorConfig;
    //            }
    //
    //            @Override
    //            protected String createValidatorConfig() {
    //
    //                CmsClientSitemapEntry entry = m_handler.getEntry();
    //                return entry.getId().toString();
    //            }
    //        };
    //        List<String> forbiddenNames = m_handler.getForbiddenUrlNames();
    //        String forbiddenNamesStr = CmsStringUtil.listAsString(forbiddenNames, "#");
    //        CmsClientSitemapEntry entry = m_handler.getEntry();
    //        boolean isNew = entry.isNew() && !getEditedFields().contains("field_urlname");
    //        String config = "new:" + isNew;
    //        config += "|forbidden:" + forbiddenNamesStr;
    //
    //    }

    /**
     * Gets the title from a map of field values.<p>
     * 
     * @param fieldValues the map of field values 
     * @return the title 
     */
    protected String getTitle(Map<String, String> fieldValues) {

        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            if (entry.getKey().contains("/NavText/")) {
                return entry.getValue();
            }
        }
        return null;
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

        m_dialog.getForm().getField(FIELD_URLNAME).getWidget().setFormValueAsString(urlName);
    }

    /**
     * Shows an error message next to the URL name input field.<p>
     * 
     * @param message the message which should be displayed, or null if no message should be displayed 
     */
    protected void showUrlNameError(String message) {

        m_dialog.getForm().getField(FIELD_URLNAME).getWidget().setErrorMessage(message);
    }

    /**
     * Creates the form builder instance.<p>
     * 
     * @return the form builder to use
     **/
    private A_CmsPropertyFormBuilder createFormBuilder() {

        return new CmsNavModePropertyFormBuilder();
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
        CmsBasicFormField result = new CmsBasicFormField(FIELD_LINK, description, label, null, m_linkSelector);
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
        CmsSitemapTemplate defaultTemplate = getDefaultTemplate();
        if (defaultTemplate != null) {
            CmsTemplateSelectCell defaultCell = new CmsTemplateSelectCell();
            defaultCell.setTemplate(defaultTemplate);
            result.addOption(defaultCell);
        }
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
        if (template != null) {
            // replace site path with empty string and title with "default" 
            CmsSitemapTemplate result = new CmsSitemapTemplate(
                template.getTitle(),
                template.getDescription(),
                DEFAULT_TEMPLATE_VALUE,
                template.getImgPath());
            result.setShowWeakText(true);
            return result;
        }
        return null;

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
