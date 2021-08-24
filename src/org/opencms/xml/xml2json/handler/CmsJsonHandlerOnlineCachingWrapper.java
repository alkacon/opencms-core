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

package org.opencms.xml.xml2json.handler;

import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.xml.xml2json.CmsJsonResult;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Wrapper for JSON handlers that caches online project requests.
 */
public class CmsJsonHandlerOnlineCachingWrapper implements I_CmsJsonHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonHandlerOnlineCachingWrapper.class);

    /** The wrapped handler. */
    private I_CmsJsonHandler m_handler;

    /** The cache. */
    private LoadingCache<CmsJsonHandlerContext.Key, CmsJsonResult> m_cache;

    /**
     * Creates a new instance.
     *
     * @param handler the handler to wrap
     * @param spec the CacheBuilder specification to use for the cache
     */
    public CmsJsonHandlerOnlineCachingWrapper(I_CmsJsonHandler handler, String spec) {

        m_handler = handler;
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.from(spec);
        m_cache = cacheBuilder.build(new CacheLoader<CmsJsonHandlerContext.Key, CmsJsonResult>() {

            @Override
            @SuppressWarnings("synthetic-access")
            public CmsJsonResult load(CmsJsonHandlerContext.Key key) throws Exception {

                CmsJsonResult result = m_handler.renderJson(key.getContext());
                return result;

            }
        });
        OpenCms.addCmsEventListener(
            evt -> m_cache.invalidateAll(),
            new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES, I_CmsEventListener.EVENT_PUBLISH_PROJECT});

    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#getOrder()
     */
    public double getOrder() {

        return m_handler.getOrder();
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#matches(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public boolean matches(CmsJsonHandlerContext context) {

        return m_handler.matches(context);
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) {

        if (!context.getCms().getRequestContext().getCurrentProject().isOnlineProject()) {
            // never cache offline requests
            return m_handler.renderJson(context);
        } else {
            try {
                return m_cache.get(context.getKey());
            } catch (ExecutionException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_handler.toString();
    }

}
