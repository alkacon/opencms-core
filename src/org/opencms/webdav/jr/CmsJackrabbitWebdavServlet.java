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

package org.opencms.webdav.jr;

import org.opencms.main.OpenCms;
import org.opencms.repository.A_CmsRepository;

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.header.IfHeader;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.SimpleLockManager;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;

import com.google.common.collect.Iterators;

/**
 * Webdav access servlet for OpenCms, implemented using jackrabbit-webdav library.
 */
public class CmsJackrabbitWebdavServlet extends AbstractWebdavServlet {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The locator factory. */
    private CmsDavLocatorFactory m_locatorFactory = new CmsDavLocatorFactory();

    /** The session provider. */
    private CmsDavSessionProvider m_sessionProvider = new CmsDavSessionProvider();

    /** The resource factory. */
    private CmsDavResourceFactory m_resourceFactory = new CmsDavResourceFactory();

    /** The lock manager. */
    private LockManager m_lockManager = new SimpleLockManager();

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getDavSessionProvider()
     */
    @Override
    public DavSessionProvider getDavSessionProvider() {

        return m_sessionProvider;
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getLocatorFactory()
     */
    @Override
    public DavLocatorFactory getLocatorFactory() {

        return m_locatorFactory;
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getResourceFactory()
     */
    @Override
    public DavResourceFactory getResourceFactory() {

        return m_resourceFactory;
    }

    /**
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        final LinkedHashMap<String, String> params = new LinkedHashMap<>();
        for (String name : Collections.list(config.getInitParameterNames())) {
            params.put(name, config.getInitParameter(name));
        }
        // Force relative URI
        params.put(AbstractWebdavServlet.INIT_PARAM_CREATE_ABSOLUTE_URI, "false");
        super.init(new ServletConfig() {

            public String getInitParameter(String name) {

                return params.get(name);

            }

            public Enumeration<String> getInitParameterNames() {

                return Collections.enumeration(params.keySet());
            }

            public ServletContext getServletContext() {

                return config.getServletContext();
            }

            public String getServletName() {

                return config.getServletName();
            }
        });
        String repName = config.getInitParameter(CmsDavUtil.PARAM_REPOSITORY);
        A_CmsRepository repository = OpenCms.getRepositoryManager().getRepository(repName, A_CmsRepository.class);
        m_sessionProvider.setRepository(repository);
        m_resourceFactory.setLockManager(m_lockManager);
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setDavSessionProvider(org.apache.jackrabbit.webdav.DavSessionProvider)
     */
    @Override
    public void setDavSessionProvider(DavSessionProvider davSessionProvider) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setLocatorFactory(org.apache.jackrabbit.webdav.DavLocatorFactory)
     */
    @Override
    public void setLocatorFactory(DavLocatorFactory locatorFactory) {

        throw new UnsupportedOperationException();

    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#setResourceFactory(org.apache.jackrabbit.webdav.DavResourceFactory)
     */
    @Override
    public void setResourceFactory(DavResourceFactory resourceFactory) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getInputContext(org.apache.jackrabbit.webdav.DavServletRequest, java.io.InputStream)
     */
    @Override
    protected InputContext getInputContext(DavServletRequest request, InputStream in) {

        return new CmsDavInputContext(request, in);
    }

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#isPreconditionValid(org.apache.jackrabbit.webdav.WebdavRequest, org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    protected boolean isPreconditionValid(WebdavRequest request, DavResource resource) {

        IfHeader header = new IfHeader(request);
        String[] tokens = Iterators.toArray(header.getAllTokens(), String.class);
        for (String token : tokens) {
            if ("DAV:no-lock".equals(token)) {
                return false;
            }
        }
        return !resource.exists() || request.matchesIfHeader(resource);
    }

}
