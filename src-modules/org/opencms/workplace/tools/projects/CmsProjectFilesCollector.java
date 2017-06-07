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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Collector for {@link org.opencms.file.CmsResource} objects from a project.<p>
 *
 * @since 6.1.0
 */
public class CmsProjectFilesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "projectresources";

    /** Project Parameter name constant. */
    public static final String PARAM_PROJECT = "project";

    /** Resource state Parameter name constant. */
    public static final String PARAM_STATE = "state";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectFilesCollector.class);

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     * @param projectId the id of the project
     * @param state the state of the resources to filter
     */
    public CmsProjectFilesCollector(A_CmsListExplorerDialog wp, CmsUUID projectId, CmsResourceState state) {

        super(wp);
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + PARAM_STATE
            + I_CmsListResourceCollector.SEP_KEYVAL
            + state;
        m_collectorParameter += I_CmsListResourceCollector.SEP_PARAM
            + PARAM_PROJECT
            + I_CmsListResourceCollector.SEP_KEYVAL
            + projectId;
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
    public List getResources(CmsObject cms, Map params) throws CmsException {

        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        try {
            projectId = new CmsUUID((String)params.get(PARAM_PROJECT));
        } catch (Throwable e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
        CmsResourceState state = CmsResource.STATE_KEEP;
        try {
            state = CmsResourceState.valueOf(Integer.parseInt((String)params.get(PARAM_STATE)));
        } catch (Throwable e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }

        // show files in the selected project with the selected status
        List resources = cms.readProjectView(projectId, state);

        // remove not visible files
        Iterator itRes = resources.iterator();
        // dont's show resources that  are in a different site root
        String siteRoot = cms.getRequestContext().getSiteRoot();
        // this is not sufficient (startsWith) if one siteRoot is prefix of another as siteRoot ends without slash!
        siteRoot += "/";
        while (itRes.hasNext()) {
            CmsResource resource = (CmsResource)itRes.next();
            String rootPath = resource.getRootPath();
            if (!rootPath.startsWith(siteRoot)
                && !rootPath.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)
                && !OpenCms.getSiteManager().startsWithShared(rootPath)) {
                itRes.remove();
            }
        }
        return resources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // no-op
    }
}
