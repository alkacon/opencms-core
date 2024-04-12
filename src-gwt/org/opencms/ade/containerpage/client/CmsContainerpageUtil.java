/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsMenuListItem;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableData;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;

/**
 * Utility class for the container-page editor.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerpageUtil {

    /** The container page controller. */
    private CmsContainerpageController m_controller;

    /** List of buttons of the tool-bar. */
    private A_CmsToolbarOptionButton[] m_optionButtons;

    /**
     * Constructor.<p>
     *
     * @param controller the container page controller
     * @param optionButtons the tool-bar option buttons
     */
    public CmsContainerpageUtil(CmsContainerpageController controller, A_CmsToolbarOptionButton... optionButtons) {

        m_controller = controller;
        m_optionButtons = optionButtons;
    }

    /**
     * Adds an option bar to the given drag element.<p>
     *
     * @param element the element
     */
    public void addOptionBar(CmsContainerPageElementPanel element) {

        // the view permission is required for any actions regarding this element
        if (element.hasViewPermission()) {
            CmsElementOptionBar optionBar = CmsElementOptionBar.createOptionBarForElement(
                element,
                m_controller.getDndHandler(),
                m_optionButtons);
            element.setElementOptionBar(optionBar);
        }
    }

    /**
     * Transforms all contained elements into {@link CmsContainerPageElementPanel}.<p>
     *
     * @param container the container
     */
    public void consumeContainerElements(I_CmsDropContainer container) {

        boolean containsElements = false;
        // the drag element widgets are created from the existing DOM elements,
        Element child = container.getElement().getFirstChildElement();
        List<CmsContainerPageElementPanel> children = Lists.newArrayList();
        while (child != null) {
            boolean isContainerElement = CmsDomUtil.hasClass(
                CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER,
                child);
            boolean isGroupcontainerElement = CmsDomUtil.hasClass(
                CmsContainerElement.CLASS_GROUP_CONTAINER_ELEMENT_MARKER,
                child);
            if (isContainerElement || isGroupcontainerElement) {
                containsElements = true;
                String serializedData = child.getAttribute(CmsGwtConstants.ATTR_DATA_ELEMENT);
                child.removeAttribute(CmsGwtConstants.ATTR_DATA_ELEMENT);
                CmsContainerElement elementData = null;
                try {
                    elementData = m_controller.getSerializedElement(serializedData);
                } catch (Exception e) {
                    CmsErrorDialog.handleException(
                        new Exception(
                            "Deserialization of element data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                            e));
                }
                if (isContainerElement) {

                    // searching for content element root
                    Element elementRoot = (Element)child.getNextSibling();
                    while ((elementRoot != null) && (elementRoot.getNodeType() != Node.ELEMENT_NODE)) {
                        Element temp = elementRoot;
                        elementRoot = (Element)elementRoot.getNextSibling();
                        temp.removeFromParent();
                    }
                    if (elementRoot == null) {
                        child.removeFromParent();
                        child = null;
                        continue;
                    }
                    if (CmsDomUtil.hasClass(CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER, elementRoot)) {
                        // broken element, already at next start marker
                        if (elementData != null) {
                            alertParsingError(elementData.getSitePath());
                        }
                        child.removeFromParent();
                        child = elementRoot;
                        continue;
                    }
                    if (CmsDomUtil.hasClass(CmsContainerElement.CLASS_CONTAINER_ELEMENT_END_MARKER, elementRoot)) {
                        // broken element, no content element root
                        if (elementData != null) {
                            alertParsingError(elementData.getSitePath());
                        }
                        child.removeFromParent();
                        child = elementRoot.getNextSiblingElement();
                        elementRoot.removeFromParent();
                        continue;
                    } else {
                        // looking for the next marker that wraps the current element
                        Element endMarker = (Element)elementRoot.getNextSibling();
                        // only if the end marker node is not null and has neither the end-marker class or start-marker class
                        // remove the current node and check the next sibling
                        while (!((endMarker == null)
                            || ((endMarker.getNodeType() == Node.ELEMENT_NODE)
                                && (CmsDomUtil.hasClass(
                                    CmsContainerElement.CLASS_CONTAINER_ELEMENT_END_MARKER,
                                    endMarker)
                                    || CmsDomUtil.hasClass(
                                        CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER,
                                        endMarker))))) {
                            Element temp = endMarker;
                            endMarker = (Element)endMarker.getNextSibling();
                            temp.removeFromParent();
                        }
                        if (endMarker == null) {
                            if (elementData != null) {
                                alertParsingError(elementData.getSitePath());
                            }
                            // broken element, end marker missing
                            elementRoot.removeFromParent();
                            child.removeFromParent();
                            child = null;
                            continue;
                        }
                        if (CmsDomUtil.hasClass(CmsContainerElement.CLASS_CONTAINER_ELEMENT_START_MARKER, endMarker)) {
                            if (elementData != null) {
                                alertParsingError(elementData.getSitePath());
                            }
                            // broken element, end marker missing
                            elementRoot.removeFromParent();
                            child.removeFromParent();
                            child = endMarker;
                        }
                        if (elementData == null) {
                            // deserialization failed, remove whole element
                            child.removeFromParent();
                            elementRoot.removeFromParent();
                            child = endMarker.getNextSiblingElement();
                            endMarker.removeFromParent();
                            continue;
                        }
                        CmsDomUtil.removeScriptTags(elementRoot);
                        CmsContainerPageElementPanel containerElement = createElement(
                            elementRoot,
                            container,
                            elementData);
                        children.add(containerElement);
                        if (elementData.isNew()) {
                            containerElement.setNewType(elementData.getResourceType());
                        }
                        container.adoptElement(containerElement);
                        child.removeFromParent();
                        child = endMarker.getNextSiblingElement();
                        endMarker.removeFromParent();
                    }
                } else if (isGroupcontainerElement && (container instanceof CmsContainerPageContainer)) {
                    if (elementData == null) {
                        Element sibling = child.getNextSiblingElement();
                        container.getElement().removeChild(child);
                        child = sibling;
                        continue;
                    }
                    CmsDomUtil.removeScriptTags(child);
                    CmsGroupContainerElementPanel groupContainer = createGroupcontainer(child, container, elementData);
                    groupContainer.setContainerId(container.getContainerId());
                    container.adoptElement(groupContainer);
                    consumeContainerElements(groupContainer);
                    if (groupContainer.getWidgetCount() == 0) {
                        groupContainer.addStyleName(
                            I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
                    }
                    children.add(groupContainer);
                    // important: adding the option-bar only after the group-containers have been consumed
                    if (container.isEditable()
                        && (!m_controller.isDetailPage() || container.isDetailView() || container.isDetailOnly())) {
                        //only allow editing if either element of detail only container or not in detail view
                        if (m_controller.requiresOptionBar(groupContainer, container)) {
                            addOptionBar(groupContainer);
                        }
                    }
                    child = child.getNextSiblingElement();
                }
            } else {
                Element sibling = child.getNextSiblingElement();
                if (!containsElements && (sibling == null) && (container instanceof CmsContainerPageContainer)) {
                    // this element is no container element and is the container only child, assume it is an empty container marker
                    ((CmsContainerPageContainer)container).setEmptyContainerElement(child);
                } else {
                    // e.g. option bar
                    if (!CmsContainerPageElementPanel.isOverlay(child)) {
                        container.getElement().removeChild(child);
                    }
                }
                child = sibling;
                continue;
            }
        }
        container.onConsumeChildren(children);
    }

    /**
     * The method will create {@link CmsContainerPageContainer} object for all given containers
     * by converting the associated DOM elements. The contained elements will be transformed into {@link CmsContainerPageElementPanel}.<p>
     *
     * @param containers the container data
     * @param context the parent element to the containers
     *
     * @return the drag target containers
     */
    public Map<String, CmsContainerPageContainer> consumeContainers(
        Map<String, CmsContainer> containers,
        Element context) {

        Map<String, CmsContainerPageContainer> result = new HashMap<String, CmsContainerPageContainer>();
        List<Element> containerElements = CmsDomUtil.getElementsByClass(CmsContainerElement.CLASS_CONTAINER, context);
        for (Element containerElement : containerElements) {
            String data = containerElement.getAttribute(CmsGwtConstants.ATTR_DATA_CONTAINER);
            try {
                CmsContainer container = m_controller.getSerializedContainer(data);
                containers.put(container.getName(), container);
                try {
                    CmsContainerPageContainer dragContainer = new CmsContainerPageContainer(
                        container,
                        containerElement);
                    consumeContainerElements(dragContainer);
                    result.put(container.getName(), dragContainer);
                } catch (Exception e) {
                    CmsErrorDialog.handleException(
                        "Error parsing container "
                            + container.getName()
                            + ". Please check if your HTML is well formed.",
                        e);
                }
            } catch (Exception e) {
                CmsErrorDialog.handleException(
                    new Exception(
                        "Deserialization of container data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                        e));
            }
            containerElement.removeAttribute(CmsGwtConstants.ATTR_DATA_CONTAINER);
        }
        return result;
    }

    /**
     * Creates an drag container element.<p>
     *
     * @param containerElement the container element data
     * @param container the container parent
     * @param isNew in case of a newly created element
     *
     * @return the draggable element
     *
     * @throws Exception if something goes wrong
     */
    public CmsContainerPageElementPanel createElement(
        CmsContainerElementData containerElement,
        I_CmsDropContainer container,
        boolean isNew)
    throws Exception {

        if (containerElement.isGroupContainer() || containerElement.isInheritContainer()) {
            List<CmsContainerElementData> subElements = new ArrayList<CmsContainerElementData>();
            for (String subId : containerElement.getSubItems()) {
                CmsContainerElementData element = m_controller.getCachedElement(subId);
                if (element != null) {
                    subElements.add(element);
                } else {
                    CmsDebugLog.getInstance().printLine("Cached element not found");
                }
            }
            return createGroupcontainerElement(containerElement, subElements, container);
        }
        Element element = CmsDomUtil.createElement(containerElement.getContents().get(container.getContainerId()));
        // ensure any embedded flash players are set opaque so UI elements may be placed above them
        CmsDomUtil.fixFlashZindex(element);
        if (isNew) {
            CmsContentEditor.replaceResourceIds(
                element,
                CmsUUID.getNullUUID().toString(),
                CmsContainerpageController.getServerId(containerElement.getClientId()));
        }

        CmsContainerPageElementPanel result = createElement(element, container, containerElement);
        if (!CmsContainerpageController.get().shouldShowInContext(containerElement)) {
            result.getElement().getStyle().setDisplay(Style.Display.NONE);
            result.addStyleName(CmsTemplateContextInfo.DUMMY_ELEMENT_MARKER);
        }
        return result;
    }

    /**
     * Creates a drag container element for group-container elements.<p>
     *
     * @param containerElement the container element data
     * @param subElements the sub-elements
     * @param container the drag parent
     *
     * @return the draggable element
     *
     * @throws Exception if something goes wrong
     */
    public CmsContainerPageElementPanel createGroupcontainerElement(
        CmsContainerElementData containerElement,
        List<CmsContainerElementData> subElements,
        I_CmsDropContainer container)
    throws Exception {

        Element element = DOM.createDiv();
        element.addClassName(CmsContainerElement.CLASS_GROUP_CONTAINER_ELEMENT_MARKER);
        CmsGroupContainerElementPanel groupContainer = createGroupcontainer(element, container, containerElement);
        groupContainer.setContainerId(container.getContainerId());
        //adding sub-elements
        Iterator<CmsContainerElementData> it = subElements.iterator();
        while (it.hasNext()) {
            CmsContainerElementData subElement = it.next();
            if (subElement.getContents().containsKey(container.getContainerId())) {
                CmsContainerPageElementPanel subDragElement = createElement(subElement, groupContainer, false);
                groupContainer.add(subDragElement);
            }
        }
        if (subElements.size() == 0) {
            groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
        }
        addOptionBar(groupContainer);
        return groupContainer;
    }

    /**
     * Creates a list item.<p>
     *
     * @param containerElement the element data
     *
     * @return the list item widget
     */
    public CmsMenuListItem createListItem(final CmsContainerElementData containerElement) {

        CmsMenuListItem listItem = new CmsMenuListItem(containerElement);
        if (!containerElement.isGroupContainer()
            && !containerElement.isInheritContainer()
            && CmsStringUtil.isEmptyOrWhitespaceOnly(containerElement.getNoEditReason())) {
            listItem.enableEdit(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    CmsEditableData editableData = new CmsEditableData();
                    editableData.setElementLanguage(CmsCoreProvider.get().getLocale());
                    editableData.setStructureId(
                        new CmsUUID(CmsContainerpageController.getServerId(containerElement.getClientId())));
                    editableData.setSitePath(containerElement.getSitePath());
                    getController().getContentEditorHandler().openDialog(editableData, false, null, null, null);
                    ((CmsPushButton)event.getSource()).clearHoverState();
                }
            });
        } else {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(containerElement.getNoEditReason())) {
                listItem.disableEdit(containerElement.getNoEditReason(), true);
            } else {
                listItem.disableEdit(Messages.get().key(Messages.GUI_CLIPBOARD_ITEM_CAN_NOT_BE_EDITED_0), false);
            }
        }
        String clientId = containerElement.getClientId();
        String serverId = CmsContainerpageController.getServerId(clientId);
        if (CmsUUID.isValidUUID(serverId)) {
            CmsContextMenuButton button = new CmsContextMenuButton(new CmsUUID(serverId), new CmsContextMenuHandler() {

                @Override
                public void refreshResource(CmsUUID structureId) {

                    Window.Location.reload();
                }
            }, AdeContext.gallery);
            listItem.getListItemWidget().addButtonToFront(button);
        }
        if (!containerElement.isAddDisabled()) {
            listItem.initMoveHandle(m_controller.getDndHandler(), true);
        }
        return listItem;
    }

    /**
     * Returns the container page controller.<p>
     *
     * @return the container page controller
     */
    protected CmsContainerpageController getController() {

        return m_controller;
    }

    /**
     * Displays the element parsing error dialog.<p>
     *
     * @param sitePath the element site path
     */
    private void alertParsingError(String sitePath) {

        new CmsErrorDialog(
            "Error parsing element "
                + sitePath
                + ". Please check if the HTML generated by the element formatter is well formed.",
            null).center();
    }

    /**
     * Creates an drag container element.<p>
     *
     * @param element the DOM element
     * @param dragParent the drag parent
     * @param elementData the element data
     *
     * @return the draggable element
     */
    private CmsContainerPageElementPanel createElement(
        Element element,
        I_CmsDropContainer dragParent,
        CmsContainerElement elementData) {

        CmsContainerPageElementPanel dragElement = new CmsContainerPageElementPanel(
            element,
            dragParent,
            elementData.getClientId(),
            elementData.getSitePath(),
            elementData.getNoEditReason(),
            elementData.getLockInfo(),
            elementData.getTitle(),
            elementData.getSubTitle(),
            elementData.getResourceType(),
            elementData.hasSettings(dragParent.getContainerId()),
            elementData.hasViewPermission(),
            elementData.hasWritePermission(),
            elementData.isReleasedAndNotExpired(),
            elementData.isNewEditorDisabled(),
            elementData.hasEditHandler(),
            elementData.getModelGroupId(),
            elementData.isWasModelGroup(),
            elementData.getElementView(),
            elementData.getBigIconClasses(),
            elementData.isReused());
        dragElement.setCreateNew(elementData.isCreateNew());
        if (m_controller.requiresOptionBar(dragElement, dragParent)) {
            addOptionBar(dragElement);
        }
        if (m_controller.isInlineEditable(dragElement, dragParent)) {
            dragElement.initInlineEditor(m_controller);
        }
        return dragElement;
    }

    /**
     * Creates a drag container element. This will not add an option-bar!<p>
     *
     * @param element the DOM element
     * @param dragParent the drag parent
     * @param elementData the element data
     *
     * @return the draggable element
     */
    private CmsGroupContainerElementPanel createGroupcontainer(
        Element element,
        I_CmsDropContainer dragParent,
        CmsContainerElement elementData) {

        CmsGroupContainerElementPanel groupContainer = new CmsGroupContainerElementPanel(
            element,
            dragParent,
            elementData.getClientId(),
            elementData.getSitePath(),
            elementData.getResourceType(),
            elementData.getNoEditReason(),
            elementData.getTitle(),
            elementData.getSubTitle(),
            elementData.hasSettings(dragParent.getContainerId()),
            elementData.hasViewPermission(),
            elementData.hasWritePermission(),
            elementData.isReleasedAndNotExpired(),
            elementData.getElementView(),
            elementData.getBigIconClasses());
        return groupContainer;
    }
}
