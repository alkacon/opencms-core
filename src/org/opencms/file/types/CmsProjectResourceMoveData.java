/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/Attic/CmsProjectResourceMoveData.java,v $
 * Date   : $Date: 2009/07/09 13:23:08 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;

/**
 * Data container to carry a move resource for a project, only used by {@link A_CmsResourceTypeFolderBase}.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 7.6.0   
 */
public class CmsProjectResourceMoveData {

    /** The project to move the resource on. */
    private CmsProject m_project;

    /** The project resource to remove. */
    private CmsResource m_sourceResource;

    /** The project resource to add. */
    private CmsResource m_targetResource;

    /**
     * Creates an instance with all data to carry.<p>
     * 
     * @param project 
     *      the project to move the resource on
     *      
     * @param sourceResource
     *      the project resource to remove
     *      
     * @param targetResource
     *      the project resource to add.
     */
    public CmsProjectResourceMoveData(CmsProject project, CmsResource sourceResource, CmsResource targetResource) {

        super();
        m_project = project;
        m_sourceResource = sourceResource;
        m_targetResource = targetResource;
    }

    /**
     * Returns the project to move the resource on. <p>
     * 
     * @return
     *      the project to move the resource on
     */
    public CmsProject getProject() {

        return m_project;
    }

    /**
     * Returns the resource to remove from the project.<p> 
     * 
     * @return  the resource to remove from the project.
     */
    public CmsResource getSourceResource() {

        return m_sourceResource;
    }

    /**
     * Returns the resource to add to the project.<p> 
     * 
     * @return the resource to add to the project.
     */

    public CmsResource getTargetResource() {

        return m_targetResource;
    }

}
