/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeDelete.java,v $
 * Date   : $Date: 2011/05/27 14:51:46 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.21 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeDelete implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible;

    /** The deleted entry with children. */
    private CmsClientSitemapEntry m_entry;

    /**
     * Constructor.<p>
     * 
     * @param entry the deleted entry
     * @param ensureVisible the ensure visible flag
     */
    public CmsClientSitemapChangeDelete(CmsClientSitemapEntry entry, boolean ensureVisible) {

        m_ensureVisible = ensureVisible;
        m_entry = entry;
    }

    /**
     * Constructor.<p>
     * 
     * @param entry the deleted entry
     * @param parentId the parent entry id
     */
    public CmsClientSitemapChangeDelete(CmsClientSitemapEntry entry, CmsUUID parentId) {

        m_ensureVisible = true;
        m_entry = entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(final CmsToolbarClipboardView view) {

        view.removeModified(m_entry.getId().toString());
        if (!m_entry.isNew()) {
            // make sure to only add items that are not of resource state 'new'
            CmsCoreProvider.get().getResourceState(m_entry.getId(), new AsyncCallback<CmsResourceState>() {

                /**
                 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                 */
                public void onFailure(Throwable caught) {

                    // do nothing
                }

                /**
                 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(Object o)
                 */
                public void onSuccess(CmsResourceState result) {

                    if (!result.isNew()) {
                        view.addDeleted(getEntry());
                        applyToClipboardData(CmsSitemapView.getInstance().getController().getData().getClipboardData());
                    }
                }
            });
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        CmsClientSitemapEntry deleteParent = controller.getEntry(CmsResource.getParentFolder(getEntry().getSitePath()));
        deleteParent.removeSubEntry(getEntry().getPosition());
        // apply to detailpage table
        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getData().getDetailPageTable();
        CmsUUID id = m_entry.getId();
        if (detailPageTable.contains(id)) {
            detailPageTable.remove(id);
        }
        removeDeletedFromModified(getEntry(), controller.getData().getClipboardData());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem deleteParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        CmsSitemapTreeItem deleteItem = (CmsSitemapTreeItem)deleteParent.getChild(getEntry().getName());
        deleteItem.onFinishLoading();
        if (m_ensureVisible) {
            view.ensureVisible(deleteItem);
        }
        deleteParent.removeChild(deleteItem);
        view.updateDetailPageView(m_entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_entry.getId(), m_entry.getSitePath(), ChangeType.delete);
        change.setDefaultFileId(m_entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        applyToClipboardData(data);
        change.setClipBoardData(data);
        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getData().getDetailPageTable();
        CmsUUID id = m_entry.getId();
        if (detailPageTable.contains(id)) {
            CmsDetailPageTable copyTable = detailPageTable.copy();
            copyTable.remove(id);
            change.setDetailPageInfos(copyTable.toList());
        }
        return change;
    }

    /**
     * Returns the deleted entry.<p>
     *
     * @return the deleted entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return m_entry.isDetailPage();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#updateEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        // nothing to do
    }

    /**
     * Applys the change to the given clip-board data.<p>
     * 
     * @param clipboardData the clip-board data
     */
    protected void applyToClipboardData(CmsSitemapClipboardData clipboardData) {

        if (!getEntry().isNew()) {
            clipboardData.addDeleted(getEntry());
            removeDeletedFromModified(getEntry(), clipboardData);
        }
    }

    /**
     * Removes delted entry and all it's sub-entries from the modified list.<p>
     * 
     * @param entry the deleted entry
     * @param modified the modified list
     */
    private void removeDeletedFromModified(CmsClientSitemapEntry entry, CmsSitemapClipboardData clipboardData) {

        clipboardData.removeModified(entry);
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeletedFromModified(child, clipboardData);
        }
    }
}