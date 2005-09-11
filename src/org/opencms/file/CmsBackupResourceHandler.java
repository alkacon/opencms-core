/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsBackupResourceHandler.java,v $
 * Date   : $Date: 2005/09/11 13:27:06 $
 * Version: $Revision: 1.2 $
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
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Resource init handler that loads backup versions of resources.<p>
 *
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.2 $
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
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res) {

        String versionId = req.getParameter(PARAM_VERSIONID);

        // only do something if the resource was not found and there was a "versionid" parameter included
        if (resource == null && versionId != null) {

            String uri = cms.getRequestContext().getUri();
            // check if the resource starts with the BACKUP_HANDLER
            if (uri.startsWith(BACKUP_HANDLER)) {
                // test if the current user is allowed to read backup versions of resources
                // this can be done by trying to read the backup handler resource
                if (cms.existsResource(BACKUP_HANDLER)) {
                    try {
                        // extract the "real" resourcename
                        uri = uri.substring(BACKUP_HANDLER.length(), uri.length());
                        int id = new Integer(versionId).intValue();
                        // we now must switch to the root site to read the backup resource
                        cms.getRequestContext().saveSiteRoot();
                        cms.getRequestContext().setSiteRoot("/");
                        resource = cms.readBackupFile(uri, id);
                        // store a request attribute to indicate that this is in fact a backup version
                        req.setAttribute(ATTRIBUTE_NAME, Boolean.TRUE);
                    } catch (CmsException e) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error(
                                Messages.get().container(Messages.ERR_BACKUPRESOURCE_2, uri, versionId));
                        }
                    } finally {
                        // restore the siteroot and modify the uri to the one of the correct resource
                        cms.getRequestContext().restoreSiteRoot();
                        cms.getRequestContext().setUri(cms.getSitePath(resource));
                    }
                }
            }
        }
        return resource;
    }
}