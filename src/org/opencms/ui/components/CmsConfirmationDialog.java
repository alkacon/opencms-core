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

package org.opencms.ui.components;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Basic confirmation dialog.<p>
 */
public class CmsConfirmationDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** OK button .*/
    private Button m_okButton;

    /** Cancel button. */
    private Button m_cancelButton;

    /** Label to display text. */
    private Label m_label;

    /** Warning icon. */
    private Label m_icon;

    /** Action to execute when confirmed. */
    Runnable m_okAction;

    /** Action to execute when cancelled. */
    Runnable m_cancelAction;

    /**
     * Creates a new instance.<p>
     *
     * @param message the message
     * @param okAction the action for the confirmation case
     * @param cancelAction the action for the cancel case
     */
    public CmsConfirmationDialog(String message, Runnable okAction, Runnable cancelAction) {
        m_okAction = okAction;

        m_cancelAction = cancelAction;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_label.setValue(message);
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (m_okAction != null) {
                    m_okAction.run();
                }
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if (m_cancelAction != null) {
                    m_cancelAction.run();
                }

            }
        });
    }

    /**
     * Shows the confirmation dialog in a window.<p>
     *
     * @param title the window title
     * @param message the message to display in the dialog
     * @param okAction the action to execute when the user clicks OK
     */
    public static void show(String title, String message, final Runnable okAction) {

        show(title, message, okAction, null);
    }

    /**
     * Shows the confirmation dialog in a window.<p>
     *
     * @param title the window title
     * @param message the message to display in the dialog
     * @param okAction the action to execute when the user clicks OK
     * @param cancelAction the action for the cancel case
     */
    public static void show(String title, String message, final Runnable okAction, final Runnable cancelAction) {

        final Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(title);
        CmsConfirmationDialog dialog = new CmsConfirmationDialog(message, new Runnable() {

            public void run() {

                window.close();
                okAction.run();
            }
        }, new Runnable() {

            public void run() {

                if (cancelAction != null) {
                    cancelAction.run();
                }
                window.close();
            }
        });
        window.setContent(dialog);
        UI.getCurrent().addWindow(window);
    }

    /**
     * Gets the label.<p>
     *
     * @return the label
     */
    public Label getLabel() {

        return m_label;
    }

    /**
     * Sets the message.<p>
     *
     * @param message the message
     */
    public void setMessage(String message) {

        m_label.setValue(message);
    }

}
