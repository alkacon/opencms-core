/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsUriDescriptor.java,v $
* Date   : $Date: 2001/05/28 08:51:27 $
* Version: $Revision: 1.2 $
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
package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * This descriptor is used to locate CmsUri-Object with the CmsUriLocator. It
 * is the key for a CmsUri.
 *
 * @author: Andreas Schouten
 */
public class CmsUriDescriptor {

    /**
     * The uri string
     */
    private String m_uri;

    /**
     * Creates a new UriDescriptor
     * @param uri - the uri
     */
    public CmsUriDescriptor(String uri){
        m_uri = uri;
    }

    /**
     * Returns the uri.
     * @returns the uri
     */
    public String getKey(){
        return m_uri;
    }

	/**
	 * Compares the overgiven object with this object.
	 *
	 * @return true, if the object is identically else it returns false.
	 */
	public boolean equals(Object obj) {
		// check if the object is a CmsUriDescriptor object
		if (obj instanceof CmsUriDescriptor) {
			// same key?
			if (((CmsUriDescriptor)obj).getKey().equals(m_uri) ){
				return true;
			}
		}
		return false;
	}

    /**
     * We have to return a hashcode for the hashtable. We can use the hashcode
     * from the String uri.
     * @return The hashCode.
     */
    public int hashCode(){
        return m_uri.hashCode();
    }

    /**
     * toString methode
     */
    public String toString(){
        return m_uri;
    }

}