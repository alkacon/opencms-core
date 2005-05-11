/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/CmsPriorityTitleResourceComparator.java,v $
 * Date   : $Date: 2005/05/11 10:58:19 $
 * Version: $Revision: 1.5 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsStringUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator for sorting resource objects based on priority and title.<p>
 * 
 * Serves as {@link java.util.Comparator} for resources and as comparator key for the resource
 * at the same time. Uses lazy initializing of comparator keys in a resource.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 5.7.2 
 * 
 */
public class CmsPriorityTitleResourceComparator implements Comparator {

    /** The current OpenCms user context. */
    private CmsObject m_cms;

    /** The title of this comparator key. */
    private String m_title;
    
    /** The interal map of comparator keys. */
    private Map m_keys;

    /** The priority of this comparator key. */
    private int m_priority;

    /**
     * Creates a new instance of this comparator key.<p>
     * 
     * @param cms the current OpenCms user context
     */
    public CmsPriorityTitleResourceComparator(CmsObject cms) {

        m_cms = cms;
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
    private static CmsPriorityTitleResourceComparator create(CmsResource resource, CmsObject cms) {

        CmsPriorityTitleResourceComparator result = new CmsPriorityTitleResourceComparator(null);
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
        
        CmsPriorityTitleResourceComparator key0 = (CmsPriorityTitleResourceComparator)m_keys.get(res0.getStructureId());
        CmsPriorityTitleResourceComparator key1 = (CmsPriorityTitleResourceComparator)m_keys.get(res1.getStructureId());

        if (key0 == null) {
            // initialize key if null
            key0 = CmsPriorityTitleResourceComparator.create(res0, m_cms);
            m_keys.put(res0.getStructureId(), key0);
        }
        if (key1 == null) {
            // initialize key if null
            key1 = CmsPriorityTitleResourceComparator.create(res1, m_cms);
            m_keys.put(res1.getStructureId(), key1);
        }

        if (key0.getPriority() > key1.getPriority()) {
            return -1;
        }

        if (key0.getPriority() < key1.getPriority()) {
            return 1;
        }

        return key0.getTitle().compareTo(key1.getTitle());
    }

    /**
     * Returns the title of this resource comparator key.<p>
     * 
     * @return the title of this resource comparator key
     */
    public String getTitle() {

        return m_title;
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
            properties = CmsProperty.toMap(
                cms.readPropertyObjects(
                    cms.getRequestContext().removeSiteRoot(resource.getRootPath()), false));
        } catch (CmsException e) {
            m_priority = 0;
            m_title = "";
            return;
        }

        try {
            m_priority = Integer.parseInt((String)properties.get(CmsPriorityResourceCollector.C_PROPERTY_PRIORITY));
        } catch (NumberFormatException e) {
            m_priority = CmsPriorityResourceCollector.C_PRIORITY_STANDARD;
        }

        m_title = (String)properties.get(I_CmsConstants.C_PROPERTY_TITLE);
        if (CmsStringUtil.isEmpty(m_title)) {
            m_title = "";
        }
       
    }

}