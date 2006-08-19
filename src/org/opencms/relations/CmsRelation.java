/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelation.java,v $
 * Date   : $Date: 2006/08/19 13:40:45 $
 * Version: $Revision: 1.1.2.1 $
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
 * @version $Revision: 1.1.2.1 $ 
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

}
