/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * A dialog containing a form.<p>
 * 
 * @since 8.0.0
 */
public class CmsFormDialog extends CmsPopup implements I_CmsFormDialog {

    /** The dialog width. */
    public static final int STANDARD_DIALOG_WIDTH = 600;

    /** The widget containing the form fields. */
    protected CmsForm m_form;

    /** The form handler for this dialog. */
    protected I_CmsFormHandler m_formHandler;

    /** The OK button of this dialog. */
    private CmsPushButton m_okButton;

    /** 
     * Constructs a new form dialog with a given title.<p>
     * 
     * @param title the title of the form dialog
     * @param form the form to use  
     */
    public CmsFormDialog(String title, CmsForm form) {

        super(title);
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(true);
        setWidth(STANDARD_DIALOG_WIDTH);
        addButton(createCancelButton());
        m_okButton = createOkButton();
        addButton(m_okButton);
        m_form = form;
        m_form.setFormDialog(this);
    }

    /**
     * Static utility method for opening a property form dialog.<p>
     * 
     * @param propertyConfig the configuration of the properties 
     * @param properties the current values of the properties 
     * @param title the title of the dialog 
     * @param formHandler the form handler which should be used 
     */
    public static void showPropertyDialog(
        Map<String, CmsXmlContentProperty> propertyConfig,
        Map<String, String> properties,
        String title,
        I_CmsFormHandler formHandler) {

        CmsSimpleFormFieldPanel simplePanel = new CmsSimpleFormFieldPanel();
        CmsForm form = new CmsForm(simplePanel);
        CmsFormDialog dialog = new CmsFormDialog(Messages.get().key(Messages.GUI_FORM_PROPERTIES_EDIT_0), form);
        dialog.setFormHandler(formHandler);
        Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(propertyConfig.values());

        for (I_CmsFormField field : formFields.values()) {
            String currentValue = properties.get(field.getId());
            form.addField(field, currentValue);
        }
        form.render();
        dialog.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        initContent();
        super.center();
        notifyWidgetsOfOpen();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormDialog#closeDialog()
     */
    public void closeDialog() {

        hide();
    }

    /**
     * Gets the form of this dialog.<p>
     * 
     * @return the form of this dialog 
     */
    public CmsForm getForm() {

        return m_form;
    }

    /**
     * Returns the 'OK' button.<p>
     * 
     * @return the 'OK' button
     */
    public CmsPushButton getOkButton() {

        return m_okButton;
    }

    /**
     * Sets the form handler for this form dialog.<p>
     * 
     * @param formHandler the new form handler 
     */
    public void setFormHandler(I_CmsFormHandler formHandler) {

        m_form.setFormHandler(formHandler);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormDialog#setOkButtonEnabled(boolean)
     */
    public void setOkButtonEnabled(final boolean enabled) {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                // The event handling of GWT gets confused if we don't execute this as a scheduled command 
                getOkButton().setDown(false);
                getOkButton().setEnabled(enabled);
            }
        });

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        initContent();
        super.show();
        notifyWidgetsOfOpen();
    }

    /**
     * Initializes the form content.<p>
     */
    protected void initContent() {

        setMainContent(m_form.getWidget());
    }

    /**
     * The method which should be called when the user clicks on the OK button of the dialog.<p>
     */
    protected void onClickOk() {

        m_form.validateAndSubmit();
    }

    /**
     * Creates the cancel button.<p>
     * 
     * @return the cancel button
     */
    private CmsPushButton createCancelButton() {

        addDialogClose(null);

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                CmsFormDialog.this.hide();
            }
        });
        return button;
    }

    /**
     * Creates the OK button.<p>
     * 
     * @return the OK button
     */
    private CmsPushButton createOkButton() {

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_OK_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClickOk();
            }
        });
        return button;
    }

    /**
     * Tells all widgets that the dialog has been opened.<p>
     */
    private void notifyWidgetsOfOpen() {

        for (Map.Entry<String, I_CmsFormField> fieldEntry : m_form.getFields().entrySet()) {
            fieldEntry.getValue().getWidget().setAutoHideParent(this);
        }
    }

}
