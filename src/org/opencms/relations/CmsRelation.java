/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelation.java,v $
 * Date   : $Date: 2006/09/10 20:56:15 $
 * Version: $Revision: 1.1.2.3 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

/**
 * A relation between two opencms resources.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 6.3.0 
 */
public class CmsRelation {

    /** The start date of the relation. */
    private final long m_dateBegin;

    /** The end date of the relation. */
    private final long m_dateEnd;

    /** The structure id of the source resource. */
    private final CmsUUID m_sourceId;

    /** The path of the source resource. */
    private final String m_sourcePath;

    /** The structure id of the target resource. */
    private final CmsUUID m_targetId;

    /** The path of the target resource. */
    private final String m_targetPath;

    /** The relation type. */
    private final CmsRelationType m_type;

    /**
     * Creates a new relation object of the given type between the given resources.<p>
     * 
     * @param source the source resource
     * @param target the target resource
     * @param type the relation type
     */
    public CmsRelation(CmsResource source, CmsResource target, CmsRelationType type) {

        this(
            source.getStructureId(),
            source.getRootPath(),
            target.getStructureId(),
            target.getRootPath(),
            target.getDateReleased(),
            target.getDateExpired(),
            type);
    }

    /**
     * Base constructor.<p>
     * 
     * @param sourceId the source structure id
     * @param sourcePath the source path
     * @param targetId the target structure id
     * @param targetPath the target path
     * @param dateBegin the start end of the relation
     * @param dateEnd the end date of the relation
     * @param type the relation type
     */
    public CmsRelation(
        CmsUUID sourceId,
        String sourcePath,
        CmsUUID targetId,
        String targetPath,
        long dateBegin,
        long dateEnd,
        CmsRelationType type) {

        m_sourceId = sourceId;
        m_sourcePath = sourcePath;
        m_targetId = targetId;
        m_targetPath = targetPath;
        m_dateBegin = dateBegin;
        m_dateEnd = dateEnd;
        m_type = type;

    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CmsRelation other = (CmsRelation)obj;
        if (m_dateBegin != other.m_dateBegin) {
            return false;
        }
        if (m_dateEnd != other.m_dateEnd) {
            return false;
        }
        if (m_sourceId == null) {
            if (other.m_sourceId != null) {
                return false;
            }
        } else if (!m_sourceId.equals(other.m_sourceId)) {
            return false;
        }
        if (m_sourcePath == null) {
            if (other.m_sourcePath != null) {
                return false;
            }
        } else if (!m_sourcePath.equals(other.m_sourcePath)) {
            return false;
        }
        if (m_targetId == null) {
            if (other.m_targetId != null) {
                return false;
            }
        } else if (!m_targetId.equals(other.m_targetId)) {
            return false;
        }
        if (m_targetPath == null) {
            if (other.m_targetPath != null) {
                return false;
            }
        } else if (!m_targetPath.equals(other.m_targetPath)) {
            return false;
        }
        if (m_type == null) {
            if (other.m_type != null) {
                return false;
            }
        } else if (!m_type.equals(other.m_type)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the start date of the relation.<p>
     *
     * @return the start date of the relation
     */
    public long getDateBegin() {

        return m_dateBegin;
    }

    /**
     * Returns the end date of the relation.<p>
     *
     * @return the end date of the relation
     */
    public long getDateEnd() {

        return m_dateEnd;
    }

    /**
     * Returns the structure id of the source resource.<p>
     *
     * @return the structure id of the source resource
     */
    public CmsUUID getSourceId() {

        return m_sourceId;
    }

    /**
     * Returns the path of the source resource.<p>
     *
     * @return the path of the source resource
     */
    public String getSourcePath() {

        return m_sourcePath;
    }

    /**
     * Returns the target resource wenn possible to read with the given filter.<p>
     * 
     * @param cms the current user context
     * @param filter the filter to use
     * 
     * @return the target resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getTarget(CmsObject cms, CmsResourceFilter filter) throws CmsException {

        try {
            return cms.readResource(m_targetId, filter);
        } catch (CmsVfsResourceNotFoundException e) {
            String targetPath = cms.getRequestContext().removeSiteRoot(getTargetPath());
            return cms.readResource(targetPath, filter);
        }
    }

    /**
     * Returns the tructure id of the target resource.<p>
     *
     * @return the tructure id of the target resource
     */
    public CmsUUID getTargetId() {

        return m_targetId;
    }

    /**
     * Returns the path of the target resource.<p>
     *
     * @return the path of the target resource
     */
    public String getTargetPath() {

        return m_targetPath;
    }

    /**
     * Returns the relation type.<p>
     *
     * @return the relation type
     */
    public CmsRelationType getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int)(m_dateBegin ^ (m_dateBegin >>> 32));
        result = PRIME * result + (int)(m_dateEnd ^ (m_dateEnd >>> 32));
        result = PRIME * result + ((m_sourceId == null) ? 0 : m_sourceId.hashCode());
        result = PRIME * result + ((m_sourcePath == null) ? 0 : m_sourcePath.hashCode());
        result = PRIME * result + ((m_targetId == null) ? 0 : m_targetId.hashCode());
        result = PRIME * result + ((m_targetPath == null) ? 0 : m_targetPath.hashCode());
        result = PRIME * result + ((m_type == null) ? 0 : m_type.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer str = new StringBuffer();
        str.append("CmsRelation [");
        str.append("source id: ").append(m_sourceId).append(", ");
        str.append("source path: ").append(m_sourcePath).append(", ");
        str.append("target id: ").append(m_targetId).append(", ");
        str.append("target path: ").append(m_targetPath).append(", ");
        str.append("date begin: ").append(m_dateBegin).append(", ");
        str.append("date end: ").append(m_dateEnd).append(", ");
        str.append("type: ").append(m_type);
        str.append("]");
        return str.toString();
    }
}
