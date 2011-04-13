/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsSitemapConfigurationCache.java,v $
 * Date   : $Date: 2011/04/13 07:06:11 $
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

package org.opencms.ade.config;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Cache class for sitemap configuration data.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapConfigurationCache extends CmsVfsMemoryObjectCache
implements I_CmsConfigurationDataReader<CmsSitemapConfigurationData> {

    /** An admin CMS object. **/
    private CmsObject m_adminCms;

    /** 
     * Creates a new instance.<p>
     * 
     * @param adminCms the admin CMS object to use 
     */
    public CmsSitemapConfigurationCache(CmsObject adminCms) {

        m_adminCms = adminCms;
    }

    /**
     * @see org.opencms.ade.config.I_CmsConfigurationDataReader#getConfiguration(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsSitemapConfigurationData getConfiguration(CmsObject cms, String path) throws CmsException {

        cms = initCmsObject(cms);
        Object cached = getCachedObject(cms, cms.getRequestContext().addSiteRoot(path));
        if (cached != null) {
            return (CmsSitemapConfigurationData)cached;
        }
        CmsResource configRes = cms.readResource(path);
        CmsConfigurationParser parser = new CmsConfigurationParser(cms, configRes);
        CmsSitemapConfigurationData result = parser.getSitemapConfigurationData(new CmsConfigurationSourceInfo(
            configRes,
            false));
        putCachedObject(cms, cms.getRequestContext().addSiteRoot(path), result);
        return result;
    }

    /**
     * Initializes a CMS object for internal use and copies the site root and project from an existing CMS object.<p> 
     * 
     * @param cms the CMS object from which the site root and project should be copied 
     * 
     * @return the initialized CMS object
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_adminCms);
        result.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        result.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        return result;
    }
}
