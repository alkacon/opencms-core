/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/client/Attic/CmsResourcePreview.java,v $
 * Date   : $Date: 2010/06/10 08:45:04 $
 * Version: $Revision: 1.5 $
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

package org.opencms.ade.galleries.preview.binary.client;

import org.opencms.ade.galleries.client.preview.A_CmsPreviewController;
import org.opencms.ade.galleries.client.preview.A_CmsResourcePreview;
import org.opencms.ade.galleries.preview.binary.shared.I_CmsBinaryPreviewProvider;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * The binary resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsResourcePreview extends A_CmsResourcePreview {

    private CmsBinaryPreviewController m_controller;

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#setDataInEditor()
     */
    public boolean setDataInEditor() {

        if (m_controller == null) {
            return true;
        }
        return m_controller.closeGalleryDialog();
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsBinaryPreviewProvider.PREVIEW_NAME;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(java.lang.String, java.lang.String, java.lang.String)
     */
    public void openPreview(String galleryMode, String resourcePath, String parentElementId) {

        RootPanel parentPanel = RootPanel.get(parentElementId);

        // inserting the preview into the DOM
        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);

        CmsBinaryPreviewDialog preview = new CmsBinaryPreviewDialog(
            mode,
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth());

        // initialize the controller and controller handler
        m_controller = new CmsBinaryPreviewController(new CmsBinaryPreviewHandler(preview));
        parentPanel.add(preview);

        //load preview data
        m_controller.loadResourceInfo(resourcePath);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#selectResource(java.lang.String, java.lang.String, java.lang.String)
     */
    public void selectResource(String galleryMode, String resourcePath, String title) {

        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);
        A_CmsPreviewController.select(mode, resourcePath, title);
    }
}