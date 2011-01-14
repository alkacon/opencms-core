/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeEdit.java,v $
 * Date   : $Date: 2011/01/14 14:19:54 $
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
import org.opencms.ade.sitemap.shared.CmsSitemapChange;

/**
 * Stores one edition change to the sitemap.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.12 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeEdit implements I_CmsClientSitemapChange {

    /** If true, tell the view to ensure that the affected  item is visible. */
    private boolean m_ensureVisible;

    /** The new entry without children. */
    private CmsClientSitemapEntry m_newEntry;

    /** The old entry without children. */
    private CmsClientSitemapEntry m_oldEntry;

    /**
     * Constructor.<p>
     * 
     * @param oldEntry the old entry
     * @param newEntry the new entry
     */
    public CmsClientSitemapChangeEdit(CmsClientSitemapEntry oldEntry, CmsClientSitemapEntry newEntry) {

        m_ensureVisible = true;
        m_oldEntry = new CmsClientSitemapEntry(oldEntry);
        m_newEntry = newEntry;
        //      m_newEntry.setLock(oldEntry.getLock());
    }

    /**
     * Constructor.<p>
     * 
     * @param oldEntry the old entry
     * @param newEntry the new entry
     * @param ensureVisible the ensure visible flag
     */
    public CmsClientSitemapChangeEdit(
        CmsClientSitemapEntry oldEntry,
        CmsClientSitemapEntry newEntry,
        boolean ensureVisible) {

        m_ensureVisible = ensureVisible;
        m_oldEntry = new CmsClientSitemapEntry(oldEntry);
        m_newEntry = newEntry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.addModified(getNewEntry(), getOldEntry().getSitePath());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        CmsClientSitemapEntry editEntry = controller.getEntry(getOldEntry().getSitePath());
        editEntry.setTitle(getNewEntry().getTitle());
        editEntry.setVfsPath(getNewEntry().getVfsPath());
        editEntry.setProperties(getNewEntry().getProperties());
        editEntry.setNew(getNewEntry().isNew());
        controller.getRedirectUpdater().handleSave(editEntry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem editEntry = view.getTreeItem(getOldEntry().getSitePath());
        if (m_ensureVisible) {
            view.ensureVisible(editEntry);
        }
        editEntry.updateEntry(getNewEntry());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapChange change = new CmsSitemapChange(m_newEntry.getId(), m_newEntry.getSitePath());
        change.setProperties(m_newEntry.getProperties());
        if (!m_oldEntry.getTitle().equals(m_newEntry.getTitle())) {
            change.setTitle(m_newEntry.getTitle());
        }
        return change;
    }

    /**
     * Returns the new entry.<p>
     *
     * @return the new entry
     */
    public CmsClientSitemapEntry getNewEntry() {

        return m_newEntry;
    }

    /**
     * Returns the old entry.<p>
     *
     * @return the old entry
     */
    public CmsClientSitemapEntry getOldEntry() {

        return m_oldEntry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return false; // detail page information can not be edited directly 
    }
}