/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsPublishedResources.java,v $
* Date   : $Date: 2003/08/15 17:38:04 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.file;

import java.io.Serializable;
import java.util.Vector;

/**
 * Describes the results of publishing in the Cms.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.3 $ $Date: 2003/08/15 17:38:04 $
 */
public class CmsPublishedResources implements Cloneable, Serializable{

    /**
     * The vector of changed resources in the vfs
     */
    private Vector m_changedResources;

    /**
     * The vector of changed resources in the cos
     */
    private Vector m_changedModuleMasters;
    
    /**
     * The id of the project that was published  
     */
    private CmsProject m_project; 
    

    public CmsPublishedResources(CmsProject project) {
        m_project = project;
        m_changedResources = null;
        m_changedModuleMasters = null;
    }

    public CmsPublishedResources(CmsProject project, Vector changedResources, Vector changedModuleMasters){
        m_project = project;
        m_changedResources = changedResources;
        m_changedModuleMasters = changedModuleMasters;
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new CmsPublishedResources((CmsProject)m_project.clone(), (Vector)m_changedResources.clone(), (Vector)m_changedModuleMasters.clone());
    }

    /**
     * Sets the vector for changed resources in vfs
     *
     * @param changedResources The vector of changed resources
     */
    public void setChangedResources(Vector changedResources){
        m_changedResources = changedResources;
    }

    /**
     * Sets the vector for changed resources in cos
     *
     * @param changedModuleMasters The vector of changed masters
     */
    public void setChangedModuleMasters(Vector changedModuleMasters){
        m_changedModuleMasters = changedModuleMasters;
    }

    /**
     * Returns the vector of changed resources in cos
     *
     * @return Vector The vector of changed masters
     */
    public Vector getChangedModuleMasters(){
        return m_changedModuleMasters;
    }

    /**
     * Sets the vector for changed resources in vfs
     *
     * @return Vector The vector of changed resources
     */
    public Vector getChangedResources(){
        return m_changedResources;
    }
}