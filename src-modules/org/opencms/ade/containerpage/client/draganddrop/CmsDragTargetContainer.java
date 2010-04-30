/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragTargetContainer.java,v $
 * Date   : $Date: 2010/04/30 07:04:20 $
 * Version: $Revision: 1.9 $
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

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.ade.containerpage.shared.I_CmsContainer;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drag target implementation representing a container page container.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 8.0.0
 */
public class CmsDragTargetContainer implements I_CmsDragTargetContainer {

    /** HTML class used to identify container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_CONTAINER_ELEMENTS}. */
    public static final String CLASS_CONTAINER_ELEMENTS = "cms_ade_element";

    /** HTML class used to identify sub container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_SUB_CONTAINER_ELEMENTS}. */
    public static final String CLASS_SUB_CONTAINER_ELEMENTS = "cms_ade_subcontainer";

    /** Container element id. */
    private String m_containerId;

    private CmsDebugLog m_debug;

    private CmsHighlightingBorder m_highlighting;

    /** This container wrapped in a {@link com.google.gwt.user.client.ui.RootPanel}. */
    private RootPanel m_root;

    /**
     * Constructor.<p>
     * 
     * @param containerData the container data
     */
    public CmsDragTargetContainer(I_CmsContainer containerData) {

        m_root = RootPanel.get(containerData.getName());
        m_containerId = containerData.getName();
        m_root.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragTarget());
        m_debug = CmsDebugLog.getInstance();
        m_debug.printLine("created instance of container id: " + containerData.getName());
        //   consumeChildren();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        m_root.add(w);

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
    public List<CmsDragContainerElement> getAllDragElements() {

        List<CmsDragContainerElement> elements = new ArrayList<CmsDragContainerElement>();
        Iterator<Widget> it = m_root.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsDragContainerElement) {
                elements.add((CmsDragContainerElement)w);
            } else {
                m_debug.printLine("WARNING: " + w.toString() + " is no instance of CmsDragContainerElement");
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
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#getElement()
     */
    public com.google.gwt.user.client.Element getElement() {

        return m_root.getElement();
    }

    /**
     * Returns an element iterator.<p>
     * 
     * @return the element iterator
     */
    public Iterator<Widget> getElementIterator() {

        return m_root.iterator();
    }

    /**
     * Collects the containers position data into a bean.<p>
     * 
     * @return the position bean
     */
    public CmsPositionBean getPositionInfo() {

        return CmsPositionBean.generatePositionInfo(m_root);
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidget(int)
     */
    public Widget getWidget(int index) {

        return m_root.getWidget(index);
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidgetCount()
     */
    public int getWidgetCount() {

        return m_root.getWidgetCount();
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
     */
    public int getWidgetIndex(Widget child) {

        return m_root.getWidgetIndex(child);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#highlightContainer()
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
        m_highlighting = new CmsHighlightingBorder(getInnerDimensions(), CmsHighlightingBorder.BorderColor.red);
        RootPanel.get().add(m_highlighting);

    }

    /**
     * @see com.google.gwt.user.client.ui.InsertPanel#insert(com.google.gwt.user.client.ui.Widget, int)
     */
    public void insert(Widget w, int beforeIndex) {

        m_root.insert(w, beforeIndex);

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#insert(com.google.gwt.user.client.ui.Widget, int, int, int)
     */
    public void insert(Widget w, int left, int top, int beforeIndex) {

        m_root.insert(w, left, top, beforeIndex);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_root.iterator();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragInside(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragInside(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#onDrop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDrop(I_CmsDragHandler<?, ?> handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#refreshHighlighting()
     */
    public void refreshHighlighting() {

        m_highlighting.setPosition(getInnerDimensions());

    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#remove(int)
     */
    public boolean remove(int index) {

        return m_root.remove(index);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget w) {

        return m_root.remove(w);
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer#removeHighlighting()
     */
    public void removeHighlighting() {

        m_highlighting.removeFromParent();
        m_highlighting = null;
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragTarget#setWidgetPosition(com.google.gwt.user.client.ui.Widget, int, int)
     */
    public void setWidgetPosition(Widget w, int left, int top) {

        m_root.setWidgetPosition(w, left, top);

    }

    /**
     * Returns a position info representing the dimensions of all visible child elements (excluding elements with position:absolute).
     * If the container has no visible elements, it's outer dimensions are returned.<p>
     * 
     * @return the position info
     */
    private CmsPositionBean getInnerDimensions() {

        boolean first = true;
        int top = 0;
        int left = 0;
        int height = 0;
        int width = 0;
        Iterator<Widget> it = iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            String positioning = w.getElement().getStyle().getPosition();
            if (w.isVisible()
                && !(positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName()))) {
                if (first) {
                    first = false;
                    top = w.getAbsoluteTop();
                    left = w.getAbsoluteLeft();
                    height = w.getOffsetHeight();
                    width = w.getOffsetWidth();
                } else {
                    int wTop = w.getAbsoluteTop();
                    top = top < wTop ? top : wTop;
                    int wLeft = w.getAbsoluteLeft();
                    left = left < wLeft ? left : wLeft;
                    int wHeight = w.getOffsetHeight();
                    height = height > (wTop + wHeight - top) ? height : (wTop + wHeight - top);
                    int wWidth = w.getOffsetWidth();
                    width = width > (wLeft + wWidth - left) ? width : (wLeft + wWidth - left);
                }
            }
        }
        if (!first) {
            CmsPositionBean result = new CmsPositionBean();
            result.setHeight(height);
            result.setWidth(width);
            result.setTop(top);
            result.setLeft(left);
            return result;
        } else {
            return getPositionInfo();
        }
    }
}
