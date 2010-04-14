/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsContainerDragHandler.java,v $
 * Date   : $Date: 2010/04/14 14:33:47 $
 * Version: $Revision: 1.11 $
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

import org.opencms.ade.containerpage.client.CmsContainerpageDataProvider;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.client.draganddrop.A_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragElementExt;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsDraggableListItemWidget;
import org.opencms.gwt.client.ui.CmsToolbarButton;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page editor implementation of the drag and drop handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 8.0.0
 */
@SuppressWarnings("unchecked")
public class CmsContainerDragHandler extends A_CmsDragHandler<I_CmsDragElementExt, I_CmsDragTargetContainer> {

    /**
     * Bean holding info about draggable elements.<p>
     */
    protected class DragInfo {

        /** The draggable element. */
        private I_CmsDragElementExt m_draggable;

        /** The cursor offset left. */
        private int m_offsetLeft;

        /** The cursor offset top. */
        private int m_offsetTop;

        /** The elements place-holder. */
        private Widget m_placeholder1;

        /**
         * Constructor.<p>
         * 
         * @param draggable the draggable element
         * @param placeholder the elements place-holder
         * @param offsetLeft the cursor offset left
         * @param offsetTop the cursor offset top
         */
        public DragInfo(I_CmsDragElementExt draggable, Widget placeholder, int offsetLeft, int offsetTop) {

            m_draggable = draggable;
            m_placeholder1 = placeholder;
            m_offsetLeft = offsetLeft;
            m_offsetTop = offsetTop;
        }

        /**
         * Returns the draggable element.<p>
         *
         * @return the draggable element
         */
        public I_CmsDragElementExt getDraggable() {

            return m_draggable;
        }

        /**
         * Returns the offset left.<p>
         *
         * @return the offset left
         */
        public int getOffsetLeft() {

            return m_offsetLeft;
        }

        /**
         * Returns the offset top.<p>
         *
         * @return the offset top
         */
        public int getOffsetTop() {

            return m_offsetTop;
        }

        /**
         * Returns the place-holder.<p>
         * 
         * @return the place-holder
         */
        public Widget getPlaceholder() {

            return m_placeholder1;
        }
    }

    /** Instance of the drag and drop handler. */
    private static CmsContainerDragHandler INSTANCE;

    /** The current element info. */
    private DragInfo m_current;

    /** Flag if the drag process started from a tool-bar menu. */
    private boolean m_dragFromMenu;

    /** The element info of the favorite list drop-zone. */
    private DragInfo m_dropZoneInfo;

    /** The element info of the start element. */
    private DragInfo m_startInfo;

    /** Map of element info's. */
    private Map<I_CmsDragTargetContainer, DragInfo> m_targetInfos;

    /**
     * Constructor.<p>
     */
    protected CmsContainerDragHandler() {

        // nothing to do here
    }

    /**
     * Returns the drag handler instance.<p>
     * 
     * @return the drag handler
     */
    public static CmsContainerDragHandler get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContainerDragHandler();
        }
        return INSTANCE;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getCurrentTarget()
     */
    public I_CmsDragTargetContainer getCurrentTarget() {

        return m_currentTarget;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getDragElement()
     */
    public I_CmsDragElementExt getDragElement() {

        return m_dragElement;
    }

    /**
     * Returns the dropZoneInfo.<p>
     *
     * @return the dropZoneInfo
     */
    public DragInfo getDropZoneInfo() {

        return m_dropZoneInfo;
    }

    /**
     * Returns if the current drag process started from a tool-bar menu.<p> 
     * 
     * @return <code>true</code> if the current drag process started from a tool-bar menu
     */
    public boolean isDragFromMenu() {

        return m_dragFromMenu;
    }

    /**
     * Sets the dropZoneInfo.<p>
     *
     * @param dropZoneInfo the dropZoneInfo to set
     */
    public void setDropZoneInfo(DragInfo dropZoneInfo) {

        m_dropZoneInfo = dropZoneInfo;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#elementCancelAction()
     */
    @Override
    protected void elementCancelAction() {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#elementDropAction()
     */
    @Override
    protected void elementDropAction() {

        if (((m_current != m_startInfo) || (Math.abs(m_currentTarget.getWidgetIndex((Widget)m_current.getDraggable())
            - m_currentTarget.getWidgetIndex(m_current.getPlaceholder())) > 1))
            && (m_current != m_dropZoneInfo)) {
            CmsContainerpageDataProvider.get().addToRecentList(m_dragElement.getClientId());
            CmsContainerpageDataProvider.get().setPageChanged();
        }
        m_currentTarget.insert(
            (Widget)m_current.getDraggable(),
            m_currentTarget.getWidgetIndex(m_current.getPlaceholder()));

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#elementEnterTargetAction()
     */
    @Override
    protected void elementEnterTargetAction() {

        m_current.getDraggable().setVisible(false);
        // hide the current place-holder if it's not the one from the initial parent target
        if (m_current != m_startInfo) {
            m_current.getPlaceholder().setVisible(false);

        }
        this.getDragElement().getDragParent().getElement().removeClassName(
            I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        m_currentTarget.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        // if the element is dragged into a target that is not the initial parent target,
        // show the overlay on the initial place-holder and place it at the start position
        // otherwise remove the overlay
        if (m_targetInfos.get(m_currentTarget) != m_startInfo) {
            m_startInfo.getPlaceholder().addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
            I_CmsDragTarget orgTarget = m_dragElement.getDragParent();
            orgTarget.insert(m_current.getPlaceholder(), orgTarget.getWidgetIndex((Widget)m_dragElement));
        } else {
            m_startInfo.getPlaceholder().removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
        }

        m_current = m_targetInfos.get(m_currentTarget);
        positionElement();
        m_placeholder = m_current.getPlaceholder();
        sortTarget();
        m_current.getDraggable().setVisible(true);
        m_current.getPlaceholder().setVisible(true);

        // update the drag target highlighting
        updateHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#elementLeaveTargetAction()
     */
    @Override
    protected void elementLeaveTargetAction() {

        if (m_currentTarget != m_dragElement.getDragParent()) {
            m_current.getDraggable().setVisible(false);
            m_current.getPlaceholder().setVisible(false);

            m_currentTarget.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
            m_dragElement.getDragParent().getElement().addClassName(
                I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());

            m_current = m_startInfo;
            positionElement();
            m_placeholder = m_current.getPlaceholder();
            m_current.getDraggable().setVisible(true);
            m_placeholder.setVisible(true);
            m_placeholder.removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
        } else {
            m_currentTarget.insert(m_placeholder, m_currentTarget.getWidgetIndex((Widget)m_dragElement));
        }
        updateHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#positionElement()
     */
    @Override
    protected void positionElement() {

        Element element = m_current.getDraggable().getElement();
        Element parentElement = (Element)element.getParentElement();
        int left = m_currentEvent.getRelativeX(parentElement) - m_current.getOffsetLeft();
        int top = m_currentEvent.getRelativeY(parentElement) - m_current.getOffsetTop();
        DOM.setStyleAttribute(element, "left", left + "px");
        DOM.setStyleAttribute(element, "top", top + "px");
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#prepareElementForDrag()
     */
    @Override
    protected void prepareElementForDrag() {

        if (m_dragElement instanceof CmsDraggableListItemWidget) {
            m_dragFromMenu = true;

            CmsDragTargetMenu dragParent = new CmsDragTargetMenu();
            dragParent.setWidth(m_dragElement.getElement().getOffsetWidth() + "px");
            RootPanel.get().add(dragParent);
            m_currentTarget = dragParent;
            m_dragElement.setDragParent(dragParent);
            m_placeholder = new SimplePanel();
            dragParent.add((Widget)m_dragElement);
            CmsContainerpageDataProvider.get().getContainerpageUtil().getClipboard().hideMenu();
            Document.get().getBody().addClassName(CmsToolbarButton.ButtonData.MOVE.getIconClass());
        } else {
            m_dragFromMenu = false;
            m_placeholder = createPlaceholder(m_dragElement);
        }
        m_targetInfos = new HashMap<I_CmsDragTargetContainer, DragInfo>();
        //  m_placeholder = createPlaceholder(m_dragElement);
        m_startInfo = new DragInfo(m_dragElement, m_placeholder, m_cursorOffsetLeft, m_cursorOffsetTop);
        m_current = m_startInfo;
        m_targetInfos.put(m_currentTarget, m_current);
        prepareElement(m_current, m_currentTarget, false);
        positionElement();
        m_targets = new ArrayList<I_CmsDragTargetContainer>();
        m_targets.add(m_currentTarget);
        m_currentTarget.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        m_currentTarget.highlightContainer();
        String clientId = m_dragElement.getClientId();
        CmsContainerpageDataProvider.get().getElement(clientId, new I_CmsSimpleCallback<CmsContainerElement>() {

            /**
             * Executed with the requested element data.
             * Generates drag element widgets from the contents for each available container type.<p>
             * 
             * @param arg the element data
             * 
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#execute(Object)
             */
            public void execute(CmsContainerElement arg) {

                if ((arg != null) && isDragging()) {
                    if (!isDragFromMenu()) {
                        // preparing the tool-bar menu drop-zone
                        CmsContainerpageDataProvider.get().getContainerpageUtil().getClipboard().showDropzone(true);
                        I_CmsDragTargetContainer dropZone = CmsContainerpageDataProvider.get().getContainerpageUtil().getClipboard().getDropzone();
                        CmsDraggableListItemWidget menuItem = new CmsDraggableListItemWidget(new CmsListInfoBean(
                            arg.getTitle(),
                            arg.getFile(),
                            null), true);
                        menuItem.setClientId(arg.getClientId());
                        menuItem.setDragParent(dropZone);
                        dropZone.add(menuItem);
                        DragInfo infoMenu = new DragInfo(
                            menuItem,
                            createPlaceholder(menuItem),
                            menuItem.getOffsetWidth() - 20,
                            20);
                        addTargetInfo(dropZone, infoMenu);
                        prepareElement(infoMenu, dropZone, true);
                        setDropZoneInfo(infoMenu);
                        getTargets().add(0, dropZone);
                    }
                    Iterator<Entry<String, CmsDragTargetContainer>> it = CmsContainerpageDataProvider.get().getContainerTargets().entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<String, CmsDragTargetContainer> entry = it.next();
                        String containerType = CmsContainerpageDataProvider.get().getContainerType(entry.getKey());
                        if (arg.getContents().containsKey(containerType)
                            && (entry.getValue() != getDragElement().getDragParent())) {
                            try {

                                CmsDragContainerElement dragElement = CmsContainerpageDataProvider.get().getContainerpageUtil().createElement(
                                    arg,
                                    entry.getValue(),
                                    containerType);
                                entry.getValue().add(dragElement);
                                int offsetLeft = dragElement.getOffsetWidth() - 20;
                                DragInfo info = new DragInfo(
                                    dragElement,
                                    createPlaceholder(dragElement),
                                    offsetLeft,
                                    20);
                                addTargetInfo(entry.getValue(), info);
                                prepareElement(info, entry.getValue(), true);
                                addDragTarget(entry.getValue());
                                entry.getValue().highlightContainer();
                            } catch (Exception e) {
                                continue;
                            }

                        }
                    }
                }

            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#restoreElementAfterDrag()
     */
    @Override
    protected void restoreElementAfterDrag() {

        CmsContainerpageDataProvider.get().getContainerpageUtil().getClipboard().showDropzone(false);
        if (m_current.equals(getDropZoneInfo())) {
            CmsDebugLog.getInstance().printLine("Droped to menu");
            CmsContainerpageDataProvider.get().addToFavoriteList(m_dragElement.getClientId());
            m_current = m_startInfo;
        }
        Iterator<Entry<I_CmsDragTargetContainer, DragInfo>> it = m_targetInfos.entrySet().iterator();
        while (it.hasNext()) {
            Entry<I_CmsDragTargetContainer, DragInfo> entry = it.next();
            if (entry.getValue().equals(m_current) && !(m_dragFromMenu && (m_current == m_startInfo))) {
                entry.getValue().getDraggable().clearDrag();
                entry.getValue().getPlaceholder().removeFromParent();
            } else {
                entry.getValue().getPlaceholder().removeFromParent();
                ((Widget)entry.getValue().getDraggable()).removeFromParent();
            }
            entry.getKey().getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
            entry.getKey().removeHighlighting();
        }
        if (m_dragFromMenu) {
            CmsContainerpageDataProvider.get().getContainerpageUtil().getClipboard().setActive(false);
            ((Widget)m_dragElement.getDragParent()).removeFromParent();
            Document.get().getBody().removeClassName(CmsToolbarButton.ButtonData.MOVE.getIconClass());
        }
        m_targetInfos = null;
        m_current = null;
        m_startInfo = null;
        m_dropZoneInfo = null;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        updateHighlighting();

    }

    /**
     * Updates the drag target highlighting.<p>
     */
    protected void updateHighlighting() {

        Iterator<Entry<I_CmsDragTargetContainer, DragInfo>> it = m_targetInfos.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getKey().refreshHighlighting();
        }
    }

    /**
     * Adds a target info to the target info's map.<p>
     * 
     * @param target the drag target
     * @param info the drag info object
     */
    void addTargetInfo(I_CmsDragTargetContainer target, DragInfo info) {

        m_targetInfos.put(target, info);

    }

    /**
     * Creates a place-holder for the draggable element.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    Widget createPlaceholder(I_CmsDragElement element) {

        Widget result = new HTML(element.getElement().getInnerHTML());
        result.addStyleName(element.getElement().getClassName()
            + " "
            + I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());

        Element overlay = DOM.createDiv();
        overlay.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().placeholderOverlay());
        result.getElement().appendChild(overlay);

        return result;
    }

    /**
     * Adjust the style properties of the draggable element and inserts the place-holder into the target.<p>
     * 
     * @param elementInfo the drag info object
     * @param target the drag target
     * @param setHidden if <code>true</code> the element and it's place-holder will get hidden
     */
    void prepareElement(DragInfo elementInfo, I_CmsDragTargetContainer target, boolean setHidden) {

        target.insert(elementInfo.getPlaceholder(), target.getWidgetIndex((Widget)elementInfo.getDraggable()));
        elementInfo.getDraggable().prepareDrag();
        if (setHidden) {
            elementInfo.getPlaceholder().setVisible(false);
            elementInfo.getDraggable().setVisible(false);
        }
    }

}
