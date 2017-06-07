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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

/**
 * Provides a generic error dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsErrorDialog extends CmsPopup {

    /** The active error dialog ids. */
    private static Set<String> m_activeErrorDialogIds = new HashSet<String>();

    /** The stack trace line break. */
    private static final String LINE_BREAK = "\n";

    /** The 'close' button. */
    private CmsPushButton m_closeButton;

    /** The details fieldset. */
    private CmsFieldSet m_detailsFieldset;

    /** String which identifies the error dialog. */
    private String m_errorDialogId;

    /** The message HTML. */
    private CmsMessageWidget m_messageWidget;

    /**
     * Constructor.<p>
     *
     * @param message the error message
     * @param details the error details, will be 'pre' formatted
     */
    public CmsErrorDialog(String message, String details) {

        super(Messages.get().key(Messages.GUI_ERROR_0), WIDE_WIDTH);
        m_errorDialogId = new Date() + " " + message;
        setAutoHideEnabled(false);
        setModal(true);
        setGlassEnabled(true);
        addDialogClose(null);
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(Messages.get().key(Messages.GUI_CLOSE_0));
        m_closeButton.setUseMinWidth(true);
        m_closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClose();
            }
        });
        addButton(m_closeButton);

        Panel content = new FlowPanel();
        m_messageWidget = createMessageWidget(message);
        content.add(m_messageWidget);
        if (details != null) {
            // prepend the message
            details = message + LINE_BREAK + LINE_BREAK + details;
            m_detailsFieldset = createDetailsFieldSet(details);
            m_detailsFieldset.addOpenHandler(new OpenHandler<CmsFieldSet>() {

                /**
                 * On open.<p>
                 *
                 * @param event the open event
                 */
                public void onOpen(OpenEvent<CmsFieldSet> event) {

                    center();
                }
            });
            content.add(m_detailsFieldset);
            m_detailsFieldset.setVisible(false);
            CmsPushButton detailsB = new CmsPushButton();
            detailsB.setText(Messages.get().key(Messages.GUI_DETAILS_0));
            detailsB.setUseMinWidth(true);
            detailsB.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    toggleDetails();
                }
            });
            addButton(detailsB, 0);
            if (CmsDomUtil.isCopyToClipboardSupported()) {
                final String id = "details" + CmsClientStringUtil.randomUUID();
                m_detailsFieldset.getElement().setId(id);
                CmsPushButton copy = new CmsPushButton();
                copy.setText(Messages.get().key(Messages.GUI_COPY_TO_CLIPBOARD_0));
                copy.setUseMinWidth(true);
                copy.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {

                        CmsDomUtil.copyToClipboard("#" + id + " .gwt-HTML");
                    }
                });
                copy.getElement().getStyle().setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
                copy.getElement().getStyle().setMarginLeft(0, Unit.PX);
                copy.setTitle(Messages.get().key(Messages.GUI_COPY_TO_CLIPBOARD_DESCRIPTION_0));
                addButton(copy, 0);
            }
        }
        setMainContent(content);
    }

    /**
     * Handles the exception by logging the exception to the server log and displaying an error dialog on the client.<p>
     *
     * @param message the error message
     * @param t the throwable
     */
    public static void handleException(String message, Throwable t) {

        StackTraceElement[] trace;
        String className;
        if (t instanceof CmsRpcException) {
            CmsRpcException ex = (CmsRpcException)t;
            trace = ex.getOriginalStackTrace();
            className = ex.getOriginalClassName();
        } else {
            message = CmsClientStringUtil.getMessage(t);
            trace = t.getStackTrace();
            className = t.getClass().getName();
        }
        // send the ticket to the server
        String ticket = CmsLog.log(message + LINE_BREAK + CmsClientStringUtil.getStackTraceAsString(trace, LINE_BREAK));

        String errorMessage = message == null
        ? className + ": " + Messages.get().key(Messages.GUI_NO_DESCIPTION_0)
        : message;
        errorMessage += LINE_BREAK + Messages.get().key(Messages.GUI_REASON_0) + ":" + t;

        String details = Messages.get().key(Messages.GUI_TICKET_MESSAGE_3, ticket, className, message)
            + CmsClientStringUtil.getStackTraceAsString(trace, LINE_BREAK);
        new CmsErrorDialog(errorMessage, details).center();
    }

    /**
     * Handles the exception by logging the exception to the server log and displaying an error dialog on the client.<p>
     *
     * @param t the throwable
     */
    public static void handleException(Throwable t) {

        String message;
        StackTraceElement[] trace;
        String cause = null;
        String className;
        if (t instanceof CmsRpcException) {
            CmsRpcException ex = (CmsRpcException)t;
            message = ex.getOriginalMessage();
            trace = ex.getOriginalStackTrace();
            cause = ex.getOriginalCauseMessage();
            className = ex.getOriginalClassName();
        } else {
            message = CmsClientStringUtil.getMessage(t);
            trace = t.getStackTrace();
            if (t.getCause() != null) {
                cause = CmsClientStringUtil.getMessage(t.getCause());
            }
            className = t.getClass().getName();
        }
        // send the ticket to the server
        String ticket = CmsLog.log(message + LINE_BREAK + CmsClientStringUtil.getStackTraceAsString(trace, LINE_BREAK));

        String errorMessage = message == null
        ? className + ": " + Messages.get().key(Messages.GUI_NO_DESCIPTION_0)
        : message;
        if (cause != null) {
            errorMessage += LINE_BREAK + Messages.get().key(Messages.GUI_REASON_0) + ":" + cause;
        }

        String details = Messages.get().key(Messages.GUI_TICKET_MESSAGE_3, ticket, className, message)
            + CmsClientStringUtil.getStackTraceAsString(trace, LINE_BREAK);
        new CmsErrorDialog(errorMessage, details).center();
    }

    /**
     * Checks if any error dialogs are showing.<p>
     *
     * @return true if any error dialogs are showing
     */
    public static boolean isShowingErrorDialogs() {

        return m_activeErrorDialogIds.size() > 0;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        m_activeErrorDialogIds.add(m_errorDialogId);
        show();
        super.center();

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#hide()
     */
    @Override
    public void hide() {

        m_activeErrorDialogIds.remove(m_errorDialogId);
        super.hide();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        m_activeErrorDialogIds.add(m_errorDialogId);
        super.show();
        onShow();
    }

    /**
     * Executed on 'close' click. <p>
     */
    protected void onClose() {

        m_closeButton.setEnabled(false);
        hide();
    }

    /**
     * Toggles the details visibility.<p>
     */
    void toggleDetails() {

        if (m_detailsFieldset != null) {
            m_detailsFieldset.setVisible(!m_detailsFieldset.isVisible());
            center();
        }
    }

    /**
     * Creates a field-set containing the error details.<p>
     *
     * @param details the error details
     *
     * @return the field-set widget
     */
    private CmsFieldSet createDetailsFieldSet(String details) {

        CmsFieldSet fieldset = new CmsFieldSet();
        fieldset.addStyleName(I_CmsLayoutBundle.INSTANCE.errorDialogCss().details());
        fieldset.setLegend(Messages.get().key(Messages.GUI_LABEL_STACKTRACE_0));
        fieldset.addContent(new HTML("<pre>" + details + "</pre>"));
        fieldset.setOpen(true);
        return fieldset;
    }

    /**
     * Creates the message HTML widget containing error icon and message.<p>
     *
     * @param message the message
     *
     * @return the HTML widget
     */
    private CmsMessageWidget createMessageWidget(String message) {

        CmsMessageWidget widget = new CmsMessageWidget();
        widget.setIcon(FontOpenCms.ERROR, I_CmsConstantsBundle.INSTANCE.css().colorError());
        widget.setMessageHtml(message);
        return widget;
    }

    /**
     * Checks the available space and sets max-height to the details field-set.
     */
    private void onShow() {

        if (m_detailsFieldset != null) {
            m_detailsFieldset.getContentPanel().getElement().getStyle().setPropertyPx(
                "maxHeight",
                getAvailableHeight(m_messageWidget.getOffsetHeight()));
        }
    }
}
