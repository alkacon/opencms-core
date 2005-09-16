/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsResourceFilter.java,v $
 * Date   : $Date: 2005/09/16 08:46:24 $
 * Version: $Revision: 1.21.2.1 $
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

package org.opencms.file;

/**
 * Provides filters for resource result sets obtained from requests to the OpenCms VFS.<p>
 * 
 * Using the constant filters provided by this class
 * you can control "special" behaviour 
 * of access to the VFS. For example, in the "Offline" project 
 * there can be deleted files, by using this filter you can control
 * if deleted files should be included in a result set or not.<p> 
 * 
 * @author Michael Emmerich 
 * @author Alexander Kandzior 
 * @author Carsten Weinholz 
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.21.2.1 $
 * 
 * @since 6.0.0 
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
    public static final CmsResourceFilter ALL = new CmsResourceFilter();

    /**
     * Filter to display all modified (new/changed/deleted) resources.<p>
     */
    public static final CmsResourceFilter ALL_MODIFIED = ALL.addExcludeState(CmsResource.STATE_UNCHANGED);

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
    public static final CmsResourceFilter DEFAULT = ALL.addExcludeState(CmsResource.STATE_DELETED).addRequireTimerange();

    /**
     * Default filter to display files for the online project.<p>
     */
    public static final CmsResourceFilter DEFAULT_FILES = DEFAULT.addRequireFile();

    /**
     * Default filter to display folders for the online project.<p>
     */
    public static final CmsResourceFilter DEFAULT_FOLDERS = DEFAULT.addRequireFolder();

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
    public static final CmsResourceFilter IGNORE_EXPIRATION = ALL.addExcludeState(CmsResource.STATE_DELETED);

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

    /** 
     * Filter to display only visible and not deleted resources.<p>
     * 
     * This filter used the following rules:
     * <ul>
     * <li>Excludes: Resources marked as deleted.</li>
     * <li>Includes: Resources outside the 'time window' set with release and expiration date.</li>
     * <li>Excludes: Resources marked as 'invisible' using permissions.</li>
     * </ul> 
     */
    public static final CmsResourceFilter ONLY_VISIBLE_NO_DELETED = ONLY_VISIBLE.addExcludeState(CmsResource.STATE_DELETED);

    private static final int EXCLUDED = 2;

    private static final int IGNORED = 0;

    private static final int REQUIRED = 1;

    /** The cache id for this filter. */
    private String m_cacheId;

    /** Indicates if the resource flag is filtered (true) or not (false). */
    private int m_filterFlags;

    /** Indicates if the date of the last modification is used (true) or ignored (false). */
    private boolean m_filterLastModified;

    /** Indicates if the resource state (unchanged/new/deleted/modified) is filtered (true) or not (false). */
    private int m_filterState;

    /** Indicates if the resource valid timerage is used (true) or ignored (false). */
    private boolean m_filterTimerange;

    /** Indicates if the resource type is filtered (true) or not (false). */
    private int m_filterType;

    /** Indicates if the expire date is used (true) or ignored (false). */
    private boolean m_filterExpire;

    /** Indicates if the release date is used (true) or ignored (false). */
    private boolean m_filterRelease;
    
    /** Indicates if the visible permission is used (true) or ignored (false). */
    private boolean m_filterVisible;

    /** The required/excluded flags for filtering resources. */
    private int m_flags;

    /** The required start date for the timerange of the last modification date. */
    private long m_modifiedAfter;

    /** The required end data for the timerange of the last modification date. */
    private long m_modifiedBefore;

    /** The required start date for the timerange of the expire date. */
    private long m_expireAfter;

    /** The required end data for the timerange of the expire date. */
    private long m_expireBefore;

    /** The required start date for the timerange of the release date. */
    private long m_releaseAfter;

    /** The required end data for the timerange of the release date. */
    private long m_releaseBefore;
    
    /** Indicates if the filter should return only folders. */
    private Boolean m_onlyFolders;

    /** The required/excluded state for filtering resources. */
    private int m_state;

    /** The required/excluded type for filtering resources. */
    private int m_type;

    /**
     * Hides the public contructor.<p>
     */
    private CmsResourceFilter() {

        m_filterState = IGNORED;
        m_state = -1;

        m_filterType = IGNORED;
        m_type = -1;

        m_filterFlags = IGNORED;
        m_flags = -1;

        m_filterVisible = false;

        m_filterTimerange = false;
        m_filterLastModified = false;
        m_filterRelease = false;
        m_filterExpire = false;
        m_modifiedAfter = 0L;
        m_modifiedBefore = 0L;
        m_releaseAfter = 0L;
        m_releaseBefore = 0L;
        m_expireAfter = 0L;
        m_expireBefore = 0L;

        updateCacheId();
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
     * Returns an extended filter in order to avoid the given flags in the filtered resources.<p> 
     *  
     * @param flags the resource flags to exclude
     * @return a filter excluding the given resource flags
     */
    public CmsResourceFilter addExcludeFlags(int flags) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_flags = flags;
        extendedFilter.m_filterFlags = EXCLUDED;
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
        extendedFilter.m_filterState = EXCLUDED;
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
        extendedFilter.m_filterType = EXCLUDED;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter that requires all returned resources to be files.<p> 
     *  
     * @return an extended filter that requires all returned resources to be files
     */
    public CmsResourceFilter addRequireFile() {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_onlyFolders = Boolean.FALSE;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter to guarantee a distinct resource flags of the filtered resources.<p>
     * 
     * @param flags the required resource flags
     * @return a filter requiring the given resource flags
     */
    public CmsResourceFilter addRequireFlags(int flags) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_flags = flags;
        extendedFilter.m_filterFlags = REQUIRED;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter that requires all returned resources to be folders.<p> 
     *  
     * @return an extended filter that requires all returned resources to be folders
     */
    public CmsResourceFilter addRequireFolder() {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_onlyFolders = Boolean.TRUE;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter to restrict the results to resources modified in the given timerange.<p>
     * 
     * @param time the required time
     * @return a filter to restrict the results to resources modified in the given timerange
     */
    public CmsResourceFilter addRequireLastModifiedAfter(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterLastModified = true;
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
    public CmsResourceFilter addRequireLastModifiedBefore(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterLastModified = true;
        extendedFilter.m_modifiedBefore = time;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter to restrict the results to resources that expire in the given timerange.<p>
     * 
     * @param time the required time 
     * @return a filter to restrict the results to resources that expire in the given timerange
     */
    public CmsResourceFilter addRequireExpireBefore(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterExpire = true;
        extendedFilter.m_expireBefore = time;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }
    
    /**
     * Returns an extended filter to restrict the results to resources that expire in the given timerange.<p>
     * 
     * @param time the required time 
     * @return a filter to restrict the results to resources that expire in the given timerange
     */
    public CmsResourceFilter addRequireExpireAfter(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterExpire = true;
        extendedFilter.m_expireAfter = time;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }
    
    /**
     * Returns an extended filter to restrict the results to resources that are released in the given timerange.<p>
     * 
     * @param time the required time 
     * @return a filter to restrict the results to resources that are released in the given timerange
     */
    public CmsResourceFilter addRequireReleaseBefore(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterRelease = true;
        extendedFilter.m_releaseBefore = time;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }
    
    /**
     * Returns an extended filter to restrict the results to resources that are released in the given timerange.<p>
     * 
     * @param time the required time 
     * @return a filter to restrict the results to resources that are released in the given timerange
     */
    public CmsResourceFilter addRequireReleaseAfter(long time) {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterRelease = true;
        extendedFilter.m_releaseAfter = time;
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
        extendedFilter.m_filterState = REQUIRED;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * Returns an extended filter to guarantee all filtered resources are valid (released and not expired).<p>
     * 
     * @return a filter excluding invalid resources
     */
    public CmsResourceFilter addRequireTimerange() {

        CmsResourceFilter extendedFilter = (CmsResourceFilter)clone();

        extendedFilter.m_filterTimerange = true;
        extendedFilter.updateCacheId();

        return extendedFilter;
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
        extendedFilter.m_filterType = REQUIRED;
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

        extendedFilter.m_filterVisible = true;
        extendedFilter.updateCacheId();

        return extendedFilter;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsResourceFilter filter = new CmsResourceFilter();

        filter.m_filterState = m_filterState;
        filter.m_filterFlags = m_filterFlags;
        filter.m_filterType = m_filterType;
        filter.m_filterVisible = m_filterVisible;
        filter.m_filterTimerange = m_filterTimerange;
        filter.m_filterLastModified = m_filterLastModified;
        filter.m_filterExpire = m_filterExpire;
        filter.m_filterRelease = m_filterRelease;
        
        filter.m_type = m_type;
        filter.m_state = m_state;
        filter.m_flags = m_flags;
        filter.m_modifiedAfter = m_modifiedAfter;
        filter.m_modifiedBefore = m_modifiedBefore;
        filter.m_releaseAfter = m_releaseAfter;
        filter.m_releaseBefore = m_releaseBefore;
        filter.m_expireAfter = m_expireAfter;
        filter.m_expireBefore = m_expireBefore;
        filter.m_cacheId = m_cacheId;

        return filter;
    }

    /**
     * return if the stored flags should be excluded while filtering resources.<p>
     * 
     * @return if the flags should be excluded
     */
    public boolean excludeFlags() {

        return m_filterFlags == EXCLUDED;
    }

    /**
     * return if the stored state should be excluded while filtering resources.<p>
     * 
     * @return if the state should be excluded
     */
    public boolean excludeState() {

        return m_filterState == EXCLUDED;
    }

    /**
     * Returns if the stored type should be excluded while filtering resources.<p>
     * 
     * @return if the type should be excluded
     */
    public boolean excludeType() {

        return m_filterType == EXCLUDED;
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
     * Returns the flags for this filter.<p>
     * 
     * @return the flags for this filter
     */
    public int getFlags() {

        return m_flags;
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
     * Returns the start of the expire time range for this filter.<p>
     * 
     * @return start of the expire time range for this filter
     */
    public long getExpireAfter() {

        return m_expireAfter;
    }

    /**
     * Returns the end of the expire time range for this filter.<p>
     * 
     * @return the end of the expire time range for this filter
     */
    public long getExpireBefore() {

        return m_expireBefore;
    }
    /**
     * Returns the start of the release time range for this filter.<p>
     * 
     * @return start of the release time range for this filter
     */
    public long getReleaseAfter() {

        return m_releaseAfter;
    }

    /**
     * Returns the end of the release time range for this filter.<p>
     * 
     * @return the end of the release time range for this filter
     */
    public long getReleaseBefore() {

        return m_releaseBefore;
    }
    /**
     * Returns the state of the "only folders" flag.<p>
     * 
     * If the result is <code>null</code>, then this flag is not set.<p>
     * 
     * @return the state of the "only folders" flag
     */
    public Boolean getOnlyFolders() {

        return m_onlyFolders;
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
     * Returns the type for this filter.<p>
     * 
     * @return the type for this filter
     */
    public int getType() {

        return m_type;
    }

    /**
     * Check if deleted resources should be filtered.<p>
     * 
     * @return true if deleted resources should be included, false otherwiese
     */
    public boolean includeDeleted() {

        return (m_filterState == IGNORED)
            || ((m_filterState == REQUIRED) && (m_state == CmsResource.STATE_DELETED))
            || ((m_filterState == EXCLUDED) && (m_state != CmsResource.STATE_DELETED));
    }

    /**
     * Validates if a CmsResource fits to all filer settings.<p>
     * 
     * Please note that the "visible permission" setting of the filter is NOT checked
     * in this method since the permission information is not part of the resource.
     * The visible permission information in the filter will be used in the permission
     * checks 
     *
     * @param context the current request context
     * @param resource the resource to be validated
     * @return true if the resource passes all validations, false otherwise
     */
    public boolean isValid(CmsRequestContext context, CmsResource resource) {

        if (this == ALL) {
            // shortcut for "ALL" filter where nothing is filtered
            return true;
        }

        // check for required resource state
        switch (m_filterState) {
            case EXCLUDED:
                if (resource.getState() == m_state) {
                    return false;
                }
                break;
            case REQUIRED:
                if (resource.getState() != m_state) {
                    return false;
                }
                break;
            default:
        // ignored
        }

        // check for required resource state
        switch (m_filterFlags) {
            case EXCLUDED:
                if ((resource.getFlags() & m_flags) != 0) {
                    return false;
                }
                break;
            case REQUIRED:
                if ((resource.getFlags() & m_flags) != m_flags) {
                    return false;
                }
                break;
            default:
        // ignored
        }

        // check for required resource type
        switch (m_filterType) {
            case EXCLUDED:
                if (resource.getTypeId() == m_type) {
                    return false;
                }
                break;
            case REQUIRED:
                if (resource.getTypeId() != m_type) {
                    return false;
                }
                break;
            default:
        // ignored
        }

        if (m_onlyFolders != null) {
            if (m_onlyFolders.booleanValue()) {
                if (!resource.isFolder()) {
                    // only folder resource are allowed
                    return false;
                }
            } else {
                if (resource.isFolder()) {
                    // no folder resources are allowed
                    return false;
                }
            }
        }

        // check if the resource was last modified within the given time range
        if (m_filterLastModified) {
            if (m_modifiedAfter > 0L && resource.getDateLastModified() < m_modifiedAfter) {
                return false;
            }
            if (m_modifiedBefore > 0L && resource.getDateLastModified() > m_modifiedBefore) {
                return false;
            }
        }

        // check if the resource expires within the given time range
        if (m_filterExpire) {
            if (m_expireAfter > 0L && resource.getDateExpired() < m_expireAfter) {
                return false;
            }
            if (m_expireBefore > 0L && resource.getDateExpired() > m_expireBefore) {
                return false;
            }
        }
        
        // check if the resource is released within the given time range
        if (m_filterRelease) {
            if (m_releaseAfter > 0L && resource.getDateReleased() < m_releaseAfter) {
                return false;
            }
            if (m_releaseBefore > 0L && resource.getDateReleased() > m_releaseBefore) {
                return false;
            }
        }
        
        // check if the resource is currently released and not expired
        if (m_filterTimerange
            && ((resource.getDateReleased() > context.getRequestTime()) || (resource.getDateExpired() < context.getRequestTime()))) {
            return false;
        }

        // everything is ok, so return true
        return true;
    }

    /**
     * Returns if the stored flags is required while filtering resources.<p>
     * 
     * @return if the flags is required
     */
    public boolean requireFlags() {

        return m_filterFlags == REQUIRED;
    }

    /**
     * Returns if the stored state is required while filtering resources.<p>
     * 
     * @return if the state is required
     */
    public boolean requireState() {

        return m_filterState == REQUIRED;
    }

    /**
     * Returns if the release timerange of the resource should be required.<p>
     * 
     * @return true if the release timerange of the resource should be required
     */
    public boolean requireTimerange() {

        return m_filterTimerange;
    }

    /**
     * Returns if the stored type is required while filtering resources.<p>
     * 
     * @return true if the type is required
     */
    public boolean requireType() {

        return m_filterType == REQUIRED;
    }

    /**
     * Returns if the visible permission should be required for resources.<p>
     * 
     * @return true if the visible permission is required, false if the visible permission is ignored
     */
    public boolean requireVisible() {

        return m_filterVisible;
    }

    /**
     * Returns the name of the filter, if it is one of the filters used as a constant of this class and
     * a default message otherwise.<p>
     * 
     * @return the name of the filter
     */
    public String toString() {

        if (this.equals(CmsResourceFilter.ALL)) {
            return "ALL";
        } else if (this.equals(CmsResourceFilter.ALL_MODIFIED)) {
            return "ALL_MODIFIED";
        } else if (this.equals(CmsResourceFilter.DEFAULT)) {
            return "DEFAULT";
        } else if (this.equals(CmsResourceFilter.DEFAULT_FILES)) {
            return "DEFAULT_FILES";
        } else if (this.equals(CmsResourceFilter.DEFAULT_FOLDERS)) {
            return "DEFAULT_FOLDERS";
        } else if (this.equals(CmsResourceFilter.IGNORE_EXPIRATION)) {
            return "IGNORE_EXPIRATION";
        } else if (this.equals(CmsResourceFilter.ONLY_VISIBLE)) {
            return "ONLY_VISIBLE";
        } else if (this.equals(CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
            return "ONLY_VISIBLE_NO_DELETED";
        } else {
            return "Nonstandard Resource Filter";
        }
    }

    /**
     * Recalculates the cache id.<p>
     */
    private void updateCacheId() {

        StringBuffer result = new StringBuffer(32);
        if (m_filterVisible) {
            result.append(" Vi");
        }
        if (m_filterTimerange) {
            result.append(" Ti");
        }
        switch (m_filterState) {
            case REQUIRED:
                result.append(" Sr");
                result.append(m_state);
                break;
            case EXCLUDED:
                result.append(" Sx");
                result.append(m_state);
                break;
            default:
        // ignored
        }
        switch (m_filterFlags) {
            case REQUIRED:
                result.append(" Fr");
                result.append(m_flags);
                break;
            case EXCLUDED:
                result.append(" Fx");
                result.append(m_flags);
                break;
            default:
        // ignored
        }
        switch (m_filterType) {
            case REQUIRED:
                result.append(" Tr");
                result.append(m_type);
                break;
            case EXCLUDED:
                result.append(" Tx");
                result.append(m_type);
                break;
            default:
        // ignored
        }
        if (m_onlyFolders != null) {
            if (m_onlyFolders.booleanValue()) {
                result.append(" Fo");
            } else {
                result.append(" Fi");
            }
        }
        if (m_filterLastModified) {
            result.append(" Lt");
            result.append(m_modifiedAfter);
            result.append("-");
            result.append(m_modifiedBefore);
        }
        if (m_filterExpire) {
            result.append(" Ex");
            result.append(m_expireAfter);
            result.append("-");
            result.append(m_expireBefore);
        }
        if (m_filterRelease) {
            result.append(" Re");
            result.append(m_releaseAfter);
            result.append("-");
            result.append(m_releaseBefore);
        }
        m_cacheId = result.toString();
    }
}