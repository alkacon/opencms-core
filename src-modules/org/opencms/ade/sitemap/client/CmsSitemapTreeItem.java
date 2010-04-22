/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/Attic/CmsSitemapTreeItem.java,v $
 * Date   : $Date: 2010/04/22 08:18:40 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;

/**
 * Sitemap entry tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.tree.CmsLazyTreeItem
 * @see org.opencms.ade.sitemap.shared.CmsClientSitemapEntry
 */
public class CmsSitemapTreeItem extends CmsLazyTreeItem {

    /** The handler. */
    private CmsSitemapHoverbarHandler m_handler;

    /**
     * Default constructor.<p>
     * 
     * @param widget the widget to use
     * @param handler the handler
     */
    public CmsSitemapTreeItem(CmsListItemWidget widget, CmsSitemapHoverbarHandler handler) {

        super(widget);
        m_handler = handler;
    }

    /**
     * Returns the underlying sitemap entry.<p>
     *
     * @return the underlying sitemap entry
     */
    public CmsClientSitemapEntry getEntry() {

        return m_handler.getEntry();
    }

    /**
     * Refreshes the displayed data from the given sitemap entry.<p>
     * 
     * @param entry the sitemap entry to update
     */
    public void updateEntry(CmsClientSitemapEntry entry) {

        CmsListItemWidget widget = (CmsListItemWidget)getWidget();
        widget.setTitleLabel(entry.getTitle());
        widget.setAdditionalInfoLabel(0, entry.getName());
        widget.setAdditionalInfoLabel(1, entry.getVfsPath());
        m_handler.updateEntry(entry);
    }
}
