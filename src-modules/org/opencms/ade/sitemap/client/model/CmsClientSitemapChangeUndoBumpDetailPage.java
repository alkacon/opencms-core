/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeUndoBumpDetailPage.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;

/**
 * Change class for undoing a detail page "bump" operation.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeUndoBumpDetailPage extends A_CmsClientSitemapChange {

    /** The original detail page index. */
    int m_originalIndex;

    /** The sitemap entry which was affected. */
    private CmsClientSitemapEntry m_entry;

    /**
     * Creates a new change.<p>
     * 
     * @param entry the entry on which the "Undo" change should operate
     * @param originalIndex the original index   
     */
    public CmsClientSitemapChangeUndoBumpDetailPage(CmsClientSitemapEntry entry, int originalIndex) {

        m_entry = entry;
        m_originalIndex = originalIndex;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.A_CmsClientSitemapChange#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    @Override
    public void applyToModel(CmsSitemapController controller) {

        if (m_originalIndex != -1) {
            controller.getDetailPageTable().move(m_entry.getId(), m_originalIndex);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.A_CmsClientSitemapChange#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    @Override
    public void applyToView(CmsSitemapView view) {

        if (m_originalIndex != -1) {
            view.updateDetailPageView(m_entry);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.I_CmsClientSitemapChange#isChangingDetailPages()
     */
    public boolean isChangingDetailPages() {

        return true;
    }

}
