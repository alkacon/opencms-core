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

package org.opencms.workplace.list;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.main.CmsException;

import java.util.List;
import java.util.Map;

/**
 * Collector to provide {@link CmsResource} objects for a explorer List.<p>
 *
 * @since 6.1.0
 */
public interface I_CmsListResourceCollector extends I_CmsResourceCollector {

    /** Parameter name constant. */
    String PARAM_FILTER = "filter";

    /** Parameter name constant. */
    String PARAM_ORDER = "order";

    /** Parameter name constant. */
    String PARAM_PAGE = "page";

    /** Resources parameter name constant. */
    String PARAM_RESOURCES = "resources";

    /** Parameter name constant. */
    String PARAM_SORTBY = "sortby";

    /** Key-Value delimiter constant. */
    String SEP_KEYVAL = ":";

    /** Parameter delimiter constant. */
    String SEP_PARAM = "|";

    /**
     * Returns a list of list items from a list of resources.<p>
     *
     * @param parameter the collector parameter or <code>null</code> for default.<p>
     *
     * @return a list of {@link CmsListItem} objects
     *
     * @throws CmsException if something goes wrong
     */
    List<CmsListItem> getListItems(String parameter) throws CmsException;

    /**
     * Returns the resource for the given item.<p>
     *
     * @param cms the cms object
     * @param item the item
     *
     * @return the resource
     */
    CmsResource getResource(CmsObject cms, CmsListItem item);

    /**
     * Returns all, unsorted and unfiltered, resources.<p>
     *
     * Be sure to cache the resources.<p>
     *
     * @param cms the cms object
     * @param params the parameter map
     *
     * @return a list of {@link CmsResource} objects
     *
     * @throws CmsException if something goes wrong
     */
    List<CmsResource> getResources(CmsObject cms, Map<String, String> params) throws CmsException;

    /**
     * Returns the workplace object.<p>
     *
     * @return the workplace object
     */
    A_CmsListExplorerDialog getWp();

    /**
     * Sets the current display page.<p>
     *
     * @param page the new display page
     */
    void setPage(int page);
}
