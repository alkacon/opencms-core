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
import org.opencms.ade.containerpage.client.ui.CmsDroppedElementModeSelectionDialog;
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementReuseMode;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.contenteditor.shared.CmsEditorConstants;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Objects;
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
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page editor drag and drop controller.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageDNDController implements I_CmsDNDController {

    /** The container highlighting offset. */
    public static final int HIGHLIGHTING_OFFSET = 4;

    /** The minimum margin set to empty containers. */
    private static final int MINIMUM_CONTAINER_MARGIN = 10;

    /** The container page controller. */
    protected CmsContainerpageController m_controller;

    /** The id of the dragged element. */
    protected String m_draggableId;

    /** The current place holder index. */
    private int m_currentIndex = -1;

    /** The current drop target. */
    private I_CmsDropTarget m_currentTarget;

    /** Map of current drag info beans. */
    private Map<I_CmsDropTarget, Element> m_dragInfos;

    /** The drag overlay. */
    private Element m_dragOverlay;

    /** The ionitial drop target. */
    private I_CmsDropTarget m_initialDropTarget;

    /** Creating new flag. */
    private boolean m_isNew;

    /** The id of the container from which an element was dragged. */
    private String m_originalContainerId;

    /** The original position of the draggable. */
    private int m_originalIndex;

    /**
     * Constructor.<p>
     * 
     * @param controller the container page controller
     */
    public CmsContainerpageDNDController(CmsContainerpageController controller) {

        m_controller = controller;
        m_dragInfos = new HashMap<I_CmsDropTarget, Element>();
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
    public boolean onDragStart(final I_CmsDraggable draggable, I_CmsDropTarget target, final CmsDNDHandler handler) {

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

        String containerId = null;
        if (target instanceof CmsContainerPageContainer) {
            containerId = ((CmsContainerPageContainer)target).getContainerId();
        }
        m_originalContainerId = containerId;

        if (target != null) {
            handler.addTarget(target);
            if (target instanceof I_CmsDropContainer) {
                prepareTargetContainer((I_CmsDropContainer)target, draggable, handler.getPlaceholder());
                Element helper = handler.getDragHelper();
                handler.setCursorOffsetX(helper.getOffsetWidth() - 15);
                handler.setCursorOffsetY(20);
            }
        }

        m_dragInfos.put(target, handler.getPlaceholder());
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

                prepareHelperElements(arg, handler, draggable);
            }
        };
        if (isNewId(clientId)) {
            // for new content elements dragged from the gallery menu, the given id contains the resource type name
            m_isNew = true;
            m_controller.getNewElement(clientId, callback);
        } else {
            if (isCopyModel(draggable)) {
                m_controller.copyElement(draggable.getId(), new AsyncCallback<CmsUUID>() {

                    public void onFailure(Throwable caught) {

                        // will never be called 
                    }

                    @SuppressWarnings("synthetic-access")
                    public void onSuccess(CmsUUID result) {

                        String idString = result.toString();
                        m_draggableId = idString;

                        m_controller.getElementForDragAndDropFromContainer(idString, m_originalContainerId, callback);
                    }
                });
            } else {
                m_controller.getElementForDragAndDropFromContainer(clientId, m_originalContainerId, callback);
            }

        }
        if (target instanceof CmsContainerPageContainer) {
            CmsContainerpageEditor.getZIndexManager().start(containerId);
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
        boolean isListItem = draggable instanceof CmsListItem;
        CmsContainerPageElementPanel listContainerElement = null;
        final boolean[] triggerReload = {false};
        boolean hasWritePermissions = true;

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
                        containerElement.setNewType(CmsContainerpageController.getServerId(m_draggableId));
                    } else {
                        CmsContainerElementData elementData = m_controller.getCachedElement(m_draggableId);
                        hasWritePermissions = elementData.hasWritePermission();
                        if ((elementData.getDndId() != null)
                            && (null != m_controller.getCachedElement(elementData.getDndId()))) {
                            elementData = m_controller.getCachedElement(elementData.getDndId());
                        }
                        containerElement = m_controller.getContainerpageUtil().createElement(elementData, container);
                        if (isListItem) {
                            listContainerElement = containerElement;
                        }
                        m_controller.addToRecentList(elementData.getClientId(), null);
                    }
                    handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
                    if (container.getPlaceholderIndex() >= container.getWidgetCount()) {
                        container.add(containerElement);
                    } else {
                        container.insert(containerElement, container.getPlaceholderIndex());
                    }
                    // changes are only relevant to the container page if not group-container editing
                    changedContainerpage = !m_controller.isGroupcontainerEditing();
                    triggerReload[0] = containerElement.getElement().getInnerHTML().contains(
                        CmsGwtConstants.FORMATTER_RELOAD_MARKER)
                        && changedContainerpage
                        && !m_isNew;

                    if (draggable instanceof CmsContainerPageElementPanel) {
                        ((CmsContainerPageElementPanel)draggable).removeFromParent();
                    }
                    m_controller.initializeSubContainers(containerElement);
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
            CmsDomUtil.showOverlay(draggable.getElement(), false);
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
            CmsDomUtil.showOverlay(draggable.getElement(), false);
            // to reset mouse over state remove and attach the option bar 
            CmsContainerPageElementPanel containerElement = (CmsContainerPageElementPanel)draggable;
            CmsElementOptionBar optionBar = containerElement.getElementOptionBar();
            optionBar.removeFromParent();
            containerElement.setElementOptionBar(optionBar);
        }
        final Runnable checkReload = new Runnable() {

            public void run() {

                if (triggerReload[0]) {
                    CmsContainerpageController.get().reloadPage();
                }
            }

        };

        if (listContainerElement != null) {
            final CmsContainerPageElementPanel finalListContainerElement = listContainerElement;
            final String serverIdStr = CmsContainerpageController.getServerId(m_draggableId);
            CmsUUID structureId = new CmsUUID(serverIdStr);

            AsyncCallback<String> modeCallback = new AsyncCallback<String>() {

                public void onFailure(Throwable caught) {

                    finalListContainerElement.removeFromParent();
                }

                public void onSuccess(String result) {

                    if (Objects.equal(result, CmsEditorConstants.MODE_COPY)) {
                        final CmsContainerpageController controller = CmsContainerpageController.get();
                        CmsContainerElementData data = controller.getCachedElement(finalListContainerElement.getId());
                        final Map<String, String> settings = data.getSettings();
                        controller.copyElement(serverIdStr, new AsyncCallback<CmsUUID>() {

                            public void onFailure(Throwable caught) {

                                // this should never happen, in theory

                            }

                            public void onSuccess(CmsUUID resultId) {

                                controller.getElementWithSettings(
                                    "" + resultId,
                                    settings,
                                    new I_CmsSimpleCallback<CmsContainerElementData>() {

                                        public void execute(CmsContainerElementData newData) {

                                            try {
                                                controller.replaceContainerElement(finalListContainerElement, newData);
                                                controller.setPageChanged(checkReload);
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    });
                            }
                        });

                    } else if (Objects.equal(result, CmsEditorConstants.MODE_REUSE)) {
                        m_controller.setPageChanged(checkReload);
                    }

                }
            };
            ElementReuseMode reuseMode = CmsContainerpageController.get().getData().getElementReuseMode();
            if (!hasWritePermissions) {
                // User is not allowed to create this element in current view, so reuse the element instead 
                reuseMode = ElementReuseMode.reuse;
            }
            switch (reuseMode) {
                case ask:
                    // when dropping elements from the into the page, we ask the user if the dropped element should 
                    // be used, or a copy of it. If the user wants a copy, we copy the corresponding resource and replace the element
                    // in the page 
                    CmsDroppedElementModeSelectionDialog.showDialog(structureId, modeCallback);
                    break;
                case copy:
                    modeCallback.onSuccess(CmsEditorConstants.MODE_COPY);
                    break;
                case reuse:
                default:
                    modeCallback.onSuccess(CmsEditorConstants.MODE_REUSE);
                    break;
            }
        } else {
            if (changedContainerpage) {
                m_controller.setPageChanged(checkReload);
            }
        }
        stopDrag(handler);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (hasChangedPosition(target)) {
            checkPlaceholderVisibility(target);
            updateHighlighting(false);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        Element placeholder = m_dragInfos.get(target);
        if (placeholder != null) {
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            handler.setPlaceholder(placeholder);
            placeholder.getStyle().clearDisplay();
            if ((target != m_initialDropTarget) && (target instanceof I_CmsDropContainer)) {
                ((I_CmsDropContainer)target).checkMaxElementsOnEnter();
            }
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

        m_currentTarget = null;
        m_currentIndex = -1;
        Element placeholder = m_dragInfos.get(m_initialDropTarget);
        if (placeholder != null) {
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            handler.setPlaceholder(placeholder);
            placeholder.getStyle().setDisplay(Display.NONE);
            if ((target != m_initialDropTarget) && (target instanceof I_CmsDropContainer)) {
                ((I_CmsDropContainer)target).checkMaxElementsOnLeave();
            }
        }
        updateHighlighting(false);
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
     * @param draggable the draggable
     */
    protected void prepareHelperElements(
        CmsContainerElementData elementData,
        CmsDNDHandler handler,
        I_CmsDraggable draggable) {

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

        if ((elementData.getDndId() != null) && (m_controller.getCachedElement(elementData.getDndId()) != null)) {
            m_draggableId = elementData.getDndId();
            elementData = m_controller.getCachedElement(m_draggableId);
        } else {
            m_draggableId = elementData.getClientId();
        }
        if (m_controller.isGroupcontainerEditing()) {
            CmsGroupContainerElementPanel groupContainer = m_controller.getGroupcontainer();
            if ((groupContainer != m_initialDropTarget)
                && !(elementData.isGroupContainer() || elementData.isInheritContainer())
                && (elementData.getContents().get(groupContainer.getContainerId()) != null)) {
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
                    placeholder = CmsDomUtil.createElement(htmlContent);
                    // ensure any embedded flash players are set opaque so UI elements may be placed above them
                    CmsDomUtil.fixFlashZindex(placeholder);
                    placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine(e.getMessage());
                }

                if (placeholder != null) {
                    prepareDragInfo(placeholder, groupContainer, handler);
                    groupContainer.highlightContainer();
                }
            }
            return;
        }
        if (!m_controller.isEditingDisabled()) {
            for (CmsContainerPageContainer container : m_controller.getContainerTargets().values()) {
                if (draggable.getElement().isOrHasChild(container.getElement())) {
                    // skip containers that are children of the draggable element
                    continue;
                }
                if ((container != m_initialDropTarget)
                    && !container.isDetailView()
                    && (elementData.getContents().get(container.getContainerId()) != null)) {

                    Element placeholder = null;
                    if (elementData.isGroupContainer() || elementData.isInheritContainer()) {
                        placeholder = DOM.createDiv();
                        String content = "";
                        for (String groupId : elementData.getSubItems()) {
                            CmsContainerElementData subData = m_controller.getCachedElement(groupId);
                            if (subData != null) {
                                if (subData.isShowInContext(CmsContainerpageController.get().getData().getTemplateContextInfo().getCurrentContext())) {
                                    if ((subData.getContents().get(container.getContainerId()) != null)) {
                                        content += subData.getContents().get(container.getContainerId());
                                    }
                                } else {
                                    content += "<div class='"
                                        + CmsTemplateContextInfo.DUMMY_ELEMENT_MARKER
                                        + "' style='display: none !important;'></div>";
                                }
                            }
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
                            placeholder.setInnerHTML(content);
                            // ensure any embedded flash players are set opaque so UI elements may be placed above them
                            CmsDomUtil.fixFlashZindex(placeholder);
                        } else {
                            placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
                        }
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
                            placeholder = CmsDomUtil.createElement(htmlContent);
                            // ensure any embedded flash players are set opaque so UI elements may be placed above them
                            CmsDomUtil.fixFlashZindex(placeholder);
                            placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
                        } catch (Exception e) {
                            CmsDebugLog.getInstance().printLine(e.getMessage());
                        }
                    }
                    if (placeholder != null) {
                        prepareDragInfo(placeholder, container, handler);
                    }
                }
            }
            initNestedContainers();
            // add highlighting after all drag targets have been initialized
            updateHighlighting(true);
        }
    }

    /**
     * Checks and sets if the place holder should be visible.<p>
     *  
     * @param target the target container
     */
    private void checkPlaceholderVisibility(I_CmsDropTarget target) {

        if (target instanceof I_CmsDropContainer) {
            I_CmsDropContainer container = (I_CmsDropContainer)target;
            container.setPlaceholderVisibility((container != m_initialDropTarget)
                || ((m_currentIndex - m_originalIndex) > 1)
                || ((m_originalIndex - m_currentIndex) > 0));
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
     * Initializes the nested container infos.<p>
     */
    private void initNestedContainers() {

        for (CmsContainer container : m_controller.getContainers().values()) {
            if (container.isSubContainer()) {
                CmsContainerPageContainer containerWidget = m_controller.m_targetContainers.get(container.getName());
                // check if the sub container is a valid drop targets
                if (m_dragInfos.keySet().contains(containerWidget)) {
                    CmsContainer parentContainer = m_controller.getContainers().get(container.getParentContainerName());
                    // add the container to all it's ancestors as a dnd child
                    while (parentContainer != null) {
                        if (m_dragInfos.keySet().contains(
                            m_controller.m_targetContainers.get(parentContainer.getName()))) {
                            m_controller.m_targetContainers.get(parentContainer.getName()).addDndChild(containerWidget);
                        }
                        if (parentContainer.isSubContainer()) {
                            parentContainer = m_controller.getContainers().get(parentContainer.getParentContainerName());
                        } else {
                            parentContainer = null;
                        }
                    }
                }
            }

        }
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
     * @param placeholder the placeholder element
     * @param target the drop target
     * @param handler the drag and drop handler
     */
    private void prepareDragInfo(Element placeholder, I_CmsDropContainer target, CmsDNDHandler handler) {

        target.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        String positioning = CmsDomUtil.getCurrentStyle(
            target.getElement(),
            org.opencms.gwt.client.util.CmsDomUtil.Style.position);
        // set target relative, if not absolute or fixed
        if (!Position.ABSOLUTE.getCssName().equals(positioning) && !Position.FIXED.getCssName().equals(positioning)) {
            target.getElement().getStyle().setPosition(Position.RELATIVE);
            // check for empty containers that don't have a minimum top and bottom margin to avoid containers overlapping
            if (target.getElement().getFirstChildElement() == null) {
                if (CmsDomUtil.getCurrentStyleInt(target.getElement(), CmsDomUtil.Style.marginTop) < MINIMUM_CONTAINER_MARGIN) {
                    target.getElement().getStyle().setMarginTop(MINIMUM_CONTAINER_MARGIN, Unit.PX);
                }
                if (CmsDomUtil.getCurrentStyleInt(target.getElement(), CmsDomUtil.Style.marginBottom) < MINIMUM_CONTAINER_MARGIN) {
                    target.getElement().getStyle().setMarginBottom(MINIMUM_CONTAINER_MARGIN, Unit.PX);
                }
            }
        }
        m_dragInfos.put(target, placeholder);
        handler.addTarget(target);
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
        targetContainer.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        CmsDomUtil.showOverlay(draggable.getElement(), true);
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
     * Function which is called when the drag process is stopped, either by cancelling or dropping.<p>
     * 
     * @param handler the drag and drop handler 
     */
    private void stopDrag(final CmsDNDHandler handler) {

        removeDragOverlay();
        CmsContainerpageEditor.getZIndexManager().stop();
        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            target.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
            target.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().clearFix());
            Style targetStyle = target.getElement().getStyle();
            if (!(target instanceof CmsGroupContainerElementPanel)) {
                targetStyle.clearPosition();
            }
            targetStyle.clearMarginTop();
            targetStyle.clearMarginBottom();
            if (target instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)target).removeHighlighting();
            }
        }
        handler.getDragHelper().removeFromParent();
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
                        // reset the nested container infos
                        ((I_CmsDropContainer)target).clearDnDChildren();
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
     * 
     * @param initial <code>true</code> when initially highlighting the drop containers 
     */
    private void updateHighlighting(boolean initial) {

        Map<I_CmsDropContainer, CmsPositionBean> containers = new HashMap<I_CmsDropContainer, CmsPositionBean>();
        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            if ((target instanceof I_CmsDropContainer)
                && !Display.NONE.getCssName().equalsIgnoreCase(
                    CmsDomUtil.getCurrentStyle(target.getElement(), CmsDomUtil.Style.display))) {
                if (initial && (target != m_initialDropTarget)) {
                    ((I_CmsDropContainer)target).highlightContainer();
                } else {
                    ((I_CmsDropContainer)target).updatePositionInfo();
                }
                containers.put((I_CmsDropContainer)target, ((I_CmsDropContainer)target).getPositionInfo());
            }
        }
        List<I_CmsDropContainer> containersToMatch = new ArrayList<I_CmsDropContainer>(containers.keySet());
        for (I_CmsDropContainer contA : containers.keySet()) {
            containersToMatch.remove(contA);
            for (I_CmsDropContainer contB : containersToMatch) {
                CmsPositionBean posA = containers.get(contA);
                CmsPositionBean posB = containers.get(contB);
                if (CmsPositionBean.checkCollision(posA, posB, HIGHLIGHTING_OFFSET * 3)) {
                    if (contA.hasDnDChildren() && contA.getDnDChildren().contains(contB)) {
                        if (!posA.isInside(posB, HIGHLIGHTING_OFFSET)) {
                            // the nested container is not completely inside the other
                            // increase the size of the outer container
                            posA.ensureSurrounds(posB, HIGHLIGHTING_OFFSET);
                        }
                    } else if (contB.hasDnDChildren() && contB.getDnDChildren().contains(contA)) {
                        if (!posB.isInside(posA, HIGHLIGHTING_OFFSET)) {
                            // the nested container is not completely inside the other
                            // increase the size of the outer container
                            posB.ensureSurrounds(posA, HIGHLIGHTING_OFFSET);
                        }
                    } else {
                        CmsPositionBean.avoidCollision(posA, posB, HIGHLIGHTING_OFFSET * 3);
                    }
                }
            }
        }

        for (Entry<I_CmsDropContainer, CmsPositionBean> containerEntry : containers.entrySet()) {
            containerEntry.getKey().refreshHighlighting(containerEntry.getValue());
        }
    }
}
