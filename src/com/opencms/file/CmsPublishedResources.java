/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsPublishedResources.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.2 $
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

import java.io.*;
import java.util.*;

/**
 * Describes the results of publishing in the Cms.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.2 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsPublishedResources implements Cloneable, Serializable{

    /**
     * The vector of changed resources in the vfs
     */
    private Vector m_changedResources = null;

    /**
     * The vector of changed resources in the cos
     */
    private Vector m_changedModuleMasters = null;

    public CmsPublishedResources(){
        m_changedResources = null;
        m_changedModuleMasters = null;
    }

    public CmsPublishedResources(Vector changedResources, Vector changedModuleMasters){
        m_changedResources = changedResources;
        m_changedModuleMasters = changedModuleMasters;
    }

    /**
    * Clones the CmsPublishedResources by creating a new CmsPublishedResources.
    * @return Cloned CmsPublishedResources.
    */
    public Object clone(Vector changedResources, Vector changedModuleMasters) {
        return new CmsPublishedResources(changedResources, changedModuleMasters);
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