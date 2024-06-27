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

package org.opencms.ade.containerpage.shared.rpc;

import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsContainerPageGalleryData;
import org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsDialogOptionsAndInfo;
import org.opencms.ade.containerpage.shared.CmsElementSettingsConfig;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.ade.containerpage.shared.CmsReuseInfo;
import org.opencms.gwt.shared.CmsListElementCreationDialogData;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The RPC service asynchronous interface used by the container-page editor.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsContainerpageServiceAsync {

    /**
    * Adds an element specified by it's id to the favorite list.<p>
    *
    * @param context the rpc context
    * @param clientId the element id
    * @param callback the call-back executed on response
    */
    void addToFavoriteList(CmsContainerPageRpcContext context, String clientId, AsyncCallback<Void> callback);

    /**
     * Adds an element specified by it's id to the recent list.<p>
     *
     * @param context the rpc context
     * @param clientId the element id
     * @param callback the call-back executed on response
     */
    void addToRecentList(CmsContainerPageRpcContext context, String clientId, AsyncCallback<Void> callback);

    /**
     * Check if a page or its elements have been changed.<p>
     *
     * @param structureId the id of the container page
     * @param detailContentId the structure id of the detail content (may be null)
     * @param contentLocale the content locale
     * @param callback the callback for the result
     */
    void checkContainerpageOrElementsChanged(
        CmsUUID structureId,
        CmsUUID detailContentId,
        String contentLocale,
        AsyncCallback<Boolean> callback);

    /**
     * To create a new element of the given type this method will check if a model resource needs to be selected, otherwise creates the new element.
     * Returns a bean containing either the new element data or a list of model resources to select.<p>
     *
     * @param pageStructureId the container page structure id
     * @param detailContentId the detail content id
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param container the parent container
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void checkCreateNewElement(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        String clientId,
        String resourceType,
        CmsContainer container,
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
     * Creates  a new element with a given model element and returns the copy'S structure id.<p>
     *
     * @param pageId the container page id
     * @param originalElementId the model element id
     * @param locale the content locale
     * @param resultCallback the callback for the result
     */
    void copyElement(CmsUUID pageId, CmsUUID originalElementId, String locale, AsyncCallback<CmsUUID> resultCallback);

    /**
     * Creates a new element of the given type and returns the new element data containing structure id and site path.<p>
     *
     * @param pageStructureId the container page structure id
     * @param detailContentId the structure id of the detail content
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param modelResourceStructureId the model resource structure id
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void createNewElement(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
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
    void getContainerInfo(AsyncCallback<CmsContainer> callback);

    /**
     * Returns the delete options.<p>
     *
     * @param clientId the client element id
     * @param pageStructureId the current page structure id
     * @param requestParams optional request parameters
     * @param callback the async callback
     */
    void getDeleteOptions(
        String clientId,
        CmsUUID pageStructureId,
        String requestParams,
        AsyncCallback<CmsDialogOptionsAndInfo> callback);

    /**
     * Returns the edit options.<p>
     *
     * @param clientId the client element id
     * @param pageStructureId the current page structure id
     * @param requestParams optional request parameters
     * @param isListElement in case a list element, not a container element is about to be edited
     * @param callback the async callback
     */
    void getEditOptions(
        String clientId,
        CmsUUID pageStructureId,
        String requestParams,
        boolean isListElement,
        AsyncCallback<CmsDialogOptionsAndInfo> callback);

    /**
     * This method is used for serialization purposes only.<p>
     *
     * @param callback the callback
     */
    void getElementInfo(AsyncCallback<CmsContainerElement> callback);

    /**
     * Requests container element data by client id.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containers the containers of the current page
     * @param alwaysCopy <code>true</code> in case reading data for a clipboard element used as a copy group
     * @param dndSource in the DND case, the id of the origin container from which the element is dragged
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getElementsData(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        boolean alwaysCopy,
        String dndSource,
        String locale,
        AsyncCallback<Map<String, CmsContainerElementData>> callback);

    /**
     * Returns container element settings config data.<p>
     *
     * @param  context the rpc context
     * @param clientId the requested element id
     * @param containerId the parent container id
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getElementSettingsConfig(
        CmsContainerPageRpcContext context,
        String clientId,
        String containerId,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsElementSettingsConfig> callback);

    /**
     * Checks which structure ids of a given set belong to resources locked for publishing by the current user, and then returns those.
     *
     * @param idsToCheck the set of ids to check
     * @param callback the callback to call with the result
     */
    void getElementsLockedForPublishing(Set<CmsUUID> idsToCheck, AsyncCallback<Set<CmsUUID>> callback);

    /**
     * Gets the element data for an id and a map of settings.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientId the requested element ids
     * @param settings the settings for which the element data should be loaded
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the callback for receiving the element data
     */
    void getElementWithSettings(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
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
     * @param detailContentId the detail content structure id
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getFavoriteList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<List<CmsContainerElementData>> callback);

    /**
     * Returns the gallery configuration data according to the current page containers and the selected element view.<p>
     *
     * @param containers the page containers
     * @param elementView the element view
     * @param uri the page URI
     * @param detailContentId the detail content id
     * @param locale the content locale
     * @param contextInfo the template context information
     * @param callback the call-back executed on response
     */
    void getGalleryDataForPage(
        List<CmsContainer> containers,
        CmsUUID elementView,
        String uri,
        CmsUUID detailContentId,
        String locale,
        CmsTemplateContextInfo contextInfo,
        AsyncCallback<CmsContainerPageGalleryData> callback);

    /**
     * Loads the data for the list element creation dialog.
     *
     * @param structureId the structure id of the container element for which we want to load the options
     * @param jsonListAddData the list-add metadata read from the DOM
     * @param callback the callback for the result
     */
    void getListElementCreationOptions(
        CmsUUID structureId,
        String jsonListAddData,
        AsyncCallback<CmsListElementCreationDialogData> callback);

    /**
     * Returns new container element data for the given resource type name.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param resourceType the requested element resource type name
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getNewElementData(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsContainerElementData> callback);

    /**
     * Gets the edit handler options for creating a new element.<p>
     *
     * @param clientId the client id of the selected element
     * @param pageStructureId the container page structure id
     * @param requestParams the request parameter string
     * @param callback the callback to call when done
     */
    void getNewOptions(
        String clientId,
        CmsUUID pageStructureId,
        String requestParams,
        AsyncCallback<CmsDialogOptionsAndInfo> callback);

    /**
     * Requests the container element data of the recent list.<p>
     *
     * @param pageStructureId the container page structure id
     * @param detailContentId the detail content structure id
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void getRecentList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<List<CmsContainerElementData>> callback);

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
     * Loads reuse information for a container element.
     *
     * @param pageId the id of the current page
     * @param detailId the detail id for the current page
     * @param elementId the id of the container element
     * @param callback the callback to call with the result
     */
    void getReuseInfo(CmsUUID pageId, CmsUUID detailId, CmsUUID elementId, AsyncCallback<CmsReuseInfo> callback);

    /**
     * Handles the element deletion.<p>
     *
     * @param clientId the client element id
     * @param deleteOption the selected delete option
     * @param pageStructureId the current page structure id
     * @param requestParams optional request parameters
     * @param callback the asynchronous callback to execute with the results
     */
    void handleDelete(
        String clientId,
        String deleteOption,
        CmsUUID pageStructureId,
        String requestParams,
        AsyncCallback<Void> callback);

    /**
     * Loads the clipboard tab to initially select.<p>
     *
     * @param resultCallback the result callback
     */
    void loadClipboardTab(AsyncCallback<Integer> resultCallback);

    /**
     * Returns the initialization data.<p>
     *
     * @param callback the async callback
     */
    void prefetch(AsyncCallback<CmsCntPageData> callback);

    /**
     * Prepares an element to be edited.<p>
     *
     * @param clientId the client element id
     * @param editOption the selected delete option
     * @param pageStructureId the current page structure id
     * @param requestParams optional request parameters
     * @param callback the async callback
     */
    void prepareForEdit(
        String clientId,
        String editOption,
        CmsUUID pageStructureId,
        String requestParams,
        AsyncCallback<CmsUUID> callback);

    /**
     * Returns the element data to replace a given content element with another while keeping it's settings.<p>
     *
     * @param  context the rpc context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientId the id of the element to replace
     * @param replaceId the id of the replacing element
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the async callback
     */
    void replaceElement(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String clientId,
        String replaceId,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsContainerElementData> callback);

    /**
     * Saves the selected clipboard tab.<p>
     *
     * @param tabIndex the index of the selected clipboard tab
     * @param callback the result callback
     */
    void saveClipboardTab(int tabIndex, AsyncCallback<Void> callback);

    /**
     * Saves the container-page. Returning the save time stamp.<p>
     *
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     * @param callback the call-back executed on response
     */
    void saveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers, AsyncCallback<Long> callback);

    /**
     * Saves the detail containers. Returning the save time stamp.<p>
     *
     * @param detailId the detail content id
     * @param detailContainerResource the detail container resource path
     * @param containers the container-page's containers
     * @param callback the call-back executed on response
     */
    void saveDetailContainers(
        CmsUUID detailId,
        String detailContainerResource,
        List<CmsContainer> containers,

        AsyncCallback<Long> callback);

    /**
     * Saves the settings for the given element to the container page and returns the updated element data.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientId the requested element ids
     * @param settings the settings for which the element data should be loaded
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the callback for receiving the element data
     */
    void saveElementSettings(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        List<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsContainerElementData> callback);

    /**
     * Saves the favorite list.<p>
     *
     * @param clientIds favorite list element id's
     * @param uri the container page URI
     * @param callback the call-back executed on response
     */
    void saveFavoriteList(List<String> clientIds, String uri, AsyncCallback<Void> callback);

    /**
     * Saves a group-container element.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param groupContainer the group-container to save
     * @param containers the containers of the current page
     * @param locale the content locale
     * @param callback the call-back executed on response
     */
    void saveGroupContainer(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<CmsGroupContainerSaveResult> callback);

    /**
     * Saves an inheritance container.<p>
     *
     * @param pageStructureId the current page's structure id
     * @param detailContentId the detail content structure id
     * @param inheritanceContainer the inheritance container to save
     * @param containers the containers of the current page
     * @param locale the requested locale
     * @param callback the callback
     */
    void saveInheritanceContainer(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        CmsInheritanceContainer inheritanceContainer,
        Collection<CmsContainer> containers,
        String locale,
        AsyncCallback<Map<String, CmsContainerElementData>> callback);

    /**
     * Saves the recent list.<p>
     *
     * @param clientIds recent list element id's
     * @param uri the container page URI
     * @param callback the call-back executed on response
     */
    void saveRecentList(List<String> clientIds, String uri, AsyncCallback<Void> callback);

    /**
     * Saves the default value for small element editability on page load.<p>
     *
     * @param editSmallElements the default value
     *
     * @param callback the callback for the response
     */
    void setEditSmallElements(boolean editSmallElements, AsyncCallback<Void> callback);

    /**
     * Sets the element view.<p>
     *
     * @param elementView the element view
     * @param callback the call-back executed on response
     */
    void setElementView(CmsUUID elementView, AsyncCallback<Void> callback);

    /**
     * Stores information about the container page last edited.<p>
     *
     * @param pageId the page id
     * @param detailId the detail content id
     * @param callback the callback
     */
    void setLastPage(CmsUUID pageId, CmsUUID detailId, AsyncCallback<Void> callback);

}
