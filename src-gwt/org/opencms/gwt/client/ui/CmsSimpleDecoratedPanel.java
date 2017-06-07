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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel containing two sub-panels next to each other , one for 'decorations' (check boxes, etc.) and one containing a main widget.<p>
 *
 * This widget does not calculate the width of the decoration panel automatically. You have to pass the appropriate width
 * as a parameter to the constructor.
 *
 * @since 8.0.0
 *
 */
public class CmsSimpleDecoratedPanel extends Composite implements I_CmsTruncable {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsSimpleDecoratedPanelUiBinder extends UiBinder<Widget, CmsSimpleDecoratedPanel> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsSimpleDecoratedPanelUiBinder uiBinder = GWT.create(I_CmsSimpleDecoratedPanelUiBinder.class);

    /** The float panel. */
    @UiField
    protected FlowPanel m_decorationBox = new FlowPanel();

    /** The panel containing both the main and float panel. */
    @UiField
    protected FlowPanel m_panel = new FlowPanel();

    /** The main panel. */
    @UiField
    protected FlowPanel m_primary = new FlowPanel();

    /** The width of the decoration box. */
    private int m_decorationWidth;

    /**
     * Creates a new instance of this widget.<p>
     *
     * @param decorationWidth the width which the decoration box should have
     * @param mainWidget the main widget
     * @param decoration the list of decoration widgets (from left to right)
     */
    public CmsSimpleDecoratedPanel(int decorationWidth, Widget mainWidget, List<Widget> decoration) {

        initWidget(uiBinder.createAndBindUi(this));
        for (Widget widget : decoration) {
            m_decorationBox.add(widget);
        }
        if (mainWidget != null) {
            m_primary.add(mainWidget);
        }
        m_decorationWidth = decorationWidth;
        init();
    }

    /**
     * Adds a style name to the decoration box.<p>
     *
     * @param cssClass the CSS class to add
     */
    public void addDecorationBoxStyle(String cssClass) {

        m_decorationBox.addStyleName(cssClass);
    }

    /**
     * Returns the widget at the given position.<p>
     *
     * @param index the position
     *
     * @return  the widget at the given position
     */
    public Widget getWidget(int index) {

        return m_primary.getWidget(index);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        int width = widgetWidth;
        width -= getDecorationWidth();
        for (Widget widget : m_primary) {
            if (widget instanceof I_CmsTruncable) {
                ((I_CmsTruncable)widget).truncate(textMetricsPrefix, width);
            }
        }
    }

    /**
     * Returns the width of the decoration box.<p>
     *
     * @return the width of the decoration box
     */
    private int getDecorationWidth() {

        return m_decorationWidth;
    }

    /**
     * Internal helper method for initializing the layout of this widget.<p>
     */
    private void init() {

        int decorationWidth = getDecorationWidth();
        m_decorationBox.setWidth(decorationWidth + "px");
        m_primary.getElement().getStyle().setMarginLeft(decorationWidth, Style.Unit.PX);
    }

}
