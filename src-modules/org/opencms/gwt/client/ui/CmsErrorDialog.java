/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsErrorDialog.java,v $
 * Date   : $Date: 2011/02/09 13:20:50 $
 * Version: $Revision: 1.3 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

/**
 * Provides a generic error dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsErrorDialog extends CmsPopupDialog {

    /** The 'close' button. */
    private CmsPushButton m_closeButton;

    private CmsFieldSet m_detailsFieldset;

    private HTML m_messageHtml;

    /**
     * Constructor.<p>
     * 
     * @param message the error message
     * @param details the error details
     */
    public CmsErrorDialog(String message, String details) {

        super();
        setAutoHideEnabled(false);
        setModal(true);
        setGlassEnabled(true);
        setText(Messages.get().key(Messages.GUI_ERROR_0));
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_closeButton.setUseMinWidth(true);
        m_closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            @Override
            public void onClick(ClickEvent event) {

                onClose();
            }
        });
        addButton(m_closeButton);

        Panel content = new FlowPanel();
        m_messageHtml = createMessageHtml(message);
        content.add(m_messageHtml);
        if (details != null) {
            m_detailsFieldset = createDetailsFieldSet(details);
            content.add(m_detailsFieldset);
        }
        setContent(content);
        this.show();
        this.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
        onShow();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

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
        fieldset.setCollapsed(true);
        return fieldset;
    }

    /**
     * Creates the message HTML widget containing error icon and message.<p>
     * 
     * @param message the message
     * 
     * @return the HTML widget
     */
    private HTML createMessageHtml(String message) {

        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<div class=\"").append(I_CmsLayoutBundle.INSTANCE.errorDialogCss().errorIcon()).append(
            "\"></div><p class=\"").append(I_CmsLayoutBundle.INSTANCE.errorDialogCss().message()).append("\">").append(
            message).append("</p><hr class=\"").append(I_CmsLayoutBundle.INSTANCE.generalCss().clearAll()).append(
            "\" />");
        return new HTML(buffer.toString());
    }

    /**
     * Checks the available space and sets max-height to the details field-set.
     */
    private void onShow() {

        int maxHeight = Window.getClientHeight() - 180 - m_messageHtml.getOffsetHeight();
        if (m_detailsFieldset != null) {
            m_detailsFieldset.getContentPanel().getElement().getStyle().setPropertyPx("maxHeight", maxHeight);
        }
    }

}
