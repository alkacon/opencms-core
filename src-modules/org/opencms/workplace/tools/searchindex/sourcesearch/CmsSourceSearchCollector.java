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

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListResourceCollector;
import org.opencms.workplace.list.CmsListItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Collector for {@link org.opencms.file.CmsResource} resources to do source search in.<p>
 *
 * @since 7.5.3
 */
public class CmsSourceSearchCollector extends A_CmsListResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "sourcesearchresources";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSourceSearchCollector.class);

    /** The source search files dialog. */
    private CmsSourceSearchFilesDialog m_wp;

    /**
     * Constructor, creates a new instance.<p>
     *
     * @param wp the workplace object
     */
    public CmsSourceSearchCollector(CmsSourceSearchFilesDialog wp) {

        super(wp);
        m_wp = wp;
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
     * Returns the resource for the given item.<p>
     *
     * @param cms the cms object
     * @param item the item
     *
     * @return the resource
     */
    @Override
    public CmsResource getResource(CmsObject cms, CmsListItem item) {

        CmsResource res = null;

        CmsUUID id = new CmsUUID(item.getId());
        if (!id.isNullUUID()) {
            try {
                res = cms.readResource(id, CmsResourceFilter.ALL);
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return res;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map params) throws CmsException {

        ArrayList<CmsResource> files = new ArrayList<CmsResource>();
        // read the files again, because they can be changed
        if (m_wp.getFiles() != null) {
            Iterator<CmsResource> iter = m_wp.getFiles().iterator();
            while (iter.hasNext()) {
                CmsResource oldResource = iter.next();
                CmsResource newResource = cms.readResource(cms.getSitePath(oldResource));
                files.add(newResource);
            }
        }
        return files;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListResourceCollector#setAdditionalColumns(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.explorer.CmsResourceUtil)
     */
    @Override
    protected void setAdditionalColumns(CmsListItem item, CmsResourceUtil resUtil) {

        // no-op
    }
}
