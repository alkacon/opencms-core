/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.ade.postupload.shared.rpc;

import org.opencms.ade.postupload.shared.CmsPostUploadDialogBean;
import org.opencms.ade.postupload.shared.CmsPostUploadDialogPanelBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous service interface for the upload property dialog.<p>
 */
public interface I_CmsPostUploadDialogServiceAsync {

    /**
     * Loads the dialog bean info for the given resource.<p>
     *
     * @param uuid the structure id of the resource to load
     * @param useConfiguration true if the property configurations should be used
     * @param addBasicProperties if true, the basic properties configured for the sitemap are added in the dialog
     * @param asyncCallback the asynchronous callback for the result
     */
    void load(
        CmsUUID uuid,
        boolean useConfiguration,
        boolean addBasicProperties,
        AsyncCallback<CmsPostUploadDialogPanelBean> asyncCallback);

    /**
     * Generates dialog data for prefetching in the host page.<p>
     *
     * @param asyncCallback the asynchronous callback for the result
     */
    void prefetch(AsyncCallback<CmsPostUploadDialogBean> asyncCallback);

}
