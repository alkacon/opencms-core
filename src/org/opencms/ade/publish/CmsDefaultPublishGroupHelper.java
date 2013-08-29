/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishResource;

import java.util.List;
import java.util.Locale;

/**
 * Default implementation of the publish group helper which operates on {@link CmsPublishResource} objects.<p>
 */
public class CmsDefaultPublishGroupHelper extends A_CmsPublishGroupHelper<CmsPublishResource, CmsPublishGroup> {

    /**
     * Creates a new publish group helper.<p>
     *  
     * @param locale the locale to use 
     */
    public CmsDefaultPublishGroupHelper(Locale locale) {

        super(locale);

    }

    /**
     * @see org.opencms.ade.publish.A_CmsPublishGroupHelper#createGroup(java.lang.String, java.util.List)
     */
    @Override
    protected CmsPublishGroup createGroup(String name, List<CmsPublishResource> resources) {

        return new CmsPublishGroup(name, resources);
    }

    /**
     * @see org.opencms.ade.publish.A_CmsPublishGroupHelper#getDateLastModified(java.lang.Object)
     */
    @Override
    protected long getDateLastModified(CmsPublishResource res) {

        return res.getSortDate();
    }

    /**
     * 
     * @see org.opencms.ade.publish.A_CmsPublishGroupHelper#getRootPath(java.lang.Object)
     */
    @Override
    protected String getRootPath(CmsPublishResource res) {

        return res.getName();
    }

}
