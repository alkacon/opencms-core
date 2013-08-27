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

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The synchronous publish list interface.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsPublishService extends RemoteService {

    /**
     * Tries to publish a list of resources.<p>
     * 
     * @param toPublish list of IDs of resources to publish
     * @param toRemove list of IDs of resources to remove from the publish list
     * @param action the work flow action
     *  
     * @return the workflow response
     * 
     * @throws CmsRpcException  if something goes wrong
     */
    CmsWorkflowResponse executeAction(List<CmsUUID> toPublish, List<CmsUUID> toRemove, CmsWorkflowAction action)
    throws CmsRpcException;

    /**
     * Returns the initial publish data.<p>
     * 
     * @param params a map of additional publish parameters 
     * 
     * @return the initial publish data
     *  
     * @throws CmsRpcException if something goes wrong
     */
    CmsPublishData getInitData(HashMap<String, String> params) throws CmsRpcException;

    /**
     * Retrieves the publish list, subdivided into groups based on the time of their last change.<p>
     * 
     * @param workflow the selected workflow 
     * @param options the publish options for which the publish list should be fetched
     * 
     * @return the publish list groups 
     *  
     * @throws CmsRpcException if something goes wrong
     */
    List<CmsPublishGroup> getResourceGroups(CmsWorkflow workflow, CmsPublishOptions options) throws CmsRpcException;

}
