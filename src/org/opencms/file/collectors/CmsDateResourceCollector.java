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

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A default resource collector that supports flexible sorting based on resource dates.<p>
 *
 * @since 7.0.2
 */
public class CmsDateResourceCollector extends A_CmsResourceCollector {

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {
        "allInFolderDateDesc",
        "allInFolderDateAsc",
        "allInSubTreeDateDesc",
        "allInSubTreeDateAsc"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

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
    throws CmsDataAccessException, CmsException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allInFolderDateDesc"
            case 1:
                // "allInFolderDateAsc"
                return getCreateInFolder(cms, new CmsExtendedCollectorData(param));
            case 2:
                // "allInSubTreeDateDesc"
            case 3:
                // "allInSubTreeDateAsc"
                return null;
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) throws CmsDataAccessException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allInFolderDateDesc"
            case 1:
                // "allInFolderDateAsc"
                return param;
            case 2:
                // "allInSubTreeDateDesc"
            case 3:
                // "allInSubTreeDateAsc"
                return null;
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.A_CmsResourceCollector#getCreateTypeId(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public int getCreateTypeId(CmsObject cms, String collectorName, String param) {

        int result = -1;
        if (param != null) {
            result = new CmsExtendedCollectorData(param).getType();
        }
        return result;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param)
    throws CmsDataAccessException, CmsException {

        return getResults(cms, collectorName, param, -1);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List<CmsResource> getResults(CmsObject cms, String collectorName, String param, int numResults)
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "allInFolderDateDesc"
                return allInFolderDate(cms, param, false, false, numResults);
            case 1:
                // "allInFolderDateAsc"
                return allInFolderDate(cms, param, false, true, numResults);
            case 2:
                // "allInSubTreeDateDesc"
                return allInFolderDate(cms, param, true, false, numResults);
            case 3:
                // "allInSubTreeDateAsc"
                return allInFolderDate(cms, param, true, true, numResults);
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * Returns a List of all resources in the folder pointed to by the parameter
     * sorted by the selected dates.<p>
     *
     * @param cms the current CmsObject
     * @param param must contain an extended collector parameter set as described by {@link CmsExtendedCollectorData}
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param asc if <code>true</code>, the sort is ascending (old dates first), otherwise it is descending
     *      (new dates first)
     * @param numResults number of results
     *
     * @return a List of all resources in the folder pointed to by the parameter sorted by the selected dates
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allInFolderDate(CmsObject cms, String param, boolean tree, boolean asc, int numResults)
    throws CmsException {

        CmsExtendedCollectorData data = new CmsExtendedCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());
        List<String> dateIdentifiers = data.getAdditionalParams();

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        // a special date comparator is used to sort the resources
        CmsDateResourceComparator comparator = new CmsDateResourceComparator(cms, dateIdentifiers, asc);
        Collections.sort(result, comparator);

        return shrinkToFit(result, data.getCount(), numResults);
    }
}