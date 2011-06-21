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

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.I_CmsAutoHider;

/**
 * Factory class to create gallery dialog with or without parameter.<p>
 * 
 * @since 8.0.
 */
public final class CmsGalleryFactory {

    /**
     * Prevent instantiation.<p>
     */
    private CmsGalleryFactory() {

        // empty
    }

    /**
     * Returns a gallery dialog object.<p>
     * 
     * @return gallery dialog
     */
    public static CmsGalleryDialog createDialog() {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(CmsTabbedPanelStyle.buttonTabs);
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog));
        return galleryDialog;
    }

    /**
     * Returns a gallery dialog object with drag and drop handler.<p>
     * 
     * @param dndHandler the reference to the drag and drop handler
     * @param autoHideParent the auto-hide parent to this dialog if present
     * 
     * @return gallery dialog
     */
    public static CmsGalleryDialog createDialog(CmsDNDHandler dndHandler, I_CmsAutoHider autoHideParent) {

        CmsGalleryDialog galleryDialog = new CmsGalleryDialog(dndHandler, autoHideParent);
        new CmsGalleryController(new CmsGalleryControllerHandler(galleryDialog));
        return galleryDialog;
    }
}