/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageDataProvider.java,v $
 * Date   : $Date: 2010/04/06 09:49:44 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * @version $Revision: 1.2 $
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
        private List<String> m_clientIds;

        /**
         * Constructor.<p>
         * 
         * @param clientIds the client id's
         * @param callback the call-back
         */
        public MultiElementAction(List<String> clientIds, I_CmsSimpleCallback<Map<String, CmsContainerElement>> callback) {

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

    /** Key to access the current URI within the window object. */
    @SuppressWarnings("unused")
    private static final String KEY_CURRENT_URI = "cms_ade_current_uri";

    /** The current URI. */
    private static String m_currentUri;

    /** The container types within this page. */
    /*DEFAULT*/Set<String> m_containerTypes;

    /** The container element data. All requested elements will be cached here.*/
    /*DEFAULT*/Map<String, CmsContainerElement> m_elements;

    /** The container-page RPC service. */
    private I_CmsContainerpageServiceAsync m_containerpageService;

    /** The container data. */
    private Map<String, CmsContainerJso> m_containers;

    /** The drag targets within this page. */
    private Map<String, CmsDragTargetContainer> m_targetContainers;

    /**
     * Hidden constructor.<p>
     */
    private CmsContainerpageDataProvider() {

        m_elements = new HashMap<String, CmsContainerElement>();
        m_containerTypes = new HashSet<String>();
        m_containers = new HashMap<String, CmsContainerJso>();
        m_targetContainers = new HashMap<String, CmsDragTargetContainer>();

        JsArray<CmsContainerJso> containers = CmsContainerJso.getContainers();
        List<CmsDragContainerElement> elements = new ArrayList<CmsDragContainerElement>();
        for (int i = 0; i < containers.length(); i++) {
            CmsContainerJso container = containers.get(i);
            m_containerTypes.add(container.getType());
            m_containers.put(container.getName(), container);
            CmsDragTargetContainer target = new CmsDragTargetContainer(container);
            elements.addAll(target.consumeChildren());
            m_targetContainers.put(container.getName(), target);
        }
        Iterator<CmsDragContainerElement> it = elements.iterator();
        while (it.hasNext()) {
            CmsDragContainerElement element = it.next();
            CmsElementOptionBar optionBar = CmsElementOptionBar.createOptionBarForElement(
                element,
                CmsContainerpageEditor.INSTANCE.getToolbarButtons());
            element.setElementOptionBar(optionBar);
        }
    }

    /**
     * Returns the data provider instance.<p>
     * 
     * @return the data provider
     */
    public static CmsContainerpageDataProvider get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContainerpageDataProvider();
        }

        return INSTANCE;
    }

    /**
     * Returns the current URI.<p>
     * 
     * @return the current URI
     */
    public static String getCurrentUri() {

        if (m_currentUri == null) {
            m_currentUri = getCurrentUriNative();
        }
        return m_currentUri;
    }

    /**
     * Initialises the data provider. This will also transform the pages containers and elements into the appropriate widgets (done within the private constructor).<p> 
     */
    public static void init() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContainerpageDataProvider();
        }
    }

    /**
     * Accesses the window object to read the current URI.<p>
     * 
     * @return the current URI
     */
    private static native String getCurrentUriNative() /*-{
        return $wnd[@org.opencms.ade.containerpage.client.CmsContainerpageDataProvider::KEY_CURRENT_URI];
    }-*/;

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
    public void getElements(List<String> clientIds, I_CmsSimpleCallback<Map<String, CmsContainerElement>> callback) {

        MultiElementAction action = new MultiElementAction(clientIds, callback);
        action.execute();
    }

    /** 
     * Adds the given element data to the element cache.<p>
     * 
     * @param elements the element data
     */
    /*DEFAULT*/void addElements(Map<String, CmsContainerElement> elements) {

        m_elements.putAll(elements);
    }
}
