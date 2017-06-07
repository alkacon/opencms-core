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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsLog;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Collector for locked resources.<p>
 *
 * @since 6.5.4
 */
public class CmsLockedResourcesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "lockedResources";

    /** This constant is just a hack to mark related resources in the list. */
    private static final int FLAG_RELATED_RESOURCE = 8192;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLockedResourcesCollector.class);

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     * @param resources list of locked resources
     */
    public CmsLockedResourcesCollector(A_CmsListExplorerDialog wp, List<String> resources) {

        super(wp);
        setResourcesParam(resources);
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

        List<CmsResource> resources = new ArrayList<CmsResource>();
        Iterator<String> itResourceNames = getResourceNamesFromParam(params).iterator();
        while (itResourceNames.hasNext()) {
            String resName = itResourceNames.next();
            boolean relatedResource = resName.endsWith("*");
            if (relatedResource) {
                resName = resName.substring(0, resName.length() - 1);
            }
            try {
                CmsResource resource = cms.readResource(resName, CmsResourceFilter.ALL);
                if (relatedResource) {
                    resource = new CmsResource(
                        resource.getStructureId(),
                        resource.getResourceId(),
                        resource.getRootPath(),
                        resource.getTypeId(),
                        resource.isFolder(),
                        resource.getFlags() | FLAG_RELATED_RESOURCE,
                        resource.getProjectLastModified(),
                        resource.getState(),
                        resource.getDateCreated(),
                        resource.getUserCreated(),
                        resource.getDateLastModified(),
                        resource.getUserLastModified(),
                        resource.getDateReleased(),
                        resource.getDateExpired(),
                        resource.getSiblingCount(),
                        resource.getLength(),
                        resource.getDateContent(),
                        resource.getVersion());
                }
                resources.add(resource);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return resources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        item.set(
            CmsLockedResourcesList.LIST_COLUMN_IS_RELATED,
            Boolean.valueOf((resUtil.getResource().getFlags() & FLAG_RELATED_RESOURCE) == FLAG_RELATED_RESOURCE));
    }
}
