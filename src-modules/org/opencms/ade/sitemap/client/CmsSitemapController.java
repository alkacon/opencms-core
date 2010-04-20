/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapController.java,v $
 * Date   : $Date: 2010/04/20 08:27:48 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapChange;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapChange.ChangeType;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sitemap editor controller.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapController {

    /** The list of changes. */
    private List<CmsClientSitemapChange> m_changes;

    /** The current available sitemap data. */
    private CmsSitemapTreeEntry m_data;

    /** The toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /** The list of undone changes. */
    private List<CmsClientSitemapChange> m_undone;

    /** The sitemap view. */
    private CmsSitemapView m_view;

    /**
     * Constructor.<p>
     * 
     * @param view the sitemap view 
     */
    public CmsSitemapController(CmsSitemapView view) {

        m_changes = new ArrayList<CmsClientSitemapChange>();
        m_undone = new ArrayList<CmsClientSitemapChange>();
        m_view = view;
    }

    /**
     * Adds the given entries to the entry identified by the given path.<p>
     * 
     * @param entryPath the path of the entry to add the data to
     * @param data the data to add
     */
    public void addData(String entryPath, List<CmsClientSitemapEntry> data) {

        CmsSitemapTreeEntry parent = getTreeEntry(entryPath);
        parent.addChildren(data);
    }

    /**
     * Changes a single property of the sitemap entry identified by the given path.<p>
     * 
     * Can also be used to delete a property if the given value is <code>null</code>.<p>
     * 
     * @param path the sitemap path
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    public void changeProperty(String path, String propertyName, String propertyValue) {

        CmsClientSitemapEntry fromEntry = getTreeEntry(path).getEntry();
        String oldPropertyValue = fromEntry.getProperties().get(propertyName);
        if ((oldPropertyValue == null) && (propertyValue == null)) {
            return;
        }
        if ((oldPropertyValue != null)
            && (propertyValue != null)
            && oldPropertyValue.trim().equals(propertyValue.trim())) {
            return;
        }
        CmsClientSitemapEntry toEntry = fromEntry.cloneEntry();
        if (propertyValue == null) {
            toEntry.getProperties().remove(propertyName);
        } else {
            toEntry.getProperties().put(propertyName, propertyValue);
        }
        addChange(fromEntry, toEntry, ChangeType.EDIT, 0, false);
    }

    /**
     * Changes the title of the sitemap entry identified by the given path.<p>
     * 
     * @param path the sitemap path
     * @param newTitle the new title
     */
    public void changeTitle(String path, String newTitle) {

        CmsClientSitemapEntry fromEntry = getTreeEntry(path).getEntry();
        if ((newTitle == null) || fromEntry.getTitle().trim().equals(newTitle.trim())) {
            return;
        }
        CmsClientSitemapEntry toEntry = null;
        addChange(fromEntry, toEntry, ChangeType.EDIT, 0, false);
    }

    /**
     * Changes the VFS reference of the entry identified by the given path.<p>
     * 
     * @param path the sitemap path
     * @param newVfsPath the new VFS reference
     */
    public void changeVfsReference(String path, String newVfsPath) {

        CmsClientSitemapEntry fromEntry = getTreeEntry(path).getEntry();
        if ((newVfsPath == null) || fromEntry.getVfsPath().trim().equals(newVfsPath.trim())) {
            return;
        }
        CmsClientSitemapEntry toEntry = null;
        addChange(fromEntry, toEntry, ChangeType.EDIT, 0, false);
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

                CmsSitemapProvider.getService().save(CmsSitemapProvider.get().getUri(), getChanges(), this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(Void result) {

                if (result == null) {
                    // ok
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_LOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
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

        if (getTreeEntry(newEntry.getSitePath()) != null) {
            return;
        }
        addChange(null, newEntry, ChangeType.NEW, 0, false);
    }

    /**
     * Deletes the entry identified by the given path and all its descendants.<p>
     * 
     * @param path the path to delete
     */
    public void delete(String path) {

        CmsClientSitemapEntry oldEntry = getTreeEntry(path).getEntry();
        addChange(oldEntry, null, ChangeType.DELETE, 0, false);
    }

    /**
     * Returns the changes.<p>
     *
     * @return the changes
     */
    public List<CmsClientSitemapChange> getChanges() {

        return m_changes;
    }

    /**
     * Returns the toolbar.<p>
     *
     * @return the toolbar
     */
    public CmsSitemapToolbar getToolbar() {

        return m_toolbar;
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
     * Moves a sitemap entry with all its descendants to a new path.<p>
     * 
     * @param fromPath the source path
     * @param toPath the destination path
     */
    public void move(String fromPath, String toPath) {

        move(fromPath, toPath, 0);
    }

    /**
     * Moves a sitemap entry with all its descendants to a new path.<p>
     * 
     * @param fromPath the source path
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(String fromPath, String toPath, int position) {

        if ((fromPath == null) || (toPath == null) || fromPath.trim().equalsIgnoreCase(toPath.trim())) {
            return;
        }
        CmsClientSitemapEntry fromEntry = getTreeEntry(fromPath).getEntry();
        CmsClientSitemapEntry toEntry = fromEntry.cloneEntry();
        toEntry.setSitePath(toPath);
        addChange(fromEntry, toEntry, ChangeType.MOVE, position, false);
    }

    /**
     * Re-does the last undone change.<p>
     */
    public void redo() {

        if (m_undone.isEmpty()) {
            return;
        }

        // redo
        CmsClientSitemapChange change = m_undone.remove(m_undone.size() - 1);
        addChange(change.getOld(), change.getNew(), change.getType(), change.getPosition(), true);

        // state
        if (m_undone.isEmpty()) {
            // TODO: disable redo button            
        }
    }

    /**
     * Replaces all properties of the sitemap entry identified by the given path.<p>
     * 
     * @param path the sitemap path
     * @param properties the properties
     */
    public void replaceProperties(String path, Map<String, String> properties) {

        CmsClientSitemapEntry fromEntry = getTreeEntry(path).getEntry();
        if ((properties != null) && properties.equals(fromEntry.getProperties())) {
            return;
        }
        CmsClientSitemapEntry toEntry = fromEntry.cloneEntry();
        toEntry.setProperties(properties);
        addChange(fromEntry, toEntry, ChangeType.EDIT, 0, false);
    }

    /**
     * Discards all changes, even unlocking the sitemap resource.<p>
     */
    public void reset() {

        // unlock
        CmsRpcAction<String> lockAction = new CmsRpcAction<String>() {

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

                // state
                getToolbar().getSaveButton().setEnabled(false);
                getToolbar().getResetButton().setEnabled(false);
                // TODO: disable undo button
                // TODO: disable redo button

                // discard
                getChanges().clear();

                stop();

                if (result == null) {
                    // ok
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_UNLOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
            }
        };
        lockAction.execute();
    }

    /**
     * Sets the root of the current sitemap.<p>
     * 
     * @param root the root of the current sitemap
     */
    public void setRoot(CmsClientSitemapEntry root) {

        m_data = new CmsSitemapTreeEntry(root);
    }

    /**
     * Sets the toolbar.<p>
     * 
     * @param toolbar the toolbar
     */
    public void setToolbar(CmsSitemapToolbar toolbar) {

        m_toolbar = toolbar;
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
            // TODO: enable redo button            
        }

        // undo
        CmsClientSitemapChange change = m_changes.remove(m_changes.size() - 1);
        m_undone.add(change);

        // post-state
        if (!isDirty()) {
            reset();
        }
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
    private void addChange(
        CmsClientSitemapEntry oldEntry,
        CmsClientSitemapEntry newEntry,
        ChangeType changeType,
        int position,
        boolean redo) {

        CmsClientSitemapChange change = new CmsClientSitemapChange(
            oldEntry.cloneEntry(),
            newEntry.cloneEntry(),
            changeType,
            position);

        // state
        if (!isDirty()) {
            startEdit();
        }

        if (!redo) {
            // after a new change no changes can be redone
            m_undone.clear();
            // TODO: disable redo button
        }

        // add it
        m_changes.add(change);

        // update data
        switch (changeType) {
            case DELETE:
                CmsSitemapTreeEntry deleteParent = getTreeEntry(CmsResource.getParentFolder(oldEntry.getSitePath()));
                deleteParent.removeChild(oldEntry.getName());
                break;
            case EDIT:
                CmsSitemapTreeEntry editEntry = getTreeEntry(oldEntry.getSitePath());
                editEntry.setEntry(newEntry);
                break;
            case MOVE:
                CmsSitemapTreeEntry sourceParent = getTreeEntry(CmsResource.getParentFolder(oldEntry.getSitePath()));
                CmsSitemapTreeEntry moved = sourceParent.removeChild(oldEntry.getName());
                CmsSitemapTreeEntry destParent = getTreeEntry(CmsResource.getParentFolder(newEntry.getSitePath()));
                destParent.insertChild(moved, position);
                break;
            case NEW:
                CmsSitemapTreeEntry newParent = getTreeEntry(CmsResource.getParentFolder(newEntry.getSitePath()));
                newParent.addChild(newEntry);
                break;
            default:
        }

        // refresh view
        m_view.refresh(change);
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param entryPath the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    private CmsSitemapTreeEntry getTreeEntry(String entryPath) {

        if (!entryPath.startsWith(m_data.getEntry().getSitePath())) {
            return null;
        }
        String path = entryPath.substring(m_data.getEntry().getSitePath().length());
        String[] names = CmsStringUtil.splitAsArray(path, "/");
        CmsSitemapTreeEntry result = m_data;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            result = result.getChild(name + "/");
            if (result == null) {
                // not found
                break;
            }
        }
        return result;
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
                getToolbar().getSaveButton().setEnabled(true);
                getToolbar().getResetButton().setEnabled(true);
                // TODO: enable undo button

                stop();
                if (result == null) {
                    // ok
                    return;
                }
                // error
                String title = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_ERROR_0);
                String text = Messages.get().key(Messages.ERR_LOCK_2, CmsSitemapProvider.get().getUri(), result);

                new CmsAlertDialog(title, text).center();
            }
        };
        lockAction.execute();
    }
}