/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/rpc/Attic/I_CmsPreviewServiceAsync.java,v $
 * Date   : $Date: 2010/06/10 08:45:04 $
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

package org.opencms.ade.galleries.shared.rpc;

import org.opencms.ade.galleries.shared.CmsResourceInfoBean;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles the common RPC services related to the gallery preview dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsPreviewService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewServiceAsync
 */
public interface I_CmsPreviewServiceAsync {

    /**
     * Returns the data to be displayed in the preview dialog.<p>
     * 
     * @param resourcePath the path to the selected resource
     * 
     * @param callback the call-back 
     */
    void getResourceInfo(String resourcePath, AsyncCallback<CmsResourceInfoBean> callback);

    /**
     * Returns the data to be displayed in the preview dialog.<p>
     * 
     * @param resourcePath the path to the selected resource
     * @param properties a map with the key/value pairs of the properties to be updated
     * 
     * @param callback the call-back 
     */
    void updateProperties(
        String resourcePath,
        Map<String, String> properties,
        AsyncCallback<CmsResourceInfoBean> callback);

}