/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsMenuButton.java,v $
 * Date   : $Date: 2010/03/11 08:07:18 $
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

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a menu button.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsMenuButton extends Composite implements HasWidgets, I_CmsHasToggleHandlers {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsMenuButtonUiBinder extends UiBinder<Widget, CmsMenuButton> {
        // GWT interface, nothing to do here
    }

    /**
     * The menu CSS interface.<p>
     */
    interface I_MenuButtonCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String button();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String connect();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String hidden();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String menu();
    }

    /** The ui-binder instance for this class. */
    private static I_CmsMenuButtonUiBinder uiBinder = GWT.create(I_CmsMenuButtonUiBinder.class);

    /** The menu button. */
    @UiField
    CmsTextButton m_button;

    /** DIV element connecting the button and the menu pop-up. */
    @UiField
    DivElement m_menuConnect;

    /** The menu CSS. */
    @UiField
    I_MenuButtonCss m_style;

    private CmsMenuContent m_content;

    private boolean m_initialized;

    private boolean m_isOpen;

    /**
     * Constructor.<p>
     */
    public CmsMenuButton() {

        initWidget(uiBinder.createAndBindUi(this));
        m_initialized = false;
        m_content = new CmsMenuContent();
        m_isOpen = false;
        m_button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                toggleMenu();
            }
        });
        m_content.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                if (event.isAutoClosed()) {
                    setButtonUp();
                }

            }

        });
    }

    /**
     * Constructor.<p>
     * 
     * @param buttonText the menu button text
     * @param imageClass the menu button image sprite class
     */
    @UiConstructor
    public CmsMenuButton(String buttonText, String imageClass) {

        this();
        m_button.setUpFace(buttonText, imageClass);
    }

    /**
     * This will set the menu content widget.<p>
     * 
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget widget) {

        m_content.setWidget(widget);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsHasToggleHandlers#addToggleHandler(org.opencms.gwt.client.ui.I_CmsToggleHandler)
     */
    public HandlerRegistration addToggleHandler(I_CmsToggleHandler handler) {

        return addHandler(handler, CmsToggleEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    public void clear() {

        m_content.clear();

    }

    /**
     * Closes the menu and fires the on toggle event.<p>
     */
    public void closeMenu() {

        m_content.hide();
        setButtonUp();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_content.iterator();
    }

    /**
     * Opens the menu and fires the on toggle event.<p>
     */
    public void openMenu() {

        if (!m_initialized) {
            if (Window.Navigator.getUserAgent().toLowerCase().contains("msie")) {
                // 
                m_content.setPopupPosition(m_button.getAbsoluteLeft() - 5, m_button.getAbsoluteTop() + 34);
            } else {
                m_content.setPopupPosition(m_button.getAbsoluteLeft() - 4, m_button.getAbsoluteTop() + 34);
            }
            m_menuConnect.getStyle().setWidth(m_button.getOffsetWidth() + 2, Style.Unit.PX);
            m_initialized = true;
        }
        m_isOpen = true;
        m_button.setDown(true);

        m_menuConnect.removeClassName(m_style.hidden());
        m_content.show();
        CmsToggleEvent.fire(this, true);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget w) {

        return m_content.remove(w);
    }

    /** 
     * Toggles the menu state.<p>
     */
    public void toggleMenu() {

        if (!m_isOpen) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    /**
     * Sets button to state up, hides menu fragments (not the content pop-up) and fires the toggle event.<p>
     */
    void setButtonUp() {

        m_isOpen = false;
        m_button.setDown(false);
        m_menuConnect.addClassName(m_style.hidden());
        CmsToggleEvent.fire(this, false);
    }

}
