/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbMetadefinitionCache.java,v $
 * Date   : $Date: 2000/06/05 13:37:55 $
 * Version: $Revision: 1.3 $
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
 * It provides a caching algorithem for metainformation access methods.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 2000/06/05 13:37:55 $
 */

public class CmsRbMetadefinitionCache extends CmsRbMetadefinition {
	
     /** The metainfocache */
     private CmsCache m_metacache=null;
     
     /** The cache size */
     private final static int C_METACACHE=2000;
    
    
    /**
     * Constructor, creates a new Cms Project Resource Broker.
     * 
     * @param accessProject The project access object.
     */
    public CmsRbMetadefinitionCache(I_CmsAccessMetadefinition accessMetadefinition) {
        super(accessMetadefinition);
        m_metacache=new CmsCache(C_METACACHE);
    }

    
     /**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(CmsResource resource, String meta)
		throws CmsException {
        String metainfo;
        String key=resource.getProjectId()+resource.getAbsolutePath()+meta;
        metainfo=(String)m_metacache.get(key);
        if (metainfo == null) {
            metainfo=m_accessMetadefinition.readMetainformation(resource, meta);
            m_metacache.put(key,metainfo);
        }
		return metainfo;
	}	
    
     /**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return metainfo The metainfo as string or null if the metainfo not exists.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException {
        String metainfo;
        String key=projectId+path+meta;
        metainfo=(String)m_metacache.get(key);
        if (metainfo == null) {
            metainfo=m_accessMetadefinition.readMetainformation(meta, projectId,path, resourceType);
            m_metacache.put(key,metainfo);
        }
		return metainfo;
	}
    
     /**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(CmsResource resource, String meta,
											  String value)
		throws CmsException {
        
        String key=resource.getProjectId()+resource.getAbsolutePath()+meta;
  		m_accessMetadefinition.writeMetainformation(resource, meta, value);
        m_metacache.put(key,value);
	}

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param value The value for the metainfo to be set.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(String meta, String value, int projectId, 
									 String path, int resourceType)
		throws CmsException {
        String key=projectId+path+meta;
        m_accessMetadefinition.writeMetainformation(meta, value, projectId, 
													path, resourceType);
        m_metacache.put(key,value);
	}
    
    	
	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(CmsResource resource, String meta)
		throws CmsException {
        
        String key=resource.getProjectId()+resource.getAbsolutePath()+meta;
		m_accessMetadefinition.deleteMetainformation(resource, meta);
        m_metacache.remove(key);
	}
	
	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException {
        String key=projectId+path+meta;
		m_accessMetadefinition.deleteMetainformation(meta, projectId, path, resourceType);
        m_metacache.remove(key);
	}
   
     /**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(Hashtable metainfos, int projectId, 
									  String path, int resourceType)
		throws CmsException {
		m_accessMetadefinition.writeMetainformations(metainfos, projectId, 
													 path, resourceType);
        m_metacache.clear();
	}
    
     /**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(CmsResource resource)
		throws CmsException {
		m_accessMetadefinition.deleteAllMetainformations(resource);
        m_metacache.clear();
	}

	 /**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(int projectId, String path)
		throws CmsException {
		m_accessMetadefinition.deleteAllMetainformations(projectId, path);
        m_metacache.clear();
	}
   
    
}