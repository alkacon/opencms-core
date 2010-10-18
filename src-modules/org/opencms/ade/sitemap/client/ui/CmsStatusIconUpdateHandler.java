/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsStatusIconUpdateHandler.java,v $
 * Date   : $Date: 2010/10/18 10:05:41 $
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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem.StatusIcon;
import org.opencms.ade.sitemap.client.control.I_CmsPropertyUpdateHandler;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;

import java.util.Map;

/**
 * A class which changes the status icon of a sitemap item when the "secure" or "export" properties
 * of the corresponding sitemap entry change.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsStatusIconUpdateHandler implements I_CmsPropertyUpdateHandler {

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsPropertyUpdateHandler#handlePropertyUpdate(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void handlePropertyUpdate(CmsClientSitemapEntry entry) {

        Map<String, CmsComputedPropertyValue> myProps = entry.getInheritedProperties();
        CmsSitemapTreeItem item = CmsSitemapView.getInstance().getTreeItem(entry.getSitePath());
        CmsComputedPropertyValue secureProp = myProps.get("secure");
        CmsComputedPropertyValue exportProp = myProps.get("export");
        StatusIcon icon = StatusIcon.none;
        if ((exportProp != null) && "true".equals(exportProp.getOwnValue())) {
            icon = StatusIcon.export;
        }
        if ((secureProp != null) && "true".equals(secureProp.getOwnValue())) {
            icon = StatusIcon.secure;
        }
        item.setStatus(icon);
    }
}
