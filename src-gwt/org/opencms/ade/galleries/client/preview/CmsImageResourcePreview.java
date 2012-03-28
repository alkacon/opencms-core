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
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsImagePreviewProvider;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The image resource preview.<p>
 * 
 * @since 8.0.0
 */
public final class CmsImageResourcePreview extends A_CmsResourcePreview<CmsImageInfoBean> {

    /** The image preview handler. */
    private CmsImagePreviewHandler m_handler;

    /** The preview dialog widget. */
    private CmsImagePreviewDialog m_previewDialog;

    /**
     * Constructor.<p>
     * 
     * @param galleryDialog the gallery dialog
     */
    public CmsImageResourcePreview(CmsGalleryDialog galleryDialog) {

        super(galleryDialog);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#getHandler()
     */
    @Override
    public I_CmsPreviewHandler<CmsImageInfoBean> getHandler() {

        if (m_handler == null) {
            throw new UnsupportedOperationException("Preview handler not initialized");
        }
        return m_handler;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewDialog()
     */
    public CmsImagePreviewDialog getPreviewDialog() {

        return m_previewDialog;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#getPreviewName()
     */
    public String getPreviewName() {

        return I_CmsImagePreviewProvider.PREVIEW_NAME;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#loadResourceInfo(java.lang.String)
     */
    public void loadResourceInfo(final String resourcePath) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                A_CmsResourcePreview.getService().getImageInfo(resourcePath, getLocale(), this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsImageInfoBean result) {

                result.setSelectedPath(resourcePath);
                showData(result);
            }
        };
        action.execute();

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsResourcePreview#openPreview(String)
     */
    public void openPreview(String resourcePath) {

        if (m_previewDialog != null) {
            m_previewDialog.removeFromParent();
        }
        FlowPanel parentPanel = getGalleryDialog().getParentPanel();
        m_previewDialog = new CmsImagePreviewDialog(
            getGalleryDialog().getController().getDialogMode(),
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth());
        // initialize the controller and controller handler
        m_handler = new CmsImagePreviewHandler(this);
        m_previewDialog.init(m_handler);
        CmsPreviewUtil.exportFunctions(getPreviewName(), this);
        parentPanel.add(m_previewDialog);
        parentPanel.removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
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

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().updateImageProperties(getResourcePath(), getLocale(), properties, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsImageInfoBean result) {

                showData(result);
                if (afterSaveCallback != null) {
                    afterSaveCallback.execute();
                }
            }
        };
        action.execute();

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#selectResource(java.lang.String, java.lang.String)
     */
    @Override
    public void selectResource(String resourcePath, String title) {

        CmsCroppingParamBean param;
        switch (getGalleryMode()) {
            case widget:
                param = getInitialCroppingParameter(resourcePath);
                if (CmsPreviewUtil.isAdvancedWidget()) {
                    CmsPreviewUtil.setVfsImage(
                        resourcePath,
                        param.getScaleParam(),
                        param.getFormatName(),
                        param.getRatio() + "");
                } else {
                    CmsPreviewUtil.setResourcePath(resourcePath
                        + ((param.isCropped() || param.isScaled()) ? "?" + param.toString() : ""));
                }
                break;
            case editor:
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("title", title);
                param = getInitialCroppingParameter(resourcePath);
                attributes.put("width", String.valueOf(param.getResultingWidth()));
                attributes.put("height", String.valueOf(param.getResultingHeight()));
                CmsPreviewUtil.setImage(CmsCoreProvider.get().link(resourcePath), attributes);
                CmsPreviewUtil.closeDialog();
                break;
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#setResource()
     */
    @Override
    public void setResource() {

        if (m_handler == null) {
            throw new UnsupportedOperationException("Preview handler not initialized");
        }
        CmsCroppingParamBean croppingParam = m_handler.getCroppingParam();

        switch (getGalleryMode()) {
            case widget:
                if (CmsPreviewUtil.isAdvancedWidget()) {
                    CmsPreviewUtil.setVfsImage(
                        m_infoBean.getResourcePath(),
                        croppingParam.getScaleParam(),
                        croppingParam.getFormatName(),
                        croppingParam.getRatio() + "");
                } else {
                    CmsPreviewUtil.setResourcePath(m_infoBean.getResourcePath()
                        + ((croppingParam.isCropped() || croppingParam.isScaled())
                        ? "?" + croppingParam.toString()
                        : ""));
                }
                break;
            case editor:
                Map<String, String> attributes = m_handler.getImageAttributes();
                CmsPreviewUtil.setImage(
                    CmsCoreProvider.get().link(
                        m_infoBean.getResourcePath()
                            + ((croppingParam.isCropped() || croppingParam.isScaled())
                            ? "?" + croppingParam.toString()
                            : "")),
                    attributes);
                break;
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
    }

    /**
     * Returns the image info bean for the given resource.<p>
     * 
     * @param resourcePath the resource path
     * 
     * @return the image info bean
     */
    private CmsImageInfoBean getImageInfo(final String resourcePath) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                A_CmsResourcePreview.getService().syncGetImageInfo(resourcePath, getLocale(), this);
                start(0, true);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsImageInfoBean result) {

                stop(false);
            }
        };
        return action.executeSync();
    }

    /**
     * Returns the initial cropping parameter bean for a given resource.<p>
     * 
     * @param resourcePath the resource path
     * 
     * @return the cropping parameter bean
     */
    private CmsCroppingParamBean getInitialCroppingParameter(String resourcePath) {

        CmsImageInfoBean imageInfo = getImageInfo(resourcePath);
        CmsImageFormatHandler formatHandler = new CmsImageFormatHandler(
            getGalleryMode(),
            resourcePath,
            imageInfo.getHeight(),
            imageInfo.getWidth());
        CmsCroppingParamBean param = formatHandler.getCroppingParam();
        if (formatHandler.isUseFormats()) {
            formatHandler.getFormats().values().iterator().next().adjustCroppingParam(param);
        }
        return param;
    }
}