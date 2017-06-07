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

import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for a single notification message.<p>
 */
public class CmsNotificationMessage extends Composite {

    /** The UI binder interface. */
    interface I_CmsNotificationMessageUiBinder extends UiBinder<Widget, CmsNotificationMessage> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsNotificationMessageUiBinder uiBinder = GWT.create(I_CmsNotificationMessageUiBinder.class);

    /** The close button. */
    @UiField(provided = true)
    CmsPushButton m_closeButton;

    /** The message content DIV. */
    @UiField
    Element m_messageContent;

    /** The notification mode. */
    private CmsNotification.Mode m_mode;

    /**
     * Constructor.<p>
     *
     * @param mode the notification mode
     * @param type the message type
     * @param message the message content
     */
    public CmsNotificationMessage(CmsNotification.Mode mode, CmsNotification.Type type, String message) {
        m_mode = mode;
        m_closeButton = new CmsPushButton(I_CmsButton.DELETE_SMALL);
        m_closeButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_closeButton.setSize(Size.small);
        initWidget(uiBinder.createAndBindUi(this));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(message)) {
            m_messageContent.setInnerHTML(message);
            addStyleName(classForType(type));
            if (!CmsNotification.Mode.BROADCAST.equals(m_mode)) {
                m_closeButton.setVisible(false);
            }
        } else {
            setVisible(false);
        }
    }

    /**
     * Returns if the given message is of a blocking mode.<p>
     *
     * @return <code>true</code> if the given message is of a blocking mode
     */
    public boolean isBlockingMode() {

        return CmsNotification.Mode.BUSY.equals(m_mode);
    }

    /**
     * Returns if the given message is of a busy mode.<p>
     *
     * @return <code>true</code> if the given message is of a busy mode
     */
    public boolean isBusyMode() {

        return CmsNotification.Mode.BUSY.equals(m_mode);
    }

    /**
     * Handles the close button click event.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_closeButton")
    public void onCloseClick(ClickEvent event) {

        CmsNotification.get().removeMessage(this);
    }

    /**
     * Returns the class name for the given type.<p>
     *
     * @param type the type
     *
     * @return the class name
     */
    private String classForType(Type type) {

        switch (type) {
            case ERROR:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationError();
            case NORMAL:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationNormal();
            case WARNING:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationWarning();
            default:
                return I_CmsLayoutBundle.INSTANCE.notificationCss().notificationNormal();
        }
    }

}
