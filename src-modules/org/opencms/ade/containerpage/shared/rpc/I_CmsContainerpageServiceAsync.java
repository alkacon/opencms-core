/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/rpc/Attic/I_CmsContainerpageServiceAsync.java,v $
 * Date   : $Date: 2010/05/18 14:09:26 $
 * Version: $Revision: 1.10 $
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * The RPC service asynchronous interface used by the container-page editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 8.0.0
 */
public interface I_CmsContainerpageServiceAsync {

    /**
     * Adds an element specified by it's id to the favorite list.<p>
     * 
     * @param clientId the element id
     * @param callback the call-back executed on response
     */
    void addToFavoriteList(String clientId, AsyncCallback<Void> callback);

    /**
     * Adds an element specified by it's id to the recent list.<p>
     * 
     * @param clientId the element id
     * @param callback the call-back executed on response
     */
    void addToRecentList(String clientId, AsyncCallback<Void> callback);

    /**
     * Requests container element data by client id.<p>
     * 
     * @param containerpageUri the current URI
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containerTypes the container type of the current page
     * @param callback the call-back executed on response
     */
    void getElementsData(
        String containerpageUri,
        String reqParams,
        Collection<String> clientIds,
        Set<String> containerTypes,
        AsyncCallback<Map<String, CmsContainerElement>> callback);

    /**
     * Gets the element data for an id and a map of properties.<p>
     * 
     * @param containerPageUri the current URI
     * @param reqParams optional request parameters 
     * @param clientId the requested element ids 
     * @param properties the properties for which the element data should be loaded 
     * @param types the container types of the current page
     * @param callback the callback for receiving the element data  
     */
    void getElementWithProperties(
        String containerPageUri,
        String reqParams,
        String clientId,
        Map<String, String> properties,
        Set<String> types,
        AsyncCallback<CmsContainerElement> callback);

    /**
     * Requests the container element data of the favorite list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containerTypes the container types of the current page
     * @param callback the call-back executed on response
     */
    void getFavoriteList(
        String containerpageUri,
        Set<String> containerTypes,
        AsyncCallback<List<CmsContainerElement>> callback);

    /**
     * Requests the container element data of the recent list.<p>
     * 
     * @param containerpageUri the current URI
     * @param containerTypes the container types of the current page
     * @param callback the call-back executed on response
     */
    void getRecentList(
        String containerpageUri,
        Set<String> containerTypes,
        AsyncCallback<List<CmsContainerElement>> callback);

    /**
     * Returns the initialization data.<p>
     * 
     * @param callback the async callback
     */
    void prefetch(AsyncCallback<CmsCntPageData> callback);

    /**
     * Saves the container-page.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the container-page's containers
     * @param callback the call-back executed on response
     */
    void saveContainerpage(String containerpageUri, List<CmsContainer> containers, AsyncCallback<Void> callback);

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds favorite list element id's
     * @param callback the call-back executed on response
     */
    void saveFavoriteList(List<String> clientIds, AsyncCallback<Void> callback);

    /**
     * Saves the recent list.<p>
     * 
     * @param clientIds recent list element id's
     * @param callback the call-back executed on response
     */
    void saveRecentList(List<String> clientIds, AsyncCallback<Void> callback);

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     * 
     * @param visible <code>true</code> if the tool-bar is visible
     * @param callback the call-back executed on response
     */
    void setToolbarVisible(boolean visible, AsyncCallback<Void> callback);

    /**
     * Generates request builder to make a synchronized RPC call saving the container-page.<p>
     * 
     * @param containerpageUri the current URI
     * @param containers the container-page's containers
     * @param callback the call-back executed on response
     */
    @SynchronizedRpcRequest
    void syncSaveContainerpage(String containerpageUri, List<CmsContainer> containers, AsyncCallback<Void> callback);
}
