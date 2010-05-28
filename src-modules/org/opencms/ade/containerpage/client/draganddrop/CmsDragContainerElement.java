/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragContainerElement.java,v $
 * Date   : $Date: 2010/05/28 12:29:08 $
 * Version: $Revision: 1.16 $
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

import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsHighlightingBorder;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of a draggable element. To be used for content elements within a container-page.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.16 $
 * 
 * @since 8.0.0
 */
public class CmsDragContainerElement extends AbsolutePanel
implements I_CmsDragContainerElement<I_CmsDragTargetContainer>, HasClickHandlers {

    /** The CSS class of the move handle. */
    private static final String MOVE_HANDLE_CLASS = I_CmsButton.ButtonData.MOVE.getIconClass();

    /** The start offset left of the element to its parent. */
    protected int m_cursorOffsetLeft;

    /** The start offset top of the element to its parent. */
    protected int m_cursorOffsetTop;

    /** The drag handle widget. */
    protected Widget m_dragHandle;

    /** Highlighting border for this element. */
    protected CmsHighlightingBorder m_highlighting;

    /** The elements client id. */
    private String m_clientId;

    /** The current drag parent. */
    private I_CmsDragTargetContainer m_dragParent;

    /** The option bar, holding optional function buttons. */
    private CmsElementOptionBar m_elementOptionBar;

    /** Indicates whether this element has properties to edit. */
    private boolean m_hasProperties;

    /** The is new element type. */
    private String m_newType;

    /** The no edit reason, if empty editing is allowed. */
    private String m_noEditReason;

    /** The element resource site-path. */
    private String m_sitePath;

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
    public CmsDragContainerElement(
        Element element,
        I_CmsDragTargetContainer parent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        super(element);
        m_clientId = clientId;
        m_sitePath = sitePath;
        m_noEditReason = noEditReason;
        m_hasProperties = hasProps;
        setDragParent(parent);

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasContextMenuHandlers#addContextMenuHandler(com.google.gwt.event.dom.client.ContextMenuHandler)
     */
    public HandlerRegistration addContextMenuHandler(ContextMenuHandler handler) {

        return addDomHandler(handler, ContextMenuEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler(com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {

        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {

        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler(com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {

        return addDomHandler(handler, MouseWheelEvent.getType());
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#clearDrag()
     */
    public void clearDrag() {

        Element element = getElement();
        Style style = element.getStyle();
        style.clearPosition();
        style.clearWidth();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearMargin();
        style.clearDisplay();
        element.removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        element.removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        element.removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        element.removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        getElementOptionBar().removeStyleName(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());
    }

    /**
     * Returns the client id.<p>
     *
     * @return the client id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the cursor offset left.<p>
     *
     * @return the cursor offset left
     */
    public int getCursorOffsetLeft() {

        return m_cursorOffsetLeft;
    }

    /**
     * Returns the cursor offset top.<p>
     *
     * @return the cursor offset top
     */
    public int getCursorOffsetTop() {

        return m_cursorOffsetTop;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#getDragParent()
     */
    public I_CmsDragTargetContainer getDragParent() {

        return m_dragParent;
    }

    /**
     * Returns the option bar of this element.<p>
     * 
     * @return the option bar widget
     */
    public CmsElementOptionBar getElementOptionBar() {

        return m_elementOptionBar;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#getNewType()
     */
    public String getNewType() {

        return m_newType;
    }

    /**
     * Returns the no edit reason.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns true if the element has properties to edit.<p>
     * 
     * @return true if the element has properties to edit 
     */
    public boolean hasProperties() {

        return m_hasProperties;
    }

    /**
     * Puts a highlighting border around the element.<p>
     */
    public void highlightElement() {

        if (m_highlighting == null) {
            m_highlighting = new CmsHighlightingBorder(CmsPositionBean.generatePositionInfo(this), isNew()
            ? CmsHighlightingBorder.BorderColor.blue
            : CmsHighlightingBorder.BorderColor.red);
            RootPanel.get().add(m_highlighting);
        } else {
            m_highlighting.setPosition(CmsPositionBean.generatePositionInfo(this));
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    public boolean isHandleEvent(NativeEvent event) {

        EventTarget target = event.getEventTarget();
        if (com.google.gwt.dom.client.Element.is(target)) {
            return CmsDomUtil.hasClass(MOVE_HANDLE_CLASS, com.google.gwt.dom.client.Element.as(target));
        }
        return false;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#isNew()
     */
    public boolean isNew() {

        return m_newType != null;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#prepareDrag()
     */
    public void prepareDrag() {

        removeHighlighting();
        String width = CmsDomUtil.getCurrentStyle(getElement(), CmsDomUtil.Style.width);
        Style style = getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setProperty(CmsDomUtil.Style.width.name(), width);
        style.setZIndex(100);
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        if (!CmsDomUtil.hasBackground(getElement())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        }

        if (!CmsDomUtil.hasBorder(getElement())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        }
        if (m_elementOptionBar != null) {
            m_elementOptionBar.addStyleName(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#removeFromParent()
     */
    @Override
    public void removeFromParent() {

        removeHighlighting();
        super.removeFromParent();
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
     * Sets the client id.<p>
     *
     * @param clientId the client id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#setDragParent(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void setDragParent(I_CmsDragTargetContainer target) {

        m_dragParent = target;
    }

    /**
     * Sets the elementOptionBar.<p>
     *
     * @param elementOptionBar the elementOptionBar to set
     */
    public void setElementOptionBar(CmsElementOptionBar elementOptionBar) {

        if ((m_elementOptionBar != null) && (getWidgetIndex(m_elementOptionBar) >= 0)) {
            m_elementOptionBar.removeFromParent();
        }
        add(elementOptionBar);
        m_elementOptionBar = elementOptionBar;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#setNewType(java.lang.String)
     */
    public void setNewType(String type) {

        m_newType = type;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the drag handle of the element. If set, the element will only be draggable by the handle.<p>
     * 
     * @param handle the handle
     * @throws UnsupportedOperationException thrown if the handle to set is not a child element of the draggable
     */
    protected void setDragHandle(Widget handle) throws UnsupportedOperationException {

        if ((handle == null) || getElement().isOrHasChild(handle.getElement())) {
            m_dragHandle = handle;
        } else {
            throw new UnsupportedOperationException("The drag handle has to be a child of the draggable element.");
        }
    }
}
