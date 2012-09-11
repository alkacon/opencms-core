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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsGalleryControllerHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.ui.CmsPopup;

import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The gallery dialog popup.<p>
 */
public class CmsGalleryPopup extends CmsPopup {

    /** The main panel. */
    private SimplePanel m_container;

    /** The gallery controller. */
    private CmsGalleryController m_controller;

    /**
     * 
     * @param handler the widget handler, used to set the widgets value
     * @param referencePath the reference path, for example the resource being edited
     * @param currentElement the currently selected resource
     * @param resourceTypes the resource types (comma separated list)
     * @param tabIds the tabs to use
     */
    public CmsGalleryPopup(
        I_CmsGalleryWidgetHandler handler,
        String referencePath,
        String currentElement,
        String resourceTypes,
        GalleryTabId... tabIds) {

        this();
        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(null, this);
        m_controller = new CmsGalleryController(
            new CmsGalleryControllerHandler(galleryDialog),
            GalleryMode.widget,
            referencePath,
            currentElement,
            resourceTypes,
            tabIds);
        galleryDialog.setWidgetHandler(handler);
        m_container.setWidget(galleryDialog);
    }

    /**
     * Constructor.<p>
     * 
     * @param handler the widget handler, used to set the widgets value
     * @param referencePath the reference path, for example the resource being edited
     * @param galleryPath the startup gallery
     * @param currentElement the currently selected resource
     * @param resourceTypes the resource types (comma separated list)
     * @param galleryTypes the gallery types (comma separated list)
     * @param useFormats the use image formats flag
     * @param imageFormats the image formats (comma separated list)
     * @param imageFormatNames the image format names (comma separated list)
     */
    public CmsGalleryPopup(
        I_CmsGalleryWidgetHandler handler,
        String referencePath,
        String galleryPath,
        String currentElement,
        String resourceTypes,
        String galleryTypes,
        boolean useFormats,
        String imageFormats,
        String imageFormatNames) {

        this();
        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(null, this);
        m_controller = new CmsGalleryController(
            new CmsGalleryControllerHandler(galleryDialog),
            GalleryMode.widget,
            referencePath,
            galleryPath,
            currentElement,
            resourceTypes,
            galleryTypes,
            useFormats,
            imageFormats,
            imageFormatNames);
        galleryDialog.setWidgetHandler(handler);
        m_container.setWidget(galleryDialog);
    }

    /**
     * Constructor.<p>
     */
    private CmsGalleryPopup() {

        super(650);
        setGlassEnabled(true);
        catchNotifications();
        removePadding();
        m_container = new SimplePanel();
        setMainContent(m_container);
        addDialogClose(null);
    }

    /**
     * Searches for a specific element and opens it's preview if found.<p>
     * 
     * @param path the element path
     */
    public void searchElement(String path) {

        center();
        m_controller.searchElement(path);
    }
}
