/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2011/04/12 14:41:01 $
 * Version: $Revision: 1.81 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System 
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.ade.config.CmsSitemapConfigurationData;
import org.opencms.ade.detailpage.CmsSitemapDetailPageFinder;
import org.opencms.ade.detailpage.I_CmsDetailPageFinder;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

/**
 * Sitemap Manager.<p>
 * 
 * Provides all relevant functions for using the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.81 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager {

    /** The path to the sitemap editor jsp. */
    public static final String PATH_SITEMAP_EDITOR_JSP = "/system/modules/org.opencms.ade.sitemap/pages/sitemap.jsp";

    /** The detail page finder. */
    private I_CmsDetailPageFinder m_detailPageFinder = new CmsSitemapDetailPageFinder();

    /**
     * Creates a new sitemap manager.<p>
     * 
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsSitemapManager(CmsMemoryMonitor memoryMonitor, CmsSystemConfiguration systemConfiguration) {

        // do nothing

    }

    /**
     * Finds the entry point to a sitemap.<p>
     * 
     * @param cms the CMS context
     * @param openPath the resource path to find the sitemap to
     * 
     * @return the sitemap entry point
     * 
     * @throws CmsException
     */
    public String findEntryPoint(CmsObject cms, String openPath) throws CmsException {

        String openRootPath = cms.getRequestContext().addSiteRoot(openPath);
        CmsResource entryPoint = OpenCms.getADEConfigurationManager().getEntryPoint(cms, openRootPath);
        String result = cms.getSitePath(entryPoint);
        return result;
    }

    /**
     * Gets the detail page finder.
     *
     * @return the detail page finder
     */
    public I_CmsDetailPageFinder getDetailPageFinder() {

        return m_detailPageFinder;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param entryPoint the the sitemap entry point
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsObject cms, String entryPoint)
    throws CmsException {

        CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(entryPoint));
        return sitemapConfig.getPropertyConfiguration();
    }
}
