/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeMove.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
 * Version: $Revision: 1.18 $
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
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EditStatus;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

/**
 * Stores one move change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeMove implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    protected boolean m_ensureVisible = true;

    /** The destination id. */
    private CmsUUID m_destinationId;

    /** The destination path. */
    private String m_destinationPath;

    /** The destination position. */
    private int m_destinationPosition;

    /** The entry to change. */
    private CmsClientSitemapEntry m_entry;

    /** The new edit status. */
    private EditStatus m_newStatus;

    /** The source path. */
    private String m_sourcePath;

    /** The source position. */
    private int m_sourcePosition;

    /**
     * Constructor.<p>
     * 
     * @param entry the entry to change
     * @param destinationPath the destination path
     * @param destinationId the id of the new parent
     * @param destinationPosition the destination position
     */
    public CmsClientSitemapChangeMove(
        CmsClientSitemapEntry entry,
        String destinationPath,
        CmsUUID destinationId,
        int destinationPosition) {

        m_entry = entry;
        m_sourcePath = m_entry.getSitePath();
        m_destinationId = destinationId;
        m_destinationPath = destinationPath;
        m_sourcePosition = m_entry.getPosition();
        m_destinationPosition = destinationPosition;
    }

    /**
     * Constructor used for creating a "revert move" change.<p>
     * 
     * @param entry the entry to change 
     * @param destPath the destination path 
     * @param destPosition the destination position 
     * @param newStatus the new edit status 
     */
    public CmsClientSitemapChangeMove(
        CmsClientSitemapEntry entry,
        String destPath,
        int destPosition,
        EditStatus newStatus) {

        m_entry = entry;
        m_sourcePath = m_entry.getSitePath();
        m_destinationPath = destPath;
        m_sourcePosition = m_entry.getPosition();
        m_destinationPosition = destPosition;
        m_newStatus = newStatus;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.addModified(getEntry(), getSourcePath());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        CmsClientSitemapEntry sourceParent = controller.getEntry(CmsResource.getParentFolder(getSourcePath()));
        CmsClientSitemapEntry moved = sourceParent.removeSubEntry(getSourcePosition());
        CmsClientSitemapEntry destParent = controller.getEntry(CmsResource.getParentFolder(getDestinationPath()));
        if (getDestinationPosition() < destParent.getSubEntries().size()) {
            destParent.insertSubEntry(moved, getDestinationPosition());
        } else {
            // inserting as last entry of the parent list
            destParent.addSubEntry(moved);
            // make sure change position index matches the real index
            m_destinationPosition = destParent.getSubEntries().size() - 1;
        }
        if (m_newStatus == null) {
            m_entry.setEdited();
        } else {
            m_entry.setEditStatus(m_newStatus);
        }
        moved.updateSitePath(getDestinationPath());
        controller.getRedirectUpdater().handleMove(m_sourcePath, m_destinationPath);
        applyToClipboardData(controller.getData().getClipboardData());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem sourceParent = view.getTreeItem(CmsResource.getParentFolder(getSourcePath()));
        sourceParent.getTree().setAnimationEnabled(false);
        CmsSitemapTreeItem moved = (CmsSitemapTreeItem)sourceParent.removeChild(getSourcePosition());
        CmsSitemapTreeItem destParent = view.getTreeItem(CmsResource.getParentFolder(getDestinationPath()));
        if (getDestinationPosition() < destParent.getChildCount()) {
            destParent.insertChild(moved, getDestinationPosition());
        } else {
            destParent.addChild(moved);
        }
        moved.updateEntry(m_entry);
        moved.updateSitePath(getDestinationPath());
        if (m_ensureVisible) {
            view.ensureVisible(moved);
        }
        sourceParent.getTree().setAnimationEnabled(true);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_entry.getId(), m_entry.getSitePath());
        change.setDefaultFileId(m_entry.getDefaultFileId());
        if (!m_destinationPath.equals(m_sourcePath)) {
            change.setParentId(m_destinationId);
            change.setName(CmsResource.getName(m_destinationPath));
        }
        change.setPosition(m_destinationPosition);
        change.setLeafType(m_entry.isLeafType());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        applyToClipboardData(data);
        change.setClipBoardData(data);
        return change;
    }

    /**
     * Returns the id of the new parent.<p>
     * 
     * @return the id of the new parent
     */
    public CmsUUID getDestinationId() {

        return m_destinationId;
    }

    /**
     * Returns the destination path.<p>
     *
     * @return the destination path
     */
    public String getDestinationPath() {

        return m_destinationPath;
    }

    /**
     * Returns the destination position.<p>
     *
     * @return the destination position
     */
    public int getDestinationPosition() {

        return m_destinationPosition;
    }

    /**
     * Returns the source path.<p>
     *
     * @return the source path
     */
    public String getSourcePath() {

        return m_sourcePath;
    }

    /**
     * Returns the source position.<p>
     *
     * @return the source position
     */
    public int getSourcePosition() {

        return m_sourcePosition;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return false; // id stays the same when moving, so there's no need for updating detail page information  
    }

    /**
     * Returns the entry to change.<p>
     * 
     * @return the entry
     */
    protected CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Applys the change to the given clip-board data.<p>
     * 
     * @param clipboardData the clip-board data
     */
    private void applyToClipboardData(CmsSitemapClipboardData clipboardData) {

        clipboardData.getModifications().remove(getEntry());
        clipboardData.getModifications().add(0, getEntry());
    }
}