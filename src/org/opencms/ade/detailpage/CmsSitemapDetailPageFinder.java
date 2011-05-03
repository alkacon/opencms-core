/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/detailpage/CmsSitemapDetailPageFinder.java,v $
 * Date   : $Date: 2011/05/03 10:49:15 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.detailpage;

import org.opencms.ade.config.CmsADEConfigurationManager;
import org.opencms.ade.config.CmsSitemapConfigurationData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class uses information from the detail page information stored in the sitemap to find the detail page for 
 * a given resource.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapDetailPageFinder implements I_CmsDetailPageFinder {

    /**
     * @see org.opencms.ade.detailpage.I_CmsDetailPageFinder#getAllDetailPages(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public Collection<String> getAllDetailPages(CmsObject cms, CmsResource res) throws CmsException {

        CmsADEConfigurationManager confManager = OpenCms.getADEConfigurationManager();
        String typeName = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        Map<String, List<CmsDetailPageInfo>> bestDetailPagesByType = confManager.getAllDetailPages(cms);
        List<CmsDetailPageInfo> pageInfos = bestDetailPagesByType.get(typeName);
        if (pageInfos == null) {
            return Collections.<String> emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (CmsDetailPageInfo pageInfo : pageInfos) {
            String uri = pageInfo.getUri();
            if (!CmsResource.isFolder(uri)) {
                uri = CmsResource.getFolderPath(uri);
            }
            result.add(uri);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.detailpage.I_CmsDetailPageFinder#getDetailPage(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getDetailPage(CmsObject cms, String rootPath, String linkSource) throws CmsException {

        String folder = CmsResource.getFolderPath(rootPath);
        if (rootPath.endsWith(".jsp") || rootPath.startsWith(CmsResource.VFS_FOLDER_SYSTEM + "/")) {
            // exclude these for performance reasons 
            return null;
        }
        if (folder == null) {
            return null;
        }
        Map<String, String> folderTypes = OpenCms.getADEConfigurationManager().getFolderTypes(cms);
        String folderType = folderTypes.get(folder);
        if (folderType == null) {
            return null;
        }
        CmsADEConfigurationManager confManager = OpenCms.getADEConfigurationManager();
        String rootLinkSource = cms.getRequestContext().addSiteRoot(linkSource);
        CmsSitemapConfigurationData sitemapConf = confManager.getSitemapConfiguration(cms, rootLinkSource);
        String typeName = folderType;
        List<CmsDetailPageInfo> pageInfos = sitemapConf.getDetailPageInfo().get(typeName);
        if ((pageInfos == null) || pageInfos.isEmpty()) {
            return null;
        }
        CmsDetailPageInfo info = pageInfos.get(0);
        String detailPageUri = info.getUri();
        if (!CmsResource.isFolder(detailPageUri)) {
            detailPageUri = CmsResource.getFolderPath(detailPageUri);
        }
        return detailPageUri;
    }

}
