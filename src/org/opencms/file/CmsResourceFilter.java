/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResourceFilter.java,v $
 * Date   : $Date: 2004/06/14 14:25:57 $
 * Version: $Revision: 1.5 $
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
 * Provides filters for resource result sets obtained from requests to the VFS.<p>
 * 
 * Using the constant filters provided by this class
 * you can control "special" behaviour 
 * of access to the VFS. For example, in the "Offline" project 
 * there can be deleted files, by using this filter you can control
 * if deleted files should be included in a result set or not.<p> 
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.3.5
 */
public final class CmsResourceFilter {
    
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
    public static CmsResourceFilter ALL = new CmsResourceFilter(true, true, true);
    
    /** 
     * Default filter to display resources for the online project.<p>
     * 
     * This filter uses the following rules:
     * <ul>
     * <li>Excludes: Resources marked as deleted.</li>
     * <li>Excludes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Includes: Resources marked as 'invisible' using permissions.</li>
     * </ul> 
     */
    public static CmsResourceFilter DEFAULT = new CmsResourceFilter(false, false, true);
      
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
    public static CmsResourceFilter IGNORE_EXPIRATION = new CmsResourceFilter(false, true, true);

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
    public static CmsResourceFilter ONLY_VISIBLE = new CmsResourceFilter(true, true, false);
    
    /** The cache id for this filter. */
    private String m_cacheId;
    
    /** Flag for filtering deleted resources. */
    private boolean m_includeDeleted;
    
    /** Flag to filter resources with the visible permission. */
    private boolean m_includeInvisible;
    
    /** Flag for filtering resources before relase date and after expiration date. */
    private boolean m_includeUnreleased;
    
    
    /**
     * Hides the public contructor.<p>
     */
    private CmsResourceFilter() {
        // noop
    }
    
    /**
     * Creates a new CmsResourceFilter.<p>
     * 
     * @param includeDeleted flag for filtering deleted resources
     * @param includeUnreleased flag for filtering resources before relase date and after expiration date
     * @param includeInvisible flag to filter resources with the visible permission
     */
    private CmsResourceFilter(boolean includeDeleted, boolean includeUnreleased, boolean includeInvisible) {
        
        m_includeDeleted = includeDeleted;
        m_includeUnreleased = includeUnreleased;
        m_includeInvisible = includeInvisible;
        
        m_cacheId = String.valueOf(
            (m_includeDeleted?1:0) 
            + (m_includeUnreleased?2:0) 
            + (m_includeInvisible?4:0)
        );
    }
    
    /**
     * Returns the unique cache id for this filter.<p>
     * 
     * @return the unique cache id for this filter
     */
    public String getCacheId() {
        
        return m_cacheId;
    }

    /**
     * Check if deleted resources should be filtered.<p>
     * 
     * @return true if deleted resources should be included, false otherwiese
     */
    public boolean includeDeleted() {
        
        return m_includeDeleted;
    }
    
    /**
     * Check if the visible permission should be ignored for resources.<p>
     * 
     * @return true if the visible permission should be ignored for resources
     */
    public boolean includeInvisible() {
        
        return m_includeInvisible;
    }
    
    /**
     * Check if resources before release date and after expireing date should be filtered.<p>
     * 
     * @return true if resources before release date and after expireing date should be included, false otherwiese
     */
    public boolean includeUnreleased() {
        
        return m_includeUnreleased;
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
