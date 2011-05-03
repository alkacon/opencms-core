/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/A_CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2011/05/03 10:49:11 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapController.ReloadMode;
import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectBox;
import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectCell;
import org.opencms.ade.sitemap.shared.CmsClientProperty;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsPropertyModification;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.gwt.client.ui.input.CmsDefaultStringModel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.3 $
 *  
 *  @since 8.0.0
 */
public abstract class A_CmsSitemapEntryEditor implements I_CmsFormWidgetMultiFactory {

    /** The field id for the link selector widget. */
    private static final String FIELD_LINK = "field_link";

    /** The field id of the "url name" form field. */
    private static final String FIELD_URLNAME = "field_urlname";

    /** The list of all property names. */
    protected List<String> m_allProps;

    /** The sitemap controller which changes the actual entry data when the user clicks OK in this dialog. */
    protected CmsSitemapController m_controller;

    /** The form dialog. */
    protected CmsFormDialog m_dialog;

    /** The sitemap entry being edited. */
    protected CmsClientSitemapEntry m_entry;

    /** The form containing the fields. */
    protected CmsForm m_form;

    /** The handler for this sitemap entry editor. */
    protected I_CmsSitemapEntryEditorHandler m_handler;

    /** The configuration of the properties. */
    protected Map<String, CmsXmlContentProperty> m_propertyConfig;

    /** The URL name field. */
    protected I_CmsFormField m_urlNameField;

    /** The model for the URL name field. */
    protected CmsDefaultStringModel m_urlNameModel;

    /**
     * Creates a new sitemap entry editor.<p>
     * 
     * @param handler the handler
     */
    public A_CmsSitemapEntryEditor(final I_CmsSitemapEntryEditorHandler handler) {

        CmsForm form = new CmsForm(null);
        m_form = form;
        m_dialog = new CmsFormDialog(handler.getDialogTitle(), form);

        m_controller = handler.getController();
        m_propertyConfig = removeHiddenProperties(m_controller.getData().getProperties());
        m_handler = handler;
        m_dialog.setFormHandler(new I_CmsFormHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map, java.util.Set)
             */
            public void onSubmitForm(Map<String, String> fieldValues, Set<String> editedFields) {

                ReloadMode reloadMode = getReloadMode(fieldValues, editedFields);
                Map<String, String> changedPropValues = removeTabSuffixes(fieldValues);
                Set<String> editedModels = removeTabSuffixes(editedFields);
                changedPropValues.keySet().retainAll(editedModels);
                List<CmsPropertyModification> propChanges = getPropertyChanges(changedPropValues);
                if (!m_handler.hasEditableName()) {
                    // The root element's name can't be edited 
                    m_dialog.hide();
                    m_handler.handleSubmit("", null, propChanges, editedFields.contains(FIELD_URLNAME), reloadMode);
                    return;
                }
                final String urlNameValue = getAndRemoveValue(fieldValues, FIELD_URLNAME);
                fieldValues.remove(FIELD_LINK);
                //final CmsLinkBean link = m_linkSelector.getLinkBean();
                //CmsClientSitemapEntry.setRedirect(fieldValues, link);
                m_handler.handleSubmit(
                    urlNameValue,
                    null,
                    propChanges,
                    editedFields.contains(FIELD_URLNAME),
                    reloadMode);
            }

            /**
             * Check if a field name belongs to one of a given list of properties.<p>
             * 
             * @param fieldName the field name 
             * @param propNames the property names 
             * 
             * @return true if the field name matches one of the property names 
             */
            private boolean checkContains(String fieldName, String... propNames) {

                for (String propName : propNames) {
                    if (fieldName.contains("/" + propName + "/")) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * Returns the reload mode to use for the given changes.<p>
             * 
             * @param fieldValues the field values 
             * @param editedFields the set of edited fields
             * 
             * @return the reload mode 
             */
            private CmsSitemapController.ReloadMode getReloadMode(
                Map<String, String> fieldValues,
                Set<String> editedFields) {

                if (CmsSitemapView.getInstance().isNavigationMode()) {
                    return ReloadMode.none;
                }
                for (String fieldName : editedFields) {
                    if (checkContains(
                        fieldName,
                        CmsClientProperty.PROPERTY_DEFAULTFILE,
                        CmsClientProperty.PROPERTY_NAVPOS)) {
                        return ReloadMode.reloadParent;

                    }
                    if (checkContains(fieldName, CmsClientProperty.PROPERTY_NAVTEXT)) {
                        return ReloadMode.reloadEntry;
                    }
                }
                return ReloadMode.none;
            }

        });
    }

    /**
     * Checks whether a widget can be used in the sitemap entry editor, and throws an exception otherwise.<p>
     * 
     * @param key the widget key 
     * @param widget the created widget 
     */
    public static void checkWidgetRequirements(String key, I_CmsFormWidget widget) {

        if (!((widget instanceof I_CmsHasGhostValue) && (widget instanceof HasValueChangeHandlers<?>))) {
            throw new CmsWidgetNotSupportedException(key);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory#createFormWidget(java.lang.String, java.util.Map)
     */
    public I_CmsFormWidget createFormWidget(String key, Map<String, String> widgetParams) {

        I_CmsFormWidget result = null;

        if ("template".equals(key)) {
            result = createTemplateSelector(m_controller.getData().getTemplates());
        } else if (CmsTextBox.WIDGET_TYPE.equals(key)) {
            CmsTextBox textBox = new CmsTextBox();
            textBox.setErrorMessageWidth("345px");
            // we need this because the tab containing the text box may not be visible 
            // at the time the error message is set, so measuring the field's size would
            // yield an invalid value  
            result = textBox;
        } else {
            result = CmsWidgetFactoryRegistry.instance().createFormWidget(key, widgetParams);
            checkWidgetRequirements(key, result);
        }
        return result;
    }

    /**
     * Shows the sitemap entry editor to the user.<p>
     */
    public void start() {

        CmsForm form = m_dialog.getForm();

        // creates tabs, etc. if necessary 
        setupFieldContainer();

        String firstTab = form.getWidget().getDefaultGroup();

        if (m_handler.hasEditableName()) {
            // the root entry name can't be edited 
            CmsBasicFormField urlNameField = createUrlNameField();
            form.addField(firstTab, urlNameField);
        }

        CmsClientSitemapEntry entry = m_handler.getEntry();

        CmsSitemapData data = CmsSitemapView.getInstance().getController().getData();
        m_allProps = data.getAllPropertyNames();
        m_entry = entry;

        // create fields and add them to the correct location 
        buildFields();
        form.setValidatorClass("org.opencms.gwt.CmsDefaultFormValidator");
        form.render();
        m_dialog.center();
    }

    /**
     * Starts the sitemap entry editor and validates all form fields.<p>
     */
    public void startAndValidate() {

        start();
        m_dialog.getForm().validateAllFields();
    }

    /**
     * Builds and renders the fields for the properties.<p>
     */
    protected abstract void buildFields();

    /**
     * Creates the text field for editing the URL name.<p>
     * 
     * @return the newly created form field 
      */
    protected CmsBasicFormField createUrlNameField() {

        if (m_urlNameField != null) {
            m_urlNameField.unbind();
        }

        String description = message(Messages.GUI_URLNAME_PROPERTY_DESC_0);
        String label = message(Messages.GUI_URLNAME_PROPERTY_0);
        final CmsTextBox textbox = new CmsTextBox();

        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, textbox);
        String urlName = m_handler.getName();
        if (urlName == null) {
            urlName = "";
        }
        List<String> forbiddenUrlNames = m_handler.getForbiddenUrlNames();
        result.setValidator(new CmsUrlNameValidator(forbiddenUrlNames));
        I_CmsStringModel model = getUrlNameModel(urlName);
        result.getWidget().setFormValueAsString(model.getValue());
        result.bind(model);
        //result.getWidget().setFormValueAsString(getUrlNameModel().getValue());
        m_urlNameField = result;
        return result;
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
     * Lazily creates the model object for the URL name field.<p>
     * 
     * @param urlName the initial value for the URL name 
     * 
     * @return the model object for the URL name field 
     */
    protected CmsDefaultStringModel getUrlNameModel(String urlName) {

        if (m_urlNameModel == null) {
            m_urlNameModel = new CmsDefaultStringModel("urlname");
            m_urlNameModel.setValue(urlName, false);
        }
        return m_urlNameModel;
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
     * Removes the tab suffix from a field id.<p>
     * 
     * @param fieldId a field id 
     * 
     * @return the field id without the suffix 
     */
    protected String removeTabSuffix(String fieldId) {

        return fieldId.replaceAll("#.*$", "");
    }

    /**
     * Removes the tab suffixes from each field id of a collection.<p> 
     * 
     * @param fieldIds the field ids from which to remove the tab suffix
     *   
     * @return a new collection of field ids without tab suffixes 
     */
    protected Set<String> removeTabSuffixes(Collection<String> fieldIds) {

        Set<String> result = new HashSet<String>();
        for (String fieldId : fieldIds) {
            result.add(removeTabSuffix(fieldId));
        }
        return result;
    }

    /**
     * Removes the tab suffixes from the keys of a map.<p>
     * 
     * @param fieldValues a map of field values 
     * 
     * @return a new map of field values, with tab suffixes removed from the keys
     */
    protected Map<String, String> removeTabSuffixes(Map<String, String> fieldValues) {

        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String newKey = removeTabSuffix(key);
            result.put(newKey, entry.getValue());
        }
        return result;
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
     * Sets up the widget which will contain the input fields for the properties.<p>
     */
    protected abstract void setupFieldContainer();

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
        return result;
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
