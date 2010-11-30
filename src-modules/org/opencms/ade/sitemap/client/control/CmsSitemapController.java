/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/control/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2010/11/30 08:56:13 $
 * Version: $Revision: 1.33 $
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
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeCreateSubSitemap;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeDelete;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeEdit;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMergeSitemap;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMove;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapCompositeChange;
import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsBrokenLinkData;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapBrokenLinkBean;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EditStatus;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsPropertyInheritanceState;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
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

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.33 $ 
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

    /** The list of changes. */
    protected List<I_CmsClientSitemapChange> m_changes;

    /** The sitemap data. */
    protected CmsSitemapData m_data;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The list of undone changes. */
    protected List<I_CmsClientSitemapChange> m_undone;

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

        m_changes = new ArrayList<I_CmsClientSitemapChange>();
        m_undone = new ArrayList<I_CmsClientSitemapChange>();
        m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObject(getService(), CmsSitemapData.DICT_NAME);

        m_hiddenProperties = new HashSet<String>();
        m_hiddenProperties.add(CmsSitemapManager.Property.sitemap.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.internalRedirect.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.externalRedirect.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.isRedirect.toString());
        m_eventBus = new SimpleEventBus();

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
     * Adds a new clear undo event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addClearUndoHandler(I_CmsSitemapClearUndoHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapClearUndoEvent.getType(), this, handler);
    }

    /**
     * Adds a new first undo event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addFirstUndoHandler(I_CmsSitemapFirstUndoHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapFirstUndoEvent.getType(), this, handler);
    }

    /**
     * Adds a new last redo event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addLastRedoHandler(I_CmsSitemapLastRedoHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapLastRedoEvent.getType(), this, handler);
    }

    /**
     * Adds a new last undo event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addLastUndoHandler(I_CmsSitemapLastUndoHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapLastUndoEvent.getType(), this, handler);
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
     * Adds a new reset event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addResetHandler(I_CmsSitemapResetHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapResetEvent.getType(), this, handler);
    }

    /**
     * Adds a new start edit event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addStartEditHandler(I_CmsSitemapStartEditHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapStartEditEvent.getType(), this, handler);
    }

    /**
     * Ask to save the page before leaving, if necessary.<p>
     * 
     * @param target the leaving target
     */
    public void askToLeave(final String target) {

        if (hasChanges()) {
            CmsConfirmDialog dialog = new CmsConfirmDialog(
                Messages.get().key(Messages.GUI_CONFIRM_LEAVING_TITLE_0),
                Messages.get().key(Messages.GUI_CONFIRM_DIRTY_LEAVING_0));
            dialog.setOkText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_SAVE_0));
            dialog.setCloseText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
            dialog.setHandler(new I_CmsConfirmDialogHandler() {

                /**
                 * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
                 */
                public void onClose() {

                    // doing nothing
                }

                /**
                 * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
                 */
                public void onOk() {

                    saveAndLeavePage(target);
                }
            });
            dialog.center();
        } else {
            Window.Location.assign(CmsCoreProvider.get().link(target));
        }
    }

    /**
     * Commits the changes.<p>
     * 
     * @param sync if to use a synchronized or an asynchronized request
     */
    public void commit(final boolean sync) {

        // save the sitemap
        CmsRpcAction<Long> saveAction = new CmsRpcAction<Long>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                setLoadingMessage(Messages.get().key(Messages.GUI_SAVING_0));
                start(0, true);
                List<I_CmsSitemapChange> changes = getChangesToSave();
                if (sync) {
                    getService().saveSync(getSitemapUri(), changes, getData().getClipboardData(), this);
                } else {
                    getService().save(getSitemapUri(), changes, getData().getClipboardData(), this);
                }
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Long result) {

                m_data.setTimestamp(result.longValue());
                markAllEntriesAsOld();
                resetChanges();

                stop(true);
            }
        };
        saveAction.execute();
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     */
    public void create(final CmsClientSitemapEntry newEntry) {

        assert (getEntry(newEntry.getSitePath()) == null);
        assert (getEntry(CmsResource.getParentFolder(newEntry.getSitePath())) != null);
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
                    addChange(new CmsClientSitemapChangeNew(newEntry), false);
                }
            };
            action.execute();
        } else {
            addChange(new CmsClientSitemapChangeNew(newEntry), false);
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
        newEntry.setPosition(-1);
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

        CmsRpcAction<CmsSubSitemapInfo> subSitemapAction = new CmsRpcAction<CmsSubSitemapInfo>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                List<I_CmsSitemapChange> changes = getChangesToSave();
                getService().saveAndCreateSubSitemap(getSitemapUri(), changes, path, this);

            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSubSitemapInfo result) {

                stop(false);
                resetChanges();
                markAllEntriesAsOld();
                onCreateSubSitemap(path, result);

            }
        };
        if (CmsCoreProvider.get().lockAndCheckModification(getSitemapUri(), m_data.getTimestamp())) {
            subSitemapAction.execute();
        }
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     * 
     * @param sitePath the site path of the entry to delete
     */
    public void delete(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        assert (entry != null);
        addChange(new CmsClientSitemapChangeDelete(entry), false);
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
            addChange(change, false);
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
        addChange(change, false);

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
                m_eventBus.fireEventFromSource(new CmsSitemapLoadEvent(target, originalPath), this);
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
     * Checks if any change made.<p>
     * 
     * @return <code>true</code> if there is at least one change to commit
     */
    public boolean hasChanges() {

        return !m_changes.isEmpty();
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
            addChange(change, false);
        }
    }

    /**
     * Recomputes the inherited properties for the whole loaded portion of the tree.<p>
     */
    public void recomputePropertyInheritance() {

        Map<String, CmsXmlContentProperty> propertyConfig = m_data.getProperties();
        Map<String, CmsComputedPropertyValue> parentProperties = m_data.getParentProperties();
        CmsPropertyInheritanceState propState = new CmsPropertyInheritanceState(parentProperties, propertyConfig, true);
        recomputeProperties(m_data.getRoot(), propState);
        CmsSitemapView.getInstance().getTree().getItem(0).updateSitePath();

    }

    /**
     * Re-does the last undone change.<p>
     */
    public void redo() {

        if (m_undone.isEmpty()) {
            return;
        }

        // redo
        I_CmsClientSitemapChange change = m_undone.remove(m_undone.size() - 1);
        addChange(change, true);

        // state
        if (m_undone.isEmpty()) {
            m_eventBus.fireEventFromSource(new CmsSitemapLastRedoEvent(), this);
        }
    }

    /**
     * Discards all changes, even unlocking the sitemap resource.<p>
     */
    public void reset() {

        resetChanges();

        Window.Location.reload();
    }

    /**
     * Commits all changes and leaves the sitemap editor opening the provided site-path.<p>
     * 
     * @param sitePath the site-path
     */
    public void saveAndLeavePage(String sitePath) {

        commit(true);
        Window.Location.assign(CmsCoreProvider.get().link(sitePath));
    }

    /**
     * Merges a subsitemap at the given path back into this sitemap.<p>
     * 
     * @param path the path at which the sitemap should be merged into the current sitemap 
     */
    public void saveAndMergeSubSitemap(final String path) {

        CmsRpcAction<CmsSitemapMergeInfo> mergeAction = new CmsRpcAction<CmsSitemapMergeInfo>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                List<I_CmsSitemapChange> changes = getChangesToSave();
                getService().saveAndMergeSubSitemap(getSitemapUri(), changes, path, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSitemapMergeInfo result) {

                stop(false);
                resetChanges();
                CmsClientSitemapEntry target = getEntry(path);
                I_CmsClientSitemapChange change = new CmsClientSitemapChangeMergeSitemap(path, target, result);
                executeChange(change);

            }
        };
        CmsClientSitemapEntry entry = getEntry(path);

        CmsSimplePropertyValue sitemapProp = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
        String sitemapVal = sitemapProp == null ? null : sitemapProp.getOwnValue();
        if (CmsCoreProvider.get().lockAndCheckModification(getSitemapUri(), m_data.getTimestamp())
            && CmsCoreProvider.get().lock(sitemapVal)) {
            mergeAction.execute();
        }
    }

    /**
     * Undoes the last change.<p>
     */
    public void undo() {

        if (!hasChanges()) {
            return;
        }

        // pre-state
        if (m_undone.isEmpty()) {
            m_eventBus.fireEventFromSource(new CmsSitemapFirstUndoEvent(), this);
        }

        // undo
        I_CmsClientSitemapChange change = m_changes.remove(m_changes.size() - 1);
        m_undone.add(change.getChangeForUndo());

        // update data
        I_CmsClientSitemapChange revertChange = change.revert();
        revertChange.applyToModel(this);

        // refresh view
        fireChange(revertChange);

        // post-state
        if (!hasChanges()) {
            m_eventBus.fireEventFromSource(new CmsSitemapLastUndoEvent(), this);
            CmsCoreProvider.get().unlock();
        }
    }

    /**
    * Adds a change to the queue.<p>
    * 
    * @param change the change to be added  
    * @param redo if redoing a change
    */
    protected void addChange(I_CmsClientSitemapChange change, boolean redo) {

        // state
        if (!hasChanges()) {
            if (CmsCoreProvider.get().lockAndCheckModification(getSitemapUri(), m_data.getTimestamp())) {
                m_eventBus.fireEventFromSource(new CmsSitemapStartEditEvent(), this);
            } else {
                // could not lock
                return;
            }
        }

        if (!redo && !m_undone.isEmpty()) {
            // after a new change no changes can be redone
            m_undone.clear();
            m_eventBus.fireEventFromSource(new CmsSitemapClearUndoEvent(), this);
        }

        // add it
        m_changes.add(change);

        // apply change to the model
        change.applyToModel(this);

        // refresh view, in dnd mode view already ok
        fireChange(change);
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
            return new CmsClientSitemapChangeMove(entry, toPath, position);
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
     * Resets the list of changes.<p>
     */
    protected void resetChanges() {

        m_changes.clear();
        m_undone.clear();
        // state
        m_eventBus.fireEventFromSource(new CmsSitemapResetEvent(), this);
        CmsCoreProvider.get().unlock();
    }

    /**
     * Converts the internal list of client-side changes to changes which can be saved.<p>
     * 
     * @return the list of changes to save 
     */
    List<I_CmsSitemapChange> getChangesToSave() {

        List<I_CmsSitemapChange> changes = new ArrayList<I_CmsSitemapChange>();
        for (I_CmsClientSitemapChange change : m_changes) {
            changes.addAll(change.getChangesForCommit());
        }
        return changes;
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