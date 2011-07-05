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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry.EntryType;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;

/**
 * This class represents the change of merging a sub-sitemap back into a parent sitemap.<p>
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeMergeSitemap implements I_CmsClientSitemapChange {

    /** The entry whose children should be merged back into the parent sitemap. */
    private CmsClientSitemapEntry m_entry;

    /** The internal change object represents the removal of the 'sitemap' property. */
    private I_CmsClientSitemapChange m_internalChange;

    /** The result of the merge operation. */
    private CmsSitemapMergeInfo m_mergeInfo;

    /**
     * Constructor.<p>
     * 
     * @param entry the entry which references the sub-sitemap
     * @param mergeInfo the result of the server-side merge operation 
     */
    public CmsClientSitemapChangeMergeSitemap(CmsClientSitemapEntry entry, CmsSitemapMergeInfo mergeInfo) {

        m_entry = entry;
        m_mergeInfo = mergeInfo;
        m_entry.setEntryType(EntryType.folder);
        m_internalChange = new CmsClientSitemapChangeEdit(m_entry, mergeInfo.getMergedEntry(), false);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.addModified(m_entry, m_entry.getSitePath());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        m_entry.setSubEntries(m_mergeInfo.getMergedEntry().getSubEntries());
        m_internalChange.applyToModel(controller);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        m_internalChange.applyToView(view);
        view.onLoad(new CmsSitemapLoadEvent(m_entry, true));
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#updateEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        if (m_entry.getSitePath().equals(entry.getSitePath())) {
            m_entry.update(entry);
        }
    }
}
