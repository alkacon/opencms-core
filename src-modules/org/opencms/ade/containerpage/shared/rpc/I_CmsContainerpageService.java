/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/rpc/Attic/I_CmsContainerpageService.java,v $
 * Date   : $Date: 2010/04/21 14:13:46 $
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

package org.opencms.ade.containerpage.shared.rpc;

import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.shared.rpc.CmsRpcException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The RPC service interface used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
@RemoteServiceRelativePath("org.opencms.ade.containerpage.CmsContainerpageService.gwt")
public interface I_CmsContainerpageService extends RemoteService {

    /**
     * Adds an element specified by it's id to the favorite list.<p>
     * 
     * @param clientId the element id
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToFavoriteList(String clientId) throws CmsRpcException;

    /**
     * Adds an element specified by it's id to the recent list.<p>
     * 
     * @param clientId the element id
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToRecentList(String clientId) throws CmsRpcException;

    /**
     * Returns container element data by client id.<p>
     * 
     * @param containerpageUri the current URI
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containerTypes the container types of the current page
     * 
     * @return the element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElement> getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Set<String> containerTypes) throws CmsRpcException;

    /**
     * Returns the container element data of the favorite list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containerTypes the container types of the current page
     * 
     * @return the favorite list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    LinkedHashMap<String, CmsContainerElement> getFavoriteList(String containerpageUri, Set<String> containerTypes)
    throws CmsRpcException;

    /**
     * Returns the container element data of the recent list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containerTypes the container types of the current page
     * 
     * @return the recent list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    LinkedHashMap<String, CmsContainerElement> getRecentList(String containerpageUri, Set<String> containerTypes)
    throws CmsRpcException;

    /**
     * Saves the container-page.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the container-page's containers
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveContainerpage(String containerpageUri, List<CmsContainer> containers) throws CmsRpcException;

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds favorite list element id's
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveFavoriteList(List<String> clientIds) throws CmsRpcException;

    /**
     * Saves the recent list.<p>
     * 
     * @param clientIds recent list element id's
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveRecentList(List<String> clientIds) throws CmsRpcException;

}
