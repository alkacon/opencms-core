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

package org.opencms.webdav;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.repository.A_CmsRepository;
import org.opencms.repository.I_CmsRepositorySession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;

/**
 * Session provider implementation.
 *
 * <p>Handles the OpenCms authorization.
 */
public class CmsDavSessionProvider implements DavSessionProvider {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDavSessionProvider.class);

    /** The repository implementation. */
    private A_CmsRepository m_repository;

    /**
     * Authorizes the user using HTTP BASIC authentication, and if successful, attaches the created session to the request
     *
     * @see org.apache.jackrabbit.webdav.DavSessionProvider#attachSession(org.apache.jackrabbit.webdav.WebdavRequest)
     */
    public boolean attachSession(WebdavRequest request) throws DavException {

        if (m_repository == null) {
            throw new IllegalStateException("Uninitialized repository");
        }
        String authHeader = request.getHeader("Authorization");
        I_CmsRepositorySession repoSession = null;
        String basic = HttpServletRequest.BASIC_AUTH;
        if ((authHeader != null) && authHeader.toUpperCase().startsWith(basic)) {
            String base64Token = authHeader.substring(basic.length() + 1);
            String token = new String(Base64.decodeBase64(base64Token.getBytes()));
            String password = null;
            String username = null;
            int pos = token.indexOf(":");
            if (pos != -1) {
                username = token.substring(0, pos);
                password = token.substring(pos + 1);
            }
            try {
                repoSession = m_repository.login(username, password);
            } catch (CmsException e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        if (repoSession == null) {
            throw new DavException(HttpServletResponse.SC_UNAUTHORIZED);
        }

        request.setDavSession(new CmsDavSession(repoSession));

        return true;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSessionProvider#releaseSession(org.apache.jackrabbit.webdav.WebdavRequest)
     */
    public void releaseSession(WebdavRequest request) {

        // TODO Auto-generated method stub

    }

    /**
     * Sets the repository.
     *
     * @param repository the repository
     */
    public void setRepository(A_CmsRepository repository) {

        m_repository = repository;
    }

}
