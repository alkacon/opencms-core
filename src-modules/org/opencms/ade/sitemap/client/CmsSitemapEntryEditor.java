/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapEntryEditor.java,v $
 * Date   : $Date: 2010/05/07 14:05:48 $
 * Version: $Revision: 1.1 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.CmsNonEmptyValidator;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsValidationHandler;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * A dialog for editing the properties, title, url name and template of a sitemap entry.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.1 $
 *  
 *  @since 8.0.0
 */
public class CmsSitemapEntryEditor extends CmsFormDialog {

    /** The field id of the "title" form field. */
    public static final String FIELD_TITLE = "field_title";

    /** The field id of the "url name" form field. */
    public static final String FIELD_URLNAME = "field_urlname";

    /** The sitemap controller which changes the actual entry data when the user clicks OK in this dialog. */
    protected CmsSitemapController m_controller;

    /** The sitemap entry which is being edited. */
    protected CmsClientSitemapEntry m_entry;

    /** The service for translating url names. */
    protected I_CmsSitemapServiceAsync m_service;

    /** The configuration of the properties. */
    private Map<String, CmsXmlContentProperty> m_propertyConfig;

    /**
     * Creates a new sitemap entry editor.<p>
     * 
     * @param controller the controller which should be used to update the edited sitemap entry 
     * @param entry the entry which should be edited
     * @param service the sitemap service which should be used for URL name translation  
     * @param propertyConfig the configuration of the properties to edit 
     */
    public CmsSitemapEntryEditor(CmsSitemapController controller,

    CmsClientSitemapEntry entry, I_CmsSitemapServiceAsync service, Map<String, CmsXmlContentProperty> propertyConfig) {

        super(Messages.get().key(Messages.GUI_PROPERTY_EDITOR_TITLE_0));
        m_entry = entry;
        m_controller = controller;
        m_propertyConfig = removeHiddenProperties(propertyConfig);
        m_service = service;
    }

    /**
     * Helper method for removing hidden properties from a map of property configurations.<p>
     * 
     * The map passed into the method is not changed; a map which only contains the non-hidden
     * property definitions is returned.
     * 
     * @param propConfig the property configuration 
     * 
     * @return the filtered property configuration 
     */
    private static Map<String, CmsXmlContentProperty> removeHiddenProperties(
        Map<String, CmsXmlContentProperty> propConfig) {

        Map<String, CmsXmlContentProperty> result = new HashMap<String, CmsXmlContentProperty>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : propConfig.entrySet()) {
            if (!CmsSitemapController.isHiddenProperty(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Shows the sitemap entry editor to the user.
     */
    public void start() {

        Map<String, String> properties = m_entry.getProperties();
        CmsForm form = getForm();

        CmsBasicFormField urlNameField = createUrlNameField(m_entry);
        form.addField(urlNameField, m_entry.getName());

        CmsBasicFormField titleField = createTitleField(m_entry);
        form.addField(titleField, m_entry.getTitle());
        form.addSeparator();
        Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(m_propertyConfig.values());
        for (I_CmsFormField field : formFields.values()) {
            String currentValue = properties.get(field.getId());
            form.addField(field, currentValue);
        }
        center();

    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickOk()
     */
    @Override
    protected void onClickOk() {

        m_form.validate(new I_CmsValidationHandler() {

            public void onValidationComplete(boolean validationSucceeded) {

                if (validationSucceeded) {

                    final Map<String, String> fieldValues = getForm().collectValues();
                    final String titleValue = fieldValues.get(FIELD_TITLE);
                    final String urlNameValue = fieldValues.get(FIELD_URLNAME);

                    fieldValues.remove(FIELD_TITLE);
                    fieldValues.remove(FIELD_URLNAME);

                    CmsRpcAction<String> translateAction = new CmsRpcAction<String>() {

                        @Override
                        public void execute() {

                            start(0);
                            m_service.translateUrlName(urlNameValue, this);

                        }

                        @Override
                        protected void onResponse(String newUrlName) {

                            setUrlNameField(newUrlName);
                            stop();

                            if (m_controller.hasSiblingEntriesWithName(m_entry, newUrlName)) {
                                showUrlNameError("the URL name already exists at this level.");
                            } else {
                                hide();
                                m_controller.edit(m_entry, titleValue, null, newUrlName, fieldValues);
                            }

                        }

                    };
                    translateAction.execute();
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
     * Creates the text field for editing the title.<p>
     * 
     * @param entry the entry which is being edited
     *  
     * @return the newly created form field 
     */
    private CmsBasicFormField createTitleField(CmsClientSitemapEntry entry) {

        String description = Messages.get().key(Messages.GUI_TITLE_PROPERTY_DESC_0);
        String label = Messages.get().key(Messages.GUI_TITLE_PROPERTY_0);

        CmsBasicFormField result = new CmsBasicFormField(FIELD_TITLE, description, label, null, new CmsTextBox());
        String title = entry.getTitle();
        if (title == null) {
            title = "";
        }
        result.getWidget().setFormValueAsString(entry.getTitle());
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

        String description = Messages.get().key(Messages.GUI_URLNAME_PROPERTY_DESC_0);
        String label = Messages.get().key(Messages.GUI_URLNAME_PROPERTY_0);

        CmsBasicFormField result = new CmsBasicFormField(FIELD_URLNAME, description, label, null, new CmsTextBox());
        String urlName = entry.getName();
        if (urlName == null) {
            urlName = "";
        }
        result.getWidget().setFormValueAsString(urlName);
        result.setValidator(new CmsNonEmptyValidator(Messages.get().key(Messages.GUI_URLNAME_CANT_BE_EMPTY_0)));
        return result;
    }

}
