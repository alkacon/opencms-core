/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsImagePreviewController.java,v $
 * Date   : $Date: 2011/04/28 19:42:42 $
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
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $ 
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

        switch (galleryMode) {
            case widget:
                CmsPreviewUtil.setResourcePath(resourcePath);
                break;
            case editor:
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("title", title);
                CmsPreviewUtil.setImage(CmsCoreProvider.get().link(resourcePath), attributes);
                CmsPreviewUtil.closeDialog();
                break;
            case sitemap:
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
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

                getService().getImageInfo(resourcePath, this);
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

        switch (galleryMode) {
            case widget:
                CmsPreviewUtil.setResourcePath(m_infoBean.getResourcePath() + m_handler.getScaleParam());
                break;
            case editor:
                CmsPreviewUtil.setImage(
                    CmsCoreProvider.get().link(m_infoBean.getResourcePath() + m_handler.getScaleParam()),
                    m_handler.getImageAttributes());
                break;
            case sitemap:
            case ade:
            case view:
            default:
                //nothing to do here, should not be called
                break;
        }
    }
}