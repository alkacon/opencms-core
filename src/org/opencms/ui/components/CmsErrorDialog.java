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

package org.opencms.ui.components;

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Dialog used to display error stack traces in the workplace.<p>
 */
public class CmsErrorDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The dialog context. */
    private I_CmsDialogContext m_dialogContext;

    /** Label to display. */
    private Label m_errorLabel;

    /** Error message label. */
    private Label m_errorMessage;

    /** Warning icon. */
    private Label m_icon;

    /** The OK button. */
    private Button m_okButton;

    /**
     * Creates a new instance.<p>
     *
     * @param t the error to be displayed
     * @param context the dialog context
     */
    public CmsErrorDialog(Throwable t, I_CmsDialogContext context) {
        m_dialogContext = context;
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontAwesome.WARNING.getHtml());
        m_icon.addStyleName(ValoTheme.LABEL_COLORED);
        m_icon.addStyleName(ValoTheme.LABEL_HUGE);
        m_errorLabel.setContentMode(ContentMode.PREFORMATTED);
        m_errorLabel.setValue(ExceptionUtils.getFullStackTrace(t));
        m_errorMessage.setValue(t.getLocalizedMessage());
        m_okButton.addClickListener(new ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_dialogContext.finish(null);
            }
        });
    }

}
