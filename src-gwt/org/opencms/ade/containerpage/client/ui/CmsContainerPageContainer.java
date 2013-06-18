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
import org.opencms.ade.containerpage.shared.I_CmsContainer;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Event;
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

    /** Flag indicating if this container is a detail view only container. */
    boolean m_detailOnly;

    /** Container element id. */
    private String m_containerId;

    /** The container type. */
    private String m_containerType;

    /** Highlighting border for this container. */
    private CmsHighlightingBorder m_highlighting;

    /** True if this is a detail view container. */
    private boolean m_isDetailView;

    /** The maximum number of elements in this container. */
    private int m_maxElements;

    /** The overflowing element. */
    private Widget m_overflowingElement;

    /** The drag and drop placeholder. */
    private Element m_placeholder;

    /** The drag and drop placeholder position index. */
    private int m_placeholderIndex = -1;

    /** The wrapped widget. This will be a @link com.google.gwt.user.client.RootPanel. */
    private Widget m_widget;

    /**
     * Constructor.<p>
     * 
     * @param containerData the container data
     */
    public CmsContainerPageContainer(I_CmsContainer containerData) {

        initWidget(RootPanel.get(containerData.getName()));
        m_containerId = containerData.getName();
        m_containerType = containerData.getType();
        m_maxElements = containerData.getMaxElements();
        m_isDetailView = containerData.isDetailView();
        m_detailOnly = containerData.isDetailOnly();
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragTarget());
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        add(w, getElement());
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
                        widget.getElement().getStyle().setDisplay(Display.NONE);
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
            //           m_overflowingElement.getElement().getStyle().clearDisplay();
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;
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
     * @see org.opencms.ade.containerpage.client.ui.I_CmsDropContainer#insert(com.google.gwt.user.client.ui.Widget, int)
     */
    public void insert(Widget w, int beforeIndex) {

        insert(w, getElement(), beforeIndex, true);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, Orientation)
     */
    public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

        m_placeholder = placeholder;
        repositionPlaceholder(x, y, orientation);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#isAttached()
     */
    @Override
    public boolean isAttached() {

        if (m_widget != null) {
            return m_widget.isAttached();
        }
        return false;
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
     * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // Fire any handler added to the composite itself.
        super.onBrowserEvent(event);

        // Delegate events to the widget.
        m_widget.onBrowserEvent(event);
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
            m_highlighting.setPosition(CmsPositionBean.getInnerDimensions(getElement()));
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
            if (child instanceof CmsContainerPageElementPanel) {
                ((CmsContainerPageElementPanel)child).showEditableListButtons();
            }
        }
    }

    /**
     * Provides subclasses access to the topmost widget that defines this
     * composite.
     * 
     * @return the widget
     */
    protected Widget getWidget() {

        return m_widget;
    }

    /**
     * Sets the widget to be wrapped by the composite. The wrapped widget must be
     * set before calling any {@link Widget} methods on this object, or adding it
     * to a panel. This method may only be called once for a given composite.
     * 
     * @param widget the widget to be wrapped
     */
    protected void initWidget(Widget widget) {

        // Validate. Make sure the widget is not being set twice.
        if (m_widget != null) {
            throw new IllegalStateException("Composite.initWidget() may only be " + "called once.");
        }

        // Use the contained widget's element as the composite's element,
        // effectively merging them within the DOM.
        setElement(widget.getElement());

        adopt(widget);

        // Logical attach.
        m_widget = widget;
    }
}
