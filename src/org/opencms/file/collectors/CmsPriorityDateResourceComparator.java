/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/CmsPriorityDateResourceComparator.java,v $
 * Date   : $Date: 2005/06/23 11:11:58 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator for sorting resource objects based on priority and date.<p>
 * 
 * Serves as {@link java.util.Comparator} for resources and as comparator key for the resource
 * at the same time. Uses lazy initializing of comparator keys in a resource.<p>
 * 
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 6.0.0 
 */
public class CmsPriorityDateResourceComparator implements Comparator {

    /** The name of the date property to read. */
    public static final String C_PROPERTY_DATE = "collector.date";

    /** The date sort order. */
    private boolean m_asc;

    /** The current OpenCms user context. */
    private CmsObject m_cms;

    /** The date of this comparator key. */
    private long m_date;

    /** The interal map of comparator keys. */
    private Map m_keys;

    /** The priority of this comparator key. */
    private int m_priority;

    /**
     * Creates a new instance of this comparator key.<p>
     * 
     * @param cms the current OpenCms user context
     * @param asc if true, the date sort order is ascending, otherwise descending
     */
    public CmsPriorityDateResourceComparator(CmsObject cms, boolean asc) {

        m_cms = cms;
        m_asc = asc;
        m_keys = new HashMap();
    }

    /**
     * Creates a new instance of this comparator key.<p>
     * 
     * @param resource the resource to create the key for
     * @param cms the current OpenCms user context
     * 
     * @return a new instance of this comparatoy key
     */
    private static CmsPriorityDateResourceComparator create(CmsResource resource, CmsObject cms) {

        CmsPriorityDateResourceComparator result = new CmsPriorityDateResourceComparator(null, false);
        result.init(resource, cms);
        return result;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1) {

        if (!(arg0 instanceof CmsResource) || !(arg1 instanceof CmsResource)) {
            return 0;
        }

        CmsResource res0 = (CmsResource)arg0;
        CmsResource res1 = (CmsResource)arg1;

        CmsPriorityDateResourceComparator key0 = (CmsPriorityDateResourceComparator)m_keys.get(res0.getStructureId());
        CmsPriorityDateResourceComparator key1 = (CmsPriorityDateResourceComparator)m_keys.get(res1.getStructureId());

        if (key0 == null) {
            // initialize key if null
            key0 = CmsPriorityDateResourceComparator.create(res0, m_cms);
            m_keys.put(res0.getStructureId(), key0);
        }
        if (key1 == null) {
            // initialize key if null
            key1 = CmsPriorityDateResourceComparator.create(res1, m_cms);
            m_keys.put(res1.getStructureId(), key1);
        }

        // check priority
        if (key0.getPriority() > key1.getPriority()) {
            return -1;
        }

        if (key0.getPriority() < key1.getPriority()) {
            return 1;
        }

        if (m_asc) {
            // sort in ascending order
            if (key0.getDate() > key1.getDate()) {
                return 1;
            }
            if (key0.getDate() < key1.getDate()) {
                return -1;
            }
        } else {
            // sort in descending order
            if (key0.getDate() > key1.getDate()) {
                return -1;
            }
            if (key0.getDate() < key1.getDate()) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Returns the date of this resource comparator key.<p>
     * 
     * @return the date of this resource comparator key
     */
    public long getDate() {

        return m_date;
    }

    /**
     * Returns the priority of this resource comparator key.<p>
     * 
     * @return the priority of this resource comparator key
     */
    public int getPriority() {

        return m_priority;
    }

    /**
     * Initializes the comparator key based on the member variables.<p> 
     * 
     * @param resource the resource to use 
     * @param cms the current OpenCms user contxt
     */
    private void init(CmsResource resource, CmsObject cms) {

        Map properties = null;

        try {
            properties = CmsProperty.toMap(cms.readPropertyObjects(cms.getRequestContext().removeSiteRoot(
                resource.getRootPath()), false));
        } catch (CmsException e) {
            m_priority = 0;
            m_date = 0;
            return;
        }

        try {
            m_priority = Integer.parseInt((String)properties.get(CmsPriorityResourceCollector.C_PROPERTY_PRIORITY));
        } catch (NumberFormatException e) {
            m_priority = CmsPriorityResourceCollector.C_PRIORITY_STANDARD;
        }

        try {
            m_date = Long.parseLong((String)properties.get(C_PROPERTY_DATE));
        } catch (NumberFormatException e) {
            m_date = 0;
        }
    }

}