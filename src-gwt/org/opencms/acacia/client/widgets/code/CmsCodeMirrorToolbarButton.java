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

package org.opencms.acacia.client.widgets.code;

import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * Simple toolbar button class for the CodeMirror widget.
 */
public class CmsCodeMirrorToolbarButton extends Composite implements HasClickHandlers {

    /** The root element. */
    protected HTML m_root;

    /** True if the button is enabled. */
    protected boolean m_enabled = true;

    /** Controls the style for enabled / disabled state. */
    protected CmsStyleVariable m_enabledStyle = new CmsStyleVariable(this);

    /**
     * Creates a new instance.
     *
     * @param icon the icon
     */
    public CmsCodeMirrorToolbarButton(FontOpenCms icon) {

        m_root = new HTML();
        m_root.setHTML("<span>" + icon.getCodePointEntity() + "</span>");
        initWidget(m_root);
        addStyleName("oc-codewidget-button");
    }

    /**
     * Creates a new instance.
     *
     * @param icon the icon text.
     */
    public CmsCodeMirrorToolbarButton(String icon) {

        m_root = new HTML();
        m_root.setHTML("<span>" + icon + "</span>");
        initWidget(m_root);
        addStyleName("oc-codewidget-button");
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(event -> {
            if (m_enabled) {
                handler.onClick(event);
            }
        }, ClickEvent.getType());
    }

    /**
     * Enables / disables the button.
     *
     * @param enabled true if the button should be enabled
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
        m_enabledStyle.setValue(enabled ? null : "oc-disabled");
    }

}
