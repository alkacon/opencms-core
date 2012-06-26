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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.I_CmsSitemapController;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem.LoadState;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;
import org.opencms.gwt.client.util.CmsDomUtil.Target;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor controller.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapController implements I_CmsSitemapController {

    /** The name to use for new entries. */
    public static final String NEW_ENTRY_NAME = Messages.get().key(Messages.GUI_NEW_ENTRY_NAME_0);

    /** A map of *all* detail page info beans, indexed by page id. */
    protected Map<CmsUUID, CmsDetailPageInfo> m_allDetailPageInfos = new HashMap<CmsUUID, CmsDetailPageInfo>();

    /** The sitemap data. */
    protected CmsSitemapData m_data;

    /** The detail page table. */
    protected CmsDetailPageTable m_detailPageTable;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The entry data model. */
    private Map<CmsUUID, CmsClientSitemapEntry> m_entriesById;

    /** The sitemap entries by path. */
    private Map<String, CmsClientSitemapEntry> m_entriesByPath;

    /** The set of names of hidden properties. */
    private Set<String> m_hiddenProperties;

    /** The map of property maps by structure id. */
    private Map<CmsUUID, Map<String, CmsClientProperty>> m_propertyMaps = Maps.newHashMap();

    /** The list of property update handlers. */
    private List<I_CmsPropertyUpdateHandler> m_propertyUpdateHandlers = new ArrayList<I_CmsPropertyUpdateHandler>();

    /** The sitemap service instance. */
    private I_CmsSitemapServiceAsync m_service;

    /** The vfs service. */
    private I_CmsVfsServiceAsync m_vfsService;

    /**
     * Constructor.<p>
     */
    public CmsSitemapController() {

        m_entriesById = new HashMap<CmsUUID, CmsClientSitemapEntry>();
        m_entriesByPath = new HashMap<String, CmsClientSitemapEntry>();
        try {
            m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObject(getService(), CmsSitemapData.DICT_NAME);
        } catch (Exception e) {
            CmsErrorDialog dialog = new CmsErrorDialog("Error", "Deserialization failed.");
            dialog.show();
        }

        m_hiddenProperties = new HashSet<String>();
        if (m_data != null) {
            m_detailPageTable = m_data.getDetailPageTable();
            m_data.getRoot().initializeAll(this);
            m_eventBus = new SimpleEventBus();
            initDetailPageInfos();
        }
    }

    /**
     * Helper method for looking up a value in a map which may be null.<p>
     * 
     * @param <A> the key type 
     * @param <B> the value type 
     * @param map the map (which may be null)
     * @param key the map key 
     * 
     * @return the value of the map at the given key, or null if the map is null 
     */
    public static <A, B> B safeLookup(Map<A, B> map, A key) {

        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Adds a new change event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration 
     */
    public HandlerRegistration addChangeHandler(I_CmsSitemapChangeHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapChangeEvent.getType(), this, handler);
    }

    /**
     * Adds a new detail page information bean.<p>
     * 
     * @param info the detail page information bean to add 
     */
    public void addDetailPageInfo(CmsDetailPageInfo info) {

        m_detailPageTable.add(info);
        m_allDetailPageInfos.put(info.getId(), info);
    }

    /**
     * Adds a new load event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addLoadHandler(I_CmsSitemapLoadHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapLoadEvent.getType(), this, handler);
    }

    /**
     * Adds a handler for property changes caused by user edits.<p>
     * 
     * @param handler a new handler for property updates caused by the user 
     */
    public void addPropertyUpdateHandler(I_CmsPropertyUpdateHandler handler) {

        m_propertyUpdateHandlers.add(handler);

    }

    /**
     * Adds the entry to the navigation.<p>
     * 
     * @param entry the entry
     */
    public void addToNavigation(CmsClientSitemapEntry entry) {

        entry.setInNavigation(true);
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
        CmsPropertyModification mod = new CmsPropertyModification(
            entry.getId(),
            CmsClientProperty.PROPERTY_NAVTEXT,
            entry.getTitle(),
            true);
        change.setPropertyChanges(Collections.singletonList(mod));
        change.setPosition(entry.getPosition());
        commitChange(change, null);
    }

    /**
     * Makes the given sitemap entry the default detail page for its detail page type.<p>
     * 
     * @param entry an entry representing a detail page 
     */
    public void bump(CmsClientSitemapEntry entry) {

        CmsDetailPageTable table = getDetailPageTable().copy();
        table.bump(entry.getId());
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.bumpDetailPage);
        change.setDetailPageInfos(table.toList());
        commitChange(change, null);
    }

    /**
     * Clears the deleted clip-board list and commits the change.<p>
     */
    public void clearDeletedList() {

        CmsSitemapClipboardData clipboardData = getData().getClipboardData().copy();
        clipboardData.getDeletions().clear();
        CmsSitemapChange change = new CmsSitemapChange(null, null, ChangeType.clipboardOnly);
        change.setClipBoardData(clipboardData);
        commitChange(change, null);
    }

    /**
     * Clears the modified clip-board list and commits the change.<p>
     */
    public void clearModifiedList() {

        CmsSitemapClipboardData clipboardData = getData().getClipboardData().copy();
        clipboardData.getModifications().clear();
        CmsSitemapChange change = new CmsSitemapChange(null, null, ChangeType.clipboardOnly);
        change.setClipBoardData(clipboardData);
        commitChange(change, null);
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     * @param parentId the parent entry id
     * @param structureId the structure id of the model page (if null, uses default model page)
     */
    public void create(final CmsClientSitemapEntry newEntry, CmsUUID parentId, CmsUUID structureId) {

        if (structureId == null) {
            structureId = m_data.getDefaultNewElementInfo().getCopyResourceId();
        }
        create(newEntry, parentId, m_data.getDefaultNewElementInfo().getId(), structureId, null);
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     * @param parentId the parent entry id
     * @param resourceTypeId the resource type id
     * @param copyResourceId the copy resource id
     * @param parameter an additional parameter which may contain more information needed to create the new resource 
     */
    public void create(
        CmsClientSitemapEntry newEntry,
        CmsUUID parentId,
        int resourceTypeId,
        CmsUUID copyResourceId,
        String parameter) {

        assert (getEntry(newEntry.getSitePath()) == null);

        CmsSitemapChange change = new CmsSitemapChange(null, newEntry.getSitePath(), ChangeType.create);
        change.setDefaultFileId(newEntry.getDefaultFileId());
        change.setParentId(parentId);
        change.setName(newEntry.getName());
        change.setPosition(newEntry.getPosition());
        change.setOwnInternalProperties(newEntry.getOwnProperties());
        change.setDefaultFileInternalProperties(newEntry.getDefaultFileProperties());
        change.setTitle(newEntry.getTitle());
        change.setCreateParameter(parameter);
        change.setNewResourceTypeId(resourceTypeId);
        change.setNewCopyResourceId(copyResourceId);
        if (isDetailPage(newEntry)) {
            CmsDetailPageTable table = getDetailPageTable().copy();
            if (!table.contains(newEntry.getId())) {
                CmsDetailPageInfo info = new CmsDetailPageInfo(
                    newEntry.getId(),
                    newEntry.getSitePath(),
                    newEntry.getDetailpageTypeName());
                table.add(info);
            }
            change.setDetailPageInfos(table.toList());
        }
        CmsSitemapClipboardData data = getData().getClipboardData().copy();
        data.addModified(newEntry);
        change.setClipBoardData(data);
        commitChange(change, null);
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     * 
     * @param parent the entry to which a new sub-entry should be added 
     */
    public void createSubEntry(final CmsClientSitemapEntry parent) {

        createSubEntry(parent, null);
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     * 
     * @param parent the entry to which a new sub-entry should be added
     * @param structureId the structure id of the model page (if null, uses default model page) 
     */
    public void createSubEntry(final CmsClientSitemapEntry parent, final CmsUUID structureId) {

        final CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry();
        CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(parent.getId());
        AsyncCallback<CmsClientSitemapEntry> callback = new AsyncCallback<CmsClientSitemapEntry>() {

            public void onFailure(Throwable caught) {

                // nothing to do
            }

            public void onSuccess(CmsClientSitemapEntry result) {

                String urlName = ensureUniqueName(parent, NEW_ENTRY_NAME);
                //newEntry.setTitle(urlName);
                newEntry.setName(urlName);
                String sitePath = parent.getSitePath() + urlName + "/";
                newEntry.setSitePath(sitePath);
                newEntry.setVfsPath(null);
                newEntry.setPosition(0);
                newEntry.setNew(true);
                newEntry.setInNavigation(true);
                newEntry.setResourceTypeName("folder");
                newEntry.getOwnProperties().put(
                    CmsClientProperty.PROPERTY_TITLE,
                    new CmsClientProperty(CmsClientProperty.PROPERTY_TITLE, NEW_ENTRY_NAME, NEW_ENTRY_NAME));
                create(newEntry, parent.getId(), structureId);
            }
        };

        if (item.getLoadState().equals(LoadState.UNLOADED)) {
            getChildren(parent.getId(), true, callback);
        } else {
            callback.onSuccess(parent);
        }
    }

    /**
     * Creates a sub-sitemap from the subtree of the current sitemap starting at the given entry.<p>
     * 
     * @param entryId the id of the entry
     */
    public void createSubSitemap(final CmsUUID entryId) {

        CmsRpcAction<CmsSitemapChange> subSitemapAction = new CmsRpcAction<CmsSitemapChange>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().createSubSitemap(entryId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSitemapChange result) {

                stop(false);
                applyChange(result);
            }
        };
        subSitemapAction.execute();
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     * 
     * @param sitePath the site path of the entry to delete
     */
    public void delete(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.delete);
        change.setParentId(parent.getId());
        change.setDefaultFileId(entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        if (!entry.isNew()) {
            data.addDeleted(entry);
            removeDeletedFromModified(entry, data);
        }
        change.setClipBoardData(data);
        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getData().getDetailPageTable();
        CmsUUID id = entry.getId();
        if (detailPageTable.contains(id)) {
            CmsDetailPageTable copyTable = detailPageTable.copy();
            copyTable.remove(id);
            change.setDetailPageInfos(copyTable.toList());
        }
        commitChange(change, null);
    }

    /**
     * Edits the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     * @param propertyChanges the property changes 
     * @param reloadStatus a value indicating which entries need to be reloaded after the change
     */
    public void edit(
        CmsClientSitemapEntry entry,
        List<CmsPropertyModification> propertyChanges,
        final CmsReloadMode reloadStatus) {

        CmsSitemapChange change = getChangeForEdit(entry, propertyChanges);
        final String updateTarget = ((reloadStatus == CmsReloadMode.reloadParent))
        ? getParentEntry(entry).getSitePath()
        : entry.getSitePath();
        Command callback = new Command() {

            public void execute() {

                if ((reloadStatus == CmsReloadMode.reloadParent) || (reloadStatus == CmsReloadMode.reloadEntry)) {
                    updateEntry(updateTarget);
                }
            }
        };
        if (change != null) {
            commitChange(change, callback);
        }
    }

    /**
     * Edits an entry and changes its URL name.<p>
     * 
     * @param entry the entry which is being edited 
     * @param newUrlName the new URL name of the entry 
     * @param propertyChanges the property changes  
     * @param keepNewStatus <code>true</code> if the entry should keep it's new status
     * @param reloadStatus a value indicating which entries need to be reloaded after the change   
     */
    public void editAndChangeName(
        final CmsClientSitemapEntry entry,
        String newUrlName,
        List<CmsPropertyModification> propertyChanges,
        final boolean keepNewStatus,
        final CmsReloadMode reloadStatus) {

        CmsSitemapChange change = getChangeForEdit(entry, propertyChanges);
        change.setName(newUrlName);
        final CmsUUID entryId = entry.getId();
        final boolean newStatus = keepNewStatus && entry.isNew();

        final CmsUUID updateTarget = ((reloadStatus == CmsReloadMode.reloadParent))
        ? getParentEntry(entry).getId()
        : entryId;
        Command callback = new Command() {

            public void execute() {

                if ((reloadStatus == CmsReloadMode.reloadParent) || (reloadStatus == CmsReloadMode.reloadEntry)) {
                    updateEntry(updateTarget);
                }
                getEntryById(entryId).setNew(newStatus);
            }

        };

        commitChange(change, callback);
    }

    /**
     * Ensure the uniqueness of a given URL-name within the children of the given parent site-map entry.<p>
     * 
     * @param parent the parent entry
     * @param newName the proposed name
     * 
     * @return the unique name
     */
    public String ensureUniqueName(CmsClientSitemapEntry parent, String newName) {

        return ensureUniqueName(parent.getSitePath(), newName);
    }

    /**
     * Ensure the uniqueness of a given URL-name within the children of the given parent folder.<p>
     * 
     * @param parentFolder the parent folder
     * @param newName the proposed name
     * 
     * @return the unique name
     */
    public String ensureUniqueName(final String parentFolder, String newName) {

        // using lower case folder names
        final String lowerCaseName = newName.toLowerCase();
        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, false);
                CmsCoreProvider.getService().getUniqueFileName(parentFolder, lowerCaseName, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(String result) {

                stop(false);
            }

        };
        return action.executeSync();
    }

    /**
     * Applies the given property modification.<p>
     * 
     * @param propMod the property modification to apply
     */
    public void executePropertyModification(CmsPropertyModification propMod) {

        CmsClientSitemapEntry entry = getEntryById(propMod.getId());
        if (entry != null) {
            Map<String, CmsClientProperty> props = getPropertiesForId(propMod.getId());
            if (props != null) {
                propMod.updatePropertyInMap(props);
                entry.setOwnProperties(props);
            }
        }
    }

    /**
     * Retrieves the child entries of the given node from the server.<p>
     * 
     * @param entryId the entry id
     * @param setOpen if the entry should be opened
     * @param callback the callback to execute after the children have been loaded 
     */
    public void getChildren(
        final CmsUUID entryId,
        final boolean setOpen,
        final AsyncCallback<CmsClientSitemapEntry> callback) {

        CmsRpcAction<CmsClientSitemapEntry> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500, false);
                // loading grand children as well
                getService().getChildren(getEntryPoint(), entryId, 2, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                CmsClientSitemapEntry target = getEntryById(entryId);
                if (target == null) {
                    // this might happen after an automated deletion
                    stop(false);
                    return;
                }
                target.setSubEntries(result.getSubEntries(), CmsSitemapController.this);
                CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(target.getId());
                target.update(result);
                target.initializeAll(CmsSitemapController.this);
                item.updateEntry(target);
                m_eventBus.fireEventFromSource(new CmsSitemapLoadEvent(target, setOpen), CmsSitemapController.this);
                stop(false);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        getChildrenAction.execute();
    }

    /**
    * Returns the sitemap data.<p>
    *
    * @return the sitemap data
    */
    public CmsSitemapData getData() {

        return m_data;
    }

    /**
     * Returns the detail page info for a given entry id.<p>
     * 
     * @param id a sitemap entry id 
     * 
     * @return the detail page info for that id 
     */
    public CmsDetailPageInfo getDetailPageInfo(CmsUUID id) {

        return m_allDetailPageInfos.get(id);
    }

    /**
     * Returns the detail page table.<p>
     * 
     * @return the detail page table 
     */
    public CmsDetailPageTable getDetailPageTable() {

        return m_detailPageTable;
    }

    /**
     * Gets the effective value of a property value for a sitemap entry.<p>
     *  
     * @param entry the sitemap entry 
     * @param name the name of the property 
     * 
     * @return the effective value 
     */
    public String getEffectiveProperty(CmsClientSitemapEntry entry, String name) {

        CmsClientProperty prop = getEffectivePropertyObject(entry, name);
        if (prop == null) {
            return null;
        }
        return prop.getEffectiveValue();
    }

    /**
     * Gets the value of a property which is effective at a given sitemap entry.<p>
     * 
     * @param entry the sitemap entry 
     * @param name the name of the property  
     * @return the effective property value 
     */
    public CmsClientProperty getEffectivePropertyObject(CmsClientSitemapEntry entry, String name) {

        Map<String, CmsClientProperty> dfProps = entry.getDefaultFileProperties();
        CmsClientProperty result = safeLookup(dfProps, name);
        if (!CmsClientProperty.isPropertyEmpty(result)) {
            return result.withOrigin(entry.getSitePath());
        }
        result = safeLookup(entry.getOwnProperties(), name);
        if (!CmsClientProperty.isPropertyEmpty(result)) {
            return result.withOrigin(entry.getSitePath());
        }
        return getInheritedPropertyObject(entry, name);
    }

    /**
     * Returns all entries with an id from a given list.<p>
     * 
     * @param ids a list of sitemap entry ids 
     * 
     * @return all entries whose id is contained in the id list 
     */
    public Map<CmsUUID, CmsClientSitemapEntry> getEntriesById(Collection<CmsUUID> ids) {

        // TODO: use some map of id -> entry instead
        List<CmsClientSitemapEntry> entriesToProcess = new ArrayList<CmsClientSitemapEntry>();
        Map<CmsUUID, CmsClientSitemapEntry> result = new HashMap<CmsUUID, CmsClientSitemapEntry>();

        entriesToProcess.add(m_data.getRoot());
        while (!entriesToProcess.isEmpty()) {
            CmsClientSitemapEntry entry = entriesToProcess.remove(entriesToProcess.size() - 1);
            if (ids.contains(entry.getId())) {
                result.put(entry.getId(), entry);
                if (result.size() == ids.size()) {
                    return result;
                }
            }
            entriesToProcess.addAll(entry.getSubEntries());
        }
        return result;
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param entryPath the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsClientSitemapEntry getEntry(String entryPath) {

        return m_entriesByPath.get(entryPath);
    }

    /**
     * Finds an entry by id.<p>
     * 
     * @param id the id of the entry to find 
     * 
     * @return the found entry, or null if the entry wasn't found 
     */
    public CmsClientSitemapEntry getEntryById(CmsUUID id) {

        return m_entriesById.get(id);
    }

    /**
     * Gets the value for a property which a sitemap entry would inherit if it didn't have its own properties.<p>
     * 
     * @param entry the sitemap entry 
     * @param name the property name 
     * @return the inherited property value 
     */
    public String getInheritedProperty(CmsClientSitemapEntry entry, String name) {

        CmsClientProperty prop = getInheritedPropertyObject(entry, name);
        if (prop == null) {
            return null;
        }
        return prop.getEffectiveValue();
    }

    /**
     * Gets the property object which would be inherited by a sitemap entry.<p>
     * 
     * @param entry the sitemap entry 
     * @param name the name of the property 
     * @return the property object which would be inherited 
     */
    public CmsClientProperty getInheritedPropertyObject(CmsClientSitemapEntry entry, String name) {

        CmsClientSitemapEntry currentEntry = entry;
        while (currentEntry != null) {
            currentEntry = getParentEntry(currentEntry);
            if (currentEntry != null) {
                CmsClientProperty folderProp = currentEntry.getOwnProperties().get(name);
                if (!CmsClientProperty.isPropertyEmpty(folderProp)) {
                    return folderProp.withOrigin(currentEntry.getSitePath());
                }
            }
        }
        CmsClientProperty parentProp = getParentProperties().get(name);
        if (!CmsClientProperty.isPropertyEmpty(parentProp)) {
            String origin = parentProp.getOrigin();
            String siteRoot = CmsCoreProvider.get().getSiteRoot();
            if (origin.startsWith(siteRoot)) {
                origin = origin.substring(siteRoot.length());
            }
            return parentProp.withOrigin(origin);
        }
        return null;

    }

    /**
     * Returns a list of all descendant sitemap entries of a given path which have already been loaded on the client.<p>
     * 
     * @param path the path for which the descendants should be collected 
     * 
     * @return the list of descendant sitemap entries 
     */
    public List<CmsClientSitemapEntry> getLoadedDescendants(String path) {

        LinkedList<CmsClientSitemapEntry> remainingEntries = new LinkedList<CmsClientSitemapEntry>();
        List<CmsClientSitemapEntry> result = new ArrayList<CmsClientSitemapEntry>();
        CmsClientSitemapEntry entry = getEntry(path);
        remainingEntries.add(entry);
        while (remainingEntries.size() > 0) {
            CmsClientSitemapEntry currentEntry = remainingEntries.removeFirst();
            result.add(currentEntry);
            for (CmsClientSitemapEntry subEntry : currentEntry.getSubEntries()) {
                remainingEntries.add(subEntry);
            }
        }

        return result;

    }

    /**
     * Returns the no edit reason or <code>null</code> if editing is allowed.<p>
     * 
     * @param entry the entry to get the no edit reason for
     * 
     * @return the no edit reason
     */
    public String getNoEditReason(CmsClientSitemapEntry entry) {

        String reason = null;
        if ((entry.getLock() != null) && (entry.getLock().getLockOwner() != null) && !entry.getLock().isOwnedByUser()) {
            reason = Messages.get().key(Messages.GUI_DISABLED_LOCKED_BY_1, entry.getLock().getLockOwner());
        }
        if (entry.hasBlockingLockedChildren()) {
            reason = Messages.get().key(Messages.GUI_DISABLED_BLOCKING_LOCKED_CHILDREN_0);
        }
        return reason;
    }

    /**
     * Returns the parent entry of a sitemap entry, or null if it is the root entry.<p>
     * 
     * @param entry a sitemap entry
     *  
     * @return the parent entry or null 
     */
    public CmsClientSitemapEntry getParentEntry(CmsClientSitemapEntry entry) {

        String path = entry.getSitePath();
        String parentPath = CmsResource.getParentFolder(path);
        if (parentPath == null) {
            return null;
        }
        return getEntry(parentPath);
    }

    /**
     * Gets the properties for a given structure id.<p>
     * 
     * @param id the structure id of a sitemap entry 
     * 
     * @return the properties for that structure id 
     */
    public Map<String, CmsClientProperty> getPropertiesForId(CmsUUID id) {

        return m_propertyMaps.get(id);
    }

    /** 
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    public I_CmsSitemapServiceAsync getService() {

        if (m_service == null) {
            m_service = GWT.create(I_CmsSitemapService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.sitemap.CmsVfsSitemapService.gwt");
            ((ServiceDefTarget)m_service).setServiceEntryPoint(serviceUrl);
        }
        return m_service;
    }

    /**
     * Leaves the current sitemap to open the parent sitemap.<p>
     */
    public void gotoParentSitemap() {

        openSiteMap(getData().getParentSitemap());
    }

    /**
     * Checks whether this entry belongs to a detail page.<p>
     * 
     * @param entry the entry to check
     * 
     * @return true if this entry belongs to a detail page 
     */
    public boolean isDetailPage(CmsClientSitemapEntry entry) {

        return (entry.getDetailpageTypeName() != null) || isDetailPage(entry.getId());
    }

    /**
     * Returns true if the id is the id of a detail page.<p>
     * 
     * @param id the sitemap entry id 
     * @return true if the id is the id of a detail page entry
     */
    public boolean isDetailPage(CmsUUID id) {

        return m_allDetailPageInfos.containsKey(id);
    }

    /**
     * Checks if the current sitemap is editable.<p>
     *
     * @return <code>true</code> if the current sitemap is editable
     */
    public boolean isEditable() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_data.getNoEditReason());
    }

    /**
     * Checks whether a string is the name of a hidden property.<p>
     * 
     * A hidden property is a property which should not appear in the property editor
     * because it requires special treatment.<p>
     * 
     * @param propertyName the property name which should be checked
     * 
     * @return true if the argument is the name of a hidden property
     */
    public boolean isHiddenProperty(String propertyName) {

        if (propertyName.equals("secure") && !m_data.isSecure()) {
            // "secure" property should not be editable in a site for which no secure server is configured 
            return true;
        }

        return m_hiddenProperties.contains(propertyName);
    }

    /**
     * Checks if the given site path is the sitemap root.<p>
     * 
     * @param sitePath the site path to check
     * 
     * @return <code>true</code> if the given site path is the sitemap root
     */
    public boolean isRoot(String sitePath) {

        return m_data.getRoot().getSitePath().equals(sitePath);
    }

    /**
     * Ask to save the page before leaving, if necessary.<p>
     * 
     * @param target the leaving target
     */
    public void leaveEditor(String target) {

        Window.Location.assign(CmsCoreProvider.get().link(target));
    }

    /**
    * Merges a subsitemap at the given id back into this sitemap.<p>
    * 
    * @param entryId the id of the sub sitemap entry 
    */
    public void mergeSubSitemap(final CmsUUID entryId) {

        CmsRpcAction<CmsSitemapChange> mergeAction = new CmsRpcAction<CmsSitemapChange>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().mergeSubSitemap(getEntryPoint(), entryId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSitemapChange result) {

                stop(false);
                applyChange(result);

            }
        };
        mergeAction.execute();

    }

    /**
     * Moves the given sitemap entry with all its descendants to the new position.<p>
     * 
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(CmsClientSitemapEntry entry, String toPath, int position) {

        // check for valid data
        if (!isValidEntryAndPath(entry, toPath)) {
            // invalid data, do nothing 
            CmsDebugLog.getInstance().printLine("invalid data, doing nothing");
            return;
        }
        // check for relevance
        if (isChangedPosition(entry, toPath, position)) {
            // only register real changes
            CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            change.setDefaultFileId(entry.getDefaultFileId());
            if (!toPath.equals(entry.getSitePath())) {
                change.setParentId(getEntry(CmsResource.getParentFolder(toPath)).getId());
                change.setName(CmsResource.getName(toPath));
            }
            if (CmsSitemapView.getInstance().isNavigationMode()) {
                change.setPosition(position);
            }
            change.setLeafType(entry.isLeafType());
            CmsSitemapClipboardData data = getData().getClipboardData().copy();
            data.addModified(entry);
            change.setClipBoardData(data);
            commitChange(change, null);
        }
    }

    /**
     * Opens the site-map specified.<p>
     * 
     * @param sitePath the site path to the site-map folder
     */
    public void openSiteMap(String sitePath) {

        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(CmsCoreData.PARAM_PATH, sitePath);
        parameter.put(CmsCoreData.PARAM_RETURNCODE, getData().getReturnCode());
        FormElement form = CmsDomUtil.generateHiddenForm(
            CmsCoreProvider.get().link(CmsCoreProvider.get().getUri()),
            Method.post,
            Target.TOP,
            parameter);
        RootPanel.getBodyElement().appendChild(form);
        form.submit();
    }

    /**
     * Recomputes properties for all sitemap entries.<p>
     */
    public void recomputeProperties() {

        CmsClientSitemapEntry root = getData().getRoot();
        recomputeProperties(root);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#registerEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void registerEntry(CmsClientSitemapEntry entry) {

        if (m_entriesById.containsKey(entry.getId())) {
            CmsClientSitemapEntry oldEntry = m_entriesById.get(entry.getId());
            oldEntry.update(entry);
            if (oldEntry != m_entriesByPath.get(oldEntry.getSitePath())) {
                m_entriesByPath.put(oldEntry.getSitePath(), oldEntry);
            }
        } else {
            m_entriesById.put(entry.getId(), entry);
            m_entriesByPath.put(entry.getSitePath(), entry);
        }

    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#registerPathChange(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry, java.lang.String)
     */
    public void registerPathChange(CmsClientSitemapEntry entry, String oldPath) {

        m_entriesById.put(entry.getId(), entry);
        m_entriesByPath.remove(oldPath);
        m_entriesByPath.put(entry.getSitePath(), entry);
    }

    /**
     * Removes the entry with the given site-path from navigation.<p>
     * 
     * @param entryId the entry id
     */
    public void removeFromNavigation(CmsUUID entryId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.remove);
        change.setParentId(parent.getId());
        change.setDefaultFileId(entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        data.addModified(entry);
        change.setClipBoardData(data);
        //TODO: handle detail page delete

        commitChange(change, null);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#replaceProperties(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, CmsClientProperty> replaceProperties(CmsUUID id, Map<String, CmsClientProperty> properties) {

        if ((id == null) || (properties == null)) {
            return null;
        }
        Map<String, CmsClientProperty> props = m_propertyMaps.get(id);
        if (props == null) {
            props = properties;
            m_propertyMaps.put(id, props);
        } else {
            props.clear();
            props.putAll(properties);
        }
        return props;

    }

    /**
     * Undeletes the resource with the given structure id.<p>
     * 
     * @param entryId the entry id
     * @param sitePath the site-path
     */
    public void undelete(CmsUUID entryId, String sitePath) {

        CmsSitemapChange change = new CmsSitemapChange(entryId, sitePath, ChangeType.undelete);
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        data.getDeletions().remove(entryId);
        change.setClipBoardData(data);
        commitChange(change, null);
    }

    /**
     * Updates the given entry.<p>
     * 
     * @param entryId the entry id
      */
    public void updateEntry(CmsUUID entryId) {

        getChildren(entryId, CmsSitemapTreeItem.getItemById(entryId).isOpen(), null);
    }

    /**
    * Updates the given entry.<p>
    * 
    * @param sitePath the entry sitepath
     */
    public void updateEntry(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        getChildren(entry.getId(), CmsSitemapTreeItem.getItemById(entry.getId()).isOpen(), null);
    }

    /**
     * Updates the given entry only, not evaluating any child changes.<p>
     * 
     * @param entryId the entry id
     */
    public void updateSingleEntry(final CmsUUID entryId) {

        CmsRpcAction<CmsClientSitemapEntry> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                getService().getChildren(getEntryPoint(), entryId, 0, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                CmsClientSitemapEntry target = getEntryById(entryId);
                if (target == null) {
                    // this might happen after an automated deletion
                    stop(false);
                    return;
                }
                target.update(result);
                CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(target.getId());
                item.updateEntry(target);
            }
        };
        getChildrenAction.execute();
    }

    /**
     * Fires a sitemap change event.<p>
     * 
     * @param change the change event to fire 
     */
    protected void applyChange(CmsSitemapChange change) {

        // update the clip board data
        if (change.getClipBoardData() != null) {
            getData().getClipboardData().setDeletions(change.getClipBoardData().getDeletions());
            getData().getClipboardData().setModifications(change.getClipBoardData().getModifications());
        }
        switch (change.getChangeType()) {
            case bumpDetailPage:
                CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
                if (detailPageTable.contains(change.getEntryId())) {
                    detailPageTable.bump(change.getEntryId());
                }
                break;
            case clipboardOnly:
                // nothing to do
                break;
            case remove:
                CmsClientSitemapEntry entry = getEntryById(change.getEntryId());
                entry.setInNavigation(false);
                CmsPropertyModification propMod = new CmsPropertyModification(
                    entry.getId(),
                    CmsClientProperty.PROPERTY_NAVTEXT,
                    null,
                    true);
                executePropertyModification(propMod);
                propMod = new CmsPropertyModification(entry.getId(), CmsClientProperty.PROPERTY_NAVTEXT, null, true);
                executePropertyModification(propMod);
                entry.normalizeProperties();
                break;
            case undelete:
            case create:
                CmsClientSitemapEntry newEntry = change.getUpdatedEntry();
                getEntryById(change.getParentId()).insertSubEntry(newEntry, change.getPosition(), this);
                newEntry.initializeAll(this);
                if (isDetailPage(newEntry)) {
                    CmsDetailPageInfo info = getDetailPageInfo(newEntry.getId());
                    if (info == null) {
                        info = new CmsDetailPageInfo(
                            newEntry.getId(),
                            newEntry.getSitePath(),
                            newEntry.getDetailpageTypeName());
                    }
                    addDetailPageInfo(info);
                }
                break;
            case delete:
                removeEntry(change.getEntryId(), change.getParentId());
                break;
            case modify:
                if (change.hasNewParent() || change.hasChangedPosition()) {
                    CmsClientSitemapEntry moved = getEntryById(change.getEntryId());
                    String oldSitepath = moved.getSitePath();
                    CmsClientSitemapEntry sourceParent = getEntry(CmsResource.getParentFolder(oldSitepath));
                    sourceParent.removeSubEntry(moved.getId());
                    CmsClientSitemapEntry destParent = change.hasNewParent()
                    ? getEntryById(change.getParentId())
                    : sourceParent;
                    if (change.getPosition() < destParent.getSubEntries().size()) {
                        destParent.insertSubEntry(moved, change.getPosition(), this);
                    } else {
                        // inserting as last entry of the parent list
                        destParent.addSubEntry(moved, this);
                    }
                    if (change.hasNewParent()) {
                        cleanupOldPaths(oldSitepath, destParent.getSitePath());
                    }
                }
                if (change.hasChangedName()) {
                    CmsClientSitemapEntry changed = getEntryById(change.getEntryId());
                    String oldSitepath = changed.getSitePath();
                    CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(oldSitepath));
                    String newSitepath = CmsStringUtil.joinPaths(parent.getSitePath(), change.getName());
                    changed.updateSitePath(newSitepath, this);
                    cleanupOldPaths(oldSitepath, newSitepath);
                }
                if (change.hasChangedProperties()) {
                    for (CmsPropertyModification modification : change.getPropertyChanges()) {
                        executePropertyModification(modification);
                    }
                    getEntryById(change.getEntryId()).normalizeProperties();
                }
                if (change.getUpdatedEntry() != null) {
                    CmsClientSitemapEntry oldEntry = getEntryById(change.getEntryId());
                    CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(oldEntry.getSitePath()));
                    removeEntry(change.getEntryId(), parent.getId());
                    parent.insertSubEntry(change.getUpdatedEntry(), oldEntry.getPosition(), this);
                    change.getUpdatedEntry().initializeAll(this);
                }
                break;
            default:
        }
        if (change.getChangeType() != ChangeType.delete) {
            recomputeProperties();
        }
        m_eventBus.fireEventFromSource(new CmsSitemapChangeEvent(change), this);
    }

    /**
     * Cleans up wrong path references.<p>
     * 
     * @param oldSitepath the old sitepath
     * @param newSitepath the new sitepath
     */
    private void cleanupOldPaths(String oldSitepath, String newSitepath) {

        // use a separate list to avoid concurrent changes
        List<CmsClientSitemapEntry> entries = new ArrayList<CmsClientSitemapEntry>(m_entriesById.values());
        for (CmsClientSitemapEntry entry : entries) {
            if (entry.getSitePath().startsWith(oldSitepath)) {
                String currentPath = entry.getSitePath();
                String updatedSitePath = CmsStringUtil.joinPaths(
                    newSitepath,
                    currentPath.substring(oldSitepath.length()));
                entry.updateSitePath(updatedSitePath, this);
            }
        }
    }

    /**
    * Adds a change to the queue.<p>
    * 
    * @param change the change to commit
    * @param callback the callback to execute after the change has been applied 
    */
    protected void commitChange(final CmsSitemapChange change, final Command callback) {

        if (change != null) {
            // save the sitemap
            CmsRpcAction<CmsSitemapChange> saveAction = new CmsRpcAction<CmsSitemapChange>() {

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                */
                @Override
                public void execute() {

                    setLoadingMessage(Messages.get().key(Messages.GUI_SAVING_0));
                    start(0, true);
                    getService().saveSync(getEntryPoint(), change, this);

                }

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                */
                @Override
                public void onResponse(CmsSitemapChange result) {

                    stop(true);
                    applyChange(result);
                    if (callback != null) {
                        callback.execute();
                    }
                }
            };
            saveAction.execute();
        }
    }

    /**
     * Creates a change object for an edit operation.<p>
     *  
     * @param entry the edited sitemap entry
     * @param propertyChanges the list of property changes 
     *  
     * @return the change object
     */
    protected CmsSitemapChange getChangeForEdit(
        CmsClientSitemapEntry entry,
        List<CmsPropertyModification> propertyChanges) {

        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
        change.setDefaultFileId(entry.getDefaultFileId());
        change.setLeafType(entry.isLeafType());
        List<CmsPropertyModification> propertyChangeData = new ArrayList<CmsPropertyModification>();
        for (CmsPropertyModification propChange : propertyChanges) {
            propertyChangeData.add(propChange);
        }
        change.setPropertyChanges(propertyChangeData);

        CmsSitemapClipboardData data = getData().getClipboardData().copy();
        data.addModified(entry);
        change.setClipBoardData(data);
        return change;
    }

    /**
     * Returns the URI of the current sitemap.<p>
     * 
     * @return the URI of the current sitemap 
     */
    protected String getEntryPoint() {

        return m_data.getRoot().getSitePath();

    }

    /**
     * Helper method for getting the full path of a sitemap entry whose URL name is being edited.<p> 
     * 
     * @param entry the sitemap entry 
     * @param newUrlName the new url name of the sitemap entry 
     * 
     * @return the new full site path of the sitemap entry 
     */
    protected String getPath(CmsClientSitemapEntry entry, String newUrlName) {

        if (newUrlName.equals("")) {
            return entry.getSitePath();
        }
        return CmsResource.getParentFolder(entry.getSitePath()) + newUrlName + "/";
    }

    /** 
     * Returns the sitemap service instance.<p>
     * 
     * @return the sitemap service instance
     */
    protected I_CmsVfsServiceAsync getVfsService() {

        if (m_vfsService == null) {
            m_vfsService = CmsCoreProvider.getVfsService();
        }
        return m_vfsService;
    }

    /**
     * Initializes the detail page information.<p>
     */
    protected void initDetailPageInfos() {

        for (CmsUUID id : m_data.getDetailPageTable().getAllIds()) {
            CmsDetailPageInfo info = m_data.getDetailPageTable().get(id);
            m_allDetailPageInfos.put(id, info);
        }
    }

    /**
     * Recomputes the properties for a client sitemap entry.<p>
     * 
     * @param entry the entry for whose descendants the properties should be recomputed 
     */
    protected void recomputeProperties(CmsClientSitemapEntry entry) {

        for (I_CmsPropertyUpdateHandler handler : m_propertyUpdateHandlers) {
            handler.handlePropertyUpdate(entry);
        }
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            recomputeProperties(child);
        }
    }

    /**
     * Returns the properties above the sitemap root.<p>
     * 
     * @return the map of properties of the root's parent
     */
    private Map<String, CmsClientProperty> getParentProperties() {

        return m_data.getParentProperties();
    }

    /**
     * Checks if the given path and position indicate a changed position for the entry.<p>
     * 
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     * 
     * @return <code>true</code> if this is a position change
     */
    private boolean isChangedPosition(CmsClientSitemapEntry entry, String toPath, int position) {

        return (!entry.getSitePath().equals(toPath) || (entry.getPosition() != position));
    }

    /**
     * Validates the entry and the given path.<p>
     * 
     * @param entry the entry
     * @param toPath the path
     * 
     * @return <code>true</code> if entry and path are valid
     */
    private boolean isValidEntryAndPath(CmsClientSitemapEntry entry, String toPath) {

        return ((toPath != null)
            && (CmsResource.getParentFolder(toPath) != null)
            && (entry != null)
            && (getEntry(CmsResource.getParentFolder(toPath)) != null) && (getEntry(entry.getSitePath()) != null));
    }

    /**
     * Removes all children of the given entry recursively from the data model.<p>
     * 
     * @param entry the entry
     */
    private void removeAllChildren(CmsClientSitemapEntry entry) {

        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeAllChildren(child);
            m_entriesById.remove(child.getId());
            m_entriesByPath.remove(child.getSitePath());
            // apply to detailpage table
            CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
            if (detailPageTable.contains(child.getId())) {
                detailPageTable.remove(child.getId());
            }
        }
        entry.getSubEntries().clear();
    }

    /**
     * Removes the given entry and it's descendants from the modified list.<p>
     * 
     * @param entry the entry
     * @param data the clip board data
     */
    private void removeDeletedFromModified(CmsClientSitemapEntry entry, CmsSitemapClipboardData data) {

        data.removeModified(entry);
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeletedFromModified(child, data);
        }
    }

    /**
     * Removes the given entry from the data model.<p>
     * 
     * @param entryId the id of the entry to remove
     * @param parentId the parent entry id
     */
    private void removeEntry(CmsUUID entryId, CmsUUID parentId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsClientSitemapEntry deleteParent = getEntryById(parentId);
        removeAllChildren(entry);
        deleteParent.removeSubEntry(entry.getPosition());
        m_entriesById.remove(entryId);
        m_entriesByPath.remove(entry.getSitePath());
        // apply to detailpage table
        CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
        if (detailPageTable.contains(entryId)) {
            detailPageTable.remove(entryId);
        }
    }

}