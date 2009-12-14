/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapResourceHandler.java,v $
 * Date   : $Date: 2009/12/14 09:41:04 $
 * Version: $Revision: 1.8 $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads a resource given its sitemap's URI.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapResourceHandler implements I_CmsResourceInit {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapResourceHandler.class);

    /**
     * Default constructor.<p>
     */
    public CmsSitemapResourceHandler() {

        // empty
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException {

        // check if the resource was already found
        boolean abort = (resource != null);
        // check if the resource comes from the root site
        abort |= CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot());
        // check if the resource comes from the /system/ folder
        abort |= cms.getRequestContext().getUri().startsWith("/system/");
        if (abort) {
            // skip in all cases above 
            return resource;
        }

        // check if the resource is in the site map
        try {
            // find the site map entry
            CmsSiteEntryBean entry = OpenCms.getSitemapManager().getEntryForUri(cms, cms.getRequestContext().getUri());
            if (entry == null) {
                return resource;
            }
            // read the resource
            resource = cms.readResource(entry.getResourceId());
            // set the element
            req.setAttribute(CmsSitemapManager.ATTR_SITEMAP_ENTRY, entry.cloneWithoutSubEntries());
            // store the requested path 
            req.setAttribute(CmsSitemapManager.ATTR_SITEMAP_CURRENT_URI, cms.getRequestContext().getUri());
            // set the resource path
            cms.getRequestContext().setUri(cms.getSitePath(resource));
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_SITEMAP_1, uri);
            LOG.error(msg.key(), e);
            throw new CmsResourceInitException(msg, e);
        }
        return resource;
    }
}