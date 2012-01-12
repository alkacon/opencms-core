/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.security.CmsPermissionViolationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads a resource given its permalink.<p>
 *
 * The permalink must have following format:<br>
 * <code>/${CONTEXT}/${SERVLET}/permalink/${UUID}.${EXT}</code><p>
 * 
 * for example:<br>
 * <code>/opencms/opencms/permalink/a7b5d298-b3ab-11d8-b3e3-514d35713fed.html</code><p>
 * 
 * @since 6.3 
 */
public class CmsPermalinkResourceHandler implements I_CmsResourceInit {

    /** The permalink handler path. */
    public static final String PERMALINK_HANDLER = "/permalink/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPermalinkResourceHandler.class);

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException, CmsPermissionViolationException {

        // only do something if the resource was not found 
        if (resource == null) {
            String uri = cms.getRequestContext().getUri();
            // check if the resource starts with the PERMALINK_HANDLER
            if (uri.startsWith(PERMALINK_HANDLER)) {
                // get the id of the real resource
                String id = uri.substring(PERMALINK_HANDLER.length());
                String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    // we now must switch to the root site to read the resource
                    cms.getRequestContext().setSiteRoot("/");

                    // get rid of the file extension if needed
                    int pos = id.indexOf('.');
                    if (pos > -1) {
                        id = id.substring(0, id.indexOf('.'));
                    }

                    // read the resource
                    resource = cms.readDefaultFile(id);
                } catch (CmsPermissionViolationException e) {
                    throw e;
                } catch (Throwable e) {
                    CmsMessageContainer msg = Messages.get().container(Messages.ERR_PERMALINK_1, id);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(msg.key(), e);
                    }
                    throw new CmsResourceInitException(msg, e);
                } finally {
                    // restore the siteroot
                    cms.getRequestContext().setSiteRoot(storedSiteRoot);
                    // resource may be null in case of an error
                    if (resource != null) {
                        // modify the uri to the one of the real resource
                        cms.getRequestContext().setUri(cms.getSitePath(resource));
                    }
                }
            }
        }
        return resource;
    }
}