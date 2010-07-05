/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/A_CmsPreviewHandler.java,v $
 * Date   : $Date: 2010/07/05 14:48:07 $
 * Version: $Revision: 1.2 $
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

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import java.util.Map;

import com.google.gwt.user.client.Command;

/**
 * Preview dialog handler.<p>
 * 
 * Delegates the actions of the preview controller to the preview dialog.<p>
 * 
 * @param <T> the resource info bean type
 * 
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public abstract class A_CmsPreviewHandler<T extends CmsResourceInfoBean> implements I_CmsPreviewHandler<T> {

    /** The reference to the preview dialog. */
    protected A_CmsPreviewDialog<T> m_previewDialog;

    /** The preview controller. */
    protected A_CmsPreviewController<T> m_controller;

    /**
     * Constructor.<p>
     * 
     * @param previewDialog the reference to the preview dialog 
     */
    public A_CmsPreviewHandler(A_CmsPreviewDialog<T> previewDialog) {

        m_previewDialog = previewDialog;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#closePreview()
     */
    public void closePreview() {

        if (m_previewDialog.hasChanges()) {
            //TODO: localization
            m_previewDialog.confirmSaveChanges("Do you want to save before leaving the preview?", new Command() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    if (m_previewDialog.getGalleryMode().equals(GalleryMode.editor)) {
                        CmsPreviewUtil.enableEditorOk(false);
                    }
                    m_previewDialog.removePreview();
                }
            }, null);
            return;
        }
        if (m_previewDialog.getGalleryMode() == GalleryMode.editor) {
            CmsPreviewUtil.enableEditorOk(false);
        }
        m_previewDialog.removePreview();
    }

    /**
     * Returns the reference to the preview dialog.<p>
     *
     * @return the preview dialog
     */
    public A_CmsPreviewDialog<T> getPreviewDialog() {

        return m_previewDialog;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPropertiesHandler#saveProperties(java.util.Map)
     */
    public void saveProperties(Map<String, String> properties) {

        m_controller.saveProperties(properties);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPropertiesHandler#selectResource()
     */
    public void selectResource() {

        m_controller.setResource(m_previewDialog.getGalleryMode());
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#setDataInEditor()
     */
    public boolean setDataInEditor() {

        if (m_previewDialog.getGalleryMode() == GalleryMode.editor) {
            if (m_previewDialog.hasChanges()) {
                //TODO: localization
                m_previewDialog.confirmSaveChanges("Do you want to save before leaving the dialog?", new Command() {

                    /**
                     * @see com.google.gwt.user.client.Command#execute()
                     */
                    public void execute() {

                        CmsPreviewUtil.closeDialog();
                    }
                }, null);
                return false;
            } else {
                m_controller.setResource(m_previewDialog.getGalleryMode());
                return true;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#showData(org.opencms.ade.galleries.shared.CmsResourceInfoBean)
     */
    public void showData(T resourceInfo) {

        // once the resource info is displayed, enable the OK button for editor mode
        if (m_previewDialog.getGalleryMode().equals(GalleryMode.editor)) {
            CmsPreviewUtil.enableEditorOk(true);
        }
        m_previewDialog.fillContent(resourceInfo);
    }
}
