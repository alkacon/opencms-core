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

package org.opencms.ade.containerpage.shared.rpc;

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SynchronizedRpcRequest;

/**
 * The RPC service asynchronous interface used by the container-page editor.<p>
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
     * To create a new element of the given type this method will check if a model resource needs to be selected, otherwise creates the new element.
     * Returns a bean containing either the new element data or a list of model resources to select.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void checkCreateNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        String locale,
        AsyncCallback<CmsCreateElementData> callback);

    /**
     * Checks whether the Acacia widgets are available for all fields of the content.<p>
     *  
     * @param structureId the structure id of the content to check.<p>
     * 
     * @param resultCallback the callback for the result 
     */
    void checkNewWidgetsAvailable(CmsUUID structureId, AsyncCallback<Boolean> resultCallback);

    /**
     * Creates a new element of the given type and returns the new element data containing structure id and site path.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param modelResourceStructureId the model resource structure id
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void createNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsUUID modelResourceStructureId,
        String locale,
        AsyncCallback<CmsContainerElement> callback);

    /**
     * This method is used for serialization purposes only.<p>
     *  
     * @param callback the callback
     */
    void getElementInfo(AsyncCallback<CmsContainerElement> callback);

    /**
     * Requests container element data by client id.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getElementsData(
        CmsUUID pageStructureId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<Map<String, CmsContainerElementData>> callback);

    /**
     * Gets the element data for an id and a map of settings.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param reqParams optional request parameters 
     * @param clientId the requested element ids 
     * @param settings the settings for which the element data should be loaded 
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the callback for receiving the element data  
     */
    void getElementWithSettings(
        CmsUUID pageStructureId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsContainerElementData> callback);

    /**
     * Requests the container element data of the favorite list.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getFavoriteList(CmsUUID pageStructureId,

    Collection<CmsContainer> containers, String locale, AsyncCallback<List<CmsContainerElementData>> callback);

    /**
     * Returns new container element data for the given resource type name.<p>
     * 
     * @param  pageStructureId the container page structure id
     * @param reqParams optional request parameters
     * @param resourceType the requested element resource type name
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getNewElementData(
        CmsUUID pageStructureId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsContainerElementData> callback);

    /**
     * Requests the container element data of the recent list.<p>
     * 
     * @param pageStructureId the container page structure id 
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getRecentList(CmsUUID pageStructureId,

    Collection<CmsContainer> containers, String locale, AsyncCallback<List<CmsContainerElementData>> callback);

    /**
     * Gets the status of a removed element.<p>
     * 
     * @param id the element's client id 
     * @param containerpageId the id of the container page which should be excluded from the relation check, or null if no page should be excluded 
     * 
     * @param callback the asynchronous callback to execute with the results 
     */
    void getRemovedElementStatus(String id, CmsUUID containerpageId, AsyncCallback<CmsRemovedElementStatus> callback);

    /**
     * Returns the initialization data.<p>
     * 
     * @param callback the async callback
     */
    void prefetch(AsyncCallback<CmsCntPageData> callback);

    /**
     * Saves the container-page.<p> 
     * 
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void saveContainerpage(
        CmsUUID pageStructureId,
        List<CmsContainer> containers,
        String locale,
        AsyncCallback<Void> callback);

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds favorite list element id's
     * @param callback the call-back executed on response
     */
    void saveFavoriteList(List<String> clientIds, AsyncCallback<Void> callback);

    /**
     * Saves a group-container element.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param reqParams optional request parameters
     * @param groupContainer the group-container to save
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void saveGroupContainer(
        CmsUUID pageStructureId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsGroupContainerSaveResult> callback);

    /**
     * Saves an inheritance container.<p>
     * 
     * @param pageStructureId the current page's structure id
     * @param inheritanceContainer the inheritance container to save
     * @param containers the containers of the current page
     * @param locale the requested locale
     * @param callback the callback
     */
    void saveInheritanceContainer(
        CmsUUID pageStructureId,
        CmsInheritanceContainer inheritanceContainer,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<Map<String, CmsContainerElementData>> callback);

    /**
     * Saves the recent list.<p>
     * 
     * @param clientIds recent list element id's
     * @param callback the call-back executed on response
     */
    void saveRecentList(List<String> clientIds, AsyncCallback<Void> callback);

    /** 
     * Saves the default value for small element editability on page load.<p>
     * 
     * @param editSmallElements the default value 
     * 
     * @param callback the callback for the response 
     */
    void setEditSmallElements(boolean editSmallElements, AsyncCallback<Void> callback);

    /**
     * Generates request builder to make a synchronized RPC call saving the container-page.<p>
     * 
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    @SynchronizedRpcRequest
    void syncSaveContainerpage(
        CmsUUID pageStructureId,
        List<CmsContainer> containers,
        String locale,
        AsyncCallback<Void> callback);
}