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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Locale;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

/**
 * Dialog to edit the user data.<p>
 */
public class CmsUserDataDialog extends CmsBasicDialog implements I_CmsHasTitle {

    /** The serial version id. */
    private static final long serialVersionUID = 8907786853232656944L;

    /** The embedded dialog id. */
    public static final String DIALOG_ID = "edituserdata";

    /** The field binder. */
    private BeanFieldGroup<CmsUser> m_binder;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The form layout. */
    private FormLayout m_form;

    /** The OK  button. */
    private Button m_okButton;

    /** The edited user. */
    private CmsUser m_user;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsUserDataDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsObject cms = context.getCms();
        m_user = cms.getRequestContext().getCurrentUser();
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        initFields();

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsUserDataDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * @see org.opencms.ui.dialogs.I_CmsHasTitle#getTitle(java.util.Locale)
     */
    public String getTitle(Locale locale) {

        return org.opencms.ui.components.Messages.get().getBundle(locale).key(
            org.opencms.ui.components.Messages.GUI_USER_EDIT_0);
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        try {
            m_binder.commit();
            m_context.getCms().writeUser(m_user);
            m_context.finish(Collections.<CmsUUID> emptyList());
        } catch (Exception e) {
            m_context.error(e);
        }
    }

    /**
     * Builds the text field for the given property.<p>
     *
     * @param label the field label
     * @param name the property name
     *
     * @return the field
     */
    private TextField buildField(String label, String name) {

        TextField field = (TextField)m_binder.buildAndBind(label, name);
        field.setConverter(new CmsNullToEmptyConverter());
        return field;
    }

    /**
     * Initializes the form fields.<p>
     */
    private void initFields() {

        m_binder = new BeanFieldGroup<CmsUser>(CmsUser.class);
        m_binder.setItemDataSource(m_user);

        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_FIRSTNAME_0), "firstname"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_LASTNAME_0), "lastname"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_EMAIL_0), "email"));
        m_form.addComponent(
            buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_INSTITUTION_0), "institution"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_ADDRESS_0), "address"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_ZIPCODE_0), "zipcode"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_CITY_0), "city"));
        m_form.addComponent(buildField(CmsVaadinUtils.getMessageText(Messages.GUI_USER_DATA_COUNTRY_0), "country"));
        // Buffer the form content
        m_binder.setBuffered(true);
    }
}
