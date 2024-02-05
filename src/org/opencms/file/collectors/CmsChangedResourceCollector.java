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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A resource collector that collects resources changed in a given time frame and supports flexible sorting based on resource dates.<p>
 *
 * @since 8.0
 */
public class CmsChangedResourceCollector extends A_CmsResourceCollector {

    /** The collector parameter key for the maximum number of resources to return. */
    public static final String PARAM_KEY_COUNT = "count";

    /** The collector parameter key for the date from which a resource should be changed. */
    public static final String PARAM_KEY_DATEFROM = "datefrom";

    /** The collector parameter key for the date to which a resource should be changed. */
    public static final String PARAM_KEY_DATETO = "dateto";

    /** The collector parameter key for the name of the resource type to exclude from the result. */
    public static final String PARAM_KEY_EXCLUDETYPE = "excludetype";

    /** The collector parameter key for the resource, i.e. the parent folder from which the subscribed or visited resources should be read from. */
    public static final String PARAM_KEY_RESOURCE = "resource";

    /** The collector parameter key for the sort attribute that should be used to sort the result. */
    public static final String PARAM_KEY_SORTBY = "sortby";

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {
        "allChangedInFolderDateDesc",
        "allChangedInFolderDateAsc",
        "allChangedInSubTreeDateDesc",
        "allChangedInSubTreeDateAsc"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangedResourceCollector.class);

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List<String> getCollectorNames() {

        return COLLECTORS_LIST;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) {

        // this collector does not support creation of new resources
        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        // this collector does not support creation of new resources
        return null;
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
                // "allChangedInFolderDateDesc"
                return allChangedInFolderDate(cms, param, false, false, numResults);
            case 1:
                // "allChangedInFolderDateAsc"
                return allChangedInFolderDate(cms, param, false, true, numResults);
            case 2:
                // "allChangedInSubTreeDateDesc"
                return allChangedInFolderDate(cms, param, true, false, numResults);
            case 3:
                // "allChangedInSubTreeDateAsc"
                return allChangedInFolderDate(cms, param, true, true, numResults);
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * Returns a List of all changed resources in the folder pointed to by the parameter
     * sorted by the date attributes specified in the parameter.<p>
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
    protected List<CmsResource> allChangedInFolderDate(
        CmsObject cms,
        String param,
        boolean tree,
        boolean asc,
        int numResults) throws CmsException {

        Map<String, String> params = getParameters(param);

        String foldername = "/";
        if (params.containsKey(PARAM_KEY_RESOURCE)) {
            foldername = CmsResource.getFolderPath(params.get(PARAM_KEY_RESOURCE));
        }

        long dateFrom = 0L;
        long dateTo = Long.MAX_VALUE;
        if (params.containsKey(PARAM_KEY_DATEFROM)) {
            try {
                dateFrom = Long.parseLong(params.get(PARAM_KEY_DATEFROM));
            } catch (NumberFormatException e) {
                // error parsing from date
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                        PARAM_KEY_DATEFROM + "=" + params.get(PARAM_KEY_DATEFROM)));
                throw e;
            }
        }
        if (params.containsKey(PARAM_KEY_DATETO)) {
            try {
                dateTo = Long.parseLong(params.get(PARAM_KEY_DATETO));
            } catch (NumberFormatException e) {
                // error parsing to date
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                        PARAM_KEY_DATETO + "=" + params.get(PARAM_KEY_DATETO)));
                throw e;
            }
        }

        // create the filter to read the resources
        CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addExcludeFlags(
            CmsResource.FLAG_TEMPFILE).addRequireLastModifiedAfter(dateFrom).addRequireLastModifiedBefore(dateTo);

        // check if a resource type has to be excluded
        if (params.containsKey(PARAM_KEY_EXCLUDETYPE)) {
            String excludeType = params.get(PARAM_KEY_EXCLUDETYPE);
            int typeId = -1;
            try {
                // try to look up the resource type
                I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(excludeType);
                typeId = resourceType.getTypeId();
            } catch (CmsLoaderException e1) {
                // maybe the int ID is directly used?
                try {
                    int typeInt = Integer.parseInt(excludeType);
                    I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(typeInt);
                    typeId = resourceType.getTypeId();
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_RESTYPE_INTID_2,
                                resourceType.getTypeName(),
                                Integer.valueOf(resourceType.getTypeId())));
                    }
                } catch (NumberFormatException e2) {
                    // bad number format used for type
                    throw new CmsRuntimeException(
                        Messages.get().container(
                            Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                            PARAM_KEY_EXCLUDETYPE + "=" + params.get(PARAM_KEY_EXCLUDETYPE)),
                        e2);
                } catch (CmsLoaderException e2) {
                    // this resource type does not exist
                    throw new CmsRuntimeException(
                        Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_1, excludeType),
                        e2);
                }
            }
            if (typeId != -1) {
                filter = filter.addExcludeType(typeId);
            }
        }

        // read the resources using the configured filter
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        // determine which attribute should be used to sort the result
        String sortBy = CmsDateResourceComparator.DATE_ATTRIBUTES_LIST.get(1);
        if (params.containsKey(PARAM_KEY_SORTBY)) {
            sortBy = params.get(PARAM_KEY_SORTBY);
        }
        List<String> dateIdentifiers = new ArrayList<String>(1);
        dateIdentifiers.add(sortBy);

        // a special date comparator is used to sort the resources
        CmsDateResourceComparator comparator = new CmsDateResourceComparator(cms, dateIdentifiers, asc);
        Collections.sort(result, comparator);

        int count = -1;
        if (params.containsKey(PARAM_KEY_COUNT)) {
            try {
                count = Integer.parseInt(params.get(PARAM_KEY_COUNT));
            } catch (NumberFormatException e) {
                // error parsing the count
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_COLLECTOR_PARAM_INVALID_1,
                        PARAM_KEY_COUNT + "=" + params.get(PARAM_KEY_COUNT)));
                throw e;
            }
        }
        if ((count > 0) || (numResults > 0)) {
            return shrinkToFit(result, count, numResults);
        } else {
            return result;
        }
    }

    /**
     * Returns the collector parameters.<p>
     *
     * @param param the collector parameter
     *
     * @return the collector parameters
     */
    private Map<String, String> getParameters(String param) {

        if (CmsStringUtil.isNotEmpty(param)) {
            return CmsStringUtil.splitAsMap(param, "|", "=");
        }
        return Collections.emptyMap();
    }
}