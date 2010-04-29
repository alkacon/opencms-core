/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2010/04/29 09:31:56 $
 * Version: $Revision: 1.10 $
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

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeDelete;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeEdit;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeMove;
import org.opencms.ade.sitemap.shared.CmsSitemapChangeNew;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.I_CmsSitemapChange;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapController {

    /** The list of changes. */
    protected List<I_CmsSitemapChange> m_changes;

    /** The edition data. */
    protected CmsSitemapData m_editData;

    /** The handler. */
    protected CmsSitemapControllerHandler m_handler;

    /** Async loading of data needed only when editing. */
    protected CmsRpcAction<CmsSitemapData> m_initAction;

    /** The current available sitemap data. */
    protected CmsClientSitemapEntry m_sitemap;

    /** The list of undone changes. */
    protected List<I_CmsSitemapChange> m_undone;

    /**
     * Constructor.<p>
     */
    public CmsSitemapController() {

        m_changes = new ArrayList<I_CmsSitemapChange>();
        m_undone = new ArrayList<I_CmsSitemapChange>();

        // async loading of data needed only when editing 
        m_initAction = new CmsRpcAction<CmsSitemapData>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                CmsSitemapProvider.getService().getInitData(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsSitemapData data) {

                m_editData = data;
                m_handler.onEditDataAvailable(data);
                stop();
                m_initAction = null;
            }
        };
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

                CmsSitemapProvider.getService().save(CmsSitemapProvider.get().getUri(), m_changes, this);
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

                internalReset(false);
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
        addChange(new CmsSitemapChangeNew(newEntry), false);
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     * 
     * @param sitePath the site path of the entry to delete
     */
    public void delete(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        assert (entry != null);
        addChange(new CmsSitemapChangeDelete(entry), false);
    }

    /**
     * Edits the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     * @param title the new title, can be <code>null</code> to keep the old one
     * @param vfsReference the new VFS reference, can be <code>null</code> to keep the old one
     * @param properties the new properties, can be <code>null</code> to keep the old properties
     */
    public void edit(CmsClientSitemapEntry entry, String title, String vfsReference, Map<String, String> properties) {

        boolean changedTitle = ((title != null) && !title.trim().equals(entry.getTitle()));
        boolean changedVfsRef = ((vfsReference != null) && !vfsReference.trim().equals(entry.getVfsPath()));
        boolean changedProperties = ((properties != null) && !properties.equals(entry.getProperties()));
        assert (!changedTitle && !changedVfsRef && !changedProperties);

        CmsClientSitemapEntry newEntry = entry.cloneEntry();
        if (changedTitle) {
            newEntry.setTitle(title);
        }
        if (changedVfsRef) {
            newEntry.setVfsPath(vfsReference);
        }
        if (changedProperties) {
            newEntry.setProperties(properties);
        }
        addChange(new CmsSitemapChangeEdit(entry, newEntry), false);
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
                CmsSitemapProvider.getService().getChildren(sitePath, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsClientSitemapEntry> result) {

                CmsClientSitemapEntry target = getEntry(sitePath);
                target.setChildren(result);
                m_handler.onGetChildren(target);
            }
        };
        getChildrenAction.execute();
    }

    /**
     * Returns the edit data.<p>
     *
     * @return the edit data
     */
    public CmsSitemapData getEditData() {

        if (m_initAction != null) {
            // not ready yet, show overlay
            m_initAction.start(0);
        }
        return m_editData;
    }

    /**
     * Initializes the controller.<p>
     * 
     * @param command the command to execute after initialization
     */
    public void initialize(final Command command) {

        CmsRpcAction<CmsClientSitemapEntry> getRootsAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500);
                CmsSitemapProvider.getService().getRoot(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry root) {

                m_sitemap = root;
                m_handler.onInit(root);
                command.execute();
                stop();
            }
        };
        getRootsAction.execute();

        if (CmsSitemapProvider.get().isEditable()) {
            // async loading of data needed only when editing 
            m_initAction.execute();
        }
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
     * Checks if the given site path is the sitemap root.<p>
     * 
     * @param sitePath the site path to check
     * 
     * @return <code>true</code> if the given site path is the sitemap root
     */
    public boolean isRoot(String sitePath) {

        return m_sitemap.getSitePath().equals(sitePath);
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

        addChange(new CmsSitemapChangeMove(entry.getSitePath(), entry.getPosition(), toPath, position), false);
    }

    /**
     * Re-does the last undone change.<p>
     */
    public void redo() {

        if (m_undone.isEmpty()) {
            return;
        }

        // redo
        I_CmsSitemapChange change = m_undone.remove(m_undone.size() - 1);
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

        internalReset(true);
    }

    /**
     * Sets the controller handler.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(CmsSitemapControllerHandler handler) {

        m_handler = handler;
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
        I_CmsSitemapChange change = m_changes.remove(m_changes.size() - 1);
        m_undone.add(change);

        // update data
        I_CmsSitemapChange revertChange = change.revert();
        update(revertChange);

        // refresh view
        m_handler.onChange(revertChange);

        // post-state
        if (!isDirty()) {
            m_handler.onLastUndo();
            internalReset(false);
        }
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param entryPath the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    protected CmsClientSitemapEntry getEntry(String entryPath) {

        if (!entryPath.startsWith(m_sitemap.getSitePath())) {
            return null;
        }
        String path = entryPath.substring(m_sitemap.getSitePath().length());
        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsClientSitemapEntry result = m_sitemap;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            boolean found = false;
            for (CmsClientSitemapEntry child : result.getChildren()) {
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
     * Discards all changes, even unlocking the sitemap resource.<p>
     * 
     * @param reload if to reload after unlocking
     */
    protected void internalReset(final boolean reload) {

        // unlock
        CmsRpcAction<String> unlockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(0);
                CmsCoreProvider.getCoreService().unlock(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                stop();

                if (result == null) {
                    // ok
                    if (reload) {
                        Window.Location.reload();
                    }
                    return;
                }
                // unable to unlock
                String text = Messages.get().key(
                    Messages.GUI_UNLOCK_NOTIFICATION_2,
                    CmsSitemapProvider.get().getUri(),
                    result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        unlockAction.execute();
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
    private void addChange(I_CmsSitemapChange change, boolean redo) {

        // state
        if (!isDirty()) {
            startEdit();
        }

        if (!redo) {
            // after a new change no changes can be redone
            m_undone.clear();
            m_handler.onClearUndo();
        }

        // add it
        m_changes.add(change);

        // update data
        update(change);

        // refresh view
        m_handler.onChange(change);
    }

    /**
     * Sets the state when start editing, even locking the sitemap resource.<p>
     */
    private void startEdit() {

        // lock the sitemap
        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                start(0);
                CmsCoreProvider.getCoreService().lock(CmsSitemapProvider.get().getUri(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(String result) {

                // state
                m_handler.onStartEdit();

                stop();
                if (result == null) {
                    // ok
                    return;
                }
                // unable to lock
                String text = Messages.get().key(
                    Messages.GUI_LOCK_NOTIFICATION_2,
                    CmsSitemapProvider.get().getUri(),
                    result);
                CmsNotification.get().send(CmsNotification.Type.WARNING, text);
            }
        };
        lockAction.execute();
    }

    /**
     * Updates the internal data for the given change.<p>
     * 
     * @param change the change
     */
    private void update(I_CmsSitemapChange change) {

        if (change instanceof CmsSitemapChangeDelete) {
            CmsSitemapChangeDelete changeDelete = (CmsSitemapChangeDelete)change;
            CmsClientSitemapEntry deleteParent = getEntry(CmsResource.getParentFolder(changeDelete.getEntry().getSitePath()));
            deleteParent.removeChild(changeDelete.getEntry().getPosition());
        } else if (change instanceof CmsSitemapChangeEdit) {
            CmsSitemapChangeEdit changeEdit = (CmsSitemapChangeEdit)change;
            CmsClientSitemapEntry editEntry = getEntry(changeEdit.getOldEntry().getSitePath());
            editEntry.setTitle(changeEdit.getNewEntry().getTitle());
            editEntry.setVfsPath(changeEdit.getNewEntry().getVfsPath());
            editEntry.setProperties(changeEdit.getNewEntry().getProperties());
        } else if (change instanceof CmsSitemapChangeMove) {
            CmsSitemapChangeMove changeMove = (CmsSitemapChangeMove)change;
            CmsClientSitemapEntry sourceParent = getEntry(CmsResource.getParentFolder(changeMove.getSourcePath()));
            CmsClientSitemapEntry moved = sourceParent.removeChild(changeMove.getSourcePosition());
            CmsClientSitemapEntry destParent = getEntry(CmsResource.getParentFolder(changeMove.getDestinationPath()));
            destParent.insertChild(moved, changeMove.getDestinationPosition());
        } else if (change instanceof CmsSitemapChangeNew) {
            CmsSitemapChangeNew changeNew = (CmsSitemapChangeNew)change;
            CmsClientSitemapEntry newParent = getEntry(CmsResource.getParentFolder(changeNew.getEntry().getSitePath()));
            newParent.addChild(changeNew.getEntry());
        }
    }
}