/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResourceFilter.java,v $
 * Date   : $Date: 2004/06/05 10:11:05 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.file;

import org.opencms.main.I_CmsConstants;

/**
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsResourceFilter {
    
    /** 
     * Filter to display all resources.<p>
     * 
     * This filter uses the following rules:
     * <ul>
     * <li>Includes: Resources marked as deleted.</li>
     * <li>Includes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Includes: Resources marked as 'invisible' using permissions.</li>
     * </ul>
     */
    public static CmsResourceFilter ALL = new CmsResourceFilter(true, true, false);
    
    /** 
     * Default Filter to display resources.<p>
     * 
     * This filter uses the following rules:
     * <ul>
     * <li>Excludes: Resources marked as deleted.</li>
     * <li>Excludes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Includes: Resources marked as 'invisible' using permissions.</li>
     * </ul> 
     */
    public static CmsResourceFilter DEFAULT = new CmsResourceFilter(false, false, false);
      
    /** 
     * Filter to display resources ignoring the release and expiration dates.<p>
     * 
     * This filter uses the following rules:
     * <ul>
     * <li>Excludes: Resources marked as deleted.</li>
     * <li>Includes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Includes: Resources marked as 'invisible' using permissions.</li>
     * </ul> 
     */
    public static CmsResourceFilter IGNORE_EXPIRATION = new CmsResourceFilter(false, true, false);

    /** 
     * Filter to display only visible resources.<p>
     * 
     * This filter used the following rules:
     * <ul>
     * <li>Includes: Resources marked as deleted.</li>
     * <li>Includes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Excludes: Resources marked as 'invisible' using permissions.</li>
     * </ul> 
     */
    public static CmsResourceFilter ONLY_VISIBLE = new CmsResourceFilter(true, true, true);
    
    /** Flag for filtering deleted resources */
    private boolean m_includeDeleted;
    
    /** Flag for filtering resources before relase date and after expiration date */
    private boolean m_includeUnreleased;
    
    /** Flag to filter resources with the visible permission */
    private boolean m_includeVisiblePermission;    
    
    /**
     * Creates a new CmsResourceFilter.<p>
     */
    public CmsResourceFilter() {
        
        this (false, false, false);
    }
    
    /**
     * Creates a new CmsResourceFilter.<p>
     * 
     * @param includeDeleted flag for filtering deleted resources
     * @param includeUnreleased flag for filtering resources before relase date and after expiration date
     * @param includeVisiblePermission flag to filter resources with the visible permission
     */
    public CmsResourceFilter(boolean includeDeleted, boolean includeUnreleased, boolean includeVisiblePermission) {
        
        m_includeDeleted = includeDeleted;
        m_includeUnreleased = includeUnreleased;
        m_includeVisiblePermission = includeVisiblePermission;       
    }

    /**
     * Check if deleted resources should be filtered.<p>
     * 
     * @return true if deleted resources should be included, false otherwiese.
     */
    public boolean includeDeleted() {
        
        return m_includeDeleted;
    }
    
    /**
     * Check if resources before release date and after expireing date should be filtered.<p>
     * 
     * @return true if resources before release date and after expireing date should be included, false otherwiese.
     */
    public boolean includeUnreleased() {
        
        return m_includeUnreleased;
    }
    
    /**
     * Check if resources with the visible permission should be filtered.<p>
     * 
     * @return true if resources with the visible permission should be included, false otherwiese.
     */
    public boolean includeVisiblePermission() {
        
        return m_includeVisiblePermission;
    }
 
    /**
     * Validates if a CmsResource fits to all filer settings.<p>
     *
     * @param context the current request context
     * @param resource the resource to be validated
     * @return true if the resource passes all validations, false otherwise
     */
    public boolean isValid(CmsRequestContext context, CmsResource resource) {
        
        // check if the resource is marked as deleted and the include deleted flag is set
        if (!m_includeDeleted && (resource.getState() == I_CmsConstants.C_STATE_DELETED)) {
            return false;
        }
        // check if the resource is within the valid time frame
        if (!m_includeUnreleased && ((resource.getDateReleased() > context.getRequestTime()) || (resource.getDateExpired() < context.getRequestTime()))) {
            return false;
        }
        
        // everything is ok, so return true
        return true;
    }
}
