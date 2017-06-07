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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsObject;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collector for receiving {@link org.opencms.file.CmsResource} objects from a {@link CmsContentCheckResult}.<p>
 *
 * @since 6.1.2
 */
public class CmsContentCheckCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "checkresources";

    /** Parameter to get all resources with errors and warnings. */
    public static final String PARAM_ALL = "all";

    /** Parameter to get all resources with errors. */
    public static final String PARAM_ERROR = "error";

    /** Parameter to get all resources with  warnings. */
    public static final String PARAM_WARNING = "warning";

    /** The list of resources delivered by the collector. */
    private CmsContentCheckResult m_results;

    /**
     * Constructor, creates a new CmsContentCheckCollector.<p>
     *
     * @param wp the workplace object
     *
     * @param results a CmsContentCheckResult object, containing the results of the content check.
     */
    public CmsContentCheckCollector(A_CmsListExplorerDialog wp, CmsContentCheckResult results) {

        super(wp);
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM + PARAM_ALL;
        m_results = results;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List getCollectorNames() {

        List names = new ArrayList();
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @Override
    public List getResources(CmsObject cms, Map params) {

        if (params.containsKey(PARAM_ERROR)) {
            return m_results.getErrorResources();
        } else if (params.containsKey(PARAM_WARNING)) {
            return m_results.getWarningResources();
        } else if (params.containsKey(PARAM_ALL)) {
            return m_results.getAllResources();
        } else {
            // the default is to return all resources
            return m_results.getAllResources();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // no-op
    }
}
