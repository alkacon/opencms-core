/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/control/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2011/01/14 14:19:54 $
 * Version: $Revision: 1.35 $
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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeBumpDetailPage;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeCreateSubSitemap;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeDelete;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeEdit;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMove;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapCompositeChange;
import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsBrokenLinkData;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EditStatus;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsSingleResourceLock;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.CmsDetailPageTable;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsPropertyInheritanceState;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.35 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapController {

    /**
     * Debug class used for debugging property updates by changing the color of the affected sitemap entries.<p>
     */
    public static class DebugPropertyUpdateHandler implements I_CmsPropertyUpdateHandler {

        /**
         * @see org.opencms.ade.sitemap.client.control.I_CmsPropertyUpdateHandler#handlePropertyUpdate(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
         */
        public void handlePropertyUpdate(CmsClientSitemapEntry entry) {

            Map<String, CmsComputedPropertyValue> myProps = entry.getInheritedProperties();
            String color = "gray";
            if (myProps.containsKey("color")) {
                color = myProps.get("color").getOwnValue();
            }
            CmsSitemapTreeItem item = CmsSitemapView.getInstance().getTreeItem(entry.getSitePath());
            com.google.gwt.dom.client.Style style = item.getListItemWidget().getContentPanel().getElement().getStyle();
            style.setBackgroundImage("none");
            style.setBackgroundColor(color);
        }
    }

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

    /** The lock for the configuration file. */
    CmsSingleResourceLock m_configLock;

    /** The lock for the sitemap file. */
    CmsSingleResourceLock m_sitemapLock;

    /** The set of names of hidden properties. */
    private Set<String> m_hiddenProperties;

    /** The list of property update handlers. */
    private List<I_CmsPropertyUpdateHandler> m_propertyUpdateHandlers = new ArrayList<I_CmsPropertyUpdateHandler>();

    /** Object for keeping track of and updating redirects. */
    private CmsRedirectUpdater m_redirectUpdater;

    /** The sitemap service instance. */
    private I_CmsSitemapServiceAsync m_service;

    /**
     * Constructor.<p>
     */
    public CmsSitemapController() {

        m_redirectUpdater = new CmsRedirectUpdater();

        m_undone = new ArrayList<I_CmsClientSitemapChange>();
        m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObject(getService(), CmsSitemapData.DICT_NAME);

        m_hiddenProperties = new HashSet<String>();
        m_hiddenProperties.add(CmsSitemapManager.Property.sitemap.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.internalRedirect.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.externalRedirect.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.isRedirect.toString());
        m_detailPageTable = m_data.getDetailPageTable();
        RootPanel.get().add(new Label("detail pages: " + m_detailPageTable.size()));
        m_configLock = new CmsSingleResourceLock(m_data.getConfigPath());
        m_sitemapLock = new CmsSingleResourceLock(CmsCoreProvider.get().getUri());
        m_eventBus = new SimpleEventBus();
        initDetailPageInfos();

    }

    /**
     * Ensure the uniqueness of a given URL-name within the children of the given parent site-map entry.<p>
     * 
     * @param parent the parent entry
     * @param newName the proposed name
     * 
     * @return the unique name
     */
    public static String ensureUniqueName(CmsClientSitemapEntry parent, String newName) {

        Set<String> otherUrlNames = new HashSet<String>();
        if (parent == null) {
            CmsDebugLog.getInstance().printLine("Parent ==null");
            return newName;

        }
        if (parent.getSubEntries() == null) {
            CmsDebugLog.getInstance().printLine("No siblings");
            return newName;
        }
        for (CmsClientSitemapEntry sibling : parent.getSubEntries()) {
            otherUrlNames.add(sibling.getName());
        }
        int counter = 0;
        String newUrlName = newName;
        // check if the new name contains a counter suffix
        if (newName.matches(".*_[0-9]+")) {
            int underscoreIndex = newName.lastIndexOf("_");
            counter = Integer.parseInt(newName.substring(underscoreIndex + 1));
            newName = newName.substring(0, underscoreIndex);
        }
        while (otherUrlNames.contains(newUrlName)) {
            counter += 1;
            newUrlName = newName + "_" + counter;
        }
        return newUrlName;
    }

    /**
     * Returns the URI of the current sitemap.<p>
     * 
     * @return the URI of the current sitemap 
     */
    protected static String getSitemapUri() {

        return CmsCoreProvider.get().getUri();

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
     * Ask to save the page before leaving, if necessary.<p>
     * 
     * @param target the leaving target
     */
    public void leaveEditor(final String target) {

        Window.Location.assign(CmsCoreProvider.get().link(target));
    }

    /**
     * Makes the given sitemap entry the default detail page for its detail page type.<p>
     * 
     * @param entry an entry representing a detail page 
     */
    public void bump(CmsClientSitemapEntry entry) {

        CmsClientSitemapChangeBumpDetailPage change = new CmsClientSitemapChangeBumpDetailPage(entry);
        applyChange(change, false);
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     */
    public void create(final CmsClientSitemapEntry newEntry) {

        final CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(newEntry.getSitePath()));
        assert (getEntry(newEntry.getSitePath()) == null);
        assert (parent != null);
        newEntry.setEditStatus(EditStatus.created);
        if (newEntry.getId() == null) {
            // get a new valid UUID from server
            CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                 */
                @Override
                public void execute() {

                    start(0, true);
                    CmsCoreProvider.getService().createUUID(this);
                }

                /**
                 * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                 */
                @Override
                protected void onResponse(CmsUUID result) {

                    stop(false);
                    newEntry.setId(result);
                    applyChange(new CmsClientSitemapChangeNew(newEntry, parent.getId()), false);
                }
            };
            action.execute();
        } else {
            applyChange(new CmsClientSitemapChangeNew(newEntry, parent.getId()), false);
        }
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     * 
     * @param entry the entry to which a new sub-entry should be added 
     */
    public void createSubEntry(CmsClientSitemapEntry entry) {

        CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry();
        String urlName = generateUrlName(entry);
        newEntry.setTitle(urlName);
        newEntry.setName(urlName);
        String sitePath = entry.getSitePath() + urlName + "/";
        newEntry.setSitePath(sitePath);
        newEntry.setVfsPath(null);
        newEntry.setPosition(0);
        newEntry.setNew(true);
        create(newEntry);
        // leave properties empty
    }

    /**
     * Creates a sub-sitemap from the subtree of the current sitemap starting at a given path.<p>
     * 
     * @param path the path whose subtree should be converted to a sub-sitemap 
     */
    public void createSubSitemap(final String path) {

        //        CmsRpcAction<CmsSubSitemapInfo> subSitemapAction = new CmsRpcAction<CmsSubSitemapInfo>() {
        //
        //            /**
        //             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
        //             */
        //            @Override
        //            public void execute() {
        //
        //                start(0, true);
        //                List<CmsSitemapChange> changes = getChangesToSave();
        //                getService().saveAndCreateSubSitemap(getSitemapUri(), changes, path, this);
        //
        //            }
        //
        //            /**
        //             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
        //             */
        //            @Override
        //            protected void onResponse(CmsSubSitemapInfo result) {
        //
        //                stop(false);
        //                resetChanges();
        //                markAllEntriesAsOld();
        //                onCreateSubSitemap(path, result);
        //
        //            }
        //        };
        //        if (CmsCoreProvider.get().lockAndCheckModification(getSitemapUri(), m_data.getTimestamp())) {
        //            subSitemapAction.execute();
        //        }
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     * 
     * @param sitePath the site path of the entry to delete
     */
    public void delete(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        assert (entry != null);
        applyChange(new CmsClientSitemapChangeDelete(entry, parent.getId()), false);
    }

    /**
     * Edits the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     * @param title the new title, can be <code>null</code> to keep the old one
     * @param vfsReference the new VFS reference, can be <code>null</code> to keep the old one
     * @param properties the new properties, can be <code>null</code> to keep the old properties
     * @param isNew if false, the entry's name has been edited 
     */
    public void edit(
        CmsClientSitemapEntry entry,
        String title,
        String vfsReference,
        Map<String, CmsSimplePropertyValue> properties,
        boolean isNew) {

        CmsClientSitemapChangeEdit change = getChangeForEdit(entry, title, vfsReference, properties, isNew);
        if (change != null) {
            applyChange(change, false);
        }
    }

    /**
     * Edits an entry and changes its URL name.<p>
     * 
     * @param entry the entry which is being edited 
     * @param newTitle the new title of the entry 
     * @param newUrlName the new URL name of the entry 
     * @param vfsPath the vfs path of the entry 
     * @param fieldValues the new properties of the entry 
     * @param editedName true if the name has been edited 
     */
    public void editAndChangeName(
        CmsClientSitemapEntry entry,
        String newTitle,
        String newUrlName,
        String vfsPath,
        Map<String, CmsSimplePropertyValue> fieldValues,
        boolean editedName) {

        CmsClientSitemapChangeEdit edit = getChangeForEdit(entry, newTitle, vfsPath, fieldValues, !editedName);
        CmsClientSitemapChangeMove move = getChangeForMove(entry, getPath(entry, newUrlName), entry.getPosition());
        CmsClientSitemapCompositeChange change = new CmsClientSitemapCompositeChange();
        change.addChange(edit);
        change.addChange(move);
        applyChange(change, false);

    }

    /**
     * Fetches broken link data bean, containing a list of all not yet loaded sub elements and a list of beans 
     * which represent the links that would be broken if the sitemap entries
     * in the "open" list and the descendants of the sitemap entries in the "closed" list were deleted.<p>
     * 
     * @param deleteEntry the entry to delete 
     * @param open the list of sitemap entry ids which should be considered without their descendants  
     * @param closed the list of sitemap entry ids which should be considered with their descendantw 
     * 
     * @param callback the callback which will be called with the results 
     */
    public void getBrokenLinks(
        final CmsClientSitemapEntry deleteEntry,
        final List<CmsUUID> open,
        final List<CmsUUID> closed,
        final AsyncCallback<List<CmsSitemapBrokenLinkBean>> callback) {

        CmsRpcAction<CmsBrokenLinkData> action = new CmsRpcAction<CmsBrokenLinkData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().getBrokenLinksToSitemapEntries(deleteEntry, open, closed, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsBrokenLinkData result) {

                stop(false);
                addChildren(deleteEntry, result.getClosedEntries());
                callback.onSuccess(result.getBrokenLinks());
            }
        };
        action.execute();
    }

    /**
     * Retrieves the children entries of the given node from the server.<p>
     * 
     * @param originalPath the original site path of the sitemap entry to get the children for
     * @param sitePath the current site path, in case if has been moved or renamed
     */
    public void getChildren(final String originalPath, final String sitePath) {

        CmsRpcAction<List<CmsClientSitemapEntry>> getChildrenAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500, false);

                getService().getChildren(getSitemapUri(), originalPath, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> result) {

                CmsClientSitemapEntry target = getEntry(sitePath);
                if (target == null) {
                    // this might happen after an automated deletion
                    stop(false);
                    recomputePropertyInheritance();
                    return;
                }
                target.setSubEntries(result);
                if (!originalPath.equals(sitePath)) {
                    target.setSitePath("abc"); // hack to be able to execute updateSitePath
                    target.updateSitePath(sitePath);
                }
                m_eventBus.fireEventFromSource(new CmsSitemapLoadEvent(target, originalPath), CmsSitemapController.this);
                stop(false);
                recomputePropertyInheritance();
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
     * This method returns the default template for a given sitemap path.<p>
     * 
     * Starting from the given path, it traverses the ancestors of the entry to find a sitemap 
     * entry with a non-null 'template-inherited' property value, and then returns this value.
     * 
     * @param sitemapPath the sitemap path for which the default template should be returned
     *  
     * @return the default template 
     */
    public CmsSitemapTemplate getDefaultTemplate(String sitemapPath) {

        if ((sitemapPath == null) || sitemapPath.equals("")) {
            return m_data.getDefaultTemplate();
        }

        CmsClientSitemapEntry entry = getEntry(sitemapPath);
        if (entry == null) {
            return m_data.getDefaultTemplate();
        }
        CmsComputedPropertyValue templateInherited = entry.getInheritedProperties().get(
            CmsSitemapManager.Property.template.name());
        if (templateInherited != null) {
            return m_data.getTemplates().get(templateInherited.getOwnValue());
        }
        return m_data.getDefaultTemplate();
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
     * Returns the redirect updater.<p>
     * 
     * @return the redirect updater
     */
    public CmsRedirectUpdater getRedirectUpdater() {

        return m_redirectUpdater;
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
     * Marks all entries as old.<p>
     * 
     * This means editing those entries' titles will not change their URL names.
     */
    public void markAllEntriesAsOld() {

        CmsClientSitemapEntry root = m_data.getRoot();
        LinkedList<CmsClientSitemapEntry> entriesToProcess = new LinkedList<CmsClientSitemapEntry>();
        entriesToProcess.add(root);
        while (!entriesToProcess.isEmpty()) {
            CmsClientSitemapEntry entry = entriesToProcess.removeFirst();
            entry.setNew(false);
            entry.setEditStatus(EditStatus.normal);
            CmsSitemapView.getInstance().getTreeItem(entry.getSitePath()).updateColor(entry);
            entriesToProcess.addAll(entry.getSubEntries());
        }
    }

    /**
     * Moves the given sitemap entry with all its descendants to the new position.<p>
     * 
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(CmsClientSitemapEntry entry, String toPath, int position) {

        CmsClientSitemapChangeMove change = getChangeForMove(entry, toPath, position);
        if (change != null) {
            applyChange(change, false);
        }
    }

    /**
     * Recomputes the inherited properties for the whole loaded portion of the tree.<p>
     */
    public void recomputePropertyInheritance() {

        Map<String, CmsXmlContentProperty> propertyConfig = m_data.getProperties();
        Map<String, CmsComputedPropertyValue> parentProperties = m_data.getParentProperties();
        CmsPropertyInheritanceState propState = new CmsPropertyInheritanceState(parentProperties, propertyConfig, false);
        recomputeProperties(m_data.getRoot(), propState);
        CmsSitemapView.getInstance().getTree().getItem(0).updateSitePath();

    }

    /**
     * Merges a subsitemap at the given path back into this sitemap.<p>
     * 
     * @param path the path at which the sitemap should be merged into the current sitemap 
     */
    public void saveAndMergeSubSitemap(final String path) {

        //        CmsRpcAction<CmsSitemapMergeInfo> mergeAction = new CmsRpcAction<CmsSitemapMergeInfo>() {
        //
        //            /**
        //             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
        //             */
        //            @Override
        //            public void execute() {
        //
        //                start(0, true);
        //                List<CmsSitemapChange> changes = getChangesToSave();
        //                getService().saveAndMergeSubSitemap(getSitemapUri(), changes, path, this);
        //            }
        //
        //            /**
        //             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
        //             */
        //            @Override
        //            protected void onResponse(CmsSitemapMergeInfo result) {
        //
        //                stop(false);
        //                resetChanges();
        //                CmsClientSitemapEntry target = getEntry(path);
        //                I_CmsClientSitemapChange change = new CmsClientSitemapChangeMergeSitemap(path, target, result);
        //                executeChange(change);
        //
        //            }
        //        };
        //        CmsClientSitemapEntry entry = getEntry(path);
        //
        //        CmsSimplePropertyValue sitemapProp = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
        //        String sitemapVal = sitemapProp == null ? null : sitemapProp.getOwnValue();
        //        if (CmsCoreProvider.get().lockAndCheckModification(getSitemapUri(), m_data.getTimestamp())
        //            && CmsCoreProvider.get().lock(sitemapVal)) {
        //            mergeAction.execute();
        //        }
    }

    /**
    * Adds a change to the queue.<p>
    * 
    * @param change the change to be added  
    * @param redo if redoing a change
    */
    protected void applyChange(final I_CmsClientSitemapChange change, boolean redo) {

        // save the sitemap
        CmsRpcAction<Boolean> saveAction = new CmsRpcAction<Boolean>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                List<CmsSitemapChange> changes = new ArrayList<CmsSitemapChange>();
                changes.add(change.getChangeForCommit());

                setLoadingMessage(Messages.get().key(Messages.GUI_SAVING_0));
                start(0, true);
                getService().saveSync(getSitemapUri(), changes, getData().getClipboardData(), this);

            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Boolean result) {

                if (result.booleanValue()) {
                    stop(true);
                    change.applyToModel(CmsSitemapController.this);
                    fireChange(change);
                } else {
                    stop(false);
                    CmsNotification.get().send(Type.WARNING, "Could not apply changes");
                }
            }
        };
        saveAction.execute();
    }

    /**
     * Adds loaded child entries to the given sub-tree.<p>
     * 
     * @param parent the start element of the sub-tree
     * @param loadedEntries the loaded entries
     */
    protected void addChildren(CmsClientSitemapEntry parent, List<CmsClientSitemapEntry> loadedEntries) {

        for (CmsClientSitemapEntry child : parent.getSubEntries()) {
            if (child.getSubEntries().size() == 0) {
                Iterator<CmsClientSitemapEntry> it = loadedEntries.iterator();
                while (it.hasNext()) {
                    CmsClientSitemapEntry closed = it.next();
                    if (closed.getId().equals(child.getId())) {
                        child.setSubEntries(closed.getSubEntries());
                        for (CmsClientSitemapEntry grandChild : closed.getSubEntries()) {
                            CmsSitemapView.getInstance().createSitemapItem(grandChild);
                        }
                        it.remove();
                    }
                }
            } else {
                addChildren(child, loadedEntries);
            }
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
        recomputePropertyInheritance();
    }

    /**
     * Creates a change object for an edit operation.<p>
     *  
     * @param entry the edited sitemap entry
     * @param title the title 
     * @param vfsReference the vfs path 
     * @param properties the properties 
     * @param isNew true if the entry's url name has not been edited before
     *  
     * @return the change object
     */
    protected CmsClientSitemapChangeEdit getChangeForEdit(
        CmsClientSitemapEntry entry,
        String title,
        String vfsReference,
        Map<String, CmsSimplePropertyValue> properties,
        boolean isNew) {

        // check changes
        boolean changedTitle = ((title != null) && !title.trim().equals(entry.getTitle()));
        boolean changedVfsRef = ((vfsReference != null) && !vfsReference.trim().equals(entry.getVfsPath()));

        Map<String, CmsSimplePropertyValue> oldProps = new HashMap<String, CmsSimplePropertyValue>(
            entry.getProperties());
        Map<String, CmsSimplePropertyValue> newProps = new HashMap<String, CmsSimplePropertyValue>(
            entry.getProperties());

        CmsCollectionUtil.updateMapAndRemoveNulls(properties, newProps);
        boolean changedProperties = !oldProps.equals(newProps);
        //TODO: fix property comparison   
        if (!changedTitle && !changedVfsRef && !changedProperties && isNew) {
            // nothing to do
            return null;
        }

        // create changes
        CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry(entry);
        if (changedTitle) {
            newEntry.setTitle(title);
        }
        newEntry.setEdited();

        // We don't calculate isNew by comparing the old and new url names.
        // This is because any editing of the URL name field should mark the entry as "not new", even if the final
        // value is the same as the initial one. 
        newEntry.setNew(entry.isNew() && isNew);
        if (changedVfsRef) {
            newEntry.setVfsPath(vfsReference);
        }
        if (changedProperties) {
            newEntry.setProperties(newProps);
        }

        // apply changes
        return new CmsClientSitemapChangeEdit(entry, newEntry);

    }

    /**
     * Returns a change object for a move operation.<p>
     *  
     * @param entry the entry being moved 
     * @param toPath the target path of the move operation 
     * @param position the target position of the move operation
     *  
     * @return the change object
     */
    protected CmsClientSitemapChangeMove getChangeForMove(CmsClientSitemapEntry entry, String toPath, int position) {

        // check for valid data
        if (!isValidEntryAndPath(entry, toPath)) {
            // invalid data, do nothing 
            CmsDebugLog.getInstance().printLine("invalid data, doing nothing");
            return null;
        }
        // check for relevance
        if (isChangedPosition(entry, toPath, position)) {
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
    protected I_CmsSitemapServiceAsync getService() {

        if (m_service == null) {
            m_service = GWT.create(I_CmsSitemapService.class);
        }
        return m_service;
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
     * @param path the path in the current sitemap at which the sub-sitemap has been created
     * @param info the info bean which is the result of the sub-sitemap creation  
    
     */
    protected void onCreateSubSitemap(String path, CmsSubSitemapInfo info) {

        CmsClientSitemapEntry entry = getEntry(path);
        CmsClientSitemapChangeCreateSubSitemap change = new CmsClientSitemapChangeCreateSubSitemap(entry, info);
        executeChange(change);
    }

    /**
     * Recomputes the properties for a client sitemap entry.<p>
     * 
     * @param entry the sitemap entry whose properties should be updated 
     * @param propState the property state of the entry's parent 
     */
    protected void recomputeProperties(CmsClientSitemapEntry entry, CmsPropertyInheritanceState propState) {

        CmsPropertyInheritanceState myState = propState.update(entry.getProperties(), entry.getSitePath());
        Map<String, CmsComputedPropertyValue> myProps = new HashMap<String, CmsComputedPropertyValue>(
            myState.getInheritedProperties());
        Map<String, CmsComputedPropertyValue> myParentProps = new HashMap<String, CmsComputedPropertyValue>(
            propState.getInheritedProperties());
        entry.setParentInheritedProperties(myParentProps);
        entry.setInheritedProperties(myProps);
        for (I_CmsPropertyUpdateHandler handler : m_propertyUpdateHandlers) {
            handler.handlePropertyUpdate(entry);
        }

        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            recomputeProperties(child, myState);
        }
        //CmsSitemapView.getInstance().getTreeItem(entry.getSitePath());
    }

    /**
     * Generates an URL name for a new child entry which is being added to a given sitemap entry.<p>
     *  
     * @param entry the sitemap entry to which a new child entry is being added
     *   
     * @return the generated URL name 
     */
    private String generateUrlName(CmsClientSitemapEntry entry) {

        Set<String> subUrlNames = new HashSet<String>();
        for (CmsClientSitemapEntry subEntry : entry.getSubEntries()) {
            subUrlNames.add(subEntry.getName());
        }
        int counter = 1;
        String prefix = "item_";
        while (subUrlNames.contains(prefix + counter)) {
            counter += 1;
        }
        return prefix + counter;
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