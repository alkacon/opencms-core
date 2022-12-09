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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.repository.A_CmsRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.logging.Log;
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

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJackrabbitWebdavServlet.class);

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

    /** The repository. */
    private A_CmsRepository m_repository;

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
        m_repository = repository;
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

    /**
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        LOG.debug("WEBDAV: " + request.getMethod() + " " + request.getRequestURI());
        //printHeaderInfo(request);
        if (Boolean.parseBoolean(m_repository.getConfiguration().getString("failOnRangeHeader", "false"))) {
            // The MacOS WebDav client uses range requests and seems to assume they work even if the server sends a HTTP status of 200
            // in response (rather than 206 Partial Content). This can cause big files to be corrupted. To prevent this,
            // we send an empty response with status 400 when detecting a range request.
            String range = request.getHeader("range");
            if (range != null) {
                response.setStatus(400);
                return;
            }
        }
        try {
            super.service(request, response);
        } catch (ServletException | IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Debug method for printing header information.
     *
     * @param request the current request
     */
    private void printHeaderInfo(HttpServletRequest request) {

        System.out.println("--------------- " + request.getMethod() + " " + request.getRequestURI());
        Enumeration<String> hnames = request.getHeaderNames();
        while (hnames.hasMoreElements()) {
            String name = hnames.nextElement();
            System.out.println(name + ": " + EnumerationUtils.toList(request.getHeaders(name)));
        }
    }

}
