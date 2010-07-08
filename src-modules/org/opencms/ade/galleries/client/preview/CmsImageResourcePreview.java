/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsImageResourcePreview.java,v $
 * Date   : $Date: 2010/07/08 06:49:42 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsImagePreviewProvider;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.I_CmsHasInit;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * The image resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsImageResourcePreview implements I_CmsResourcePreview, I_CmsHasInit {

    /** The preview controller. */
    private CmsImagePreviewController m_controller;

    private static CmsImageResourcePreview m_instance;

    /**
     * Constructor.<p>
     */
    private CmsImageResourcePreview() {

        // hiding constructor
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        CmsPreviewUtil.exportFunctions(getInstance().getPreviewName(), getInstance());
    }

    /**
     * Returns the resource preview instance.<p>
     * 
     * @return the resource preview instance
     */
    private static CmsImageResourcePreview getInstance() {

        if (m_instance == null) {
            m_instance = new CmsImageResourcePreview();
        }
        return m_instance;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#clear()
     */
    public void clear() {

        m_controller = null;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsImagePreviewProvider.PREVIEW_NAME;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(String, String, String)
     */
    public void openPreview(String galleryMode, String resourcePath, String parentElementId) {

        RootPanel parentPanel = RootPanel.get(parentElementId);

        // inserting the preview into the DOM
        GalleryMode mode = GalleryMode.valueOf(galleryMode);

        CmsImagePreviewDialog preview = new CmsImagePreviewDialog(
            mode,
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth());

        // initialize the controller and controller handler
        m_controller = new CmsImagePreviewController(new CmsImagePreviewHandler(preview, this));

        parentPanel.add(preview);

        //load preview data
        m_controller.loadResourceInfo(resourcePath);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#selectResource(java.lang.String, java.lang.String, java.lang.String)
     */
    public void selectResource(String galleryMode, String resourcePath, String title) {

        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);
        CmsImagePreviewController.select(mode, resourcePath, title);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#setDataInEditor()
     */
    public boolean setDataInEditor() {

        if (m_controller == null) {
            return true;
        }
        return m_controller.closeGalleryDialog();
    }
}