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

package org.opencms.ui.apps.user;

import org.opencms.main.OpenCms;
import org.opencms.security.CmsDefaultPasswordGenerator;
import org.opencms.security.I_CmsPasswordGenerator;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsButtonFormRow;
import org.opencms.ui.components.OpenCmsTheme;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;

/**
 * Dialog to generate a random password.<p>
 */
public class CmsGeneratePasswordDialog extends CmsBasicDialog {

    /**Vaadin serial id. */
    private static final long serialVersionUID = -7522845215366141986L;

    /**
     * public constructor.<p>
     *
     * @param passwordFetcher fetcher to send result to
     * @param close runnable
     */
    public CmsGeneratePasswordDialog(final I_CmsPasswordFetcher passwordFetcher, final Runnable close) {

        setWidth("500px");
        FormLayout layout = new FormLayout();
        layout.setWidth("100%");
        layout.addStyleName(OpenCmsTheme.FORMLAYOUT_WORKPLACE_MAIN);
        layout.setMargin(true);
        layout.setSpacing(true);
        //        layout.setWidth("400px");
        final TextField passwordField = new TextField();
        passwordField.setValue(getRandomPassword());
        CmsButtonFormRow<TextField> row = new CmsButtonFormRow<TextField>(
            passwordField,
            VaadinIcons.REFRESH,
            new Runnable() {

                public void run() {

                    passwordField.setValue(getRandomPassword());
                }
            },
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GEN_PASSWORD_REFRESH_0));

        Label label = new Label(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GEN_PASSWORD_TEXT_0));
        label.setWidth("100%");
        layout.addComponent(label);
        row.setWidth("100%");
        layout.addComponent(row);
        setContent(layout);
        Button closeButton = new Button(CmsVaadinUtils.messageCancel());
        closeButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -8994698147835117427L;

            public void buttonClick(ClickEvent event) {

                close.run();

            }
        });

        Button okButton = new Button(CmsVaadinUtils.messageOk());
        okButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -1375411076842792728L;

            public void buttonClick(ClickEvent event) {

                passwordFetcher.fetchPassword(passwordField.getValue());
                close.run();
            }
        });
        addButton(okButton);
        addButton(closeButton);
    }

    /**
     * Gets random password from password handler.<p>
     *
     * @return String
     */
    public static String getRandomPassword() {

        if (OpenCms.getPasswordHandler() instanceof I_CmsPasswordGenerator) {
            return ((I_CmsPasswordGenerator)OpenCms.getPasswordHandler()).getRandomPassword();
        }
        return CmsDefaultPasswordGenerator.getRandomPWD();
    }
}
