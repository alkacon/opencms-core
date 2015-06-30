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

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.I_HasResizeOnShow;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The attribute values panel.<p>
 */
public class CmsValuePanel extends FlowPanel implements I_CmsDropTarget, I_HasResizeOnShow {

    /** The current place holder. */
    protected Element m_placeholder;

    /** The placeholder position index. */
    protected int m_placeholderIndex = -1;

    /** The highlighting border. */
    private CmsHighlightingBorder m_highlighting;

    /**
     * Constructor.<p>
     */
    public CmsValuePanel() {

        setStyleName(I_CmsLayoutBundle.INSTANCE.form().attribute());
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
     */
    public boolean checkPosition(int x, int y, Orientation orientation) {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;
    }

    /**
     * Highlights the outline of this panel.<p>
     */
    public void highlightOutline() {

        m_highlighting = new CmsHighlightingBorder(getElement(), CmsHighlightingBorder.BorderColor.red);
        add(m_highlighting);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
     */
    public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

        m_placeholder = placeholder;
        repositionPlaceholder(x, y, orientation);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
     */
    public void onDrop(I_CmsDraggable draggable) {

        // nothing to do
    }

    /**
     * Removes the highlighting border.<p>
     */
    public void removeHighlighting() {

        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#removePlaceholder()
     */
    public void removePlaceholder() {

        if (m_placeholder == null) {
            return;
        }
        m_placeholder.removeFromParent();
        m_placeholder = null;
        m_placeholderIndex = -1;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int, org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation)
     */
    public void repositionPlaceholder(int x, int y, Orientation orientation) {

        // handle vertical orientation only
        m_placeholderIndex = CmsDomUtil.positionElementInside(m_placeholder, getElement(), m_placeholderIndex, -1, y);
    }

    /**
     * @see org.opencms.gwt.client.I_HasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        for (Widget w : this) {
            if (w instanceof I_HasResizeOnShow) {
                ((I_HasResizeOnShow)w).resizeOnShow();
            }
        }
    }

    /**
     * Updates the highlighting position if present.<p>
     */
    public void updateHighlightingPosition() {

        if (m_highlighting != null) {
            m_highlighting.resetPosition();
        }
    }
}
