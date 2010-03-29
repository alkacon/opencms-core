/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/shared/rpc/Attic/I_CmsPublishService.java,v $
 * Date   : $Date: 2010/03/29 08:47:34 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.publish.shared.rpc;

import org.opencms.ade.publish.shared.CmsClientPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishGroups;
import org.opencms.ade.publish.shared.CmsPublishOptionsAndProjects;
import org.opencms.ade.publish.shared.CmsPublishStatus;
import org.opencms.gwt.shared.rpc.CmsRpcException;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The synchronous publish list interface.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
@RemoteServiceRelativePath("org.opencms.ade.publish.CmsPublishService.gwt")
public interface I_CmsPublishService extends RemoteService {

    /**
     * Gets a map of projects from the server.<p>
     * 
     * The map's keys are the project uuids, the values are the user-readable names.
     * 
     * @return a map of projects 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    Map<String, String> getProjects() throws CmsRpcException;

    /**
     * Retrieves the publish list, subdivided into groups based on the time of their last change.<p>
     * 
     * @param options the publish options for which the publish list should be fetched. 
     * 
     * @return the publish list groups 
     *  
     * @throws CmsRpcException if something goes wrong
     */
    CmsPublishGroups getPublishGroups(CmsClientPublishOptions options) throws CmsRpcException;

    /**
     * Retrieves the publish options.<p>
     * 
     * @return the publish options last used
     * 
     * @throws CmsRpcException if something goes wrong.
     */
    CmsClientPublishOptions getPublishOptions() throws CmsRpcException;

    /**
     * Gets a bean containing both the publish options and the list of projects.<p>
     * 
     * @return a list containing projects and publish options
     *  
     * @throws CmsRpcException if something goes wrong
     */
    CmsPublishOptionsAndProjects getPublishOptionsAndProjects() throws CmsRpcException;

    /**
     * Tries to publish a list of resources.<p>
     * 
     * @param toPublish list of uuids of resources to publish
     * 
     * @param toRemove list of uuids of resources to remove from the publish list
     * 
     * @param force if true, ignore "broken link" problems
     *  
     * @return a status object containing the 
     * @throws CmsRpcException
     */
    CmsPublishStatus publishResources(List<String> toPublish, List<String> toRemove, boolean force)
    throws CmsRpcException;

}
