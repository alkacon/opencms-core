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
import org.opencms.search.CmsSearchManager;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Solr collector.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrCollector extends A_CmsResourceCollector {

    /** Constant array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {"byQuery", "byContext"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The folder path to create the "create link" for. */
    private static final String PARAM_CREATE_PATH = "createPath=";

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) throws CmsException {

        collectorName = collectorName == null ? COLLECTORS[1] : collectorName;
        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0: // byQuery
            case 1: // byContext
                return getCreateInFolder(cms, param);
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {

        collectorName = collectorName == null ? COLLECTORS[1] : collectorName;
        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0: // byQuery
            case 1: // byContext
                String solrParams = null;
                if (param.indexOf('|') > 0) {
                    solrParams = param.substring(0, param.indexOf('|'));
                    CmsSolrQuery q = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(solrParams));
                    String type = CmsSolrQuery.getResourceType(q.getFilterQueries());
                    String rows = q.getRows().toString();
                    int lastPipe = param.lastIndexOf('|');
                    if (lastPipe > 0) {
                        int idx = param.indexOf(PARAM_CREATE_PATH, lastPipe);
                        if (idx > 0) {
                            String path = param.substring(idx + PARAM_CREATE_PATH.length());
                            if ((type != null) && (rows != null) && (path != null)) {
                                return path + "|" + type + "|" + rows;
                            }
                        }
                    }
                }
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_COLLECTOR_NAME_INVALID_1,
                    collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String name, String param) throws CmsException {

        name = name == null ? COLLECTORS[1] : name;
        if ((param != null) && (param.indexOf('|') != -1)) {
            param = param.substring(0, param.indexOf('|'));
        }
        Map<String, String[]> pm = CmsRequestUtil.createParameterMap(param);
        CmsSolrIndex index = CmsSearchManager.getIndexSolr(cms, pm);
        CmsSolrQuery q = COLLECTORS_LIST.indexOf(name) == 0 ? new CmsSolrQuery(null, pm) : new CmsSolrQuery(cms, pm);
        return new ArrayList<CmsResource>(index.search(cms, q, true));
    }
}
