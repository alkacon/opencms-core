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

package org.opencms.ade.publish.shared.rpc;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The asynchronous interface to the publish service.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsPublishServiceAsync {

    /**
     * Asynchronous version of {@link I_CmsPublishService#getInitData()}.<p>
     * 
     * @param callback the result callback
     */
    void getInitData(AsyncCallback<CmsPublishData> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getProjects()}.<p>
     * 
     * @param callback the result callback
     */
    void getProjects(AsyncCallback<List<CmsProjectBean>> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getPublishGroups(CmsPublishOptions)}.<p>
     * 
     * @param options the publish list options
     * @param callback the result callback
     */
    void getPublishGroups(CmsPublishOptions options, AsyncCallback<List<CmsPublishGroup>> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getPublishOptions()}.<p>
     * 
     * @param callback the result callback
     */
    void getPublishOptions(AsyncCallback<CmsPublishOptions> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#publishResources(List, List, boolean)}.<p>
     * 
     * @param toPublish the resources to publish 
     * @param toRemove the resources to remove
     * @param force if true, try to ignore broken links
     * @param callback the result callback 
     */
    void publishResources(
        List<CmsUUID> toPublish,
        List<CmsUUID> toRemove,
        boolean force,
        AsyncCallback<List<CmsPublishResource>> callback);
}
