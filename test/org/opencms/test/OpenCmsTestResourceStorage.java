/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestResourceStorage.java,v $
 * Date   : $Date: 2004/05/26 11:30:15 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.test;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage object for storing all attributes of vfs resources.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class OpenCmsTestResourceStorage {
    
    /** A CmsObject to use to access resources */
    CmsObject m_cms;
  
    /** Strings for mapping the filename */
    String m_mapSource;
    String m_mapTarget;
    
    /** internal storage */
    Map m_storage;
       
    /**
     * Creates a new OpenCmsTestResourceStorage.<p>
     * 
     * @param cms the current CmsObject
     */
    public OpenCmsTestResourceStorage(CmsObject cms) {
        m_storage = new HashMap();
        m_mapSource = null;
        m_mapTarget = null;
        m_cms = cms;
    }
    
    /** 
     * Adds a CmsResource to the resource storage.<p>
     * @param res the resource to add
     * @throws CmsException if something goes wrong
     */
    public void add(String resourceName, CmsResource resource) throws CmsException{
        m_storage.put(resourceName, new OpenCmsTestResourceStorageEntry(m_cms, resourceName, resource));
    }
    
    /**
     * Gets an entry from the storage.<p>
     * 
     * @param resourceName the name of the resource to get 
     * @return OpenCmsTestResourceStorageEntry with all the attributes of a CmsResource
     */
    public OpenCmsTestResourceStorageEntry get(String resourceName) throws CmsException {
        //TODO: do the name mapping here
        String mappedResourceName = resourceName;
        
        OpenCmsTestResourceStorageEntry entry= null;
        entry = (OpenCmsTestResourceStorageEntry)m_storage.get(mappedResourceName);
        
        if (entry == null) {
            throw new CmsException("Not found in storage "+resourceName+" -> "+mappedResourceName, CmsException.C_NOT_FOUND);
        }
        
        return entry;
    }
    
    /**
     * Sets the mapping for resourcenames.<p>
     *
     * @param source the source resource name
     * @param target the target resource name
     */
    public void setMapping(String source, String target) {        
        m_mapSource = source;
        m_mapTarget = target;
    }
}
