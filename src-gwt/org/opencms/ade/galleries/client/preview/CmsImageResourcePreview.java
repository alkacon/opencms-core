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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsImagePreviewProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The image resource preview.<p>
 * 
 * @since 8.0.0
 */
public final class CmsImageResourcePreview implements I_CmsResourcePreview, I_CmsHasInit {

    /** The preview instance. */
    private static CmsImageResourcePreview m_instance;

    /** The preview controller. */
    private CmsImagePreviewController m_controller;

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

        FlowPanel parentPanel = CmsGalleryDialog.getPreviewParent(parentElementId);

        // inserting the preview into the DOM
        GalleryMode mode = GalleryMode.valueOf(galleryMode);

        CmsImagePreviewDialog preview = new CmsImagePreviewDialog(
            mode,
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth());
        // initialize the controller and controller handler
        m_controller = new CmsImagePreviewController(new CmsImagePreviewHandler(preview, this, parentElementId));
        exportRemovePreview(parentElementId);
        CmsPreviewUtil.exportFunctions(getPreviewName(), this);
        parentPanel.add(preview);
        parentPanel.removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
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

        CmsDebugLog.getInstance().printLine("Setting data");
        if (m_controller == null) {
            return true;
        }
        return m_controller.closeGalleryDialog();
    }

    /**
     * Exports the remove preview function.<p>
     * 
     * @param parentId the previews parent element id
     */
    private native void exportRemovePreview(String parentId) /*-{
      $wnd["removePreview" + parentId] = function() {
         @org.opencms.ade.galleries.client.preview.CmsImageResourcePreview::m_instance.@org.opencms.ade.galleries.client.preview.CmsImageResourcePreview::removePreview()();
      };
    }-*/;

    /**
     * Removes the preview.<p>
     */
    private void removePreview() {

        m_controller.removePreview();
        m_controller = null;
    }
}