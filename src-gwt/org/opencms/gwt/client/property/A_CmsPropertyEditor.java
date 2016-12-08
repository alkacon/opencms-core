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

package org.opencms.gwt.client.property;

import org.opencms.file.CmsResource;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.input.CmsDefaultStringModel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.I_CmsHasGhostValue;
import org.opencms.gwt.client.ui.input.I_CmsStringModel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

/**
 * The abstract base class for dialogs to edit properties.<p>
 *
 *  @since 8.0.0
 */
public abstract class A_CmsPropertyEditor implements I_CmsFormWidgetMultiFactory {

    /** The field id for the link selector widget. */
    public static final String FIELD_LINK = "field_link";

    /** The field id of the "url name" form field. */
    public static final String FIELD_URLNAME = "field_urlname";

    /** The list of all property names. */
    protected List<String> m_allProps;

    /** The reason to disable the form input fields. */
    protected String m_disabledReason;

    /** The form containing the fields. */
    protected CmsForm m_form;

    /** The handler for this sitemap entry editor. */
    protected I_CmsPropertyEditorHandler m_handler;

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
     * @param propertyConfig the property configuration
     */
    public A_CmsPropertyEditor(
        Map<String, CmsXmlContentProperty> propertyConfig,
        final I_CmsPropertyEditorHandler handler) {

        CmsForm form = new CmsForm(null);
        m_form = form;
        m_handler = handler;
        m_propertyConfig = removeHiddenProperties(propertyConfig);

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
            result = createTemplateSelector();
        } else if (CmsTextBox.WIDGET_TYPE.equals(key)) {
            CmsTextBox textBox = new CmsTextBox().colorWhite();
            textBox.setErrorMessageWidth("345px");
            textBox.setTriggerChangeOnKeyPress(true);
            // we need this because the tab containing the text box may not be visible
            // at the time the error message is set, so measuring the field's size would
            // yield an invalid value
            result = textBox;
        } else if ("select".equals(key)) {
            final CmsPropertySelectBox box = new CmsPropertySelectBox(widgetParams);
            result = box;

        } else {
            result = CmsWidgetFactoryRegistry.instance().createFormWidget(key, widgetParams);
            checkWidgetRequirements(key, result);
        }
        return result;
    }

    /**
     * Disables all input to the form.<p>
     *
     * @param disabledReason the reason to display to the user
     */
    public void disableInput(String disabledReason) {

        m_disabledReason = disabledReason;
        for (I_CmsFormField field : m_form.getFields().values()) {
            field.getWidget().setEnabled(false);
        }
        m_urlNameField.getWidget().setEnabled(false);
        CmsNotification.get().send(Type.WARNING, m_disabledReason);
    }

    /**
     * Gets the form for the properties.<p>
     *
     * @return the property form
     */
    public CmsForm getForm() {

        return m_form;
    }

    /**
     * Initializes the widgets for editing the properties.<p>
     *
     * @param dialog the dialog which the property editor is part of
     */
    public void initializeWidgets(CmsPopup dialog) {

        // creates tabs, etc. if necessary
        setupFieldContainer();
        addSpecialFields();
        // create fields and add them to the correct location
        buildFields();
        m_form.setValidatorClass("org.opencms.gwt.CmsDefaultFormValidator");
        m_form.render();
        if ((dialog != null) && (dialog.getWidth() > 12)) {

            getForm().getWidget().truncate("property_editing", dialog.getWidth() - 12);
        }
    }

    /**
     * Sets the names of properties which can be edited.<p>
     *
     * @param propertyNames the property names
     */
    public void setPropertyNames(List<String> propertyNames) {

        m_allProps = propertyNames;
    }

    /**
     * Method to add special, non-property fields.<p>
     */
    protected void addSpecialFields() {

        String firstTab = m_form.getWidget().getDefaultGroup();
        if (m_handler.hasEditableName()) {
            // the root entry name can't be edited
            CmsBasicFormField urlNameField = createUrlNameField();
            m_form.addField(firstTab, urlNameField);
        }
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
        textbox.setTriggerChangeOnKeyPress(true);
        textbox.setInhibitValidationForKeypresses(true);

        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, textbox);
        result.getLayoutData().put("property", A_CmsPropertyEditor.FIELD_URLNAME);
        String urlName = m_handler.getName();
        if (urlName == null) {
            urlName = "";
        }
        String parent = CmsResource.getParentFolder(m_handler.getPath());
        CmsUUID id = m_handler.getId();

        result.setValidator(new CmsUrlNameValidator(parent, id));
        I_CmsStringModel model = getUrlNameModel(urlName);
        result.getWidget().setFormValueAsString(model.getValue());
        result.bind(model);
        //result.getWidget().setFormValueAsString(getUrlNameModel().getValue());
        m_urlNameField = result;
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

        m_form.getField(FIELD_URLNAME).getWidget().setFormValueAsString(urlName);
    }

    /**
     * Shows an error message next to the URL name input field.<p>
     *
     * @param message the message which should be displayed, or null if no message should be displayed
     */
    protected void showUrlNameError(String message) {

        m_form.getField(FIELD_URLNAME).getWidget().setErrorMessage(message);
    }

    /**
     * Helper method for creating the template selection widget.<p>
     *
     * @return the template selector widget
     */
    private I_CmsFormWidget createTemplateSelector() {

        if (m_handler.useAdeTemplates()) {

            CmsSelectBox selectBox = null;
            Map<String, String> values = new LinkedHashMap<String, String>();
            for (Map.Entry<String, CmsClientTemplateBean> templateEntry : m_handler.getPossibleTemplates().entrySet()) {
                CmsClientTemplateBean template = templateEntry.getValue();
                String title = template.getTitle();
                if ((title == null) || (title.length() == 0)) {
                    title = template.getSitePath();
                }
                values.put(template.getSitePath(), title);
            }
            selectBox = new CmsPropertySelectBox(values);
            return selectBox;
        } else {
            CmsTextBox textbox = new CmsTextBox();
            return textbox;
        }
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
            if (!m_handler.isHiddenProperty(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
