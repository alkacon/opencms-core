/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResourceFilter.java,v $
 * Date   : $Date: 2004/08/20 11:44:14 $
 * Version: $Revision: 1.7 $
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

import org.opencms.file.types.CmsResourceTypeFolder;
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
 * @author Carsten Weinholz (c.weinholz@alkaconc.om)
 * 
 * @version $Revision: 1.7 $
 * @since 5.3.5
 */
public final class CmsResourceFilter {
    
    /** Mode flag to indicate that the given type should be excluded. */
    public static int C_FILTER_EXCLUDE_TYPE = 1;
    
    /** Mode flag to indicate that the given state should be excluded. */
    public static int C_FILTER_EXCLUDE_STATE = 2;
    
    /** Mode flag to filter for visible resources. */
    public static int C_FILTER_REQUIRE_VISIBLE = 4;
    
    /** Mode flag to filter for valid resources. */
    public static int C_FILTER_REQUIRE_VALID = 8;
    
    /** Mode flag to require immediate children only. */
    public static int C_FILTER_REQUIRE_CHILDS = 16;
    
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
    public static final CmsResourceFilter ALL = new CmsResourceFilter(); 

    /**
     * Filter to display all modified (new/changed/deleted) resources.<p>
     */
    public static final CmsResourceFilter ALL_MODIFIED = ALL.addExcludeState(I_CmsConstants.C_STATE_UNCHANGED);
    
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
    public static final CmsResourceFilter DEFAULT = ALL.addExcludeState(I_CmsConstants.C_STATE_DELETED).addRequireValid();
    
    /**
     * Default filter to display folders for the online project.<p>
     */
    public static final CmsResourceFilter DEFAULT_FOLDERS = DEFAULT.addRequireType(CmsResourceTypeFolder.C_RESOURCE_TYPE_ID); 
    
    /**
     * Default filter to display files for the online project.<p>
     */
    public static final CmsResourceFilter DEFAULT_FILES = DEFAULT.addExcludeType(CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
    
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
    public static final CmsResourceFilter IGNORE_EXPIRATION = ALL.addExcludeState(I_CmsConstants.C_STATE_DELETED);    

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
    public static final CmsResourceFilter ONLY_VISIBLE = ALL.addRequireVisible();
    
    
    /** The cache id for this filter. */
    private String m_cacheId;
    
    /** The filter mode to define whats required/excluded. */
    private int m_mode;
    
    /** The required/excluded type for filtering resources. */
    private int m_type;
    
    /** The required/excluded state for filtering resources. */
    private int m_state;

    /** The required start date for modifications. */
    private long m_modifiedAfter;
    
    /** The required end data for modifications. */
    private long m_modifiedBefore;
    
    /**
     * Recalculates the cache id.<p>
     */
    private void updateCacheId() {
        m_cacheId = 
            "M" + String.valueOf(m_mode) 
            + "T" + String.valueOf(m_type) 
            + "S" + String.valueOf(m_state) 
            + "A" + String.valueOf(m_modifiedAfter) 
            + "B" + String.valueOf(m_modifiedBefore);    
    }
    
    /**
     * Hides the public contructor.<p>
     */
    private CmsResourceFilter() {
        m_mode = 0;
        m_type = -1;
        m_state = -1;
        m_modifiedAfter = 0L;
        m_modifiedBefore = 0L;
        updateCacheId();
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        CmsResourceFilter filter = new CmsResourceFilter();
        filter.m_mode = m_mode;
        filter.m_type = m_type;
        filter.m_state = m_state;
        filter.m_modifiedAfter = m_modifiedAfter;
        filter.m_modifiedBefore = m_modifiedBefore;
        filter.m_cacheId = m_cacheId;       
        return filter;
    }
    
    /**
     * Returns a new CmsResourceFilter requiring the given type.<p>
     * 
     * @param type the required resource type
     * @return a filter requiring the given type
     */
    public static CmsResourceFilter requireType(int type) {
        return new CmsResourceFilter().addRequireType(type);
    }
    
    /**
     * Returns an extended filter to guarantee a distinct resource type of the filtered resources.<p>
     * 
     * @param type the required resource type
     * @return a filter requiring the given resource type
     */
    public CmsResourceFilter addRequireType(int type) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_type = type;
        extendedFilter.m_mode &= ~C_FILTER_EXCLUDE_TYPE;
        extendedFilter.updateCacheId();
        
        return extendedFilter;
    }
    
    /**
     * Returns an extended filter in order to avoid the given type in the filtered resources.<p> 
     *  
     * @param type the resource type to exclude
     * @return a filter excluding the given resource type
     */
    public CmsResourceFilter addExcludeType(int type) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_type = type;
        extendedFilter.m_mode |= C_FILTER_EXCLUDE_TYPE;
        extendedFilter.updateCacheId();
        
        return extendedFilter;        
    }
    
    /**
     * Returns an extended filter to guarantee a distinct resource state of the filtered resources.<p>
     * 
     * @param state the required resource state
     * @return a filter requiring the given resource state
     */
    public CmsResourceFilter addRequireState(int state) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_state = state;
        extendedFilter.m_mode &= ~C_FILTER_EXCLUDE_STATE;
        extendedFilter.updateCacheId();       
        
        return extendedFilter;        
    }

    /**
     * Returns an extended filter in order to avoid the given type in the filtered resources.<p> 
     *  
     * @param state the resource state to exclude
     * @return a filter excluding the given resource state
     */
    public CmsResourceFilter addExcludeState(int state) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_state = state;
        extendedFilter.m_mode |= C_FILTER_EXCLUDE_STATE;
        extendedFilter.updateCacheId();        
        
        return extendedFilter;        
    }
    
    /**
     * Returns an extended filter to guarantee all filtered resources are visible.<p>
     * 
     * @return a filter excluding invisible resources
     */
    public CmsResourceFilter addRequireVisible() {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_mode |= C_FILTER_REQUIRE_VISIBLE;
        extendedFilter.updateCacheId();
        
        return extendedFilter;        
    }
    
    /**
     * Returns an extended filter to guarantee all filtered resources are valid (released and not expired).<p>
     * 
     * @return a filter excluding invalid resources
     */
    public CmsResourceFilter addRequireValid() {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_mode |= C_FILTER_REQUIRE_VALID;
        extendedFilter.updateCacheId();
        
        return extendedFilter;        
    }
    
    /**
     * Returns an extended filter to restrict the results to immediate child resources only.<p>
     * 
     * @return a filter to restrict the results to immediate child resources only
     */
    public CmsResourceFilter addRequireImmediateChilds() {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_mode |= C_FILTER_REQUIRE_CHILDS;
        extendedFilter.updateCacheId();
    
        return extendedFilter;        
    }
    
    /**
     * Returns an extended filter to restrict the results to resources modified in the given timerange.<p>
     * 
     * @param time the required time
     * @return a filter to restrict the results to resources modified in the given timerange
     */
    public CmsResourceFilter addRequireModifiedAfter(long time) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_modifiedAfter = time;
        extendedFilter.updateCacheId();      

        return extendedFilter;  
    }
    
    /**
     * Returns an extended filter to restrict the results to resources modified in the given timerange.<p>
     * 
     * @param time the required time 
     * @return a filter to restrict the results to resources modified in the given timerange
     */
    public CmsResourceFilter addRequireModifiedBefore(long time) {
        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();
        
        extendedFilter.m_modifiedBefore = time;
        extendedFilter.updateCacheId();        

        return extendedFilter;  
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
     * Returns the mode for this filter.<p>
     * 
     * @return the mode for this filter
     */
    public int getMode() {

        return m_mode;
    }
    
    /**
     * Returns the type for this filter.<p>
     * 
     * @return the type for this filter
     */
    public int getType() {
        
        return m_type;
    }
    
    /**
     * Returns the state for this filter.<p>
     * 
     * @return the state for this filter
     */
    public int getState() {
        
        return m_state;
    }
    
    /**
     * Returns the start of the modification time range for this filter.<p>
     * 
     * @return start of the modification time range for this filter
     */
    public long getModifiedAfter() {
     
        return m_modifiedAfter;
    }
    
    /**
     * Returns the end of the modification time range for this filter.<p>
     * 
     * @return the end of the modification time range for this filter
     */
    public long getModifiedBefore() {

        return m_modifiedBefore;
    }
    
    /**
     * Check if deleted resources should be filtered.<p>
     * 
     * @return true if deleted resources should be included, false otherwiese
     */
    public boolean includeDeleted() {
        
        return (!(m_state == I_CmsConstants.C_STATE_DELETED && (m_mode & C_FILTER_EXCLUDE_STATE) > 0));
    }
    
    /**
     * Check if the visible permission should be ignored for resources.<p>
     * 
     * @return true if the visible permission should be ignored for resources
     */
    public boolean includeInvisible() {
        
        return ((m_mode & C_FILTER_REQUIRE_VISIBLE) == 0);
    }
    
    /**
     * Check if resources before release date and after expireing date should be filtered.<p>
     * 
     * @return true if resources before release date and after expireing date should be included, false otherwiese
     */
    public boolean includeInvalid() {
        
        return ((m_mode & C_FILTER_REQUIRE_VALID) == 0);
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
        if (!includeDeleted() 
            && (resource.getState() == I_CmsConstants.C_STATE_DELETED)) {
            return false;
        }
        // check if the resource is within the valid time frame
        if (!includeInvalid()
            && ((resource.getDateReleased() > context.getRequestTime()) || (resource.getDateExpired() < context.getRequestTime()))) {
            return false;
        }
        
        // everything is ok, so return true
        return true;
    }
}
