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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A collector to fetch sorted XML contents in a folder or subtree based on their priority
 * and date or title values.<p>
 *
 * The date or title information has to be stored as property for each resource.<p>
 *
 * @since 6.0.0
 */
public class CmsPriorityResourceCollector extends A_CmsResourceCollector {

    /** The standard priority value if no value was set on resource. */
    public static final int PRIORITY_STANDARD = 3;

    /** The name of the channel property to read. */
    public static final String PROPERTY_CHANNEL = "collector.channel";

    /** The name of the priority property to read. */
    public static final String PROPERTY_PRIORITY = "collector.priority";

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {
        "allInFolderPriorityDateAsc",
        "allInSubTreePriorityDateAsc",
        "allInFolderPriorityDateDesc",
        "allInSubTreePriorityDateDesc",
        "allInFolderPriorityTitleDesc",
        "allInSubTreePriorityTitleDesc",
        "allMappedToUriPriorityDateAsc",
        "allMappedToUriPriorityDateDesc"};

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
    public String getCreateLink(CmsObject cms, String collectorName, String param) throws CmsException {

        // if action is not set, use default action
        if (collectorName == null) {
            collectorName = COLLECTORS[1];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
            case 2:
            case 4:
                // "allInFolderPriorityDateAsc", "allInFolderPriorityDateDesc" or "allInFolderPriorityTitleDesc"
                return getCreateInFolder(cms, param);
            case 1:
            case 3:
            case 5:
            case 6:
            case 7:
                // "allInSubTreePriorityDateAsc", "allInSubTreePriorityDateDesc" or "allInSubTreePriorityTitleDesc"
                // "allMappedToUriPriorityDateAsc", "allMappedToUriPriorityDateDesc"
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
            collectorName = COLLECTORS[1];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
            case 2:
            case 4:
                // "allInFolderPriorityDateAsc", "allInFolderPriorityDateDesc" or "allInFolderPriorityTitleDesc"
                return param;
            case 1:
            case 3:
            case 5:
            case 6:
            case 7:
                // "allInSubTreePriorityDateAsc", "allInSubTreePriorityDateDesc" or "allInSubTreePriorityTitleDesc"
                // "allMappedToUriPriorityDateAsc", "allMappedToUriPriorityDateDesc"
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
            result = new CmsCollectorData(param).getType();
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
    throws CmsException, CmsDataAccessException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {

            case 0:
                // "allInFolderPriorityDateAsc"
                return allInFolderPriorityDate(cms, param, false, true, numResults);
            case 1:
                // "allInSubTreePriorityDateAsc"
                return allInFolderPriorityDate(cms, param, true, true, numResults);
            case 2:
                // "allInFolderPriorityDateDesc"
                return allInFolderPriorityDate(cms, param, false, false, numResults);
            case 3:
                // "allInSubTreePriorityDateDesc"
                return allInFolderPriorityDate(cms, param, true, false, numResults);
            case 4:
                // "allInFolderPriorityTitleDesc"
                return allInFolderPriorityTitle(cms, param, false, numResults);
            case 5:
                // "allInSubTreePriorityTitleDesc"
                return allInFolderPriorityTitle(cms, param, true, numResults);
            case 6:
                // "allMappedToUriPriorityDateAsc"
                return allMappedToUriPriorityDate(cms, param, true, numResults);
            case 7:
                // "allMappedToUriPriorityDateDesc"
                return allMappedToUriPriorityDate(cms, param, false, numResults);
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }

    }

    /**
     * Returns a list of all resource in a specified folder sorted by priority, then date ascending or descending.<p>
     *
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param asc if true, the date sort order is ascending, otherwise descending
     * @param numResults the number of results
     *
     * @return all resources in the folder matching the given criteria
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allInFolderPriorityDate(
        CmsObject cms,
        String param,
        boolean tree,
        boolean asc,
        int numResults) throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        // create priority comparator to use to sort the resources
        CmsPriorityDateResourceComparator comparator = new CmsPriorityDateResourceComparator(cms, asc);
        Collections.sort(result, comparator);

        return shrinkToFit(result, data.getCount(), numResults);
    }

    /**
     * Returns a list of all resource in a specified folder sorted by priority descending, then Title ascending.<p>
     *
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param numResults the number of results
     *
     * @return all resources in the folder matching the given criteria
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allInFolderPriorityTitle(CmsObject cms, String param, boolean tree, int numResults)
    throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        // create priority comparator to use to sort the resources
        CmsPriorityTitleResourceComparator comparator = new CmsPriorityTitleResourceComparator(cms);
        Collections.sort(result, comparator);

        return shrinkToFit(result, data.getCount(), numResults);
    }

    /**
     * Returns a list of all resource from specified folder that have been mapped to
     * the currently requested uri, sorted by priority, then date ascending or descending.<p>
     *
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param asc if true, the date sort order is ascending, otherwise descending
     * @param numResults the number of results
     *
     * @return all resources in the folder matching the given criteria
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allMappedToUriPriorityDate(CmsObject cms, String param, boolean asc, int numResults)
    throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }

        List<CmsResource> result = cms.readResources(foldername, filter, true);
        List<CmsResource> mapped = new ArrayList<CmsResource>();

        // sort out the resources mapped to the current page
        Iterator<CmsResource> i = result.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
            // read all properties - reason: comparator will do this later anyway, so we just prefill the cache
            CmsProperty prop = cms.readPropertyObject(res, PROPERTY_CHANNEL, false);
            if (!prop.isNullProperty()) {
                if (CmsProject.isInsideProject(
                    prop.getValueList(),
                    cms.getRequestContext().getSiteRoot() + cms.getRequestContext().getUri())) {
                    mapped.add(res);
                }
            }
        }

        if (mapped.isEmpty()) {
            // nothing was mapped, no need for further processing
            return mapped;
        }

        // create priority comparator to use to sort the resources
        CmsPriorityDateResourceComparator comparator = new CmsPriorityDateResourceComparator(cms, asc);
        Collections.sort(mapped, comparator);

        return shrinkToFit(mapped, data.getCount(), numResults);
    }
}