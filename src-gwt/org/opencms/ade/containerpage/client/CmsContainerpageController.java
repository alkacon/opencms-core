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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.CmsContainerpageEvent.EventType;
import org.opencms.ade.containerpage.client.ui.CmsConfirmRemoveDialog;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsRemovedElementDeletionDialog;
import org.opencms.ade.containerpage.client.ui.CmsReuseInfoDialog;
import org.opencms.ade.containerpage.client.ui.CmsSmallElementsHandler;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsGroupContainerEditor;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsInheritanceContainerEditor;
import org.opencms.ade.containerpage.shared.CmsCntPageData;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsContainerPageGalleryData;
import org.opencms.ade.containerpage.shared.CmsContainerPageRpcContext;
import org.opencms.ade.containerpage.shared.CmsCreateElementData;
import org.opencms.ade.containerpage.shared.CmsDialogOptionsAndInfo;
import org.opencms.ade.containerpage.shared.CmsElementSettingsConfig;
import org.opencms.ade.containerpage.shared.CmsElementViewInfo;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.ade.containerpage.shared.CmsGroupContainerSaveResult;
import org.opencms.ade.containerpage.shared.CmsInheritanceContainer;
import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.ade.containerpage.shared.CmsReuseInfo;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageService;
import org.opencms.ade.containerpage.shared.rpc.I_CmsContainerpageServiceAsync;
import org.opencms.ade.contenteditor.client.CmsContentEditor;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsElementToolbarContext;
import org.opencms.gwt.client.dnd.CmsCompositeDNDController;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.contextmenu.CmsEmbeddedAction;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGalleryContainerInfo;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsGwtLog;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.gwt.shared.I_CmsAutoBeanFactory;
import org.opencms.gwt.shared.I_CmsUnlockData;
import org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync;
import org.opencms.util.CmsDefaultSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import elemental2.core.JsObject;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.webstorage.WebStorageWindow;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

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
     * Wrapper for the event details object for custom page editor events.
     */
    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "?")
    public interface I_EventDetails {

        /**
         * Gets the HTML element for the container itself.
         *
         * @return the HTML element for the container
         */
        @JsProperty
        elemental2.dom.Element getContainer();

        /**
         * Gets the HTML element for the page element.
         *
         * @return the HTML element for the page element
         */
        @JsProperty
        elemental2.dom.Element getElement();

        /**
         * The 'placeholder' state of the element.
         *
         * @return the 'placedholder' state
         */
        @JsProperty
        boolean getIsPlaceholder();

        /**
         * Gets the replaced element.
         *
         * @return the replaced element
         */
        @JsProperty
        elemental2.dom.Element getReplacedElement();

        /**
         * Gets the type.
         *
         * @return the type
         */
        @JsProperty
        String getType();

        /**
         * Sets the HTML element for the container.
         *
         * @param container the HTML element for the container
         */
        @JsProperty
        void setContainer(elemental2.dom.Element container);

        /**
         * Sets the HTML element for the page element.
         * @param element the HTML element for the page element
         */
        @JsProperty
        void setElement(elemental2.dom.Element element);

        /**
         * Sets the 'placeholder' (i.e. 'new') state
         *
         * @param isPlaceholder true if the element was new
         */
        @JsProperty
        void setIsPlaceholder(boolean isPlaceholder);

        /**
         * Sets the replaced element.
         *
         * @param element the replaced element
         */
        @JsProperty
        void setReplacedElement(elemental2.dom.Element element);

        /**
         * Sets the type.
         *
         * @param type the type
         */
        @JsProperty
        void setType(String type);

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
        boolean beginContainer(String name, CmsContainer container);

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
     * Interface for classes that should be notified when container page elements are reloaded.
     */
    public interface I_ReloadHandler {

        /**
         * Called after all other onReload calls for the current operation.
         * */
        void finish();

        /**
         * Is called when an element has been replaced on the page.
         *
         * @param oldElement the old element
         * @param newElement the replacement element that is currently on the page
         */
        void onReload(CmsContainerPageElementPanel oldElement, CmsContainerPageElementPanel newElement);
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
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#beginContainer(java.lang.String, org.opencms.ade.containerpage.shared.CmsContainer)
         */
        public boolean beginContainer(String name, CmsContainer container) {

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
    protected class PageStateVisitor implements I_PageContentVisitor {

        /** The current container name. */
        protected String m_containerName;

        /** The contaienr which is currently being processed. */
        protected CmsContainer m_currentContainer;

        /** The list of collected containers. */
        protected List<CmsContainer> m_resultContainers = new ArrayList<CmsContainer>();

        /** The list of elements of the currently processed container which have already been processed. */
        List<CmsContainerElement> m_currentElements;

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#beginContainer(java.lang.String, org.opencms.ade.containerpage.shared.CmsContainer)
         */
        public boolean beginContainer(String name, CmsContainer container) {

            m_currentContainer = container;
            m_containerName = name;
            m_currentElements = new ArrayList<CmsContainerElement>();
            return true;
        }

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#endContainer()
         */
        public void endContainer() {

            CmsContainer container = new CmsContainer(
                m_containerName,
                m_currentContainer.getType(),
                null,
                m_currentContainer.getWidth(),
                m_currentContainer.getMaxElements(),
                m_currentContainer.isDetailViewContainer(),
                m_currentContainer.isDetailView(),
                true,
                m_currentElements,
                m_currentContainer.getParentContainerName(),
                m_currentContainer.getParentInstanceId(),
                m_currentContainer.getSettingPresets());
            container.setDetailOnly(m_currentContainer.isDetailOnly());
            container.setRootContainer(isRootContainer(m_currentContainer));
            m_resultContainers.add(container);
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
            element.setCreateNew(elementWidget.isCreateNew());
            element.setModelGroupId(elementWidget.getModelGroupId());
            element.setSitePath(elementWidget.getSitePath());
            element.setNewEditorDisabled(elementWidget.isNewEditorDisabled());
            m_currentElements.add(element);
        }

    }

    /**
     * Visitor implementation which is used to gather the container contents for saving.<p>
     */
    protected class SaveDataVisitor implements I_PageContentVisitor {

        /** The current container name. */
        protected String m_containerName;

        /** The contaienr which is currently being processed. */
        protected CmsContainer m_currentContainer;

        /** The list of collected containers. */
        protected List<CmsContainer> m_resultContainers = new ArrayList<CmsContainer>();

        /** The list of elements of the currently processed container which have already been processed. */
        List<CmsContainerElement> m_currentElements;

        /**
         * @see org.opencms.ade.containerpage.client.CmsContainerpageController.I_PageContentVisitor#beginContainer(java.lang.String, org.opencms.ade.containerpage.shared.CmsContainer)
         */
        public boolean beginContainer(String name, CmsContainer container) {

            if (container.isDetailView() || ((getData().getDetailId() != null) && !container.isDetailOnly())) {
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

            CmsContainer container = new CmsContainer(
                m_containerName,
                m_currentContainer.getType(),
                null,
                m_currentContainer.getWidth(),
                m_currentContainer.getMaxElements(),
                m_currentContainer.isDetailViewContainer(),
                m_currentContainer.isDetailView(),
                true,
                m_currentElements,
                m_currentContainer.getParentContainerName(),
                m_currentContainer.getParentInstanceId(),
                m_currentContainer.getSettingPresets());

            container.setRootContainer(isRootContainer(m_currentContainer));
            m_resultContainers.add(container);
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
            element.setCreateNew(elementWidget.isCreateNew());
            element.setModelGroupId(elementWidget.getModelGroupId());
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
                    getData().getRpcContext(),
                    getData().getDetailId(),
                    getRequestParams(),
                    m_clientIds,
                    getPageState(),
                    false,
                    null,
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

        /** The callback to execute after the reload. */
        private I_ReloadHandler m_reloadHandler;

        /**
         * Constructor.<p>
         *
         * @param clientIds the client id's to reload
         * @param reloadHandler  the callback to call after the elements have been replaced
         */
        public ReloadElementAction(Set<String> clientIds, I_ReloadHandler reloadHandler) {

            super();
            m_clientIds = clientIds;
            m_reloadHandler = reloadHandler;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            getContainerpageService().getElementsData(
                getData().getRpcContext(),
                getData().getDetailId(),
                getRequestParams(),
                m_clientIds,
                getPageState(),
                false,
                null,
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
            boolean reloadMarkerFound = false;
            while (it.hasNext()) {
                org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel containerElement = it.next();
                if (!m_clientIds.contains(containerElement.getId())) {
                    continue;
                }
                try {
                    CmsContainerPageElementPanel replacer = replaceContainerElement(
                        containerElement,
                        result.get(containerElement.getId()));
                    if (replacer.getElement().getInnerHTML().contains(CmsGwtConstants.FORMATTER_RELOAD_MARKER)) {
                        reloadMarkerFound = true;
                    }
                    m_reloadHandler.onReload(containerElement, replacer);
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine("trying to replace");
                    CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                }

            }
            if (isGroupcontainerEditing()) {
                getGroupEditor().updateBackupElements(result);
                getGroupcontainer().refreshHighlighting();
            } else {
                if (reloadMarkerFound) {
                    CmsContainerpageController.get().reloadPage();
                } else {
                    long loadTime = result.values().iterator().next().getLoadTime();
                    setLoadTime(Long.valueOf(loadTime));
                }
            }
            m_handler.updateClipboard(result);
            resetEditButtons();
            CmsContainerpageController.get().fireEvent(new CmsContainerpageEvent(EventType.elementEdited));
            if (m_reloadHandler != null) {
                m_reloadHandler.finish();
            }
        }
    }

    /**
     * A RPC action implementation used to request the data for a single container-page element.<p>
     */
    private class SingleElementAction extends CmsRpcAction<Map<String, CmsContainerElementData>> {

        /** Always copy createNew elements in case reading data for a clipboard element used as a copy group. */
        private boolean m_alwaysCopy;

        /** Call-back executed on response. */
        private I_CmsSimpleCallback<CmsContainerElementData> m_callback;
        /** The requested client id. */
        private String m_clientId;

        /** If this action was triggered by drag and drop from a container, this should contain the id of the origin container. */
        private String m_dndContainer;

        /**
         * Constructor.<p>
         *
         * @param clientId the client id
         * @param callback the call-back
         * @param alwaysCopy <code>true</code> in case reading data for a clipboard element used as a copy group
         */
        public SingleElementAction(
            String clientId,
            boolean alwaysCopy,
            I_CmsSimpleCallback<CmsContainerElementData> callback) {

            super();
            m_clientId = clientId;
            m_callback = callback;
            m_alwaysCopy = alwaysCopy;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            List<String> clientIds = new ArrayList<String>();
            clientIds.add(m_clientId);
            getContainerpageService().getElementsData(
                getData().getRpcContext(),
                getData().getDetailId(),
                getRequestParams(),
                clientIds,
                getPageState(),
                m_alwaysCopy,
                m_dndContainer,
                getLocale(),
                this);
        }

        /**
         * Sets the origin container for the drag and drop case.<p>
         *
         * @param containerId the origin container name
         */
        public void setDndContainer(String containerId) {

            m_dndContainer = containerId;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(Map<String, CmsContainerElementData> result) {

            if (result != null) {
                addElements(result);
                long loadTime = result.get(m_clientId).getLoadTime();
                setLoadTime(Long.valueOf(loadTime));
                m_callback.execute(result.get(m_clientId));
            }
        }
    }

    /** The client side id/setting-hash seperator. */
    public static final String CLIENT_ID_SEPERATOR = "#";

    /** Custom event name. */
    public static final String EVENT_DRAG = "ocDrag";

    /** Custom event name. */
    public static final String EVENT_DRAG_FINISHED = "ocDragFinished";

    /** Custom event name. */
    public static final String EVENT_DRAG_STARTED = "ocDragStarted";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_ADDED = "ocElementAdded";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_DELETED = "ocElementDeleted";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_EDITED = "ocElementEdited";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_EDITED_CONTENT = "ocElementEditedContent";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_EDITED_SETTINGS = "ocElementEditedSettings";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_MODIFIED = "ocElementModified";

    /** Custom event name. */
    public static final String EVENT_ELEMENT_MOVED = "ocElementMoved";

    /** CSS class on the body to signal whether we are currently editing a group container. */
    public static final String OC_EDITING_GROUPCONTAINER = "oc-editing-groupcontainer";

    /** Parameter name. */
    public static final String PARAM_REMOVEMODE = "removemode";

    /** Instance of the data provider. */
    private static CmsContainerpageController INSTANCE;

    /** The container element data. All requested elements will be cached here.*/
    protected Map<String, CmsContainerElementData> m_elements;

    /** The new element data by resource type name. */
    protected Map<String, CmsContainerElementData> m_newElements;

    /** The gallery data update timer. */
    Timer m_galleryUpdateTimer;

    /** The currently editing group-container editor. */
    A_CmsGroupEditor m_groupEditor;

    /** The container-page handler. */
    CmsContainerpageHandler m_handler;

    /** The drag targets within this page. */
    Map<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> m_targetContainers;

    /** The UUIDs for which reuse status has already been checked. */
    private Set<CmsUUID> m_checkedReuse = new HashSet<>();

    /** The container page drag and drop controller. */
    private I_CmsDNDController m_cntDndController;

    /** The container-page RPC service. */
    private I_CmsContainerpageServiceAsync m_containerpageService;

    /** The container-page util instance. */
    private CmsContainerpageUtil m_containerpageUtil;

    /** The container data. */
    private Map<String, CmsContainer> m_containers;

    /** The XML content editor handler. */
    private CmsContentEditorHandler m_contentEditorHandler;

    /** The core RPC service instance. */
    private I_CmsCoreServiceAsync m_coreSvc;

    /** The current edit container level. */
    private int m_currentEditLevel = -1;

    /** The prefetched data. */
    private CmsCntPageData m_data;

    /** The DND controller. */
    private CmsCompositeDNDController m_dndController;

    /** The drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /** Edit button position timer. */
    private Timer m_editButtonsPositionTimer;

    /** The current element view. */
    private CmsElementViewInfo m_elementView;

    /** Flag indicating that a content element is being edited. */
    private boolean m_isContentEditing;

    /** The container page load time. */
    private long m_loadTime;

    /** The lock error message. */
    private String m_lockErrorMessage;

    /** The current lock status for the page. */
    private LockStatus m_lockStatus = LockStatus.unknown;

    /** The max container level. */
    private int m_maxContainerLevel;

    /** The model group base element id. */
    private String m_modelGroupElementId;

    /** The browser location at the time the containerpage controller was initialized. */
    private String m_originalUrl;

    /** Flag if the container-page has changed. */
    private boolean m_pageChanged;

    /** The publish lock checker. */
    private CmsPublishLockChecker m_publishLockChecker = new CmsPublishLockChecker(this);

    /** Timer to handle window resize. */
    private Timer m_resizeTimer;

    /** Handler for small elements. */
    private CmsSmallElementsHandler m_smallElementsHandler;

    /** Alternative preview handler for events. */
    private NativePreviewHandler m_previewHandler;

    /**
     * Constructor.<p>
     */
    public CmsContainerpageController() {

        m_originalUrl = Window.Location.getHref();
        INSTANCE = this;
        try {
            m_data = (CmsCntPageData)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getContainerpageService(),
                CmsCntPageData.DICT_NAME);
            m_elementView = m_data.getElementView();
            m_modelGroupElementId = m_data.getModelGroupElementId();
            m_loadTime = m_data.getLoadTime();
            try {
                WebStorageWindow window = WebStorageWindow.of(DomGlobal.window);
                for (Map.Entry<String, String> entry : m_data.getSessionStorageData().entrySet()) {
                    window.sessionStorage.setItem(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                CmsGwtLog.log("can't use webstorage API");
            }
        } catch (SerializationException e) {
            CmsErrorDialog.handleException(
                new Exception(
                    "Deserialization of page data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                    e));
        }
        m_smallElementsHandler = new CmsSmallElementsHandler(getContainerpageService());
        if (m_data != null) {
            m_smallElementsHandler.setEditSmallElements(m_data.isEditSmallElementsInitially(), false);

            m_data.setRpcContext(
                new CmsContainerPageRpcContext(
                    CmsCoreProvider.get().getStructureId(),
                    m_data.getTemplateContextInfo().getCurrentContext()));
        }

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
            serverId = clientId.substring(0, clientId.lastIndexOf(CLIENT_ID_SEPERATOR));
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
     * Adds a handler for container page events.<p>
     *
     * @param handler the handler to add
     */
    public void addContainerpageEventHandler(I_CmsContainerpageEventHandler handler) {

        CmsCoreProvider.get().getEventBus().addHandler(CmsContainerpageEvent.TYPE, handler);
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

                getContainerpageService().addToFavoriteList(getData().getRpcContext(), clientId, this);
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

                getContainerpageService().addToRecentList(getData().getRpcContext(), clientId, this);
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
     * Checks reuse status of a container element, then shows a warning dialog if it is reused, and finally executes an action.
     *
     * @param elementWidget the container element for which to check the reuse status
     * @param action the action to execute after the reuse warning dialog is closed
     */
    public void checkReuse(CmsContainerPageElementPanel elementWidget, Runnable action) {

        Runnable wrappedAction = () -> {
            m_checkedReuse.add(elementWidget.getStructureId());
            action.run();
        };
        if (elementWidget.isReused()
            && CmsCoreProvider.get().isWarnWhenEditingReusedElement()
            && !m_checkedReuse.contains(elementWidget.getStructureId())) {
            CmsUUID id = elementWidget.getStructureId();
            CmsUUID detailId = CmsContainerpageController.get().getData().getDetailId();
            CmsUUID pageId = CmsCoreProvider.get().getStructureId();
            CmsRpcAction<CmsReuseInfo> rpc = new CmsRpcAction<CmsReuseInfo>() {

                @Override
                public void execute() {

                    start(0, true);
                    getContainerpageService().getReuseInfo(pageId, detailId, id, this);
                }

                @Override
                protected void onResponse(CmsReuseInfo result) {

                    stop(false);
                    if (result.getCount() == 0) {
                        wrappedAction.run();
                    } else {
                        CmsReuseInfoDialog dialog = new CmsReuseInfoDialog(result, status -> {
                            if (status.booleanValue()) {
                                wrappedAction.run();
                            }
                        });
                        dialog.show();
                        // without delayed center(), positioning is inconsistent
                        Timer timer = new Timer() {

                            @Override
                            public void run() {

                                dialog.center();
                            }
                        };
                        timer.schedule(100);
                    }
                }
            };
            rpc.execute();
        } else {
            action.run();
        }
    }

    /**
     * Checks for container elements that are no longer present within the DOM.<p>
     */
    public void cleanUpContainers() {

        List<String> removed = new ArrayList<String>();
        for (Entry<String, CmsContainerPageContainer> entry : m_targetContainers.entrySet()) {
            if (!RootPanel.getBodyElement().isOrHasChild(entry.getValue().getElement())) {
                removed.add(entry.getKey());
            }
        }
        for (String containerId : removed) {
            m_targetContainers.remove(containerId);
            m_containers.remove(containerId);
        }
        if (removed.size() > 0) {
            scheduleGalleryUpdate();
        }
    }

    /**
     * Copies an element and asynchronously returns the structure id of the copy.<p>
     *
     * @param id the element id
     * @param callback the callback for the result
     */
    public void copyElement(final String id, final I_CmsSimpleCallback<CmsUUID> callback) {

        CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

            @Override
            public void execute() {

                start(200, false);
                getContainerpageService().copyElement(
                    CmsCoreProvider.get().getStructureId(),
                    new CmsUUID(id),
                    getData().getLocale(),
                    this);
            }

            @Override
            protected void onResponse(CmsUUID result) {

                stop(false);
                callback.execute(result);
            }

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

        final CmsContainer container = m_containers.get(element.getParentTarget().getContainerId());

        m_handler.showPageOverlay();
        CmsRpcAction<CmsCreateElementData> action = new CmsRpcAction<CmsCreateElementData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getContainerpageService().checkCreateNewElement(
                    CmsCoreProvider.get().getStructureId(),
                    getData().getDetailId(),
                    element.getId(),
                    element.getNewType(),
                    container,
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
     * Creates a new resource for drag container elements with the status new and opens the content editor.<p>
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
                    getData().getDetailId(),
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
     * Creates the details object for a custom page editor event based on the given container page element.
     *
     * @param widget a container page element
     * @return the event details object
     */
    public I_EventDetails createEventDetails(CmsContainerPageElementPanel widget) {

        final Element parentContainerElement = CmsDomUtil.getAncestor(
            widget.getElement(),
            CmsContainerElement.CLASS_CONTAINER);

        I_EventDetails details = Js.cast(new JsObject());
        details.setIsPlaceholder(widget.isNew());
        details.setElement(Js.cast(widget.getElement()));
        details.setContainer(Js.cast(parentContainerElement));
        return details;
    }

    /**
     * Creates a new element.<p>
     *
     * @param element the widget belonging to the element which is currently in memory only
     * @param callback the callback to call with the result
     */
    public void createNewElement(
        final CmsContainerPageElementPanel element,
        final AsyncCallback<CmsContainerElement> callback) {

        CmsRpcAction<CmsContainerElement> action = new CmsRpcAction<CmsContainerElement>() {

            @Override
            public void execute() {

                getContainerpageService().createNewElement(
                    CmsCoreProvider.get().getStructureId(),
                    getData().getDetailId(),
                    element.getId(),
                    element.getNewType(),
                    null,
                    getLocale(),
                    this);

            }

            @Override
            protected void onResponse(CmsContainerElement result) {

                callback.onSuccess(result);
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
        reloadElements(new String[] {relatedElementId}, () -> {/*do nothing*/});
    }

    /**
     * Disables the inline editing for all content elements but the given one.<p>
     *
     * @param notThisOne the content element not to disable
     */
    public void disableInlineEditing(CmsContainerPageElementPanel notThisOne) {

        removeEditButtonsPositionTimer();
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
     * Replaces all element instances of the original element with the new element within the former copy model.<p>
     *
     * @param originalElementId the original element id
     * @param modelGroupParent the model group parent element
     * @param elementData the replace element data
     */
    public void executeCopyModelReplace(
        String originalElementId,
        Element modelGroupParent,
        CmsContainerElementData elementData) {

        String serverId = getServerId(originalElementId);
        for (CmsContainerPageContainer cont : m_targetContainers.values()) {
            if (modelGroupParent.isOrHasChild(cont.getElement())) {
                // look for instances of the original element
                for (Widget child : cont) {
                    if ((child instanceof CmsContainerPageElementPanel)
                        && ((CmsContainerPageElementPanel)child).getId().startsWith(serverId)) {
                        CmsContainerPageElementPanel replacer = null;
                        String elementContent = elementData.getContents().get(cont.getContainerId());
                        if ((elementContent != null) && (elementContent.trim().length() > 0)) {
                            try {
                                replacer = getContainerpageUtil().createElement(elementData, cont, false);
                                cont.insert(replacer, cont.getWidgetIndex(child));
                                child.removeFromParent();
                                initializeSubContainers(replacer);
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fires an event on the core event bus.<p>
     *
     * @param event the event to fire
     */
    public void fireEvent(CmsContainerpageEvent event) {

        CmsCoreProvider.get().getEventBus().fireEvent(event);

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
    public CmsContainer getContainer(String containerName) {

        return m_containers.get(containerName);
    }

    /**
     * Gets the container element widget to which the given element belongs, or Optional.absent if none could be found.<p>
     *
     * @param element the element for which the container element widget should be found
     *
     * @return the container element widget, or Optional.absent if none can be found
     */
    public Optional<CmsContainerPageElementPanel> getContainerElementWidgetForElement(Element element) {

        final Element parentContainerElement = CmsDomUtil.getAncestor(
            element,
            I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
        if (parentContainerElement == null) {
            return Optional.absent();
        }
        final List<CmsContainerPageElementPanel> result = Lists.newArrayList();
        processPageContent(new I_PageContentVisitor() {

            public boolean beginContainer(String name, CmsContainer container) {

                // we don't need to look into the container if we have already found our container element
                return result.isEmpty();
            }

            public void endContainer() {

                // do nothing
            }

            public void handleElement(CmsContainerPageElementPanel current) {

                if ((current.getElement() == parentContainerElement) && result.isEmpty()) {
                    result.add(current);
                }
            }
        });
        if (result.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.fromNullable(result.get(0));
        }
    }

    /**
     * Gets the container info to send to the gallery service.
     *
     * @return the container info to send to the gallery service
     */
    public CmsGalleryContainerInfo getContainerInfoForGalleries() {

        if (m_targetContainers != null) {
            HashSet<CmsGalleryContainerInfo.Item> items = new HashSet<>();
            for (CmsContainerPageContainer cont : m_targetContainers.values()) {
                items.add(new CmsGalleryContainerInfo.Item(cont.getContainerType(), cont.getConfiguredWidth()));
            }
            return new CmsGalleryContainerInfo(items);
        }
        return null;
    }

    /**
     * Returns the container-page RPC service.<p>
     *
     * @return the container-page service
     */
    public I_CmsContainerpageServiceAsync getContainerpageService() {

        if (m_containerpageService == null) {
            m_containerpageService = GWT.create(I_CmsContainerpageService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.containerpage.CmsContainerpageService.gwt");
            ((ServiceDefTarget)m_containerpageService).setServiceEntryPoint(serviceUrl);
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
    public Map<String, CmsContainer> getContainers() {

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

        Map<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> result = new HashMap<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer>();
        for (Entry<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> entry : m_targetContainers.entrySet()) {
            if (entry.getValue().isEditable()
                && (!isDetailPage() || (entry.getValue().isDetailOnly() || entry.getValue().isDetailView()))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
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
     * Returns the delete options for the given content element.<p>
     *
     * @param clientId the content id
     * @param callback the callback to execute
     */
    public void getDeleteOptions(final String clientId, final I_CmsSimpleCallback<CmsDialogOptionsAndInfo> callback) {

        CmsRpcAction<CmsDialogOptionsAndInfo> action = new CmsRpcAction<CmsDialogOptionsAndInfo>() {

            @Override
            public void execute() {

                getContainerpageService().getDeleteOptions(
                    clientId,
                    getData().getRpcContext().getPageStructureId(),
                    getData().getRequestParams(),
                    this);
            }

            @Override
            protected void onResponse(CmsDialogOptionsAndInfo result) {

                callback.execute(result);
            }
        };
        action.execute();
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
     * Returns the edit options for the given content element.<p>
     *
     * @param clientId the content id
     * @param isListElement in case a list element, not a container element is about to be edited
     * @param callback the callback to execute
     */
    public void getEditOptions(
        final String clientId,
        final boolean isListElement,
        final I_CmsSimpleCallback<CmsDialogOptionsAndInfo> callback) {

        CmsRpcAction<CmsDialogOptionsAndInfo> action = new CmsRpcAction<CmsDialogOptionsAndInfo>() {

            @Override
            public void execute() {

                getContainerpageService().getEditOptions(
                    clientId,
                    getData().getRpcContext().getPageStructureId(),
                    getData().getRequestParams(),
                    isListElement,
                    this);
            }

            @Override
            protected void onResponse(CmsDialogOptionsAndInfo result) {

                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Requests the data for a container element specified by the client id for drag and drop from a container. The data will be provided to the given call-back function.<p>
     *
     * @param clientId the element id
     * @param containerId the id of the container from which the element is being dragged
     * @param alwaysCopy <code>true</code> in case reading data for a clipboard element used as a copy group
     * @param callback the call-back to execute with the requested data
     */
    public void getElementForDragAndDropFromContainer(
        final String clientId,
        final String containerId,
        boolean alwaysCopy,
        final I_CmsSimpleCallback<CmsContainerElementData> callback) {

        SingleElementAction action = new SingleElementAction(clientId, alwaysCopy, callback);
        action.setDndContainer(containerId);
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
     * Requests the element settings config data for a container element specified by the client id. The data will be provided to the given call-back function.<p>
     *
     * @param clientId the element id
     * @param containerId the parent container id
     * @param callback the call-back to execute with the requested data
     */
    public void getElementSettingsConfig(
        final String clientId,
        final String containerId,
        final I_CmsSimpleCallback<CmsElementSettingsConfig> callback) {

        CmsRpcAction<CmsElementSettingsConfig> action = new CmsRpcAction<CmsElementSettingsConfig>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(100, true);
                getContainerpageService().getElementSettingsConfig(
                    getData().getRpcContext(),
                    clientId,
                    containerId,
                    getPageState(),
                    getLocale(),
                    this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsElementSettingsConfig result) {

                if (result != null) {
                    callback.execute(result);
                }
                stop(false);
            }
        };
        action.execute();
    }

    /**
     * Returns the current element view.<p>
     *
     * @return the current element view
     */
    public CmsElementViewInfo getElementView() {

        return m_elementView;
    }

    /**
     * Retrieves a container element with a given set of settings.<p>
     *
     * @param clientId the id of the container element
     * @param settings the set of settings
     *
     * @param callback the callback which should be executed when the element has been loaded
     */
    public void getElementWithSettings(
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
                    getData().getRpcContext(),
                    getData().getDetailId(),
                    getRequestParams(),
                    clientId,
                    settings,
                    getPageState(),
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
     * Returns the time off page load.<p>
     *
     * @return the time stamp
     */
    public long getLoadTime() {

        return m_loadTime;
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
     * Returns the model group base element id.<p>
     *
     * @return the model group base element id
     */
    public String getModelGroupElementId() {

        return m_modelGroupElementId;
    }

    /**
     * Collects all container elements which are model groups.<p>
     *
     * @return the list of model group container elements
     */
    public List<CmsContainerPageElementPanel> getModelGroups() {

        final List<CmsContainerPageElementPanel> result = Lists.newArrayList();

        processPageContent(new I_PageContentVisitor() {

            public boolean beginContainer(String name, CmsContainer container) {

                return true;
            }

            public void endContainer() {

                // do nothing
            }

            public void handleElement(CmsContainerPageElementPanel element) {

                if (element.isModelGroup()) {
                    result.add(element);
                }
            }
        });
        return result;
    }

    /**
     * Returns the element data for a resource type representing a new element.<p>
     *
     * @param resourceType the resource type name
     * @param callback the callback to execute with the new element data
     */
    public void getNewElement(final String resourceType, final I_CmsSimpleCallback<CmsContainerElementData> callback) {

        CmsRpcAction<CmsContainerElementData> action = new CmsRpcAction<CmsContainerElementData>() {

            @Override
            public void execute() {

                getContainerpageService().getNewElementData(
                    getData().getRpcContext(),
                    getData().getDetailId(),
                    getRequestParams(),
                    resourceType,
                    getPageState(),
                    getLocale(),
                    this);
            }

            @Override
            protected void onResponse(CmsContainerElementData result) {

                m_elements.put(result.getClientId(), result);
                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Fetches the options for creating a new element from the edit handler.<p>
     *
     * @param clientId the client id of the element
     * @param callback the callback which is called with the result
     */
    public void getNewOptions(final String clientId, final I_CmsSimpleCallback<CmsDialogOptionsAndInfo> callback) {

        CmsRpcAction<CmsDialogOptionsAndInfo> action = new CmsRpcAction<CmsDialogOptionsAndInfo>() {

            @Override
            public void execute() {

                getContainerpageService().getNewOptions(
                    clientId,
                    getData().getRpcContext().getPageStructureId(),
                    getData().getRequestParams(),
                    this);
            }

            @Override
            protected void onResponse(CmsDialogOptionsAndInfo result) {

                callback.execute(result);
            }
        };

        action.execute();
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
    public CmsContainer getSerializedContainer(String data) throws SerializationException {

        return (CmsContainer)CmsRpcPrefetcher.getSerializedObjectFromString(getContainerpageService(), data);
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
     * Gets the view with the given id.<p>
     *
     * @param value the view id as a string
     *
     * @return the view with the given id, or null if no such view is available
     */
    public CmsElementViewInfo getView(String value) {

        for (CmsElementViewInfo info : m_data.getElementViews()) {
            if (info.getElementViewId().toString().equals(value)) {
                return info;
            }
        }
        return null;
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
            I_EventDetails details = createEventDetails(element);
            element.removeFromParent();
            cleanUpContainers();
            setPageChanged();
            sendElementDeleted(details);
            return;
        }
        checkElementReferences(element, new AsyncCallback<CmsRemovedElementStatus>() {

            public void onFailure(Throwable caught) {

                // ignore, will never be executed

            }

            public void onSuccess(CmsRemovedElementStatus status) {

                boolean showDeleteCheckbox = status.isDeletionCandidate();
                ElementDeleteMode deleteMode = status.getElementDeleteMode();
                if (deleteMode == null) {
                    deleteMode = getData().getDeleteMode();
                }
                CmsConfirmRemoveDialog removeDialog = new CmsConfirmRemoveDialog(
                    status.getElementInfo(),
                    showDeleteCheckbox,
                    deleteMode,
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
                                nextActions = new Runnable[] {new Runnable() {

                                    public void run() {

                                        deleteAction.execute();
                                    }
                                }};
                            }
                            I_CmsDropContainer container = element.getParentTarget();
                            I_EventDetails details = createEventDetails(element);
                            element.removeFromParent();
                            if (container instanceof CmsContainerPageContainer) {
                                ((CmsContainerPageContainer)container).checkEmptyContainers();
                            }
                            cleanUpContainers();
                            sendElementDeleted(details);
                            setPageChanged(nextActions);
                        }
                    });
                removeDialog.center();
            }

        });
    }

    /**
     * Calls the edit handler to handle the delete action.<p>
     *
     * @param clientId the content client id
     * @param deleteOption the selected delete option
     * @param callback the callback to execute after the delete
     */
    public void handleDelete(
        final String clientId,
        final String deleteOption,
        final I_CmsSimpleCallback<Void> callback) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                getContainerpageService().handleDelete(
                    clientId,
                    deleteOption,
                    getData().getRpcContext().getPageStructureId(),
                    getData().getRequestParams(),
                    this);
            }

            @Override
            protected void onResponse(Void result) {

                if (callback != null) {
                    callback.execute(result);
                }
            }
        };
        action.execute();
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

        removeEditButtonsPositionTimer();
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

        if (getData().getDetailId() != null) {
            CmsEmbeddedAction.PARAMS.put(CmsGwtConstants.PARAM_ADE_DETAIL_ID, "" + getData().getDetailId());
        }

        Window.addResizeHandler(new ResizeHandler() {

            public void onResize(ResizeEvent event) {

                CmsContainerpageController.this.onResize();
            }
        });
        m_containerpageUtil = containerpageUtil;
        m_handler = handler;
        m_contentEditorHandler = contentEditorHandler;
        m_dndHandler = dndHandler;
        m_cntDndController = m_dndHandler.getController();

        m_elements = new HashMap<String, CmsContainerElementData>();
        m_newElements = new HashMap<String, CmsContainerElementData>();
        m_containers = new HashMap<String, CmsContainer>();
        if (m_data == null) {
            m_handler.m_editor.disableEditing(Messages.get().key(Messages.ERR_READING_CONTAINER_PAGE_DATA_0));
            CmsErrorDialog dialog = new CmsErrorDialog(
                Messages.get().key(Messages.ERR_READING_CONTAINER_PAGE_DATA_0),
                null);
            dialog.center();
            return;
        }
        // ensure any embedded flash players are set opaque so UI elements may be placed above them
        CmsDomUtil.fixFlashZindex(RootPanel.getBodyElement());
        m_targetContainers = m_containerpageUtil.consumeContainers(m_containers, RootPanel.getBodyElement());
        updateContainerLevelInfo();
        resetEditButtons();
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            public void onPreviewNativeEvent(NativePreviewEvent event) {

                previewNativeEvent(event);
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
        AsyncCallback<Void> doNothing = new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {

                // nothing to do
            }

            public void onSuccess(Void result) {

                // nothing to do
            }
        };
        getContainerpageService().setLastPage(CmsCoreProvider.get().getStructureId(), m_data.getDetailId(), doNothing);

        // check if there is already a history item available
        String historyToken = History.getToken();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(historyToken)) {
            m_contentEditorHandler.openEditorForHistory(historyToken);
        }

        updateGalleryData(false, null);
        addContainerpageEventHandler(event -> {
            updateDetailPreviewStyles();
        });
        updateDetailPreviewStyles();
        updateButtonsForCurrentView();
        startPublishLockCheck();
    }

    /**
     * Checks for element sub containers.<p>
     *
     * @param containerElement the container element
     */
    public void initializeSubContainers(CmsContainerPageElementPanel containerElement) {

        int containerCount = m_targetContainers.size();
        m_targetContainers.putAll(m_containerpageUtil.consumeContainers(m_containers, containerElement.getElement()));
        updateContainerLevelInfo();
        if (m_targetContainers.size() > containerCount) {
            // in case new containers have been added, the gallery data needs to be updated
            scheduleGalleryUpdate();
        }
    }

    /**
     * Returns if the given container is editable.<p>
     *
     * @param dragParent the parent container
     *
     * @return <code>true</code> if the given container is editable
     */
    public boolean isContainerEditable(I_CmsDropContainer dragParent) {

        boolean isSubElement = dragParent instanceof CmsGroupContainerElementPanel;
        boolean isContainerEditable = dragParent.isEditable()
            && (isSubElement || !isDetailPage() || dragParent.isDetailView() || dragParent.isDetailOnly());
        return isContainerEditable;
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
     * Returns if this page displays a detail view.<p>
     *
     * @return <code>true</code> if this page displays a detail view
     */
    public boolean isDetailPage() {

        return m_data.getDetailId() != null;
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
     * Checks whether the given element should be inline editable.<p>
     *
     * @param element the element
     * @param dragParent the element parent
     *
     * @return <code>true</code> if the element should be inline editable
     */
    public boolean isInlineEditable(CmsContainerPageElementPanel element, I_CmsDropContainer dragParent) {

        CmsUUID elemView = element.getElementView();
        return !getData().isUseClassicEditor()
            && CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason())
            && hasActiveSelection()
            && matchRootView(elemView)
            && isContainerEditable(dragParent)
            && matchesCurrentEditLevel(dragParent)
            && (getData().isModelGroup() || !element.hasModelGroupParent())
            && (!(dragParent instanceof CmsGroupContainerElementPanel) || isGroupcontainerEditing());
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

                Map<String, String> params = new HashMap<>();
                if (getData().getDetailId() != null) {
                    params.put(CmsGwtConstants.PARAM_ADE_DETAIL_ID, "" + getData().getDetailId());
                }
                getCoreService().getContextMenuEntries(structureId, context, params, this);
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
                    getData().getDetailId(),
                    getPageState(),
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
                    getData().getDetailId(),
                    getPageState(),
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
     * @param callback the callback to execute
     */
    public void lockContainerpage(final I_CmsSimpleCallback<Boolean> callback) {

        if (m_lockStatus == LockStatus.locked) {
            callback.execute(Boolean.TRUE);
        } else if (m_lockStatus == LockStatus.failed) {
            callback.execute(Boolean.FALSE);
        } else {
            I_CmsSimpleCallback<String> call = new I_CmsSimpleCallback<String>() {

                public void execute(String lockError) {

                    if (lockError == null) {
                        onLockSuccess();
                        callback.execute(Boolean.TRUE);
                    } else {
                        onLockFail(lockError);
                        callback.execute(Boolean.FALSE);
                    }
                }
            };

            if (getData().getDetailContainerPage() != null) {
                CmsCoreProvider.get().lockOrReturnError(getData().getDetailContainerPage(), getLoadTime(), call);
            } else {
                CmsCoreProvider.get().lockOrReturnError(CmsCoreProvider.get().getStructureId(), getLoadTime(), call);
            }
        }
    }

    /**
     * Returns true if the view with the given view id and the current view have the same root view.<p>
     *
     * @param viewIdFromElement the id of a view
     * @return true if the root view of the id matches the root view of the current view
     */
    public boolean matchRootView(CmsUUID viewIdFromElement) {

        if (viewIdFromElement == null) {
            viewIdFromElement = CmsUUID.getNullUUID();
        }
        CmsElementViewInfo viewFromElement = getView(viewIdFromElement.toString());
        return (viewFromElement != null) && viewFromElement.getRootViewId().equals(m_elementView.getRootViewId());
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
        unlockContainerpage();
    }

    /**
     * Calls the edit handler to prepare the given content element for editing.<p>
     *
     * @param clientId the element id
     * @param editOption the selected edit option
     * @param callback the callback to execute
     */
    public void prepareForEdit(
        final String clientId,
        final String editOption,
        final I_CmsSimpleCallback<CmsUUID> callback) {

        CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

            @Override
            public void execute() {

                getContainerpageService().prepareForEdit(
                    clientId,
                    editOption,
                    getData().getRpcContext().getPageStructureId(),
                    getData().getRequestParams(),
                    this);
            }

            @Override
            protected void onResponse(CmsUUID result) {

                callback.execute(result);
            }
        };
        action.execute();
    }

    /**
     * Reinitializes the buttons in the container element menus.<p>
     */
    public void reinitializeButtons() {

        if (isGroupcontainerEditing()) {
            m_groupEditor.reinitializeButtons();
        } else {
            List<CmsContainerPageElementPanel> elemWidgets = getAllContainerPageElements(true);

            for (CmsContainerPageElementPanel elemWidget : elemWidgets) {
                if (requiresOptionBar(elemWidget, elemWidget.getParentTarget())) {
                    getContainerpageUtil().addOptionBar(elemWidget);
                } else {
                    // otherwise remove any present option bar
                    elemWidget.setElementOptionBar(null);
                }
                elemWidget.showEditableListButtons();
            }
        }
    }

    /**
     * Re-initializes the inline editing.<p>
     */
    public void reInitInlineEditing() {

        removeEditButtonsPositionTimer();
        if ((m_targetContainers == null) || getData().isUseClassicEditor()) {
            // if the target containers are not initialized yet or classic editor is set, don't do anything
            return;
        }
        if (isGroupcontainerEditing()) {
            for (Widget element : m_groupEditor.getGroupContainerWidget()) {
                if (((element instanceof CmsContainerPageElementPanel)
                    && isInlineEditable(
                        (CmsContainerPageElementPanel)element,
                        m_groupEditor.getGroupContainerWidget()))) {
                    ((CmsContainerPageElementPanel)element).initInlineEditor(this);
                }
            }
        } else {
            for (CmsContainerPageContainer container : m_targetContainers.values()) {
                // first remove inline editors
                for (Widget element : container) {
                    if ((element instanceof CmsContainerPageElementPanel)) {
                        ((CmsContainerPageElementPanel)element).removeInlineEditor();
                    }
                }

                // add inline editors only on suitable elements
                if (isContainerEditable(container) && matchesCurrentEditLevel(container)) {
                    for (Widget element : container) {
                        if ((element instanceof CmsContainerPageElementPanel)
                            && isInlineEditable((CmsContainerPageElementPanel)element, container)) {
                            ((CmsContainerPageElementPanel)element).initInlineEditor(this);
                        }
                    }
                }
            }
        }
    }

    /**
     * Reloads the content for the given elements and related elements.
     *
     * @param ids the element ids
     * @param callback the callback to execute after the reload
     */
    public void reloadElements(Collection<String> ids, I_ReloadHandler callback) {

        Set<String> related = new HashSet<String>();
        for (String id : ids) {
            related.addAll(getRelatedElementIds(id));
        }
        if (!related.isEmpty()) {
            ReloadElementAction action = new ReloadElementAction(related, callback);
            action.execute();
        }
    }

    /**
     * Reloads the content for the given elements and related elements.
     *
     * @param ids the element ids
     * @param callback the callback to execute after the reload
     */
    public void reloadElements(Collection<String> ids, Runnable callback) {

        reloadElements(ids, new I_ReloadHandler() {

            public void finish() {

                if (callback != null) {
                    callback.run();
                }
            }

            public void onReload(CmsContainerPageElementPanel a, CmsContainerPageElementPanel b) {

                // ignore
            }
        });

    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     *
     * Call this if the element content has changed.<p>
     *
     * @param ids the element ids
     * @param callback the callback to execute after the reload
     */
    public void reloadElements(String[] ids, I_ReloadHandler callback) {

        Set<String> related = new HashSet<String>();
        for (int i = 0; i < ids.length; i++) {
            related.addAll(getRelatedElementIds(ids[i]));
        }
        if (!related.isEmpty()) {
            ReloadElementAction action = new ReloadElementAction(related, callback);
            action.execute();
        } else {
            callback.finish();
        }
    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     *
     * Call this if the element content has changed.<p>
     *
     * @param ids the element ids
     * @param callback the callback to execute after the reload
     */
    public void reloadElements(String[] ids, Runnable callback) {

        reloadElements(ids, new I_ReloadHandler() {

            @Override
            public void finish() {

                if (callback != null) {
                    callback.run();
                }
            }

            @Override
            public void onReload(CmsContainerPageElementPanel oldElement, CmsContainerPageElementPanel newElement) {

            }
        });

    }

    /**
     * Reloads a container page element with a new set of settings.<p>
     *
     * @param elementWidget the widget of the container page element which should be reloaded
     * @param clientId the id of the container page element which should be reloaded
     * @param settings the new set of settings
     * @param reloadHandler the handler to call with the reloaded element
     */
    public void reloadElementWithSettings(
        final org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel elementWidget,
        final String clientId,
        final Map<String, String> settings,
        final I_ReloadHandler reloadHandler) {

        final I_CmsSimpleCallback<CmsContainerElementData> callback = new I_CmsSimpleCallback<CmsContainerElementData>() {

            public void execute(CmsContainerElementData newElement) {

                try {
                    final CmsContainerPageElementPanel replacement = replaceContainerElement(elementWidget, newElement);
                    resetEditButtons();
                    addToRecentList(newElement.getClientId(), null);
                    reloadHandler.onReload(elementWidget, replacement);
                    reloadHandler.finish();
                } catch (Exception e) {
                    // should never happen
                    CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                }
            }
        };

        if (!isGroupcontainerEditing()) {

            lockContainerpage(new I_CmsSimpleCallback<Boolean>() {

                public void execute(Boolean arg) {

                    if (arg.booleanValue()) {
                        CmsRpcAction<CmsContainerElementData> action = new CmsRpcAction<CmsContainerElementData>() {

                            @Override
                            public void execute() {

                                start(500, true);
                                getContainerpageService().saveElementSettings(
                                    getData().getRpcContext(),
                                    getData().getDetailId(),
                                    getRequestParams(),
                                    clientId,
                                    settings,
                                    getPageState(),
                                    getLocale(),
                                    this);
                            }

                            @Override
                            protected void onResponse(CmsContainerElementData result) {

                                stop(false);
                                CmsContainerpageController.get().fireEvent(
                                    new CmsContainerpageEvent(EventType.pageSaved));
                                setPageChanged(false, false);
                                if (result != null) {
                                    // cache the loaded element
                                    m_elements.put(result.getClientId(), result);
                                    setLoadTime(Long.valueOf(result.getLoadTime()));
                                }
                                callback.execute(result);
                            }
                        };
                        action.execute();
                    }
                }
            });

        } else {
            getElementWithSettings(clientId, settings, callback);
        }
    }

    /**
     * Reloads the page.<p>
     */
    public void reloadPage() {

        Timer timer = new Timer() {

            @Override
            public void run() {

                Window.Location.reload();
            }
        };

        timer.schedule(150);

    }

    /**
     * Removes the given container element from its parent container.<p>
     *
     * @param dragElement the element to remove
     */
    public void removeElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel dragElement) {

        ElementRemoveMode removeMode = isConfirmRemove()
        ? ElementRemoveMode.confirmRemove
        : ElementRemoveMode.saveAndCheckReferences;
        removeElement(dragElement, removeMode);
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

            I_CmsDropContainer container = dragElement.getParentTarget();
            switch (removeMode) {
                case saveAndCheckReferences:
                    I_EventDetails details = createEventDetails(dragElement);
                    dragElement.removeFromParent();
                    if (container instanceof CmsContainerPageContainer) {
                        ((CmsContainerPageContainer)container).checkEmptyContainers();
                    }
                    cleanUpContainers();
                    Runnable checkReferencesAction = new Runnable() {

                        public void run() {

                            checkReferencesToRemovedElement(id);
                        }
                    };
                    sendElementDeleted(details);
                    setPageChanged(checkReferencesAction);
                    break;
                case confirmRemove:
                    handleConfirmRemove(dragElement);
                    break;
                case silent:
                default:
                    I_EventDetails details2 = createEventDetails(dragElement);
                    dragElement.removeFromParent();
                    if (container instanceof CmsContainerPageContainer) {
                        ((CmsContainerPageContainer)container).checkEmptyContainers();
                    }
                    cleanUpContainers();
                    sendElementDeleted(details2);
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
        CmsContainerPageElementPanel containerElement,
        CmsContainerElementData elementData)
    throws Exception {

        I_CmsDropContainer parentContainer = containerElement.getParentTarget();
        String containerId = parentContainer.getContainerId();
        CmsContainerPageElementPanel replacer = null;
        String elementContent = elementData.getContents().get(containerId);
        if ((elementContent != null) && (elementContent.trim().length() > 0)) {
            replacer = getContainerpageUtil().createElement(elementData, parentContainer, false);

            if (containerElement.isNew()) {
                // if replacing element data has the same structure id, keep the 'new' state by setting the new type property
                // this should only be the case when editing settings of a new element that has not been created in the VFS yet
                String id = getServerId(containerElement.getId());
                if (elementData.getClientId().startsWith(id)) {
                    replacer.setNewType(containerElement.getNewType());
                }
            }
            replacer.setCreateNew(containerElement.isCreateNew());
            // replacer.setModelGroup(containerElement.isModelGroup());
            if (isGroupcontainerEditing() && (containerElement.getInheritanceInfo() != null)) {
                // in case of inheritance container editing, keep the inheritance info
                replacer.setInheritanceInfo(containerElement.getInheritanceInfo());
                // set the proper element options
                CmsInheritanceContainerEditor.getInstance().setOptionBar(replacer);
            }
            parentContainer.insert(replacer, parentContainer.getWidgetIndex(containerElement));
            containerElement.removeFromParent();
            initializeSubContainers(replacer);
        }
        cleanUpContainers();
        return replacer;
    }

    /**
     * Replaces the given element with another content while keeping it's settings.<p>
     *
     * @param elementWidget the element to replace
     * @param elementId the id of the replacing content
     * @param callback  the callback to execute after the element is replaced
     */
    public void replaceElement(
        final CmsContainerPageElementPanel elementWidget,
        final String elementId,
        I_ReloadHandler handler) {

        final CmsRpcAction<CmsContainerElementData> action = new CmsRpcAction<CmsContainerElementData>() {

            @Override
            public void execute() {

                start(500, true);
                getContainerpageService().replaceElement(
                    getData().getRpcContext(),
                    getData().getDetailId(),
                    getRequestParams(),
                    elementWidget.getId(),
                    elementId,
                    getPageState(),
                    getLocale(),
                    this);
            }

            @Override
            protected void onResponse(CmsContainerElementData result) {

                stop(false);

                if (result != null) {
                    // cache the loaded element
                    m_elements.put(result.getClientId(), result);
                    try {
                        CmsContainerPageElementPanel replacer = replaceContainerElement(elementWidget, result);
                        resetEditButtons();
                        addToRecentList(result.getClientId(), null);
                        setPageChanged(new Runnable() {

                            public void run() {

                                if (handler != null) {
                                    handler.onReload(elementWidget, replacer);
                                    handler.finish();
                                }
                            }
                        });
                    } catch (Exception e) {
                        // should never happen
                        CmsDebugLog.getInstance().printLine(e.getLocalizedMessage());
                    }
                }
            }
        };

        if (!isGroupcontainerEditing()) {

            lockContainerpage(new I_CmsSimpleCallback<Boolean>() {

                public void execute(Boolean arg) {

                    if (arg.booleanValue()) {
                        action.execute();
                    }
                }
            });

        } else {
            action.execute();
        }
    }

    /**
     * Replaces the given element with another content while keeping it's settings.<p>
     *
     * @param elementWidget the element to replace
     * @param elementId the id of the replacing content
     * @param callback  the callback to execute after the element is replaced
     */
    public void replaceElement(
        final CmsContainerPageElementPanel elementWidget,
        final String elementId,
        Runnable callback) {

        replaceElement(elementWidget, elementId, new I_ReloadHandler() {

            @Override
            public void finish() {

                if (callback != null) {
                    callback.run();
                }
            }

            @Override
            public void onReload(CmsContainerPageElementPanel oldElement, CmsContainerPageElementPanel newElement) {

                // do nothing
            }
        });

    }

    /**
     * Checks whether the given element should display the option bar.<p>
     *
     * @param element the element
     * @param dragParent the element parent
     *
     * @return <code>true</code> if the given element should display the option bar
     */
    public boolean requiresOptionBar(CmsContainerPageElementPanel element, I_CmsDropContainer dragParent) {

        return element.hasViewPermission()
            && (!element.hasModelGroupParent() || getData().isModelGroup())
            && (matchRootView(element.getElementView())
                || isGroupcontainerEditing()
                || shouldShowModelgroupOptionBar(element))
            && isContainerEditable(dragParent)
            && matchesCurrentEditLevel(dragParent);
    }

    /**
     * Resets all edit buttons an there positions.<p>
     */
    public void resetEditButtons() {

        removeEditButtonsPositionTimer();
        m_editButtonsPositionTimer = new Timer() {

            /** Timer run counter. */
            private int m_timerRuns;

            @Override
            public void run() {

                for (org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer container : m_targetContainers.values()) {
                    container.showEditableListButtons();
                    container.updateOptionBars();
                }
                if (m_timerRuns > 3) {
                    cancel();
                }
                m_timerRuns++;
            }
        };
        m_editButtonsPositionTimer.scheduleRepeating(100);
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
            CmsRpcAction<Long> action = new CmsRpcAction<Long>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    if (getData().getDetailContainerPage() != null) {
                        getContainerpageService().saveDetailContainers(
                            getData().getDetailId(),
                            getData().getDetailContainerPage(),
                            getPageContent(),
                            this);
                    } else {
                        getContainerpageService().saveContainerpage(
                            CmsCoreProvider.get().getStructureId(),
                            getPageContent(),
                            this);
                    }
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Long result) {

                    setLoadTime(result);
                    CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_SAVED_0));
                    CmsContainerpageController.get().fireEvent(new CmsContainerpageEvent(EventType.pageSaved));
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
            CmsRpcAction<Long> action = new CmsRpcAction<Long>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    if (getData().getDetailContainerPage() != null) {
                        getContainerpageService().saveDetailContainers(
                            getData().getDetailId(),
                            getData().getDetailContainerPage(),
                            getPageContent(),
                            this);
                    } else {
                        getContainerpageService().saveContainerpage(
                            CmsCoreProvider.get().getStructureId(),
                            getPageContent(),
                            this);
                    }
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Long result) {

                    setLoadTime(result);
                    CmsNotification.get().send(Type.NORMAL, Messages.get().key(Messages.GUI_NOTIFICATION_PAGE_SAVED_0));
                    CmsContainerpageController.get().fireEvent(new CmsContainerpageEvent(EventType.pageSaved));
                    setPageChanged(false, true);
                    Window.Location.assign(targetUri);
                }
            };
            action.execute();
        }
    }

    /**
     * Saves the clipboard tab  index selected by the user.<p>
     *
     * @param tabIndex the tab index
     */
    public void saveClipboardTab(final int tabIndex) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(1, false);
                getContainerpageService().saveClipboardTab(tabIndex, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
            }
        };
        action.execute();
    }

    /**
     * Saves the current state of the container-page.<p>
     *
     * @param afterSaveActions the actions to execute after saving
     */
    public void saveContainerpage(final Runnable... afterSaveActions) {

        if (hasPageChanged()) {
            final CmsRpcAction<Long> action = new CmsRpcAction<Long>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    start(500, true);
                    if (getData().getDetailContainerPage() != null) {
                        getContainerpageService().saveDetailContainers(
                            getData().getDetailId(),
                            getData().getDetailContainerPage(),
                            getPageContent(),
                            this);
                    } else {
                        getContainerpageService().saveContainerpage(
                            CmsCoreProvider.get().getStructureId(),
                            getPageContent(),
                            this);
                    }
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Long result) {

                    setLoadTime(result);
                    stop(false);
                    setPageChanged(false, false);
                    CmsContainerpageController.get().fireEvent(new CmsContainerpageEvent(EventType.pageSaved));
                    for (Runnable afterSaveAction : afterSaveActions) {
                        afterSaveAction.run();
                    }
                }
            };
            if (getData().getDetailContainerPage() != null) {
                action.execute();
            } else {
                lockContainerpage(new I_CmsSimpleCallback<Boolean>() {

                    public void execute(Boolean arg) {

                        if (arg.booleanValue()) {
                            action.execute();
                        }
                    }
                });
            }
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

                getContainerpageService().saveFavoriteList(clientIds, CmsCoreProvider.get().getUri(), this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(Void result) {

                CmsNotification.get().send(
                    Type.NORMAL,
                    Messages.get().key(Messages.GUI_NOTIFICATION_FAVORITES_SAVED_0));
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

                    start(0, true);
                    getContainerpageService().saveGroupContainer(
                        getData().getRpcContext(),
                        getData().getDetailId(),
                        getRequestParams(),
                        groupContainer,
                        getPageState(),
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(CmsGroupContainerSaveResult saveResult) {

                    stop(false);
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

                    start(0, true);
                    getContainerpageService().saveInheritanceContainer(
                        CmsCoreProvider.get().getStructureId(),
                        getData().getDetailId(),
                        inheritanceContainer,
                        getPageState(),
                        getLocale(),
                        this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(Map<String, CmsContainerElementData> result) {

                    stop(false);
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
     * Sends the ocDragFinished event.
     *
     * @param dragElement the drag element
     * @param target the target element
     * @param isNew true if the dragged element is new
     */
    public void sendDragFinished(Element dragElement, Element target, boolean isNew) {

        I_EventDetails details = Js.cast(new JsObject());
        if (dragElement != null) {
            details.setElement(Js.cast(dragElement));
        }
        if (target != null) {
            final Element parentContainerElement = CmsDomUtil.getAncestor(target, CmsContainerElement.CLASS_CONTAINER);
            details.setContainer(Js.cast(parentContainerElement));
        }
        if (isNew) {
            details.setIsPlaceholder(isNew);
        }
        sendEvent(EVENT_DRAG_FINISHED, details);
        I_EventDetails details2 = copyEventDetails(details);
        details2.setType("finish");
        sendEvent(EVENT_DRAG, details2);
    }

    /**
     * Sends the ocDragStarted event.
     *
     * @param isNew true if the user started dragging a new element
     */
    public void sendDragStarted(boolean isNew) {

        I_EventDetails details = Js.cast(new JsObject());
        details.setIsPlaceholder(isNew);
        sendEvent(EVENT_DRAG_STARTED, details);
        I_EventDetails details2 = copyEventDetails(details);
        details2.setType("start");
        sendEvent(EVENT_DRAG, details2);
    }

    /**
     * Sends the custom 'ocElementAdded' event for added container elements.
     *
     * @param widget the container element
     */
    public void sendElementAdded(CmsContainerPageElementPanel widget) {

        if (widget != null) {
            I_EventDetails details = createEventDetails(widget);
            sendEvent(EVENT_ELEMENT_ADDED, details);
            sendEvent(EVENT_ELEMENT_MODIFIED, addTypeToDetails(details, "add"));
        }
    }

    /**
     * Sends the custom 'ocElementDeleted' event for a deleted container element.
     *
     * @param details the event details
     */
    public void sendElementDeleted(I_EventDetails details) {

        sendEvent(EVENT_ELEMENT_DELETED, details);
        sendEvent(EVENT_ELEMENT_MODIFIED, addTypeToDetails(details, "delete"));
    }

    /**
     * Sends the custom 'ocElementEditedContent' event for an edited container element.
     *
     * @param widget the container element
     * @param replacedElement the element that was formerly on the page before the user edited it
     * @param wasNew true if the element was a new element before editing
     */
    public void sendElementEditedContent(
        CmsContainerPageElementPanel widget,
        elemental2.dom.Element replacedElement,
        boolean wasNew) {

        if (widget != null) {
            I_EventDetails details = createEventDetails(widget);
            details.setReplacedElement(replacedElement);
            details.setIsPlaceholder(wasNew);
            sendEvent(EVENT_ELEMENT_EDITED_CONTENT, details);
            sendEvent(EVENT_ELEMENT_EDITED, details);
            sendEvent(EVENT_ELEMENT_MODIFIED, addTypeToDetails(details, "editContent"));
        }
    }

    /**
     * Sends the custom 'ocElementEditedSettings' event for an edited container element.
     *
     * @param widget the container element
     * @param replacedWidget the former container element that has now been replaced with 'widget'
     */
    public void sendElementEditedSettings(
        CmsContainerPageElementPanel widget,
        CmsContainerPageElementPanel replacedWidget) {

        if (widget != null) {
            I_EventDetails details = createEventDetails(widget);
            details.setReplacedElement(Js.cast(replacedWidget.getElement()));
            sendEvent(EVENT_ELEMENT_EDITED_SETTINGS, details);
            sendEvent(EVENT_ELEMENT_EDITED, details);
            sendEvent(EVENT_ELEMENT_MODIFIED, addTypeToDetails(details, "editSettings"));
        }
    }

    /**
     * Sends the custom 'ocElementMoved' event for a moved container element.
     *
     * @param widget the container element
     */
    public void sendElementMoved(CmsContainerPageElementPanel widget) {

        if (widget != null) {
            I_EventDetails details = createEventDetails(widget);
            sendEvent(EVENT_ELEMENT_MOVED, details);
            sendEvent(EVENT_ELEMENT_MODIFIED, addTypeToDetails(details, "move"));
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
     * Sets the element view.<p>
     *
     * @param viewInfo the element view
     * @param nextAction the action to execute after setting the view
     */
    public void setElementView(CmsElementViewInfo viewInfo, Runnable nextAction) {

        if (viewInfo != null) {
            m_elementView = viewInfo;

            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void execute() {

                    getContainerpageService().setElementView(m_elementView.getElementViewId(), this);
                }

                @Override
                protected void onResponse(Void result) {

                    // nothing to do
                }
            };
            action.execute();

            m_currentEditLevel = -1;
            reinitializeButtons();
            updateButtonsForCurrentView();
            reInitInlineEditing();
            updateGalleryData(true, nextAction);
        }
    }

    /**
     * Sets the model group base element id.<p>
     *
     * @param modelGroupElementId the model group base element id
     */
    public void setModelGroupElementId(String modelGroupElementId) {

        m_modelGroupElementId = modelGroupElementId;
    }

    /**
     * Marks the page as changed.<p>
     *
     * @param nextActions the actions to perform after the page has been marked as changed
     */
    public void setPageChanged(Runnable... nextActions) {

        if (!isGroupcontainerEditing()) {
            // the container page will be saved immediately
            m_pageChanged = true;
            saveContainerpage(nextActions);
        }
    }

    /**
     * Sets an alternative event preview handler.
     *
     * @param handler the alternative preview handler to use
     */
    public void setPreviewHandler(NativePreviewHandler handler) {
        m_previewHandler = handler;
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
    public void startEditingGroupcontainer(
        final CmsGroupContainerElementPanel groupContainer,
        final boolean isElementGroup) {

        removeEditButtonsPositionTimer();
        I_CmsSimpleCallback<Boolean> callback = new I_CmsSimpleCallback<Boolean>() {

            public void execute(Boolean arg) {

                if (arg.booleanValue()) {
                    if (isElementGroup) {
                        m_groupEditor = CmsGroupContainerEditor.openGroupcontainerEditor(
                            groupContainer,
                            CmsContainerpageController.this,
                            m_handler);
                    } else {
                        m_groupEditor = CmsInheritanceContainerEditor.openInheritanceContainerEditor(
                            groupContainer,
                            CmsContainerpageController.this,
                            m_handler);
                    }
                } else {
                    CmsNotification.get().send(
                        Type.WARNING,
                        Messages.get().key(Messages.GUI_NOTIFICATION_UNABLE_TO_LOCK_0));
                }
            }
        };
        RootPanel.get().addStyleName(OC_EDITING_GROUPCONTAINER);
        if ((m_groupEditor == null) && (groupContainer.isNew())) {
            callback.execute(Boolean.TRUE);
        } else {
            lockContainerpage(callback);
        }

    }

    /**
     * Starts the publish lock check.
     */
    public void startPublishLockCheck() {

        Set<CmsUUID> elementIds = new HashSet<>();
        processPageContent(new I_PageContentVisitor() {

            public boolean beginContainer(String name, CmsContainer container) {

                return true;
            }

            public void endContainer() {

                // do nothing
            }

            public void handleElement(CmsContainerPageElementPanel element) {

                if (element.hasWritePermission() && element.getLockInfo().isPublishLock()) {
                    CmsUUID structureId = element.getStructureId();
                    if (structureId != null) {
                        elementIds.add(structureId);
                    }
                }
            }
        });

        m_publishLockChecker.addIdsToCheck(elementIds);
    }

    /**
     * Tells the controller that group-container editing has stopped.<p>
     */
    public void stopEditingGroupcontainer() {

        m_groupEditor = null;
        RootPanel.get().removeStyleName(OC_EDITING_GROUPCONTAINER);
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
     * Updates he
     */
    public void updateButtonsForCurrentView() {

        String nonDefaultViewClass = I_CmsLayoutBundle.INSTANCE.containerpageCss().nonDefaultView();
        CmsUUID viewId = getElementView().getRootViewId();
        if (viewId.isNullUUID()) {
            RootPanel.get().removeStyleName(nonDefaultViewClass);
        } else {
            RootPanel.get().addStyleName(nonDefaultViewClass);
        }
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

        for (CmsContainerElementData element : elements.values()) {
            m_elements.put(element.getClientId(), element);
        }
    }

    /**
     * Asks the user whether an element which has been removed should be deleted.<p>
     *
     * @param status the status of the removed element
     */
    protected void askWhetherRemovedElementShouldBeDeleted(final CmsRemovedElementStatus status) {

        CmsRemovedElementDeletionDialog dialog = new CmsRemovedElementDeletionDialog(status);
        dialog.center();
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

        removeEditButtonsPositionTimer();
        m_handler.deactivateCurrentButton();
        m_handler.disableToolbarButtons();
    }

    /**
     * Helper method to get all current container page elements.<p>
     *
     * @param includeGroupContents true if the contents of group containers should also be included
     *
     * @return the list of current container page elements
     */
    protected List<CmsContainerPageElementPanel> getAllContainerPageElements(boolean includeGroupContents) {

        List<CmsContainerPageElementPanel> elemWidgets = new ArrayList<CmsContainerPageElementPanel>();
        for (Entry<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> entry : CmsContainerpageController.get().getContainerTargets().entrySet()) {
            Iterator<Widget> elIt = entry.getValue().iterator();
            while (elIt.hasNext()) {
                try {
                    org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel elementWidget = (org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)elIt.next();
                    elemWidgets.add(elementWidget);
                    if (includeGroupContents && (elementWidget instanceof CmsGroupContainerElementPanel)) {
                        List<CmsContainerPageElementPanel> groupChildren = ((CmsGroupContainerElementPanel)elementWidget).getGroupChildren();
                        elemWidgets.addAll(groupChildren);
                    }
                } catch (ClassCastException e) {
                    // no proper container element, skip it (this should never happen!)
                    CmsDebugLog.getInstance().printLine(
                        "WARNING: there is an inappropriate element within a container");
                }
            }
        }
        return elemWidgets;
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
     * Returns the containers of the page in their current state.<p>
     *
     * @return the containers of the page
     */
    protected List<CmsContainer> getPageState() {

        PageStateVisitor visitor = new PageStateVisitor();
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
     * Checks if any of the containers are nested containers.<p>
     *
     * @return true if there are nested containers
     */
    protected boolean hasNestedContainers() {

        boolean hasNestedContainers = false;
        for (CmsContainer container : m_containers.values()) {
            if (container.getParentContainerName() != null) {
                hasNestedContainers = true;
                break;
            }
        }
        return hasNestedContainers;
    }

    /**
     * Returns whether the given container is considered a root container.<p>
     *
     * @param container the container to check
     *
     * @return <code>true</code> if the given container is a root container
     */
    protected boolean isRootContainer(CmsContainer container) {

        boolean isRoot = false;
        if (!container.isSubContainer()) {
            isRoot = true;
        } else if (container.isDetailOnly()) {
            CmsContainer parent = getContainer(container.getParentContainerName());
            isRoot = (parent != null) && !parent.isDetailOnly();
        }
        return isRoot;
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

        String oldId = element.getNewType();
        element.setNewType(null);
        if (inline) {
            String newId = getServerId(newElementData.getClientId());
            CmsContentEditor.replaceResourceIds(element.getElement(), oldId, newId);
        }
        element.setId(newElementData.getClientId());
        element.setSitePath(newElementData.getSitePath());
        if (!isGroupcontainerEditing()) {
            setPageChanged();
        }
        getHandler().hidePageOverlay();
        getHandler().openEditorForElement(element, inline, true);
    }

    /**
    * Previews events. Shows the leaving page dialog, if the page has changed and an anchor has been clicked.<p>
    * Also triggers an element view change on 'Ctrl+E'.<p>
    *
    * @param event the native event
    */
    protected void previewNativeEvent(NativePreviewEvent event) {
        if (m_previewHandler != null) {
            m_previewHandler.onPreviewNativeEvent(event);
            return;
        }

        Event nativeEvent = Event.as(event.getNativeEvent());

        if ((nativeEvent.getTypeInt() == Event.ONCLICK) && hasPageChanged()) {
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
        if (event.getTypeInt() == Event.ONKEYDOWN) {
            int keyCode = nativeEvent.getKeyCode();
            if ((keyCode == KeyCodes.KEY_F5) && hasPageChanged()) {
                // user pressed F5
                nativeEvent.preventDefault();
                nativeEvent.stopPropagation();
                m_handler.leavePage(Window.Location.getHref());
            }
            if (nativeEvent.getCtrlKey() || nativeEvent.getMetaKey()) {
                // look for short cuts
                if (keyCode == KeyCodes.KEY_E) {
                    if (nativeEvent.getShiftKey()) {
                        circleContainerEditLayers();
                    } else {
                        openNextElementView();
                    }
                    nativeEvent.preventDefault();
                    nativeEvent.stopPropagation();
                }
            }
        }
    }

    /**
     * Iterates over all the container contents and calls a visitor object with the visited containers/elements as parameters.
     *
     * @param visitor the visitor which the container elements should be passed to
     */
    protected void processPageContent(I_PageContentVisitor visitor) {

        for (Entry<String, org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer> entry : m_targetContainers.entrySet()) {

            CmsContainer cnt = m_containers.get(entry.getKey());
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
     * Schedules an update of the gallery data according to the current element view and the editable containers.<p>
     */
    protected void scheduleGalleryUpdate() {

        // only if not already scheduled
        if (m_galleryUpdateTimer == null) {
            m_galleryUpdateTimer = new Timer() {

                @Override
                public void run() {

                    m_galleryUpdateTimer = null;
                    updateGalleryData(false, null);
                }
            };
            m_galleryUpdateTimer.schedule(50);
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
                lockContainerpage(new I_CmsSimpleCallback<Boolean>() {

                    public void execute(Boolean arg) {

                        // nothing to do
                    }
                });
            }
        } else {
            m_pageChanged = changed;
            if (unlock) {
                unlockContainerpage();
            }
        }
    }

    /**
     * Asynchronously unlocks the container page.
     */
    protected void unlockContainerpage() {

        I_CmsAutoBeanFactory factory = CmsCoreProvider.AUTO_BEAN_FACTORY;
        AutoBean<I_CmsUnlockData> unlockParams = factory.unlockData();
        unlockParams.as().setPageId("" + CmsCoreProvider.get().getStructureId());
        if (getData().getDetailId() != null) {
            unlockParams.as().setDetailId("" + getData().getDetailId());
        }
        unlockParams.as().setLocale(CmsCoreProvider.get().getLocale());
        String url = CmsCoreProvider.get().link("/handleBuiltinService" + CmsGwtConstants.HANDLER_UNLOCK_PAGE);
        sendBeacon(url, AutoBeanCodex.encode(unlockParams).getPayload());
    }

    /**
     * Returns the pages of editable containers.<p>
     *
     * @return the containers
     */
    List<CmsContainer> getEditableContainers() {

        List<CmsContainer> containers = new ArrayList<CmsContainer>();
        for (CmsContainer container : m_containers.values()) {
            if ((m_targetContainers.get(container.getName()) != null)
                && isContainerEditable(m_targetContainers.get(container.getName()))) {
                containers.add(container);
            }
        }
        return containers;
    }

    /**
     * Handles a window resize to reset highlighting and the edit button positions.<p>
     */
    void handleResize() {

        m_resizeTimer = null;
        resetEditButtons();
    }

    /**
     * Call on window resize.<p>
     */
    void onResize() {

        if (!isGroupcontainerEditing() && (m_resizeTimer == null)) {
            m_resizeTimer = new Timer() {

                @Override
                public void run() {

                    handleResize();
                }
            };
            m_resizeTimer.schedule(300);
        }
    }

    /**
     * Sets the load time.<p>
     *
     * @param time the time to set
     */
    void setLoadTime(Long time) {

        if (time != null) {
            m_loadTime = time.longValue();
        }
    }

    /**
     * Updates the gallery data according to the current element view and the editable containers.<p>
     * This method should only be called from the gallery update timer to avoid unnecessary requests.<p>
     *
     * @param viewChanged <code>true</code> in case the element view changed
     * @param nextAction the action to execute after updating the gallery data
     */
    void updateGalleryData(final boolean viewChanged, final Runnable nextAction) {

        CmsRpcAction<CmsContainerPageGalleryData> dataAction = new CmsRpcAction<CmsContainerPageGalleryData>() {

            @Override
            public void execute() {

                getContainerpageService().getGalleryDataForPage(
                    getEditableContainers(),
                    getElementView().getElementViewId(),
                    CmsCoreProvider.get().getUri(),
                    getData().getDetailId(),
                    getData().getLocale(),
                    CmsContainerpageController.get().getData().getTemplateContextInfo(),
                    this);
            }

            @Override
            protected void onResponse(CmsContainerPageGalleryData result) {

                m_handler.m_editor.getAdd().updateGalleryData(result, viewChanged);
                if (nextAction != null) {
                    nextAction.run();
                }
            }
        };
        dataAction.execute();
    }

    /**
     * Creates a copy of the event details and adds a 'type' field with the specified value to the result.
     *
     * @param details the details to copy
     * @param type the type to add
     * @return the copy with the added type
     */
    private I_EventDetails addTypeToDetails(I_EventDetails details, String type) {

        I_EventDetails result = copyEventDetails(details);
        result.setType(type);
        return result;
    }

    /**
     * Checks whether there are other references to a given container page element.<p>
     *
     * @param element the element to check
     * @param callback the callback which will be called with the result of the check (true if there are other references)
     */
    private void checkElementReferences(
        final CmsContainerPageElementPanel element,
        final AsyncCallback<CmsRemovedElementStatus> callback) {

        ReferenceCheckVisitor visitor = new ReferenceCheckVisitor(element);
        processPageContent(visitor);
        if (visitor.hasReferences()) {
            // Don't need to ask the server because we already know we have other references in the same page
            CmsRpcAction<CmsListInfoBean> infoAction = new CmsRpcAction<CmsListInfoBean>() {

                @Override
                public void execute() {

                    start(200, true);
                    CmsCoreProvider.getVfsService().getPageInfo(new CmsUUID(getServerId(element.getId())), this);
                }

                @Override
                protected void onResponse(CmsListInfoBean result) {

                    stop(false);
                    callback.onSuccess(new CmsRemovedElementStatus(null, result, false, null));
                }
            };
            infoAction.execute();
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
                    callback.onSuccess(status);
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
     * Selects the next container edit level.<p>
     */
    private void circleContainerEditLayers() {

        if (m_isContentEditing || isGroupcontainerEditing() || (m_maxContainerLevel == 0)) {
            return;
        }
        boolean hasEditables = false;
        int previousLevel = m_currentEditLevel;
        String message = "";
        while (!hasEditables) {
            if (m_currentEditLevel == m_maxContainerLevel) {
                m_currentEditLevel = -1;
                message = Messages.get().key(Messages.GUI_SWITCH_EDIT_LEVEL_ALL_1, m_elementView.getTitle());
            } else {
                m_currentEditLevel++;
                message = Messages.get().key(Messages.GUI_SWITCH_EDIT_LEVEL_1, Integer.valueOf(m_currentEditLevel));
            }
            reinitializeButtons();
            hasEditables = !CmsDomUtil.getElementsByClass(
                I_CmsElementToolbarContext.ELEMENT_OPTION_BAR_CSS_CLASS).isEmpty();
        }
        if (previousLevel != m_currentEditLevel) {
            CmsNotification.get().send(Type.NORMAL, message);
        }
    }

    /**
     * Copies an event details object.
     *
     * @param original the original event details
     * @return the copied object
     */
    private I_EventDetails copyEventDetails(I_EventDetails original) {

        JsPropertyMap<Object> originalMap = Js.cast(original);
        JsPropertyMap<Object> result = Js.cast(new JsObject());
        originalMap.forEach(key -> {
            result.set(key, originalMap.get(key));
        });
        return Js.cast(result);
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

    /**
     * Checks whether the given container matches the current edit level.<p>
     *
     * @param container the container to check
     *
     * @return <code>true</code> if the given container matches the current edit level
     */
    private boolean matchesCurrentEditLevel(I_CmsDropContainer container) {

        boolean result = !(container instanceof CmsContainerPageContainer)
            || (m_currentEditLevel == -1)
            || (m_currentEditLevel == ((CmsContainerPageContainer)container).getContainerLevel());
        return result;
    }

    /**
     * Opens the next available root element view.<p>
     */
    private void openNextElementView() {

        List<CmsElementViewInfo> views = getData().getElementViews();
        if (views.size() > 1) {
            CmsUUID current = m_elementView.getRootViewId();

            // look for the current view index
            int currentIndex = -1;
            for (int i = 0; i < views.size(); i++) {
                CmsElementViewInfo view = views.get(i);
                if (view.isRoot() && current.equals(view.getElementViewId())) {
                    currentIndex = i;
                    break;
                }
            }
            if (currentIndex != -1) {
                CmsElementViewInfo target = null;
                // look for the next root view
                for (int i = currentIndex + 1; i < views.size(); i++) {
                    CmsElementViewInfo view = views.get(i);
                    if (view.isRoot()) {
                        target = view;
                        break;
                    }
                }
                if (target == null) {
                    // start at the beginning
                    for (int i = 0; i < currentIndex; i++) {
                        CmsElementViewInfo view = views.get(i);
                        if (view.isRoot()) {
                            target = view;
                            break;
                        }
                    }
                }
                if (target != null) {
                    final String viewName = target.getTitle();
                    Runnable action = new Runnable() {

                        public void run() {

                            CmsNotification.get().send(
                                Type.NORMAL,
                                Messages.get().key(Messages.GUI_SWITCH_ELEMENT_VIEW_NOTIFICATION_1, viewName));
                        }
                    };
                    setElementView(target, action);
                }
            }
        }
    }

    /**
     * Removes the edit buttons position timer.<p>
     */
    private void removeEditButtonsPositionTimer() {

        if (m_editButtonsPositionTimer != null) {
            m_editButtonsPositionTimer.cancel();
            m_editButtonsPositionTimer = null;
        }
    }

    /**
     * Calls the browser's sendBeacon function.
     *
     * @param url the URL to send the data to
     * @param data the data to send
     */
    private native void sendBeacon(String url, String data) /*-{
        $wnd.navigator.sendBeacon(url, data);
    }-*/;

    /**
     * Creates a custom event and dispatches it to the document.
     *
     * @param type the event type
     * @param details the event details
     */
    private void sendEvent(String type, I_EventDetails details) {

        CustomEventInit<I_EventDetails> init = Js.cast(CustomEventInit.create());
        init.setBubbles(false);
        init.setCancelable(false);
        init.setDetail(details);
        CustomEvent<I_EventDetails> event1 = new elemental2.dom.CustomEvent<I_EventDetails>(type, init);
        CustomEvent<I_EventDetails> event = event1;
        // fire event in a timer so the page editor is not blocked by the event handler reacting to it
        Timer timer = new Timer() {

            @Override
            public void run() {

                DomGlobal.document.documentElement.dispatchEvent(event);
            }
        };
        timer.schedule(0);
    }

    /**
     * Checks whether given element is a model group and it's option bar edit points should be visible.<p>
     *
     * @param element the element to check
     *
     * @return <code>true</code> in case the current page is not a model group page,
     *  the given element is a model group and it is inside a view visible to the current user
     */
    private boolean shouldShowModelgroupOptionBar(CmsContainerPageElementPanel element) {

        if (!getData().isModelGroup() && element.isModelGroup()) {
            for (CmsElementViewInfo info : getData().getElementViews()) {
                if (info.getElementViewId().equals(element.getElementView())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Updates the container level info on the present containers.<p>
     */
    private void updateContainerLevelInfo() {

        Map<String, CmsContainerPageContainer> containers = new HashMap<String, CmsContainerPageContainer>();
        List<CmsContainerPageContainer> temp = new ArrayList<CmsContainerPageContainer>(m_targetContainers.values());
        m_maxContainerLevel = 0;
        boolean progress = true;
        while (!temp.isEmpty() && progress) {
            int size = containers.size();
            Iterator<CmsContainerPageContainer> it = temp.iterator();
            while (it.hasNext()) {
                CmsContainerPageContainer container = it.next();
                int level = -1;
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(container.getParentContainerId())) {
                    level = 0;
                } else if (containers.containsKey(container.getParentContainerId())) {
                    level = containers.get(container.getParentContainerId()).getContainerLevel() + 1;
                }
                if (level > -1) {
                    container.setContainerLevel(level);
                    containers.put(container.getContainerId(), container);
                    it.remove();
                    if (level > m_maxContainerLevel) {
                        m_maxContainerLevel = level;
                    }
                }
            }
            progress = containers.size() > size;
        }
    }

    /**
     * Sets the oc-detail-preview class on first container elements of an appropriate type in detail containers,
     * if we are currently not showing a detail content.
     */
    private void updateDetailPreviewStyles() {

        Set<String> detailTypes = getData().getDetailTypes();
        if ((getData().getDetailId() != null) || detailTypes.isEmpty()) {
            return;
        }
        for (Element elem : CmsDomUtil.getElementsByClass(CmsGwtConstants.CLASS_DETAIL_PREVIEW)) {
            elem.removeClassName(CmsGwtConstants.CLASS_DETAIL_PREVIEW);
        }
        boolean defaultDetailPage = detailTypes.contains(CmsGwtConstants.DEFAULT_DETAILPAGE_TYPE);

        processPageContent(new I_PageContentVisitor() {

            boolean m_isdetail = false;

            public boolean beginContainer(String name, CmsContainer container) {

                m_isdetail = container.isDetailViewContainer();
                return true;
            }

            public void endContainer() {

                // do nothing
            }

            public void handleElement(CmsContainerPageElementPanel element) {

                if (m_isdetail && (defaultDetailPage || detailTypes.contains(element.getResourceType()))) {
                    element.addStyleName(CmsGwtConstants.CLASS_DETAIL_PREVIEW);
                }
            }
        });

    }
}
