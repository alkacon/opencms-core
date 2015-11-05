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
import org.opencms.ui.Messages;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Dialog used to display error stack traces in the workplace.<p>
 */
public class CmsErrorDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Label to display. */
    private Label m_errorLabel;

    /** Error message label. */
    private Label m_errorMessage;

    /** Warning icon. */
    private Label m_icon;

    /** The OK button. */
    private Button m_okButton;

    /** The dialog context. */
    private Runnable m_onClose;

    /** The dialog window. */
    private Window m_window;

    /**
     * Creates a new instance.<p>
     *
     * @param message the error message
     * @param t the error to be displayed
     * @param onClose executed on close
     * @param window the dialog window if available
     */
    public CmsErrorDialog(String message, Throwable t, Runnable onClose, final Window window) {
        m_onClose = onClose;
        m_window = window;
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
        m_errorLabel.addStyleName(OpenCmsTheme.FULL_WIDTH_PADDING);
        m_errorMessage.setValue(message);
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClose();
            }
        });
        if (m_window != null) {
            m_window.addCloseListener(new CloseListener() {

                private static final long serialVersionUID = 1L;

                public void windowClose(CloseEvent e) {

                    onClose();
                }
            });
        }

    }

    /**
     * Shows the error dialog.<p>
     *
     * @param message the error message
     * @param t the error to be displayed
     */
    public static void showErrorDialog(String message, Throwable t) {

        showErrorDialog(message, t, null);
    }

    /**
     * Shows the error dialog.<p>
     *
     * @param message the error message
     * @param t the error to be displayed
     * @param onClose executed on close
     */
    public static void showErrorDialog(String message, Throwable t, Runnable onClose) {

        Window window = prepareWindow(DialogWidth.max);
        window.setCaption(Messages.get().getBundle().key(Messages.GUI_ERROR_0));
        window.setContent(new CmsErrorDialog(message, t, onClose, window));
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Shows the error dialog.<p>
     *
     * @param t the error to be displayed
     */
    public static void showErrorDialog(Throwable t) {

        showErrorDialog(t.getLocalizedMessage(), t, null);
    }

    /**
     * Shows the error dialog.<p>
     *
     * @param t the error to be displayed
     * @param onClose executed on close
     */
    public static void showErrorDialog(Throwable t, Runnable onClose) {

        showErrorDialog(t.getLocalizedMessage(), t, onClose);
    }

    /**
     * Called on dialog close.<p>
     */
    void onClose() {

        if (m_onClose != null) {
            m_onClose.run();
        }
        if (m_window != null) {
            m_window.close();
        }
    }

}
