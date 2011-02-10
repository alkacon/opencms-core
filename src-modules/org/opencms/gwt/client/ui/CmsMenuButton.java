/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsMenuButton.java,v $
 * Date   : $Date: 2011/02/10 16:36:57 $
 * Version: $Revision: 1.25 $
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
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.CmsSlideAnimation;
import org.opencms.util.CmsStringUtil;

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
 * @version $Revision: 1.25 $
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

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String showAbove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarMode();
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

    /** Flag if the menu is open. */
    private boolean m_isOpen;

    /** Flag if the menu opens to the right hand side. */
    private boolean m_isOpenRight;

    /** Flag if the button is in toolbar mode. */
    private boolean m_isToolbarMode;

    /**
     * Constructor.<p>
     * 
     * @param buttonText the menu button text
     * @param imageClass the menu button image sprite class
     */
    @UiConstructor
    public CmsMenuButton(String buttonText, String imageClass) {

        this();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(buttonText)) {
            m_button.setText(buttonText);
        }
        m_button.setImageClass(imageClass);
    }

    /**
     * Constructor.<p>
     */
    private CmsMenuButton() {

        initWidget(uiBinder.createAndBindUi(this));
        m_button.setSize(I_CmsButton.Size.big);
        m_content = new CmsMenuContent();
        m_content.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().menuPopup());
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
                m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
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
     * Disables the menu button.<p>
     * 
     * @param disabledReason the reason to set in the button title
     */
    public void disable(String disabledReason) {

        m_button.disable(disabledReason);
    }

    /**
     * Hides the menu content as well as the menu connector.<p>
     */
    public void hide() {

        m_content.setVisible(false);
        m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
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
     * Returns the isOpenRight.<p>
     *
     * @return the isOpenRight
     */
    public boolean isOpenRight() {

        return m_isOpenRight;
    }

    /**
     * Returns the isToolbarMode.<p>
     *
     * @return the isToolbarMode
     */
    public boolean isToolbarMode() {

        return m_isToolbarMode;
    }

    /**
     * Opens the menu and fires the on toggle event.<p>
     */
    public void openMenu() {

        m_menuConnect.getStyle().setWidth(m_button.getOffsetWidth() + 2, Style.Unit.PX);

        m_isOpen = true;
        m_button.setDown(true);

        m_menuConnect.removeClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
        m_content.show();

        positionPopup();
        CmsSlideAnimation.slideIn(m_content.getElement(), null, 200);
        m_resizeRegistration = Window.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                positionPopup();
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
     * Sets the isOpenRight.<p>
     *
     * @param isOpenRight the isOpenRight to set
     */
    public void setOpenRight(boolean isOpenRight) {

        m_isOpenRight = isOpenRight;
    }

    /**
     * Sets the isToolbarMode.<p>
     *
     * @param isToolbarMode the isToolbarMode to set
     */
    public void setToolbarMode(boolean isToolbarMode) {

        m_isToolbarMode = isToolbarMode;
        if (m_isToolbarMode) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().toolbarMode());
            // important, so a click on the button won't trigger the auto-close 
            m_content.addAutoHidePartner(getElement());
            m_content.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().menuPopup());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().toolbarMode());
            m_content.removeAutoHidePartner(getElement());
            m_content.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().menuPopup());
        }
    }

    /**
     * Shows the menu content as well as the menu connector.<p>
     */
    public void show() {

        m_content.setVisible(true);
        m_menuConnect.removeClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
    }

    /**
     * Called on auto close.<p>
     */
    protected void autoClose() {

        setButtonUp();
    }

    /**
     * Returns the popup content.<p>
     * 
     * @return the popup content
     */
    protected CmsMenuContent getPopupContent() {

        return m_content;
    }

    /**
     * Hides the menu content without altering the button state.<p>
     */
    protected void hideMenu() {

        m_content.hide();
        m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
        if (m_resizeRegistration != null) {
            m_resizeRegistration.removeHandler();
            m_resizeRegistration = null;
        }
    }

    /**
     * Positions the menu popup below the button.<p>
     */
    protected void positionPopup() {

        CmsPositionBean buttonPosition = CmsPositionBean.generatePositionInfo(m_button.getElement());
        int contentTop;
        int contentWidth = m_content.getOffsetWidth();
        int contentLeft = m_isOpenRight ? buttonPosition.getLeft() - 5 : buttonPosition.getLeft()
            - contentWidth
            + buttonPosition.getWidth()
            + 5;
        int windowWidth = Window.getClientWidth();
        if (m_isToolbarMode) {
            // overriding position absolute set by PopupPanel 
            m_content.getElement().getStyle().setPosition(Position.FIXED);
            contentTop = buttonPosition.getTop() + buttonPosition.getHeight() - Window.getScrollTop() + 4;
            if ((contentWidth + 10 < windowWidth) && (contentWidth + contentLeft > windowWidth)) {
                contentLeft = windowWidth - contentWidth - 10;
            } else {
                contentLeft -= Window.getScrollLeft();
            }
            m_content.setPopupPosition(contentLeft, contentTop);
            return;
        }

        contentTop = buttonPosition.getTop() + buttonPosition.getHeight() + 1;

        int contentHeight = m_content.getOffsetHeight();
        int windowHeight = Window.getClientHeight();
        boolean showBelowButton = true;
        if ((contentHeight + 10 < windowHeight)
            && (buttonPosition.getTop() - Window.getScrollTop() > contentHeight)
            && (contentHeight + contentTop - Window.getScrollTop() > windowHeight)) {
            // content fits into the window height, there is enough space above the button and there is to little space below the button
            // so show above
            showBelowButton = false;
            contentTop = buttonPosition.getTop() - 1 - contentHeight;
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().showAbove());
            m_menuConnect.removeClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
            m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
        } else {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().showAbove());
            m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
            m_menuConnect.removeClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerBottom());
        }

        if ((contentWidth + 10 < windowWidth) && (contentWidth + contentLeft > windowWidth)) {
            contentLeft = windowWidth - contentWidth - 10;
        }
        m_content.setPopupPosition(contentLeft, contentTop);
        m_content.showConnect(buttonPosition.getWidth() + 2, m_isOpenRight, showBelowButton);
    }

    /**
     * Sets button to state up, hides menu fragments (not the content pop-up) and fires the toggle event.<p>
     */
    private void setButtonUp() {

        m_isOpen = false;
        m_button.setDown(false);
        m_menuConnect.addClassName(I_CmsLayoutBundle.INSTANCE.menuButtonCss().hidden());
    }
}
