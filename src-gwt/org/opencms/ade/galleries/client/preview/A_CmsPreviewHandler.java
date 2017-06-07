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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
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
 * @since 8.0.0
 */
public abstract class A_CmsPreviewHandler<T extends CmsResourceInfoBean> implements I_CmsPreviewHandler<T> {

    /** The resource info. */
    protected T m_resourceInfo;

    /** The resource preview instance. */
    protected I_CmsResourcePreview<T> m_resourcePreview;

    /**
     * Constructor.<p>
     *
     * @param resourcePreview the resource preview instance
     */
    public A_CmsPreviewHandler(I_CmsResourcePreview<T> resourcePreview) {

        m_resourcePreview = resourcePreview;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#closePreview()
     */
    public void closePreview() {

        if (m_resourcePreview.getPreviewDialog().getGalleryMode() == GalleryMode.editor) {
            CmsPreviewUtil.enableEditorOk(false);
        }
        m_resourcePreview.getGalleryDialog().setPreviewVisible(false);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#getGalleryDialog()
     */
    public CmsGalleryDialog getGalleryDialog() {

        return m_resourcePreview.getGalleryDialog();
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPropertiesHandler#saveProperties(java.util.Map, com.google.gwt.user.client.Command)
     */
    public void saveProperties(Map<String, String> properties, Command afterSaveCallback) {

        m_resourcePreview.saveProperties(properties, afterSaveCallback);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPropertiesHandler#selectResource()
     */
    public void selectResource() {

        m_resourcePreview.setResource();
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler#setDataInEditor()
     */
    public boolean setDataInEditor() {

        if (m_resourcePreview.getGalleryMode() == GalleryMode.editor) {
            if (m_resourcePreview.getPreviewDialog().hasChanges()) {
                m_resourcePreview.getPreviewDialog().confirmSaveChanges(
                    Messages.get().key(Messages.GUI_PREVIEW_CONFIRM_LEAVE_SAVE_0),
                    new Command() {

                        /**
                         * @see com.google.gwt.user.client.Command#execute()
                         */
                        public void execute() {

                            m_resourcePreview.getPreviewDialog().saveChanges(new Command() {

                                public void execute() {

                                    CmsPreviewUtil.setDataAndCloseDialog();
                                }
                            });
                        }
                    },
                    null);
                return false;
            } else {
                m_resourcePreview.setResource();
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

        m_resourceInfo = resourceInfo;
        // once the resource info is displayed, enable the OK button for editor mode
        if (m_resourcePreview.getGalleryMode().equals(GalleryMode.editor)) {
            CmsPreviewUtil.enableEditorOk(true);
        }
        m_resourcePreview.getPreviewDialog().fillContent(resourceInfo);
    }
}
