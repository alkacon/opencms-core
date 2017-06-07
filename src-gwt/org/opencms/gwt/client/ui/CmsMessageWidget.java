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

import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays a message text with an icon in front. Default is the 'warning' icon.<p>
 *
 * @since 8.0.1
 */
public class CmsMessageWidget extends Widget {

    /** The UI binder interface. */
    interface I_CmsMessageWidgetUiBinder extends UiBinder<Element, CmsMessageWidget> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_CmsMessageWidgetUiBinder uiBinder = GWT.create(I_CmsMessageWidgetUiBinder.class);

    /** The element displaying the icon. */
    @UiField
    protected Element m_iconCell;

    /** The element holding the message. */
    @UiField
    protected Element m_message;

    /**
     * Constructor.<p>
     */
    public CmsMessageWidget() {

        setElement(uiBinder.createAndBindUi(this));
        setIcon(FontOpenCms.WARNING, I_CmsConstantsBundle.INSTANCE.css().colorWarning());
    }

    /**
     * Sets the icon CSS class.<p>
     *
     * @param icon the icon
     * @param color the icon color
     */
    public void setIcon(FontOpenCms icon, String color) {

        if (icon != null) {
            m_iconCell.setInnerHTML(icon.getHtml(32, color));
        } else {
            m_iconCell.setInnerHTML("");
        }
    }

    /**
     * Sets the message HTML.<p>
     *
     * @param message the message HTML
     */
    public void setMessageHtml(String message) {

        m_message.setInnerHTML(message);
    }

    /**
     * Sets the message text.<p>
     *
     * @param message the message text
     */
    public void setMessageText(String message) {

        m_message.setInnerText(message);
    }
}
