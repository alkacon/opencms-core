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

package org.opencms.acacia.client.ui;

import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * In-line edit overlay covering rest of the page.<p>
 */
public class CmsInlineEditOverlay extends Composite implements HasClickHandlers {

    /** The ui binder. */
    interface I_CmsInlineEditOverlayUiBinder extends UiBinder<HTMLPanel, CmsInlineEditOverlay> {
        // nothing to do
    }

    /** The width rquired by the button bar. */
    private static final int BUTTON_BAR_WIDTH = 28;

    /** List of present overlays. */
    private static List<CmsInlineEditOverlay> m_overlays = new ArrayList<CmsInlineEditOverlay>();

    /** The ui binder instance. */
    private static I_CmsInlineEditOverlayUiBinder uiBinder = GWT.create(I_CmsInlineEditOverlayUiBinder.class);

    /** Bottom border. */
    @UiField
    protected Element m_borderBottom;

    /** Left border. */
    @UiField
    protected Element m_borderLeft;

    /** Right border. */
    @UiField
    protected Element m_borderRight;

    /** Top border. */
    @UiField
    protected Element m_borderTop;

    /** The button bar element. */
    @UiField
    protected Element m_buttonBar;

    /** Edit overlay. */
    @UiField
    protected Element m_overlayBottom;

    /** Edit overlay. */
    @UiField
    protected Element m_overlayLeft;

    /** Edit overlay. */
    @UiField
    protected Element m_overlayRight;

    /** Edit overlay. */
    @UiField
    protected Element m_overlayTop;

    /** The edit button panel. */
    @UiField
    FlowPanel m_buttonPanel;

    /** Style of border. */
    private Style m_borderBottomStyle;

    /** Style of border. */
    private Style m_borderLeftStyle;

    /** Style of border. */
    private Style m_borderRightStyle;

    /** Style of border. */
    private Style m_borderTopStyle;

    /** Map of attached edit buttons and their absolute top positions. */
    private Map<CmsInlineEntityWidget, Integer> m_buttons;

    /** The current overlay position. */
    private CmsPositionBean m_currentPosition;

    /** The element to surround with the overlay. */
    private Element m_element;

    /** Flag indicating this overlay has a button bar. */
    private boolean m_hasButtonBar;

    /** The main panel. */
    private HTMLPanel m_main;

    /** The overlay offset. */
    private int m_offset = 3;

    /** Style of overlay. */
    private Style m_overlayBottomStyle;

    /** Style of overlay. */
    private Style m_overlayLeftStyle;

    /** Style of overlay. */
    private Style m_overlayRightStyle;

    /** Style of overlay. */
    private Style m_overlayTopStyle;

    /**
     * Constructor.<p>
     *
     * @param element the element to surround with the overlay
     */
    public CmsInlineEditOverlay(Element element) {

        m_main = uiBinder.createAndBindUi(this);
        initWidget(m_main);
        m_element = element;
        m_overlayLeftStyle = m_overlayLeft.getStyle();
        m_overlayBottomStyle = m_overlayBottom.getStyle();
        m_overlayRightStyle = m_overlayRight.getStyle();
        m_overlayTopStyle = m_overlayTop.getStyle();
        m_borderBottomStyle = m_borderBottom.getStyle();
        m_borderLeftStyle = m_borderLeft.getStyle();
        m_borderRightStyle = m_borderRight.getStyle();
        m_borderTopStyle = m_borderTop.getStyle();
        m_buttonBar.getStyle().setDisplay(Display.NONE);
        m_buttonPanel.addDomHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                // prevent the click event to propagated from the button panel to the main widget
                event.stopPropagation();
            }
        }, ClickEvent.getType());
        m_buttons = new HashMap<CmsInlineEntityWidget, Integer>();
    }

    /**
     * Adds an overlay surrounding the given DOM element.<p>
     *
     * @param element the element
     *
     * @return the overlay widget
     */
    public static CmsInlineEditOverlay addOverlayForElement(Element element) {

        CmsInlineEditOverlay overlay = new CmsInlineEditOverlay(element);
        if (!m_overlays.isEmpty()) {
            m_overlays.get(m_overlays.size() - 1).setVisible(false);
        }
        m_overlays.add(overlay);
        RootPanel.get().add(overlay);
        overlay.updatePosition();
        overlay.checkZIndex();
        return overlay;
    }

    /**
     * Returns the root overlay if available.<p>
     *
     * @return the root overlay
     */
    public static CmsInlineEditOverlay getRootOverlay() {

        return m_overlays.isEmpty() ? null : m_overlays.get(0);
    }

    /**
     * Removes all present overlays.<p>
     */
    public static void removeAll() {

        for (CmsInlineEditOverlay overlay : m_overlays) {
            overlay.removeFromParent();
        }
        m_overlays.clear();
    }

    /**
     * Removes the last overlay to display the previous or none.<p>
     */
    public static void removeLastOverlay() {

        if (!m_overlays.isEmpty()) {
            CmsInlineEditOverlay last = m_overlays.remove(m_overlays.size() - 1);
            last.removeFromParent();
        }
        if (!m_overlays.isEmpty()) {
            m_overlays.get(m_overlays.size() - 1).setVisible(true);
        }
    }

    /**
     * Updates the current overlay's position.<p>
     */
    public static void updateCurrentOverlayPosition() {

        if (!m_overlays.isEmpty()) {
            m_overlays.get(m_overlays.size() - 1).updatePosition();
        }
    }

    /**
     * Adds a button widget to the button panel.<p>
     *
     * @param widget the button widget
     * @param absoluteTop the absolute top position
     */
    public void addButton(CmsInlineEntityWidget widget, int absoluteTop) {

        setButtonBarVisible(true);
        m_buttonPanel.add(widget);
        setButtonPosition(widget, absoluteTop);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * Increases the overlay z-index if necessary.<p>
     */
    public void checkZIndex() {

        int zIndex = 100000;
        Element parent = m_element.getParentElement();
        while (parent != null) {
            int parentIndex = CmsDomUtil.getCurrentStyleInt(parent, CmsDomUtil.Style.zIndex);
            if (parentIndex > zIndex) {
                zIndex = parentIndex;
            }
            parent = parent.getParentElement();
        }
        if (zIndex > 100000) {
            getElement().getStyle().setZIndex(zIndex);
        }
    }

    /**
     * Clears and hides the button panel.<p>
     */
    public void clearButtonPanel() {

        m_buttonPanel.clear();
        m_buttons.clear();
        setButtonBarVisible(false);
    }

    /**
     * Updates the position of the given button widget.<p>
     *
     * @param widget the button widget
     * @param absoluteTop the top absolute top position
     */
    public void setButtonPosition(CmsInlineEntityWidget widget, int absoluteTop) {

        if (m_buttonPanel.getWidgetIndex(widget) > -1) {
            int buttonBarTop = CmsClientStringUtil.parseInt(m_buttonBar.getStyle().getTop());
            if (absoluteTop < buttonBarTop) {
                absoluteTop = buttonBarTop;
            }
            int positionTop = getAvailablePosition(widget, absoluteTop) - buttonBarTop;
            widget.getElement().getStyle().setTop(positionTop, Unit.PX);
            if (CmsClientStringUtil.parseInt(m_buttonBar.getStyle().getHeight()) < (positionTop + 20)) {
                increaseOverlayHeight(positionTop + 20);
            }
        }
    }

    /**
     * Sets the overlay offset.<p>
     *
     * @param offset the offset
     */
    public void setOffset(int offset) {

        m_offset = offset;
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);
        if (!visible && m_hasButtonBar) {
            for (Widget widget : m_buttonPanel) {
                if (widget instanceof CmsInlineEntityWidget) {
                    ((CmsInlineEntityWidget)widget).setContentHighlightingVisible(false);
                }
            }
        }
    }

    /**
     * Updates the overlay position.<p>
     */
    public void updatePosition() {

        setPosition(CmsPositionBean.getBoundingClientRect(m_element));
        for (Widget widget : m_buttonPanel) {
            if (widget instanceof CmsInlineEntityWidget) {
                ((CmsInlineEntityWidget)widget).positionWidget();
            }
        }
    }

    /**
     * Returns the available absolute top position for the given button.<p>
     *
     * @param widget the button widget
     * @param absoluteTop the proposed position
     *
     * @return the available position
     */
    private int getAvailablePosition(CmsInlineEntityWidget widget, int absoluteTop) {

        m_buttons.remove(widget);
        boolean positionBlocked = true;
        while (positionBlocked) {
            positionBlocked = false;
            for (int pos : m_buttons.values()) {
                if (((pos - 24) < absoluteTop) && (absoluteTop < (pos + 24))) {
                    positionBlocked = true;
                    absoluteTop = pos + 25;
                    break;
                }
            }
        }
        m_buttons.put(widget, Integer.valueOf(absoluteTop));
        return absoluteTop;
    }

    /**
     * Increases the overlay height to make space for edit buttons.<p>
     *
     * @param height the height to set
     */
    private void increaseOverlayHeight(int height) {

        if (m_currentPosition != null) {
            m_currentPosition.setHeight(height);
            setPosition(m_currentPosition);
        }
    }

    /**
     * Sets button bar visibility.<p>
     *
     * @param visible <code>true</code> to set the button bar visible
     */
    private void setButtonBarVisible(boolean visible) {

        if (m_hasButtonBar != visible) {
            m_hasButtonBar = visible;
            if (m_hasButtonBar) {

                m_buttonBar.getStyle().clearDisplay();
                int width = CmsClientStringUtil.parseInt(m_borderTopStyle.getWidth()) + BUTTON_BAR_WIDTH;
                m_borderTopStyle.setWidth(width, Unit.PX);
                m_borderBottomStyle.setWidth(width, Unit.PX);
                m_borderRightStyle.setLeft(
                    CmsClientStringUtil.parseInt(m_borderRightStyle.getLeft()) + BUTTON_BAR_WIDTH,
                    Unit.PX);
            } else {
                m_buttonBar.getStyle().setDisplay(Display.NONE);
                int width = CmsClientStringUtil.parseInt(m_borderTopStyle.getWidth()) - BUTTON_BAR_WIDTH;
                m_borderTopStyle.setWidth(width, Unit.PX);
                m_borderBottomStyle.setWidth(width, Unit.PX);
                m_borderRightStyle.setLeft(
                    CmsClientStringUtil.parseInt(m_borderRightStyle.getLeft()) - BUTTON_BAR_WIDTH,
                    Unit.PX);
            }
        }
    }

    /**
     * Sets position and size of the overlay area.<p>
     *
     * @param position the position of highlighted area
     */
    private void setPosition(CmsPositionBean position) {

        m_currentPosition = position;
        setSelectPosition(position.getLeft(), position.getTop(), position.getHeight(), position.getWidth());
    }

    /**
     * Sets position and size of the overlay area.<p>
     *
     * @param posX the new X position
     * @param posY the new Y position
     * @param height the new height
     * @param width the new width
     */
    private void setSelectPosition(int posX, int posY, int height, int width) {

        int useWidth = Window.getClientWidth();
        int bodyWidth = RootPanel.getBodyElement().getClientWidth() + RootPanel.getBodyElement().getOffsetLeft();
        if (bodyWidth > useWidth) {
            useWidth = bodyWidth;
        }
        int useHeight = Window.getClientHeight();
        int bodyHeight = RootPanel.getBodyElement().getClientHeight() + RootPanel.getBodyElement().getOffsetTop();
        if (bodyHeight > useHeight) {
            useHeight = bodyHeight;
        }

        m_overlayLeftStyle.setWidth(posX - m_offset, Unit.PX);
        m_overlayLeftStyle.setHeight(useHeight, Unit.PX);

        m_borderLeftStyle.setHeight(height + (4 * m_offset), Unit.PX);
        m_borderLeftStyle.setTop(posY - (2 * m_offset), Unit.PX);
        m_borderLeftStyle.setLeft(posX - (2 * m_offset), Unit.PX);

        m_overlayTopStyle.setLeft(posX - m_offset, Unit.PX);
        m_overlayTopStyle.setWidth(width + (2 * m_offset), Unit.PX);
        m_overlayTopStyle.setHeight(posY - m_offset, Unit.PX);

        m_borderTopStyle.setLeft(posX - m_offset, Unit.PX);
        m_borderTopStyle.setTop(posY - (2 * m_offset), Unit.PX);
        if (m_hasButtonBar) {
            m_borderTopStyle.setWidth(width + (2 * m_offset) + BUTTON_BAR_WIDTH, Unit.PX);
        } else {
            m_borderTopStyle.setWidth(width + (2 * m_offset), Unit.PX);
        }

        m_overlayBottomStyle.setLeft(posX - m_offset, Unit.PX);
        m_overlayBottomStyle.setWidth(width + m_offset + m_offset, Unit.PX);
        m_overlayBottomStyle.setHeight(useHeight - posY - height - m_offset, Unit.PX);
        m_overlayBottomStyle.setTop(posY + height + m_offset, Unit.PX);

        m_borderBottomStyle.setLeft(posX - m_offset, Unit.PX);
        m_borderBottomStyle.setTop((posY + height) + m_offset, Unit.PX);
        if (m_hasButtonBar) {
            m_borderBottomStyle.setWidth(width + (2 * m_offset) + BUTTON_BAR_WIDTH, Unit.PX);
        } else {
            m_borderBottomStyle.setWidth(width + (2 * m_offset), Unit.PX);
        }

        m_overlayRightStyle.setLeft(posX + width + m_offset, Unit.PX);
        m_overlayRightStyle.setWidth(useWidth - posX - width - m_offset, Unit.PX);
        m_overlayRightStyle.setHeight(useHeight, Unit.PX);

        m_borderRightStyle.setHeight(height + (4 * m_offset), Unit.PX);
        m_borderRightStyle.setTop(posY - (2 * m_offset), Unit.PX);
        if (m_hasButtonBar) {
            m_borderRightStyle.setLeft(posX + width + m_offset + BUTTON_BAR_WIDTH, Unit.PX);
        } else {
            m_borderRightStyle.setLeft(posX + width + m_offset, Unit.PX);
        }

        m_buttonBar.getStyle().setTop(posY - m_offset, Unit.PX);
        m_buttonBar.getStyle().setHeight(height + (2 * m_offset), Unit.PX);
        m_buttonBar.getStyle().setLeft(posX + width + m_offset + 1, Unit.PX);
    }
}
