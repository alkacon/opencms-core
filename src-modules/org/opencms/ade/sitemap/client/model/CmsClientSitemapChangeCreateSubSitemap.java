/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeCreateSubSitemap.java,v $
 * Date   : $Date: 2010/06/10 13:27:41 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSubSitemapInfo;
import org.opencms.xml.sitemap.CmsSitemapManager;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side change class for removing a subtree after converting it into a sub-sitemap.<p>
 * 
 * Unlike the other classes implementing {@link I_CmsClientSitemapChange}, this class does not represent a change which is only saved  
 * when the user clicks the 'Save' button, so only some methods of {@link I_CmsClientSitemapChange} are supported.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeCreateSubSitemap implements I_CmsClientSitemapChange {

    /** The list of elementary changes which this change consists of. */
    private List<I_CmsClientSitemapChange> m_internalChanges;

    /** The result of the sub-sitemap creation operation. */
    private CmsSubSitemapInfo m_subSitemapInfo;

    /**
     * Constructor.<p>
     * 
     * @param entry the sitemap entry whose subtree has been converted into a sub-sitemap 
     * @param info the result of the sub-sitemap creation operation 
     */
    public CmsClientSitemapChangeCreateSubSitemap(CmsClientSitemapEntry entry, CmsSubSitemapInfo info) {

        String subSitemapPath = info.getSitemapPath();
        List<I_CmsClientSitemapChange> changes = new ArrayList<I_CmsClientSitemapChange>();
        for (CmsClientSitemapEntry childEntry : entry.getSubEntries()) {
            CmsClientSitemapChangeDelete deleteAction = new CmsClientSitemapChangeDelete(childEntry);
            deleteAction.setEnsureVisible(false);
            changes.add(deleteAction);
        }
        CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry(entry);
        newEntry.getProperties().put(CmsSitemapManager.Property.sitemap.name(), subSitemapPath);
        CmsClientSitemapChangeEdit editAction = new CmsClientSitemapChangeEdit(entry, newEntry);
        editAction.setEnsureVisible(false);
        changes.add(editAction);
        m_internalChanges = changes;
        m_subSitemapInfo = info;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        controller.getData().setTimestamp(m_subSitemapInfo.getParentTimestamp());

        for (I_CmsClientSitemapChange change : m_internalChanges) {
            change.applyToModel(controller);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        for (I_CmsClientSitemapChange change : m_internalChanges) {
            change.applyToView(view);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public I_CmsSitemapChange getChangeForCommit() {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForUndo()
     */
    public I_CmsClientSitemapChange getChangeForUndo() {

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

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#setEnsureVisible(boolean)
     */
    public void setEnsureVisible(boolean ensureVisible) {

        throw new UnsupportedOperationException();
    }
}
