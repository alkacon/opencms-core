/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManager.java,v $
 * Date   : $Date: 2011/03/23 14:51:56 $
 * Version: $Revision: 1.62 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.site;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import java.util.List;

/**
 * Static site manager, kept for reasons of keeping the API backward compatible only.<p>
 *
 * Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance
 * instead of the direct static access methods of this class.<p>
 *
 * Please note that this class may be removed or significantly changes in the next major OpenCms 
 * released, so make sure you remove all references ASAP.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.62 $ 
 * 
 * @since 6.0.0 
 * 
 * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance
 *      instead of the direct static access methods of this class.
 */
public final class CmsSiteManager {

    /**
     * Hide the public constructor.<p>
     */
    private CmsSiteManager() {

        // NOOP
    }

    /**
     * Returns a list of all sites available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * 
     * @return a list of all site available for the current user
     * 
     * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance, 
     *      then use the method with the same name and signature.
     */
    public static List getAvailableSites(CmsObject cms, boolean workplaceMode) {

        return OpenCms.getSiteManager().getAvailableSites(cms, workplaceMode, cms.getRequestContext().getOuFqn());
    }

    /**
     * Returns a list of all sites that are compatible to the given organizational unit.<p>
     * 
     * @param cms the current cms context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * @param ouFqn the organizational unit
     * 
     * @return a list of all site available for the current user
     * 
     * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance, 
     *      then use the method with the same name and signature.
     */
    public static List getAvailableSites(CmsObject cms, boolean workplaceMode, String ouFqn) {

        return OpenCms.getSiteManager().getAvailableSites(cms, workplaceMode, ouFqn);
    }

    /**
     * Returns the current site for the provided OpenCms user context object.<p>
     * 
     * In the unlikely case that no site matches with the provided OpenCms user context,
     * the default site is returned.<p>
     * 
     * @param cms the OpenCms user context object to check for the site
     * 
     * @return the current site for the provided OpenCms user context object
     * 
     * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance, 
     *      then use the method with the same name and signature.
     */
    public static CmsSite getCurrentSite(CmsObject cms) {

        return OpenCms.getSiteManager().getCurrentSite(cms);
    }

    /**
    * Returns the site with has the provided site root path, 
     * or <code>null</code> if no configured site has that root path.<p>
     * 
     * The site root must have the form:
     * <code>/sites/default</code>.<br>
     * That means there must be a leading, but no trailing slash.<p>
     * 
     * @param siteRoot the root path to look up the site for
     * 
     * @return the site with has the provided site root path, 
     *      or <code>null</code> if no configured site has that root path
     *      
     * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance, 
     *      then use the method with the same name and signature.
     */
    public static CmsSite getSite(String siteRoot) {

        return OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
    }

    /**
     * Returns the site root part of the resources root path, 
     * or <code>null</code> if the path does not match any site root.<p>
     * 
     * The site root returned will have the form:
     * <code>/sites/default</code>.<br>
     * That means there will a leading, but no trailing slash.<p>
     * 
     * @param rootPath the root path of a resource
     * 
     * @return the site root part of the resources root path, 
     *      or <code>null</code> if the path does not match any site root
     * 
     * @deprecated Use {@link OpenCms#getSiteManager()} to obtain the configured site manager instance, 
     *      then use the method with the same name and signature.
     */
    public static String getSiteRoot(String rootPath) {

        return OpenCms.getSiteManager().getSiteRoot(rootPath);
    }
}