/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2010/05/26 14:44:10 $
 * Version: $Revision: 1.12 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectBox;
import org.opencms.ade.sitemap.client.ui.CmsTemplateSelectCell;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsNonEmptyValidator;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsValidationHandler;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.12 $
 *  
 *  @since 8.0.0
 */
public class CmsSitemapEntryEditor extends CmsFormDialog {

    /**
     * The editor mode.<p>
     */
    public enum Mode {

        /** Edition of an existing entry. */
        EDIT,

        /** Creation of a new entry. */
        NEW;
    }

    /** The key for the default template. */
    public static final String DEFAULT_TEMPLATE_VALUE = "";

    /** The field id of the "title" form field. */
    public static final String FIELD_TITLE = "field_title";

    /** The field id of the "url name" form field. */
    public static final String FIELD_URLNAME = "field_urlname";

    /** The field id of the 'template' property. */
    private static final String FIELD_TEMPLATE = "template";

    /** The field id of the 'template-inherited' property. */
    private static final String FIELD_TEMPLATE_INHERIT_CHECKBOX = "field_template_inherited";

    /** The sitemap controller which changes the actual entry data when the user clicks OK in this dialog. */
    protected CmsSitemapController m_controller;

    /** The sitemap entry which is being edited. */
    protected CmsClientSitemapEntry m_entry;

    /** The current mode. */
    protected Mode m_mode;

    /** The mode handler for this sitemap entry editor. */
    protected I_CmsSitemapEntryEditorModeHandler m_modeHandler;

    /** The configuration of the properties. */
    private Map<String, CmsXmlContentProperty> m_propertyConfig;

    /**
     * Creates a new sitemap entry editor.<p>
     * 
     * @param controller the controller which should be used to update the edited sitemap entry 
     * @param sitePath the site path of the entry which should be edited
     * @param mode the edition mode
     */
    public CmsSitemapEntryEditor(CmsSitemapController controller, String sitePath, Mode mode) {

        super(message(Messages.GUI_PROPERTY_EDITOR_TITLE_0));

        m_entry = controller.getEntry(sitePath);
        m_controller = controller;
        m_propertyConfig = removeHiddenProperties(controller.getData().getProperties());
        m_mode = mode;
        m_modeHandler = createModeHandler(m_mode, controller, m_entry);
    }

    /**
     * Helper method which retrieves a value for a given key from a map and then deletes the entry for the key.<p>
     * 
     * @param map the map from which to retrieve the value 
     * @param key the key
     * 
     * @return the removed value 
     */
    protected static String getAndRemoveValue(Map<String, String> map, String key) {

        String value = map.get(key);
        if (value != null) {
            map.remove(key);
        }
        return value;
    }

    /**
     * Returns a localized message from the message bundle.<p>
     * 
     * @param key the message key
     * @param args the message parameters
     *  
     * @return the localized message 
     */
    protected static String message(String key, Object... args) {

        return Messages.get().key(key, args);
    }

    /**
     * Helper method for creating new mode handlers.<p>
     * 
     * @param mode the mode for which to create a mode handler
     * @param controller a sitemap controller for the mode handler
     * @param entry the sitemap entry for the mode handler 
     * 
     * @return the newly created mode handler 
     */
    private static I_CmsSitemapEntryEditorModeHandler createModeHandler(
        Mode mode,
        CmsSitemapController controller,
        CmsClientSitemapEntry entry) {

        if (mode == Mode.EDIT) {
            return new CmsEditEntryMode(controller, entry);
        } else {
            return new CmsNewEntryMode(controller, entry);
        }
    }

    /**
     * Helper method for extracting new values for the 'template' and 'template-inherited' properties from the
     * raw form data.<p>
     * 
     * @param fieldValues the string map produced by the form 
     * 
     * @return a pair containing the 'template' and 'template-inherit' property, in that order
     */
    private static CmsPair<String, String> getTemplateProperties(Map<String, String> fieldValues) {

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
     * Checks whether a given sitemap entry has sibling entries with a given URL name.<p>
     * 
     * @param entry the entry which should be checked 
     * @param urlNameValue the url name value
     * @return true if the url name value occurs in siblings of the sitemap entry which was passed in 
     */
    public boolean hasSiblingEntriesWithName(CmsClientSitemapEntry entry, String urlNameValue) {

        String parentPath = CmsResource.getParentFolder(entry.getSitePath());
        CmsClientSitemapEntry parentEntry = m_controller.getEntry(parentPath);
        for (CmsClientSitemapEntry siblingEntry : parentEntry.getSubEntries()) {
            if ((siblingEntry != entry) && urlNameValue.equals(siblingEntry.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shows the sitemap entry editor to the user.<p>
     */
    public void start() {

        CmsForm form = getForm();

        form.addLabel(Messages.get().key(Messages.GUI_PROPERTY_EDITOR_TEXT_0));

        CmsBasicFormField urlNameField = createUrlNameField(m_entry);
        form.addField(urlNameField);

        CmsBasicFormField titleField = createTitleField(m_entry);
        form.addField(titleField);

        Map<String, String> properties = m_entry.getProperties();
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
     * Handles a form submit after the normal fields have already been validated successfully.<p>
     */
    protected void handleSubmit() {

        final Map<String, String> fieldValues = getForm().collectValues();
        final String titleValue = getAndRemoveValue(fieldValues, FIELD_TITLE);
        final String urlNameValue = getAndRemoveValue(fieldValues, FIELD_URLNAME);

        CmsPair<String, String> templateProps = getTemplateProperties(fieldValues);
        fieldValues.put(CmsSitemapManager.Property.template.toString(), templateProps.getFirst());
        fieldValues.put(CmsSitemapManager.Property.templateInherited.toString(), templateProps.getSecond());

        validateUrlName(urlNameValue, new AsyncCallback<String>() {

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
             */
            public void onFailure(Throwable caught) {

                // do nothing, this will never be called 
            }

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object) 
             */
            public void onSuccess(String newUrlName) {

                hide();
                m_modeHandler.handleSubmit(titleValue, newUrlName, null, fieldValues);
            }

        });
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickOk()
     */
    @Override
    protected void onClickOk() {

        m_form.validate(new I_CmsValidationHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.I_CmsValidationHandler#onValidationComplete(boolean)
             */
            public void onValidationComplete(boolean validationSucceeded) {

                if (validationSucceeded) {
                    handleSubmit();
                }
            }
        });
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
     * Validates the url name, and if it is ok, executes another action and passes the translated url
     * name to it.<p>
     * 
     * @param urlName the url name to validate 
     * @param nextAction the action which should be executed if the validation turns out ok
     */
    protected void validateUrlName(String urlName, final AsyncCallback<String> nextAction) {

        CmsCoreProvider.get().translateUrlName(urlName, new AsyncCallback<String>() {

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
             */
            public void onFailure(Throwable caught) {

                // will never be executed; do nothing 
            }

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
             */
            public void onSuccess(String newUrlName) {

                setUrlNameField(newUrlName);
                String newPath = m_modeHandler.createPath(newUrlName);
                if (!m_modeHandler.isPathAllowed(newPath)) {
                    showUrlNameError(message(Messages.GUI_URLNAME_ALREADY_EXISTS_0));
                } else if (nextAction != null) {
                    nextAction.onSuccess(newUrlName);
                }
            }
        });

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
     * @param entry the entry which is being edited
     *  
     * @return the newly created form field 
     */
    private CmsBasicFormField createTitleField(CmsClientSitemapEntry entry) {

        String description = message(Messages.GUI_TITLE_PROPERTY_DESC_0);
        String label = message(Messages.GUI_TITLE_PROPERTY_0);

        CmsBasicFormField result = new CmsBasicFormField(FIELD_TITLE, description, label, null, new CmsTextBox());
        String title = m_modeHandler.getTitle();
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
     * @param entry the entry which is being edited
     *  
     * @return the newly created form field 
     */
    private CmsBasicFormField createUrlNameField(CmsClientSitemapEntry entry) {

        String description = message(Messages.GUI_URLNAME_PROPERTY_DESC_0);
        String label = message(Messages.GUI_URLNAME_PROPERTY_0);
        final CmsTextBox textbox = new CmsTextBox();
        textbox.addBlurHandler(new BlurHandler() {

            /**
             * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
             */
            public void onBlur(BlurEvent event) {

                validateUrlName(textbox.getText(), null);
            }
        });
        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, textbox);
        String urlName = m_modeHandler.getName();
        if (urlName == null) {
            urlName = "";
        }
        result.getWidget().setFormValueAsString(urlName);
        result.setValidator(new CmsNonEmptyValidator(message(Messages.GUI_URLNAME_CANT_BE_EMPTY_0)));
        return result;
    }

    /**
     * Returns the template which should be used as the "use default" option in the template selector.<p>
     * 
     * @return the default template 
     */
    private CmsSitemapTemplate getDefaultTemplate() {

        CmsSitemapTemplate template = m_controller.getDefaultTemplate(m_entry.getSitePath());
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
