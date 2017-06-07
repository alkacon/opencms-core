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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleGroup;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.jsp.util.CmsJspValueTransformers.CmsLocalePropertyLoaderTransformer;
import org.opencms.jsp.util.CmsJspValueTransformers.CmsPropertyLoaderTransformer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;

/**
 * Provides utility methods that allow convenient access to the OpenCms VFS,
 * indented to be used from a JSP with the JSTL or EL.<p>
 *
 * @since 7.0.2
 *
 * @see CmsJspContentAccessBean
 */
public final class CmsJspVfsAccessBean {

    /**
     * Transformer that loads a resource from the OpenCms VFS,
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsAvailableLocaleLoaderTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            List<Locale> result;
            // read the available locales
            result = OpenCms.getLocaleManager().getAvailableLocales(getCmsObject(), String.valueOf(input));
            return result;
        }
    }

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
     * Provides Booleans that indicate if a specified resource exists in the OpenCms VFS
     * and is of type XML content or XML page,
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsExistsXmlTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            // first read the resource using the lazy map
            CmsResource resource = getReadResource().get(input);
            return Boolean.valueOf(
                (resource != null)
                    && (CmsResourceTypeXmlPage.isXmlPage(resource)
                        || CmsResourceTypeXmlContent.isXmlContent(resource)));
        }
    }

    /**
     * Transformer that loads a resource permission from the OpenCms VFS,
     * the input is used as String for the resource name to read the permissions for.<p>
     */
    public class CmsPermissionsLoaderTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            CmsPermissionSet result;
            try {
                // read the requested resource permissions
                result = getCmsObject().getPermissions((String)input);
            } catch (CmsException e) {
                // unable to read resource, return null
                result = null;
            }
            return result;
        }
    }

    /**
     * Transformer that a properties of a resource from the OpenCms VFS,
     * the input is used as String for the property name to read.<p>
     */
    public class CmsPropertyLoaderSingleTransformer implements Transformer {

        /** The resource where the properties are read from. */
        private CmsResource m_resource;

        /** Indicates if properties should be searched when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param resource the resource where the properties are read from
         * @param search indicates if properties should be searched when loaded
         */
        public CmsPropertyLoaderSingleTransformer(CmsResource resource, boolean search) {

            m_resource = resource;
            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String result;
            try {
                // read the properties of the requested resource
                result = getCmsObject().readPropertyObject(m_resource, String.valueOf(input), m_search).getValue();
            } catch (CmsException e) {
                // in case of any error we assume the property does not exist
                result = null;
            }
            return result;
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

    /**
     * Transformer that loads properties of a resource from the OpenCms VFS with another lazy map,
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsResourceLocalePropertyLoaderTransformer implements Transformer {

        /** Indicates if properties should be searched when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param search indicates if properties should be searched when loaded
         */
        public CmsResourceLocalePropertyLoaderTransformer(boolean search) {

            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Map<String, String> result = null;
            // first read the resource using the lazy map
            CmsResource resource = getReadResource().get(input);
            if (resource != null) {
                result = CmsCollectionsGenericWrapper.createLazyMap(
                    new CmsLocalePropertyLoaderTransformer(getCmsObject(), resource, m_search));
            }
            // result may still be null
            return (result == null) ? Collections.EMPTY_MAP : result;
        }
    }

    /**
     * Transformer that loads properties of a resource from the OpenCms VFS with another lazy map,
     * the input is used as String for the resource name to read.<p>
     */
    public class CmsResourcePropertyLoaderTransformer implements Transformer {

        /** Indicates if properties should be searched when loaded. */
        private boolean m_search;

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param search indicates if properties should be searched when loaded
         */
        public CmsResourcePropertyLoaderTransformer(boolean search) {

            m_search = search;
        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            Map<String, String> result = null;
            // first read the resource using the lazy map
            CmsResource resource = getReadResource().get(input);
            if (resource != null) {
                result = CmsCollectionsGenericWrapper.createLazyMap(
                    new CmsPropertyLoaderTransformer(getCmsObject(), resource, m_search));
            }
            // result may still be null
            return (result == null) ? Collections.EMPTY_MAP : result;
        }
    }

    /**
     * Transformer that calculates links to resources in the OpenCms VFS,
     * the input is used as String for the resource name to use as link target.<p>
     *
     * This is using the same logic as
     * {@link org.opencms.jsp.CmsJspTagLink#linkTagAction(String, javax.servlet.ServletRequest)}.<p>
     */
    public class CmsVfsLinkTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            return OpenCms.getLinkManager().substituteLink(
                getCmsObject(),
                CmsLinkManager.getAbsoluteUri(String.valueOf(input), getCmsObject().getRequestContext().getUri()));
        }
    }

    /**
     * Provides XML content access beans for VFS resources.<p>
     */
    public class CmsXmlContentAccessTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            CmsJspContentAccessBean result = null;
            // first read the resource using the lazy map
            CmsResource resource = getReadResource().get(input);
            if ((resource != null)
                && (CmsResourceTypeXmlPage.isXmlPage(resource) || CmsResourceTypeXmlContent.isXmlContent(resource))) {
                // make sure we have a resource that really is an XML content
                result = new CmsJspContentAccessBean(getCmsObject(), resource);
            }
            return result;
        }
    }

    /** Request context attribute for indicating the model file for a create resource operation. */
    public static final String ATTRIBUTE_VFS_ACCESS_BEAN = CmsJspVfsAccessBean.class.getName() + ".VFS_ACCESS_BEAN";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspVfsAccessBean.class);

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** Contains booleans that indicate if a resource exists in the VFS. */
    private Map<String, Boolean> m_existsResource;

    /** Contains booleans that indicate if a resource exists and is an XML content. */
    private Map<String, Boolean> m_existsXml;

    /** Links calculated for the OpenCms VFS. */
    private Map<String, String> m_links;

    /** Resource permissions loaded from the OpenCms VFS. */
    private Map<String, CmsPermissionSet> m_permissions;

    /** Properties loaded from the OpenCms VFS. */
    private Map<String, Map<String, String>> m_properties;

    /** Properties loaded locale specific from the OpenCms VFS. */
    private Map<String, Map<String, Map<String, String>>> m_propertiesLocale;

    /** Properties loaded from the OpenCms VFS with search. */
    private Map<String, Map<String, String>> m_propertiesSearch;

    /** Properties loaded locale specific from the OpenCms VFS with search. */
    private Map<String, Map<String, Map<String, String>>> m_propertiesSearchLocale;

    /** Available locales as determined by the {@link org.opencms.i18n.CmsLocaleManager} */
    private Map<String, List<Locale>> m_availableLocales;

    /** Resources loaded from the OpenCms VFS. */
    private Map<String, CmsResource> m_resources;

    /** XML contents read from the VFS. */
    private Map<String, CmsJspContentAccessBean> m_xmlContent;

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
        Object attribute = cms.getRequestContext().getAttribute(ATTRIBUTE_VFS_ACCESS_BEAN);
        if (attribute != null) {
            result = (CmsJspVfsAccessBean)attribute;
        } else {
            result = new CmsJspVfsAccessBean(cms);
            cms.getRequestContext().setAttribute(ATTRIBUTE_VFS_ACCESS_BEAN, result);
        }
        return result;
    }

    /**
     * Returns a lazily generated map from site paths of resources to the available locales for the resource.
     *
     * @return a lazily generated map from site paths of resources to the available locales for the resource.
     */
    public Map<String, List<Locale>> getAvailableLocales() {

        if (m_availableLocales == null) {
            // create lazy map only on demand
            m_availableLocales = CmsCollectionsGenericWrapper.createLazyMap(new CmsAvailableLocaleLoaderTransformer());
        }
        return m_availableLocales;
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
     * Short form for {@link #getRequestContext()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * The current URI is: ${cms:vfs(pageContext).context.uri}
     * </pre>
     *
     * @return the OpenCms request context of the current user this bean was initialized with
     *
     * @see #getRequestContext()
     */
    public CmsRequestContext getContext() {

        return getRequestContext();
    }

    /**
     * Returns the current project from the context.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * The current project name is: ${cms:vfs(pageContext).currentProject.name}
     * </pre>
     *
     * @return the current project
     */
    public CmsProject getCurrentProject() {

        return m_cms.getRequestContext().getCurrentProject();
    }

    /**
     * Returns the current user from the context.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * The current user name is: ${cms:vfs(pageContext).currentUser.name}
     * </pre>
     *
     * @return the current user
     */
    public CmsUser getCurrentUser() {

        return m_cms.getRequestContext().getCurrentUser();
    }

    /**
     * Short form for {@link #getExistsResource()}.<p>
     *
     * Usage example on a JSP with the EL / JSTL:<pre>
     * &lt;c:if test="${cms:vfs(pageContext).exists['/checkme.html']}" &gt;
     *     The resource "/checkme.html" exists.
     * &lt;/c:if&gt;
     * </pre>
     *
     * @return a map that lazily reads resources from the OpenCms VFS
     *
     * @see #getExistsResource()
     */
    public Map<String, Boolean> getExists() {

        return getExistsResource();
    }

    /**
     * Returns a map that lazily checks if a resources exists in the OpenCms VFS.<p>
     *
     * Usage example on a JSP with the EL / JSTL:<pre>
     * &lt;c:if test="${cms:vfs(pageContext).existsResource['/checkme.html']}" &gt;
     *     The resource "/checkme.html" exists.
     * &lt;/c:if&gt;
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     &lt;c:if test="${content.vfs.existsResource['/checkme.html']}" &gt;
     *         The resource "/checkme.html" exists.
     *     &lt;/c:if&gt;
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily checks if a resources exists in the OpenCms VFS
     *
     * @see #getExists() for a short form of this method
     */
    public Map<String, Boolean> getExistsResource() {

        if (m_existsResource == null) {
            // create lazy map only on demand
            m_existsResource = CmsCollectionsGenericWrapper.createLazyMap(new CmsExistsResourceTransformer());
        }
        return m_existsResource;
    }

    /**
     * Returns a map that lazily checks if a resources exists in the VFS and is of type XML content or XML page.<p>
     *
     * Usage example on a JSP with the EL / JSTL:<pre>
     * &lt;c:if test="${cms:vfs(pageContext).existsXml['/text.xml']}" &gt;
     *     The resource "/text.xml" exists and is an XML document.
     * &lt;/c:if&gt;
     * </pre>
     *
     * @return a map that lazily checks if a resources exists in the VFS and is of type XML content or XML page
     */
    public Map<String, Boolean> getExistsXml() {

        if (m_existsXml == null) {
            // create lazy map only on demand
            m_existsXml = CmsCollectionsGenericWrapper.createLazyMap(new CmsExistsXmlTransformer());
        }
        return m_existsXml;
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
     * Returns a map that lazily calculates links to files in the OpenCms VFS,
     * which have been adjusted according to the web application path and the
     * OpenCms static export rules.<p>
     *
     * Please note that the target is always assumed to be in the OpenCms VFS, so you can't use
     * this method for links external to OpenCms.<p>
     *
     * Relative links are converted to absolute links, using the current element URI as base.<p>
     *
     * Relative links are converted to absolute links, using the current OpenCms request context URI as base.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Link to the "/index.html" file: ${cms:vfs(pageContext).link['/index.html']}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Link to the "/index.html" file: ${content.vfs.link['/index.html']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily calculates links to resources in the OpenCms VFS
     *
     * @see org.opencms.jsp.CmsJspActionElement#link(String)
     * @see org.opencms.jsp.CmsJspTagLink#linkTagAction(String, javax.servlet.ServletRequest)
     */
    public Map<String, String> getLink() {

        if (m_links == null) {
            // create lazy map only on demand
            m_links = CmsCollectionsGenericWrapper.createLazyMap(new CmsVfsLinkTransformer());
        }
        return m_links;
    }

    /**
     * Gets a lazy loading map used to access locale variants of a resource with a given path.<p>
     *
     * Usage in JSP: ${myvfsaccessbeaninstance.localeResource['/foo/bar/index.html']['de']}
     *
     * @return the lazy loading map
     */
    public Map<String, Map<String, CmsJspResourceWrapper>> getLocaleResource() {

        return CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

            @SuppressWarnings("synthetic-access")
            public Object transform(Object arg) {

                if (!(arg instanceof String)) {
                    return new HashMap<String, CmsJspResourceWrapper>();
                }
                String path = (String)arg;
                try {
                    CmsResource res = m_cms.readResource(path);
                    CmsJspResourceWrapper wrapper = new CmsJspResourceWrapper(m_cms, res);
                    return wrapper.getLocaleResource();
                } catch (Exception e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return new HashMap<String, CmsJspResourceWrapper>();
                }
            }

        });
    }

    /**
     * Returns a lazy loading map used to detect the main locale of a resource which is part of a locale group.<p>
     *
     * Usage in JSPs: ${myvfsaccessbeaninstance.mainLocale['/foo/index.html']}
     *
     * @return the lazy loading map
     */
    public Map<String, Locale> getMainLocale() {

        return CmsCollectionsGenericWrapper.createLazyMap(new Transformer() {

            @SuppressWarnings("synthetic-access")
            public Object transform(Object arg) {

                if (!(arg instanceof String)) {
                    return null;
                }
                String path = (String)arg;
                try {
                    CmsResource res = m_cms.readResource(path);
                    CmsLocaleGroup localeGroup = m_cms.getLocaleGroupService().readLocaleGroup(res);
                    return localeGroup.getMainLocale();
                } catch (Exception e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                    return null;
                }
            }
        });
    }

    /**
     * Short form for {@link #getReadPermissions()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Permission string of the "/index.html" resource: ${cms:vfs(pageContext).readPermissions['/index.html'].permissionString}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Permission string of the "/index.html" resource: ${content.vfs.readPermissions['/index.html'].permissionString}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads resource permissions from the OpenCms VFS
     *
     * @see #getReadPermissions()
     */
    public Map<String, CmsPermissionSet> getPermissions() {

        return getReadPermissions();
    }

    /**
     * Short form for {@link #getReadProperties()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource: ${cms:vfs(pageContext).property['/index.html']['Title']}
     * </pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, without search
     *
     * @see #getReadProperties()
     */
    public Map<String, Map<String, String>> getProperty() {

        return getReadProperties();
    }

    /**
     * Short form for {@link #getReadPropertiesLocale()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource for locale "de": ${cms:vfs(pageContext).property['/index.html']['de']['Title']}
     * </pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, without search
     *
     * @see #getReadPropertiesLocale()
     */
    public Map<String, Map<String, Map<String, String>>> getPropertyLocale() {

        return getReadPropertiesLocale();
    }

    /**
     * Short form for {@link #getReadPropertiesSearch()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource (searched): ${cms:vfs(pageContext).propertySearch['/index.html']['Title']}
     * </pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, with search
     *
     * @see #getReadPropertiesSearch()
     */
    public Map<String, Map<String, String>> getPropertySearch() {

        return getReadPropertiesSearch();
    }

    /**
     * Short form for {@link #getReadPropertiesSearchLocale()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource (searched) for locale "de": ${cms:vfs(pageContext).propertySearch['/index.html']['de']['Title']}
     * </pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, with search
     *
     * @see #getReadPropertiesSearchLocale()
     */
    public Map<String, Map<String, Map<String, String>>> getPropertySearchLocale() {

        return getReadPropertiesSearchLocale();
    }

    /**
     * Returns a map that lazily reads resource permissions from the OpenCms VFS.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Permission string of the "/index.html" resource: ${cms:vfs(pageContext).readPermissions['/index.html'].permissionString}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Permission string of the "/index.html" resource: ${content.vfs.readPermissions['/index.html'].permissionString}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads resource permissions from the OpenCms VFS
     *
     * @see #getPermissions() for a short form of this method
     */
    public Map<String, CmsPermissionSet> getReadPermissions() {

        if (m_permissions == null) {
            // create lazy map only on demand
            m_permissions = CmsCollectionsGenericWrapper.createLazyMap(new CmsPermissionsLoaderTransformer());
        }
        return m_permissions;
    }

    /**
     * Returns a map that lazily reads all resource properties from the OpenCms VFS, without search.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource: ${cms:vfs(pageContext).readProperties['/index.html']['Title']}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource: ${content.vfs.readProperties['/index.html']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, without search
     *
     * @see #getProperty() for a short form of this method
     */
    public Map<String, Map<String, String>> getReadProperties() {

        if (m_properties == null) {
            // create lazy map only on demand
            m_properties = CmsCollectionsGenericWrapper.createLazyMap(new CmsResourcePropertyLoaderTransformer(false));
        }
        return m_properties;
    }

    /**
     * Returns a map that lazily reads all resource properties from the OpenCms VFS, without search.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource for locale "de": ${cms:vfs(pageContext).readProperties['/index.html']['de']['Title']}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource: ${content.vfs.readProperties['/index.html']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, without search
     *
     * @see #getProperty() for a short form of this method
     */
    public Map<String, Map<String, Map<String, String>>> getReadPropertiesLocale() {

        if (m_propertiesLocale == null) {
            // create lazy map only on demand
            m_propertiesLocale = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsResourceLocalePropertyLoaderTransformer(false));
        }
        return m_propertiesLocale;
    }

    /**
     * Returns a map that lazily reads all resource properties from the OpenCms VFS, with search.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource (searched): ${cms:vfs(pageContext).readPropertiesSearch['/index.html']['Title']}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource (searched): ${content.vfs.readPropertiesSearch['/index.html']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, with search
     *
     * @see #getPropertySearch() for a short form of this method
     */
    public Map<String, Map<String, String>> getReadPropertiesSearch() {

        if (m_propertiesSearch == null) {
            // create lazy map only on demand
            m_propertiesSearch = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsResourcePropertyLoaderTransformer(true));
        }
        return m_propertiesSearch;
    }

    /**
     * Returns a map that lazily reads all resource properties from the OpenCms VFS, with search and provides locale specific access to them.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title property of the "/index.html" resource (searched): ${cms:vfs(pageContext).readPropertiesSearch['/index.html']['Title']}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Title property of the "/index.html" resource (searched) for locale "de": ${content.vfs.readPropertiesSearchLocale['/index.html']['de']['Title']}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads all resource properties from the OpenCms VFS, with search
     *
     * @see #getPropertySearch() for a short form of this method
     */
    public Map<String, Map<String, Map<String, String>>> getReadPropertiesSearchLocale() {

        if (m_propertiesSearchLocale == null) {
            // create lazy map only on demand
            m_propertiesSearchLocale = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsResourceLocalePropertyLoaderTransformer(true));
        }
        return m_propertiesSearchLocale;
    }

    /**
     * Returns a map that lazily reads resources from the OpenCms VFS.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Root path of the "/index.html" resource: ${cms:vfs(pageContext).readResource['/index.html'].rootPath}
     * </pre>
     *
     * Usage example on a JSP with the <code>&lt;cms:contentaccess&gt;</code> tag:<pre>
     * &lt;cms:contentload ... &gt;
     *     &lt;cms:contentaccess var="content" /&gt;
     *     Root path of the "/index.html" resource: ${content.vfs.readResource['/index.html'].rootPath}
     * &lt;/cms:contentload&gt;</pre>
     *
     * @return a map that lazily reads resources from the OpenCms VFS
     *
     * @see #getResource() for a short form of this method
     */
    public Map<String, CmsResource> getReadResource() {

        if (m_resources == null) {
            // create lazy map only on demand
            m_resources = CmsCollectionsGenericWrapper.createLazyMap(new CmsResourceLoaderTransformer());
        }
        return m_resources;
    }

    /**
     * Returns a map that lazily reads XML documents from the OpenCms VFS that are wrapped using a
     * {@link CmsJspContentAccessBean}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title of "/text.xml": ${cms:vfs(pageContext).readXml['/text.xml'].value['Title']}
     * </pre>
     *
     * @return a map that lazily reads wrapped XML documents from the OpenCms VFS
     *
     * @see #getXml() for a short form of this method
     */
    public Map<String, CmsJspContentAccessBean> getReadXml() {

        if (m_xmlContent == null) {
            // create lazy map only on demand
            m_xmlContent = CmsCollectionsGenericWrapper.createLazyMap(new CmsXmlContentAccessTransformer());
        }
        return m_xmlContent;
    }

    /**
     * Returns the OpenCms request context the current user this bean was initialized with.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * The current URI is: ${cms:vfs(pageContext).requestContext.uri}
     * </pre>
     *
     * @return the OpenCms request context the current user this bean was initialized with
     *
     * @see #getContext() for a short form of this method
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
    }

    /**
     * Short form for {@link #getReadResource()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Root path of the "/index.html" resource: ${cms:vfs(pageContext).resource['/index.html'].rootPath}
     * </pre>
     *
     * @return a map that lazily reads resources from the OpenCms VFS
     *
     * @see #getReadResource()
     */
    public Map<String, CmsResource> getResource() {

        return getReadResource();
    }

    /**
     * Short form for {@link #getReadXml()}.<p>
     *
     * Usage example on a JSP with the EL:<pre>
     * Title of "/text.xml": ${cms:vfs(pageContext).xml['/text.xml'].value['Title']}
     * </pre>
     *
     * @return a map that lazily reads wrapped XML documents from the OpenCms VFS
     *
     * @see #getReadXml()
     */
    public Map<String, CmsJspContentAccessBean> getXml() {

        return getReadXml();
    }
}