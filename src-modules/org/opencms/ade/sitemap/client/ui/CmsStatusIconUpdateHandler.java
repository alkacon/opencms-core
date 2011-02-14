/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsStatusIconUpdateHandler.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
 * Version: $Revision: 1.7 $
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.I_CmsPropertyUpdateHandler;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.shared.CmsListInfoBean.PageIcon;

/**
 * A class which changes the status icon of a sitemap item when the "secure" or "export" properties
 * of the corresponding sitemap entry change.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsStatusIconUpdateHandler implements I_CmsPropertyUpdateHandler {

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsPropertyUpdateHandler#handlePropertyUpdate(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void handlePropertyUpdate(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem item = CmsSitemapView.getInstance().getTreeItem(entry.getSitePath());
        if (item == null) {
            return;
        }
        CmsSitemapController controller = CmsSitemapView.getInstance().getController();
        String secureProp = controller.getEffectiveProperty(entry, "secure");
        String exportProp = controller.getEffectiveProperty(entry, "export");

        PageIcon icon = PageIcon.none;
        if (!entry.isInNavigation()) {
            icon = PageIcon.hidden;
        }
        if ("true".equals(exportProp)) {
            icon = PageIcon.export;
        }
        if ("true".equals(secureProp)) {
            icon = PageIcon.secure;
        }
        //        if ((entry.getOwnProperty(CmsSitemapManager.Property.isRedirect.getName())) != null) {
        //            icon = PageIcon.redirect;
        //        }
        item.setPageIcon(icon);
    }
}
