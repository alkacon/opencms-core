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

import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.common.base.Optional;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a UI list item.<p>
 *
 * @since 8.0.0
 */
public class CmsSimpleListItem extends Composite implements I_CmsListItem {

    /** The logical id, it is not the HTML id. */
    protected String m_id;

    /** The underlying panel. */
    protected CmsFlowPanel m_panel;

    /**
     * Constructor.<p>
     */
    public CmsSimpleListItem() {

        m_panel = new CmsFlowPanel(CmsDomUtil.Tag.li.name());
        m_panel.setStyleName(I_CmsLayoutBundle.INSTANCE.listTreeCss().listTreeItem());
        initWidget(m_panel);
    }

    /**
     * Constructor.<p>
     *
     * @param widget the widget to use
     */
    public CmsSimpleListItem(CmsListItemWidget widget) {

        this();
        add(widget);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        m_panel.add(w);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getCursorOffsetDelta()
     */
    public Optional<int[]> getCursorOffsetDelta() {

        return Optional.absent();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getDragHelper(org.opencms.gwt.client.dnd.I_CmsDropTarget target) {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getParentTarget()
     */
    public I_CmsDropTarget getParentTarget() {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getPlaceholder(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public Element getPlaceholder(org.opencms.gwt.client.dnd.I_CmsDropTarget target) {

        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * Returns the child widget with the given index.<p>
     *
     * @param index the index
     *
     * @return the child widget
     */
    public Widget getWidget(int index) {

        return m_panel.getWidget(index);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    public void onDragCancel() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onDrop(org.opencms.gwt.client.dnd.I_CmsDropTarget target) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void onStartDrag(org.opencms.gwt.client.dnd.I_CmsDropTarget target) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsListItem#setId(java.lang.String)
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        for (Widget widget : m_panel) {
            if (!(widget instanceof I_CmsTruncable)) {
                continue;
            }
            int width = widgetWidth - 4; // just to be on the safe side
            if (widget instanceof CmsList<?>) {
                width -= 25; // 25px left margin
            }
            ((I_CmsTruncable)widget).truncate(textMetricsPrefix, width);
        }
    }

}
