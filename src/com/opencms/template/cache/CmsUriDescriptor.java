/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsUriDescriptor.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.7 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

package com.opencms.template.cache;


/**
 * This descriptor is used to locate CmsUri-Object with the CmsUriLocator. It
 * is the key for a CmsUri.
 *
 * @author Andreas Schouten
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
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
     * @return the uri
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