/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsUriLocator.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.12 $
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

package com.opencms.template.cache;

import java.util.Vector;

/**
 * The UriLocator is used to receive CmsUri-Objects. It is the Cache for these
 * CmsUri-Objects. The CmsUri-Objects are stored in memory or - if they are not
 * used a long time written to an external database. The locator manages all the
 * reading, writing and management of the CmsUri's.
 *
 * @author Andreas Schouten
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsUriLocator {

    /**
     * A hashtable to store the uri's.
     */
    private CmsLruCache m_uris;

    /**
     * The default constructor for this locator.
     */
    CmsUriLocator(int cacheSize) {
        if(cacheSize < 2){
            cacheSize = 10000;
        }
        m_uris = new CmsLruCache(cacheSize);
    }

    /**
     * Adds a new Uri to this locator.
     * @param descriptor - the UriDescriptor for this uri.
     * @param uri - the Uri to put in this locator.
     */
    public void put(CmsUriDescriptor desc, CmsUri uri) {
        m_uris.put(desc, uri);
    }

    /**
     * Gets a uri from this locator.
     * @param desc - the descriptor to locate the uri.
     * @return the uri that was found.
     */
    public CmsUri get(CmsUriDescriptor desc) {
        return (CmsUri) m_uris.get(desc);
    }

    /**
     * Deletes all invalid uris from cache.
     * @param invalidUris A Vector with the names of the uris (String) to be deleted from cache.
     */
    public void deleteUris(Vector invalidUris){
        for (int i = 0; i < invalidUris.size(); i++){
            m_uris.deleteUri((String)invalidUris.elementAt(i));
        }
    }

    /**
     * Clears the cache compleatly.
     */
    public void clearCache(){
        m_uris.clearCache();
    }

    /**
     * Gets the Information of max size and size for the cache.
     *
     * @return a Vector whith informations about the size of the cache.
     */
    public Vector getCacheInfo(){
        return m_uris.getCacheInfo();
    }

}