/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbProjectCache.java,v $
 * Date   : $Date: 2000/06/05 13:37:55 $
 * Version: $Revision: 1.6 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/06/05 13:37:55 $
 */
class CmsRbProjectCache extends CmsRbProject {
	
     /** The projectcache */
     private CmsCache m_projectcache=null;
     
     /** The cache size */
     private final static int C_PROJECTCACHE=200;

    
    /**
     * Constructor, creates a new Cms Project Resource Broker.
     * 
     * @param accessProject The project access object.
     */
    public CmsRbProjectCache(I_CmsAccessProject accessProject) {
        super(accessProject);
        m_projectcache=new CmsCache(C_PROJECTCACHE);     
    }
    
    	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(int id)
		 throws CmsException {
         CmsProject project=null;
         project=(CmsProject)m_projectcache.get(new Integer(id));
         if (project== null) {
             project=m_accessProject.readProject(id);
             m_projectcache.put(new Integer(id),project);
         }
		 return project;
	 }
     
     /**
	 * Updates a project.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject writeProject(CmsProject project)
		 throws CmsException {
         m_projectcache.put(new Integer(project.getId()),project);
		 return( m_accessProject.writeProject(project) );
	 }
     
     /**
	 * Deletes a project.
	 * 
	 * @param project The project that will be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void deleteProject(CmsProject project)
		 throws CmsException {
         m_projectcache.remove(new Integer(project.getId()));
         m_accessProject.deleteProject(project);
	 }
    
}