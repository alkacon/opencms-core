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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Allows JSP access to the results of a &lt;cms:contentload ... &gt; operation using the JSTL and EL.<p>
 *
 * @since 7.0.2
 *
 * @see org.opencms.jsp.CmsJspTagContentLoad
 * @see CmsJspContentAccessBean
 */
public class CmsJspContentLoadBean {

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** The List of results form the content loader. */
    protected List<CmsJspContentAccessBean> m_content;

    /** The selected locale for accessing entries from the XML content. */
    protected Locale m_locale;

    /**
     * No argument constructor, required for a JavaBean.<p>
     *
     * You must call {@link #init(CmsObject, Locale, List)} and provide the
     * required values when you use this constructor.<p>
     *
     * @see #init(CmsObject, Locale, List)
     */
    public CmsJspContentLoadBean() {

        // must call init() manually later
    }

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     *
     * The current request context locale is used.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param content the content to access, must contain Object of type {@link CmsResource}
     */
    public CmsJspContentLoadBean(CmsObject cms, List<CmsResource> content) {

        this(cms, cms.getRequestContext().getLocale(), content);
    }

    /**
     * Creates a new context bean using the OpenCms context of the current user with the given locale.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param content the content to access, must contain Object of type {@link CmsResource}
     */
    public CmsJspContentLoadBean(CmsObject cms, Locale locale, List<CmsResource> content) {

        init(cms, locale, content);
    }

    /**
     * Converts a list of {@link CmsResource} objects to a list of {@link CmsJspContentAccessBean} objects,
     * using the current request context locale.<p>
     *
     * @param cms the current OpenCms user context
     * @param resources a list of of {@link CmsResource} objects that should be converted
     *
     * @return a list of {@link CmsJspContentAccessBean} objects created from the given {@link CmsResource} objects
     */
    public static List<CmsJspContentAccessBean> convertResourceList(CmsObject cms, List<CmsResource> resources) {

        return convertResourceList(cms, cms.getRequestContext().getLocale(), resources);
    }

    /**
     * Converts a list of {@link CmsResource} objects to a list of {@link CmsJspContentAccessBean} objects,
     * using the given locale.<p>
     *
     * @param cms the current OpenCms user context
     * @param locale the default locale to use when accessing the content
     * @param resources a list of of {@link CmsResource} objects that should be converted
     *
     * @return a list of {@link CmsJspContentAccessBean} objects created from the given {@link CmsResource} objects
     */
    public static List<CmsJspContentAccessBean> convertResourceList(
        CmsObject cms,
        Locale locale,
        List<CmsResource> resources) {

        List<CmsJspContentAccessBean> result = new ArrayList<CmsJspContentAccessBean>(resources.size());
        for (int i = 0, size = resources.size(); i < size; i++) {
            CmsResource res = resources.get(i);
            result.add(new CmsJspContentAccessBean(cms, locale, res));
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
     * Returns a List of {@link CmsJspContentAccessBean} instances, which have been wrapped around
     * the original {@link CmsResource} instances of the collector result.<p>
     *
     * @return a List of {@link CmsJspContentAccessBean} instances, which have been wrapped around
     * the original {@link CmsResource} instances of the collector result.<p>
     */
    public List<CmsJspContentAccessBean> getContent() {

        return m_content;
    }

    /**
     * Returns the Locale this bean was initialized with.<p>
     *
     * @return the locale  this bean was initialized with
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Initialize this instance.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param locale the Locale to use when accessing the content
     * @param content the content to access, must contain Object of type {@link CmsResource}
     */
    public void init(CmsObject cms, Locale locale, List<CmsResource> content) {

        m_cms = cms;
        m_locale = locale;
        m_content = convertResourceList(m_cms, m_locale, content);
    }
}