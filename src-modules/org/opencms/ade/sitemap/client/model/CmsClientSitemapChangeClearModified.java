/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeClearModified.java,v $
 * Date   : $Date: 2011/05/03 10:48:54 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;

/**
 * Stores a clear modified list change to the sitemap.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeClearModified implements I_CmsClientSitemapChange {

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToClipboardView(org.opencms.ade.sitemap.client.toolbar.CmsToolbarClipboardView)
     */
    public void applyToClipboardView(CmsToolbarClipboardView view) {

        view.clearModified();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    public void applyToModel(CmsSitemapController controller) {

        controller.getData().getClipboardData().getModifications().clear();
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    public void applyToView(CmsSitemapView view) {

        // nothing to do
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#getChangeForCommit()
     */
    public CmsSitemapChange getChangeForCommit() {

        CmsSitemapClipboardData clipboardData = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        clipboardData.getModifications().clear();
        CmsSitemapChange change = new CmsSitemapChange(null, null, ChangeType.clipboardOnly);
        change.setClipBoardData(clipboardData);
        return change;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return false;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#updateEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        // nothing to do

    }

}
