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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The abstract class for the tab handler.<p>
 *
 * This class receives event information from the gallery dialog and
 * delegates it to the gallery controller.
 *
 * @since 8.0.0
 */
public abstract class A_CmsTabHandler implements CloseHandler<PopupPanel> {

    /** The gallery controller. */
    protected CmsGalleryController m_controller;

    /**
     * Constructor.<p>
     *
     * @param controller the controller
     */
    public A_CmsTabHandler(CmsGalleryController controller) {

        m_controller = controller;
    }

    /**
     * Adds a change handler for the gallery search bean.<p>
     *
     * @param handler the handler
     *
     * @return the handler registration
     */
    public HandlerRegistration addSearchChangeHandler(ValueChangeHandler<CmsGallerySearchBean> handler) {

        return m_controller.addValueChangeHandler(handler);
    }

    /**
     * Clears the search parameters of this tab.<p>
     */
    public abstract void clearParams();

    /**
     * Returns the gallery folder info to the given path.<p>
     *
     * @param galleryPath the gallery folder path
     *
     * @return the gallery folder info
     */
    public CmsGalleryFolderBean getGalleryInfo(String galleryPath) {

        return m_controller.getGalleryInfo(galleryPath);
    }

    /**
     * Returns the resource type info for the given resource type name.<p>
     *
     * @param typeName the resource type name
     *
     * @return the type info
     */
    public CmsResourceTypeBean getTypeInfo(String typeName) {

        return m_controller.getTypeInfo(typeName);
    }

    /**
     * Execute when the upload dialog is closed.<p>
     *
     * @param event the close event
     */
    public void onClose(CloseEvent<PopupPanel> event) {

        m_controller.setSearchObjectChanged();
    }

    /**
     * Will be triggered when the tab is deselected.<p>
     */
    public void onDeselection() {

        // do nothing
    }

    /**
     * Will be triggered when the tab is selected.<p>
     */
    public abstract void onSelection();

    /**
     * Sorts the list, if present.<p>
     *
     * @param sortParams the sort parameters
     * @param filter the filter phrase
     */
    public abstract void onSort(String sortParams, String filter);

    /**
     * Removes the search parameter with the given key from the search object.<p>
     *
     * @param paramKey the parameter key
     */
    public abstract void removeParam(String paramKey);

    /**
     * Selects the given resource and sets its path into the xml-content field or editor link.<p>
     *
     * @param resourcePath the item resource path
     * @param structureId the structure id
     * @param title the resource title
     * @param resourceType the item resource type
     */
    public void selectResource(String resourcePath, CmsUUID structureId, String title, String resourceType) {

        m_controller.selectResource(resourcePath, structureId, title, resourceType);
    }

    /**
     * Selects the result tab.<p>
     */
    public void selectResultTab() {

        m_controller.selectResultTab();
    }

    /**
     * Delegates the clear input action (click on the clear button) to the controller.<p>
     *
     * @param searchQuery the search query
     */
    public void setSearchQuery(String searchQuery) {

        m_controller.addSearchQuery(searchQuery);
    }

    /**
     * Updates the gallery index and triggers a new search afterwards.<p>
     */
    public void updateIndex() {

        m_controller.updateIndex();
    }

    /**
     * Updates the tab size.<p>
     */
    public void updateSize() {

        m_controller.updateActiveTabSize();
    }

}