/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsPublishedResources.java,v $
 * Date   : $Date: 2003/09/01 09:09:17 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.db;

import com.opencms.file.CmsProject;

import java.io.Serializable;
import java.util.Vector;

/**
 * Contains the results of a published OpenCms project.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)

 * @version $Revision: 1.1 $ 
 */
public class CmsPublishedResources implements Cloneable, Serializable {

    /** A vector of changed resources in the cos */
    private Vector m_changedCosResources;

    /** A vector of changed resources in the vfs */
    private Vector m_changedVfsResources;
    
    /** The project that was published */
    private CmsProject m_project; 
    
    /**
     * Constructs a new CmsPublishedResources instance with empty 
     * result vectors.<p>
     * 
     * @param project the project that was published
     */
    public CmsPublishedResources(CmsProject project) {
        m_project = project;
        m_changedVfsResources = null;
        m_changedCosResources = null;
    }

    /**
     * Constructs a new CmsPublishedResources instance.<p>
     * 
     * @param project the project that was published
     * @param changedVfsResources the changed vfs resources
     * @param changedCosResources the changed cos resources
     */
    private CmsPublishedResources(CmsProject project, Vector changedVfsResources, Vector changedCosResources) {
        m_project = project;
        m_changedVfsResources = changedVfsResources;
        m_changedCosResources = changedCosResources;
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new CmsPublishedResources(
            (CmsProject)m_project.clone(), 
            (Vector)m_changedVfsResources.clone(), 
            (Vector)m_changedCosResources.clone()
        );
    }

    /**
     * Returns the vector of changed resources in the cos.<p>
     *
     * @return the vector of changed masters
     */
    public Vector getChangedCosResources() {
        return m_changedCosResources;
    }

    /**
     * Sets the vector for changed resources in the vfs.<p>
     *
     * @return the vector of changed resources
     */
    public Vector getChangedVfsResources() {
        return m_changedVfsResources;
    }

    /**
     * Sets the vector for changed resources in the cos.<p>
     *
     * @param changedCosResources The vector of changed masters
     */
    public void setChangedCosResources(Vector changedCosResources) {
        m_changedCosResources = changedCosResources;
    }

    /**
     * Sets the vector for changed resources in the vfs.<p>
     *
     * @param changedVfsResources the vector of changed resources
     */
    public void setChangedVfsResources(Vector changedVfsResources) {
        m_changedVfsResources = changedVfsResources;
    }
}