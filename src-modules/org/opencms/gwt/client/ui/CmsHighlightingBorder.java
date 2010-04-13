/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsHighlightingBorder.java,v $
 * Date   : $Date: 2010/04/13 09:17:18 $
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
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * A Widget to display a highlighting border around a specified position.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsHighlightingBorder extends Composite {

    /** Enumeration of available border colours. */
    public enum BorderColor {
        /** Color blue. */
        blue(I_CmsLayoutBundle.INSTANCE.highlightCss().colorBlue()),

        /** Color red. */
        red(I_CmsLayoutBundle.INSTANCE.highlightCss().colorRed());

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

    /** The border offset to the given position. */
    private static final int BORDER_OFFSET = 4;

    /** The ui-binder instance. */
    private static I_CmsHighlightingBorderUiBinder uiBinder = GWT.create(I_CmsHighlightingBorderUiBinder.class);

    /**
     * Constructor.<p>
     * 
     * @param position the position data
     * @param color the border colour
     */
    public CmsHighlightingBorder(CmsPositionBean position, BorderColor color) {

        this(position.getHeight(), position.getWidth(), position.getLeft(), position.getTop(), color);
    }

    /**
     * Constructor.<p>
     * 
     * @param height the height
     * @param width the width
     * @param positionLeft the absolute left position
     * @param positionTop the absolute top position
     * @param color the border colour
     */
    public CmsHighlightingBorder(int height, int width, int positionLeft, int positionTop, BorderColor color) {

        initWidget(uiBinder.createAndBindUi(this));

        getWidget().addStyleName(color.getCssClass());
        Style style = getElement().getStyle();
        style.setLeft(positionLeft - BORDER_OFFSET, Unit.PX);
        style.setTop(positionTop - BORDER_OFFSET, Unit.PX);
        style.setHeight(height + 2 * BORDER_OFFSET, Unit.PX);
        style.setWidth(width + 2 * BORDER_OFFSET, Unit.PX);
    }

    /**
     * Hides the border.<p>
     */
    public void hide() {

        setVisible(false);
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
     * @param positionLeft the absolute left position
     * @param positionTop the absolute top position
     */
    public void setPosition(int positionLeft, int positionTop) {

        Style style = getElement().getStyle();
        style.setLeft(positionLeft - BORDER_OFFSET, Unit.PX);
        style.setTop(positionTop - BORDER_OFFSET, Unit.PX);
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

        Style style = getElement().getStyle();
        style.setLeft(positionLeft - BORDER_OFFSET, Unit.PX);
        style.setTop(positionTop - BORDER_OFFSET, Unit.PX);
        style.setHeight(height + 2 * BORDER_OFFSET, Unit.PX);
        style.setWidth(width + 2 * BORDER_OFFSET, Unit.PX);
    }

    /**
     * Shows the border.<p>
     */
    public void show() {

        setVisible(true);
    }

}
