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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collector for model files used in the list for the new XML content dialog.<p>
 *
 * @since 6.5.4
 */
public class CmsNewResourceXmlContentModelCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "xmlContentModelFiles";

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     * @param resources list of locked resources
     */
    public CmsNewResourceXmlContentModelCollector(A_CmsListExplorerDialog wp, List<CmsResource> resources) {

        super(wp);
        m_resources = resources;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        List<String> names = new ArrayList<String>(1);
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * Returns the dummy resource object representing the "none" selection, this has to be treated specially.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResource(org.opencms.file.CmsObject, org.opencms.workplace.list.CmsListItem)
     */
    @Override
    public CmsResource getResource(CmsObject cms, CmsListItem item) {

        // check if the item is the "dummy" item
        if (item.getId().equals(CmsUUID.getConstantUUID(CmsNewResourceXmlContent.VALUE_NONE + "s").getStringValue())) {
            for (CmsResource result : m_resources) {
                if (item.getId().equals(result.getStructureId().getStringValue())) {
                    return result;
                }
            }
        }
        // all other items are real resources, use the default implementation
        return super.getResource(cms, item);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) {

        return m_resources;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String parameter) {

        return m_resources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // no-op
    }
}
