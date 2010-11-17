/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsMenuContent.java,v $
 * Date   : $Date: 2010/11/17 07:20:17 $
 * Version: $Revision: 1.9 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a tool-bar menu button content pop-up to be shown at the top of a page.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsMenuContent extends PopupPanel implements I_CmsAutoHider {

    /** The default pop-up width. */
    private static final String DEFAULT_WIDTH = "650px";

    /** Connecting element. Used to visually connect the content with the menu button. */
    private Element m_connect;

    /** The container element, all content will be inserted into this element. */
    private Element m_containerElement;

    /**
     * Constructor.<p>
     */
    public CmsMenuContent() {

        super(true);
        m_containerElement = super.getContainerElement();
        setWidth(DEFAULT_WIDTH);
        setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
        addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().menuPopup());
        Element shadowDiv = DOM.createDiv();
        shadowDiv.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupShadow()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_containerElement.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupContent()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        getElement().insertFirst(shadowDiv);
    }

    /**
     * Constructor.<p>
     * 
     * @param content the menu content
     */
    public CmsMenuContent(Widget content) {

        this();
        setWidget(content);
    }

    /**
     * Show the connecting element at the appropriate position.<p>
     * 
     * @param width the required width
     * @param showLeft <code>true</code> to show on the left hand side
     */
    public void showConnect(int width, boolean showLeft) {

        if (m_connect == null) {
            m_connect = DOM.createDiv();
            m_connect.addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().contentConnect());
            getElement().appendChild(m_connect);
        }
        Style style = m_connect.getStyle();
        style.setWidth(width, Unit.PX);
        if (showLeft) {
            style.clearRight();
            style.setLeft(3, Unit.PX);
        } else {
            style.clearLeft();
            style.setRight(3, Unit.PX);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#getContainerElement()
     */
    @Override
    protected com.google.gwt.user.client.Element getContainerElement() {

        if (m_containerElement == null) {
            // will happen when called from super constructor
            m_containerElement = super.getContainerElement();
        }
        return m_containerElement.cast();
    }
}
