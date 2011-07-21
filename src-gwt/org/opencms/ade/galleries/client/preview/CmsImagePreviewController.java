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

import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Image preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @since 8.0.0
 */
public class CmsImagePreviewController extends A_CmsPreviewController<CmsImageInfoBean> {

    /** The image preview handler. */
    private CmsImagePreviewHandler m_handler;

    /**
     * Constructor.<p>
     * 
     * @param handler the controller handler 
     */
    public CmsImagePreviewController(CmsImagePreviewHandler handler) {

        m_handler = handler;
        m_handler.init(this);
    }

    /**
     * Selects the image resource.<p>
     * 
     * @param galleryMode
     * @param resourcePath
     * @param title
     */
    public static void select(GalleryMode galleryMode, String resourcePath, String title) {

        CmsCroppingParamBean param;
        switch (galleryMode) {
            case widget:
                param = getInitialCroppingParameter(galleryMode, resourcePath);
                if (CmsPreviewUtil.isAdvancedWidget()) {
                    CmsPreviewUtil.setVfsImage(
                        resourcePath,
                        param.getScaleParam(),
                        param.getFormatName(),
                        ((double)param.getTargetWidth() / param.getTargetHeight()) + "");
                } else {
                    CmsPreviewUtil.setResourcePath(resourcePath
                        + ((param.isCropped() || param.isScaled()) ? "?" + param.toString() : ""));
                }
                break;
            case editor:
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("title", title);
                param = getInitialCroppingParameter(galleryMode, resourcePath);
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
     * Returns the image info bean for the given resource.<p>
     * 
     * @param resourcePath the resource path
     * 
     * @return the image info bean
     */
    private static CmsImageInfoBean getImageInfo(final String resourcePath) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                A_CmsPreviewController.getService().syncGetImageInfo(resourcePath, this);
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
     * @param galleryMode the gallery mode
     * @param resourcePath the resource path
     * 
     * @return the cropping parameter bean
     */
    private static CmsCroppingParamBean getInitialCroppingParameter(GalleryMode galleryMode, String resourcePath) {

        CmsImageInfoBean imageInfo = getImageInfo(resourcePath);
        CmsImageFormatHandler formatHandler = new CmsImageFormatHandler(
            galleryMode,
            resourcePath,
            imageInfo.getHeight(),
            imageInfo.getWidth());
        CmsCroppingParamBean param = formatHandler.getCroppingParam();
        formatHandler.getFormats().values().iterator().next().adjustCroppingParam(param);
        return param;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewController#getHandler()
     */
    @Override
    public I_CmsPreviewHandler<CmsImageInfoBean> getHandler() {

        return m_handler;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#loadResourceInfo(java.lang.String)
     */
    public void loadResourceInfo(final String resourcePath) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                A_CmsPreviewController.getService().getImageInfo(resourcePath, this);
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
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewController#removePreview()
     */
    @Override
    public void removePreview() {

        super.removePreview();
        m_handler = null;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#saveProperties(java.util.Map)
     */
    public void saveProperties(final Map<String, String> properties) {

        CmsRpcAction<CmsImageInfoBean> action = new CmsRpcAction<CmsImageInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().updateImageProperties(getResourcePath(), properties, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsImageInfoBean result) {

                showData(result);
            }
        };
        action.execute();

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewController#setResource(org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode)
     */
    @Override
    public void setResource(GalleryMode galleryMode) {

        CmsCroppingParamBean croppingParam = m_handler.getCroppingParam();

        switch (galleryMode) {
            case widget:
                if (CmsPreviewUtil.isAdvancedWidget()) {
                    CmsPreviewUtil.setVfsImage(
                        m_infoBean.getResourcePath(),
                        croppingParam.getScaleParam(),
                        croppingParam.getFormatName(),
                        ((double)croppingParam.getTargetWidth() / croppingParam.getTargetHeight()) + "");
                } else {
                    CmsPreviewUtil.setResourcePath(m_infoBean.getResourcePath()
                        + ((croppingParam.isCropped() || croppingParam.isScaled())
                        ? "?" + croppingParam.toString()
                        : ""));
                }
                break;
            case editor:
                Map<String, String> attributes = m_handler.getImageAttributes();
                CmsPreviewUtil.setImage(CmsCoreProvider.get().link(
                    m_infoBean.getResourcePath()
                        + ((croppingParam.isCropped() || croppingParam.isScaled())
                        ? "?" + croppingParam.toString()
                        : "")), attributes);
                break;
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
    }
}