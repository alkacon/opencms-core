/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsSwitchLinkStrategyHandler.java,v $
 * Date   : $Date: 2010/09/30 10:09:14 $
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Switch link strategy.<p>
 * 
 * Switches between the sitemap strategy and the VFS strategy
 * depending on the sitemap usesage in the given context.<p>
 *
 * @author  Ruediger Kurz
 *
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsSwitchLinkStrategyHandler implements I_CmsLinkStrategyHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSwitchLinkStrategyHandler.class);

    /** A reference on the sitemap strategy. */
    private CmsSitemapLinkStrategyHandler m_sitemapStrategy;

    /** A reference on the VFS strategy. */
    private CmsVfsLinkStrategyHandler m_vfsStrategy;

    /**
     * A private constructor that initializes the two possible strategies.<p> 
     */
    public CmsSwitchLinkStrategyHandler() {

        m_sitemapStrategy = new CmsSitemapLinkStrategyHandler();
        m_vfsStrategy = new CmsVfsLinkStrategyHandler();
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#getRfsName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getRfsName(CmsObject cms, String vfsName, String parameters) {

        // make the decision
        if (!isSitemapInUse(cms, vfsName, false)) {
            return m_vfsStrategy.getRfsName(cms, vfsName, parameters);
        }
        return m_sitemapStrategy.getRfsName(cms, vfsName, parameters);
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#getVfsNameInternal(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsStaticExportData getVfsNameInternal(CmsObject cms, String rfsName) {

        // make the decision
        if (!isSitemapInUse(cms, rfsName, true)) {
            return m_vfsStrategy.getVfsNameInternal(cms, rfsName);
        }
        return m_sitemapStrategy.getVfsNameInternal(cms, rfsName);
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#isExportLink(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean isExportLink(CmsObject cms, String vfsName) {

        // make the decision
        if (!isSitemapInUse(cms, vfsName, false)) {
            return m_vfsStrategy.isExportLink(cms, vfsName);
        }
        return m_sitemapStrategy.isExportLink(cms, vfsName);
    }

    /**
     * Returns the site root for a given RFS name or <code>null</code> if no site root was found.<p>
     * 
     * @param cms the current cms object
     * @param path the RFS name to get the site root for 
     * @param isRfsPath signals if the given name is a RFS name
     * 
     * @return the site root for a given RFS name or <code>null</code> if no site root was found
     */
    private String getSiteRootForPath(CmsObject cms, String path, boolean isRfsPath) {

        String siteRoot;
        if (!(cms.getRequestContext().getSiteRoot().equals("") || cms.getRequestContext().getSiteRoot().equals("/"))) {
            // the cms object has a site root set
            return cms.getRequestContext().getSiteRoot();
        } else if (path.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            // the name starts with "/system/"
            return null;
        } else {
            // the name starts with a configured site e.g. "/sites/default/"
            siteRoot = OpenCms.getSiteManager().getSiteRoot(path);
        }
        // if the site root is still not found and the path is a RFS path 
        if (((isRfsPath && (CmsStringUtil.isEmptyOrWhitespaceOnly(siteRoot) || siteRoot.equals("/"))))) {
            // the name starts with a export name
            String folderName = null;
            for (Map.Entry<String, String> exportNameRes : OpenCms.getStaticExportManager().getExportnames().entrySet()) {
                if (path.startsWith(exportNameRes.getKey())) {
                    folderName = exportNameRes.getValue();
                    break;
                }
            }
            siteRoot = OpenCms.getSiteManager().getSiteRoot(folderName);
        }
        return siteRoot;
    }

    /**
     * Returns <code>true</code> if a sitemap is in use for the given name.<p>
     * 
     * @param cms the cms object
     * @param path the path to check
     * @param isRfsPath signals if the given name is a RFS name
     * 
     * @return <code>true</code> if a sitemap is in use for the given name
     */
    private boolean isSitemapInUse(CmsObject cms, String path, boolean isRfsPath) {

        try {
            if (OpenCms.getSitemapManager().isSiteUsingSitemap(cms, getSiteRootForPath(cms, path, isRfsPath))) {
                return true;
            }
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        return false;
    }
}
