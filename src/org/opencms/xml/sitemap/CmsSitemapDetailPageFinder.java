/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapDetailPageFinder.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class uses information from the detail page information stored in the sitemap to find the detail page for 
 * a given resource.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapDetailPageFinder implements I_CmsDetailPageFinder {

    /**
     * @see org.opencms.xml.sitemap.I_CmsDetailPageFinder#getAllDetailPages(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Collection<String> getAllDetailPages(CmsObject cms, CmsResource res) throws CmsException {

        CmsSitemapManager manager = OpenCms.getSitemapManager();
        CmsResourceManager resManager = OpenCms.getResourceManager();
        String resourceType = resManager.getResourceType(res.getTypeId()).getTypeName();
        List<CmsDetailPageInfo> detailPages = manager.getBestDetailPages(cms, resourceType);
        List<String> result = new ArrayList<String>();
        for (CmsDetailPageInfo info : detailPages) {
            result.add(info.getUri());
        }
        return result;
    }

    /**
     * @see org.opencms.xml.sitemap.I_CmsDetailPageFinder#getDetailPage(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String)
     */
    public String getDetailPage(CmsObject cms, CmsResource res, String linkSource) throws CmsException {

        if (!CmsResourceTypeXmlContent.isXmlContent(res)) {
            return null;
        }

        CmsSitemapManager sitemapManager = OpenCms.getSitemapManager();
        CmsSitemapEntry entry = sitemapManager.getEntryForUri(cms, linkSource);
        String type = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        CmsDetailPageInfo detailPageInfo = null;

        if (entry.isSitemap()) {
            detailPageInfo = entry.getSitemapInfo().getBestDetailPage(type);
        }
        if (detailPageInfo == null) {
            CmsSitemapRuntimeInfo info = sitemapManager.getRuntimeInfoForSite(
                cms,
                cms.getRequestContext().getSiteRoot(),
                cms.getRequestContext().getLocale());

            if (info != null) {
                detailPageInfo = info.getBestDetailPage(type);
            }
        }

        if (detailPageInfo == null) {
            return (new CmsPropertyDetailPageFinder()).getDetailPage(cms, res, linkSource);
        }

        return detailPageInfo.getUri();
    }
}
