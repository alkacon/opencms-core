/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationFilter.java,v $
 * Date   : $Date: 2007/05/02 16:55:31 $
 * Version: $Revision: 1.1.2.4 $
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

package org.opencms.relations;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to retrieve the relations for a given resource.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.4 $ 
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

    /** To filter relations for a given date. */
    private long m_date;

    /** If set the filter extends the result to the given path and all its childs. */
    private boolean m_includeChilds = false;

    /** To filter relations for a given source path. */
    private String m_path;

    /** If set the filter looks for matching targets. */
    private boolean m_source = false;

    /** The structure id of the resource to filter. */
    private CmsUUID m_structureId;

    /** If set the filter looks for matching sources. */
    private boolean m_target = false;

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
    public Object clone() {

        CmsRelationFilter filter = new CmsRelationFilter(m_source, m_target);
        filter.m_structureId = m_structureId;
        filter.m_types = new HashSet(m_types);
        filter.m_date = m_date;
        filter.m_path = m_path;
        filter.m_includeChilds = m_includeChilds;
        return filter;
    }

    /**
     * Returns an extended filter with the given relation date restriction.<p>
     * 
     * @param date the relation date to filter
     *  
     * @return an extended filter with the given relation date restriction
     */
    public CmsRelationFilter filterDate(long date) {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_date = date;
        return filter;
    }

    /**
     * Returns an extended filter that will extend the result to the given path and all its childs.<p>
     * 
     * @return an extended filter with the given relation date restriction
     */
    public CmsRelationFilter filterIncludeChilds() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_includeChilds = true;
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
        filter.m_types.add(CmsRelationType.EMBEDDED_IMAGE);
        filter.m_types.add(CmsRelationType.EMBEDDED_OBJECT);
        filter.m_types.add(CmsRelationType.XML_STRONG);
        filter.m_types.add(CmsRelationType.JSP_STRONG);
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
     * Returns an extended filter with weak type restriction.<p>
     * 
     * @return an extended filter with weak type restriction
     */
    public CmsRelationFilter filterWeak() {

        CmsRelationFilter filter = (CmsRelationFilter)this.clone();
        filter.m_types.add(CmsRelationType.HYPERLINK);
        filter.m_types.add(CmsRelationType.JSP_WEAK);
        filter.m_types.add(CmsRelationType.XML_WEAK);
        return filter;
    }

    /**
     * Returns the relation date restriction.<p>
     *
     * @return the relation date restriction
     */
    public long getDate() {

        return m_date;
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
     * Returns the include childs flag.<p>
     * 
     * @return if set the filter extends the result to the given path and all its childs
     */
    public boolean isIncludeChilds() {

        return m_includeChilds;
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
        str.append("date").append("=").append(m_date).append(", ");
        str.append("types").append("=").append(m_types).append(", ");
        str.append("childs").append("=").append(m_includeChilds);
        str.append("]");
        return str.toString();
    }
}
