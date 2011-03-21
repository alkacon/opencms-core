/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/rpc/Attic/I_CmsContainerpageService.java,v $
 * Date   : $Date: 2011/03/21 12:49:33 $
 * Version: $Revision: 1.16 $
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

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.gwt.CmsRpcException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The RPC service interface used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.16 $
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
     * Creates a new element of the given type and returns the new element data containing structure id and site path.<p>
     * 
     * @param containerpageUri the current URI
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * 
     * @return the new element data containing structure id and site path
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElement createNewElement(String containerpageUri, String clientId, String resourceType)
    throws CmsRpcException;

    /**
     * Deletes an element from the VFS.<p>
     * 
     * @param clientId the elements client id
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void deleteElement(String clientId) throws CmsRpcException;

    /**
     * Returns container element data by client id.<p>
     * 
     * @param containerpageUri the current URI
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containers the containers of the current page
     * 
     * @return the element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElementData> getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers) throws CmsRpcException;

    /**
     * Gets the element data for an id and a map of properties.<p>
     * 
     * @param containerPageUri the current URI
     * @param reqParams optional request parameters 
     * @param clientId the requested element ids 
     * @param properties the properties for which the element data should be loaded 
     * @param containers the containers of the current page 
     * 
     * @return the element data 
     * 
     * @throws CmsRpcException if something goes wrong processing the request 
     */
    CmsContainerElementData getElementWithProperties(
        String containerPageUri,
        String reqParams,
        String clientId,
        Map<String, String> properties,
        Collection<CmsContainer> containers) throws CmsRpcException;

    /**
     * Returns the container element data of the favorite list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the containers of the current page
     * 
     * @return the favorite list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getFavoriteList(String containerpageUri, Collection<CmsContainer> containers)
    throws CmsRpcException;

    /**
     * Returns the container element data of the recent list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the containers of the current page
     * 
     * @return the recent list element data
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getRecentList(String containerpageUri, Collection<CmsContainer> containers)
    throws CmsRpcException;

    /**
     * Returns the initialization data.<p>
     * 
     * @return the initialization data
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    CmsCntPageData prefetch() throws CmsRpcException;

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
     * Saves a group-container element.<p>
     * 
     * @param containerpageUri the current URI
     * @param reqParams optional request parameters
     * @param groupContainer the group-container to save
     * @param containers the containers of the current page
     * 
     * @return the data of the saved group container 
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElementData> saveGroupContainer(
        String containerpageUri,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers) throws CmsRpcException;

    /**
     * Saves the recent list.<p>
     * 
     * @param clientIds recent list element id's
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveRecentList(List<String> clientIds) throws CmsRpcException;

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     * 
     * @param visible <code>true</code> if the tool-bar is visible
     * 
     * @throws CmsRpcException
     */
    void setToolbarVisible(boolean visible) throws CmsRpcException;

    /**
     * Saves the container-page in a synchronized RPC call.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the container-page's containers
     * 
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void syncSaveContainerpage(String containerpageUri, List<CmsContainer> containers) throws CmsRpcException;
}
