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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A Widget to display a highlighting border around a specified position.<p>
 *
 * @since 8.0.0
 */
public class CmsHighlightingBorder extends Composite {

    /** Enumeration of available border colours. */
    public enum BorderColor {
        /** Color blue. */
        blue(I_CmsLayoutBundle.INSTANCE.highlightCss().colorBlue()),

        /** Color grey. */
        grey(I_CmsLayoutBundle.INSTANCE.highlightCss().colorGrey()),

        /** Color red. */
        red(I_CmsLayoutBundle.INSTANCE.highlightCss().colorRed()),

        /** Solid grey. */
        solidGrey(I_CmsLayoutBundle.INSTANCE.highlightCss().colorSolidGrey());

        /** CSS class used to display the border colour. */
        private String m_cssClass;

        /**
         * Constructor.<p>
         *
         * @param cssClass the CSS class to display the border colour
         */
        private BorderColor(String cssClass) {

            m_cssClass = cssClass;
        }

        /**
         * Returns the associated CSS class.<p>
         *
         * @return the CSS class
         */
        public String getCssClass() {

            return m_cssClass;
        }
    }

    /** The ui-binder interface for this composite. */
    interface I_CmsHighlightingBorderUiBinder extends UiBinder<HTML, CmsHighlightingBorder> {
        // GWT interface, nothing to do here
    }

    /** The default border offset to the given position. */
    private static final int BORDER_OFFSET = 4;

    /** The border width. */
    private static final int BORDER_WIDTH = 2;

    /** The ui-binder instance. */
    private static I_CmsHighlightingBorderUiBinder uiBinder = GWT.create(I_CmsHighlightingBorderUiBinder.class);

    /** The bottom border. */
    @UiField
    protected DivElement m_borderBottom;

    /** The left border. */
    @UiField
    protected DivElement m_borderLeft;

    /** The right border. */
    @UiField
    protected DivElement m_borderRight;

    /** The top border. */
    @UiField
    protected DivElement m_borderTop;

    /** The border offset. */
    private int m_borderOffset;

    /** The style variable used to change the color of the border. */
    private CmsStyleVariable m_colorVariable;

    /** The positioning parent element. */
    private Element m_positioningParent;

    /**
     * Constructor.<p>
     *
     * @param position the position data
     * @param color the border color
     */
    public CmsHighlightingBorder(CmsPositionBean position, BorderColor color) {

        this(position.getHeight(), position.getWidth(), position.getLeft(), position.getTop(), color, BORDER_OFFSET);
    }

    /**
     * Constructor.<p>
     *
     * @param position the position data
     * @param color the border color
     * @param borderOffset the border offset
     */
    public CmsHighlightingBorder(CmsPositionBean position, BorderColor color, int borderOffset) {

        this(position.getHeight(), position.getWidth(), position.getLeft(), position.getTop(), color, borderOffset);
    }

    /**
     * Constructor.<p>
     *
     * @param positioningParent the element the border is positioned around, position is set relative to it
     * @param color the border color
     */
    public CmsHighlightingBorder(Element positioningParent, BorderColor color) {

        m_borderOffset = BORDER_OFFSET;
        initWidget(uiBinder.createAndBindUi(this));
        m_colorVariable = new CmsStyleVariable(getWidget());
        m_colorVariable.setValue(color.getCssClass());
        m_positioningParent = positioningParent;
        resetPosition();
    }

    /**
     * Constructor.<p>
     *
     * @param height the height
     * @param width the width
     * @param positionLeft the absolute left position
     * @param positionTop the absolute top position
     * @param color the border color
     * @param borderOffset the border offset
     */
    public CmsHighlightingBorder(
        int height,
        int width,
        int positionLeft,
        int positionTop,
        BorderColor color,
        int borderOffset) {

        m_borderOffset = borderOffset;
        initWidget(uiBinder.createAndBindUi(this));
        m_colorVariable = new CmsStyleVariable(getWidget());
        m_colorVariable.setValue(color.getCssClass());
        setPosition(height, width, positionLeft, positionTop);
    }

    /**
     * Enables the border animation.<p>
     * (Is enabled by default)<p>
     *
     * @param animated <code>true</code> to enable border animation
     */
    public void enableAnimation(boolean animated) {

        if (animated) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.highlightCss().animated());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.highlightCss().animated());
        }
    }

    /**
     * Hides the border.<p>
     */
    public void hide() {

        setVisible(false);
    }

    /**
     * Recalculates the position and dimension when a positioning parent is given.<p>
     */
    public void resetPosition() {

        // fail if no positioning parent given
        assert m_positioningParent != null;
        if (m_positioningParent != null) {
            setPosition(m_positioningParent.getOffsetHeight(), m_positioningParent.getOffsetWidth(), 0, 0);
        }
    }

    /**
     * Sets the color of the border.<p>
     *
     * @param color the color of the border
     */
    public void setColor(BorderColor color) {

        m_colorVariable.setValue(color.getCssClass());
    }

    /**
     * Sets the border position.<p>
     *
     * @param position the position data
     */
    public void setPosition(CmsPositionBean position) {

        setPosition(position.getHeight(), position.getWidth(), position.getLeft(), position.getTop());
    }

    /**
     * Sets the border position.<p>
     *
     * @param height the height
     * @param width the width
     * @param positionLeft the absolute left position
     * @param positionTop the absolute top position
     */
    public void setPosition(int height, int width, int positionLeft, int positionTop) {

        positionLeft -= m_borderOffset;

        // make sure highlighting does not introduce additional horizontal scroll-bars
        if ((m_positioningParent == null) && (positionLeft < 0)) {
            // position left should not be negative
            width += positionLeft;
            positionLeft = 0;
        }
        width += (2 * m_borderOffset) - BORDER_WIDTH;
        if ((m_positioningParent == null)
            && (Window.getClientWidth() < (width + positionLeft))
            && (Window.getScrollLeft() == 0)) {
            // highlighting should not extend over the right hand
            width = Window.getClientWidth() - (positionLeft + BORDER_WIDTH);
        }
        Style style = getElement().getStyle();
        style.setLeft(positionLeft, Unit.PX);
        style.setTop(positionTop - m_borderOffset, Unit.PX);
        setHeight((height + (2 * m_borderOffset)) - BORDER_WIDTH);
        setWidth(width);
    }

    /**
     * Shows the border.<p>
     */
    public void show() {

        setVisible(true);
    }

    /**
     * Sets the highlighting height.<p>
     *
     * @param height the height
     */
    private void setHeight(int height) {

        m_borderRight.getStyle().setHeight(height, Unit.PX);
        m_borderLeft.getStyle().setHeight(height, Unit.PX);
        m_borderBottom.getStyle().setTop(height, Unit.PX);
    }

    /**
     * Sets the highlighting width.<p>
     *
     * @param width the width
     */
    private void setWidth(int width) {

        m_borderTop.getStyle().setWidth(width + BORDER_WIDTH, Unit.PX);
        m_borderBottom.getStyle().setWidth(width + BORDER_WIDTH, Unit.PX);
        m_borderRight.getStyle().setLeft(width, Unit.PX);
    }

}
