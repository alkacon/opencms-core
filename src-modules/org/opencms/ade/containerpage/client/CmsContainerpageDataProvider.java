/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageDataProvider.java,v $
 * Date   : $Date: 2010/04/14 14:33:47 $
 * Version: $Revision: 1.5 $
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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetContainer;
import org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

/**
 * Data provider for the container-page editor. All data concerning the container-page is requested and maintained by this provider.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public final class CmsContainerpageDataProvider {

    /**
     * A RPC action implementation used to request the data for container-page elements.<p>
     */
    private class MultiElementAction extends CmsRpcAction<Map<String, CmsContainerElement>> {

        /** Call-back executed on response. */
        private I_CmsSimpleCallback<Map<String, CmsContainerElement>> m_callback;

        /** The requested client id's. */
        private Set<String> m_clientIds;

        /**
         * Constructor.<p>
         * 
         * @param clientIds the client id's
         * @param callback the call-back
         */
        public MultiElementAction(Set<String> clientIds, I_CmsSimpleCallback<Map<String, CmsContainerElement>> callback) {

            super();
            m_clientIds = clientIds;
            m_callback = callback;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            Map<String, CmsContainerElement> result = new HashMap<String, CmsContainerElement>();
            List<String> neededIds = new ArrayList<String>();
            Iterator<String> it = m_clientIds.iterator();
            while (it.hasNext()) {
                String clientId = it.next();
                if (m_elements.containsKey(clientId)) {
                    result.put(clientId, m_elements.get(clientId));
                } else {
                    neededIds.add(clientId);
                }
            }
            if (neededIds.size() == 0) {
                m_callback.execute(result);
            } else {
                getContainerpageService().getElementsData(
                    CmsContainerpageDataProvider.getCurrentUri(),
                    null,
                    m_clientIds,
                    m_containerTypes,
                    this);
            }

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElement> result) {

            if (result != null) {
                addElements(result);
                Map<String, CmsContainerElement> elements = new HashMap<String, CmsContainerElement>();
                Iterator<String> it = m_clientIds.iterator();
                while (it.hasNext()) {
                    String clientId = it.next();
                    elements.put(clientId, m_elements.get(clientId));
                }
                m_callback.execute(elements);
                return;
            }
            m_callback.onError("An error occurred while retrieving elements.");

        }

    }

    /**
     * A RPC action implementation used to reload the data for a container-page element.<p>
     */
    private class ReloadElementAction extends CmsRpcAction<Map<String, CmsContainerElement>> {

        /** The requested client id's. */
        private Set<String> m_clientIds;

        public ReloadElementAction(Set<String> clientIds) {

            super();
            m_clientIds = clientIds;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            getContainerpageService().getElementsData(
                CmsContainerpageDataProvider.getCurrentUri(),
                null,
                m_clientIds,
                m_containerTypes,
                this);

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElement> result) {

            if (result != null) {
                addElements(result);
                Iterator<CmsDragContainerElement> it = getAllDragElements().iterator();
                while (it.hasNext()) {
                    CmsDragContainerElement dragElement = it.next();
                    if (m_clientIds.contains(dragElement.getClientId())) {
                        try {
                            replaceDragElement(dragElement, m_elements.get(dragElement.getClientId()));
                        } catch (Exception e) {
                            CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                        }
                    }
                }
            }

        }
    }

    /**
     * A RPC action implementation used to request the data for a single container-page element.<p>
     */
    private class SingleElementAction extends CmsRpcAction<Map<String, CmsContainerElement>> {

        /** Call-back executed on response. */
        private I_CmsSimpleCallback<CmsContainerElement> m_callback;

        /** The requested client id. */
        private String m_clientId;

        /**
         * Constructor.<p>
         * 
         * @param clientId the client id
         * @param callback the call-back
         */
        public SingleElementAction(String clientId, I_CmsSimpleCallback<CmsContainerElement> callback) {

            super();
            m_clientId = clientId;
            m_callback = callback;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            if (m_elements.containsKey(m_clientId)) {
                m_callback.execute(m_elements.get(m_clientId));
            } else {
                List<String> clientIds = new ArrayList<String>();
                clientIds.add(m_clientId);
                getContainerpageService().getElementsData(
                    CmsContainerpageDataProvider.getCurrentUri(),
                    null,
                    clientIds,
                    m_containerTypes,
                    this);
            }

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElement> result) {

            if (result != null) {
                addElements(result);
                m_callback.execute(m_elements.get(m_clientId));
            }
            m_callback.onError("An error occurred while retrieving element with id: '" + m_clientId + "'.");

        }
    }

    /** Instance of the data provider. */
    private static CmsContainerpageDataProvider INSTANCE;

    /** The container types within this page. */
    /*DEFAULT*/Set<String> m_containerTypes;

    /** The container element data. All requested elements will be cached here.*/
    /*DEFAULT*/Map<String, CmsContainerElement> m_elements;

    /** The container-page RPC service. */
    private I_CmsContainerpageServiceAsync m_containerpageService;

    /** The container-page util instance. */
    private CmsContainerpageUtil m_containerpageUtil;

    /** The container data. */
    private Map<String, CmsContainerJso> m_containers;

    /** Flag if the container-page has changed. */
    private boolean m_pageChanged;

    /** The drag targets within this page. */
    private Map<String, CmsDragTargetContainer> m_targetContainers;

    /**
     * Hidden constructor.<p>
     */
    private CmsContainerpageDataProvider(List<I_CmsContainerpageToolbarButton> toolbarButtons) {

        m_elements = new HashMap<String, CmsContainerElement>();
        m_containerTypes = new HashSet<String>();
        m_containers = new HashMap<String, CmsContainerJso>();
        m_containerpageUtil = new CmsContainerpageUtil(toolbarButtons);

        JsArray<CmsContainerJso> containers = CmsContainerJso.getContainers();
        for (int i = 0; i < containers.length(); i++) {
            CmsContainerJso container = containers.get(i);
            m_containerTypes.add(container.getType());
            m_containers.put(container.getName(), container);
        }
        m_targetContainers = m_containerpageUtil.consumeContainers(m_containers);
    }

    /**
     * Returns the data provider instance.<p>
     * 
     * @return the data provider
     */
    public static CmsContainerpageDataProvider get() {

        if (INSTANCE == null) {
            CmsDebugLog.getInstance().printLine("WARNING: The data provider has not been initialized!");
            return null;
        }

        return INSTANCE;
    }

    /**
     * Returns the current URI.<p>
     * 
     * @return the current URI
     */
    public static String getCurrentUri() {

        return CmsCoreProvider.get().getUri();

    }

    /**
     * Initializes the data provider. This will also transform the pages containers and elements into the appropriate widgets (done within the private constructor).<p>
     *  
     * @param toolbarButtons the tool-bar buttons of the container-page editor
     */
    public static void init(List<I_CmsContainerpageToolbarButton> toolbarButtons) {

        if (INSTANCE == null) {
            INSTANCE = new CmsContainerpageDataProvider(toolbarButtons);
        }
    }

    /**
     * Adds an element specified by it's id to the favorite list.<p>
     * 
     * @param clientId the element id
     */
    public void addToFavoriteList(final String clientId) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().addToFavoriteList(clientId, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                CmsDebugLog.getInstance().printLine("Added " + clientId + " to favorite list");

            }
        };
        action.execute();
    }

    /**
     * Adds an element specified by it's id to the recent list.<p>
     * 
     * @param clientId the element id
     */
    public void addToRecentList(final String clientId) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().addToRecentList(clientId, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                CmsDebugLog.getInstance().printLine("Added " + clientId + " to recent list");

            }
        };
        action.execute();
    }

    /**
     * Returns all drag elements of the page.<p>
     * 
     * @return the drag elements
     */
    public List<CmsDragContainerElement> getAllDragElements() {

        List<CmsDragContainerElement> result = new ArrayList<CmsDragContainerElement>();
        Iterator<CmsDragTargetContainer> it = m_targetContainers.values().iterator();
        while (it.hasNext()) {
            result.addAll(it.next().getAllDragElements());
        }
        return result;
    }

    /**
     * Returns the container data of container with the given name.
     * 
     * @param containerName the container name
     * 
     * @return the container data
     */
    public CmsContainerJso getContainer(String containerName) {

        return m_containers.get(containerName);
    }

    /**
     * Returns the container-page RPC service.<p>
     * 
     * @return the container-page service
     */
    public I_CmsContainerpageServiceAsync getContainerpageService() {

        if (m_containerpageService == null) {
            m_containerpageService = GWT.create(I_CmsContainerpageService.class);
        }
        return m_containerpageService;
    }

    /**
     * Returns the {@link org.opencms.ade.containerpage.client.CmsContainerpageUtil}.<p>
     *
     * @return the containerpage-util
     */
    public CmsContainerpageUtil getContainerpageUtil() {

        return m_containerpageUtil;
    }

    /**
     * Returns the containers.<p>
     *
     * @return the containers
     */
    public Map<String, CmsContainerJso> getContainers() {

        return m_containers;
    }

    /**
     * Returns the container drag target by name (HTML id attribute).<p>
     * 
     * @param containerName the container name
     * @return the drag target
     */
    public CmsDragTargetContainer getContainerTarget(String containerName) {

        return m_targetContainers.get(containerName);
    }

    /**
     * Returns a map of the container drag targets.<p>
     * 
     * @return the drag targets
     */
    public Map<String, CmsDragTargetContainer> getContainerTargets() {

        return m_targetContainers;
    }

    /**
     * Returns the type of container with the given name.<p>
     * 
     * @param containerName the container name
     * 
     * @return the container type
     */
    public String getContainerType(String containerName) {

        return getContainer(containerName).getType();
    }

    /**
     * Requests the data for a container element specified by the client id. The data will be provided to the given call-back function.<p>
     * 
     * @param clientId the element id
     * @param callback the call-back to execute with the requested data
     */
    public void getElement(String clientId, I_CmsSimpleCallback<CmsContainerElement> callback) {

        if (m_elements.containsKey(clientId)) {
            callback.execute(m_elements.get(clientId));
        } else {
            SingleElementAction action = new SingleElementAction(clientId, callback);
            action.execute();
        }
    }

    /**
     * Requests the data for container elements specified by the client id. The data will be provided to the given call-back function.<p>
     * 
     * @param clientIds the element id's
     * @param callback the call-back to execute with the requested data
     */
    public void getElements(Set<String> clientIds, I_CmsSimpleCallback<Map<String, CmsContainerElement>> callback) {

        MultiElementAction action = new MultiElementAction(clientIds, callback);
        action.execute();
    }

    /**
     * Returns if the page has changed.<p>
     * 
     * @return <code>true</code> if the page has changed
     */
    public boolean hasPageChanged() {

        return m_pageChanged;
    }

    /**
     * Loads the favorite list and adds the elements to the favorite list widget of the tool-bar menu.<p>
     */
    public void loadFavorites() {

        CmsRpcAction<LinkedHashMap<String, CmsContainerElement>> action = new CmsRpcAction<LinkedHashMap<String, CmsContainerElement>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().getFavoriteList(getCurrentUri(), m_containerTypes, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(LinkedHashMap<String, CmsContainerElement> result) {

                getContainerpageUtil().getClipboard().clearFavorites();
                if (result != null) {
                    Iterator<CmsContainerElement> it = result.values().iterator();
                    while (it.hasNext()) {
                        getContainerpageUtil().getClipboard().addToFavorites(it.next());
                    }
                }

            }
        };
        action.execute();
    }

    /**
     * Loads the recent list and adds the elements to the recent list widget of the tool-bar menu.<p>
     */
    public void loadRecent() {

        CmsRpcAction<LinkedHashMap<String, CmsContainerElement>> action = new CmsRpcAction<LinkedHashMap<String, CmsContainerElement>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().getRecentList(getCurrentUri(), m_containerTypes, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(LinkedHashMap<String, CmsContainerElement> result) {

                getContainerpageUtil().getClipboard().clearRecent();
                if (result != null) {
                    Iterator<CmsContainerElement> it = result.values().iterator();
                    while (it.hasNext()) {
                        getContainerpageUtil().getClipboard().addToRecent(it.next());
                    }
                }

            }
        };
        action.execute();
    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     * 
     * Call this if the element content has changed.<p>
     * 
     * @param id the element id
     */
    public void reloadElement(String id) {

        Set<String> related = getRelatedElementIds(id);
        ReloadElementAction action = new ReloadElementAction(related);
        action.execute();

    }

    /**
     * Removes the given container element from its parent container.<p>
     * 
     * @param dragElement the element to remove
     */
    public void removeElement(CmsDragContainerElement dragElement) {

        dragElement.removeFromParent();
        setPageChanged();
    }

    /**
     * Replaces the given drag-element with the given container element.<p>
     * 
     * @param dragElement the drag-element to replace
     * @param elementData the new element data
     * 
     * @throws Exception if something goes wrong
     */
    public void replaceDragElement(CmsDragContainerElement dragElement, CmsContainerElement elementData)
    throws Exception {

        CmsDragTargetContainer dragParent = (CmsDragTargetContainer)dragElement.getDragParent();
        String containerType = m_containers.get(dragParent.getContainerId()).getType();

        String elementContent = elementData.getContents().get(containerType);
        if ((elementContent != null) && (elementContent.trim().length() > 0)) {
            CmsDragContainerElement replacer = getContainerpageUtil().createElement(
                elementData,
                dragParent,
                containerType);

            dragParent.insert(replacer, dragParent.getWidgetIndex(dragElement));
            dragElement.removeFromParent();
        }
    }

    /**
     * Sets the page changed flag to <code>true</code>.<p>
     */
    public void setPageChanged() {

        CmsDebugLog.getInstance().printLine("PAGE CHANGED");
        m_pageChanged = true;
    }

    /** 
     * Adds the given element data to the element cache.<p>
     * 
     * @param elements the element data
     */
    /*DEFAULT*/void addElements(Map<String, CmsContainerElement> elements) {

        m_elements.putAll(elements);
    }

    /**
     * Returns all element id's related to the given one.<p>
     * 
     * @param id the element id
     * @return the related id's
     */
    private Set<String> getRelatedElementIds(String id) {

        Set<String> result = new HashSet<String>();
        result.add(id);
        String serverId = id;
        if (id.contains("#")) {
            serverId = id.substring(0, id.indexOf("#"));
        }
        Iterator<String> it = m_elements.keySet().iterator();
        while (it.hasNext()) {
            String elId = it.next();
            if (elId.startsWith(serverId)) {
                result.add(elId);
            }
        }

        Iterator<CmsDragContainerElement> itEl = getAllDragElements().iterator();
        while (itEl.hasNext()) {
            CmsDragContainerElement element = itEl.next();
            if (element.getClientId().startsWith(serverId)) {
                result.add(element.getClientId());
            }
        }
        return result;
    }
}
