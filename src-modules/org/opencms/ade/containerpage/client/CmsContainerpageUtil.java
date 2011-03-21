/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageUtil.java,v $
 * Date   : $Date: 2011/03/21 12:49:32 $
 * Version: $Revision: 1.18 $
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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElement;
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElement;
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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Utility class for the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageUtil {

    /** HTML class used to identify container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_CONTAINER_ELEMENTS}. */
    public static final String CLASS_CONTAINER_ELEMENTS = "cms_ade_element";

    /** HTML class used to identify group container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_GROUP_CONTAINER_ELEMENTS}. */
    public static final String CLASS_GROUP_CONTAINER_ELEMENTS = "cms_ade_groupcontainer";

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
     * Transforms all contained elements into {@link CmsContainerPageElement}.<p>
     * 
     * @param container the container
     */
    public void consumeContainerElements(I_CmsDropContainer container) {

        List<CmsContainerPageElement> elements = new ArrayList<CmsContainerPageElement>();
        // the drag element widgets are created from the existing DOM elements,
        // to establish the internal widget hierarchy the elements need to be removed from the DOM and added as widgets to the root panel
        Element child = (Element)container.getElement().getFirstChildElement();
        while (child != null) {
            boolean isContainerElement = CmsDomUtil.hasClass(CLASS_CONTAINER_ELEMENTS, child);
            boolean isGroupcontainerElement = CmsDomUtil.hasClass(CLASS_GROUP_CONTAINER_ELEMENTS, child);
            if (isContainerElement || isGroupcontainerElement) {
                String clientId = child.getAttribute("title");
                String sitePath = child.getAttribute("alt");
                String noEditReason = child.getAttribute("rel");
                String newType = child.getAttribute("newType");
                boolean hasProps = Boolean.parseBoolean(child.getAttribute("hasprops"));

                if (isContainerElement) {
                    Element elementRoot = (Element)child.getFirstChildElement();
                    DOM.removeChild(child, elementRoot);
                    CmsContainerPageElement containerElement = createElement(
                        elementRoot,
                        container,
                        clientId,
                        sitePath,
                        noEditReason,
                        hasProps);
                    if ((newType != null) && (newType.length() > 0)) {
                        containerElement.setNewType(newType);
                    }
                    elements.add(containerElement);
                    DOM.removeChild((Element)container.getElement(), child);
                } else if (isGroupcontainerElement && (container instanceof CmsContainerPageContainer)) {
                    CmsGroupContainerElement groupContainer = createGroupcontainer(
                        child,
                        container,
                        clientId,
                        sitePath,
                        noEditReason,
                        hasProps);
                    groupContainer.setContainerId(container.getContainerId());
                    elements.add(groupContainer);
                    DOM.removeChild((Element)container.getElement(), child);
                    consumeContainerElements(groupContainer);

                    // important: adding the option-bar only after the group-containers have been consumed 
                    addOptionBar(groupContainer);
                }
            } else {
                DOM.removeChild((Element)container.getElement(), child);
            }

            child = (Element)container.getElement().getFirstChildElement();
        }

        // re-append the element widgets by adding them to the root panel
        Iterator<CmsContainerPageElement> it = elements.iterator();
        while (it.hasNext()) {
            container.add(it.next());
        }
    }

    /**
     * The method will create {@link CmsContainerPageContainer} object for all given containers
     * by converting the associated DOM elements. The contained elements will be transformed into {@link CmsContainerPageElement}.<p>
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
    public CmsContainerPageElement createElement(CmsContainerElementData containerElement, I_CmsDropContainer container)
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
        boolean hasProps = !containerElement.getPropertyConfig().isEmpty();
        com.google.gwt.user.client.Element element = CmsDomUtil.createElement(containerElement.getContents().get(
            container.getContainerId()));

        return createElement(
            element,
            container,
            containerElement.getClientId(),
            containerElement.getSitePath(),
            containerElement.getNoEditReason(),
            hasProps);
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
    public CmsContainerPageElement createGroupcontainerElement(
        CmsContainerElementData containerElement,
        List<CmsContainerElementData> subElements,
        I_CmsDropContainer container) throws Exception {

        com.google.gwt.user.client.Element element = DOM.createDiv();
        element.addClassName(CmsContainerpageUtil.CLASS_GROUP_CONTAINER_ELEMENTS);
        boolean hasProps = !containerElement.getPropertyConfig().isEmpty();

        CmsGroupContainerElement groupContainer = createGroupcontainer(
            element,
            container,
            containerElement.getClientId(),
            containerElement.getSitePath(),
            containerElement.getNoEditReason(),
            hasProps);
        groupContainer.setContainerId(container.getContainerId());
        //adding sub-elements
        Iterator<CmsContainerElementData> it = subElements.iterator();
        while (it.hasNext()) {
            CmsContainerElementData subElement = it.next();
            if (subElement.getContents().containsKey(container.getContainerId())) {
                CmsContainerPageElement subDragElement = createElement(subElement, groupContainer);
                groupContainer.add(subDragElement);
            }
        }
        if (subElements.size() == 0) {
            groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().emptyGroupContainer());
        }
        addOptionBar(groupContainer);
        return groupContainer;
    }

    /**
     * Adds an option bar to the given drag element.<p>
     * 
     * @param element the element
     */
    private void addOptionBar(CmsContainerPageElement element) {

        CmsElementOptionBar optionBar = CmsElementOptionBar.createOptionBarForElement(
            element,
            m_controller.getDndHandler(),
            m_optionButtons);
        element.setElementOptionBar(optionBar);
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
     * 
     * @return the draggable element
     */
    private CmsContainerPageElement createElement(
        com.google.gwt.user.client.Element element,
        I_CmsDropContainer dragParent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        CmsContainerPageElement dragElement = new CmsContainerPageElement(
            element,
            dragParent,
            clientId,
            sitePath,
            noEditReason,
            hasProps);
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
     * 
     * @return the draggable element
     */
    private CmsGroupContainerElement createGroupcontainer(
        com.google.gwt.user.client.Element element,
        I_CmsDropContainer dragParent,
        String clientId,
        String sitePath,
        String noEditReason,
        boolean hasProps) {

        CmsGroupContainerElement groupContainer = new CmsGroupContainerElement(
            element,
            dragParent,
            clientId,
            sitePath,
            noEditReason,
            hasProps);
        return groupContainer;
    }

}
