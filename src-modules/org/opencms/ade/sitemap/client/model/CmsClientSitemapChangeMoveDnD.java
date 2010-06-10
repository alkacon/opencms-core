/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeMoveDnD.java,v $
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

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;

/**
 * Stores one move change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeMoveDnD extends CmsClientSitemapChangeMove {

    /**
     * Constructor.<p>
     * 
     * @param sourcePath the source path
     * @param sourcePosition the source position
     * @param destinationPath the destination path
     * @param destinationPosition the destination position
     */
    public CmsClientSitemapChangeMoveDnD(
        String sourcePath,
        int sourcePosition,
        String destinationPath,
        int destinationPosition) {

        super(sourcePath, sourcePosition, destinationPath, destinationPosition);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    @Override
    public void applyToView(CmsSitemapView view) {

        CmsSitemapTreeItem destItem = view.getTreeItem(getDestinationPath());
        destItem.updateSitePath(getDestinationPath());
        if (m_ensureVisible) {
            view.ensureVisible(destItem);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForUndo()
     */
    @Override
    public I_CmsClientSitemapChange getChangeForUndo() {

        return new CmsClientSitemapChangeMove(
            getSourcePath(),
            getSourcePosition(),
            getDestinationPath(),
            getDestinationPosition());
    }
}