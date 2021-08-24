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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.xml2json.CmsJsonAccessPolicy;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides context information to JSON handlers.<p>
 *
 * Also lazily loads the resource or content to be rendered as JSON.
 */
public class CmsJsonHandlerContext {

    /**
     * Cache key for JSON handler contexts.
     *
     * <p>Contains a weak reference to the context, for use in LoadingCache.
     *
     */
    public static class Key {

        /** The reference to the context from which this key was created. */
        WeakReference<CmsJsonHandlerContext> m_contextRef;

        /** The key string. */
        private String m_keyString;

        /**
         * Creates a new cache key for a context.
         *
         * @param context the context
         */
        public Key(CmsJsonHandlerContext context) {

            StringBuilder buffer = new StringBuilder();
            buffer.append(context.getCms().getRequestContext().getCurrentProject().getId());
            buffer.append("|");
            buffer.append(context.getCms().getRequestContext().getSiteRoot());
            buffer.append("|");
            buffer.append(context.getCms().getRequestContext().getCurrentUser().getId());
            buffer.append("|");
            buffer.append(context.getPath());
            buffer.append("|");
            buffer.append(context.getParameters());
            m_keyString = buffer.toString();
            m_contextRef = new WeakReference<>(context);
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            return (obj instanceof Key) && ((Key)obj).m_keyString.equals(m_keyString);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return m_keyString.hashCode();
        }

        /**
         * Gets the original context, if it hasn't been garbage collected yet.
         *
         * @return the original context or null if it has already been collected
         */
        CmsJsonHandlerContext getContext() {

            return m_contextRef.get();
        }
    }

    /** The logger instance for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsJsonHandlerContext.class);

    /** Gets the access policy. */
    private CmsJsonAccessPolicy m_accessPolicy;

    /** The CMS context with the original site root. */
    private CmsObject m_cms;

    /** The XML content (initially null). */
    private CmsXmlContent m_content;

    /** The file for the path (initially null). */
    private CmsFile m_file;

    /** The handler parameters from opencms-system.xml. */
    private CmsParameterConfiguration m_handlerConfig;

    /** The request parameters from the resource init handler call. */
    private Map<String, String> m_parameters;

    /** The path below the JSON handler prefix. */
    private String m_path;

    /** The resource for the path. */
    private CmsResource m_resource;

    /** The CMS context initialized with the root site. */
    private CmsObject m_rootCms;

    /** Temporary data storage to be used by individual handlers. */
    private Map<String, Object> m_tempData = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param rootCms the CMS context initialized for the root site
     * @param path the path below the JSON handler
     * @param resource the resource (may be null)
     * @param params the request parameters
     * @param handlerConfig the handler parameters from opencms-system.xml
     * @param policy the access policy
     */
    public CmsJsonHandlerContext(
        CmsObject cms,
        CmsObject rootCms,
        String path,
        CmsResource resource,
        Map<String, String> params,
        CmsParameterConfiguration handlerConfig,
        CmsJsonAccessPolicy policy) {

        m_cms = cms;
        m_resource = resource;
        m_rootCms = rootCms;
        m_path = path;
        m_parameters = params;
        m_handlerConfig = handlerConfig;
        if (m_parameters == null) {
            m_parameters = Collections.emptyMap();
        }
        if (m_handlerConfig == null) {
            m_handlerConfig = new CmsParameterConfiguration();
        }
        m_accessPolicy = policy;
    }

    /**
     * Gets the access policy.
     *
     * @return the access policy
     */
    public CmsJsonAccessPolicy getAccessPolicy() {

        return m_accessPolicy;
    }

    /**
     * Gets the CMS context.
     *
     * @return the CMS context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Loads the XML content.<p>
     *
     * This only works if the resource for the given path is actually an  XML content.
     *
     * @return the XML content for the path
     *
     * @throws CmsException if something goes wrong
     */
    public CmsXmlContent getContent() throws CmsException {

        if (m_content == null) {
            m_content = CmsXmlContentFactory.unmarshal(m_cms, getFile());
        }
        return m_content;
    }

    /**
     * Gets the file for the path.
     *
     * This will only work if the resource for the path is actually a file.
     *
     * @return the file for the path
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile getFile() throws CmsException {

        if (m_file == null) {
            m_file = m_cms.readFile(getResource());
        }
        return m_file;
    }

    /**
     * Gets the configured handler parameters.
     *
     * @return the configured handler parameters
     */
    public CmsParameterConfiguration getHandlerConfig() {

        return m_handlerConfig;
    }

    /**
     * Creates a cache key for this context.
     *
     * @return the cache key
     */
    public Key getKey() {

        return new Key(this);
    }

    /**
     * Gets the request parameters.
     *
     * @return the request parameters
     */
    public Map<String, String> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * Returns the path.
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Gets the resource (may be null if there is no resource).
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Gets the CMS context initialized to the root site.
     *
     * @return the CMS context initialized to the root site
     */
    public CmsObject getRootCms() {

        return m_rootCms;
    }

    /**
     * Gets the temporary storage to be used by individual handlers.
     *
     * @return the temporary storage
     */
    public Map<String, Object> getTempStorage() {

        return m_tempData;
    }

}
