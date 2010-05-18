/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2010/05/18 13:29:53 $
 * Version: $Revision: 1.21 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeDelete;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeEdit;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeMove;
import org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew;
import org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapTemplate;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.I_CmsSitemapChange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapController {

    /** The list of changes. */
    protected List<I_CmsClientSitemapChange> m_changes;

    /** The sitemap data. */
    protected CmsSitemapData m_data;

    /** The handler. */
    protected I_CmsSitemapControllerHandler m_handler;

    /** The list of undone changes. */
    protected List<I_CmsClientSitemapChange> m_undone;

    /** The set of names of hidden properties. */
    private Set<String> m_hiddenProperties;

    /** The sitemap service instance. */
    private I_CmsSitemapServiceAsync m_service;

    /**
     * Constructor.<p>
     * 
     * @param handler the handler to set
     */
    public CmsSitemapController(I_CmsSitemapControllerHandler handler) {

        m_changes = new ArrayList<I_CmsClientSitemapChange>();
        m_undone = new ArrayList<I_CmsClientSitemapChange>();
        m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObject(getService(), CmsSitemapData.DICT_NAME);
        m_handler = handler;
        m_hiddenProperties = new HashSet<String>();
        m_hiddenProperties.add(CmsSitemapManager.Property.template.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.templateInherited.toString());
        m_hiddenProperties.add(CmsSitemapManager.Property.sitemap.toString());
    }

    /**
     * Commits the changes.<p>
     */
    public void commit() {

        // save the sitemap
        CmsRpcAction<Void> saveAction = new CmsRpcAction<Void>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(0);
                List<I_CmsSitemapChange> changes = new ArrayList<I_CmsSitemapChange>();
                for (I_CmsClientSitemapChange change : m_changes) {
                    changes.add(change.getChangeForCommit());
                }
                getService().save(CmsCoreProvider.get().getUri(), changes, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Void result) {

                m_changes.clear();
                m_undone.clear();

                // state
                m_handler.onReset();
                stop(true);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#show()
             */
            @Override
            protected void show() {

                CmsNotification.get().send(CmsNotification.Type.NORMAL, Messages.get().key(Messages.GUI_SAVING_0));
            }
        };
        saveAction.execute();
    }

    /**
     * Registers a new sitemap entry.<p>
     * 
     * @param newEntry the new entry
     */
    public void create(CmsClientSitemapEntry newEntry) {

        assert (getEntry(newEntry.getSitePath()) == null);
        assert (getEntry(CmsResource.getParentFolder(newEntry.getSitePath())) != null);

        newEntry.setPosition(-1); // ensure it will be inserted at the end
        addChange(new CmsClientSitemapChangeNew(newEntry), false);
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
     * @param name the new URL name, can be <code>null</code> to keep the old one 
     * @param properties the new properties, can be <code>null</code> to keep the old properties
     */
    public void edit(CmsClientSitemapEntry entry, String title, String vfsReference, String name,

    Map<String, String> properties) {

        boolean changedTitle = ((title != null) && !title.trim().equals(entry.getTitle()));
        boolean changedVfsRef = ((vfsReference != null) && !vfsReference.trim().equals(entry.getVfsPath()));
        //boolean changedProperties = ((properties != null) && !properties.equals(entry.getProperties()));
        //assert (!changedTitle && !changedVfsRef && !changedProperties);

        CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry(entry);
        if (changedTitle) {
            newEntry.setTitle(title);
        }
        if (changedVfsRef) {
            newEntry.setVfsPath(vfsReference);
        }
        if (properties != null) {
            // to preserve the hidden properties (navigation, sitemap...), we only copy the new property values
            newEntry.getProperties().putAll(properties);
        }
        // TODO: set URL name of the entry, and also change the URL shown in the widget

        addChange(new CmsClientSitemapChangeEdit(entry, newEntry), false);
    }

    /**
     * Retrieves the children entries of the given node from the server.<p>
     * 
     * @param sitePath the site pat of the sitemap entry to get the children for
     */
    public void getChildren(final String sitePath) {

        CmsRpcAction<List<CmsClientSitemapEntry>> getChildrenAction = new CmsRpcAction<List<CmsClientSitemapEntry>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500);
                getService().getChildren(sitePath, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> result) {

                CmsClientSitemapEntry target = getEntry(sitePath);
                target.setSubEntries(result);
                m_handler.onGetChildren(target);
                stop(false);
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

        if ((sitemapPath == null) || sitemapPath.equals("") || sitemapPath.equals("/")) {
            return m_data.getDefaultTemplate();
        }
        CmsClientSitemapEntry entry = getEntry(sitemapPath);
        String templateInherited = entry.getProperties().get(CmsSitemapManager.Property.templateInherited);
        if (templateInherited != null) {
            return m_data.getTemplates().get(templateInherited);
        }
        String parentPath = CmsResource.getParentFolder(sitemapPath);
        return getDefaultTemplate(parentPath);
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
                break;
            }
        }
        return result;
    }

    /**
     * Checks whether a given sitemap entry has sibling entries with a given URL name.<p>
     * 
     * @param entry the entry which should be checked 
     * @param urlNameValue the url name value
     * @return true if the url name value occurs in siblings of the sitemap entry which was passed in 
     */
    public boolean hasSiblingEntriesWithName(CmsClientSitemapEntry entry, String urlNameValue) {

        String parentPath = CmsResource.getParentFolder(entry.getSitePath());
        CmsClientSitemapEntry parentEntry = getEntry(parentPath);
        for (CmsClientSitemapEntry siblingEntry : parentEntry.getSubEntries()) {
            if ((siblingEntry != entry) && urlNameValue.equals(siblingEntry.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any change made.<p>
     * 
     * @return <code>true</code> if there is at least a change to commit
     */
    public boolean isDirty() {

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
     * Moves the given sitemap entry with all its descendants to the new position.<p>
     * 
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(CmsClientSitemapEntry entry, String toPath, int position) {

        assert (getEntry(entry.getSitePath()) != null);
        assert ((toPath != null) && (!entry.getSitePath().equals(toPath) || (entry.getPosition() != position)));
        assert (getEntry(CmsResource.getParentFolder(toPath)) != null);

        addChange(new CmsClientSitemapChangeMove(entry.getSitePath(), entry.getPosition(), toPath, position), false);
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
            m_handler.onLastRedo();
        }
    }

    /**
     * Discards all changes, even unlocking the sitemap resource.<p>
     */
    public void reset() {

        m_changes.clear();
        m_undone.clear();

        // state
        m_handler.onReset();

        CmsCoreProvider.get().unlock();
        Window.Location.reload();
    }

    /**
     * Undoes the last change.<p>
     */
    public void undo() {

        if (!isDirty()) {
            return;
        }

        // pre-state
        if (m_undone.isEmpty()) {
            m_handler.onFirstUndo();
        }

        // undo
        I_CmsClientSitemapChange change = m_changes.remove(m_changes.size() - 1);
        m_undone.add(change);

        // update data
        I_CmsClientSitemapChange revertChange = change.revert();
        revertChange.applyToModel(this);

        // refresh view
        m_handler.onChange(revertChange);

        // post-state
        if (!isDirty()) {
            m_handler.onLastUndo();
            CmsCoreProvider.get().unlock();
        }
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
     * Adds a change to the queue.<p>
     * 
     * @param oldEntry the old entry
     * @param newEntry the new entry
     * @param changeType the change type
     * @param position the new position between its siblings, only used when moving
     * @param redo if redoing a change
     */
    private void addChange(I_CmsClientSitemapChange change, boolean redo) {

        // state
        if (!isDirty()) {
            if (CmsCoreProvider.get().lock()) {
                m_handler.onStartEdit();
            } else {
                // could not lock
                return;
            }
        }

        if (!redo) {
            // after a new change no changes can be redone
            m_undone.clear();
            m_handler.onClearUndo();
        }

        // add it
        m_changes.add(change);

        // apply change to the model
        change.applyToModel(this);

        // refresh view
        m_handler.onChange(change);
    }
}