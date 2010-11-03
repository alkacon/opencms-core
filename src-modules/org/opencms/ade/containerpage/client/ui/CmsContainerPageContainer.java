/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsContainerPageContainer.java,v $
 * Date   : $Date: 2010/11/03 08:33:16 $
 * Version: $Revision: 1.6 $
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
import org.opencms.ade.containerpage.shared.I_CmsContainer;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Container page container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageContainer implements I_CmsDropContainer, HasWidgets {

    /** Container element id. */
    private String m_containerId;

    /** The container type. */
    private String m_containerType;

    /** Highlighting border for this container. */
    private CmsHighlightingBorder m_highlighting;

    /** The maximum number of elements in this container. */
    private int m_maxElements;

    private Widget m_overflowingElement;

    /** The drag and drop placeholder. */
    private Element m_placeholder;

    /** The drag and drop placeholder position index. */
    private int m_placeholderIndex = -1;

    /** This container wrapped in a {@link com.google.gwt.user.client.ui.RootPanel}. */
    private RootPanel m_root;

    /**
     * Constructor.<p>
     * 
     * @param containerData the container data
     */
    public CmsContainerPageContainer(I_CmsContainer containerData) {

        m_root = RootPanel.get(containerData.getName());
        m_containerId = containerData.getName();
        m_containerType = containerData.getType();
        m_maxElements = containerData.getMaxElements();
        m_root.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragTarget());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        m_root.add(w);

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#checkMaxElementsOnEnter()
     */
    public void checkMaxElementsOnEnter() {

        if (getWidgetCount() >= m_maxElements) {
            m_overflowingElement = m_root.getWidget(getWidgetCount() - 1);
            m_overflowingElement.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#checkMaxElementsOnLeave()
     */
    public void checkMaxElementsOnLeave() {

        if (m_overflowingElement != null) {
            m_overflowingElement.getElement().getStyle().clearDisplay();
        }

    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int, Orientation)
     */
    public boolean checkPosition(int x, int y, Orientation orientation) {

        switch (orientation) {
            case HORIZONTAL:
                return CmsDomUtil.checkPositionInside(getElement(), x, -1);
            case VERTICAL:
                return CmsDomUtil.checkPositionInside(getElement(), -1, y);
            case ALL:
            default:
                return CmsDomUtil.checkPositionInside(getElement(), x, y);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    public void clear() {

        m_root.clear();

    }

    /**
     * Returns all contained drag elements.<p>
     * 
     * @return the drag elements
     */
    public List<CmsContainerPageElement> getAllDragElements() {

        List<CmsContainerPageElement> elements = new ArrayList<CmsContainerPageElement>();
        Iterator<Widget> it = m_root.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                elements.add((CmsContainerPageElement)w);
            } else {
                if (CmsDomUtil.hasClass(
                    org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerPlaceholder(),
                    w.getElement())) {
                    CmsDebugLog.getInstance().printLine("Ignoring sub-container placeholder.");
                } else {
                    CmsDebugLog.getInstance().printLine(
                        "WARNING: " + w.toString() + " is no instance of CmsDragContainerElement");
                }
            }
        }
        return elements;
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
     * Returns the container type.<p>
     *
     * @return the container type
     */
    public String getContainerType() {

        return m_containerType;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getElement()
     */
    public Element getElement() {

        return m_root.getElement();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#getWidgetCount()
     */
    public int getWidgetCount() {

        return m_root.getWidgetCount();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
     */
    public int getWidgetIndex(Widget w) {

        return m_root.getWidgetIndex(w);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#hideEditableListButtons()
     */
    public void hideEditableListButtons() {

        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            if (child instanceof CmsContainerPageElement) {
                ((CmsContainerPageElement)child).hideEditableListButtons();
            }
        }
    }

    /**
     * Puts a highlighting border around the container content.<p>
     */
    public void highlightContainer() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

        // adding the 'clearFix' style to all targets containing floated elements
        // in some layouts this may lead to inappropriate clearing after the target, 
        // but it is still necessary as it forces the target to enclose it's floated content 
        if ((m_root.getWidgetCount() > 0)
            && !CmsDomUtil.getCurrentStyle(m_root.getWidget(0).getElement(), CmsDomUtil.Style.floatCss).equals(
                CmsDomUtil.StyleValue.none.toString())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
        }
        m_highlighting = new CmsHighlightingBorder(
            CmsPositionBean.getInnerDimensions(getElement()),
            CmsHighlightingBorder.BorderColor.red);
        RootPanel.get().add(m_highlighting);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#insert(com.google.gwt.user.client.ui.Widget, int)
     */
    public void insert(Widget w, int beforeIndex) {

        m_root.insert(w, beforeIndex);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, Orientation)
     */
    public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

        m_placeholder = placeholder;
        repositionPlaceholder(x, y, orientation);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_root.iterator();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
     */
    public void onDrop(I_CmsDraggable draggable) {

        // nothing to do

    }

    /**
     * Refreshes position and dimension of the highlighting border. Call when anything changed during the drag process.<p>
     */
    public void refreshHighlighting() {

        m_highlighting.setPosition(CmsPositionBean.getInnerDimensions(getElement()));
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget w) {

        return m_root.remove(w);
    }

    /**
     * Removes the highlighting border.<p>
     */
    public void removeHighlighting() {

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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int, Orientation)
     */
    public void repositionPlaceholder(int x, int y, Orientation orientation) {

        switch (orientation) {
            case HORIZONTAL:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    x,
                    -1);
                break;
            case VERTICAL:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    -1,
                    y);
                break;
            case ALL:
            default:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    x,
                    y);
                break;
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#showEditableListButtons()
     */
    public void showEditableListButtons() {

        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            if (child instanceof CmsContainerPageElement) {
                ((CmsContainerPageElement)child).showEditableListButtons();
            }
        }
    }
}
