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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A row in a properties form.<p>
 *
 * This widget contains both a label and a panel into which an input widget for the form field can be placed.
 * These widgets are next to each other horizontally.
 *
 * @since 8.0.0
 */
public class CmsFormRow extends Composite implements I_CmsTruncable {

    /** The ui binder interface for this widget. */
    protected interface I_CmsFormRowUiBinder extends UiBinder<Widget, CmsFormRow> {
        // uibinder
    }

    /** The width of the label. */
    public static final int LABEL_WIDTH = 160;

    /** The width of the opener. */
    public static final int OPENER_WIDTH = 16;

    /** The default widget container width. */
    public static final int WIDGET_CONTAINER_WIDTH = 370;

    /** The required right margin. */
    public static final int WIDGET_MARGIN_RIGHT = 30;

    /** The CSS bundle used for this widget. */
    protected static I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** The ui binder instance for this form row. */
    private static I_CmsFormRowUiBinder uiBinder = GWT.create(I_CmsFormRowUiBinder.class);

    /** The label used for displaying the information icon. */
    @UiField
    protected Label m_icon;

    /** The label for the form row. */
    @UiField
    protected Label m_label;

    /** The widget container for the form row. */
    @UiField
    protected Panel m_widgetContainer;

    /**
     * The default constructor.
     */
    public CmsFormRow() {

        Widget main = uiBinder.createAndBindUi(this);
        initWidget(main);
        main.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
    }

    /**
     * Returns the width of the label as a string.<p>
     *
     * @return the width of the label as a string
     */
    public static String getLabelWidth() {

        return LABEL_WIDTH + "px";
    }

    /**
     * Returns the width of the opener as a string.<p>
     *
     * @return the width of the opener as a string
     */
    public static String getOpenerWidth() {

        return OPENER_WIDTH + "px";
    }

    /**
     * Returns the left margin of the widget container as a string.<p>
     *
     * @return the left margin of the widget container as a string
     */
    public static String getWidgetContainerLeftMargin() {

        return OPENER_WIDTH + LABEL_WIDTH + "px";
    }

    /**
     * Returns the left margin of the widget container as a string.<p>
     *
     * @return the left margin of the widget container as a string
     */
    public static String getWidgetContainerWidth() {

        return WIDGET_CONTAINER_WIDTH + "px";
    }

    /**
     * Returns the label for the form row.<p>
     *
     * @return the label for the form row
     */
    public Label getLabel() {

        return m_label;
    }

    /**
     * Returns the widget container for the form row.<p>
     *
     * @return the widget container for the form row
     */
    public Panel getWidgetContainer() {

        return m_widgetContainer;
    }

    /**
     * Shows the info icon and sets the information text as its title.<p>
     *
     * @param info the info
     */
    public void setInfo(String info) {

        if (info != null) {
            m_icon.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().inherited());
            m_icon.addStyleName(I_CmsButton.ICON_FONT);
            m_icon.addStyleName(I_CmsButton.CIRCLE_INFO);
            m_icon.setTitle(info);
        }

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        if (clientWidth > (WIDGET_CONTAINER_WIDTH + OPENER_WIDTH + LABEL_WIDTH + WIDGET_MARGIN_RIGHT)) {
            int availableWidth = clientWidth - OPENER_WIDTH - WIDGET_MARGIN_RIGHT;
            int widgetContainerWidth = (int)Math.round(
                1.00 * availableWidth * ((1.00 + WIDGET_CONTAINER_WIDTH) / (WIDGET_CONTAINER_WIDTH + LABEL_WIDTH)));
            int labelWidth = availableWidth - widgetContainerWidth;
            m_label.setWidth(labelWidth + "px");
            m_widgetContainer.setWidth(widgetContainerWidth + "px");
            m_widgetContainer.getElement().getStyle().setMarginLeft(labelWidth + OPENER_WIDTH, Unit.PX);
            for (Widget widget : m_widgetContainer) {
                if (widget instanceof I_CmsTruncable) {
                    ((I_CmsTruncable)widget).truncate(textMetricsKey, widgetContainerWidth);
                }
            }
        }
    }

}
