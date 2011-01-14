/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeNew.java,v $
 * Date   : $Date: 2011/01/14 14:19:54 $
 * Version: $Revision: 1.17 $
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
import org.opencms.ade.sitemap.shared.CmsResourceTypeInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.CmsDetailPageTable;

/**
 * Stores one addition change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.17 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeNew implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible = true;

    /** The new entry with children. */
    private CmsClientSitemapEntry m_entry;

    /** The parent entry id. */
    private CmsUUID m_parentId;

    /**
     * Constructor.<p>
     * 
     * @param entry the new entry
     * @param parentId the parent entry id
     */
    public CmsClientSitemapChangeNew(CmsClientSitemapEntry entry, CmsUUID parentId) {

        m_entry = entry;
        m_parentId = parentId;
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
        controller.getData().getClipboardData().getModifications().add(0, getEntry());
        CmsClientSitemapEntry entry = getEntry();
        if (entry.isDetailPage()) {
            CmsDetailPageInfo info = controller.getDetailPageInfo(entry.getId());
            if (info == null) {
                CmsResourceTypeInfo typeInfo = entry.getResourceTypeInfo();
                info = new CmsDetailPageInfo(m_entry.getId(), m_entry.getSitePath(), typeInfo.getName());
            }
            controller.addDetailPageInfo(info);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem newParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        getEntry().setChildrenLoadedInitially();
        CmsSitemapTreeItem newChild = getTreeItem();
        if (getEntry().getPosition() != -1) {
            newParent.insertChild(newChild, getEntry().getPosition());
        } else {
            // inserting as first child
            newParent.insertChild(newChild, 0);
        }
        newChild.updateEntry(getEntry());
        if (m_ensureVisible) {
            view.ensureVisible(newChild);
        }
        view.updateDetailPageView(m_entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_entry.getId(), m_entry.getSitePath());
        change.setNew(true);
        change.setParentId(m_parentId);
        change.setName(m_entry.getName());
        change.setPosition(m_entry.getPosition());
        change.setTitle(m_entry.getTitle());
        change.setProperties(m_entry.getProperties());
        if (isChangingDetailPages()) {
            CmsDetailPageTable table = CmsSitemapView.getInstance().getController().getDetailPageTable().copy();
            if (!table.contains(m_entry.getId())) {
                CmsDetailPageInfo info = new CmsDetailPageInfo(
                    m_entry.getId(),
                    m_entry.getSitePath(),
                    m_entry.getTypeInfo().getName());
                table.add(info);
            }
            change.setDetailPageInfos(table.toList());
        }
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
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return m_entry.isDetailPage();
    }

    /**
     * Returns the affected tree item.<p>
     * 
     * @return the affected tree item 
     */
    protected CmsSitemapTreeItem getTreeItem() {

        return CmsSitemapView.getInstance().createSitemapItem(getEntry());
    }

}