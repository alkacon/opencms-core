/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeNew.java,v $
 * Date   : $Date: 2010/05/18 12:58:17 $
 * Version: $Revision: 1.1 $
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
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.xml.sitemap.CmsSitemapChangeNew;
import org.opencms.xml.sitemap.I_CmsSitemapChange;
import org.opencms.xml.sitemap.I_CmsSitemapChange.Type;

/**
 * Stores one addition change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeNew implements I_CmsClientSitemapChange {

    /** Serialization unique id. */
    private static final long serialVersionUID = -8874622888825639281L;

    /** The new entry with children. */
    private CmsClientSitemapEntry m_entry;

    /**
     * Constructor.<p>
     * 
     * @param entry the new entry
     */
    public CmsClientSitemapChangeNew(CmsClientSitemapEntry entry) {

        m_entry = entry;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        CmsClientSitemapEntry newParent = controller.getEntry(CmsResource.getParentFolder(getEntry().getSitePath()));
        if (getEntry().getPosition() < 0) {
            newParent.addSubEntry(getEntry());
        } else {
            newParent.insertSubEntry(getEntry(), getEntry().getPosition());
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        CmsTreeItem newParent = view.getTreeItem(CmsResource.getParentFolder(getEntry().getSitePath()));
        CmsSitemapTreeItem newChild = view.create(getEntry());
        if (getEntry().getPosition() != -1) {
            newParent.insertChild(newChild, getEntry().getPosition());
        } else {
            newParent.addChild(newChild);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public I_CmsSitemapChange getChangeForCommit() {

        return new CmsSitemapChangeNew(
            getEntry().getSitePath(),
            getEntry().getPosition(),
            getEntry().getTitle(),
            getEntry().getVfsPath(),
            getEntry().getProperties());
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

        return new CmsClientSitemapChangeDelete(getEntry());
    }
}