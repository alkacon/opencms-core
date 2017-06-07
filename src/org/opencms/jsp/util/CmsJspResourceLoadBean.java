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

import java.util.List;

/**
 * Allows JSP access to the results of a &lt;cms:resourceload ... &gt; operation using the JSTL and EL.<p>
 *
 * @since 8.0
 *
 * @see org.opencms.jsp.CmsJspTagResourceLoad
 * @see CmsJspResourceAccessBean
 */
public class CmsJspResourceLoadBean {

    /** The OpenCms context of the current user. */
    protected CmsObject m_cms;

    /** The list of results of the resource loader. */
    protected List<CmsResource> m_resources;

    /**
     * No argument constructor, required for a JavaBean.<p>
     *
     * You must call {@link #init(CmsObject, List)} and provide the
     * required values when you use this constructor.<p>
     *
     * @see #init(CmsObject, List)
     */
    public CmsJspResourceLoadBean() {

        // must call init() manually later
    }

    /**
     * Creates a new context bean using the OpenCms context of the current user.<p>
     *
     * The current request context locale is used.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param resources the resources to access, must contain objects of type {@link CmsResource}
     */
    public CmsJspResourceLoadBean(CmsObject cms, List<CmsResource> resources) {

        init(cms, resources);
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
     * Returns a list of {@link CmsResource} instances.<p>
     *
     * @return a list of {@link CmsResource} instances
     */
    public List<CmsResource> getResources() {

        return m_resources;
    }

    /**
     * Initialize this instance.<p>
     *
     * @param cms the OpenCms context of the current user
     * @param resources the resources to access, must contain objects of type {@link CmsResource}
     */
    public void init(CmsObject cms, List<CmsResource> resources) {

        m_cms = cms;
        m_resources = resources;
    }
}