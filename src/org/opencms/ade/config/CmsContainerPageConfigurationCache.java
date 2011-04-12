/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsContainerPageConfigurationCache.java,v $
 * Date   : $Date: 2011/04/12 11:59:14 $
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

package org.opencms.ade.config;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Cache class for container page configuration beans.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageConfigurationCache extends CmsVfsMemoryObjectCache
implements I_CmsConfigurationDataReader<CmsContainerPageConfigurationData> {

    /** An admin CMS object. */
    private CmsObject m_adminCms;

    /**
     * Creates a new instance.<p>
     * 
     * @param adminCms an admin CMS object 
     */
    public CmsContainerPageConfigurationCache(CmsObject adminCms) {

        m_adminCms = adminCms;
    }

    /**
     * @see org.opencms.ade.config.I_CmsConfigurationDataReader#getConfiguration(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsContainerPageConfigurationData getConfiguration(CmsObject cms, String sitePath) throws CmsException {

        cms = initCmsObject(cms);
        String rootPath = cms.getRequestContext().addSiteRoot(sitePath);

        Object cached = getCachedObject(cms, rootPath);
        if (cached != null) {
            return (CmsContainerPageConfigurationData)cached;
        }

        CmsResource configRes = cms.readResource(sitePath);
        CmsConfigurationParser parser = new CmsConfigurationParser(cms, configRes);
        CmsContainerPageConfigurationData result = parser.getContainerPageConfigurationData(new CmsConfigurationSourceInfo(
            configRes,
            false));
        putCachedObject(cms, rootPath, result);
        return result;
    }

    /**
     * Initializes a CMS object for internal use, and copies the project and site root from another CMS object.<p>
     *  
     * @param cms the current CMS context
     *  
     * @return an initialized CMS object 
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_adminCms);
        result.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        result.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        return result;
    }

}
