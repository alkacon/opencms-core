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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Container page container.<p>
 * 
 * 
 * 
 * @since 8.0.0
 */
public class CmsContainerPageContainer extends ComplexPanel implements I_CmsDropContainer {

    /**
     * Element position info class.<p>
     */
    protected class ElementPositionInfo {

        /** The DOM element. */
        private Element m_element;

        /** The element position bean. */
        private CmsPositionBean m_elementPosition;

        /** The float CSS property. */
        private String m_float;

        /** Flag indicating the element is positioned absolute. */
        private boolean m_isAbsolute;

        /**
         * Constructor.<p>
         * 
         * @param element the DOM element
         */
        public ElementPositionInfo(Element element) {

            m_element = element;
            String positioning = CmsDomUtil.getCurrentStyle(m_element, Style.position);
            m_isAbsolute = Position.ABSOLUTE.getCssName().equals(positioning)
                || Position.FIXED.getCssName().equals(positioning);
            if (!m_isAbsolute) {
                m_elementPosition = CmsPositionBean.getInnerDimensions(element);
                m_float = CmsDomUtil.getCurrentStyle(m_element, Style.floatCss);
            }
        }

        /**
         * Returns the DOM element.<p>
         * 
         * @return the DOM element
         */
        public Element getElement() {

            return m_element;
        }

        /**
         * Returns the element position bean.<p>
         * 
         * @return the element position bean
         */
        public CmsPositionBean getElementPosition() {

            return m_elementPosition;
        }

        /**
         * Returns the x distance of the cursor to the element left.<p>
         *  
         * @param x the cursor x position
         * @param documentScrollLeft the document scroll left position
         * 
         * @return the y distance of the cursor to the element top
         */
        public int getRelativeLeft(int x, int documentScrollLeft) {

            return (x + documentScrollLeft) - m_elementPosition.getLeft();
        }

        /**
         * Returns the y distance of the cursor to the element top.<p>
         *  
         * @param y the cursor y position
         * @param documentScrollTop the document scroll top position
         * 
         * @return the y distance of the cursor to the element top
         */
        public int getRelativeTop(int y, int documentScrollTop) {

            return (y + documentScrollTop) - m_elementPosition.getTop();
        }

        /**
         * Returns if the element is positioned absolute.<p>
         * 
         * @return <code>true</code> if the element is positioned absolute
         */
        public boolean isAbsolute() {

            return m_isAbsolute;
        }

        /**
         * Returns if the element is floated.<p>
         * 
         * @return <code>true</code> if the element is floated
         */
        public boolean isFloating() {

            return isFloatLeft() || isFloatRight();
        }

        /**
         * Returns if the element is floated to the left.<p>
         * 
         * @return <code>true</code> if the element is floated to the left
         */
        public boolean isFloatLeft() {

            return "left".equals(m_float);
        }

        /**
         * Returns if the element is floated to the right.<p>
         * 
         * @return <code>true</code> if the element is floated to the right
         */
        public boolean isFloatRight() {

            return "right".equals(m_float);
        }

    }

    /** Flag indicating if this container is a detail view only container. */
    boolean m_detailOnly;

    /** The configured width for this container. */
    private int m_configuredWidth;

    /** Container element id. */
    private String m_containerId;

    /** The container type. */
    private String m_containerType;

    /** The element position info cache. */
    private List<ElementPositionInfo> m_elementPositions;

    /** Highlighting border for this container. */
    private CmsHighlightingBorder m_highlighting;

    /** True if this is a detail view container. */
    private boolean m_isDetailView;

    /** The maximum number of elements in this container. */
    private int m_maxElements;

    /** The overflowing element. */
    private Widget m_overflowingElement;

    /** The cached highlighting position. */
    private CmsPositionBean m_ownPosition;

    /** The drag and drop placeholder. */
    private Element m_placeholder;

    /** The drag and drop placeholder position index. */
    private int m_placeholderIndex = -1;

    /** Flag indicating the element positions need to be re-evaluated. */
    private boolean m_requiresPositionUpdate = true;

    /** The list of nested sub containers that are also valid drop targets during the current drag and drop. */
    private List<I_CmsDropTarget> m_dnDChildren;

    /**
     * Constructor.<p>
     * 
     * @param containerData the container data
     * @param element the container element
     */
    public CmsContainerPageContainer(CmsContainer containerData, Element element) {

        setElement(element);
        if (!containerData.isSubContainer()) {
            RootPanel.detachOnWindowClose(this);
        }
        m_containerId = containerData.getName();
        m_containerType = containerData.getType();
        m_maxElements = containerData.getMaxElements();
        m_isDetailView = containerData.isDetailView();
        m_detailOnly = containerData.isDetailOnly();
        m_configuredWidth = containerData.getWidth();
        addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragTarget());
        onAttach();
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        add(w, (Element)getElement());
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#addDndChild(org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    public void addDndChild(I_CmsDropTarget child) {

        if (m_dnDChildren == null) {
            m_dnDChildren = new ArrayList<I_CmsDropTarget>();
        }
        m_dnDChildren.add(child);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#adoptElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    public void adoptElement(CmsContainerPageElementPanel containerElement) {

        assert getElement().equals(containerElement.getElement().getParentElement());
        getChildren().add(containerElement);
        adopt(containerElement);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#checkMaxElementsOnEnter()
     */
    public void checkMaxElementsOnEnter() {

        if (getWidgetCount() >= m_maxElements) {
            Widget overflowElement = null;
            int index = 0;
            for (Widget widget : this) {
                boolean isDummy = widget.getStyleName().contains(CmsTemplateContextInfo.DUMMY_ELEMENT_MARKER);
                if (!isDummy) {
                    index++;
                    if (index >= m_maxElements) {
                        if (overflowElement == null) {
                            overflowElement = widget;
                        }
                    }
                }
            }
            if (overflowElement != null) {
                m_overflowingElement = overflowElement;
                m_overflowingElement.removeFromParent();
            }
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#checkMaxElementsOnLeave()
     */
    public void checkMaxElementsOnLeave() {

        if (m_overflowingElement != null) {
            add(m_overflowingElement);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int, Orientation)
     */
    public boolean checkPosition(int x, int y, Orientation orientation) {

        // ignore orientation
        int scrollTop = getElement().getOwnerDocument().getScrollTop();
        // use cached position
        int relativeTop = (y + scrollTop) - m_ownPosition.getTop();
        if ((relativeTop > 0) && (m_ownPosition.getHeight() > relativeTop)) {
            // cursor is inside the height of the element, check horizontal position
            int scrollLeft = getElement().getOwnerDocument().getScrollLeft();
            int relativeLeft = (x + scrollLeft) - m_ownPosition.getLeft();
            return (relativeLeft > 0) && (m_ownPosition.getWidth() > relativeLeft);
        }
        return false;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#clearDnDChildren()
     */
    public void clearDnDChildren() {

        if (m_dnDChildren != null) {
            m_dnDChildren.clear();
        }
    }

    /**
     * Returns all contained drag elements.<p>
     * 
     * @return the drag elements
     */
    public List<CmsContainerPageElementPanel> getAllDragElements() {

        List<CmsContainerPageElementPanel> elements = new ArrayList<CmsContainerPageElementPanel>();
        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElementPanel) {
                elements.add((CmsContainerPageElementPanel)w);
            } else {
                if (CmsDomUtil.hasClass(
                    org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerPlaceholder(),
                    w.getElement())) {
                    CmsDebugLog.getInstance().printLine("Ignoring group container placeholder.");
                } else {
                    CmsDebugLog.getInstance().printLine(
                        "WARNING: " + w.toString() + " is no instance of CmsDragContainerElement");
                }
            }
        }
        return elements;
    }

    /**
     * Returns the configured width for this container.<p>
     * 
     * @return the configured width
     */
    public int getConfiguredWidth() {

        return m_configuredWidth;
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
     * @see org.opencms.gwt.client.dnd.I_CmsNestedDropTarget#getDnDChildren()
     */
    public List<I_CmsDropTarget> getDnDChildren() {

        return m_dnDChildren;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#getPositionInfo()
     */
    public CmsPositionBean getPositionInfo() {

        return m_ownPosition;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsNestedDropTarget#hasDnDChildren()
     */
    public boolean hasDnDChildren() {

        return (m_dnDChildren != null) && !m_dnDChildren.isEmpty();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#hideEditableListButtons()
     */
    public void hideEditableListButtons() {

        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            if (child instanceof CmsContainerPageElementPanel) {
                ((CmsContainerPageElementPanel)child).hideEditableListButtons();
            }
        }
    }

    /**
     * Puts a highlighting border around the container content.<p>
     */
    public void highlightContainer() {

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        // remove any remaining highlighting
        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
        }
        // adding the 'clearFix' style to all targets containing floated elements
        // in some layouts this may lead to inappropriate clearing after the target, 
        // but it is still necessary as it forces the target to enclose it's floated content 
        if ((getWidgetCount() > 0)
            && !CmsDomUtil.getCurrentStyle(getWidget(0).getElement(), CmsDomUtil.Style.floatCss).equals(
                CmsDomUtil.StyleValue.none.toString())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
        }
        // cache the position info, to be used during drag and drop
        m_ownPosition = CmsPositionBean.getInnerDimensions(getElement(), 3, false);
        m_highlighting = new CmsHighlightingBorder(m_ownPosition, CmsHighlightingBorder.BorderColor.red);
        RootPanel.get().add(m_highlighting);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#insert(com.google.gwt.user.client.ui.Widget, int)
     */
    public void insert(Widget w, int beforeIndex) {

        insert(w, (Element)getElement(), beforeIndex, true);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, Orientation)
     */
    public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

        m_placeholder = placeholder;
        m_requiresPositionUpdate = true;
        repositionPlaceholder(x, y, orientation);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#isDetailOnly()
     */
    public boolean isDetailOnly() {

        return m_detailOnly;
    }

    /**
     * Returns true if this is a detail view container.<p>
     * 
     * @return true if this is a detail view container 
     */
    public boolean isDetailView() {

        return m_isDetailView;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
     */
    public void onDrop(I_CmsDraggable draggable) {

        m_overflowingElement = null;
    }

    /**
     * Refreshes position and dimension of the highlighting border. Call when anything changed during the drag process.<p>
     */
    public void refreshHighlighting() {

        if (m_highlighting != null) {
            refreshHighlighting(CmsPositionBean.getInnerDimensions(getElement(), 3, false));
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#refreshHighlighting(org.opencms.gwt.client.util.CmsPositionBean)
     */
    public void refreshHighlighting(CmsPositionBean positionInfo) {

        // cache the position info, to be used during drag and drop
        m_ownPosition = positionInfo;
        if (m_highlighting != null) {
            m_highlighting.setPosition(m_ownPosition);
        }
    }

    /**
     * Removes the highlighting border.<p>
     */
    public void removeHighlighting() {

        if (m_highlighting != null) {
            m_highlighting.removeFromParent();
            m_highlighting = null;
        }
        removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
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
        m_requiresPositionUpdate = true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int, Orientation)
     */
    public void repositionPlaceholder(int x, int y, Orientation orientation) {

        if (m_requiresPositionUpdate) {
            updatePositionsList();
        }
        int newPlaceholderIndex = internalRepositionPlaceholder(x, y);
        m_requiresPositionUpdate = newPlaceholderIndex != m_placeholderIndex;
        m_placeholderIndex = newPlaceholderIndex;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#showEditableListButtons()
     */
    public void showEditableListButtons() {

        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            if (child instanceof CmsContainerPageElementPanel) {
                ((CmsContainerPageElementPanel)child).showEditableListButtons();
            }
        }
    }

    /**
     * Updates the option bar positions of the child elements.<p>
     */
    public void updateOptionBars() {

        for (Widget widget : this) {
            if (widget instanceof CmsContainerPageElementPanel) {
                ((CmsContainerPageElementPanel)widget).updateOptionBarPosition();
            }
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#updatePositionInfo()
     */
    public void updatePositionInfo() {

        m_ownPosition = CmsPositionBean.getInnerDimensions(getElement(), 3, false);
    }

    /**
     * Repositions the drag and drop placeholder.<p>
     * 
     * @param x the x cursor position
     * @param y the y cursor position
     * 
     * @return the placeholder position index
     */
    private int internalRepositionPlaceholder(int x, int y) {

        int indexCorrection = 0;
        int previousTop = 0;
        int documentScrollTop = getElement().getOwnerDocument().getScrollTop();
        int documentScrollLeft = getElement().getOwnerDocument().getScrollLeft();
        for (int index = 0; index < m_elementPositions.size(); index++) {
            ElementPositionInfo info = m_elementPositions.get(index);
            if (info.getElement() == m_placeholder) {
                indexCorrection = 1;
            }
            if (info.isAbsolute()) {
                continue;
            }

            int top = info.getRelativeTop(y, documentScrollTop);

            if ((top <= 0) || (top >= info.getElementPosition().getHeight())) {
                previousTop = top;
                continue;
            }
            int left = info.getRelativeLeft(x, documentScrollLeft);
            if ((left <= 0) || (left >= info.getElementPosition().getWidth())) {
                previousTop = top;
                continue;
            }
            boolean floatSort = info.isFloating() && (top != 0) && (top == previousTop);
            previousTop = top;
            if (info.getElement() != m_placeholder) {
                if (floatSort) {
                    boolean insertBefore = false;
                    if (left < (info.getElementPosition().getWidth() / 2)) {
                        if (info.isFloatLeft()) {
                            insertBefore = true;
                        }
                    } else if (info.isFloatRight()) {
                        insertBefore = true;
                    }
                    if (insertBefore) {
                        getElement().insertBefore(m_placeholder, info.getElement());
                        return index - indexCorrection;
                    } else {
                        getElement().insertAfter(m_placeholder, info.getElement());
                        return (index + 1) - indexCorrection;
                    }
                } else {
                    if (top < (info.getElementPosition().getHeight() / 2)) {
                        getElement().insertBefore(m_placeholder, info.getElement());
                        return index - indexCorrection;
                    } else {
                        getElement().insertAfter(m_placeholder, info.getElement());
                        return (index + 1) - indexCorrection;
                    }
                }
            } else {
                return index;
            }
        }

        // not over any child position
        if ((m_placeholderIndex >= 0) && (m_placeholder.getParentElement() == getElement())) {
            // element is already attached to this parent and no new position available
            // don't do anything
            return m_placeholderIndex;
        }
        int top = CmsDomUtil.getRelativeY(y, getElement());
        int offsetHeight = getElement().getOffsetHeight();
        if ((top >= (offsetHeight / 2))) {
            // over top half, insert as first child
            getElement().insertFirst(m_placeholder);
            return 0;
        }
        // over bottom half, insert as last child
        getElement().appendChild(m_placeholder);
        return getElement().getChildCount() - 1;
    }

    /**
     * Updates the element position cache during drag and drop.<p>
     */
    private void updatePositionsList() {

        CmsDebugLog.getInstance().printLine("Updating positions");
        if (m_elementPositions != null) {
            m_elementPositions.clear();
        } else {
            m_elementPositions = new ArrayList<ElementPositionInfo>();
        }
        for (int index = 0; index < getElement().getChildCount(); index++) {
            Node node = getElement().getChild(index);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            m_elementPositions.add(new ElementPositionInfo((Element)node));
        }
        m_requiresPositionUpdate = false;
        m_ownPosition = CmsPositionBean.getInnerDimensions(getElement(), 3, false);
    }
}
