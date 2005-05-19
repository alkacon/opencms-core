/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/CmsPriorityResourceCollector.java,v $
 * Date   : $Date: 2005/05/19 09:54:29 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A collector to fetch sorted XML contents in a folder or subtree based on their priority
 * and date or title values.<p>
 * 
 * The date or title information has to be stored as property for each resource.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 5.7.2
 */
public class CmsPriorityResourceCollector extends A_CmsResourceCollector {

    /** The standard priority value if no value was set on resource. */
    public static final int C_PRIORITY_STANDARD = 3;

    /** The name of the priority property to read. */
    public static final String C_PROPERTY_PRIORITY = "collector.priority";

    /** Static array of the collectors implemented by this class. */
    private static final String[] COLLECTORS = {
        "allInFolderPriorityDateAsc",
        "allInSubTreePriorityDateAsc",
        "allInFolderPriorityDateDesc",
        "allInSubTreePriorityDateDesc",
        "allInFolderPriorityTitleDesc",
        "allInSubTreePriorityTitleDesc"};

    /** Array list for fast collector name lookup. */
    private static final List COLLECTORS_LIST = Collections.unmodifiableList(Arrays.asList(COLLECTORS));

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List getCollectorNames() {

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
                // "allInSubTreePriorityDateAsc", "allInSubTreePriorityDateDesc" or "allInSubTreePriorityTitleDesc"
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
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
                // "allInSubTreePriorityDateAsc", "allInSubTreePriorityDateDesc" or "allInSubTreePriorityTitleDesc"
                return null;
            default:
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public List getResults(CmsObject cms, String collectorName, String param) throws CmsException, CmsDataAccessException {

        // if action is not set use default
        if (collectorName == null) {
            collectorName = COLLECTORS[0];
        }

        switch (COLLECTORS_LIST.indexOf(collectorName)) {

            case 0:
                // "allInFolderPriorityDateAsc"
                return allInFolderPriorityDate(cms, param, false, true);
            case 1:
                // "allInSubTreePriorityDateAsc"
                return allInFolderPriorityDate(cms, param, true, true);
            case 2:
                // "allInFolderPriorityDateDesc"
                return allInFolderPriorityDate(cms, param, false, false);
            case 3:
                // "allInSubTreePriorityDateDesc"
                return allInFolderPriorityDate(cms, param, true, false);
            case 4:
                // "allInFolderPriorityTitleDesc"
                return allInFolderPriorityTitle(cms, param, false);
            case 5:
                // "allInSubTreePriorityTitleDesc"
                return allInFolderPriorityTitle(cms, param, true);
            default:
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_COLLECTOR_NAME_INVALID_1, collectorName));
        }

    }

    /**
     * Returns a list of all resource in a specified folder sorted by priority, then date ascending or descending.<p>
     * 
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * @param asc if true, the date sort order is ascending, otherwise descending
     * 
     * @return all resources in the folder matching the given criteria
     * 
     * @throws CmsException if something goes wrong
     */
    protected List allInFolderPriorityDate(CmsObject cms, String param, boolean tree, boolean asc) throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType());
        List result = cms.readResources(foldername, filter, tree);

        // create priority comparator to use to sort the resources
        CmsPriorityDateResourceComparator comparator = new CmsPriorityDateResourceComparator(cms, asc);
        Collections.sort(result, comparator);

        return shrinkToFit(result, data.getCount());
    }

    /**
     * Returns a list of all resource in a specified folder sorted by priority descending, then Title ascending.<p>
     * 
     * @param cms the current OpenCms user context
     * @param param the folder name to use
     * @param tree if true, look in folder and all child folders, if false, look only in given folder
     * 
     * @return all resources in the folder matching the given criteria
     * 
     * @throws CmsException if something goes wrong
     */
    protected List allInFolderPriorityTitle(CmsObject cms, String param, boolean tree) throws CmsException {

        CmsCollectorData data = new CmsCollectorData(param);
        String foldername = CmsResource.getFolderPath(data.getFileName());

        CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addRequireType(data.getType());
        List result = cms.readResources(foldername, filter, tree);

        // create priority comparator to use to sort the resources
        CmsPriorityTitleResourceComparator comparator = new CmsPriorityTitleResourceComparator(cms);
        Collections.sort(result, comparator);

        return shrinkToFit(result, data.getCount());
    }
}