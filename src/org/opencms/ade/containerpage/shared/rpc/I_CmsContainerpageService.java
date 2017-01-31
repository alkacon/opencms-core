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
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * The RPC service interface used by the container-page editor.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsContainerpageService extends RemoteService {

    /**
     * Adds an element specified by it's id to the favorite list.<p>
     *
     * @param context the rpc context
     * @param clientId the element id
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToFavoriteList(CmsContainerPageRpcContext context, String clientId) throws CmsRpcException;

    /**
     * Adds an element specified by it's id to the recent list.<p>
     *
     * @param context the rpc context
     * @param clientId the element id
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void addToRecentList(CmsContainerPageRpcContext context, String clientId) throws CmsRpcException;

    /**
     * Check if a page or its elements have been changed.<p>
     *
     * @param structureId the structure id of the resource
     * @param detailContentId the structure id of the detail content (may be null)
     * @param contentLocale the content locale
     *
     * @return true if there were changes in the page or its elements
     *
     * @throws CmsRpcException if the RPC call fails
     */
    boolean checkContainerpageOrElementsChanged(CmsUUID structureId, CmsUUID detailContentId, String contentLocale)
    throws CmsRpcException;

    /**
     * To create a new element of the given type this method will check if a model resource needs to be selected, otherwise creates the new element.
     * Returns a bean containing either the new element data or a list of model resources to select.<p>
     *
     * @param pageStructureId the container page structure id
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param container the parent container
     * @param locale the content locale
     *
     * @return the bean containing either the new element data or a list of model resources to select
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsCreateElementData checkCreateNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsContainer container,
        String locale)
    throws CmsRpcException;

    /**
     * Checks whether the Acacia widgets are available for all fields of the content.<p>
     *
     * @param structureId the structure id of the content
     * @return true if Acacia widgets are available for all fields
     *
     * @throws CmsRpcException if something goes wrong
     */
    boolean checkNewWidgetsAvailable(CmsUUID structureId) throws CmsRpcException;

    /**
     * Creates  a new element with a given model element and returns the copy'S structure id.<p>
     *
     * @param pageId the container page id
     * @param originalElementId the model element id
     * @return the structure id of the copy
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsUUID copyElement(CmsUUID pageId, CmsUUID originalElementId) throws CmsRpcException;

    /**
     * Creates a new element of the given type and returns the new element data containing structure id and site path.<p>
     *
     * @param pageStructureId the container page structure id
     * @param clientId the client id of the new element (this will be the structure id of the configured new resource)
     * @param resourceType the resource tape of the new element
     * @param modelResourceStructureId the model resource structure id
     * @param locale the content locale
     *
     * @return the new element data containing structure id and site path
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElement createNewElement(
        CmsUUID pageStructureId,
        String clientId,
        String resourceType,
        CmsUUID modelResourceStructureId,
        String locale)
    throws CmsRpcException;

    /**
     * This method is used for serialization purposes only.<p>
     *
     * @return container info
     */
    CmsContainer getContainerInfo();

    /**
     * This method is used for serialization purposes only.<p>
     *
     * @return element info
     */
    CmsContainerElement getElementInfo();

    /**
     * Returns container element data by client id.<p>
     *
     * @param  context the rpc context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientIds the requested element id's
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param alwaysCopy <code>true</code> in case reading data for a clipboard element used as a copy group
     * @param dndSource the drag and drop source container (if we are getting the data for the drag and drop case)
     * @param locale the content locale
     *
     * @return the element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    Map<String, CmsContainerElementData> getElementsData(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        Collection<String> clientIds,
        Collection<CmsContainer> containers,
        boolean allowNested,
        boolean alwaysCopy,
        String dndSource,
        String locale)
    throws CmsRpcException;

    /**
     * Returns container element settings config data.<p>
     *
     * @param  context the rpc context
     * @param clientId the requested element id
     * @param containerId the parent container id
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     *
     * @return the element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElementData getElementSettingsConfig(
        CmsContainerPageRpcContext context,
        String clientId,
        String containerId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Gets the element data for an id and a map of settings.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientId the requested element ids
     * @param settings the settings for which the element data should be loaded
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     *
     * @return the element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElementData getElementWithSettings(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Returns the container element data of the favorite list.<p>
     *
     * @param pageStructureId the container page structure id
     * @param detailContentId the detail content structure id
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     *
     * @return the favorite list element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getFavoriteList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Returns the gallery configuration data according to the current page containers and the selected element view.<p>
     *
     * @param containers the page containers
     * @param elementView the element view
     * @param uri the page URI
     * @param locale the content locale
     *
     * @return the gallery data
     *
     * @throws CmsRpcException in case something goes wrong
     */
    CmsContainerPageGalleryData getGalleryDataForPage(
        List<CmsContainer> containers,
        CmsUUID elementView,
        String uri,
        String locale)
    throws CmsRpcException;

    /**
     * Returns new container element data for the given resource type name.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param resourceType the requested element resource type name
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     *
     * @return the element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElementData getNewElementData(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String resourceType,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Returns the container element data of the recent list.<p>
     *
     * @param pageStructureId the container page structure id
     * @param detailContentId the detail content structure id
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     *
     * @return the recent list element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    List<CmsContainerElementData> getRecentList(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        Collection<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Gets the status of a removed element.<p>
     *
     * @param id the client id of the removed element
     * @param containerpageId the id of the page which should be excluded from the relation check, or null if no page should be excluded
     *
     * @return the status of the removed element
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsRemovedElementStatus getRemovedElementStatus(String id, CmsUUID containerpageId) throws CmsRpcException;

    /**
     * Loads the index of the clipboard tab last selected by the user.<p>
     *
     * @return the clipboard tab index
     */
    int loadClipboardTab();

    /**
     * Returns the initialization data.<p>
     *
     * @return the initialization data
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsCntPageData prefetch() throws CmsRpcException;

    /**
     * Saves the index of the clipboard tab selected by the user.<p>
     *
     * @param tabIndex the index of the selected clipboard tab
     */
    void saveClipboardTab(int tabIndex);

    /**
     * Saves the container-page.<p>
     *
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers) throws CmsRpcException;

    /**
     * Saves the detail containers.<p>
     *
     * @param detailId the detail content id
     * @param detailContainerResource the detail container resource path
     * @param containers the container-page's containers
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveDetailContainers(CmsUUID detailId, String detailContainerResource, List<CmsContainer> containers)
    throws CmsRpcException;

    /**
     * Saves the settings for the given element to the container page and returns the updated element data.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param clientId the requested element ids
     * @param settings the settings for which the element data should be loaded
     * @param containers the containers of the current page
     * @param allowNested if nested containers are allowed
     * @param locale the content locale
     * @return the element data
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContainerElementData saveElementSettings(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        String clientId,
        Map<String, String> settings,
        List<CmsContainer> containers,
        boolean allowNested,
        String locale)
    throws CmsRpcException;

    /**
     * Saves the favorite list.<p>
     *
     * @param clientIds favorite list element id's
     * @param uri the container page URI
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveFavoriteList(List<String> clientIds, String uri) throws CmsRpcException;

    /**
     * Saves a group-container element.<p>
     *
     * @param context the RPC context
     * @param detailContentId the detail content structure id
     * @param reqParams optional request parameters
     * @param groupContainer the group-container to save
     * @param containers the containers of the current page
     * @param locale the content locale
     *
     * @return the data of the saved group container
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsGroupContainerSaveResult saveGroupContainer(
        CmsContainerPageRpcContext context,
        CmsUUID detailContentId,
        String reqParams,
        CmsGroupContainer groupContainer,
        Collection<CmsContainer> containers,
        String locale)
    throws CmsRpcException;

    /**
     * Saves an inheritance container.<p>
     *
     * @param pageStructureId the current page's structure id
     * @param detailContentId the detail content structure id
     * @param inheritanceContainer the inheritance container to save
     * @param containers the containers of the current page
     * @param locale the requested locale
     *
     * @return the element data of the saved container
     *
     * @throws CmsRpcException if something goes wrong
     */
    Map<String, CmsContainerElementData> saveInheritanceContainer(
        CmsUUID pageStructureId,
        CmsUUID detailContentId,
        CmsInheritanceContainer inheritanceContainer,
        Collection<CmsContainer> containers,
        String locale)
    throws CmsRpcException;

    /**
     * Saves the recent list.<p>
     *
     * @param clientIds recent list element id's
     * @param uri the container page URI
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void saveRecentList(List<String> clientIds, String uri) throws CmsRpcException;

    /**
     * Enables or disables editing for small elements on page load.<p>
     *
     * @param editSmallElements the defautl setting for the small element editability
     *
     * @throws CmsRpcException if something goes wrong
     */
    void setEditSmallElements(boolean editSmallElements) throws CmsRpcException;

    /**
     * Sets the element view.<p>
     *
     * @param elementView the element view
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void setElementView(CmsUUID elementView) throws CmsRpcException;

    /**
     * Stores information about the container page last edited.<p>
     *
     * @param pageId the page id
     * @param detailId the detail content id
     *
     * @throws CmsRpcException if something goes wrong
     */
    void setLastPage(CmsUUID pageId, CmsUUID detailId) throws CmsRpcException;

    /**
     * Saves the container-page in a synchronized RPC call.<p>
     *
     * @param pageStructureId the container page structure id
     * @param containers the container-page's containers
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void syncSaveContainerpage(CmsUUID pageStructureId, List<CmsContainer> containers) throws CmsRpcException;

    /**
     * Saves the detail containers.<p>
     *
     * @param detailId the detail content id
     * @param detailContainerResource the detail container resource path
     * @param containers the container-page's containers
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    void syncSaveDetailContainers(CmsUUID detailId, String detailContainerResource, List<CmsContainer> containers)
    throws CmsRpcException;

}
