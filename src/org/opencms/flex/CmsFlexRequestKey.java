/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexRequestKey.java,v $
 * Date   : $Date: 2005/04/22 14:38:35 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Describes the caching behaviour (or caching options) for a Flex request.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsFlexRequestKey {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexRequestKey.class); 

    /** The request context this request was made in. */
    private CmsRequestContext m_context;

    /** The (Flex) Http request this key was constructed for. */
    private HttpServletRequest m_request;

    /** The OpenCms resource that this key is used for. */
    private String m_resource;

    /**
     * This constructor is used when building a cache key from a request.<p>
     * 
     * The request contains several data items that are neccessary to construct
     * the output. These items are e.g. the Query-String, the requested resource,
     * the current time etc. etc.
     * All required items are saved in the constructed cache - key.<p>
     * 
     * @param request the request to construct the key for
     * @param target the requested resource in the OpenCms VFS
     * @param online must be true for an online resource, false for offline resources
     */
    public CmsFlexRequestKey(HttpServletRequest request, String target, boolean online) {

        // store the request
        m_request = request;

        // fetch the cms from the request
        CmsObject cms = ((CmsFlexController)m_request.getAttribute(CmsFlexController.ATTRIBUTE_NAME)).getCmsObject();

        // store the request context
        m_context = cms.getRequestContext();

        // calculate the resource name
        m_resource = CmsFlexCacheKey.getKeyName(m_context.addSiteRoot(target), online);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXREQUESTKEY_CREATED_NEW_KEY_1, m_resource));
        }
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public String getElement() {

        return m_request.getParameter(I_CmsConstants.C_PARAMETER_ELEMENT);
    }

    /**
     * Returns the encoding.<p>
     *
     * @return the encoding
     */
    public String getEncoding() {

        return m_context.getEncoding();
    }

    /**
     * Returns the ip.<p>
     *
     * @return the ip
     */
    public String getIp() {

        return m_context.getRemoteAddress();
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_context.getLocale().toString();
    }

    /**
     * Returns the params.<p>
     *
     * @return the params
     */
    public Map getParams() {

        // get the params
        Map params = m_request.getParameterMap();
        if (params.size() == 0) {
            return null;
        }
        return params;
    }

    /**
     * Returns the port.<p>
     *
     * @return the port
     */
    public Integer getPort() {

        return new Integer(m_request.getServerPort());
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public String getResource() {

        return m_resource;
    }

    /**
     * Returns the schemes.<p>
     *
     * @return the schemes
     */
    public String getScheme() {

        return m_request.getScheme().toLowerCase();
    }

    /**
     * Returns the the current users session, or <code>null</code> if the current user 
     * has no session.<p>
     * 
     * @return the current users session, or <code>null</code> if the current user has no session
     */
    public HttpSession getSession() {
        
        return m_request.getSession(false);
    }

    /**
     * Returns the uri.<p>
     *
     * @return the uri
     */
    public String getUri() {

        return m_context.addSiteRoot(m_context.getUri());
    }

    /**
     * Returns the user.<p>
     *
     * @return the user
     */
    public String getUser() {

        return m_context.currentUser().getName();
    }
}