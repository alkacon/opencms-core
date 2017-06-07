/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the system folder to every root folder of target sites.<p>
 *
 * @since 6.5.6
 */
public class CmsResourceWrapperSystemFolder extends A_CmsResourceWrapper {

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#addResourcesToFolder(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        if (!resourcename.endsWith("/")) {
            resourcename += "/";
        }

        // if this is the root folder of a target site, add the system folder
        if (resourcename.equals("/")) {
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot())) {
                List<CmsResource> ret = new ArrayList<CmsResource>();
                ret.add(readResource(cms, CmsWorkplace.VFS_PATH_SYSTEM, filter));
                return ret;
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(CmsObject, CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        if (res.isFolder()) {
            if (!cms.getRequestContext().getSiteRoot().equals("/")) {

                String resourcename = cms.getRequestContext().removeSiteRoot(res.getRootPath());

                if (!resourcename.endsWith("/")) {
                    resourcename += "/";
                }

                if (resourcename.equals("/")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // only valid if site root is a target site
        if (!cms.getRequestContext().getSiteRoot().equals("/")) {

            if (!resourcename.endsWith("/")) {
                resourcename += "/";
            }

            // if accessing the system folder switch temporarily to the root site
            if (resourcename.equals(CmsWorkplace.VFS_PATH_SYSTEM)) {

                // set site root to the root folder
                String siteRoot = cms.getRequestContext().getSiteRoot();
                cms.getRequestContext().setSiteRoot("/");

                // read the resource with the correct site root
                CmsResource res = cms.readResource(resourcename, filter);

                // reset the site root back to the original
                cms.getRequestContext().setSiteRoot(siteRoot);

                // adjust the root path in the resource
                CmsWrappedResource wrap = new CmsWrappedResource(res);
                wrap.setRootPath(cms.getRequestContext().getSiteRoot() + resourcename);

                return wrap.getResource();
            }
        }

        return null;
    }
}
