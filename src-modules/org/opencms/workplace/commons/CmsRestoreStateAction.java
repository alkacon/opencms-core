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

package org.opencms.workplace.commons;

import org.opencms.file.CmsObject;
import org.opencms.workplace.list.CmsListDefaultAction;

/**
 * Show different states depending on the name of the resource.<p>
 */
public class CmsRestoreStateAction extends CmsListDefaultAction {

    /** Cms context. */
    private CmsObject m_cms;

    /** The name of the actual resource. */
    private String m_resource;

    /**
     * Default constructor.<p>
     *
     * @param id the id of the action
     * @param cms the cms context
     */
    public CmsRestoreStateAction(String id, CmsObject cms) {

        super(id);
        m_cms = cms;
    }

    /**
     * Returns the cms context.<p>
     *
     * @return the cms context
     */
    public CmsObject getCms() {

        return m_cms;
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
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
     */
    @Override
    public boolean isVisible() {

        String paramResource = getResource();
        if (paramResource == null) {
            return false;
        }

        // not for offline entries
        if ("-1".equals(getItem().getId())) {
            return false;
        }

        String itemResource = getCms().getRequestContext().removeSiteRoot(
            (String)getItem().get(CmsHistoryList.LIST_COLUMN_RESOURCE_PATH));
        return paramResource.equals(itemResource);
    }

    /**
     * Sets the resource.<p>
     *
     * @param resource the resource to set
     */
    public void setResource(String resource) {

        m_resource = resource;
    }

}
