/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsBackupResourceHandler.java,v $
 * Date   : $Date: 2006/11/27 16:02:34 $
 * Version: $Revision: 1.3.4.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads backup versions of resources.<p>
 *
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.3.4.3 $
 * 
 * @since 6.0.1 
 */
public class CmsBackupResourceHandler implements I_CmsResourceInit {

    /** Constant for the backup request attribute name. */
    public static final String ATTRIBUTE_NAME = "org.opencms.file.CmsBackupResourceHandler";

    /** The backup handler path. */
    public static final String BACKUP_HANDLER = "/system/shared/showversion";

    /** Request parameter name for the version id. */
    public static final String PARAM_VERSIONID = "versionid";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsBackupResourceHandler.class);

    /**
     * Returns the backup resource if the given request is displaying a history backup version.<p> 
     * 
     * @param req the request to check
     * 
     * @return the backup resource if the given request is displaying a history backup version
     */
    public static CmsBackupResource getBackupResouce(ServletRequest req) {

        return (CmsBackupResource)req.getAttribute(ATTRIBUTE_NAME);
    }

    /**
     * Returns <code>true</code> if the given request is displaying a history backup version.<p> 
     * 
     * @param req the request to check
     * 
     * @return <code>true</code> if the given request is displaying a history backup version
     */
    public static boolean isBackupRequest(ServletRequest req) {

        return null != req.getAttribute(ATTRIBUTE_NAME);
    }

    /**
     * @throws CmsResourceInitException 
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException {

        // we only have to check for backup resources if the handler was called
        // during a real request and NOT during a dummy-request while doing
        // a static export
        if (req != null) {
            String versionId = req.getParameter(PARAM_VERSIONID);

            // only do something if the resource was not found and there was a "versionid" parameter included
            if ((resource == null) && (versionId != null)) {

                String uri = cms.getRequestContext().getUri();
                // check if the resource starts with the BACKUP_HANDLER
                if (uri.startsWith(BACKUP_HANDLER)) {
                    // test if the current user is allowed to read backup versions of resources
                    // this can be done by trying to read the backup handler resource
                    if (cms.existsResource(BACKUP_HANDLER)) {
                        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                        try {
                            // we now must switch to the root site to read the backup resource
                            cms.getRequestContext().setSiteRoot("/");

                            // extract the "real" resourcename
                            uri = uri.substring(BACKUP_HANDLER.length(), uri.length());
                            int id = new Integer(versionId).intValue();
                            resource = cms.readBackupFile(uri, id);

                            // store a request attribute to indicate that this is in fact a backup version
                            req.setAttribute(ATTRIBUTE_NAME, resource);
                        } catch (CmsException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error(Messages.get().getBundle().key(Messages.ERR_BACKUPRESOURCE_2, uri, versionId));
                            }
                            throw new CmsResourceInitException(Messages.get().container(
                                Messages.ERR_SHOWVERSION_2,
                                uri,
                                versionId), e);
                        } finally {
                            // restore the siteroot and modify the uri to the one of the correct resource
                            cms.getRequestContext().setSiteRoot(storedSiteRoot);
                            if (resource != null) {
                                // resource may be null in case of an error
                                cms.getRequestContext().setUri(cms.getSitePath(resource));
                            }
                        }
                    }
                }
            }
        }
        return resource;
    }
}