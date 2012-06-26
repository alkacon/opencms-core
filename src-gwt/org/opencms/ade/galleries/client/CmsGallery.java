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

import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.client.A_CmsEntryPoint;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * Gallery Dialog entry class to be open from the vfs tree.<p>
 * 
 * @since 8.0.0
 */
public class CmsGallery extends A_CmsEntryPoint {

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        if (!checkBuildId("org.opencms.ade.galleries")) {
            return;
        }
        RootPanel.getBodyElement().addClassName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().galleryBody());
        // add the gallery dialog to dom
        RootPanel.get(I_CmsGalleryProviderConstants.GALLERY_DIALOG_ID).add(CmsGalleryFactory.createDialog());
    }
}
