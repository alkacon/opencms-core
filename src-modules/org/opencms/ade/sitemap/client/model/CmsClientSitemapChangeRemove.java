/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeRemove.java,v $
 * Date   : $Date: 2011/02/22 09:46:09 $
 * Version: $Revision: 1.5 $
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
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.util.CmsUUID;

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeRemove implements I_CmsClientSitemapChange {

    /** The deleted entry with children. */
    private CmsClientSitemapEntry m_entry;

    /** The tree item to which the change should be applied. */
    private CmsSitemapTreeItem m_treeItem;

    /**
     * Constructor.<p>
     * 
     * @param entry the deleted entry
     */
    public CmsClientSitemapChangeRemove(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * Constructor.<p>
     *  
     * @param entry the deleted entry
     * @param parentId the parent entry id
     */
    public CmsClientSitemapChangeRemove(CmsClientSitemapEntry entry, CmsUUID parentId) {

        m_entry = entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.addModified(getEntry(), getEntry().getSitePath());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        m_entry.setInNavigation(false);
        // apply to clipboard model
        applyToClipboardData(controller.getData().getClipboardData());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        m_treeItem = view.getTreeItem(getEntry().getSitePath());
        m_treeItem.updateEntry(m_entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_entry.getId(), m_entry.getSitePath(), ChangeType.remove);
        change.setDefaultFileId(m_entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        applyToClipboardData(data);
        change.setClipBoardData(data);
        //TODO: handle detail page delete
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
     * Sets the corresponding tree item from a new operation.<p>
     * 
     * @param treeItem the item to set
     */
    public void setTreeItem(CmsSitemapTreeItem treeItem) {

        m_treeItem = treeItem;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#updateEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        if (m_entry.getSitePath().equals(entry.getSitePath())) {
            m_entry.update(entry);
        }
    }

    /**
     * Applys the change to the given clip-board data.<p>
     * 
     * @param clipboardData the clip-board data
     */
    private void applyToClipboardData(CmsSitemapClipboardData clipboardData) {

        clipboardData.addModified(getEntry());
    }
}