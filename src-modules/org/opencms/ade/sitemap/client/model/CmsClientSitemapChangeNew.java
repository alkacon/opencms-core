/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeNew.java,v $
 * Date   : $Date: 2010/11/29 15:51:09 $
 * Version: $Revision: 1.15 $
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
import org.opencms.xml.sitemap.CmsSitemapChangeNew;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

import java.util.Collections;
import java.util.List;

/**
 * Stores one addition change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.15 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeNew implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible = true;

    /** The new entry with children. */
    private CmsClientSitemapEntry m_entry;

    /** Stores the entries site path at the time of the change event. */
    private String m_eventSitePath;

    /** The corresponding tree item, if available from a delete operation. */
    private CmsSitemapTreeItem m_treeItem;

    /**
     * Constructor.<p>
     * 
     * @param entry the new entry
     */
    public CmsClientSitemapChangeNew(CmsClientSitemapEntry entry) {

        m_entry = entry;
        m_eventSitePath = m_entry.getSitePath();
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
        // in case restored from deletion
        controller.getData().getClipboardData().getDeletions().remove(getEntry());
        controller.getData().getClipboardData().getModifications().add(0, getEntry());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem newParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        getEntry().setChildrenLoadedInitially();
        CmsSitemapTreeItem newChild = (m_treeItem != null ? m_treeItem : view.createSitemapItem(getEntry()));
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

        // use the site path stored at the event time, as the entry may have been moved in the mean time
        return Collections.<I_CmsSitemapChange> singletonList(new CmsSitemapChangeNew(
            m_eventSitePath,
            getEntry().getPosition(),
            getEntry().getTitle(),
            getEntry().getVfsPath(),
            getEntry().getProperties()));
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
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getType()
     */
    public Type getType() {

        return Type.NEW;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#revert()
     */
    public I_CmsClientSitemapChange revert() {

        CmsClientSitemapChangeDelete change = new CmsClientSitemapChangeDelete(getEntry());
        change.setTreeItem(m_treeItem);
        return change;
    }

    /**
     * Sets the corresponding tree item from a delete operation.<p>
     * 
     * @param treeItem the item to set
     */
    public void setTreeItem(CmsSitemapTreeItem treeItem) {

        m_treeItem = treeItem;
    }
}