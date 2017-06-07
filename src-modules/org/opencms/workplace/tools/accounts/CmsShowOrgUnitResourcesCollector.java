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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collector for organizational unit resources.<p>
 *
 * @since 6.5.6
 */
public class CmsShowOrgUnitResourcesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "ouResources";

    /** The resources. */
    private List<CmsResource> m_resourcesList;

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     * @param resources list of ou resources
     */
    public CmsShowOrgUnitResourcesCollector(A_CmsListExplorerDialog wp, List<CmsResource> resources) {

        super(wp);
        m_resourcesList = resources;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        List<String> names = new ArrayList<String>();
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) {

        return m_resourcesList;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // no-op
    }
}
