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

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.Messages;
import org.opencms.util.CmsUUID;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

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

    /** Hidden stack trace element. */
    private Label m_hiddenStack;

    /** The OK button. */
    private Button m_okButton;

    /** The dialog context. */
    private Runnable m_onClose;

    /** The dialog window. */
    private Window m_window;

    /** The details component. */
    private CssLayout m_details;

    /** The select text button. */
    private CmsCopyToClipboardButton m_copyText;

    /** The toggle details button. */
    private Button m_detailsButton;

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
        m_icon.setValue(FontOpenCms.ERROR.getHtml());
        m_errorLabel.setContentMode(ContentMode.PREFORMATTED);
        final String labelId = "label" + new CmsUUID().toString();
        String stacktrace = message + "\n\n" + ExceptionUtils.getFullStackTrace(t);
        m_hiddenStack.setId(labelId);
        m_hiddenStack.setValue(stacktrace);
        m_errorLabel.setValue(stacktrace);
        m_errorLabel.addStyleName(OpenCmsTheme.FULL_WIDTH_PADDING);
        m_errorMessage.setContentMode(ContentMode.HTML);
        m_errorMessage.setValue(message);

        m_copyText.setSelector("#" + labelId);
        m_details.setVisible(false);

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onClose();
            }
        });
        m_detailsButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                toggleDetails();
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

        Window window = prepareWindow(DialogWidth.wide);
        window.setCaption(Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_ERROR_0));
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

    /**
     * Toggles the details visibility.<p>
     */
    void toggleDetails() {

        m_details.setVisible(!m_details.isVisible());
        if (m_window != null) {
            m_window.center();
        }
    }

}
