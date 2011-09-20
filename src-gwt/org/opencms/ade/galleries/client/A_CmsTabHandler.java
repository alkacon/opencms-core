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

package org.opencms.ade.galleries.client;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
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
     * Clears the search parameters of this tab.<p>
     */
    public abstract void clearParams();

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
     * Selects the result tab.<p>
     */
    public void selectResultTab() {

        m_controller.selectResultTab();
    }
}