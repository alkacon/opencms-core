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
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspTagNavigation;
import org.opencms.main.CmsException;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * Allows access to the OpenCms navigation information in combination with the
 * <code>&lt;cms:navigation&gt;</code> tag.<p>
 *
 * @since 8.0
 *
 * @see org.opencms.jsp.CmsJspTagContentAccess
 */
public class CmsJspNavigationBean {

    /**
     * Provides a Map with Booleans that
     * indicate if the given URI is the currently active element in the navigation.<p>
     */
    public class CmsIsActiveTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String resourceName = (String)input;
            Boolean result = Boolean.FALSE;
            if (CmsResource.isFolder(resourceName)) {
                try {
                    CmsResource defaultFile = m_cms.readDefaultFile(resourceName);
                    if ((defaultFile != null)
                        && m_cms.getRequestContext().getSitePath(defaultFile).equals(
                            m_cms.getRequestContext().getUri())) {
                        result = Boolean.TRUE;
                    }
                } catch (@SuppressWarnings("unused") CmsException e) {
                    // error reading resource, result is false
                }
            } else {
                result = Boolean.valueOf(m_cms.getRequestContext().getUri().equals(resourceName));
            }

            return result;
        }
    }

    /**
     * Provides a Map with Booleans that
     * indicate if the given navigation URI is a parent element of the current URI.<p>
     */
    public class CmsIsParentTransformer implements Transformer {

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String resourceName = null;
            if (input instanceof CmsJspNavElement) {
                CmsJspNavElement navElement = (CmsJspNavElement)input;
                if (navElement.getResource() != null) {
                    resourceName = m_cms.getSitePath(navElement.getResource());
                } else {
                    resourceName = navElement.getResourceName();
                }
            } else {
                resourceName = String.valueOf(input);
            }
            return Boolean.valueOf(m_cms.getRequestContext().getUri().startsWith(resourceName));
        }
    }

    /** The navigation builder. */
    protected CmsJspNavBuilder m_builder;

    /** The OpenCms user context. */
    protected CmsObject m_cms;

    /** The navigation element of the currently requested uri. */
    protected CmsJspNavElement m_current;

    /** The optional end level for the navigation. */
    protected int m_endLevel;

    /** Indicates if a given navigation uri is currently active. */
    protected Map<String, Boolean> m_isActive;

    /** Indicates if the given navigation URI is a parent element of the current URI. */
    protected Map<String, Boolean> m_isParent;

    /** The result items from the navigation. */
    protected List<CmsJspNavElement> m_items;

    /** The optional parameter for the navigation. */
    protected String m_param;

    /** The optional resource for the navigation. */
    protected String m_resource;

    /** The optional start level for the navigation. */
    protected int m_startLevel;

    /** The selected navigation type. */
    protected CmsJspTagNavigation.Type m_type;

    /**
     * Base constructor.<p>
     *
     * @param cms the current users OpenCms context to build the navigation for
     * @param type the navigation type to generate
     * @param startLevel the optional start level
     * @param endLevel the optional end level
     * @param resource the optional resource for the navigation
     * @param param the optional parameter for the navigation
     */
    public CmsJspNavigationBean(
        CmsObject cms,
        CmsJspTagNavigation.Type type,
        int startLevel,
        int endLevel,
        String resource,
        String param) {
        this(cms, type, startLevel, endLevel, resource, param, null);
    }

    /**
     * Base constructor.<p>
     *
     * @param cms the current users OpenCms context to build the navigation for
     * @param type the navigation type to generate
     * @param startLevel the optional start level
     * @param endLevel the optional end level
     * @param resource the optional resource for the navigation
     * @param param the optional parameter for the navigation
     * @param locale the locale, for which Properties should be read.
     */
    public CmsJspNavigationBean(
        CmsObject cms,
        CmsJspTagNavigation.Type type,
        int startLevel,
        int endLevel,
        String resource,
        String param,
        Locale locale) {

        m_cms = cms;
        m_builder = new CmsJspNavBuilder(m_cms, locale);
        m_type = type;
        m_startLevel = startLevel;
        m_endLevel = endLevel;
        m_resource = resource;
        m_param = param;
    }

    /**
     * Returns the navigation element of the currently requested uri.<p>
     *
     * @return the navigation element of the currently requested uri
     */
    public CmsJspNavElement getCurrent() {

        if (m_current == null) {
            m_current = m_builder.getNavigationForResource();
        }
        return m_current;
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that
     * indicate if a given navigation uri is currently active.<p>
     *
     * The provided Map key is assumed to be a String that represents an absolute VFS path.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:navigation  type="treeForFolder" startLevel="1" endLevel="3" var="nav" /&gt;
     *     &lt;c:forEach var="entry" items="${nav.items}" ... &gt;
     *     ...
     *     &lt;c:if test="${nav.isActive[entry.resourceName]}" &gt;
     *         This is the currently active navigation entry
     *     &lt;/c:if&gt;
     * &lt;/c:forEach&gt;</pre>
     *
     * @return a lazy initialized Map that provides Booleans that
     *      indicate if a given navigation uri is currently active
     */
    public Map<String, Boolean> getIsActive() {

        if (m_isActive == null) {
            m_isActive = CmsCollectionsGenericWrapper.createLazyMap(new CmsIsActiveTransformer());
        }
        return m_isActive;
    }

    /**
     * Returns a lazy initialized Map that provides Booleans that
     * indicate if the given navigation URI is a parent element of the current URI.<p>
     *
     * The provided Map key is assumed to be a String that represents an absolute VFS path.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:navigation  type="treeForFolder" startLevel="1" endLevel="3" var="nav" /&gt;
     *     &lt;c:forEach var="entry" items="${nav.items}" ... &gt;
     *     ...
     *     &lt;c:if test="${nav.isParent[entry.resourceName]}" &gt;
     *         The currently active navigation entry is a parent of the currently requested URI
     *     &lt;/c:if&gt;
     * &lt;/c:forEach&gt;</pre>
     *
     * @return a lazy initialized Map that provides Booleans that
     *      indicate if the given navigation URI is a parent element of the current URI
     */
    public Map<String, Boolean> getIsParent() {

        if (m_isParent == null) {
            m_isParent = CmsCollectionsGenericWrapper.createLazyMap(new CmsIsParentTransformer());
        }
        return m_isParent;
    }

    /**
     * Returns the list of selected navigation elements.<p>
     *
     * @return the list of selected navigation elements
     */
    public List<CmsJspNavElement> getItems() {

        if (m_items == null) {
            switch (m_type) {
                // calculate the results based on the given parameters
                case forFolder:
                    if (m_startLevel == Integer.MIN_VALUE) {
                        // no start level set
                        if (m_resource == null) {
                            m_items = m_builder.getNavigationForFolder();
                        } else {
                            m_items = m_builder.getNavigationForFolder(m_resource);
                        }
                    } else {
                        // start level is set
                        if (m_resource == null) {
                            m_items = m_builder.getNavigationForFolder(m_startLevel);
                        } else {
                            m_items = m_builder.getNavigationForFolder(m_resource, m_startLevel);
                        }
                    }
                    break;
                case forSite:
                    if (m_resource == null) {
                        m_items = m_builder.getSiteNavigation();
                    } else {
                        m_items = m_builder.getSiteNavigation(m_resource, m_endLevel);
                    }
                    break;
                case breadCrumb:
                    if (m_resource != null) {
                        // resource is set
                        m_items = m_builder.getNavigationBreadCrumb(
                            m_resource,
                            m_startLevel,
                            m_endLevel,
                            Boolean.valueOf(m_param).booleanValue());
                    } else {
                        if (m_startLevel == Integer.MIN_VALUE) {
                            // default start level is zero
                            m_items = m_builder.getNavigationBreadCrumb(0, Boolean.valueOf(m_param).booleanValue());
                        } else {
                            if (m_endLevel != Integer.MIN_VALUE) {
                                m_items = m_builder.getNavigationBreadCrumb(m_startLevel, m_endLevel);
                            } else {
                                m_items = m_builder.getNavigationBreadCrumb(
                                    m_startLevel,
                                    Boolean.valueOf(m_param).booleanValue());
                            }
                        }
                    }
                    break;
                case treeForFolder:
                    if (m_resource == null) {
                        m_items = m_builder.getNavigationTreeForFolder(m_startLevel, m_endLevel);
                    } else {
                        m_items = m_builder.getNavigationTreeForFolder(m_resource, m_startLevel, m_endLevel);
                    }
                    break;
                case forResource:
                default:
                    List<CmsJspNavElement> items = new ArrayList<CmsJspNavElement>(1);
                    if (m_resource == null) {
                        items.add(m_builder.getNavigationForResource());
                    } else {
                        items.add(m_builder.getNavigationForResource(m_resource));
                    }
                    m_items = items;
                    break;
            }
        }
        return m_items;
    }
}
