/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeDelete.java,v $
 * Date   : $Date: 2010/11/29 15:51:09 $
 * Version: $Revision: 1.12 $
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.sitemap.CmsSitemapChangeDelete;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

import java.util.Collections;
import java.util.List;

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.12 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeDelete implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible;

    /** The deleted entry with children. */
    private CmsClientSitemapEntry m_entry;

    /** Stores the entries site path at the time of the change event. */
    private String m_eventSitePath;

    /** The tree item to which the change should be applied. */
    private CmsSitemapTreeItem m_treeItem;

    /**
     * Constructor.<p>
     * 
     * @param entry the deleted entry
     */
    public CmsClientSitemapChangeDelete(CmsClientSitemapEntry entry) {

        m_ensureVisible = true;
        m_entry = entry;
        m_eventSitePath = m_entry.getSitePath();
    }

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
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.addDeleted(getEntry());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        CmsClientSitemapEntry deleteParent = controller.getEntry(CmsResource.getParentFolder(getEntry().getSitePath()));
        deleteParent.removeSubEntry(getEntry().getPosition());
        // apply to clipboard model
        if (!(getEntry().isNew() && CmsStringUtil.isEmptyOrWhitespaceOnly(getEntry().getVfsPath()))) {
            // only add deleted entries to the deleted list, that do have a linked vfs path
            List<CmsClientSitemapEntry> deleted = controller.getData().getClipboardData().getDeletions();
            deleted.add(0, getEntry());
        }
        removeDeletedFromModified(getEntry(), controller.getData().getClipboardData().getModifications());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem deleteParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        m_treeItem = (CmsSitemapTreeItem)deleteParent.getChild(getEntry().getName());
        m_treeItem.onFinishLoading();
        if (m_ensureVisible) {
            view.ensureVisible(m_treeItem);
        }
        deleteParent.removeChild(m_treeItem);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForUndo()
     */
    public I_CmsClientSitemapChange getChangeForUndo() {

        return this;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangesForCommit()
     */
    public List<I_CmsSitemapChange> getChangesForCommit() {

        return Collections.<I_CmsSitemapChange> singletonList(new CmsSitemapChangeDelete(m_eventSitePath));
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
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getType()
     */
    public Type getType() {

        return Type.DELETE;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#revert()
     */
    public I_CmsClientSitemapChange revert() {

        CmsClientSitemapChangeNew change = new CmsClientSitemapChangeNew(getEntry());
        change.setTreeItem(m_treeItem);
        return change;
    }

    /**
     * Sets the corresponding tree item from a new operation.<p>
     * 
     * @param treeItem the item to set
     */
    public void setTreeItem(CmsSitemapTreeItem treeItem) {

        m_treeItem = treeItem;
    }

    /**
     * Removes delted entry and all it's sub-entries from the modified list.<p>
     * 
     * @param entry the deleted entry
     * @param modified the modified list
     */
    private void removeDeletedFromModified(CmsClientSitemapEntry entry, List<CmsClientSitemapEntry> modified) {

        modified.remove(entry);
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeletedFromModified(child, modified);
        }
    }
}