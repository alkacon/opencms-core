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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
     * @param details the error details
     */
    public CmsErrorDialog(String message, String details) {

        super(Messages.get().key(Messages.GUI_ERROR_0));
        m_errorDialogId = new Date() + " " + message;
        setAutoHideEnabled(false);
        setModal(true);
        setGlassEnabled(true);
        setWidth(512);
        addDialogClose(null);
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(Messages.get().key(Messages.GUI_OK_0));
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
        }
        setMainContent(content);
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
        String ticket = CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTraceAsString(trace, "\n"));
        String lineBreak = "<br />\n";
        String errorMessage = message == null
        ? className + ": " + Messages.get().key(Messages.GUI_NO_DESCIPTION_0)
        : message;
        if (cause != null) {
            errorMessage += lineBreak + Messages.get().key(Messages.GUI_REASON_0) + ":" + cause;
        }

        String details = Messages.get().key(Messages.GUI_TICKET_MESSAGE_3, ticket, className, message)
            + CmsClientStringUtil.getStackTraceAsString(trace, lineBreak);
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
     * Creates a field-set containing the error details.<p>
     * 
     * @param details the error details
     * 
     * @return the field-set widget
     */
    private CmsFieldSet createDetailsFieldSet(String details) {

        CmsFieldSet fieldset = new CmsFieldSet();
        fieldset.addStyleName(I_CmsLayoutBundle.INSTANCE.errorDialogCss().details());
        fieldset.setLegend(Messages.get().key(Messages.GUI_DETAILS_0));
        fieldset.addContent(new HTML(details));
        fieldset.setOpen(false);
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
        widget.setIconClass(I_CmsLayoutBundle.INSTANCE.errorDialogCss().errorIcon());
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
