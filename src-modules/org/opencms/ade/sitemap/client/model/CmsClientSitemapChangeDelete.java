/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeDelete.java,v $
 * Date   : $Date: 2010/05/19 10:19:10 $
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

import org.opencms.ade.sitemap.client.CmsSitemapController;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.xml.sitemap.CmsSitemapChangeDelete;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

/**
 * Stores one deletion change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeDelete implements I_CmsClientSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -7543739240827775824L;

    /** The deleted entry with children. */
    private CmsClientSitemapEntry m_entry;

    private CmsSitemapTreeItem m_treeItem;

    /**
     * Constructor.<p>
     * 
     * @param entry the deleted entry
     */
    public CmsClientSitemapChangeDelete(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        CmsClientSitemapEntry deleteParent = controller.getEntry(CmsResource.getParentFolder(getEntry().getSitePath()));
        deleteParent.removeSubEntry(getEntry().getPosition());
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem deleteParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        view.ensureVisible(deleteParent);
        m_treeItem = (CmsSitemapTreeItem)deleteParent.removeChild(getEntry().getName());
        m_treeItem.setOpen(false);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public I_CmsSitemapChange getChangeForCommit() {

        return new CmsSitemapChangeDelete(getEntry().getSitePath());
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
}