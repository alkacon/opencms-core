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

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.jsp.util.CmsJspStandardContextBean.CmsContainerElementWrapper;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.loader.CmsTemplateContextManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;

/**
 * Describes the caching behaviour (or caching options) for a Flex request.<p>
 *
 * @since 6.0.0
 */
public class CmsFlexRequestKey {

    /**
     * Contains the root paths to be used for determining the buckets of a flex cache entry.<p>
     */
    public static class PathsBean {

        /** The container element path. */
        private String m_containerElement;

        /** The detail element path. */
        private String m_detailElement;

        /** The site root. */
        private String m_site;

        /** The URI. */
        private String m_uri;

        /**
         * Returns the container element.<p>
         *
         * @return the container element
         */
        public String getContainerElement() {

            return m_containerElement;
        }

        /**
         * Returns the detail element.<p>
         *
         * @return the detail element
         */
        public String getDetailElement() {

            return m_detailElement;
        }

        /**
         * Returns the site.<p>
         *
         * @return the site
         */
        public String getSite() {

            return m_site;
        }

        /**
         * Returns the uri.<p>
         *
         * @return the uri
         */
        public String getUri() {

            return m_uri;
        }

        /**
         * Sets the container element.<p>
         *
         * @param containerElement the container element to set
         */
        public void setContainerElement(String containerElement) {

            m_containerElement = containerElement;
        }

        /**
         * Sets the detail element.<p>
         *
         * @param detailElement the detail element to set
         */
        public void setDetailElement(String detailElement) {

            m_detailElement = detailElement;
        }

        /**
         * Sets the site.<p>
         *
         * @param site the site to set
         */
        public void setSite(String site) {

            m_site = site;
        }

        /**
         * Sets the uri.<p>
         *
         * @param uri the uri to set
         */
        public void setUri(String uri) {

            m_uri = uri;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexRequestKey.class);

    /** The current container element. */
    private String m_containerElement;

    /** The request context this request was made in. */
    private CmsRequestContext m_context;

    /** The current detail view id. */
    private CmsUUID m_detailViewId;

    /** Stores the device this request was made with. */
    private String m_device;

    /** The bean storing the paths which should be used to determine the flex cache buckets for this entry. */
    private PathsBean m_paths = new PathsBean();

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
     * @param req the request to construct the key for
     * @param target the requested resource in the OpenCms VFS
     * @param online must be true for an online resource, false for offline resources
     */
    public CmsFlexRequestKey(HttpServletRequest req, String target, boolean online) {

        // store the request
        m_request = req;

        // fetch the cms from the request
        CmsObject cms = CmsFlexController.getCmsObject(req);

        // store the request context
        m_context = cms.getRequestContext();

        // calculate the resource name
        m_resource = CmsFlexCacheKey.getKeyName(m_context.addSiteRoot(target), online);

        // calculate the device
        m_device = OpenCms.getSystemInfo().getDeviceSelector().getDeviceType(req);

        CmsJspStandardContextBean standardContext = CmsJspStandardContextBean.getInstance(req);
        // get the current container element
        String templateContextKey = "";
        TemplateBean templateBean = (TemplateBean)(req.getAttribute(CmsTemplateContextManager.ATTR_TEMPLATE_BEAN));
        if (templateBean != null) {
            templateContextKey = templateBean.getName();
        }
        m_containerElement = standardContext.elementCachingHash() + "_tc_" + templateContextKey;

        // get the current detail view id
        m_detailViewId = standardContext.getDetailContentId();
        CmsResource detailContent = standardContext.getDetailContent();
        if (detailContent != null) {
            m_paths.setDetailElement(detailContent.getRootPath());
        }
        m_paths.setSite(m_context.getSiteRoot());
        CmsContainerElementWrapper wrapper = standardContext.getElement();
        if (wrapper != null) {
            CmsResource containerElementResource = wrapper.getResource();
            if (containerElementResource != null) {
                m_paths.setContainerElement(containerElementResource.getRootPath());
            }
        }
        m_paths.setUri(m_context.addSiteRoot(m_context.getUri()));
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXREQUESTKEY_CREATED_NEW_KEY_1, m_resource));
        }
    }

    /**
     * Returns the request attributes.<p>
     *
     * @return the request attributes
     */
    public Map<String, Object> getAttributes() {

        Map<String, Object> attrs = CmsRequestUtil.getAtrributeMap(m_request);

        if (attrs.size() == 0) {
            return null;
        }
        return attrs;
    }

    /**
     * Returns the current container element.<p>
     *
     * @return the current container element
     */
    public String getContainerElement() {

        return m_containerElement;
    }

    /**
     * Returns the device.<p>
     *
     * @return the device
     */
    public String getDevice() {

        return m_device;
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public String getElement() {

        return m_request.getParameter(I_CmsResourceLoader.PARAMETER_ELEMENT);
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
     * Returns the parameters.<p>
     *
     * @return the parameters
     */
    public Map<String, String[]> getParams() {

        // get the params
        Map<String, String[]> params = CmsCollectionsGenericWrapper.map(m_request.getParameterMap());
        if (params.size() == 0) {
            return null;
        }
        return params;
    }

    /**
     * Gets the bean containing the paths which should be used to determine the flex cache buckets for this entry.<p>
     *
     * @return the bean with the paths for this entry
     */
    public PathsBean getPaths() {

        return m_paths;
    }

    /**
     * Returns the port.<p>
     *
     * @return the port
     */
    public Integer getPort() {

        return Integer.valueOf(m_request.getServerPort());
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
     * Returns the site root.<p>
     *
     * @return the site root
     */
    public String getSite() {

        return m_context.getSiteRoot();
    }

    /**
     * Returns the uri.<p>
     *
     * @return the uri
     */
    public String getUri() {

        String uri = m_context.addSiteRoot(m_context.getUri());
        if (m_detailViewId != null) {
            uri += m_detailViewId.toString() + "/";
        }
        return uri;
    }

    /**
     * Returns the user.<p>
     *
     * @return the user
     */
    public String getUser() {

        return m_context.getCurrentUser().getName();
    }

    /**
     * Returns true if the 'force absolute links' flag is set in the request context.
     *
     * @return the 'force absolute links' flag from the request context
     */
    public boolean isForceAbsoluteLinks() {

        return m_context.isForceAbsoluteLinks();
    }
}