/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * A Solr collector.<p>
 */
public class CmsSolrCollector extends A_CmsResourceCollector {

    /** Constant array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {"byQuery", "byContext"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The name of the priority property to read. */
    public static final String PROPERTY_PRIORITY_FIELD = "collector.priority_s";

    /** The name of the date property to read. */
    public static final String PROPERTY_DATE_FIELD = "collector.date_s";

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param)
    throws CmsException, CmsDataAccessException {

        throw new CmsDataAccessException(Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[1];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
            case 1:
                // "allInFolderPriorityDateAsc", "allInFolderPriorityDateDesc" or "allInFolderPriorityTitleDesc"
                return param;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[1];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {

            case 0:
                // "byQuery"
                return new ArrayList<CmsResource>(OpenCms.getSearchManager().getIndexSolr("Solr Offline").search(
                    cms,
                    param));
            case 1:
                // "byContext"
                CmsSolrQuery q = new CmsSolrQuery(cms);
                if (param != null) {
                    q.add(new ModifiableSolrParams(CmsRequestUtil.createParameterMap(param)));
                }
                return new ArrayList<CmsResource>(
                    OpenCms.getSearchManager().getIndexSolr("Solr Offline").search(cms, q));
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }
}
