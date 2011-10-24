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

import org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsMenuListItem;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Utility class for the container-page editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageUtil {

    /** HTML class used to identify container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_CONTAINER_ELEMENT_END_MARKER}. */
    public static final String CLASS_CONTAINER_ELEMENT_END_MARKER = "cms_ade_element_end";

    /** HTML class used to identify container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_CONTAINER_ELEMENT_START_MARKER}. */
    public static final String CLASS_CONTAINER_ELEMENT_START_MARKER = "cms_ade_element_start";

    /** HTML class used to identify group container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_GROUP_CONTAINER_ELEMENT_MARKER}. */
    public static final String CLASS_GROUP_CONTAINER_ELEMENT_MARKER = "cms_ade_groupcontainer";

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

        // the drag element widgets are created from the existing DOM elements,
        Element child = (Element)container.getElement().getFirstChildElement();
        while (child != null) {
            boolean isContainerElement = CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENT_START_MARKER, child);
            boolean isGroupcontainerElement = CmsDomUtil.hasClass(CLASS_GROUP_CONTAINER_ELEMENT_MARKER, child);
            if (isContainerElement || isGroupcontainerElement) {
                String clientId = child.getAttribute("clientId");
                String sitePath = child.getAttribute("alt");
                String noEditReason = child.getAttribute("rel");
                String newType = child.getAttribute("newType");
                boolean hasProps = Boolean.parseBoolean(child.getAttribute("hasprops"));
                boolean hasViewPermission = Boolean.parseBoolean(child.getAttribute("hasviewpermission"));
                boolean releasedAndNotExpired = Boolean.parseBoolean(child.getAttribute("releasedandnotexpired"));
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
                    if (CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENT_START_MARKER, elementRoot)) {
                        // broken element, already at next start marker
                        child.removeFromParent();
                        child = elementRoot;
                        continue;
                    }
                    if (CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENT_END_MARKER, elementRoot)) {
                        // broken element, no content element root
                        child.removeFromParent();
                        child = (Element)elementRoot.getNextSiblingElement();
                        elementRoot.removeFromParent();
                        continue;
                    } else {
                        // looking for the next marker that wraps the current element
                        Element endMarker = (Element)elementRoot.getNextSibling();
                        // only if the end marker node is not null and has neither the end-marker class or start-marker class
                        // remove the current node and check the next sibling 
                        while (!((endMarker == null) || ((endMarker.getNodeType() == Node.ELEMENT_NODE) && (CmsDomUtil.hasClass(
                            CLASS_CONTAINER_ELEMENT_END_MARKER,
                            endMarker) || CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENT_START_MARKER, endMarker))))) {
                            Element temp = endMarker;
                            endMarker = (Element)endMarker.getNextSibling();
                            temp.removeFromParent();
                        }
                        if (endMarker == null) {
                            // broken element, end marker missing
                            elementRoot.removeFromParent();
                            child.removeFromParent();
                            child = null;
                            continue;
                        }
                        if (CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENT_START_MARKER, endMarker)) {
                            // broken element, end marker missing
                            elementRoot.removeFromParent();
                            child.removeFromParent();
                            child = endMarker;
                        }
                        CmsDomUtil.removeScriptTags(elementRoot);
                        CmsContainerPageElementPanel containerElement = createElement(
                            elementRoot,
                            container,
                            clientId,
                            sitePath,
                            noEditReason,
                            hasProps,
                            hasViewPermission,
                            releasedAndNotExpired);
                        if ((newType != null) && (newType.length() > 0)) {
                            containerElement.setNewType(newType);
                        }
                        container.adoptElement(containerElement);
                        child.removeFromParent();
                        child = (Element)endMarker.getNextSiblingElement(); //   (Element)container.getElement().getFirstChildElement();
                        endMarker.removeFromParent();

                    }
                } else if (isGroupcontainerElement && (container instanceof CmsContainerPageContainer)) {
                    CmsDomUtil.removeScriptTags(child);
                    CmsGroupContainerElementPanel groupContainer = createGroupcontainer(
                        child,
                        container,
                        clientId,
                        sitePath,
                        noEditReason,
                        hasProps,
                        hasViewPermission,
                        releasedAndNotExpired);
                    groupContainer.setContainerId(container.getContainerId());
                    container.adoptElement(groupContainer);
                    consumeContainerElements(groupContainer);
                    if (groupContainer.getWidgetCount() == 0) {
                        groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
                    }
                    // important: adding the option-bar only after the group-containers have been consumed 
                    addOptionBar(groupContainer);
                    child = (Element)child.getNextSiblingElement();
                }
            } else {
                Element sibling = (Element)child.getNextSiblingElement();
                DOM.removeChild((Element)container.getElement(), child);
                child = sibling;
                continue;
            }
        }
    }

    /**
     * The method will create {@link CmsContainerPageContainer} object for all given containers
     * by converting the associated DOM elements. The contained elements will be transformed into {@link CmsContainerPageElementPanel}.<p>
     * 
     * @param containers the container data
     * 
     * @return the drag target containers
     */
    public Map<String, CmsContainerPageContainer> consumeContainers(Map<String, CmsContainerJso> containers) {

        Map<String, CmsContainerPageContainer> result = new HashMap<String, CmsContainerPageContainer>();
        Iterator<CmsContainerJso> it = containers.values().iterator();
        while (it.hasNext()) {
            CmsContainerJso container = it.next();
            CmsContainerPageContainer dragContainer = new CmsContainerPageContainer(container);
            consumeContainerElements(dragContainer);
            result.put(container.getName(), dragContainer);
        }

        return result;
    }

    /**
     * Creates an drag container element.<p>
     * 
     * @param containerElement the container element data 
     * @param container the container parent
     * 
     * @return the draggable element
     * 
     * @throws Exception if something goes wrong
     */
    public CmsContainerPageElementPanel createElement(CmsContainerElementData containerElement, I_CmsDropContainer container)
    throws Exception {

        if (containerElement.isGroupContainer()) {
            List<CmsContainerElementData> subElements = new ArrayList<CmsContainerElementData>();
            for (String subId : containerElement.getSubItems()) {
                CmsContainerElementData element = m_controller.getCachedElement(subId);
                if (element != null) {
                    subElements.add(element);
                }
            }
            return createGroupcontainerElement(containerElement, subElements, container);
        }
        boolean hasProps = !containerElement.getSettingConfig().isEmpty();
        com.google.gwt.user.client.Element element = CmsDomUtil.createElement(containerElement.getContents().get(
            container.getContainerId()));
        // ensure any embedded flash players are set opaque so UI elements may be placed above them
        CmsDomUtil.fixFlashZindex(element);
        return createElement(
            element,
            container,
            containerElement.getClientId(),
            containerElement.getSitePath(),
            containerElement.getNoEditReason(),
            hasProps,
            containerElement.hasViewPermission(),
            containerElement.isReleasedAndNotExpired());
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
        I_CmsDropContainer container) throws Exception {

        com.google.gwt.user.client.Element element = DOM.createDiv();
        element.addClassName(CmsContainerpageUtil.CLASS_GROUP_CONTAINER_ELEMENT_MARKER);
        boolean hasProps = !containerElement.getSettingConfig().isEmpty();

        CmsGroupContainerElementPanel groupContainer = createGroupcontainer(
            element,
            container,
            containerElement.getClientId(),
            containerElement.getSitePath(),
            containerElement.getNoEditReason(),
            hasProps,
            containerElement.hasViewPermission(),
            containerElement.isReleasedAndNotExpired());
        groupContainer.setContainerId(container.getContainerId());
        //adding sub-elements
        Iterator<CmsContainerElementData> it = subElements.iterator();
        while (it.hasNext()) {
            CmsContainerElementData subElement = it.next();
            if (subElement.getContents().containsKey(container.getContainerId())) {
                CmsContainerPageElementPanel subDragElement = createElement(subElement, groupContainer);
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
    public CmsMenuListItem createListItem(CmsContainerElementData containerElement) {

        CmsMenuListItem listItem = new CmsMenuListItem(containerElement);
        listItem.initMoveHandle(m_controller.getDndHandler());
        return listItem;
    }

    /**
     * Creates an drag container element.<p>
     * 
     * @param element the DOM element
     * @param dragParent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason
     * @param hasProps if true, the container element has properties which can be edited 
     * @param hasViewPermission indicates if the current user has view permissions on the element resource
     * 
     * @return the draggable element
     */
    private CmsContainerPageElementPanel createElement(
        com.google.gwt.user.client.Element element,
        I_CmsDropContainer dragParent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps,
        boolean hasViewPermission,
        boolean releasedAndNotExpired) {

        CmsContainerPageElementPanel dragElement = new CmsContainerPageElementPanel(
            element,
            dragParent,
            clientId,
            sitePath,
            noEditReason,
            hasProps,
            hasViewPermission,
            releasedAndNotExpired);
        //        enableDragHandler(dragElement);
        addOptionBar(dragElement);
        return dragElement;
    }

    /**
     * Creates an drag container element. This will not add an option-bar!<p>
     * 
     * @param element the DOM element
     * @param dragParent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason
     * @param hasProps true if the group-container has properties to edit 
     * @param hasViewPermission indicates if the current user has view permissions on the element resource
     * 
     * @return the draggable element
     */
    private CmsGroupContainerElementPanel createGroupcontainer(
        com.google.gwt.user.client.Element element,
        I_CmsDropContainer dragParent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps,
        boolean hasViewPermission,
        boolean releasedAndNotExpired) {

        CmsGroupContainerElementPanel groupContainer = new CmsGroupContainerElementPanel(
            element,
            dragParent,
            clientId,
            sitePath,
            noEditReason,
            hasProps,
            hasViewPermission,
            releasedAndNotExpired);
        return groupContainer;
    }

}
