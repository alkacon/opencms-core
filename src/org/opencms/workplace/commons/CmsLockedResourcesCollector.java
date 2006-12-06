/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/CmsLockedResourcesCollector.java,v $
 * Date   : $Date: 2006/12/06 16:13:18 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.5.4 
 */
public class CmsLockedResourcesCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "lockedResources";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLockedResourcesCollector.class);

    /** Resources parameter name constant. */
    public static final String PARAM_RESOURCES = "resources";

    /**
     * Constructor, creates a new instance.<p>
     * 
     * @param wp the workplace object
     * @param resources list of locked resources
     */
    public CmsLockedResourcesCollector(A_CmsListExplorerDialog wp, List resources) {

        super(wp);
        m_collectorParameter += SEP_PARAM + PARAM_RESOURCES + SEP_KEYVAL;
        if (resources == null) {
            // search anywhere
            m_collectorParameter += "/";
        } else {
            Iterator it = resources.iterator();
            while (it.hasNext()) {
                m_collectorParameter += it.next();
                if (it.hasNext()) {
                    m_collectorParameter += "#";
                }
            }
        }
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
    public List getResources(CmsObject cms, Map params) {

        String resourcesParam = "/";
        if (params.containsKey(PARAM_RESOURCES)) {
            resourcesParam = (String)params.get(PARAM_RESOURCES);
        }
        List resources = new ArrayList();
        if (resourcesParam.length() == 0) {
            return resources;
        }
        int pos = 0;
        while (pos > -1) {
            int newPos = resourcesParam.indexOf("#", pos + 1);
            String resName;
            if (newPos == -1) {
                resName = resourcesParam.substring(pos + 1);
            } else {
                resName = resourcesParam.substring(pos + 1, newPos);
            }
            pos = newPos;
            try {
                resources.add(cms.readResource(resName, CmsResourceFilter.ALL));
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return resources;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        String relativeTo = resUtil.getRelativeTo();
        int abbrevLength = resUtil.getAbbrevLength();
        resUtil.setRelativeTo(null);
        resUtil.setAbbrevLength(0);
        item.set(CmsLockedResourcesList.LIST_COLUMN_ROOT_PATH, resUtil.getPath());
        resUtil.setRelativeTo(relativeTo);
        resUtil.setAbbrevLength(abbrevLength);
    }
}
