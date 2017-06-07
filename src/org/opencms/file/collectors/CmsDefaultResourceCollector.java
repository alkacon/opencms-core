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
import org.opencms.file.I_CmsResource;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A default resource collector to generate some example list of resources from the VFS.<p>
 *
 * @since 6.0.0
 */
public class CmsDefaultResourceCollector extends A_CmsResourceCollector {

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {
        "singleFile",
        "allInFolder",
        "allInFolderDateReleasedDesc",
        "allInFolderNavPos",
        "allInSubTree",
        "allInSubTreeDateReleasedDesc",
        "allInSubTreeNavPos"};

    /** Array list for fast collector name lookup. */
    private static final List<String> COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultResourceCollector.class);

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
                // "singleFile"
                return null;
            case 1:
                // "allInFolder"
            case 2:
                // "allInFolderDateReleasedDesc"
            case 3:
                // "allInFolderNavPos"
                return getCreateInFolder(cms, param);
            case 4:
                // "allInSubTree"
            case 5:
                // "allInSubTreeDateReleasedDesc"
            case 6:
                // "allInSubTreeNavPos"
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
                // "singleFile"
                return null;
            case 1:
                // "allInFolder"
            case 2:
                // "allInFolderDateReleasedDesc"
            case 3:
                // "allInFolderNavPos"
                return param;
            case 4:
                // "allInSubTree"
            case 5:
                // "allInSubTreeDateReleasedDesc"
            case 6:
                // "allInSubTreeNavPos"
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
    throws CmsDataAccessException, CmsException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {
            case 0:
                // "singleFile"
                return getSingleFile(cms, param);
            case 1:
                // "allInFolder"
                return getAllInFolder(cms, param, false, numResults);
            case 2:
                // "allInFolderDateReleasedDesc"
                return allInFolderDateReleasedDesc(cms, param, false, numResults);
            case 3:
                // allInFolderNavPos"
                return allInFolderNavPos(cms, param, false, numResults);
            case 4:
                // "allInSubTree"
                return getAllInFolder(cms, param, true, numResults);
            case 5:
                // "allInSubTreeDateReleasedDesc"
                return allInFolderDateReleasedDesc(cms, param, true, numResults);
            case 6:
                // "allInSubTreeNavPos"
                return allInFolderNavPos(cms, param, true, numResults);
            default:
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * Returns a List of all resources in the folder pointed to by the parameter
     * sorted by the release date, descending.<p>
     *
     * @param cms the current CmsObject
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param numResults the number of results
     *
     * @return a List of all resources in the folder pointed to by the parameter
     *      sorted by the release date, descending
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> allInFolderDateReleasedDesc(CmsObject cms, String param, boolean tree, int numResults)
    throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        Collections.sort(result, I_CmsResource.COMPARE_DATE_RELEASED);

        return shrinkToFit(result, data.getCount(), numResults);
    }

    /**
     * Collects all resources in a folder (or subtree) sorted by the NavPos property.<p>
     *
     * @param cms the current user's Cms object
     * @param param the collector's parameter(s)
     * @param readSubTree if true, collects all resources in the subtree
     * @param numResults the number of results
     * @return a List of Cms resources found by the collector
     * @throws CmsException if something goes wrong
     *
     */
    protected List<CmsResource> allInFolderNavPos(CmsObject cms, String param, boolean readSubTree, int numResults)
    throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> foundResources = cms.readResources(foldername, filter, readSubTree);

        // the Cms resources are saved in a map keyed by their nav elements
        // to save time sorting the resources by the value of their NavPos property
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
        Map<CmsJspNavElement, CmsResource> navElementMap = new HashMap<CmsJspNavElement, CmsResource>();
        for (int i = 0, n = foundResources.size(); i < n; i++) {

            CmsResource resource = foundResources.get(i);
            CmsJspNavElement navElement = navBuilder.getNavigationForResource(cms.getSitePath(resource));

            // check if the resource has the NavPos property set or not
            if ((navElement != null) && (navElement.getNavPosition() != Float.MAX_VALUE)) {
                navElementMap.put(navElement, resource);
            } else if (LOG.isInfoEnabled()) {
                // printing a log messages makes it a little easier to identify
                // resources having not the NavPos property set
                LOG.info(
                    Messages.get().getBundle().key(Messages.LOG_RESOURCE_WITHOUT_NAVPROP_1, cms.getSitePath(resource)));
            }
        }

        // all found resources have the NavPos property set
        // sort the nav. elements, and pull the found Cms resources
        // from the map in the correct order into a list
        // only resources with the NavPos property set are used here
        List<CmsJspNavElement> navElementList = new ArrayList<CmsJspNavElement>(navElementMap.keySet());
        List<CmsResource> result = new ArrayList<CmsResource>();

        Collections.sort(navElementList);
        for (int i = 0, n = navElementList.size(); i < n; i++) {

            CmsJspNavElement navElement = navElementList.get(i);
            result.add(navElementMap.get(navElement));
        }

        return shrinkToFit(result, data.getCount(), numResults);
    }

    /**
     * Returns all resources in the folder pointed to by the parameter.<p>
     *
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param numResults the number of results
     *
     * @return all resources in the folder matching the given criteria
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the given param argument is not a link to a single file
     *
     */
    protected List<CmsResource> getAllInFolder(CmsObject cms, String param, boolean tree, int numResults)
    throws CmsException, CmsIllegalArgumentException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT_FILES.addRequireType(data.getType()).addExcludeFlags(
            CmsResource.FLAG_TEMPFILE);
        if (data.isExcludeTimerange() && !cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // include all not yet released and expired resources in an offline project
            filter = filter.addExcludeTimerange();
        }
        List<CmsResource> result = cms.readResources(foldername, filter, tree);

        Collections.sort(result, I_CmsResource.COMPARE_ROOT_PATH);
        Collections.reverse(result);

        return shrinkToFit(result, data.getCount(), numResults);
    }

    /**
     * Returns a List containing the resources pointed to by the parameter.<p>
     *
     * @param cms the current CmsObject
     * @param param the name of the file to load
     *
     * @return a List containing the resources pointed to by the parameter
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getSingleFile(CmsObject cms, String param) throws CmsException {

        if ((param == null) || (cms == null)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_COLLECTOR_PARAM_SINGLE_FILE_0));
        }

        // create a list and return it
        List<CmsResource> result = new ArrayList<CmsResource>(1);
        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            result.add(cms.readFile(param));
        } else {
            // ignore release and expiration date in offline projects to be able to preview file
            result.add(cms.readFile(param, CmsResourceFilter.IGNORE_EXPIRATION));
        }
        return result;
    }
}