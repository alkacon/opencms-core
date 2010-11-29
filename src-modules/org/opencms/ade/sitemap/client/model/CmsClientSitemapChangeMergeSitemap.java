/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeMergeSitemap.java,v $
 * Date   : $Date: 2010/11/29 10:33:35 $
 * Version: $Revision: 1.3 $
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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent;
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapMergeInfo;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

import java.util.List;

/**
 * This class represents the change of merging a sub-sitemap back into a parent sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
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

    /** The path at which the sitemap has been merged into the parent sitemap. */
    private String m_path;

    /**
     * Constructor.<p>
     * 
     * @param path the path at which the sub-sitemap should be merged into the parent sitemap
     * @param entry the entry which references the sub-sitemap
     * @param mergeInfo the result of the server-side merge operation 
     */
    public CmsClientSitemapChangeMergeSitemap(String path, CmsClientSitemapEntry entry, CmsSitemapMergeInfo mergeInfo) {

        m_path = path;
        m_entry = entry;
        m_mergeInfo = mergeInfo;

        CmsClientSitemapEntry changedEntry = new CmsClientSitemapEntry(m_entry);
        changedEntry.getProperties().remove(CmsSitemapManager.Property.sitemap.name());
        m_internalChange = new CmsClientSitemapChangeEdit(m_entry, changedEntry, false);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        // TODO: Auto-generated method stub
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        // apply to sitemap model 
        m_entry.setSubEntries(m_mergeInfo.getMergedEntries());
        m_internalChange.applyToModel(controller);
        controller.getData().setTimestamp(m_mergeInfo.getTimestamp());
        // TODO: apply to clipboard model
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        m_internalChange.applyToView(view);
        view.onLoad(new CmsSitemapLoadEvent(m_entry, m_path));
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForUndo()
     */
    public I_CmsClientSitemapChange getChangeForUndo() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangesForCommit()
     */
    public List<I_CmsSitemapChange> getChangesForCommit() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getType()
     */
    public Type getType() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#revert()
     */
    public I_CmsClientSitemapChange revert() {

        throw new UnsupportedOperationException();
    }
}
