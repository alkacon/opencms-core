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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsGalleryControllerHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The gallery dialog popup.<p>
 */
public class CmsGalleryPopup extends CmsPopup implements I_CmsGalleryHandler {

    /** The main panel. */
    private SimplePanel m_container;

    /** The gallery controller. */
    private CmsGalleryController m_controller;

    /**
     * Constructor.<p>
     *
     * @param handler the widget handler, used to set the widgets value
     * @param conf the gallery configuration
     */
    public CmsGalleryPopup(I_CmsGalleryWidgetHandler handler, I_CmsGalleryConfiguration conf) {

        this();
        int dialogHeight = CmsToolbarPopup.getAvailableHeight();
        int dialogWidth = CmsToolbarPopup.getAvailableWidth();
        setWidth(dialogWidth);
        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(this);
        galleryDialog.setDialogSize(dialogWidth, dialogHeight);
        m_controller = new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog), conf);
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
     * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#filterDnd(org.opencms.ade.galleries.shared.CmsResultItemBean)
     */
    public boolean filterDnd(CmsResultItemBean resultBean) {

        return true;
    }

    /**
     * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getAdditionalTypeTabControl()
     */
    public Widget getAdditionalTypeTabControl() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getAutoHideParent()
     */
    public I_CmsAutoHider getAutoHideParent() {

        return this;
    }

    /**
     * Returns the popup content.<p>
     *
     * @return the popup content
     */
    public Panel getContainer() {

        return m_container;
    }

    /**
     * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#getDndHandler()
     */
    public CmsDNDHandler getDndHandler() {

        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.I_CmsGalleryHandler#processResultItem(org.opencms.ade.galleries.client.ui.CmsResultListItem)
     */
    public void processResultItem(CmsResultListItem item) {

        // do nothing

    }

    /**
     * Searches for a specific element and opens it's preview if found.<p>
     *
     * @param path the element path
     */
    public void searchElement(String path) {

        //center();
        m_controller.searchElement(path, new Runnable() {

            public void run() {

                center();
            }
        });
    }
}
