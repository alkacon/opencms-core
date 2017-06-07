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

import org.opencms.ade.galleries.client.preview.ui.CmsBinaryPreviewDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsBinaryPreviewProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The binary resource preview.<p>
 *
 * @since 8.0.0
 */
public final class CmsBinaryResourcePreview extends A_CmsResourcePreview<CmsResourceInfoBean> {

    /** The preview handler. */
    private CmsBinaryPreviewHandler m_handler;

    /** The preview dialog widget. */
    private CmsBinaryPreviewDialog m_previewDialog;

    /**
     * Constructor.<p>
     *
     * @param galleryDialog the gallery dialog instance
     */
    public CmsBinaryResourcePreview(CmsGalleryDialog galleryDialog) {

        super(galleryDialog);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#getHandler()
     */
    @Override
    public CmsBinaryPreviewHandler getHandler() {

        return m_handler;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewDialog()
     */
    public CmsBinaryPreviewDialog getPreviewDialog() {

        return m_previewDialog;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsBinaryPreviewProvider.PREVIEW_NAME;
    }

    /**
     * Loads the resource info and displays the retrieved data.<p>
     *
     * @param resourcePath the resource path
     */
    public void loadResourceInfo(final String resourcePath) {

        CmsRpcAction<CmsResourceInfoBean> action = new CmsRpcAction<CmsResourceInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().getResourceInfo(resourcePath, getLocale(), this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsResourceInfoBean result) {

                showData(result);
            }
        };
        action.execute();
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(java.lang.String, boolean)
     */
    public void openPreview(String resourcePath, boolean disableSelection) {

        if (m_previewDialog != null) {
            m_previewDialog.removeFromParent();
        }
        FlowPanel parentPanel = getGalleryDialog().getParentPanel();
        m_previewDialog = new CmsBinaryPreviewDialog(
            getGalleryDialog().getController().getDialogMode(),
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth(),
            disableSelection);

        m_handler = new CmsBinaryPreviewHandler(this);
        m_previewDialog.init(m_handler);
        CmsPreviewUtil.exportFunctions(getPreviewName(), this);
        parentPanel.add(m_previewDialog);
        m_handler.getGalleryDialog().setPreviewVisible(true);
        //load preview data
        loadResourceInfo(resourcePath);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#removePreview()
     */
    @Override
    public void removePreview() {

        super.removePreview();
        m_handler = null;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#saveProperties(java.util.Map, com.google.gwt.user.client.Command)
     */
    public void saveProperties(final Map<String, String> properties, final Command afterSaveCallback) {

        CmsRpcAction<CmsResourceInfoBean> action = new CmsRpcAction<CmsResourceInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().updateResourceProperties(getResourcePath(), getLocale(), properties, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsResourceInfoBean result) {

                showData(result);
                if (afterSaveCallback != null) {
                    afterSaveCallback.execute();
                }

            }
        };
        action.execute();

    }
}