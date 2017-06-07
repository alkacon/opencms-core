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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Publish group helper which only puts all publish resources into a single group.<p>
 */
public class CmsSinglePublishGroupHelper extends A_CmsPublishGroupHelper<CmsPublishResource, CmsPublishGroup> {

    /** The title to use for the single group. */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param locale the locale to use
     * @param title the title to use for the single group
     */
    public CmsSinglePublishGroupHelper(Locale locale, String title) {

        super(locale);
        m_title = title;
    }

    /**
     * @see org.opencms.ade.publish.A_CmsPublishGroupHelper#getGroups(java.util.List)
     */
    @Override
    public List<CmsPublishGroup> getGroups(List<CmsPublishResource> resources) {

        List<CmsPublishGroup> result = new ArrayList<CmsPublishGroup>();
        if (!resources.isEmpty()) { // only add a group if there are resources
            List<CmsPublishResource> copyResources = new ArrayList<CmsPublishResource>(resources);
            CmsPublishGroup singleGroup = new CmsPublishGroup(m_title, copyResources);
            result.add(singleGroup);
        }
        return result;
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

        return res.getDateLastModified();
    }

    /**
     * @see org.opencms.ade.publish.A_CmsPublishGroupHelper#getRootPath(java.lang.Object)
     */
    @Override
    protected String getRootPath(CmsPublishResource res) {

        return res.getName();
    }

}
