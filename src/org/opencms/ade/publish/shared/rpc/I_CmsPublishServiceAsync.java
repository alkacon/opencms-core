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

package org.opencms.ade.publish.shared.rpc;

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroupList;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowActionParams;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;

import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The asynchronous interface to the publish service.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsPublishServiceAsync {

    /**
     * Executes a workflow action.<p>
     *
     * @param action the workflow action
     * @param params the workflow parameters
     * @param callback the result callback
     */
    void executeAction(
        CmsWorkflowAction action,
        CmsWorkflowActionParams params,
        AsyncCallback<CmsWorkflowResponse> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getInitData(HashMap)}.<p>
     *
     * @param params the additional publish parameters
     * @param callback the result callback
     */
    void getInitData(HashMap<String, String> params, AsyncCallback<CmsPublishData> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getResourceGroups(CmsWorkflow,CmsPublishOptions,boolean)}.<p>
     *
     * @param workflow the selected workflow
     * @param options the publish list options
     * @param callback the result callback
     * @param projectChanged indicates whether the reason we get the resource groups is because the user changed the project
     */
    void getResourceGroups(
        CmsWorkflow workflow,
        CmsPublishOptions options,
        boolean projectChanged,
        AsyncCallback<CmsPublishGroupList> callback);

    /**
     * Asynchronous version of {@link I_CmsPublishService#getResourceOptions()}.<p>
     *
     * @param callback the result callback
     */
    void getResourceOptions(AsyncCallback<CmsPublishOptions> callback);

}
