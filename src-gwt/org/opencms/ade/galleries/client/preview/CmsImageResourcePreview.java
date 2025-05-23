/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsImagePreviewProvider;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsUUID;

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
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#getViewLink()
     */
    @Override
    public String getViewLink() {

        return m_infoBean.getViewLink();
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
                result.setViewLink(result.getViewLink());
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
        m_previewDialog = new CmsImagePreviewDialog(
            getGalleryDialog().getController().getDialogMode(),
            parentPanel.getOffsetHeight(),
            parentPanel.getOffsetWidth(),
            disableSelection);
        // initialize the controller and controller handler
        m_handler = new CmsImagePreviewHandler(this);
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
     * @see org.opencms.ade.galleries.client.preview.A_CmsResourcePreview#selectResource(java.lang.String, org.opencms.util.CmsUUID, java.lang.String)
     */
    @Override
    public void selectResource(final String resourcePath, final CmsUUID structureId, final String title) {

        switch (getGalleryMode()) {
            case widget:
                getInitialCroppingParameter(resourcePath, new I_CmsSimpleCallback<CmsCroppingParamBean>() {

                    public void execute(CmsCroppingParamBean param) {

                        if (getGalleryDialog().getWidgetHandler() != null) {
                            getGalleryDialog().getWidgetHandler().setWidgetValue(resourcePath, structureId, param);
                        } else {
                            if (CmsPreviewUtil.isAdvancedWidget()) {
                                CmsPreviewUtil.setVfsImage(
                                    resourcePath,
                                    param.getScaleParam(false),
                                    param.getFormatName(),
                                    param.getRatio() + "");
                            } else {
                                CmsPreviewUtil.setResourcePath(
                                    resourcePath
                                        + ((param.isCropped() || param.isScaled()) ? "?" + param.toString() : ""));
                            }
                        }
                    }
                });

                break;
            case editor:
                getInitialCroppingParameter(resourcePath, new I_CmsSimpleCallback<CmsCroppingParamBean>() {

                    public void execute(CmsCroppingParamBean param) {

                        Map<String, String> attributes = new HashMap<String, String>();
                        attributes.put("title", title);
                        attributes.put("width", String.valueOf(param.getResultingWidth()));
                        attributes.put("height", String.valueOf(param.getResultingHeight()));
                        CmsPreviewUtil.setImage(
                            CmsCoreProvider.get().link(resourcePath)
                                + (param.isScaled() ? "?" + param.toString() + ",c:transparent" : ""),
                            attributes);
                        CmsPreviewUtil.closeDialog();
                    }
                });
                break;
            case ade:
            case view:
            case adeView:
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
        final CmsCroppingParamBean croppingParam = m_handler.getCroppingParam();

        switch (getGalleryMode()) {
            case widget:
                if (getGalleryDialog().getWidgetHandler() != null) {
                    getGalleryDialog().getWidgetHandler().setWidgetValue(
                        m_infoBean.getResourcePath(),
                        m_infoBean.getStructureId(),
                        croppingParam);
                } else {
                    if (CmsPreviewUtil.isAdvancedWidget()) {
                        CmsPreviewUtil.setVfsImage(
                            m_infoBean.getResourcePath(),
                            croppingParam.getScaleParam(false),
                            croppingParam.getFormatName(),
                            croppingParam.getRatio() + "");
                    } else {
                        CmsPreviewUtil.setResourcePath(
                            m_infoBean.getResourcePath()
                                + ((croppingParam.isCropped() || croppingParam.isScaled())
                                ? "?" + croppingParam.toString()
                                : ""));
                    }
                }
                break;
            case editor:
                m_handler.getImageAttributes(new I_CmsSimpleCallback<Map<String, String>>() {

                    public void execute(Map<String, String> attributes) {

                        CmsPreviewUtil.setImage(
                            CmsCoreProvider.get().link(
                                m_infoBean.getResourcePath()
                                    + ((croppingParam.isCropped() || croppingParam.isScaled())
                                    ? "?" + croppingParam.toString() + ",c:transparent"
                                    : "")),
                            attributes);
                    }
                });

                break;
            case ade:
            case view:
            case adeView:
            default:
                //nothing to do here, should not be called
                break;
        }
    }

    /**
     * Returns the image info bean for the given resource.<p>
     *
     * @param resourcePath the resource path
     * @param callback the calback to execute
     */
    private void getImageInfo(final String resourcePath, final I_CmsSimpleCallback<CmsImageInfoBean> callback) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                A_CmsResourcePreview.getService().getImageInfo(resourcePath, getLocale(), this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsImageInfoBean result) {

                stop(false);
                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Returns the initial cropping parameter bean for a given resource.<p>
     *
     * @param resourcePath the resource path
     * @param callback the callback to execute
     */
    private void getInitialCroppingParameter(
        final String resourcePath,
        final I_CmsSimpleCallback<CmsCroppingParamBean> callback) {

        getImageInfo(resourcePath, new I_CmsSimpleCallback<CmsImageInfoBean>() {

            public void execute(CmsImageInfoBean imageInfo) {

                CmsImageFormatHandler formatHandler = new CmsImageFormatHandler(
                    getGalleryMode(),
                    getGalleryDialog(),
                    resourcePath,
                    imageInfo.getHeight(),
                    imageInfo.getWidth());
                CmsCroppingParamBean param = formatHandler.getCroppingParam();
                if (formatHandler.isUseFormats()) {
                    formatHandler.getFormats().values().iterator().next().adjustCroppingParam(param);
                }
                callback.execute(param);
            }
        });

    }
}
