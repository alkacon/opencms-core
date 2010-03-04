/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsPopupDialog.java,v $
 * Date   : $Date: 2010/03/04 15:17:18 $
 * Version: $Revision: 1.2 $
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a pop up dialog.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsPopupDialog extends DialogBox {

    /** The content widget. */
    private Widget m_content;

    /** The panel holding the dialog's buttons. */
    private HorizontalPanel m_buttonPanel;

    /** The main widget of this dialog containing all others. */
    protected VerticalPanel m_main;

    private com.google.gwt.user.client.Element m_containerElement;

    /** The default width of this dialog. */
    private static final String DEFAULT_WIDTH = "300px";

    /**
     * The Constructor.<p>
     */
    public CmsPopupDialog() {

        super(false);
        m_containerElement = super.getContainerElement();
        this.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
        this.setWidth(DEFAULT_WIDTH);
        Element shadowDiv = DOM.createDiv();
        shadowDiv.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupShadow());
        I_CmsLayoutBundle.INSTANCE.dialogCss().ensureInjected();
        m_main = new VerticalPanel();
        setWidget(m_main);
        m_main.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupMainContent());

        m_containerElement.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupContent());
        this.getElement().insertFirst(shadowDiv);
        this.setGlassStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content widget
     */
    public CmsPopupDialog(String title, Widget content) {

        this();
        this.setTitle(title);
        this.setContent(content);
    }

    /**
     * Sets the title and caption of this dialog.<p>
     * 
     * @param title the title
     */
    @Override
    public void setTitle(String title) {

        super.setTitle(title);
        this.setText(title);
    }

    /**
     * Sets the content for this dialog replacing any former content.
     * 
     * @param widget the content widget
     */
    public void setContent(Widget widget) {

        if (m_content != null) {
            m_main.remove(m_content);
        }
        m_main.insert(widget, 0);
        m_content = widget;
    }

    /**
     * Adds a button widget to the button panel.<p>
     * 
     * @param button the button widget
     */
    public void addButton(Widget button) {

        if (m_buttonPanel == null) {
            m_buttonPanel = new HorizontalPanel();
            m_main.add(m_buttonPanel);
            m_buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupButtonPanel());
            m_buttonPanel.getElement().getParentElement().setAttribute("align", "right");

        }
        //        button.addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
        m_buttonPanel.add(button);
    }

    /**
     * Removes the given button widget from the button panel.<p>
     * 
     * @param button the button widget to remove
     */
    public void removeButton(Widget button) {

        if (m_buttonPanel == null) {
            return;
        }
        m_buttonPanel.remove(button);
        if (m_buttonPanel.getWidgetCount() == 0) {
            m_main.remove(m_buttonPanel);
            m_buttonPanel = null;
        }
    }

    /**
     * Returns the content widget.
     * 
     * @return the content widget
     */
    public Widget getContent() {

        return m_content;
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#getContainerElement()
     */
    @Override
    protected com.google.gwt.user.client.Element getContainerElement() {

        if (m_containerElement == null) {
            m_containerElement = super.getContainerElement();
        }
        return m_containerElement;
    }

}
