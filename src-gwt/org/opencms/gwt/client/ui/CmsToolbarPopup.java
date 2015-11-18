/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * A popup which can be displayed below buttons in a toolbar.
 */
public class CmsToolbarPopup extends CmsPopup {

    /** The 'arrow-shaped' connector element above the popup. */
    protected Element m_arrow = DOM.createDiv();

    /** The toolbar button to which this popup belongs. */
    protected ButtonBase m_button;

    /** The toolbar width. */
    protected int m_toolbarWidth;

    /** The 'toolbar mode' flag. */
    protected boolean m_isToolbarMode;

    /** The base element of the toolbar button. */
    protected Element m_baseElement;

    /**
     * Creates a new toolbar popup.<p>
     *
     * @param button the toolbar button to which this popup belongs
     * @param toolbarMode the toolbar mode flag
     * @param baseElement the base element of the toolbar button
     */
    public CmsToolbarPopup(ButtonBase button, boolean toolbarMode, Element baseElement) {

        super();
        m_button = button;
        m_baseElement = baseElement;
        m_isToolbarMode = toolbarMode;
        setModal(false);
        setAutoHideEnabled(true);
        setWidth(DEFAULT_WIDTH);
        removePadding();
    }

    /**
     * Calculates the popup height to use.<p>
     *
     * @return the height
     */
    public static int getAvailableHeight() {

        int height = CmsGalleryDialog.DEFAULT_DIALOG_HEIGHT;
        if (Window.getClientHeight() > 590) {
            height = (int)Math.ceil((Window.getClientHeight() - 50) * 0.9);
        }
        return height;
    }

    /**
     * Calculates the popup width to use.<p>
     *
     * @return the width
     */
    public static int getAvailableWidth() {

        int width = CmsGalleryDialog.DEFAULT_DIALOG_WIDTH;
        if (Window.getClientWidth() > 1100) {
            width = 1000;
        }
        return width;
    }

    /**
     * Positions the menu popup the button.<p>
     *
     * @param popup the popup to position
     * @param button the toolbar button
     * @param toolbarWidth the width of the toolbar
     * @param isToolbarMode a flag indicating whether the button is in toolbar mode
     * @param arrow the arrow shaped connector element
     */
    protected static void positionPopup(
        CmsPopup popup,
        Widget button,
        int toolbarWidth,
        boolean isToolbarMode,
        Element arrow) {

        int spaceAssurance = 20;
        int space = toolbarWidth + (2 * spaceAssurance);

        // get the window client width
        int windowWidth = Window.getClientWidth();
        // get the min left position
        int minLeft = (windowWidth - space) / 2;
        if (minLeft < spaceAssurance) {
            minLeft = spaceAssurance;
        }
        // get the max right position
        int maxRight = minLeft + space;
        // get the middle button position
        CmsPositionBean buttonPosition = CmsPositionBean.generatePositionInfo(button.getElement());
        int buttonMiddle = (buttonPosition.getLeft() - Window.getScrollLeft()) + (buttonPosition.getWidth() / 2);
        // get the content width
        int contentWidth = popup.getOffsetWidth();

        // the optimum left position is in the middle of the button minus the half content width
        // assume that the optimum fits into the space
        int contentLeft = buttonMiddle - (contentWidth / 2);

        if (minLeft > contentLeft) {
            // if the optimum left position of the popup is outside the min left position:
            // move the popup to the right (take the min left position as left)
            contentLeft = minLeft;
        } else if ((contentLeft + contentWidth) > maxRight) {
            // if the left position plus the content width is outside the max right position:
            // move the popup to the left (take the max right position minus the content width)
            contentLeft = maxRight - contentWidth;
        }

        // limit the right position if the popup is right outside the window
        if ((contentLeft + contentWidth + spaceAssurance) > windowWidth) {
            contentLeft = windowWidth - contentWidth - spaceAssurance;
        }

        // limit the left position if the popup is left outside the window
        if (contentLeft < spaceAssurance) {
            contentLeft = spaceAssurance;
        }

        int arrowSpace = 10;
        int arrowWidth = 40;
        int arrowHeight = 12;

        // the optimum position for the arrow is in the middle of the button
        int arrowLeft = buttonMiddle - contentLeft - (arrowWidth / 2);
        if ((arrowLeft + arrowWidth + arrowSpace) > contentWidth) {
            // limit the arrow position if the maximum is reached (content width 'minus x')
            arrowLeft = contentWidth - arrowWidth - arrowSpace;
        } else if ((arrowLeft - arrowSpace) < 0) {
            // limit the arrow position if the minimum is reached ('plus x')
            arrowLeft = arrowWidth + arrowSpace;
        }

        int arrowTop = -(arrowHeight - 2);
        String arrowClass = I_CmsLayoutBundle.INSTANCE.dialogCss().menuArrowTop();

        int contentTop = (((buttonPosition.getTop() + buttonPosition.getHeight()) - Window.getScrollTop())
            + arrowHeight) - 2;
        if (!isToolbarMode) {
            contentTop = (buttonPosition.getTop() + buttonPosition.getHeight() + arrowHeight) - 2;
            int contentHeight = popup.getOffsetHeight();
            int windowHeight = Window.getClientHeight();

            if (((contentHeight + spaceAssurance) < windowHeight)
                && ((buttonPosition.getTop() - Window.getScrollTop()) > contentHeight)
                && (((contentHeight + spaceAssurance + contentTop) - Window.getScrollTop()) > windowHeight)) {
                // content fits into the window height,
                // there is enough space above the button
                // and there is to little space below the button
                // so show above
                contentTop = ((buttonPosition.getTop() - arrowHeight) + 2) - contentHeight;
                arrowTop = contentHeight - 1;
                arrowClass = I_CmsLayoutBundle.INSTANCE.dialogCss().menuArrowBottom();
            }
        } else {
            contentLeft = contentLeft - Window.getScrollLeft();
            popup.setPositionFixed();
        }

        arrow.setClassName(arrowClass);
        arrow.getStyle().setLeft(arrowLeft, Unit.PX);
        arrow.getStyle().setTop(arrowTop, Unit.PX);

        popup.showArrow(arrow);
        popup.setPopupPosition(contentLeft + Window.getScrollLeft(), contentTop);
    }

    /**
     * Positions the popup below the toolbar button.<p>
     */
    public void position() {

        positionPopup(this, m_button, getToolbarWidth(), m_isToolbarMode, m_arrow);
    }

    /**
     * Sets the isToolbarMode.<p>
     *
     * @param isToolbarMode the isToolbarMode to set
     */
    public void setToolbarMode(boolean isToolbarMode) {

        m_isToolbarMode = isToolbarMode;
        if (m_isToolbarMode) {
            // important, so a click on the button won't trigger the auto-close
            addAutoHidePartner(m_baseElement);
        } else {
            removeAutoHidePartner(m_baseElement);
        }
    }

    /**
     * Returns the toolbar width.<p>
     *
     * @return the toolbar width
     */
    private int getToolbarWidth() {

        return Window.getClientWidth() - 20;
    }
}
