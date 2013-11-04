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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleSaver;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page editor drag and drop controller.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageDNDController implements I_CmsDNDController {

    /**
     * Bean holding info about draggable elements.<p>
     */
    protected class DragInfo {

        /** The drag helper element. */
        private Element m_dragHelper;

        /** The cursor offset top. */
        private int m_offsetX;

        /** The cursor offset left. */
        private int m_offsetY;

        /** The placeholder element. */
        private Element m_placeholder;

        /**
         * Constructor.<p>
         * 
         * @param dragHelper the drag helper element
         * @param placeholder the elements place-holder
         * @param offsetX the cursor offset x
         * @param offsetY the cursor offset y
         */
        protected DragInfo(Element dragHelper, Element placeholder, int offsetX, int offsetY) {

            m_dragHelper = dragHelper;
            m_placeholder = placeholder;
            m_offsetX = offsetX;
            m_offsetY = offsetY;
        }

        /**
         * Returns the offset x.<p>
         *
         * @return the offset x
         */
        public int getOffsetX() {

            return m_offsetX;
        }

        /**
         * Returns the offset y.<p>
         *
         * @return the offset y
         */
        public int getOffsetY() {

            return m_offsetY;
        }

        /**
         * Returns the drag helper element.<p>
         * 
         * @return the drag helper element
         */
        protected Element getDragHelper() {

            return m_dragHelper;
        }

        /**
         * Returns the placeholder element.<p>
         * 
         * @return the placeholder element
         */
        protected Element getPlaceholder() {

            return m_placeholder;
        }
    }

    /** The height value above which a container's min height will be set when the user starts dragging. */
    public static final double MIN_HEIGHT_THRESHOLD = 50.0;

    /** The container page controller. */
    protected CmsContainerpageController m_controller;

    /** The id of the dragged element. */
    protected String m_draggableId;

    /** The current place holder index. */
    private int m_currentIndex = -1;

    /** The current drop target. */
    private I_CmsDropTarget m_currentTarget;

    /** Map of current drag info beans. */
    private Map<I_CmsDropTarget, DragInfo> m_dragInfos;

    /** The drag overlay. */
    private Element m_dragOverlay;

    /** The ionitial drop target. */
    private I_CmsDropTarget m_initialDropTarget;

    /** Creating new flag. */
    private boolean m_isNew;

    /** The original position of the draggable. */
    private int m_originalIndex;

    /** Objects for restoring the min. heights of containers. */
    private List<CmsStyleSaver> m_savedMinHeights = new ArrayList<CmsStyleSaver>();

    /**
     * Constructor.<p>
     * 
     * @param controller the container page controller
     */
    public CmsContainerpageDNDController(CmsContainerpageController controller) {

        m_controller = controller;
        m_dragInfos = new HashMap<I_CmsDropTarget, DragInfo>();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onAnimationStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onAnimationStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // remove highlighting
        for (I_CmsDropTarget dropTarget : m_dragInfos.keySet()) {
            if (dropTarget instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)dropTarget).removeHighlighting();
            }
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        return true;
    }

    /**
    * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
    */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        stopDrag(handler);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, final CmsDNDHandler handler) {

        installDragOverlay();
        m_currentTarget = null;
        m_currentIndex = -1;
        m_draggableId = draggable.getId();
        m_isNew = false;
        m_originalIndex = -1;
        m_initialDropTarget = target;
        handler.setOrientation(Orientation.ALL);
        m_controller.hideEditableListButtons();

        if (!m_controller.isGroupcontainerEditing()) {
            boolean locked = m_controller.lockContainerpage();
            if (!locked) {
                return false;
            }
        }

        if (target != null) {
            handler.addTarget(target);
            if (target instanceof I_CmsDropContainer) {
                prepareTargetContainer((I_CmsDropContainer)target, draggable, handler.getPlaceholder());
            }
        }

        m_dragInfos.put(
            target,
            new DragInfo(
                handler.getDragHelper(),
                handler.getPlaceholder(),
                handler.getCursorOffsetX(),
                handler.getCursorOffsetY()));
        m_controller.getHandler().hideMenu();
        String clientId = draggable.getId();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(clientId)) {
            CmsDebugLog.getInstance().printLine("draggable has no id, canceling drop");
            return false;
        }
        final I_CmsSimpleCallback<CmsContainerElementData> callback = new I_CmsSimpleCallback<CmsContainerElementData>() {

            /**
             * Execute on success.<p>
             * 
             * @param arg the container element data
             */
            public void execute(CmsContainerElementData arg) {

                prepareHelperElements(arg, handler);
            }
        };
        if (isNewId(clientId)) {
            // for new content elements dragged from the gallery menu, the given id contains the resource type name
            //clientId = m_controller.getNewResourceId(clientId);
            m_isNew = true;
            m_controller.getNewElement(clientId, callback);
        } else {
            if (isCopyModel(draggable)) {
                m_controller.copyElement(draggable.getId(), new AsyncCallback<CmsUUID>() {

                    public void onFailure(Throwable caught) {

                        // will never be called 
                    }

                    public void onSuccess(CmsUUID result) {

                        String idString = result.toString();
                        m_draggableId = idString;

                        m_controller.getElement(idString, callback);
                    }
                });
            } else {
                m_controller.getElement(clientId, callback);
            }

        }
        if (target instanceof CmsContainerPageContainer) {
            String id = ((CmsContainerPageContainer)target).getContainerId();
            CmsContainerpageEditor.getZIndexManager().start(id);
        } else {
            handler.setStartPosition(-1, 0);
        }
        return true;
    }

    /**
    * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
    */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        boolean changedContainerpage = false;
        if (target != m_initialDropTarget) {
            if (target instanceof I_CmsDropContainer) {
                I_CmsDropContainer container = (I_CmsDropContainer)target;
                try {

                    CmsContainerPageElementPanel containerElement = null;
                    if (m_isNew) {
                        // for new content elements dragged from the gallery menu, the given id contains the resource type name
                        containerElement = m_controller.getContainerpageUtil().createElement(
                            m_controller.getCachedElement(m_draggableId),
                            container);
                        containerElement.setNewType(m_draggableId);
                    } else {
                        CmsContainerElementData elementData = m_controller.getCachedElement(m_draggableId);
                        containerElement = m_controller.getContainerpageUtil().createElement(elementData, container);
                        m_controller.addToRecentList(m_draggableId, null);
                    }
                    handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
                    if (container.getPlaceholderIndex() >= container.getWidgetCount()) {
                        container.add(containerElement);
                    } else {
                        container.insert(containerElement, container.getPlaceholderIndex());
                    }
                    // changes are only relevant to the container page if not group-container editing
                    changedContainerpage = !m_controller.isGroupcontainerEditing();
                    if (draggable instanceof CmsContainerPageElementPanel) {
                        ((CmsContainerPageElementPanel)draggable).removeFromParent();
                    }
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine(e.getMessage());
                }
                if (m_controller.isGroupcontainerEditing()) {
                    container.getElement().removeClassName(
                        I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
                }
            } else if (target instanceof CmsList<?>) {
                m_controller.addToFavoriteList(m_draggableId);
            }
        } else if ((target instanceof I_CmsDropContainer)
            && (draggable instanceof CmsContainerPageElementPanel)
            && isChangedPosition(target)) {

            I_CmsDropContainer container = (I_CmsDropContainer)target;
            int count = container.getWidgetCount();
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            if (container.getPlaceholderIndex() >= count) {
                container.add((CmsContainerPageElementPanel)draggable);
            } else {
                container.insert((CmsContainerPageElementPanel)draggable, container.getPlaceholderIndex());
            }
            m_controller.addToRecentList(m_draggableId, null);
            // changes are only relevant to the container page if not group-container editing
            changedContainerpage = !m_controller.isGroupcontainerEditing();
        } else if (draggable instanceof CmsContainerPageElementPanel) {
            // to reset mouse over state remove and attach the option bar 
            CmsContainerPageElementPanel containerElement = (CmsContainerPageElementPanel)draggable;
            CmsElementOptionBar optionBar = containerElement.getElementOptionBar();
            optionBar.removeFromParent();
            containerElement.setElementOptionBar(optionBar);
        }
        if (changedContainerpage) {
            m_controller.setPageChanged();
        }
        stopDrag(handler);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (hasChangedPosition(target)) {
            updateHighlighting();
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        DragInfo info = m_dragInfos.get(target);
        if (info != null) {
            hideCurrentHelpers(handler);
            replaceCurrentHelpers(handler, info);
            if ((target != m_initialDropTarget) && (target instanceof I_CmsDropContainer)) {
                ((I_CmsDropContainer)target).checkMaxElementsOnEnter();
            }
        }
        if (target != m_initialDropTarget) {
            showOriginalPositionPlaceholder(draggable, true);
        } else {
            hideOriginalPositionPlaceholder(draggable);
        }
        if (target instanceof CmsContainerPageContainer) {
            CmsContainerPageContainer cont = (CmsContainerPageContainer)target;
            CmsContainerpageEditor.getZIndexManager().enter(cont.getContainerId());
        }
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        DragInfo info = m_dragInfos.get(m_initialDropTarget);
        if (info != null) {
            hideCurrentHelpers(handler);
            replaceCurrentHelpers(handler, info);
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            if ((target != m_initialDropTarget) && (target instanceof I_CmsDropContainer)) {
                ((I_CmsDropContainer)target).checkMaxElementsOnLeave();
            }
        }
        showOriginalPositionPlaceholder(draggable, false);
        updateHighlighting();
        if (target instanceof CmsContainerPageContainer) {
            String id = ((CmsContainerPageContainer)target).getContainerId();
            CmsContainerpageEditor.getZIndexManager().leave(id);
        }

    }

    /**
     * Prepares all helper elements for the different drop targets.<p>
     * 
     * @param elementData the element data
     * @param handler the drag and drop handler
     */
    protected void prepareHelperElements(CmsContainerElementData elementData, CmsDNDHandler handler) {

        if (!handler.isDragging()) {
            return;
        }
        if (elementData == null) {
            CmsDebugLog.getInstance().printLine("elementData == null!");
            handler.cancel();
            return;
        }

        if (!(handler.getDraggable() instanceof CmsContainerPageElementPanel)) {
            // inserting element from menu

        }

        if (m_controller.isGroupcontainerEditing()) {
            CmsGroupContainerElementPanel groupContainer = m_controller.getGroupcontainer();
            if ((groupContainer != m_initialDropTarget)
                && !(elementData.isGroupContainer() || elementData.isInheritContainer())
                && elementData.getContents().containsKey(groupContainer.getContainerId())) {
                Element helper = null;
                Element placeholder = null;
                Set<String> cssResources = elementData.getCssResources(groupContainer.getContainerId());
                if ((cssResources != null) && !cssResources.isEmpty()) {
                    // the element requires certain CSS resources, check if present and include if necessary
                    for (String cssResourceLink : cssResources) {
                        CmsDomUtil.ensureStyleSheetIncluded(cssResourceLink);
                    }
                }
                try {
                    String htmlContent = elementData.getContents().get(groupContainer.getContainerId());
                    helper = CmsDomUtil.createElement(htmlContent);
                    // ensure any embedded flash players are set opaque so UI elements may be placed above them
                    CmsDomUtil.fixFlashZindex(helper);
                    placeholder = CmsDomUtil.clone(helper);
                    placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine(e.getMessage());
                }

                if (helper != null) {
                    prepareDragInfo(helper, placeholder, groupContainer, handler);
                    groupContainer.highlightContainer();
                }
            }
            return;
        }
        if (!m_controller.isEditingDisabled()) {
            for (CmsContainerPageContainer container : m_controller.getContainerTargets().values()) {

                if ((container != m_initialDropTarget)
                    && !container.isDetailView()
                    && elementData.getContents().containsKey(container.getContainerId())) {

                    Element helper = null;
                    Element placeholder = null;
                    if (elementData.isGroupContainer() || elementData.isInheritContainer()) {
                        helper = DOM.createDiv();
                        String content = "";
                        for (String groupId : elementData.getSubItems()) {
                            CmsContainerElementData subData = m_controller.getCachedElement(groupId);
                            if ((subData != null) && subData.getContents().containsKey(container.getContainerId())) {
                                content += subData.getContents().get(container.getContainerId());
                            }
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
                            helper.setInnerHTML(content);
                            // ensure any embedded flash players are set opaque so UI elements may be placed above them
                            CmsDomUtil.fixFlashZindex(helper);
                        } else {
                            helper.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
                        }
                        placeholder = CmsDomUtil.clone(helper);
                    } else {
                        Set<String> cssResources = elementData.getCssResources(container.getContainerId());
                        if ((cssResources != null) && !cssResources.isEmpty()) {
                            // the element requires certain CSS resources, check if present and include if necessary
                            for (String cssResourceLink : cssResources) {
                                CmsDomUtil.ensureStyleSheetIncluded(cssResourceLink);
                            }
                        }
                        try {
                            String htmlContent = elementData.getContents().get(container.getContainerId());
                            helper = CmsDomUtil.createElement(htmlContent);
                            // ensure any embedded flash players are set opaque so UI elements may be placed above them
                            CmsDomUtil.fixFlashZindex(helper);
                            placeholder = CmsDomUtil.clone(helper);
                            placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
                        } catch (Exception e) {
                            CmsDebugLog.getInstance().printLine(e.getMessage());
                        }
                    }
                    if (helper != null) {
                        prepareDragInfo(helper, placeholder, container, handler);
                        container.highlightContainer();
                    }
                }
            }
        }
    }

    /**
     * Checks if the placeholder position has changed.<p>
     * 
     * @param target the current drop target
     * 
     * @return <code>true</code> if the placeholder position has changed
     */
    private boolean hasChangedPosition(I_CmsDropTarget target) {

        if ((m_currentTarget != target) || (m_currentIndex != target.getPlaceholderIndex())) {
            m_currentTarget = target;
            m_currentIndex = target.getPlaceholderIndex();
            return true;
        }
        return false;
    }

    /**
     * Hides the current drag helper and place-holder.<p>
     * 
     * @param handler the drag and drop handler
     */
    private void hideCurrentHelpers(CmsDNDHandler handler) {

        handler.getDragHelper().getStyle().setDisplay(Display.NONE);
        handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Hides the the draggable on it'e original position.<p>
     * 
     * @param draggable the draggable
     */
    private void hideOriginalPositionPlaceholder(I_CmsDraggable draggable) {

        draggable.getElement().getStyle().setDisplay(Display.NONE);
        CmsDomUtil.showOverlay(draggable.getElement(), false);
    }

    /**
     * Installs the drag overlay to avoid any mouse over issues or similar.<p>
     */
    private void installDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
        }
        m_dragOverlay = DOM.createDiv();
        m_dragOverlay.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragOverlay());
        Document.get().getBody().appendChild(m_dragOverlay);
    }

    /**
     * Checks whether the current placeholder position represents a change to the original draggable position within the tree.<p>
     * 
     * @param target the current drop target
     * 
     * @return <code>true</code> if the position changed
     */
    private boolean isChangedPosition(I_CmsDropTarget target) {

        // if the new index is not next to the old one, the position has changed
        if ((target != m_initialDropTarget)
            || !((target.getPlaceholderIndex() == (m_originalIndex + 1)) || (target.getPlaceholderIndex() == m_originalIndex))) {
            return true;
        }
        return false;
    }

    /** 
     * Checks if the draggable item is a copy model.<p>
     * 
     * @param draggable the draggable item 
     * 
     * @return true if the item is a copy model 
     */
    private boolean isCopyModel(I_CmsDraggable draggable) {

        if (!(draggable instanceof CmsResultListItem)) {
            return false;
        }
        return ((CmsResultListItem)draggable).getResult().isCopyModel();
    }

    /**
     * Checks if the given id is a new id.<p>
     * 
     * @param id the id
     * 
     * @return <code>true</code> if the id is a new id
     */
    private boolean isNewId(String id) {

        if (id.contains("#")) {
            id = id.substring(0, id.indexOf("#"));
        }
        return !CmsUUID.isValidUUID(id);
    }

    /**
     * Sets styles of helper elements, appends the to the drop target and puts them into a drag info bean.<p>
     * 
     * @param dragHelper the drag helper element
     * @param placeholder the placeholder element
     * @param target the drop target
     * @param handler the drag and drop handler
     */
    private void prepareDragInfo(Element dragHelper, Element placeholder, I_CmsDropTarget target, CmsDNDHandler handler) {

        target.getElement().appendChild(dragHelper);
        // preparing helper styles
        int width = CmsDomUtil.getCurrentStyleInt(dragHelper, CmsDomUtil.Style.width);
        Style style = dragHelper.getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setWidth(width, Unit.PX);
        style.setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexDND());
        dragHelper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        dragHelper.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        if (!CmsDomUtil.hasBackgroundImage(dragHelper)) {
            dragHelper.getStyle().setBackgroundColor(CmsDomUtil.getEffectiveBackgroundColor(dragHelper));
        }
        if (!CmsDomUtil.hasBorder(dragHelper)) {
            dragHelper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        }
        style.setDisplay(Display.NONE);

        String positioning = CmsDomUtil.getCurrentStyle(
            target.getElement(),
            org.opencms.gwt.client.util.CmsDomUtil.Style.position);
        // set target relative, if not absolute or fixed
        if (!Position.ABSOLUTE.getCssName().equals(positioning) && !Position.FIXED.getCssName().equals(positioning)) {
            target.getElement().getStyle().setPosition(Position.RELATIVE);
        }
        setMinHeight(target);
        m_dragInfos.put(target, new DragInfo(dragHelper, placeholder, width - 15, handler.getCursorOffsetY()));
        handler.addTarget(target);

        // adding drag handle
        Element button = DOM.createDiv();
        button.appendChild((new Image(I_CmsImageBundle.INSTANCE.icons().moveIconActive())).getElement());
        button.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragHandle());
        dragHelper.appendChild(button);
    }

    /**
     * Prepares the target container.<p>
     * 
     * @param targetContainer the container
     * @param draggable the draggable
     * @param placeholder the placeholder
     */
    private void prepareTargetContainer(
        I_CmsDropContainer targetContainer,
        I_CmsDraggable draggable,
        Element placeholder) {

        String positioning = CmsDomUtil.getCurrentStyle(
            targetContainer.getElement(),
            org.opencms.gwt.client.util.CmsDomUtil.Style.position);
        // set target relative, if not absolute or fixed
        if (!Position.ABSOLUTE.getCssName().equals(positioning) && !Position.FIXED.getCssName().equals(positioning)) {
            targetContainer.getElement().getStyle().setPosition(Position.RELATIVE);
        }
        m_originalIndex = targetContainer.getWidgetIndex((Widget)draggable);
        targetContainer.getElement().insertBefore(placeholder, draggable.getElement());
        draggable.getElement().getStyle().setDisplay(Display.NONE);
        targetContainer.highlightContainer();
    }

    /**
     * Removes the drag overlay.<p>
     */
    private void removeDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
            m_dragOverlay = null;
        }
    }

    /**
     * Replaces the current drag helper and place-holder in the drag handler sets them both to visible.<p>
     *  
     * @param handler the drag and drop handler
     * @param info the drag info referencing the replacement helpers
     */
    private void replaceCurrentHelpers(CmsDNDHandler handler, DragInfo info) {

        handler.setDragHelper(info.getDragHelper());
        handler.setPlaceholder(info.getPlaceholder());
        handler.setCursorOffsetX(info.getOffsetX());
        handler.setCursorOffsetY(info.getOffsetY());
        handler.getDragHelper().getStyle().setDisplay(Display.BLOCK);
        handler.getPlaceholder().getStyle().clearDisplay();
    }

    /**
     * Restores the minimum heights of containers.<p>
     */
    private void restoreMinHeights() {

        for (CmsStyleSaver savedMinHeight : m_savedMinHeights) {
            savedMinHeight.restore();
        }
        m_savedMinHeights.clear();
    }

    /**
     * Saves the minimum height of a container and sets it to the current height.<p>
     * 
     * @param target the target container 
     */
    private void setMinHeight(I_CmsDropTarget target) {

        if (target instanceof CmsContainerPageContainer) {
            CmsContainerPageContainer cont = (CmsContainerPageContainer)target;
            String realHeight = CmsDomUtil.getCurrentStyle(cont.getElement(), CmsDomUtil.Style.height);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(realHeight)
                && (Double.parseDouble(realHeight.replace("px", "")) > MIN_HEIGHT_THRESHOLD)) {
                m_savedMinHeights.add(new CmsStyleSaver(cont.getElement(), "minHeight"));
                Style style = cont.getElement().getStyle();
                style.setProperty("minHeight", realHeight);
            }
        }
    }

    /**
     * Shows the draggable on it's original position.<p>
     * 
     * @param draggable the draggable
     * @param withOverlay <code>true</code> to show the disabling overlay
     */
    private void showOriginalPositionPlaceholder(I_CmsDraggable draggable, boolean withOverlay) {

        draggable.getElement().getStyle().clearDisplay();
        CmsDomUtil.showOverlay(draggable.getElement(), withOverlay);
    }

    /** 
     * Function which is called when the drag process is stopped, either by cancelling or dropping.<p>
     * 
     * @param handler the drag and drop handler 
     */
    private void stopDrag(final CmsDNDHandler handler) {

        removeDragOverlay();
        CmsContainerpageEditor.getZIndexManager().stop();
        restoreMinHeights();
        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            if (Position.RELATIVE.getCssName().equals(target.getElement().getStyle().getPosition())) {
                target.getElement().getStyle().clearPosition();
            }
            m_dragInfos.get(target).getDragHelper().removeFromParent();
            if (target instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)target).removeHighlighting();
            }
        }
        m_currentTarget = null;
        m_currentIndex = -1;
        m_isNew = false;
        m_controller.getHandler().deactivateMenuButton();
        final List<I_CmsDropTarget> dragTargets = new ArrayList<I_CmsDropTarget>(m_dragInfos.keySet());
        m_dragInfos.clear();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                handler.clearTargets();
                m_controller.resetEditButtons();
                // in case of group editing, this will refresh the container position and size
                for (I_CmsDropTarget target : dragTargets) {
                    if (target instanceof I_CmsDropContainer) {
                        ((I_CmsDropContainer)target).refreshHighlighting();
                    }
                }
            }
        });
        if (handler.getDraggable() instanceof CmsContainerPageElementPanel) {
            ((CmsContainerPageElementPanel)(handler.getDraggable())).removeHighlighting();
        }
    }

    /**
     * Updates the drag target highlighting.<p>
     */
    private void updateHighlighting() {

        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            if (target instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)target).refreshHighlighting();
            }
        }
    }
}