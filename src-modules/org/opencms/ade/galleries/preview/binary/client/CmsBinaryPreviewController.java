/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/client/Attic/CmsBinaryPreviewController.java,v $
 * Date   : $Date: 2010/07/05 14:48:06 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.galleries.preview.binary.client;

import org.opencms.ade.galleries.client.preview.A_CmsPreviewController;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;

import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * Binary preview dialog controller.<p>
 * 
 * This class handles the communication between preview dialog and the server.  
 * 
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public final class CmsBinaryPreviewController extends A_CmsPreviewController<CmsResourceInfoBean> {

    /** The preview service. */
    private I_CmsPreviewServiceAsync m_previewService;

    /**
     * Hiding constructor.<p>
     * 
     * @param handler the preview controller handler
     */
    public CmsBinaryPreviewController(CmsBinaryPreviewHandler handler) {

        super(handler);
        handler.init(this);
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

                getService().getResourceInfo(resourcePath, this);
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
     * @see org.opencms.ade.galleries.client.preview.I_CmsPreviewController#saveProperties(java.util.Map)
     */
    public void saveProperties(final Map<String, String> properties) {

        CmsRpcAction<CmsResourceInfoBean> action = new CmsRpcAction<CmsResourceInfoBean>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().updateResourceProperties(getResourcePath(), properties, this);
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
