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
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeBumpDetailPage;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeClearDeleted;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeClearModified;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeCreateSubSitemap;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeDelete;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeEdit;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMergeSitemap;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMove;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeRemove;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeUndelete;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapCompositeChange;
import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EditStatus;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.ade.sitemap.shared.I_CmsSitemapController;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem.LoadState;
import org.opencms.gwt.client.util.CmsDebugLog;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Sitemap editor controller.<p>
 * 
 * @since 8.0.0
 */
public class CmsSitemapController implements I_CmsSitemapController {

    /** The name to use for new entries. */
    public static final String NEW_ENTRY_NAME = "page";

    /** A map of *all* detail page info beans, indexed by page id. */
    protected Map<CmsUUID, CmsDetailPageInfo> m_allDetailPageInfos = new HashMap<CmsUUID, CmsDetailPageInfo>();

    /** The sitemap data. */
    protected CmsSitemapData m_data;

    /** The detail page table. */
    protected CmsDetailPageTable m_detailPageTable;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The list of undone changes. */
    protected List<I_CmsClientSitemapChange> m_undone;

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

        m_undone = new ArrayList<I_CmsClientSitemapChange>();
        m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObject(getService(), CmsSitemapData.DICT_NAME);

        m_hiddenProperties = new HashSet<String>();
        m_detailPageTable = m_data.getDetailPageTable();
        m_data.getRoot().initializeAll(this);
        m_eventBus = new SimpleEventBus();
        initDetailPageInfos();

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
     * @param sitePath the sitepath of the entry
     */
    public void addToNavigation(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        entry.setInNavigation(true);
        CmsClientSitemapCompositeChange change = new CmsClientSitemapCompositeChange();
        CmsPropertyModification mod = new CmsPropertyModification(
            entry.getId(),
            CmsClientProperty.PROPERTY_NAVTEXT,
            entry.getTitle(),
            true);
        change.addChange(getChangeForEdit(entry, sitePath, Collections.singletonList(mod), false));
        change.addChange(getChangeForMove(entry, sitePath, entry.getPosition(), true));
        applyChange(change, null);
    }

    /**
     * Makes the given sitemap entry the default detail page for its detail page type.<p>
     * 
     * @param entry an entry representing a detail page 
     */
    public void bump(CmsClientSitemapEntry entry) {

        CmsClientSitemapChangeBumpDetailPage change = new CmsClientSitemapChangeBumpDetailPage(entry);
        applyChange(change, null);
    }

    /**
     * Clears the deleted clip-board list and commits the change.<p>
     */
    public void clearDeletedList() {

        applyChange(new CmsClientSitemapChangeClearDeleted(), null);
    }

    /**
     * Clears the modified clip-board list and commits the change.<p>
     */
    public void clearModifiedList() {

        applyChange(new CmsClientSitemapChangeClearModified(), null);
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     */
    public void create(final CmsClientSitemapEntry newEntry) {

        create(
            newEntry,
            m_data.getDefaultNewElementInfo().getId(),
            m_data.getDefaultNewElementInfo().getCopyResourceId());
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     * @param resourceTypeId the resource type id
     * @param copyResourceId the copy resource id
     */
    public void create(final CmsClientSitemapEntry newEntry, final int resourceTypeId, final CmsUUID copyResourceId) {

        final CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(newEntry.getSitePath()));
        assert (getEntry(newEntry.getSitePath()) == null);
        assert (parent != null);
        newEntry.setEditStatus(EditStatus.created);
        applyChange(new CmsClientSitemapChangeNew(newEntry, parent.getId(), resourceTypeId, copyResourceId), null);
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     * 
     * @param parent the entry to which a new sub-entry should be added 
     */
    public void createSubEntry(final CmsClientSitemapEntry parent) {

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
                create(newEntry);
            }
        };

        if (item.getLoadState().equals(LoadState.UNLOADED)) {
            getChildren(parent.getSitePath(), true, callback);
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

        CmsRpcAction<CmsSubSitemapInfo> subSitemapAction = new CmsRpcAction<CmsSubSitemapInfo>() {

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
            protected void onResponse(CmsSubSitemapInfo result) {

                stop(false);
                onCreateSubSitemap(entryId, result);
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
        applyChange(new CmsClientSitemapChangeDelete(entry, parent.getId()), null);
    }

    /**
     * Edits the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     * @param vfsReference the new VFS reference, can be <code>null</code> to keep the old one
     * @param propertyChanges the property changes 
     * @param isNew if false, the entry's name has been edited 
     */
    public void edit(
        CmsClientSitemapEntry entry,
        String vfsReference,
        List<CmsPropertyModification> propertyChanges,
        boolean isNew) {

        CmsClientSitemapChangeEdit change = getChangeForEdit(entry, vfsReference, propertyChanges, isNew);
        if (change != null) {
            applyChange(change, null);
        }
    }

    /**
     * Edits an entry and changes its URL name.<p>
     * 
     * @param entry the entry which is being edited 
     * @param newUrlName the new URL name of the entry 
     * @param vfsPath the vfs path of the entry 
     * @param propertyChanges the property changes  
     * @param editedName true if the name has been edited
     * @param reloadStatus a value indicating which entries need to be reloaded after the change   
     */
    public void editAndChangeName(
        final CmsClientSitemapEntry entry,
        String newUrlName,
        String vfsPath,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName,
        final CmsReloadMode reloadStatus) {

        CmsClientSitemapChangeEdit edit = getChangeForEdit(entry, vfsPath, propertyChanges, !editedName);
        CmsClientSitemapChangeMove move = getChangeForMove(
            entry,
            getPath(entry, newUrlName),
            entry.getPosition(),
            false);
        CmsClientSitemapCompositeChange change = new CmsClientSitemapCompositeChange();
        change.addChange(edit);
        change.addChange(move);
        AsyncCallback<Object> callback = new AsyncCallback<Object>() {

            public void onFailure(Throwable caught) {

                // do nothing 
            }

            public void onSuccess(Object result) {

                switch (reloadStatus) {
                    case reloadEntry:
                        updateEntry(entry.getSitePath());
                        break;
                    case reloadParent:
                        CmsClientSitemapEntry parent = getParentEntry(entry);
                        if (parent != null) {
                            updateEntry(parent.getSitePath());
                        }
                        break;
                    case none:
                    default:
                        break;
                }
            }

        };
        applyChange(change, callback);

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

        Map<String, CmsClientProperty> props = getPropertiesForId(propMod.getId());
        if (props != null) {
            propMod.updatePropertyInMap(props);
        }

    }

    /**
     * Retrieves the child entries of the given node from the server.<p>
     * 
     * @param sitePath the site path
     * @param setOpen if the entry should be opened
     * @param callback the callback to execute after the children have been loaded 
     */
    public void getChildren(
        final String sitePath,
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

                getService().getChildren(getEntryPoint(), sitePath, 1, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                CmsClientSitemapEntry target = getEntry(sitePath);
                if (target == null) {
                    // this might happen after an automated deletion
                    stop(false);
                    return;
                }

                target.setSubEntries(result.getSubEntries());
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

        CmsClientSitemapEntry root = m_data.getRoot();
        if (!entryPath.startsWith(root.getSitePath())) {
            return null;
        }
        String path = entryPath.substring(root.getSitePath().length());
        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsClientSitemapEntry result = root;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            boolean found = false;
            for (CmsClientSitemapEntry child : result.getSubEntries()) {
                if (child.getName().equals(name)) {
                    found = true;
                    result = child;
                    break;
                }
            }
            if (!found) {
                // not found
                result = null;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the entry which has the file with the given resource id as a default file, or null if there is no such entry.<p>
     * 
     * @param id a structure id
     *  
     * @return a sitemap entry, or null
     */
    public CmsClientSitemapEntry getEntryByDefaultFileId(CmsUUID id) {

        List<CmsClientSitemapEntry> entriesToProcess = new ArrayList<CmsClientSitemapEntry>();
        entriesToProcess.add(m_data.getRoot());
        while (!entriesToProcess.isEmpty()) {
            CmsClientSitemapEntry entry = entriesToProcess.remove(entriesToProcess.size() - 1);
            if (id.equals(entry.getDefaultFileId())) {
                return entry;
            }
            entriesToProcess.addAll(entry.getSubEntries());
        }
        return null;

    }

    /**
     * Finds an entry by id.<p>
     * 
     * @param id the id of the entry to find 
     * 
     * @return the found entry, or null if the entry wasn't found 
     */
    public CmsClientSitemapEntry getEntryById(CmsUUID id) {

        List<CmsUUID> ids = new ArrayList<CmsUUID>();
        ids.add(id);
        Map<CmsUUID, CmsClientSitemapEntry> map = getEntriesById(ids);
        if (map.isEmpty()) {
            return null;
        } else {
            return map.values().iterator().next();
        }
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

        String sitemapLocation = CmsCoreProvider.get().getUri() + "?path=" + getData().getParentSitemap();
        String returnCode = Window.Location.getParameter(CmsCoreProvider.PARAM_RETURNCODE);
        if ((returnCode != null) && (returnCode.length() != 0)) {
            sitemapLocation += "&returncode=" + returnCode;
        }
        leaveEditor(sitemapLocation);
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
    public void leaveEditor(final String target) {

        Window.Location.assign(CmsCoreProvider.get().link(target));
    }

    /**
    * Merges a subsitemap at the given id back into this sitemap.<p>
    * 
    * @param entryId the id of the sub sitemap entry 
    */
    public void mergeSubSitemap(final CmsUUID entryId) {

        CmsRpcAction<CmsSitemapMergeInfo> mergeAction = new CmsRpcAction<CmsSitemapMergeInfo>() {

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
            protected void onResponse(CmsSitemapMergeInfo result) {

                stop(false);
                CmsClientSitemapEntry target = getEntryById(entryId);
                I_CmsClientSitemapChange change = new CmsClientSitemapChangeMergeSitemap(target, result);
                executeChange(change);

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

        CmsClientSitemapChangeMove change = getChangeForMove(entry, toPath, position, false);
        if (change != null) {
            applyChange(change, null);
        }
    }

    /**
     * Recomputes properties for all sitemap entries.<p>
     */
    public void recomputeProperties() {

        CmsClientSitemapEntry root = getData().getRoot();
        recomputeProperties(root);
    }

    /**
     * Removes the entry with the given site-path from navigation.<p>
     * 
     * @param sitePath the site-path
     */
    public void removeFromNavigation(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        applyChange(new CmsClientSitemapChangeRemove(entry, parent.getId()), null);
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
     * @param structureId the structure id
     * @param sitePath the site-path
     */
    public void undelete(final CmsUUID structureId, final String sitePath) {

        applyChange(new CmsClientSitemapChangeUndelete(structureId, sitePath), null);
    }

    /**
    * Updates the given entry.<p>
    * 
    * @param sitePath the entry sitepath
     */
    public void updateEntry(String sitePath) {

        getChildren(sitePath, CmsSitemapTreeItem.getItemById(getEntry(sitePath).getId()).isOpen(), null);
    }

    /**
    * Adds a change to the queue.<p>
    * 
    * @param change the change to be added  
    * @param callback the callback to execute after the change has been applied 
    */
    protected void applyChange(final I_CmsClientSitemapChange change, final AsyncCallback<Object> callback) {

        final CmsSitemapChange commitChange = change.getChangeForCommit();
        if (commitChange != null) {
            // save the sitemap
            CmsRpcAction<List<CmsClientSitemapEntry>> saveAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                */
                @Override
                public void execute() {

                    setLoadingMessage(Messages.get().key(Messages.GUI_SAVING_0));
                    start(0, true);
                    getService().saveSync(getEntryPoint(), commitChange, this);

                }

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                */
                @Override
                public void onResponse(List<CmsClientSitemapEntry> result) {

                    stop(true);
                    if ((result != null) && !result.isEmpty()) {
                        for (CmsClientSitemapEntry entry : result) {
                            change.updateEntry(entry);
                        }
                    }
                    change.applyToModel(CmsSitemapController.this);
                    fireChange(change);
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                }
            };
            saveAction.execute();
        }
    }

    /**
     * Internal method which updates the model with a single change.<p>
     * 
     * @param change the change 
     */
    protected void executeChange(I_CmsClientSitemapChange change) {

        // apply change to the model
        change.applyToModel(this);
        // refresh view
        fireChange(change);
    }

    /**
     * Fires a sitemap change event.<p>
     * 
     * @param change the change event to fire 
     */
    protected void fireChange(I_CmsClientSitemapChange change) {

        m_eventBus.fireEventFromSource(new CmsSitemapChangeEvent(change), this);
        recomputeProperties();
    }

    /**
     * Creates a change object for an edit operation.<p>
     *  
     * @param entry the edited sitemap entry
     * @param vfsReference the vfs path 
     * @param propertyChanges the list of property changes 
     * @param isNew true if the entry's url name has not been edited before
     *  
     * @return the change object
     */
    protected CmsClientSitemapChangeEdit getChangeForEdit(
        CmsClientSitemapEntry entry,
        String vfsReference,
        List<CmsPropertyModification> propertyChanges,
        boolean isNew) {

        // check changes
        boolean changedVfsRef = ((vfsReference != null) && !vfsReference.trim().equals(entry.getVfsPath()));
        boolean changedProperties = true;
        if (!changedVfsRef && !changedProperties && isNew) {
            // nothing to do
            return null;
        }

        // create changes
        CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry(entry);
        newEntry.setEdited();

        // We don't calculate isNew by comparing the old and new url names.
        // This is because any editing of the URL name field should mark the entry as "not new", even if the final
        // value is the same as the initial one. 
        newEntry.setNew(entry.isNew() && isNew);
        if (changedVfsRef) {
            newEntry.setVfsPath(vfsReference);
        }

        // apply changes
        return new CmsClientSitemapChangeEdit(entry, propertyChanges, newEntry);

    }

    /**
     * Returns a change object for a move operation.<p>
     *  
     * @param entry the entry being moved 
     * @param toPath the target path of the move operation 
     * @param position the target position of the move operation
     * @param forceMove <code>true</code> forces a move change, even if the position is unchanged 
     * (needed to add a formerly hidden entry to navigation)
     *  
     * @return the change object
     */
    protected CmsClientSitemapChangeMove getChangeForMove(
        CmsClientSitemapEntry entry,
        String toPath,
        int position,
        boolean forceMove) {

        // check for valid data
        if (!isValidEntryAndPath(entry, toPath)) {
            // invalid data, do nothing 
            CmsDebugLog.getInstance().printLine("invalid data, doing nothing");
            return null;
        }
        // check for relevance
        if (forceMove || isChangedPosition(entry, toPath, position)) {
            // only register real changes
            return new CmsClientSitemapChangeMove(
                entry,
                toPath,
                getEntry(CmsResource.getParentFolder(toPath)).getId(),
                position);
        }
        return null;
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
     * Internal method which is called when a new sub-sitemap has been successfully created.<p>
     * 
     * @param entryId the id of the created sub-sitemap entry
     * @param info the info bean which is the result of the sub-sitemap creation  
    
     */
    protected void onCreateSubSitemap(CmsUUID entryId, CmsSubSitemapInfo info) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsClientSitemapChangeCreateSubSitemap change = new CmsClientSitemapChangeCreateSubSitemap(entry, info);
        executeChange(change);
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
}