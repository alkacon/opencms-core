/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbProperty.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.7 $
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

import java.io.*;

import com.opencms.core.*;

/**
 * Implementatrion of the I_CmsRbProperty interface, 
 * implementing a resource broker for propertys in the Cms.<BR/>
 * Only the system can access propertys. Propertys are for internal use
 * only. A property is a serializable object.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2000/02/15 17:43:59 $
 */
public class CmsRbProperty implements I_CmsRbProperty  {
	
    /**
     * The property access object which is required to access the
     * property database.
     */
    private I_CmsAccessProperty m_accessProperty;
    
    /**
     * Constructor, creates a new Cms Property Resource Broker.
     * 
     * @param AccessProperty The property access object.
     */
    public CmsRbProperty(I_CmsAccessProperty accessProperty)
    {
        m_accessProperty=accessProperty;
    }
        
     /**
	 * Creates a new a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable addProperty(String name, Serializable object)
         throws CmsException {
               
         return m_accessProperty.addProperty(name,object);
     }
    
	/**
	 * Reads a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable readProperty(String name) 
        throws CmsException {
        return m_accessProperty.readProperty(name);
     }

	/**
	 * Writes a serializable object to the propertys.
	 * 
	 * @param name The name of the property.
	 * @param object The property-object.
	 * 
	 * @return object The property-object.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public Serializable writeProperty(String name, Serializable object)
        throws  CmsException {
         
        return m_accessProperty.writeProperty(name,object);
     }

	/**
	 * Deletes a serializable object from the propertys.
	 * 
	 * @param name The name of the property.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void deleteProperty(String name)
        throws CmsException {
        
        m_accessProperty.deleteProperty(name);
    }
}
