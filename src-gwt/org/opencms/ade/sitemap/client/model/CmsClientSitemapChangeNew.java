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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

/**
 * Stores one addition change to the sitemap.<p>
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeNew implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible = true;

    /** The new entry with children. */
    private CmsClientSitemapEntry m_entry;

    /** The new entry copy resource structure id. */
    private CmsUUID m_newCopyResourceId;

    /** The new entry resource type id. */
    private int m_newResourceTypeId;

    /** The parent entry id. */
    private CmsUUID m_parentId;

    /** An additional parameter which may contain information needed to create the new resource. */
    private String m_parameter;

    /**
     * Constructor.<p>
     * 
     * @param entry the new entry
     * @param parentId the parent entry id
     * @param resourceTypeId the resource type id
     * @param copyResourceId the copy resource id 
     * @param parameter an additional parameter which may contain information needed to create the new resource 
     */
    public CmsClientSitemapChangeNew(
        CmsClientSitemapEntry entry,
        CmsUUID parentId,
        int resourceTypeId,
        CmsUUID copyResourceId,
        String parameter) {

        m_entry = entry;
        m_parentId = parentId;
        m_newResourceTypeId = resourceTypeId;
        m_newCopyResourceId = copyResourceId;
        m_parameter = parameter;
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
        CmsClientSitemapEntry newParent = controller.getEntry(CmsResource.getParentFolder(getEntry().getSitePath()));
        if (getEntry().getPosition() < 0) {
            // inserting as first child
            newParent.insertSubEntry(getEntry(), 0);
        } else {
            newParent.insertSubEntry(getEntry(), getEntry().getPosition());
        }
        CmsClientSitemapEntry entry = getEntry();
        if (controller.isDetailPage(entry)) {
            CmsDetailPageInfo info = controller.getDetailPageInfo(entry.getId());
            if (info == null) {
                info = new CmsDetailPageInfo(m_entry.getId(), m_entry.getSitePath(), entry.getDetailpageTypeName());
            }
            controller.addDetailPageInfo(info);
        }
        getEntry().initializeAll(controller);
        applyToClipboardData(controller.getData().getClipboardData());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem newParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        CmsSitemapTreeItem newChild = getTreeItem();
        if (getEntry().getPosition() != -1) {
            newParent.insertChild(newChild, getEntry().getPosition());
        } else {
            // inserting as first child
            newParent.insertChild(newChild, 0);
        }
        newChild.updateEntry(getEntry());
        newChild.getListItemWidget().updateTruncation();
        if (m_ensureVisible) {
            view.ensureVisible(newChild);
        }
        view.updateDetailPageView(m_entry);
        view.getController().recomputeProperties();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_entry.getId(), m_entry.getSitePath(), ChangeType.create);
        change.setDefaultFileId(m_entry.getDefaultFileId());
        change.setParentId(m_parentId);
        change.setName(m_entry.getName());
        change.setPosition(m_entry.getPosition());
        change.setOwnInternalProperties(m_entry.getOwnProperties());
        change.setDefaultFileInternalProperties(m_entry.getDefaultFileProperties());
        change.setTitle(m_entry.getTitle());
        change.setCreateParameter(m_parameter);
        change.setNewResourceTypeId(m_newResourceTypeId);
        change.setNewCopyResourceId(m_newCopyResourceId);
        if (CmsSitemapView.getInstance().getController().isDetailPage(m_entry)) {
            CmsDetailPageTable table = CmsSitemapView.getInstance().getController().getDetailPageTable().copy();
            if (!table.contains(m_entry.getId())) {
                CmsDetailPageInfo info = new CmsDetailPageInfo(
                    m_entry.getId(),
                    m_entry.getSitePath(),
                    m_entry.getDetailpageTypeName());
                table.add(info);
            }
            change.setDetailPageInfos(table.toList());
        }
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        applyToClipboardData(data);
        change.setClipBoardData(data);
        return change;
    }

    /** 
     * Returns the new entry.<p>
     *
     * @return the new entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_entry;
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
     * Returns the affected tree item.<p>
     * 
     * @return the affected tree item 
     */
    protected CmsSitemapTreeItem getTreeItem() {

        return CmsSitemapView.getInstance().createSitemapItem(getEntry());
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