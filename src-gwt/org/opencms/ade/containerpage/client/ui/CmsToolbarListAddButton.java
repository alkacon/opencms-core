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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.shared.CmsGwtConstants;

import com.google.gwt.event.dom.client.ClickEvent;

import elemental2.dom.Element;
import elemental2.dom.NodeList;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * Button to open the dialog for adding list elements
 */
public class CmsToolbarListAddButton extends A_CmsToolbarOptionButton {

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarListAddButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.ADD, handler);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#isOptionAvailable(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public boolean isOptionAvailable(CmsContainerPageElementPanel element) {

        return !CmsContainerpageController.get().isEditingDisabled() && (getListAddInfo(element) != null);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElementPanel containerElement) {

        String listAddInfo = getListAddInfo(containerElement);
        m_handler.openListAddDialog(containerElement.getStructureId(), listAddInfo);
    }

    /**
     * Validates that a list creation metadata element directly belongs to a given container element and not to some other element in a nested container.
     *
     * @param containerElement the container element widget
     * @param dataElement the list data element
     *
     * @return true if the data element really belongs directly to the container element
     */
    private boolean checkIfListDataBelongsToContainerElement(
        CmsContainerPageElementPanel containerElement,
        Element dataElement) {

        // note: this does not catch model groups, but we handle that case elsewhere

        Element currentElement = dataElement;
        String elementObjectId = null;
        while (currentElement != null) {
            if (currentElement.classList.contains(CmsContainerElement.CLASS_CONTAINER)) {
                return false;
            }
            JsPropertyMap props = Js.cast(currentElement);
            elementObjectId = (String)(props.get(CmsContainerPageElementPanel.PROP_ELEMENT_OBJECT_ID));
            if (elementObjectId != null) {
                break;
            }
            currentElement = currentElement.parentElement;
        }
        boolean result = containerElement.getObjectId().equals(elementObjectId);
        return result;

    }

    /**
     * Finds an element with the data-oc-listadd attribute in the container element and return the attribute's content,
     * or null if no element with that attribute exists in the content element.
     *
     * @param containerElement the container element
     * @return the information used by the dialog for adding new list elements
     */
    private String getListAddInfo(CmsContainerPageElementPanel containerElement) {

        Element element = Js.cast(containerElement.getElement());
        if (containerElement.isModelGroup()) {
            // in principle, the list element adding mechanism should also work when used inside a model group,
            // but this could cause ambiguities/conflicts (multiple lists in a model group or similar cases).
            return null;
        }
        // first try to find a sub-element with the necessary metadata in the DOM
        NodeList<Element> dataElements = element.querySelectorAll("[" + CmsGwtConstants.ATTR_DATA_LISTADD + "]");
        for (int i = 0; i < dataElements.getLength(); i++) {
            Element dataElement = dataElements.item(i);
            // just having an element with the data is not enough, we have to check if it's really part of *this*
            // container element, not just from an element of a nested container
            if (checkIfListDataBelongsToContainerElement(containerElement, dataElement)) {
                // we do not currently support more than one list-add configuration - just return the first one
                return dataElement.getAttribute(CmsGwtConstants.ATTR_DATA_LISTADD);
            }
        }
        return null;
    }
}