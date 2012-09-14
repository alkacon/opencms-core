/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.client;

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * In-line edit overlay covering rest of the page.<p>
 */
public class CmsInlineEditOverlay extends Composite implements HasClickHandlers {

    /** The ui binder. */
    interface I_CmsInlineEditOverlayUiBinder extends UiBinder<Widget, CmsInlineEditOverlay> {
        // nothing to do
    }

    /** The ui binder instance. */
    private static I_CmsInlineEditOverlayUiBinder uiBinder = GWT.create(I_CmsInlineEditOverlayUiBinder.class);

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

    /** The element to surround with the overlay. */
    private Element m_element;

    /** The overlay offset. */
    private int m_offset = 3;

    /** Style of image overlay. */
    private Style m_overlayBottomStyle;

    /** Style of image overlay. */
    private Style m_overlayLeftStyle;

    /** Style of image overlay. */
    private Style m_overlayRightStyle;

    /** Style of image overlay. */
    private Style m_overlayTopStyle;

    /**
     * Constructor.<p>
     * 
     * @param element the element to surround with the overlay
     */
    public CmsInlineEditOverlay(Element element) {

        initWidget(uiBinder.createAndBindUi(this));
        m_element = element;
        m_overlayLeftStyle = m_overlayLeft.getStyle();
        m_overlayBottomStyle = m_overlayBottom.getStyle();
        m_overlayRightStyle = m_overlayRight.getStyle();
        m_overlayTopStyle = m_overlayTop.getStyle();
    }

    /**
     * Increases the overlay z-index if necessary.<p>
     */
    public void checkZIndex() {

        int zIndex = 100000;
        Element parent = m_element.getParentElement();
        while (parent != null) {
            int parentIndex = CmsDomUtil.getCurrentStyleInt(parent, org.opencms.gwt.client.util.CmsDomUtil.Style.zIndex);
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
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
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
     * Updates the overlay position.<p>
     */
    public void updatePosition() {

        setPosition(CmsPositionBean.getInnerDimensions(m_element));
    }

    /**
     * Sets position and size of the overlay area.<p>
     * 
     * @param position the position of highlighted area
     */
    private void setPosition(CmsPositionBean position) {

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

        m_overlayTopStyle.setLeft(posX - m_offset, Unit.PX);
        m_overlayTopStyle.setWidth(width + m_offset + m_offset, Unit.PX);
        m_overlayTopStyle.setHeight(posY - m_offset, Unit.PX);

        m_overlayBottomStyle.setLeft(posX - m_offset, Unit.PX);
        m_overlayBottomStyle.setWidth(width + m_offset + m_offset, Unit.PX);
        m_overlayBottomStyle.setHeight(useHeight - posY - height - m_offset, Unit.PX);
        m_overlayBottomStyle.setTop(posY + height + m_offset, Unit.PX);

        m_overlayRightStyle.setLeft(posX + width + m_offset, Unit.PX);
        m_overlayRightStyle.setWidth(useWidth - posX - width - m_offset, Unit.PX);
        m_overlayRightStyle.setHeight(useHeight, Unit.PX);
    }
}
