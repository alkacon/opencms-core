/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspVfsAccessBean.java,v $
 * Date   : $Date: 2007/08/20 12:26:00 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

/**
 * Provides utility methods that allow convenient access to the OpenCms VFS, 
 * indented to be used from a JSP with the JSTL or EL.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.2
 * 
 * @see CmsJspContentAccessBean
 */
public final class CmsJspVfsAccessBean {

    /**
     * Provides Booleans that indicate if a specified resource exists in the OpenCms VFS,  
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsExistsResourceTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return Boolean.valueOf(getReadResource().get(input) != null);
        }
    }

    /**
     * Transformer that loads all properties of a resource from the OpenCms VFS, 
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsPropertyLoaderTransformer implements Transformer {

        /** Indicates if properties should be searched when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         * 
         * @param search indicates if properties should be searched when loaded
         */
        public CmsPropertyLoaderTransformer(boolean search) {

            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Map result = null;
            // first read the resource using the lazy map 
            CmsResource resource = (CmsResource)getReadResource().get(input);
            if (resource != null) {
                try {
                    // read the properties of the requested resource
                    result = CmsProperty.toMap(getCmsObject().readPropertyObjects(resource, m_search));
                } catch (CmsException e) {
                    // unable to read resource, return empty map
                }
            }
            // result may still be null
            return (result == null) ? Collections.EMPTY_MAP : result;
        }
    }

    /**
     * Transformer that loads a resource from the OpenCms VFS, 
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsResourceLoaderTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            CmsResource result;
            try {
                // read the requested resource
                result = CmsJspElFunctions.convertResource(getCmsObject(), input);
            } catch (CmsException e) {
                // unable to read resource, return null
                result = null;
            }
            return result;
        }
    }

    /** Request context attribute for indicating the model file for a create resource operation. */
    public static final String ATTRIBUTE_JSP_UTIL_BEAN = CmsJspVfsAccessBean.class.getName() + ".JSP_UTIL_BEAN";

    /** The OpenCms context of the current user. */
    private CmsObject m_cms;

    /** Contains booleans that indicate if a resource exists in the VFS. */
    private Map m_existsResource;

    /** Properties loaded from the OpenCms VFS. */
    private Map m_properties;

    /** Properties loaded from the OpenCms VFS with search. */
    private Map m_propertiesSearch;

    /** Resources loaded from the OpenCms VFS. */
    private Map m_resources;

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     * 
     * @param cms the OpenCms context of the current user
     */
    private CmsJspVfsAccessBean(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Creates a new instance of the JSP VFS access utility bean.<p>
     * 
     * To prevent multiple creations of the bean during a request, the OpenCms request context 
     * attributes are used to cache the created VFS access utility bean.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @return a new instance of the JSP VFS access utility bean
     */
    public static CmsJspVfsAccessBean create(CmsObject cms) {

        CmsJspVfsAccessBean result;
        Object attribute = cms.getRequestContext().getAttribute(ATTRIBUTE_JSP_UTIL_BEAN);
        if (attribute != null) {
            result = (CmsJspVfsAccessBean)attribute;
        } else {
            result = new CmsJspVfsAccessBean(cms);
            cms.getRequestContext().setAttribute(ATTRIBUTE_JSP_UTIL_BEAN, result);
        }
        return result;
    }

    /**
     * Returns the OpenCms user context this bean was initialized with.<p>
     * 
     * @return the OpenCms user context this bean was initialized with
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns a map the lazily checks if a resources exists in the OpenCms VFS.<p>
     * 
     * Usage example on a JSP with the EL / JSTL:<pre>
     * &lt;c:if test="${cms:util(pageContext).existsResource['/checkme.html']}" &gt;
     *     The resource "/checkme.html" exists.
     * &lt;/c:if&gt;
     * </pre>
     * 
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.util.existsResource['/checkme.html']}" &gt;
     *         The resource "/checkme.html" exists.
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return a map the lazily reads resources from the OpenCms VFS
     */
    public Map getExistsResource() {

        if (m_existsResource == null) {
            // create lazy map only on demand
            m_existsResource = LazyMap.decorate(new HashMap(), new CmsExistsResourceTransformer());
        }
        return m_existsResource;
    }

    /**
     * Flushes the internal caches of this VFS access bean.<p>
     * 
     * The VFS access bean uses lazy initialized Maps for all access, but once a value has been 
     * read it is cached in the Map and not read again from the VFS. This means the lazy Maps 
     * act as another layer of cache to the VFS.<p>
     * 
     * The VFS access bean instance itself is cached in the OpenCms request context attributes of the {@link CmsObject}, 
     * see {@link #create(CmsObject)}. Normally there is a new {@link CmsObject} created for 
     * all incoming requests, so the live-time of the VFS access bean is short. 
     * In that case the caching of the lazy Maps should improve performance and not be an issue.
     * However, in rare cases an instance of a {@link CmsObject} may be kept for a long time in 
     * some custom code. In theses cases flushing the caches of the lazy Maps manually may be required, otherwise 
     * the Map caches may be out of sync with the VFS.
     * 
     * @return always returns <code>true</code>
     */
    public boolean getFlushCaches() {

        m_resources = null;
        m_properties = null;
        m_propertiesSearch = null;

        return true;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, without search.<p>
     * 
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource: ${cms:util(pageContext).readProperties['/index.html']['Title']}
     * </pre>
     * 
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource: ${content.util.readProperties['/index.html']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, without search
     */
    public Map getReadProperties() {

        if (m_properties == null) {
            // create lazy map only on demand
            m_properties = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(false));
        }
        return m_properties;
    }

    /**
     * Returns a map the lazily reads all resource properties from the OpenCms VFS, with search.<p>
     * 
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource (searched): ${cms:util(pageContext).readPropertiesSearch['/index.html']['Title']}
     * </pre>
     * 
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource (searched): ${content.util.readPropertiesSearch['/index.html']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return a map the lazily reads all resource properties from the OpenCms VFS, with search
     */
    public Map getReadPropertiesSearch() {

        if (m_propertiesSearch == null) {
            // create lazy map only on demand
            m_propertiesSearch = LazyMap.decorate(new HashMap(), new CmsPropertyLoaderTransformer(true));
        }
        return m_propertiesSearch;
    }

    /**
     * Returns a map the lazily reads resources from the OpenCms VFS.<p>
     * 
     * Usage example on a JSP with the EL:<pre>
     * Root path of the "/index.html" resource: ${cms:util(pageContext).readResource['/index.html'].rootPath}
     * </pre>
     * 
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Root path of the "/index.html" resource: ${content.util.readResource['/index.html'].rootPath}
     * &lt;/cms:contentload&gt;</pre>
     * 
     * @return a map the lazily reads resources from the OpenCms VFS
     */
    public Map getReadResource() {

        if (m_resources == null) {
            // create lazy map only on demand
            m_resources = LazyMap.decorate(new HashMap(), new CmsResourceLoaderTransformer());
        }
        return m_resources;
    }
}