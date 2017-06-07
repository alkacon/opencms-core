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

import org.opencms.gwt.client.ui.css.I_CmsFloatDecoratedPanelCss;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used for laying out multiple widgets horizontally.<p>
 *
 * It contains two panels, the "primary" (or main) panel and the "float" panel,
 * to which widgets can be added. The float panel is styled so as to
 * float left of the primary panel, and the primary panel's left margin is
 * set to the width of the float panel. If the widget starts out as hidden,
 * the float panel width can not be measured, so you have to call the updateLayout
 * method  manually when the widget becomes visible.
 *
 * @since 8.0.0
 */
public class CmsFloatDecoratedPanel extends Composite implements I_CmsTruncable {

    /** Css resource for this widget. */
    static final I_CmsFloatDecoratedPanelCss CSS = I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss();

    /** The float panel. */
    private FlowPanel m_floatBox = new FlowPanel();

    /** The panel containing both the main and float panel. */
    private FlowPanel m_panel = new FlowPanel();

    /** The main panel. */
    private FlowPanel m_primary = new FlowPanel();

    /**
     * Creates a new instance of the widget.
     */
    public CmsFloatDecoratedPanel() {

        m_panel.setStyleName(CSS.floatDecoratedPanel());
        m_floatBox.setStyleName(CSS.floatBox());
        m_primary.setStyleName(CSS.primary());

        m_panel.add(m_floatBox);
        m_panel.add(m_primary);
        m_floatBox.getElement().getStyle().setFloat(Float.LEFT);
        initWidget(m_panel);
        // we only make the widget visible after the layout has been updated to prevent "flickering"
        getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

    /**
     * Adds a widget to the main panel.<p>
     *
     * @param widget the widget to add
     */
    public void add(Widget widget) {

        m_primary.add(widget);
    }

    /**
     * Adds a widget to the float panel.<p>
     *
     * @param widget the widget to add
     */
    public void addToFloat(Widget widget) {

        m_floatBox.add(widget);
        updateLayout();
    }

    /**
     * Adds a widget to the front of the float panel.<p>
     *
     * @param widget the widget to add
     */
    public void addToFrontOfFloat(Widget widget) {

        m_floatBox.insert(widget, 0);
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
        width -= (!isAttached() ? 30 * m_floatBox.getWidgetCount() : getFloatBoxWidth());
        for (Widget widget : m_primary) {
            if (widget instanceof I_CmsTruncable) {
                ((I_CmsTruncable)widget).truncate(textMetricsPrefix, width);
            }
        }
    }

    /**
     * Sets the left margin of the main panel to the width of the float panel.<p>
     */
    public void updateLayout() {

        // TODO: we should not do this kind of things...
        if (!isAttached()) {
            return;
        }
        int floatBoxWidth = getFloatBoxWidth();
        m_primary.getElement().getStyle().setMarginLeft(floatBoxWidth, Unit.PX);
        updateVerticalMargin();
    }

    /**
     * Automatically calls the updateLayout method after insertion into the DOM.<p>
     *
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        /* defer until children have been (hopefully) layouted. */
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                updateLayout();
                // layout update has finished, now it's OK to show the widget
                getElement().getStyle().setVisibility(Visibility.VISIBLE);
            }
        });
    }

    /**
     * Returns the width of the float box.<p>
     *
     * @return a width
     */
    private int getFloatBoxWidth() {

        return m_floatBox.getOffsetWidth();
    }

    /**
     * Updates the vertical margin of the float box such that its vertical middle point coincides
     * with the vertical middle point of the primary panel.<p>
     */
    private void updateVerticalMargin() {

        int floatHeight = m_floatBox.getOffsetHeight();
        int primaryHeight = m_primary.getOffsetHeight();
        int verticalOffset = (primaryHeight - floatHeight) / 2;
        m_floatBox.getElement().getStyle().setMarginTop(verticalOffset, Unit.PX);
    }
}
