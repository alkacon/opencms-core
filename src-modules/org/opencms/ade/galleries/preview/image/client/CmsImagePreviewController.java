/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImagePreviewController.java,v $
 * Date   : $Date: 2010/07/06 14:54:45 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.galleries.preview.image.client;

import org.opencms.ade.galleries.client.preview.A_CmsPreviewController;
import org.opencms.ade.galleries.client.preview.CmsPreviewUtil;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * Image preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsImagePreviewController extends A_CmsPreviewController<CmsImageInfoBean> {

    /** The preview service. */
    private I_CmsPreviewServiceAsync m_previewService;

    /**
     * Constructor.<p>
     * 
     * @param handler the controller handler 
     */
    public CmsImagePreviewController(CmsImagePreviewHandler handler) {

        super(handler);
        handler.init(this);
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
                CmsPreviewUtil.setImage(resourcePath, attributes);
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

                showData(result);
            }
        };
        action.execute();

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
     * Returns the preview service.<p>
     * 
     * @return the preview service
     */
    protected I_CmsPreviewServiceAsync getService() {

        if (m_previewService == null) {
            m_previewService = GWT.create(I_CmsPreviewService.class);
        }
        return m_previewService;
    }
}