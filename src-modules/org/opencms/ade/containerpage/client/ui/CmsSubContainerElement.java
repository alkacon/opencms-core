/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsSubContainerElement.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sub-container element. To be used for content elements within a container-page.<p>
 * The sub-container acts as a draggable element and if edited as a container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSubContainerElement extends CmsContainerPageElement implements I_CmsDropContainer {

    /** The container type. */
    private String m_containerId;

    /** The placeholder element. */
    private Element m_placeholder;

    /** The index of the current placeholder position. */
    private int m_placeholderIndex = -1;

    /**
     * Constructor.<p>
     * 
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     * @param hasProps should be true if the element has properties which can be edited 
     */
    public CmsSubContainerElement(
        Element element,
        I_CmsDropTarget parent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        super(element, parent, clientId, sitePath, noEditReason, hasProps);

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int)
     */
    public boolean checkPosition(int x, int y) {

        Element element = getElement();
        // check if the mouse pointer is within the width of the target 
        int left = CmsDomUtil.getRelativeX(x, element);
        int offsetWidth = element.getOffsetWidth();
        if ((left <= 0) || (left >= offsetWidth)) {
            return false;
        }

        // check if the mouse pointer is within the height of the target 
        int top = CmsDomUtil.getRelativeY(y, element);
        int offsetHeight = element.getOffsetHeight();
        if ((top <= 0) || (top >= offsetHeight)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the container id.<p>
     *
     * @return the container id
     */
    public String getContainerId() {

        return m_containerId;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;
    }

    /**
     * Puts a highlighting border around the container content.<p>
     */
    public void highlightContainer() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

        // adding the 'clearFix' style to all targets containing floated elements
        // in some layouts this may lead to inappropriate clearing after the target, 
        // but it is still necessary as it forces the target to enclose it's floated content 
        if ((getWidgetCount() > 0)
            && !CmsDomUtil.getCurrentStyle(getWidget(0).getElement(), CmsDomUtil.Style.floatCss).equals(
                CmsDomUtil.StyleValue.none.toString())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
        }
        m_highlighting = new CmsHighlightingBorder(
            CmsPositionBean.getInnerDimensions(getElement()),
            CmsHighlightingBorder.BorderColor.red);
        RootPanel.get().add(m_highlighting);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int)
     */
    public void insertPlaceholder(Element placeholder, int x, int y) {

        m_placeholder = placeholder;
        repositionPlaceholder(x, y);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
     */
    public void onDrop(I_CmsDraggable draggable) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#refreshHighlighting()
     */
    public void refreshHighlighting() {

        CmsPositionBean position = CmsPositionBean.getInnerDimensions(getElement());
        m_placeholder.getStyle().setHeight(position.getHeight() + 10, Unit.PX);
        m_highlighting.setPosition(position);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.CmsContainerPageElement#removeHighlighting()
     */
    @Override
    public void removeHighlighting() {

        if (m_placeholder != null) {
            m_placeholder.getStyle().clearHeight();
        }
        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }

        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#removePlaceholder()
     */
    public void removePlaceholder() {

        if (m_placeholder != null) {
            m_placeholder.removeFromParent();
            m_placeholder = null;
        }
        m_placeholderIndex = -1;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int)
     */
    public void repositionPlaceholder(int x, int y) {

        m_placeholderIndex = CmsDomUtil.positionElementInside(m_placeholder, getElement(), m_placeholderIndex, x, y);
    }

    /**
     * Sets the container id.<p>
     *
     * @param containerId the container id to set
     */
    public void setContainerId(String containerId) {

        m_containerId = containerId;
    }

}
