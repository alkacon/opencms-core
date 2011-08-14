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

import org.opencms.ade.galleries.client.preview.ui.CmsBinaryPreviewDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsBinaryPreviewProvider;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The binary resource preview.<p>
 * 
 * @since 8.0.0
 */
public final class CmsBinaryResourcePreview implements I_CmsResourcePreview, I_CmsHasInit {

    /** The preview instance. */
    private static CmsBinaryResourcePreview m_instance;

    /** The drag-and-drop handler to use for resource list items in the preview. */
    protected CmsDNDHandler m_dndHandler;

    /** The preview controller. */
    private CmsBinaryPreviewController m_controller;

    /** The gallery dialog in which this preview is displayed. */
    private CmsGalleryDialog m_galleryDialog;

    /**
     * Constructor.<p>
     */
    private CmsBinaryResourcePreview() {

        // hiding constructor
    }

    /**
     * Returns the resource preview instance.<p>
     * 
     * @return the resource preview instance
     */
    public static CmsBinaryResourcePreview getInstance() {

        if (m_instance == null) {
            m_instance = new CmsBinaryResourcePreview();
        }
        return m_instance;
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        CmsBinaryResourcePreview instance = getInstance();
        CmsPreviewUtil.exportFunctions(instance.getPreviewName(), instance);
    }

    /** 
     * Sets the drag-and-drop handler to use.<p>
     * 
     * 
     * @param dndHandler the drag-and-drop handler 
     */
    public static void setDNDHandler(CmsDNDHandler dndHandler) {

        getInstance().m_dndHandler = dndHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#clear()
     */
    public void clear() {

        m_controller = null;
    }

    /**
     * Gets the gallery dialog in which this preview is displayed.<p>
     * 
     * @return the gallery dialog  
     */
    public CmsGalleryDialog getGalleryDialog() {

        return m_galleryDialog;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsBinaryPreviewProvider.PREVIEW_NAME;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void openPreview(String galleryMode, String resourcePath, String locale, String parentElementId) {

        FlowPanel parentPanel = CmsGalleryDialog.getPreviewParent(parentElementId);

        // inserting the preview into the DOM
        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);

        CmsBinaryPreviewDialog previewDialog = new CmsBinaryPreviewDialog(
            mode,
            m_dndHandler,
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth());

        // initialize the controller and controller handler
        m_controller = new CmsBinaryPreviewController(
            new CmsBinaryPreviewHandler(previewDialog, this, parentElementId),
            locale);
        exportRemovePreview(parentElementId);
        CmsPreviewUtil.exportFunctions(getPreviewName(), this);
        parentPanel.add(previewDialog);
        parentPanel.removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
        //load preview data
        m_controller.loadResourceInfo(resourcePath);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#selectResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void selectResource(String galleryMode, String resourcePath, String locale, String title) {

        GalleryMode mode = I_CmsGalleryProviderConstants.GalleryMode.valueOf(galleryMode);
        CmsBinaryPreviewController controller = new CmsBinaryPreviewController(locale);
        controller.select(mode, resourcePath, title);
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
     * Sets the gallery dialog in which the preview is being displayed.<p>
     *  
     * @param galleryDialog the gallery dialog  
     */
    public void setGalleryDialog(CmsGalleryDialog galleryDialog) {

        m_galleryDialog = galleryDialog;
    }

    /**
     * Exports the remove preview function.<p>
     * 
     * @param parentId the previews parent element id
     */
    private native void exportRemovePreview(String parentId) /*-{
        $wnd["removePreview" + parentId] = function() {
            @org.opencms.ade.galleries.client.preview.CmsBinaryResourcePreview::m_instance.@org.opencms.ade.galleries.client.preview.CmsBinaryResourcePreview::removePreview()();
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