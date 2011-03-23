/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationFilter.java,v $
 * Date   : $Date: 2011/03/23 14:52:58 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.relations;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A filter to retrieve the relations for a given resource.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRelationFilter implements Cloneable {

    /** To filter all sources and targets. */
    public static final CmsRelationFilter ALL = new CmsRelationFilter(true, true);

    /** To filter all sources. */
    public static final CmsRelationFilter SOURCES = new CmsRelationFilter(true, false);

    /** To filter all targets. */
    public static final CmsRelationFilter TARGETS = new CmsRelationFilter(false, true);

    /** If set the filter extends the result to the given path and all its subresources. */
    private boolean m_includeSubresources;

    /** To filter relations for a given source path. */
    private String m_path;

    /** If set the filter looks for matching targets. */
    private boolean m_source;

    /** The structure id of the resource to filter. */
    private CmsUUID m_structureId;

    /** If set the filter looks for matching sources. */
    private boolean m_target;

    /** The types to filter. */
    private Set m_types = new HashSet();

    /**
     * Private constructor.<p>
     * 
     * @param source if set the filter looks for matching targets
     * @param target if set the filter looks for matching sources
     */
    private CmsRelationFilter(boolean source, boolean target) {

        m_source = source;
        m_target = target;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsRelationFilter filter = new CmsRelationFilter(m_source, m_target);
        filter.m_structureId = m_structureId;
        filter.m_types = new HashSet(m_types);
        filter.m_path = m_path;
        filter.m_includeSubresources = m_includeSubresources;
        return filter;
    }

    /**
     * Returns an extended filter with defined in content type restriction.<p>
     * 
     * @return an extended filter with defined in content type restriction
     */
    public CmsRelationFilter filterDefinedInContent() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllDefinedInContent());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterDefinedInContent(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its subresources.<p>
     * 
     * @return an extended filter with including subresources
     */
    public CmsRelationFilter filterIncludeChildren() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_includeSubresources = true;
        return filter;
    }

    /**
     * Returns an extended filter with internal type restriction.<p>
     * 
     * @return an extended filter with internal type restriction
     */
    public CmsRelationFilter filterInternal() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllInternal());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterInternal(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns an extended filter with not defined in content type restriction.<p>
     * 
     * @return an extended filter with not defined in content type restriction
     */
    public CmsRelationFilter filterNotDefinedInContent() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllNotDefinedInContent());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterNotDefinedInContent(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns an extended filter with the given source relation path restriction.<p>
     * 
     * @param path the source relation path to filter
     *  
     * @return an extended filter with the given source relation path restriction
     */
    public CmsRelationFilter filterPath(String path) {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_path = path;
        return filter;
    }

    /**
     * Returns an extended filter with the given resource (path and id) restriction.<p>
     * 
     * @param resource the resource to filter
     *  
     * @return an extended filter with the given resource (path and id) restriction
     */
    public CmsRelationFilter filterResource(CmsResource resource) {

        CmsRelationFilter filter = filterStructureId(resource.getStructureId());
        filter = filterPath(resource.getRootPath());
        return filter;
    }

    /**
     * Returns an extended filter with strong type restriction.<p>
     * 
     * @return an extended filter with strong type restriction
     */
    public CmsRelationFilter filterStrong() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllStrong());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterStrong(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns an extended filter with the given structure id restriction.<p>
     *
     * @param structureId the structure id to filter
     *  
     * @return an extended filter with the given structure id restriction
     */
    public CmsRelationFilter filterStructureId(CmsUUID structureId) {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_structureId = structureId;
        return filter;
    }

    /**
     * Returns an extended filter with the given type restriction.<p>
     * 
     * @param type the relation type to filter
     *  
     * @return an extended filter with the given type restriction
     */
    public CmsRelationFilter filterType(CmsRelationType type) {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_types.add(type);
        return filter;
    }

    /**
     * Returns an extended filter with user defined type restriction.<p>
     * 
     * @return an extended filter with user defined type restriction
     */
    public CmsRelationFilter filterUserDefined() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllUserDefined());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterUserDefined(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns an extended filter with weak type restriction.<p>
     * 
     * @return an extended filter with weak type restriction
     */
    public CmsRelationFilter filterWeak() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        if (filter.m_types.isEmpty()) {
            filter.m_types.addAll(CmsRelationType.getAllWeak());
        } else {
            filter.m_types = new HashSet(CmsRelationType.filterWeak(filter.m_types));
        }
        return filter;
    }

    /**
     * Returns the source relation path restriction.<p>
     *
     * @return the source relation path restriction
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the structure Id of the resource to filter.<p>
     *
     * @return the structure Id of the resource to filter
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the types to filter.<p>
     *
     * @return the types to filter
     */
    public Set getTypes() {

        return Collections.unmodifiableSet(m_types);
    }

    /**
     * Checks if this filter includes relations defined in the content.<p> 
     * 
     * @return <code>true</code> if this filter includes relations defined in the content
     */
    public boolean includesDefinedInContent() {

        if ((m_types == null) || m_types.isEmpty()) {
            return true;
        }
        Iterator itTypes = m_types.iterator();
        while (itTypes.hasNext()) {
            CmsRelationType type = (CmsRelationType)itTypes.next();
            if (type.isDefinedInContent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the include subresources flag.<p>
     * 
     * @return if set the filter extends the result to the given path and all its subresources
     */
    public boolean isIncludeSubresources() {

        return m_includeSubresources;
    }

    /**
     * Returns the source flag.<p>
     *
     * @return if set the filter looks for matching targets
     */
    public boolean isSource() {

        return m_source;
    }

    /**
     * Returns the target flag.<p>
     *
     * @return if set the filter looks for matching sources
     */
    public boolean isTarget() {

        return m_target;
    }

    /**
     * Returns <code>true</code> if the given relation type matches this filter.<p>
     * 
     * @param type the relation type to test
     * 
     * @return if the given relation type matches this filter
     */
    public boolean matchType(CmsRelationType type) {

        if (m_types.isEmpty()) {
            return true;
        }
        return m_types.contains(type);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(128);
        str.append("[");
        String mode = null;
        if (m_source) {
            if (m_target) {
                mode = "both";
            } else {
                mode = "source";
            }
        } else {
            if (m_target) {
                mode = "target";
            } else {
                mode = "none";
            }
        }
        str.append(mode).append("=").append(m_structureId).append(", ");
        str.append("path").append("=").append(m_path).append(", ");
        str.append("types").append("=").append(m_types).append(", ");
        str.append("subresources").append("=").append(m_includeSubresources);
        str.append("]");
        return str.toString();
    }
}
