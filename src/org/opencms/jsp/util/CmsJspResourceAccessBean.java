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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * Allows access to the attributes and properties of a resource, usually used inside a loop of a
 * <code>&lt;cms:resourceload&gt;</code> tag.<p>
 *
 * The implementation is optimized for performance and uses lazy initializing of the
 * requested values as much as possible.<p>
 *
 * @since 8.0
 *
 * @see org.opencms.jsp.CmsJspTagResourceAccess
 */
public class CmsJspResourceAccessBean {

    /**
     * Transformer that reads a history resource property,
     * the input is used as String for the history property name to read.<p>
     */
    private class CmsHistoryPropertyLoaderTransformer implements Transformer {

        /** The resource where the properties are read from. */
        private I_CmsHistoryResource m_res;

        /**
         * Creates a new property loading Transformer.<p>
         *
         * @param resource the resource where the properties are read from
         */
        public CmsHistoryPropertyLoaderTransformer(CmsResource resource) {

            m_res = (I_CmsHistoryResource)resource;

        }

        /**
         * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
         */
        public Object transform(Object input) {

            String result;
            try {
                // read the requested history property
                result = CmsProperty.get(
                    String.valueOf(input),
                    getCmsObject().readHistoryPropertyObjects(m_res)).getValue();
            } catch (@SuppressWarnings("unused") CmsException e) {
                // unable to read history property, return null
                result = null;
            }
            return result;
        }
    }

    /** The OpenCms context of the current user. */
    private CmsObject m_cms;

    /** The file that can be accessed. */
    private CmsFile m_file;

    /** The history properties of the resource. */
    private Map<String, String> m_historyProperties;

    /** The properties of the resource. */
    private Map<String, String> m_properties;

    /**
     * The properties of the resource according to the provided locale.
     * The map goes from locale -> property -> value.
     */
    private Map<String, Map<String, String>> m_localeProperties;

    /** The resource that can be accessed. */
    private CmsResource m_resource;

    /**
     * No argument constructor, required for a JavaBean.<p>
     *
     * You must call {@link #init(CmsObject, CmsResource)} and provide the
     * required values when you use this constructor.<p>
     *
     * @see #init(CmsObject, CmsResource)
     */
    public CmsJspResourceAccessBean() {

        // must call init() manually later
    }

    /**
     * Creates a content access bean based on a Resource.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param resource the resource to create the content from
     */
    public CmsJspResourceAccessBean(CmsObject cms, CmsResource resource) {

        init(cms, resource);
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
     * Returns the raw VFS file object of the current resource.<p>
     *
     * This can be used to access information from the raw file on a JSP.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     Root path of the resource: ${res.file.rootPath}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return the raw VFS file object the content accessed by this bean was created from
     */
    public CmsFile getFile() {

        if (m_file == null) {
            try {
                m_file = m_cms.readFile(m_resource);
            } catch (CmsException e) {
                // this usually should not happen, as the resource already has been read by the current user
                // and we just upgrade it to a file
                throw new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_FILE_READ_1, m_resource.getRootPath()),
                    e);
            }
        }
        return m_file;
    }

    /**
     * Returns the file contents of the raw VFS file object as String.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     String content of the resource: ${res.fileContentAsString}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return the file contents of the raw VFS file object as String
     */
    public String getFileContentAsString() {

        return new String(getFile().getContents());
    }

    /**
     * Returns the site path of the current resource, that is the result of
     * {@link CmsObject#getSitePath(CmsResource)} with the resource
     * obtained by {@link #getFile()}.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &&lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     Site path of the resource: "${res.filename}";
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return the site path of the current resource
     *
     * @see CmsObject#getSitePath(CmsResource)
     */
    public String getFilename() {

        return m_cms.getSitePath(m_resource);
    }

    /**
     * Short form for {@link #getReadHistoryProperties()}.<p>
     *
     * This works only if the current resource is implementing {@link I_CmsHistoryResource}.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     History "Title" property value of the resource: ${res.historyProperty['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads history properties of the resource
     *
     * @see #getReadHistoryProperties()
     */
    public Map<String, String> getHistoryProperty() {

        return getReadHistoryProperties();
    }

    /**
     * Short form for {@link #getReadProperties()}.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     "Title" property value of the resource: ${res.property['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads properties of the resource
     *
     * @see #getReadProperties()
     */
    public Map<String, String> getProperty() {

        return getReadProperties();
    }

    /**
     * Short form for {@link #getReadPropertiesLocale()}.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     "Title" property value of the resource: ${res.property['de']['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads properties of the resource and accesses them wrt. to the specified locale.
     *
     * @see #getReadPropertiesLocale()
     */
    public Map<String, Map<String, String>> getPropertyLocale() {

        return getReadPropertiesLocale();
    }

    /**
     * Returns a map that lazily reads history properties of the resource.<p>
     *
     * This works only if the current resource is implementing {@link I_CmsHistoryResource}.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     History "Title" property value of the resource: ${res.readHistoryProperties['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads properties of the resource
     *
     * @see #getProperty() for a short form of this method
     */
    public Map<String, String> getReadHistoryProperties() {

        if (m_historyProperties == null) {
            // create lazy map only on demand
            m_historyProperties = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsHistoryPropertyLoaderTransformer(m_resource));
        }
        return m_historyProperties;
    }

    /**
     * Returns a map that lazily reads properties of the resource.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     "Title" property value of the resource: ${res.readProperties['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads properties of the resource
     *
     * @see #getProperty() for a short form of this method
     */
    public Map<String, String> getReadProperties() {

        if (m_properties == null) {
            // create lazy map only on demand
            m_properties = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsJspValueTransformers.CmsPropertyLoaderTransformer(m_cms, m_resource, false));
        }
        return m_properties;
    }

    /**
     * Returns a map that lazily reads properties of the resource and makes the accessible according to the specified locale.<p>
     *
     * Usage example on a JSP with the <code>&lt;cms:resourceaccess&gt;</code> tag:<pre>
     * &lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     "Title" property value of the resource: ${res.readProperties['de']['Title']}
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return a map that lazily reads properties of the resource and makes the accessible according to the specified locale.
     *
     * @see #getPropertyLocale() for a short form of this method
     */
    public Map<String, Map<String, String>> getReadPropertiesLocale() {

        if (m_localeProperties == null) {
            m_localeProperties = CmsCollectionsGenericWrapper.createLazyMap(
                new CmsJspValueTransformers.CmsLocalePropertyLoaderTransformer(m_cms, m_resource, false));
        }
        return m_localeProperties;

    }

    /**
     * Returns the current resource.<p>
     *
     * Usage example on a JSP with the JSTL:<pre>
     * &&lt;cms:resourceload ... &gt;
     *     &lt;cms:resourceaccess var="res" /&gt;
     *     Root path of the resource: "${res.resource.rootPath}";
     * &lt;/cms:resourceload&gt;</pre>
     *
     * @return the current resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns an instance of a VFS access bean,
     * initialized with the OpenCms user context this bean was created with.<p>
     *
     * @return an instance of a VFS access bean,
     *      initialized with the OpenCms user context this bean was created with
     */
    public CmsJspVfsAccessBean getVfs() {

        return CmsJspVfsAccessBean.create(m_cms);
    }

    /**
     * Initialize this instance.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param resource the resource to create the content from
     */
    public void init(CmsObject cms, CmsResource resource) {

        m_cms = cms;
        m_resource = resource;
    }
}