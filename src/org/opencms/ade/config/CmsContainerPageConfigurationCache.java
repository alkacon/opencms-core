/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsContainerPageConfigurationCache.java,v $
 * Date   : $Date: 2011/05/06 10:15:44 $
 * Version: $Revision: 1.5 $
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

package org.opencms.ade.config;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Cache class for container page configuration beans.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsContainerPageConfigurationCache extends CmsVfsMemoryObjectCache
implements I_CmsConfigurationDataReader<CmsContainerPageConfigurationData> {

    /** An admin CMS object. */
    private CmsObject m_adminCms;

    /** The map of combined configurations. */
    private Map<Boolean, Map<String, CmsContainerPageConfigurationData>> m_combinedConfigurations = Maps.newHashMap();

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsContainerPageConfigurationCache.class);

    /** The list of cache flush handlers. */
    private List<I_CmsCacheFlushHandler> m_flushHandlers = new ArrayList<I_CmsCacheFlushHandler>();

    /**
     * Creates a new instance.<p>
     * 
     * @param adminCms an admin CMS object 
     */
    public CmsContainerPageConfigurationCache(CmsObject adminCms) {

        m_adminCms = adminCms;
        m_combinedConfigurations.put(Boolean.TRUE, new HashMap<String, CmsContainerPageConfigurationData>());
        m_combinedConfigurations.put(Boolean.FALSE, new HashMap<String, CmsContainerPageConfigurationData>());
    }

    /**
     * Adds a new cache flush handler.<p>
     * 
     * @param handler the cache flush handler to add 
     */
    public void addFlushHandler(I_CmsCacheFlushHandler handler) {

        m_flushHandlers.add(handler);
    }

    /**
     * @see org.opencms.ade.config.I_CmsConfigurationDataReader#getCombinedConfiguration(java.lang.String, boolean)
     */
    public CmsContainerPageConfigurationData getCombinedConfiguration(String rootPath, boolean online) {

        return m_combinedConfigurations.get(new Boolean(online)).get(rootPath);
    }

    /**
     * @see org.opencms.ade.config.I_CmsConfigurationDataReader#getConfiguration(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsContainerPageConfigurationData getConfiguration(CmsObject cms, String sitePath) throws CmsException {

        synchronized (OpenCms.getADEConfigurationManager()) {

            cms = initCmsObject(cms);
            String rootPath = cms.getRequestContext().addSiteRoot(sitePath);

            Object cached = getCachedObject(cms, rootPath);
            if (cached != null) {
                return (CmsContainerPageConfigurationData)cached;
            }
            try {
                CmsResource configRes = cms.readResource(sitePath);
                CmsConfigurationParser parser = new CmsConfigurationParser(
                    cms,
                    configRes,
                    CmsConfigurationParser.PARSER.CONTAINERPAGE);
                CmsContainerPageConfigurationData result = parser.getContainerPageConfigurationData(new CmsConfigurationSourceInfo(
                    configRes,
                    false));
                putCachedObject(cms, rootPath, result);
                return result;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return new CmsContainerPageConfigurationData();
            }
        }
    }

    /**
     * @see org.opencms.ade.config.I_CmsConfigurationDataReader#setCombinedConfiguration(java.lang.String, boolean, java.lang.Object)
     */
    public void setCombinedConfiguration(String rootPath, boolean online, CmsContainerPageConfigurationData config) {

        synchronized (OpenCms.getADEConfigurationManager()) {
            m_combinedConfigurations.get(new Boolean(online)).put(rootPath, config);
        }
    }

    /**
     * Notifies the cache flush handlers of a cache flush.<p>
     * 
     * @param online true if the online cache is being flushed 
     */
    protected void fireFlush(boolean online) {

        for (I_CmsCacheFlushHandler handler : m_flushHandlers) {
            handler.onFlushCache(online);
        }
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        synchronized (OpenCms.getADEConfigurationManager()) {

            super.flush(online);
            fireFlush(online);
            m_combinedConfigurations.get(new Boolean(online)).clear();
        }
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
    protected CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(m_adminCms);
        result.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        result.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        return result;
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource resource) {

        super.uncacheResource(resource);
        String resTypeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        String configTypeName = CmsResourceTypeXmlContainerPage.CONFIGURATION_TYPE_NAME;
        boolean isTemp = CmsResource.isTemporaryFileName(resource.getRootPath());
        if (resTypeName.equals(configTypeName) && !isTemp) {
            synchronized (OpenCms.getADEConfigurationManager()) {
                fireFlush(false);
                m_combinedConfigurations.get(Boolean.FALSE).clear();
            }
        }
    }

}
