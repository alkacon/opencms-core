/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsMenuButton.java,v $
 * Date   : $Date: 2010/10/12 06:56:47 $
 * Version: $Revision: 1.19 $
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a menu button.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.19 $
 * 
 * @since 8.0.0
 */
public class CmsMenuButton extends Composite implements HasClickHandlers {

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
    protected CmsPushButton m_button;

    /** The menu content. */
    protected CmsMenuContent m_content;

    /** DIV element connecting the button and the menu pop-up. */
    @UiField
    protected DivElement m_menuConnect;

    /** Registration of the window resize handler. */
    protected HandlerRegistration m_resizeRegistration;

    /** The menu CSS. */
    @UiField
    protected I_MenuButtonCss m_style;

    /** Flag if the menu is open. */
    private boolean m_isOpen;

    /**
     * Constructor.<p>
     * 
     * @param buttonText the menu button text
     * @param imageClass the menu button image sprite class
     */
    @UiConstructor
    public CmsMenuButton(String buttonText, String imageClass) {

        this();
        m_button.setText(buttonText);
        m_button.setImageClass(imageClass);
    }

    /**
     * Constructor.<p>
     */
    private CmsMenuButton() {

        initWidget(uiBinder.createAndBindUi(this));
        m_button.setSize(I_CmsButton.Size.big);
        m_content = new CmsMenuContent();

        // important, so a click on the button won't trigger the auto-close 
        m_content.addAutoHidePartner(getElement());

        m_isOpen = false;

        m_content.addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                if (event.isAutoClosed()) {
                    autoClose();
                    if (m_resizeRegistration != null) {
                        m_resizeRegistration.removeHandler();
                        m_resizeRegistration = null;
                    }
                }

            }

        });
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * Removes all content from menu.<p>
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
        if (m_resizeRegistration != null) {
            m_resizeRegistration.removeHandler();
            m_resizeRegistration = null;
        }
    }

    /**
     * Hides the menu content as well as the menu connector.<p>
     */
    public void hide() {

        m_content.setVisible(false);
        m_menuConnect.addClassName(m_style.hidden());
    }

    /**
     * Returns if the menu is open.<p>
     * 
     * @return <code>true</code> if the menu is opened
     */
    public boolean isOpen() {

        return m_isOpen;
    }

    /**
     * Opens the menu and fires the on toggle event.<p>
     */
    public void openMenu() {

        int windowWidth = Window.getClientWidth();
        int contentLeft = m_button.getAbsoluteLeft() - 5;
        int contentTop = m_button.getAbsoluteTop() - Window.getScrollTop() + 34;
        m_content.setPopupPosition(contentLeft - Window.getScrollLeft(), contentTop);
        m_menuConnect.getStyle().setWidth(m_button.getOffsetWidth() + 2, Style.Unit.PX);

        m_isOpen = true;
        m_button.setDown(true);

        m_menuConnect.removeClassName(m_style.hidden());
        m_content.show();
        int contentWidth = m_content.getOffsetWidth();
        if ((contentWidth + 10 < windowWidth) && (contentWidth + contentLeft > windowWidth)) {
            contentLeft = windowWidth - contentWidth - 10;
            m_content.setPopupPosition(contentLeft, contentTop);
        }
        // overriding position absolute set by PopupPanel 
        m_content.getElement().getStyle().setPosition(Position.FIXED);

        m_resizeRegistration = Window.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                // TODO: verify if there are layout differences between IE and other browsers
                m_content.setPopupPosition(
                    m_button.getAbsoluteLeft() - Window.getScrollLeft() - 5,
                    m_button.getAbsoluteTop() - Window.getScrollTop() + 34);

            }
        });
    }

    /**
     * Enables or disables the button.<p>
     * 
     * @param enabled if true, enables the button, else disables it
     */
    public void setEnabled(boolean enabled) {

        m_button.setEnabled(enabled);
    }

    /**
     * This will set the menu content widget.<p>
     * 
     * @param widget the widget to set as content 
     */
    public void setMenuWidget(Widget widget) {

        m_content.setWidget(widget);
    }

    /**
     * Shows the menu content as well as the menu connector.<p>
     */
    public void show() {

        m_content.setVisible(true);
        m_menuConnect.removeClassName(m_style.hidden());
    }

    /**
     * Called on auto close.<p>
     */
    protected void autoClose() {

        setButtonUp();
    }

    /**
     * Hides the menu content without altering the button state.<p>
     */
    protected void hideMenu() {

        m_content.hide();
        m_menuConnect.addClassName(m_style.hidden());
        if (m_resizeRegistration != null) {
            m_resizeRegistration.removeHandler();
            m_resizeRegistration = null;
        }
    }

    /**
     * Sets button to state up, hides menu fragments (not the content pop-up) and fires the toggle event.<p>
     */
    private void setButtonUp() {

        m_isOpen = false;
        m_button.setDown(false);
        m_menuConnect.addClassName(m_style.hidden());
    }

    /**
     * Returns the popup content.<p>
     * 
     * @return the popup content
     */
    protected CmsMenuContent getPopupContent() {

        return m_content;
    }
}
