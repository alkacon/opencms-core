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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.ui.CmsConfirmRemoveDialog;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsRemovedElementDeletionDialog;
import org.opencms.ade.containerpage.client.ui.CmsSmallElementsHandler;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsGroupContainerEditor;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsInheritanceContainerEditor;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageServiceAsync;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.CmsCompositeDNDController;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.util.CmsAsyncJoinHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;
import org.opencms.util.CmsDefaultSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Data provider for the container-page editor. All data concerning the container-page is requested and maintained by this provider.<p>
 * 
 * @since 8.0.0
 */
public final class CmsContainerpageController {

    /** 
     * Enum which is used to control how elements are removed from the page.<p>
     */
    public enum ElementRemoveMode {
        /** Reference checks are performed and the user is asked for confirmation whether they really want to remove the element before the page is saved. */
        confirmRemove,

        /** Reference checks are only performed after the page or group has been saved. */
        saveAndCheckReferences,

        /** Element is just removed, no checks are performed. */
        silent;
    }

    /**
     * Visitor interface used to process the current container content on the page.<p>
     */
    public static interface I_PageContentVisitor {

        /** 
         * This method is called before a container is processed.<p>
         * 
         * If the method returns false, the container will be skipped.<p>
         * 
         * @param name the container name 
         * @param container the container data object
         *  
         * @return true if the container should be processed, true if it should be skipped 
         */
        boolean beginContainer(String name, CmsContainerJso container);

        /**
         * This method is called after all elements of a container have been processed.<p>
         */
        void endContainer();

        /** 
         * This method is called for each element of a container.<p>
         * 
         * @param element the container element 
         */
        void handleElement(CmsContainerPageElementPanel element);
    }

    /**
     * This visitor implementation checks whether there are other elements in the current page
     * which correspond to the same VFS resource as a given container element.
     */
    public static class ReferenceCheckVisitor implements I_PageContentVisitor {

        /** The element for which we want to check whether there are other references to the same resource. */
        private CmsContainerPageElementPanel m_elementPanel;

        /** True if other references have been found. */
        private boolean m_hasReferences;

        /** The structure id of the element. */
        private String m_structureId;

        /**
         * Creates a new instance.<p>
         * 
         * @param elementPanel the element for which we want to check if there are other references 
         */
        public ReferenceCheckVisitor(CmsContainerPageElementPanel elementPanel) {

            m_elementPanel = elementPanel;
            m_structureId = getServerId(elementPanel.getId());
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#beginContainer(java.lang.String, org.opencms.ade.containerpage.client.CmsContainerJso)
         */
        public boolean beginContainer(String name, CmsContainerJso container) {

            return !container.isDetailView();
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#endContainer()
         */
        public void endContainer() {

            // do nothing 
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#handleElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
         */
        public void handleElement(CmsContainerPageElementPanel element) {

            if (element != m_elementPanel) {
                String id = getServerId(element.getId());
                if (m_structureId.equals(id)) {
                    m_hasReferences = true;
                }
            }
        }

        /**
         * Checks if other references have been found.<p>
         * 
         * @return true if other references have been found 
         */
        public boolean hasReferences() {

            return m_hasReferences;
        }

    }

    /** 
     * Visitor implementation which is used to gather the container contents for saving.<p>
     */
    protected class SaveDataVisitor implements I_PageContentVisitor {

        /** The current container name. */
        protected String m_containerName;

        /** The contaienr which is currently being processed. */
        protected CmsContainerJso m_currentContainer;

        /** The list of collected containers. */
        protected List<CmsContainer> m_resultContainers = new ArrayList<CmsContainer>();

        /** The list of elements of the currently processed container which have already been processed. */
        List<CmsContainerElement> m_currentElements;

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#beginContainer(java.lang.String, org.opencms.ade.containerpage.client.CmsContainerJso)
         */
        public boolean beginContainer(String name, CmsContainerJso container) {

            if (container.isDetailView()) {
                m_currentContainer = null;
                return false;

            } else {
                m_currentContainer = container;
                m_containerName = name;
                m_currentElements = new ArrayList<CmsContainerElement>();
                return true;
            }
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#endContainer()
         */
        public void endContainer() {

            m_resultContainers.add(new CmsContainer(
                m_containerName,
                m_currentContainer.getType(),
                m_currentContainer.getWidth(),
                m_currentContainer.getMaxElements(),
                m_currentElements));
        }

        /**
         * Gets the list of collected containers.<p>
         * 
         * @return the list of containers 
         */
        public List<CmsContainer> getContainers() {

            return m_resultContainers;
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#handleElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
         */
        public void handleElement(CmsContainerPageElementPanel elementWidget) {

            CmsContainerElement element = new CmsContainerElement();
            element.setClientId(elementWidget.getId());
            element.setResourceType(elementWidget.getNewType());
            element.setNew(elementWidget.isNew());
            element.setSitePath(elementWidget.getSitePath());
            element.setNewEditorDisabled(elementWidget.isNewEditorDisabled());
            m_currentElements.add(element);
        }

    }

    /**
     * A type which indicates the locking status of the currently edited container page.<p>
     */
    enum LockStatus {
        /** Locking the resource failed. */
        failed,

        /** The resource could be successfully locked. */
        locked,

        /** Locking the resource has not been tried. */
        unknown
    }

    /**
     * A RPC action implementation used to request the data for container-page elements.<p>
     */
    private class MultiElementAction extends CmsRpcAction<Map<String, CmsContainerElementData>> {

        /** Call-back executed on response. */
        private I_CmsSimpleCallback<Map<String, CmsContainerElementData>> m_callback;

        /** The requested client id's. */
        private Set<String> m_clientIds;

        /**
        "         * Constructor.<p>
         * 
         * @param clientIds the client id's
         * @param callback the call-back
         */
        public MultiElementAction(
            Set<String> clientIds,
            I_CmsSimpleCallback<Map<String, CmsContainerElementData>> callback) {

            super();
            m_clientIds = clientIds;
            m_callback = callback;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            Map<String, CmsContainerElementData> result = new HashMap<String, CmsContainerElementData>();
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
                    CmsCoreProvider.get().getStructureId(),
                    getRequestParams(),
                    m_clientIds,
                    m_containerBeans,
                    getLocale(),

                    this);
            }

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElementData> result) {

            if (result != null) {
                addElements(result);
                Map<String, CmsContainerElementData> elements = new HashMap<String, CmsContainerElementData>();
                Iterator<String> it = m_clientIds.iterator();
                while (it.hasNext()) {
                    String clientId = it.next();
                    elements.put(clientId, m_elements.get(clientId));
                }
                m_callback.execute(elements);
            }
        }

    }

    /**
     * A RPC action implementation used to reload the data for a container-page element.<p>
     */
    private class ReloadElementAction extends CmsRpcAction<Map<String, CmsContainerElementData>> {

        /** The requested client id's. */
        private Set<String> m_clientIds;

        /**
         * Constructor.<p>
         * 
         * @param clientIds the client id's to reload
         */
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
                CmsCoreProvider.get().getStructureId(),
                getRequestParams(),
                m_clientIds,
                m_containerBeans,
                getLocale(),

                this);

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElementData> result) {

            if (result == null) {
                return;
            }
            addElements(result);
            Iterator<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel> it = getAllDragElements().iterator();
            while (it.hasNext()) {
                org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel containerElement = it.next();
                if (!m_clientIds.contains(containerElement.getId())) {
                    continue;
                }
                try {
                    replaceContainerElement(containerElement, m_elements.get(containerElement.getId()));
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine("trying to replace");
                    CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                }

            }
            if (isGroupcontainerEditing()) {
                getGroupEditor().updateBackupElements(result);
                getGroupcontainer().refreshHighlighting();
            }
            m_handler.updateClipboard(result);
            resetEditableListButtons();
        }
    }

    /**
     * A RPC action implementation used to request the data for a single container-page element.<p>
     */
    private class SingleElementAction extends CmsRpcAction<Map<String, CmsContainerElementData>> {

        /** Call-back executed on response. */
        private I_CmsSimpleCallback<CmsContainerElementData> m_callback;

        /** The requested client id. */
        private String m_clientId;

        /**
         * Constructor.<p>
         * 
         * @param clientId the client id
         * @param callback the call-back
         */
        public SingleElementAction(String clientId, I_CmsSimpleCallback<CmsContainerElementData> callback) {

            super();
            m_clientId = clientId;
            m_callback = callback;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            boolean cached = false;
            if (m_elements.containsKey(m_clientId)) {
                cached = true;
                CmsContainerElementData elementData = m_elements.get(m_clientId);
                if (elementData.isGroupContainer() || elementData.isInheritContainer()) {
                    for (String subItemId : elementData.getSubItems()) {
                        if (!m_elements.containsKey(subItemId)) {
                            cached = false;
                            break;
                        }
                    }
                }
            }
            if (cached) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    /**
                     * @see com.google.gwt.user.client.Command#execute()
                     */
                    public void execute() {

                        getCallback().execute(m_elements.get(getClientId()));

                    }
                });
            } else {
                List<String> clientIds = new ArrayList<String>();
                clientIds.add(m_clientId);
                getContainerpageService().getElementsData(
                    CmsCoreProvider.get().getStructureId(),
                    getRequestParams(),
                    clientIds,
                    m_containerBeans,
                    getLocale(),

                    this);
            }

        }

        /**
         * Returns the call-back function.<p>
         * 
         * @return the call-back function
         */
        protected I_CmsSimpleCallback<CmsContainerElementData> getCallback() {

            return m_callback;
        }

        /** 
         * Returns the requested elements id.<p>
         * 
         * @return the element client id
         */
        protected String getClientId() {

            return m_clientId;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElementData> result) {

            if (result != null) {
                addElements(result);
                m_callback.execute(m_elements.get(m_clientId));
            }
        }
    }

    /** The client side id/setting-hash seperator. */
    public static final String CLIENT_ID_SEPERATOR = "#";

    /** Parameter name. */
    public static final String PARAM_REMOVEMODE = "removemode";

    /** Instance of the data provider. */
    private static CmsContainerpageController INSTANCE;

    /** The list of beans for the containers on the current page. */
    protected List<CmsContainer> m_containerBeans;

    /** The container element data. All requested elements will be cached here.*/
    protected Map<String, CmsContainerElementData> m_elements;

    /** The new element data by resource type name. */
    protected Map<String, CmsContainerElementData> m_newElements;

    /** The container-page handler. */
    CmsContainerpageHandler m_handler;

    /** The container page drag and drop controller. */
    private I_CmsDNDController m_cntDndController;

    /** The container-page RPC service. */
    private I_CmsContainerpageServiceAsync m_containerpageService;

    /** The container-page util instance. */
    private CmsContainerpageUtil m_containerpageUtil;

    /** The container data. */
    private Map<String, CmsContainerJso> m_containers;

    /** The container types within this page. */
    private Set<String> m_containerTypes;

    /** The XML content editor handler. */
    private CmsContentEditorHandler m_contentEditorHandler;

    /** The core RPC service instance. */
    private I_CmsCoreServiceAsync m_coreSvc;

    /** The prefetched data. */
    private CmsCntPageData m_data;

    /** The DND controller. */
    private CmsCompositeDNDController m_dndController;

    /** The drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /** The currently editing group-container editor. */
    private A_CmsGroupEditor m_groupEditor;

    /** Flag indicating that a content element is being edited. */
    private boolean m_isContentEditing;

    /** The lock error message. */
    private String m_lockErrorMessage;

    /** The current lock status for the page. */
    private LockStatus m_lockStatus = LockStatus.unknown;

    /** Flag if the container-page has changed. */
    private boolean m_pageChanged;

    /** Handler for small elements. */
    private CmsSmallElementsHandler m_smallElementsHandler;

    /** The drag targets within this page. */
    private Map<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> m_targetContainers;

    /**
     * Constructor.<p>
     */
    public CmsContainerpageController() {

        INSTANCE = this;
        try {
            m_data = (CmsCntPageData)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getContainerpageService(),
                CmsCntPageData.DICT_NAME);
        } catch (SerializationException e) {
            CmsErrorDialog.handleException(new Exception(
                "Deserialization of page data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                e));
        }
        m_smallElementsHandler = new CmsSmallElementsHandler(getContainerpageService());
        m_smallElementsHandler.setEditSmallElements(m_data.isEditSmallElementsInitially(), false);
    }

    /**
     * Returns the data provider instance.<p>
     * 
     * @return the data provider
     */
    public static CmsContainerpageController get() {

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
     * Returns the server id for a given client element id.<p>
     * 
     * @param clientId the client id including an optional element settings hash
     * 
     * @return the server id
     */
    public static String getServerId(String clientId) {

        String serverId = clientId;
        if (clientId.contains(CLIENT_ID_SEPERATOR)) {
            serverId = clientId.substring(0, clientId.indexOf(CLIENT_ID_SEPERATOR));
        }
        return serverId;
    }

    /** 
     * Checks whether element removal should be confirmed.<p>
     * 
     * @return true if element removal should be confirmed 
     */
    public static boolean isConfirmRemove() {

        Map<String, String> params = CmsCoreProvider.get().getAdeParameters();
        String removeMode = params.get(PARAM_REMOVEMODE);
        return (removeMode == null) || removeMode.equals("confirm");
    }

    /**
     * Asks the user whether an element which has been removed should be deleted.<p>
     * 
     * @param status the status of the removed element 
     */
    protected static void askWhetherRemovedElementShouldBeDeleted(final CmsRemovedElementStatus status) {

        CmsRemovedElementDeletionDialog dialog = new CmsRemovedElementDeletionDialog(status);
        dialog.center();
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

                CmsNotification.get().send(
                    Type.NORMAL,
                    Messages.get().key(Messages.GUI_NOTIFICATION_ADD_TO_FAVORITES_0));
            }
        };
        action.execute();
    }

    /**
     * Adds an element specified by it's id to the recent list.<p>
     * 
     * @param clientId the element id
     * @param nextAction the action to execute after the element has been added 
     */
    public void addToRecentList(final String clientId, final Runnable nextAction) {

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

                if (nextAction != null) {
                    nextAction.run();
                }
            }
        };
        action.execute();
    }

    /**
     * Checks whether GWT widgets are available for all fields of a content.<p>
     * 
     * @param structureId the structure id of the content 
     * @param resultCallback the callback for the result 
     */
    public void checkNewWidgetsAvailable(final CmsUUID structureId, final AsyncCallback<Boolean> resultCallback) {

        CmsRpcAction<Boolean> action = new CmsRpcAction<Boolean>() {

            @Override
            public void execute() {

                start(200, false);
                getContainerpageService().checkNewWidgetsAvailable(structureId, this);
            }

            @Override
            protected void onResponse(Boolean result) {

                stop(false);
                resultCallback.onSuccess(result);
            }

            // empty
        };
        action.execute();

    }

    /**
     * Creates a new resource for crag container elements with the status new and opens the content editor.<p>
     * 
     * @param element the container element
     * @param inline <code>true</code> to open the inline editor for the given element if available
     */
    public void createAndEditNewElement(
        final org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel element,
        final boolean inline) {

        if (!element.isNew()) {
            return;
        }
        m_handler.showPageOverlay();
        CmsRpcAction<CmsCreateElementData> action = new CmsRpcAction<CmsCreateElementData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().checkCreateNewElement(
                    CmsCoreProvider.get().getStructureId(),
                    element.getId(),
                    element.getNewType(),
                    getLocale(),
                    this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsCreateElementData result) {

                if (result.needsModelSelection()) {
                    getHandler().openModelResourceSelect(element, result.getModelResources());
                } else {
                    openEditorForNewElement(element, result.getCreatedElement(), inline);
                }
            }
        };
        action.execute();
    }

    /**
     * Creates a new resource for crag container elements with the status new and opens the content editor.<p>
     * 
     * @param element the container element
     * @param modelResourceStructureId the model resource structure id
     */
    public void createAndEditNewElement(
        final org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel element,
        final CmsUUID modelResourceStructureId) {

        CmsRpcAction<CmsContainerElement> action = new CmsRpcAction<CmsContainerElement>() {

            @Override
            public void execute() {

                getContainerpageService().createNewElement(
                    CmsCoreProvider.get().getStructureId(),
                    element.getId(),
                    element.getNewType(),
                    modelResourceStructureId,
                    getLocale(),
                    this);

            }

            @Override
            protected void onResponse(CmsContainerElement result) {

                openEditorForNewElement(element, result, false);

            }
        };
        action.execute();
    }

    /**
     * Deletes an element from the VFS, removes it from all containers and the client side cache.<p>
     * 
     * @param elementId the element to delete
     * @param relatedElementId related element to reload after the element has been deleted
     */
    public void deleteElement(String elementId, final String relatedElementId) {

        elementId = getServerId(elementId);
        removeContainerElements(elementId);
        addToRecentList(elementId, null);
        reloadElements(new String[] {relatedElementId});
    }

    /**
     * Disables the inline editing for all content elements but the given one.<p>
     * 
     * @param notThisOne the content element not to disable
     */
    public void disableInlineEditing(CmsContainerPageElementPanel notThisOne) {

        if (isGroupcontainerEditing()) {
            for (Widget element : m_groupEditor.getGroupContainerWidget()) {
                if ((element instanceof CmsContainerPageElementPanel) && (element != notThisOne)) {
                    ((CmsContainerPageElementPanel)element).removeInlineEditor();
                }
            }
        } else {
            for (org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer container : m_targetContainers.values()) {
                for (Widget element : container) {
                    if ((element instanceof CmsContainerPageElementPanel) && (element != notThisOne)) {
                        ((CmsContainerPageElementPanel)element).removeInlineEditor();
                    }
                }
            }
        }
    }

    /**
     * Enables the favorites editing drag and drop controller.<p>
     * 
     * @param enable if <code>true</code> favorites editing will enabled, otherwise disabled
     * @param dndController the favorites editing drag and drop controller
     */
    public void enableFavoriteEditing(boolean enable, I_CmsDNDController dndController) {

        if (m_dndHandler.isDragging()) {
            // never switch drag and drop controllers while dragging
            return;
        }
        if (enable) {
            m_dndHandler.setController(dndController);
        } else {
            m_dndHandler.setController(m_cntDndController);
        }

    }

    /**
     * Returns all drag elements of the page.<p>
     * 
     * @return the drag elements
     */
    public List<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel> getAllDragElements() {

        List<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel> result = new ArrayList<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel>();
        Iterator<org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> it = m_targetContainers.values().iterator();
        while (it.hasNext()) {
            result.addAll(it.next().getAllDragElements());
        }
        if (isGroupcontainerEditing()) {
            Iterator<Widget> itSub = m_groupEditor.getGroupContainerWidget().iterator();
            while (itSub.hasNext()) {
                Widget w = itSub.next();
                if (w instanceof org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel) {
                    result.add((org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)w);
                }
            }
        }
        return result;
    }

    /**
     * Returns the data for the requested element, or <code>null</code> if the element has not been cached yet.<p>
     * 
     * @param clientId the element id
     * 
     * @return the element data
     */
    public CmsContainerElementData getCachedElement(String clientId) {

        if (m_elements.containsKey(clientId)) {
            return m_elements.get(clientId);
        }
        return null;
    }

    /**
     * Returns the data for the requested element, or <code>null</code> if the element has not been cached yet.<p>
     * 
     * @param resourceTypeName the element resource type
     * 
     * @return the element data
     */
    public CmsContainerElementData getCachedNewElement(String resourceTypeName) {

        if (m_newElements.containsKey(resourceTypeName)) {
            return m_newElements.get(resourceTypeName);
        }
        return null;
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
    public org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer getContainerTarget(String containerName) {

        return m_targetContainers.get(containerName);
    }

    /**
     * Returns a map of the container drag targets.<p>
     * 
     * @return the drag targets
     */
    public Map<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> getContainerTargets() {

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
     * Returns the XML content editor handler.<p>
     *
     * @return the XML content editor handler
     */
    public CmsContentEditorHandler getContentEditorHandler() {

        return m_contentEditorHandler;
    }

    /**
     * Returns the prefetched data.<p>
     *
     * @return the prefetched data
     */
    public CmsCntPageData getData() {

        return m_data;
    }

    /**
     * Gets the DND controller.<p>
     * 
     * @return the DND controller
     */
    public CmsCompositeDNDController getDndController() {

        return m_dndController;
    }

    /**
     * Returns the drag and drop handler.<p>
     *
     * @return the drag and drop handler
     */
    public CmsDNDHandler getDndHandler() {

        return m_dndHandler;
    }

    /**
     * Requests the data for a container element specified by the client id. The data will be provided to the given call-back function.<p>
     * 
     * @param clientId the element id
     * @param callback the call-back to execute with the requested data
     */
    public void getElement(final String clientId, final I_CmsSimpleCallback<CmsContainerElementData> callback) {

        SingleElementAction action = new SingleElementAction(clientId, callback);
        action.execute();
    }

    /**
     * Requests the data for container elements specified by the client id. The data will be provided to the given call-back function.<p>
     * 
     * @param clientIds the element id's
     * @param callback the call-back to execute with the requested data
     */
    public void getElements(Set<String> clientIds, I_CmsSimpleCallback<Map<String, CmsContainerElementData>> callback) {

        MultiElementAction action = new MultiElementAction(clientIds, callback);
        action.execute();
    }

    /**
     * Returns the group-container element being edited.<p>
     * 
     * @return the group-container
     */
    public CmsGroupContainerElementPanel getGroupcontainer() {

        return m_groupEditor.getGroupContainerWidget();
    }

    /**
     * Returns the id of the currently edited group-container.<p>
     * 
     * @return the group-container id, or <code>null</code> if no editing is taking place
     */
    public String getGroupcontainerId() {

        if (m_groupEditor != null) {
            return m_groupEditor.getGroupContainerWidget().getContainerId();
        }
        return null;
    }

    /**
     * Returns the container-page handler.<p>
     *
     * @return the container-page handler
     */
    public CmsContainerpageHandler getHandler() {

        return m_handler;
    }

    /** 
     * Gets the lock error message.<p>
     * 
     * @return the lock error message
     */
    public String getLockErrorMessage() {

        return m_lockErrorMessage;
    }

    /**
     * Returns the element data for a resource type representing a new element.<p>
     * 
     * @param resourceType the resource type name
     * @param callback the callback to execute with the new element data
     */
    public void getNewElement(final String resourceType, final I_CmsSimpleCallback<CmsContainerElementData> callback) {

        if (m_elements.containsKey(resourceType)) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.user.client.Command#execute()
                 */
                public void execute() {

                    callback.execute(m_elements.get(resourceType));

                }
            });
        } else {
            CmsRpcAction<CmsContainerElementData> action = new CmsRpcAction<CmsContainerElementData>() {

                @Override
                public void execute() {

                    getContainerpageService().getNewElementData(CmsCoreProvider.get().getStructureId(),

                    getRequestParams(), resourceType, m_containerBeans, getLocale(), this);
                }

                @Override
                protected void onResponse(CmsContainerElementData result) {

                    m_elements.put(result.getClientId(), result);
                    callback.execute(result);
                }
            };
            action.execute();
        }
    }

    /**
     * Produces the "return code", which is needed to return to the current page from the sitemap.<p>
     * 
     * @return the return code 
     */
    public String getReturnCode() {

        CmsUUID ownId = CmsCoreProvider.get().getStructureId();
        CmsUUID detailId = m_data.getDetailId();
        if (detailId != null) {
            return "" + ownId + ":" + detailId;
        } else {
            return "" + ownId;
        }
    }

    /**
     * Returns the deserialized element data.<p>
     * 
     * @param data the data to deserialize
     * 
     * @return the container element
     * @throws SerializationException if deserialization fails
     */
    public CmsContainerElement getSerializedElement(String data) throws SerializationException {

        return (CmsContainerElement)CmsRpcPrefetcher.getSerializedObjectFromString(getContainerpageService(), data);
    }

    /** 
     * Gets the handler for small elements.<p>
     * 
     * @return the small elements handler 
     */
    public CmsSmallElementsHandler getSmallElementsHandler() {

        return m_smallElementsHandler;
    }

    /**
     * Handler that gets called when the template context setting of an element was changed by the user.<p>
     * 
     * @param element the element whose template context setting was changed 
     * 
     * @param newValue the new value of the setting  
     */
    public void handleChangeTemplateContext(final CmsContainerPageElementPanel element, final String newValue) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(newValue) || CmsTemplateContextInfo.EMPTY_VALUE.equals(newValue)) {
            if (CmsInheritanceContainerEditor.getInstance() != null) {
                CmsInheritanceContainerEditor.getInstance().removeElement(element);
            } else {
                removeElement(element, ElementRemoveMode.silent);
            }
        }
    }

    /** 
     * Asks the user for confirmation before removing a container page element.<p>
     * 
     * @param element the element for which the user should confirm the removal 
     */
    public void handleConfirmRemove(final CmsContainerPageElementPanel element) {

        if (element.isNew()) {
            element.removeFromParent();
            setPageChanged();
            return;
        }
        checkElementReferences(element, new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {

                // ignore, will never be executed 

            }

            public void onSuccess(Boolean hasOtherReferences) {

                boolean showDeleteCheckbox = !hasOtherReferences.booleanValue();
                CmsConfirmRemoveDialog removeDialog = new CmsConfirmRemoveDialog(
                    showDeleteCheckbox,
                    new AsyncCallback<Boolean>() {

                        public void onFailure(Throwable caught) {

                            element.removeHighlighting();
                        }

                        public void onSuccess(Boolean shouldDeleteResource) {

                            Runnable[] nextActions = new Runnable[] {};

                            if (shouldDeleteResource.booleanValue()) {
                                final CmsRpcAction<Void> deleteAction = new CmsRpcAction<Void>() {

                                    @Override
                                    public void execute() {

                                        start(200, true);

                                        CmsUUID id = new CmsUUID(getServerId(element.getId()));
                                        CmsCoreProvider.getVfsService().deleteResource(id, this);
                                    }

                                    @Override
                                    public void onResponse(Void result) {

                                        stop(true);
                                    }
                                };
                                nextActions = new Runnable[] {null};
                                nextActions[0] = new Runnable() {

                                    public void run() {

                                        deleteAction.execute();
                                    }
                                };
                            }
                            element.removeFromParent();
                            setPageChanged(nextActions);
                        }
                    });
                removeDialog.center();
            }

        });
    }

    /**
     * Returns if the selection button is active.<p>
     * 
     * @return <code>true</code> if the selection button is active
     */
    public boolean hasActiveSelection() {

        return m_handler.hasActiveSelection();
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
     * Hides list collector direct edit buttons, if present.<p>
     */
    public void hideEditableListButtons() {

        for (org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer container : m_targetContainers.values()) {
            container.hideEditableListButtons();
        }
    }

    /**
     * Initializes the controller.<p>
     * 
     * @param handler the container-page handler
     * @param dndHandler the drag and drop handler
     * @param contentEditorHandler the XML content editor handler
     * @param containerpageUtil the container-page utility
     */
    public void init(
        CmsContainerpageHandler handler,
        CmsDNDHandler dndHandler,
        CmsContentEditorHandler contentEditorHandler,
        CmsContainerpageUtil containerpageUtil) {

        m_containerpageUtil = containerpageUtil;
        m_handler = handler;
        m_contentEditorHandler = contentEditorHandler;
        m_dndHandler = dndHandler;
        m_cntDndController = m_dndHandler.getController();

        m_elements = new HashMap<String, CmsContainerElementData>();
        m_newElements = new HashMap<String, CmsContainerElementData>();
        m_containerTypes = new HashSet<String>();
        m_containers = new HashMap<String, CmsContainerJso>();
        if (m_data == null) {
            m_handler.m_editor.disableEditing(Messages.get().key(Messages.ERR_READING_CONTAINER_PAGE_DATA_0));
            CmsErrorDialog dialog = new CmsErrorDialog(
                Messages.get().key(Messages.ERR_READING_CONTAINER_PAGE_DATA_0),
                null);
            dialog.center();
            return;
        }
        JsArray<CmsContainerJso> containers = CmsContainerJso.getContainers();
        for (int i = 0; i < containers.length(); i++) {
            CmsContainerJso container = containers.get(i);
            m_containerTypes.add(container.getType());
            m_containers.put(container.getName(), container);
        }
        m_containerBeans = createEmptyContainerBeans();
        // ensure any embedded flash players are set opaque so UI elements may be placed above them
        CmsDomUtil.fixFlashZindex(RootPanel.getBodyElement());
        m_targetContainers = m_containerpageUtil.consumeContainers(m_containers);
        for (CmsContainerPageContainer cont : m_targetContainers.values()) {
            Element elem = DOM.getElementById(cont.getContainerId());
            CmsContainerpageEditor.getZIndexManager().addContainer(cont.getContainerId(), elem);
        }
        resetEditableListButtons();
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            public void onPreviewNativeEvent(NativePreviewEvent event) {

                previewNativeEvent(event);
            }
        });
        // adding on close handler
        Window.addWindowClosingHandler(new ClosingHandler() {

            /**
             * @see com.google.gwt.user.client.Window.ClosingHandler#onWindowClosing(com.google.gwt.user.client.Window.ClosingEvent)
             */
            public void onWindowClosing(ClosingEvent event) {

                deactivateOnClosing();
                if (hasPageChanged() && !isEditingDisabled()) {
                    boolean savePage = Window.confirm(Messages.get().key(Messages.GUI_DIALOG_SAVE_BEFORE_LEAVING_0));
                    if (savePage) {
                        syncSaveContainerpage();
                    } else {
                        unlockContainerpage();
                    }
                }
            }
        });
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_data.getNoEditReason())) {
            m_handler.m_editor.disableEditing(m_data.getNoEditReason());
        } else {
            checkLockInfo();
        }

        // initialize the browser history handler
        History.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                String historyToken = event.getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(historyToken)) {
                    getContentEditorHandler().openEditorForHistory(historyToken);
                } else {
                    getContentEditorHandler().closeContentEditor();
                }
            }
        });
        // check if there is already a history item available
        String historyToken = History.getToken();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(historyToken)) {
            m_contentEditorHandler.openEditorForHistory(historyToken);
        }
    }

    /**
     * Returns the flag indicating that a content element is being edited.<p>
     *
     * @return the flag indicating that a content element is being edited
     */
    public boolean isContentEditing() {

        return m_isContentEditing;
    }

    /**
     * Checks if the page editing features should be disabled.<p>
     * 
     * @return true if the page editing features should be disabled 
     */
    public boolean isEditingDisabled() {

        return (m_data == null)
            || CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_data.getNoEditReason())
            || (m_lockStatus == LockStatus.failed);
    }

    /**
     * Returns if a group-container is currently being edited.<p>
     * 
     * @return <code>true</code> if a group-container is being edited
     */
    public boolean isGroupcontainerEditing() {

        return m_groupEditor != null;
    }

    /**
     * Method to leave the page without saving.<p>
     * 
     * @param targetUri the new URI to call
     */
    public void leaveUnsaved(String targetUri) {

        setPageChanged(false, true);
        Window.Location.assign(targetUri);
    }

    /**
     * Loads the context menu entries.<p>
     * 
     * @param structureId the structure id of the resource to get the context menu entries for  
     * @param context the ade context (sitemap or containerpae)
     */
    public void loadContextMenu(final CmsUUID structureId, final AdeContext context) {

        /** The RPC menu action for the container page dialog. */
        CmsRpcAction<List<CmsContextMenuEntryBean>> menuAction = new CmsRpcAction<List<CmsContextMenuEntryBean>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                getCoreService().getContextMenuEntries(structureId, context, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsContextMenuEntryBean> menuBeans) {

                m_handler.insertContextMenu(menuBeans, structureId);
            }
        };
        menuAction.execute();

    }

    /**
     * Loads the favorite list and adds the elements to the favorite list widget of the tool-bar menu.<p>
     * 
     * @param callback the call-back to execute with the result data 
     */
    public void loadFavorites(final I_CmsSimpleCallback<List<CmsContainerElementData>> callback) {

        CmsRpcAction<List<CmsContainerElementData>> action = new CmsRpcAction<List<CmsContainerElementData>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, true);
                getContainerpageService().getFavoriteList(
                    CmsCoreProvider.get().getStructureId(),
                    m_containerBeans,
                    getLocale(),
                    this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(List<CmsContainerElementData> result) {

                stop(false);
                addElements(result);
                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Loads the recent list and adds the elements to the recent list widget of the tool-bar menu.<p>
     * 
     * @param callback the call-back to execute with the result data
     */
    public void loadRecent(final I_CmsSimpleCallback<List<CmsContainerElementData>> callback) {

        CmsRpcAction<List<CmsContainerElementData>> action = new CmsRpcAction<List<CmsContainerElementData>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, true);
                getContainerpageService().getRecentList(
                    CmsCoreProvider.get().getStructureId(),
                    m_containerBeans,
                    getLocale(),
                    this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(List<CmsContainerElementData> result) {

                stop(false);
                addElements(result);
                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Locks the container-page.<p>
     * 
     * @return <code>true</code> if page was locked successfully 
     */
    public boolean lockContainerpage() {

        if (m_lockStatus == LockStatus.locked) {
            return true;
        }
        if (m_lockStatus == LockStatus.failed) {
            return false;
        }
        String lockError = CmsCoreProvider.get().lockOrReturnError(CmsCoreProvider.get().getStructureId());
        if (lockError == null) {
            onLockSuccess();
            return true;
        } else {
            onLockFail(lockError);
            return false;
        }
    }

    /**
     * This method should be called when locking the page has failed.<p>
     * 
     * @param lockError the locking information  
     */
    public void onLockFail(String lockError) {

        m_lockStatus = LockStatus.failed;
        m_handler.onLockFail(lockError);
    }

    /**
     * This method should be called when locking the page has succeeded.<p>
     *  
     */
    public void onLockSuccess() {

        assert m_lockStatus == LockStatus.unknown;
        m_lockStatus = LockStatus.locked;
    }

    /**
     * Handler which is executed when the window closes.<p>
     */
    public void onWindowClose() {

        // causes synchronous RPC call 
        CmsCoreProvider.get().unlock();
    }

    /**
     * Re-initializes the inline editing.<p>
     */
    public void reInitInlineEditing() {

        if ((m_targetContainers == null) || getData().isUseClassicEditor()) {
            // if the target containers are not initialized yet or classic editor is set, don't do anything
            return;
        }
        if (isGroupcontainerEditing()) {
            for (Widget element : m_groupEditor.getGroupContainerWidget()) {
                if ((element instanceof CmsContainerPageElementPanel)) {
                    ((CmsContainerPageElementPanel)element).initInlineEditor(this);
                }
            }
        } else {
            for (org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer container : m_targetContainers.values()) {
                for (Widget element : container) {
                    if (element instanceof CmsContainerPageElementPanel) {
                        ((CmsContainerPageElementPanel)element).initInlineEditor(this);
                    }
                }
            }
        }
    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     * 
     * Call this if the element content has changed.<p>
     * 
     * @param ids the element id's
     */
    public void reloadElements(String[] ids) {

        Set<String> related = new HashSet<String>();
        for (int i = 0; i < ids.length; i++) {
            related.addAll(getRelatedElementIds(ids[i]));
        }
        if (!related.isEmpty()) {
            ReloadElementAction action = new ReloadElementAction(related);
            action.execute();
        }
    }

    /**
     * Reloads a container page element with a new set of settings.<p>
     * 
     * @param elementWidget the widget of the container page element which should be reloaded
     * @param clientId the id of the container page element which should be reloaded
     * @param settings the new set of settings 
     * @param afterReloadAction a callback which is executed after the element has been reloaded 
     */
    public void reloadElementWithSettings(
        final org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel elementWidget,
        String clientId,
        Map<String, String> settings,
        final AsyncCallback<CmsContainerPageElementPanel> afterReloadAction) {

        I_CmsSimpleCallback<CmsContainerElementData> callback = new I_CmsSimpleCallback<CmsContainerElementData>() {

            public void execute(CmsContainerElementData newElement) {

                try {
                    final CmsContainerPageElementPanel replacement = replaceContainerElement(elementWidget, newElement);
                    Runnable joinAction = new Runnable() {

                        public void run() {

                            if (afterReloadAction != null) {
                                afterReloadAction.onSuccess(replacement);
                            }
                        }
                    };
                    final CmsAsyncJoinHandler joinHandler = new CmsAsyncJoinHandler(joinAction);
                    joinHandler.addTokens("recentDone");

                    if (!isGroupcontainerEditing()) {
                        joinHandler.addTokens("saveDone");
                        setPageChanged(new Runnable() {

                            public void run() {

                                joinHandler.removeToken("saveDone");
                            }
                        });
                    }
                    resetEditableListButtons();
                    addToRecentList(newElement.getClientId(), new Runnable() {

                        public void run() {

                            joinHandler.removeToken("recentDone");
                        }
                    });
                } catch (Exception e) {
                    // should never happen
                    CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                }
            }
        };
        getElementWithSettings(clientId, settings, callback);
    }

    /**
     * Removes the given container element from its parent container.<p>
     * 
     * @param dragElement the element to remove
     * @param removeMode the remove mode  
     */
    public void removeElement(
        org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel dragElement,
        ElementRemoveMode removeMode) {

        if (isGroupcontainerEditing()) {
            dragElement.removeFromParent();
            if (!getGroupcontainer().iterator().hasNext()) {
                // group-container is empty, mark it
                getGroupcontainer().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
            }
            getGroupcontainer().refreshHighlighting();
        } else {
            final String id = dragElement.getId();
            if (id != null) {
                addToRecentList(id, null);
            }
            switch (removeMode) {
                case saveAndCheckReferences:
                    dragElement.removeFromParent();
                    Runnable checkReferencesAction = new Runnable() {

                        public void run() {

                            checkReferencesToRemovedElement(id);
                        }
                    };
                    setPageChanged(checkReferencesAction);
                    break;
                case confirmRemove:
                    handleConfirmRemove(dragElement);
                    break;
                case silent:
                default:
                    dragElement.removeFromParent();
                    setPageChanged();
                    break;
            }
        }
    }

    /**
     * Replaces the given drag-element with the given container element.<p>
     * 
     * @param containerElement the container element to replace
     * @param elementData the new element data
     * 
     * @return the container element which replaced the old one 
     * 
     * @throws Exception if something goes wrong
     */
    public CmsContainerPageElementPanel replaceContainerElement(
        org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel containerElement,
        CmsContainerElementData elementData) throws Exception {

        I_CmsDropContainer parentContainer = containerElement.getParentTarget();
        String containerId = parentContainer.getContainerId();

        String elementContent = elementData.getContents().get(containerId);
        if ((elementContent != null) && (elementContent.trim().length() > 0)) {
            org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel replacer = getContainerpageUtil().createElement(
                elementData,
                parentContainer);
            if (containerElement.isNew()) {
                // if replacing element data has the same structure id, keep the 'new' state by setting the new type property
                // this should only be the case when editing settings of a new element that has not been created in the VFS yet
                String id = getServerId(containerElement.getId());
                if (elementData.getClientId().startsWith(id)) {
                    replacer.setNewType(containerElement.getNewType());
                }
            }
            if (isGroupcontainerEditing() && (containerElement.getInheritanceInfo() != null)) {
                // in case of inheritance container editing, keep the inheritance info
                replacer.setInheritanceInfo(containerElement.getInheritanceInfo());
                // set the proper element options
                CmsInheritanceContainerEditor.getInstance().setOptionBar(replacer);
            }
            parentContainer.insert(replacer, parentContainer.getWidgetIndex(containerElement));
            containerElement.removeFromParent();
            return replacer;
        }
        return null;
    }

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    public void resetEditableListButtons() {

        for (org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer container : m_targetContainers.values()) {
            container.showEditableListButtons();
        }
    }

    /**
     * Resets the container-page.<p>
     */
    public void resetPage() {

        setPageChanged(false, true);
        Window.Location.reload();
    }

    /**
     * Method to save and leave the page.<p>
     * 
     * @param leaveCommand the command to execute to leave the page
     */
    public void saveAndLeave(final Command leaveCommand) {

        if (hasPageChanged()) {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getContainerpageService().saveContainerpage(
                        CmsCoreProvider.get().getStructureId(),
                        getPageContent(),
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Void result) {

                    CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_SAVED_0));
                    setPageChanged(false, true);
                    leaveCommand.execute();
                }
            };
            action.execute();
        }
    }

    /**
     * Method to save and leave the page.<p>
     * 
     * @param targetUri the new URI to call
     */
    public void saveAndLeave(final String targetUri) {

        if (hasPageChanged()) {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getContainerpageService().saveContainerpage(
                        CmsCoreProvider.get().getStructureId(),
                        getPageContent(),
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Void result) {

                    CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_SAVED_0));
                    setPageChanged(false, true);
                    Window.Location.assign(targetUri);
                }
            };
            action.execute();
        }
    }

    /**
     * Saves the current state of the container-page.<p>
     * 
     * @param afterSaveActions the actions to execute after saving 
     */
    public void saveContainerpage(final Runnable... afterSaveActions) {

        if (hasPageChanged()) {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    if (lockContainerpage()) {
                        setLoadingMessage(org.opencms.gwt.client.Messages.get().key(
                            org.opencms.gwt.client.Messages.GUI_SAVING_0));
                        start(500, true);
                        getContainerpageService().saveContainerpage(
                            CmsCoreProvider.get().getStructureId(),
                            getPageContent(),
                            getLocale(),
                            this);
                    }
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Void result) {

                    stop(false);
                    setPageChanged(false, false);
                    for (Runnable afterSaveAction : afterSaveActions) {
                        afterSaveAction.run();
                    }
                }
            };
            action.execute();
        }
    }

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds the client id's of the list's elements
     */
    public void saveFavoriteList(final List<String> clientIds) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().saveFavoriteList(clientIds, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_FAVORITES_SAVED_0));
            }
        };
        action.execute();
    }

    /**
     * Saves the group-container.<p>
     * 
     * @param groupContainer the group-container data to save 
     * @param groupContainerElement the group-container widget
     */
    public void saveGroupcontainer(
        final CmsGroupContainer groupContainer,
        final CmsGroupContainerElementPanel groupContainerElement) {

        if (getGroupcontainer() != null) {
            CmsRpcAction<CmsGroupContainerSaveResult> action = new CmsRpcAction<CmsGroupContainerSaveResult>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getContainerpageService().saveGroupContainer(
                        CmsCoreProvider.get().getStructureId(),
                        getRequestParams(),
                        groupContainer,
                        m_containerBeans,
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(CmsGroupContainerSaveResult saveResult) {

                    Map<String, CmsContainerElementData> elementData = saveResult.getElementData();
                    m_elements.putAll(elementData);
                    try {
                        replaceContainerElement(groupContainerElement, elementData.get(groupContainerElement.getId()));
                    } catch (Exception e) {
                        CmsDebugLog.getInstance().printLine("Error replacing group container element");
                    }
                    addToRecentList(groupContainerElement.getId(), null);
                    CmsNotification.get().send(
                        Type.NORMAL,
                        Messages.get().key(Messages.GUI_NOTIFICATION_GROUP_CONTAINER_SAVED_0));
                    List<CmsRemovedElementStatus> removedElements = saveResult.getRemovedElements();
                    for (CmsRemovedElementStatus removedElement : removedElements) {
                        askWhetherRemovedElementShouldBeDeleted(removedElement);
                    }

                }
            };
            action.execute();

        }
    }

    /**
     * Saves the inheritance container.<p>
     * 
     * @param inheritanceContainer the inheritance container data to save 
     * @param groupContainerElement the group container widget
     */
    public void saveInheritContainer(
        final CmsInheritanceContainer inheritanceContainer,
        final CmsGroupContainerElementPanel groupContainerElement) {

        if (getGroupcontainer() != null) {
            CmsRpcAction<Map<String, CmsContainerElementData>> action = new CmsRpcAction<Map<String, CmsContainerElementData>>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getContainerpageService().saveInheritanceContainer(
                        CmsCoreProvider.get().getStructureId(),
                        inheritanceContainer,
                        m_containerBeans,
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Map<String, CmsContainerElementData> result) {

                    m_elements.putAll(result);
                    try {
                        replaceContainerElement(groupContainerElement, result.get(groupContainerElement.getId()));
                    } catch (Exception e) {
                        CmsDebugLog.getInstance().printLine("Error replacing group container element");
                    }
                    addToRecentList(groupContainerElement.getId(), null);
                    CmsNotification.get().send(
                        Type.NORMAL,
                        Messages.get().key(Messages.GUI_NOTIFICATION_INHERITANCE_CONTAINER_SAVED_0));

                }
            };
            action.execute();

        }
    }

    /**
     * Sets the flag indicating that a content element is being edited.<p>
     *
     * @param isContentEditing the flag indicating that a content element is being edited
     */
    public void setContentEditing(boolean isContentEditing) {

        if (m_groupEditor != null) {
            if (isContentEditing) {
                m_groupEditor.hidePopup();
            } else {
                m_groupEditor.showPopup();
            }
        }
        m_isContentEditing = isContentEditing;
    }

    /**
     * Sets the DND controller.<p>
     * 
     * @param dnd the new DND controller 
     */
    public void setDndController(CmsCompositeDNDController dnd) {

        m_dndController = dnd;
    }

    /**
     * Marks the page as changed.<p>
     * 
     * @param nextActions the actions to perform after the page has been marked as changed 
     */
    public void setPageChanged(Runnable... nextActions) {

        // the container page will be saved immediately
        m_pageChanged = true;
        saveContainerpage(nextActions);
        //        if (!hasPageChanged()) {
        //            setPageChanged(true, false);
        //        }
    }

    /**
     * Writes the tool-bar visibility into the session cache.<p>
     * 
     * @param visible <code>true</code> if the tool-bar is visible
     */
    public void setToolbarVisible(final boolean visible) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getCoreService().setToolbarVisible(visible, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                //nothing to do
            }
        };
        action.execute();
        if (visible) {
            resetEditableListButtons();
        }
    }

    /**
     * Method to determine whether a container element should be shown in the current template context.<p>
     * 
     * @param elementData the element data 
     * 
     * @return true if the element should be shown
     */
    public boolean shouldShowInContext(CmsContainerElementData elementData) {

        CmsTemplateContextInfo contextInfo = getData().getTemplateContextInfo();
        if (contextInfo.getCurrentContext() == null) {
            return true;
        }
        CmsDefaultSet<String> allowedContexts = contextInfo.getAllowedContexts().get(elementData.getResourceType());
        if ((allowedContexts != null) && !allowedContexts.contains(contextInfo.getCurrentContext())) {
            return false;
        }

        String settingValue = elementData.getSettings().get(CmsTemplateContextInfo.SETTING);
        return (settingValue == null) || settingValue.contains(contextInfo.getCurrentContext());
    }

    /**
     * Tells the controller that group-container editing has started.<p>
     * 
     * @param groupContainer the group container
     * @param isElementGroup <code>true</code> if the group container is an element group and not an inheritance group
     */
    public void startEditingGroupcontainer(CmsGroupContainerElementPanel groupContainer, boolean isElementGroup) {

        if ((m_groupEditor == null)
            && (groupContainer.isNew() || CmsCoreProvider.get().lock(groupContainer.getStructureId()))) {
            if (isElementGroup) {
                m_groupEditor = CmsGroupContainerEditor.openGroupcontainerEditor(groupContainer, this, m_handler);
            } else {
                m_groupEditor = CmsInheritanceContainerEditor.openInheritanceContainerEditor(
                    groupContainer,
                    this,
                    m_handler);
            }
            return;
        }
        CmsNotification.get().send(Type.WARNING, Messages.get().key(Messages.GUI_NOTIFICATION_UNABLE_TO_LOCK_0));
    }

    /**
     * Tells the controller that group-container editing has stopped.<p>
     */
    public void stopEditingGroupcontainer() {

        m_groupEditor = null;
    }

    /**
     * Unlocks the given resource.<p>
     * 
     * @param structureId the structure id of the resource to unlock
     * 
     * @return <code>true</code> if the resource was unlocked successfully
     */
    public boolean unlockResource(CmsUUID structureId) {

        return CmsCoreProvider.get().unlock(structureId);
    }

    /** 
     * Adds the given element data to the element cache.<p>
     * 
     * @param elements the element data
     */
    protected void addElements(List<CmsContainerElementData> elements) {

        for (CmsContainerElementData element : elements) {
            m_elements.put(element.getClientId(), element);
        }
    }

    /** 
     * Adds the given element data to the element cache.<p>
     * 
     * @param elements the element data
     */
    protected void addElements(Map<String, CmsContainerElementData> elements) {

        m_elements.putAll(elements);
    }

    /**
     * Checks that a removed can be possibly deleted and if so, asks the user if it should be deleted.<p>
     * 
     * @param id the client id of the element
     */
    protected void checkReferencesToRemovedElement(final String id) {

        if (id != null) {
            //NOTE: We only use an RPC call here to check for references on the server side. If, at a later point, we decide
            //to add a save button again, this will have to be changed, because then we have to consider client-side state.
            CmsRpcAction<CmsRemovedElementStatus> getStatusAction = new CmsRpcAction<CmsRemovedElementStatus>() {

                @Override
                public void execute() {

                    start(200, true);
                    getContainerpageService().getRemovedElementStatus(id, null, this);
                }

                @Override
                public void onResponse(final CmsRemovedElementStatus status) {

                    stop(false);
                    if (status.isDeletionCandidate()) {
                        askWhetherRemovedElementShouldBeDeleted(status);

                    }
                }

            };
            getStatusAction.execute();

        }
    }

    /**
     * Disables option and toolbar buttons.<p>
     */
    protected void deactivateOnClosing() {

        m_handler.deactivateCurrentButton();
        m_handler.disableToolbarButtons();
    }

    /**
     * Returns the container-page RPC service.<p>
     * 
     * @return the container-page service
     */
    protected I_CmsContainerpageServiceAsync getContainerpageService() {

        if (m_containerpageService == null) {
            m_containerpageService = GWT.create(I_CmsContainerpageService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.containerpage.CmsContainerpageService.gwt");
            ((ServiceDefTarget)m_containerpageService).setServiceEntryPoint(serviceUrl);
        }
        return m_containerpageService;
    }

    /**
     * Returns the core RPC service.<p>
     * 
     * @return the core service
     */
    protected I_CmsCoreServiceAsync getCoreService() {

        if (m_coreSvc == null) {
            m_coreSvc = CmsCoreProvider.getService();
        }
        return m_coreSvc;
    }

    /**
     * Returns the currently active group editor.<p>
     * 
     * @return the currently active group editor
     */
    protected A_CmsGroupEditor getGroupEditor() {

        return m_groupEditor;
    }

    /**
     * Returns the content locale.<p>
     * 
     * @return the content locale
     */
    protected String getLocale() {

        return m_data.getLocale();
    }

    /**
     * Gets the page content for purposes of saving.<p>
     * 
     * @return the page content 
     */
    protected List<CmsContainer> getPageContent() {

        SaveDataVisitor visitor = new SaveDataVisitor();
        processPageContent(visitor);
        return visitor.getContainers();

    }

    /**
     * Returns the request parameters of the displayed container-page.<p>
     * 
     * @return the request parameters
     */
    protected String getRequestParams() {

        return m_data.getRequestParams();
    }

    /**
     * Opens the editor for the newly created element.<p>
     * 
     * @param element the container element
     * @param newElementData the new element data
     * @param inline <code>true</code> to open the inline editor for the given element if available
     */
    protected void openEditorForNewElement(
        org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel element,
        CmsContainerElement newElementData,
        boolean inline) {

        element.setNewType(null);
        if (inline) {
            String oldId = CmsUUID.getNullUUID().toString();
            String newId = getServerId(newElementData.getClientId());
            CmsContentEditor.replaceResourceIds(element.getElement(), oldId, newId);
        }
        element.setId(newElementData.getClientId());
        element.setSitePath(newElementData.getSitePath());

        setPageChanged();
        getHandler().hidePageOverlay();
        getHandler().openEditorForElement(element, inline);
    }

    /**
    * Previews events. Shows the leaving page dialog, if the page has changed and an anchor has been clicked.<p>
    * 
    * @param event the native event
    */
    protected void previewNativeEvent(NativePreviewEvent event) {

        Event nativeEvent = Event.as(event.getNativeEvent());
        if (!hasPageChanged()) {
            return;
        }
        if ((nativeEvent.getTypeInt() == Event.ONCLICK)) {
            EventTarget target = nativeEvent.getEventTarget();
            if (!Element.is(target)) {
                return;
            }
            Element element = Element.as(target);
            element = CmsDomUtil.getAncestor(element, CmsDomUtil.Tag.a);
            if (element == null) {
                return;
            }
            AnchorElement anc = AnchorElement.as(element);
            final String uri = anc.getHref();

            // avoid to abort events for date-picker widgets
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(uri)
                || (CmsDomUtil.getAncestor(element, "x-date-picker") != null)) {
                return;
            }
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            m_handler.leavePage(uri);
        }
        if ((event.getTypeInt() == Event.ONKEYDOWN) && (nativeEvent.getKeyCode() == 116)) {
            // user pressed F5
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            m_handler.leavePage(Window.Location.getHref());
        }
    }

    /** 
     * Iterates over all the container contents and calls a visitor object with the visited containers/elements as parameters.
     * 
     * @param visitor the visitor which the container elements should be passed to 
     */
    protected void processPageContent(I_PageContentVisitor visitor) {

        for (Entry<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> entry : m_targetContainers.entrySet()) {

            CmsContainerJso cnt = m_containers.get(entry.getKey());
            if (visitor.beginContainer(entry.getKey(), cnt)) {
                Iterator<Widget> elIt = entry.getValue().iterator();
                while (elIt.hasNext()) {
                    try {
                        CmsContainerPageElementPanel elementWidget = (CmsContainerPageElementPanel)elIt.next();
                        visitor.handleElement(elementWidget);
                    } catch (ClassCastException e) {
                        // no proper container element, skip it (this should never happen!)
                        CmsDebugLog.getInstance().printLine(
                            "WARNING: there is an inappropriate element within a container");
                    }
                }
                visitor.endContainer();
            }
        }
    }

    /**
     * Removes all container elements with the given id from all containers and the client side cache.<p>
     * 
     * @param resourceId the resource id
     */
    protected void removeContainerElements(String resourceId) {

        boolean changed = false;
        Iterator<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel> it = getAllDragElements().iterator();
        while (it.hasNext()) {
            org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel containerElement = it.next();
            if (resourceId.startsWith(containerElement.getId())) {
                containerElement.removeFromParent();
                changed = true;
            }
        }
        for (String elementId : m_elements.keySet()) {
            if (elementId.startsWith(resourceId)) {
                m_elements.remove(elementId);
            }
        }
        if (changed) {
            setPageChanged();
        }
    }

    /**
     * Sets the page changed flag and initializes the window closing handler if necessary.<p>
     * 
     * @param changed if <code>true</code> the page has changed
     * @param unlock if <code>true</code> the page will be unlocked for unchanged pages
     */
    protected void setPageChanged(boolean changed, boolean unlock) {

        if (changed) {
            if (!m_pageChanged) {

                m_pageChanged = changed;
                if (lockContainerpage()) {
                    m_handler.enableSaveReset(!isEditingDisabled());
                }
            }
        } else {
            m_pageChanged = changed;
            m_handler.enableSaveReset(false);
            if (unlock) {
                unlockContainerpage();
            }
        }
    }

    /**
     * Saves the container-page in a synchronized RPC call.<p>
     */
    protected void syncSaveContainerpage() {

        if (hasPageChanged()) {
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    getContainerpageService().syncSaveContainerpage(
                        CmsCoreProvider.get().getStructureId(),
                        getPageContent(),
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Void result) {

                    CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_SAVED_0));
                    setPageChanged(false, false);
                }
            };
            action.execute();
        }
    }

    /**
     * Unlocks the container-page in a synchronized RPC call.<p>
     */
    protected void unlockContainerpage() {

        if (unlockResource(CmsCoreProvider.get().getStructureId())) {
            CmsDebugLog.getInstance().printLine(Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_UNLOCKED_0));
        } else {
            // ignore
        }
    }

    /**
     * Checks whether there are other references to a given container page element.<p>
     * 
     * @param element the element to check 
     * @param callback the callback which will be called with the result of the check (true if there are other references)
     */
    private void checkElementReferences(
        final CmsContainerPageElementPanel element,
        final AsyncCallback<Boolean> callback) {

        ReferenceCheckVisitor visitor = new ReferenceCheckVisitor(element);
        processPageContent(visitor);
        if (visitor.hasReferences()) {
            // Don't need to ask the server because we already know we have other references in the same page 
            callback.onSuccess(Boolean.TRUE);
        } else {
            CmsRpcAction<CmsRemovedElementStatus> getStatusAction = new CmsRpcAction<CmsRemovedElementStatus>() {

                @Override
                public void execute() {

                    start(200, true);
                    getContainerpageService().getRemovedElementStatus(
                        element.getId(),
                        CmsCoreProvider.get().getStructureId(),
                        this);
                }

                @Override
                public void onResponse(final CmsRemovedElementStatus status) {

                    stop(false);
                    callback.onSuccess(Boolean.valueOf(!status.isDeletionCandidate()));
                }

            };
            getStatusAction.execute();

        }
    }

    /**
     * Checks if the page was locked by another user at load time.<p>
     */
    private void checkLockInfo() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getData().getLockInfo())) {
            CmsNotification.get().send(Type.ERROR, getData().getLockInfo());
            m_lockStatus = LockStatus.failed;
            m_handler.m_editor.disableEditing(getData().getLockInfo());
        }
    }

    /**
     * Creates beans for each of this page's containers and ignore their contents.<p>
     * 
     * @return a list of container beans without contents 
     */
    private List<CmsContainer> createEmptyContainerBeans() {

        List<CmsContainer> result = new ArrayList<CmsContainer>();
        for (CmsContainerJso containerJso : m_containers.values()) {
            CmsContainer container = new CmsContainer(
                containerJso.getName(),
                containerJso.getType(),
                containerJso.getWidth(),
                containerJso.getMaxElements(),
                null);
            result.add(container);
        }
        return result;
    }

    /**
     * Retrieves a container element with a given set of settings.<p>
     * 
     * @param clientId the id of the container element
     * @param settings the set of settings
     *  
     * @param callback the callback which should be executed when the element has been loaded 
     */
    private void getElementWithSettings(
        final String clientId,
        final Map<String, String> settings,
        final I_CmsSimpleCallback<CmsContainerElementData> callback) {

        CmsRpcAction<CmsContainerElementData> action = new CmsRpcAction<CmsContainerElementData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(200, false);
                getContainerpageService().getElementWithSettings(
                    CmsCoreProvider.get().getStructureId(),
                    getRequestParams(),
                    clientId,
                    settings,
                    m_containerBeans,
                    getLocale(),
                    this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsContainerElementData result) {

                stop(false);
                if (result != null) {
                    // cache the loaded element
                    m_elements.put(result.getClientId(), result);
                }
                callback.execute(result);
            }

        };
        action.execute();
    }

    /**
     * Returns all element id's related to the given one.<p>
     * 
     * @param id the element id
     * @return the related id's
     */
    private Set<String> getRelatedElementIds(String id) {

        Set<String> result = new HashSet<String>();
        if (id != null) {
            result.add(id);
            String serverId = getServerId(id);

            Iterator<String> it = m_elements.keySet().iterator();
            while (it.hasNext()) {
                String elId = it.next();
                if (elId.startsWith(serverId)) {
                    result.add(elId);
                }
            }

            Iterator<org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel> itEl = getAllDragElements().iterator();
            while (itEl.hasNext()) {
                org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel element = itEl.next();
                if (element.getId().startsWith(serverId)) {
                    result.add(element.getId());
                }
            }
        }
        return result;
    }

}
