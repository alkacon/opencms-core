/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsUriLocator.java,v $
* Date   : $Date: 2001/04/26 16:14:52 $
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
package com.opencms.staging;

import java.util.*;
import java.io.*;
import com.opencms.file.*;
import com.opencms.core.*;

/**
 * The UriLocator is used to receive CmsUri-Objects. It is the Cache for these
 * CmsUri-Objects. The CmsUri-Objects are stored in memory or - if they are not
 * used a long time written to an external database. The locator manages all the
 * reading, writing and management of the CmsUri's.
 *
 * @author: Andreas Schouten
 */
public class CmsUriLocator {

    /**
     * The cach for holding the uris.
     */
    private Hashtable m_uriCache;

    /**
     * Constructor
     */
    public CmsUriLocator(){
        m_uriCache = new Hashtable();
        // later we have to read the cache from the Database here.
    }

    /**
     * returns a Uri object. either from the cache or if it is not in it,
     * an new one will be generated and stored in the cache for the future.
     *
     * @param cms The cmsObject containing the uri.
     * @return The uriObject for this uri.
     */
    public CmsUri get(CmsObject cms) throws CmsException{

        CmsUriDescriptor  uri = new CmsUriDescriptor(cms.getRequestContext().getUri());
        if(m_uriCache.containsKey(uri)){
            // check the accessrights then return it
            int groupId = cms.getRequestContext().currentGroup().getId();
            if(false /*TODO: write a method to check if the group has access(checkGroupDependencies(newVector({new Integer(groupId), new Integer(((CmsUri)m_uriCache.get(uri)).getAccessGroup())})))*/){
                // the group has no read access
                throw new CmsException("blabla... nein!", CmsException.C_ACCESS_DENIED);
            }
            return (CmsUri)m_uriCache.get(uri);
        }else{
            // create a new uri Object, put it in the cache and return it
at work...
            CmsUri uriObject = new CmsUri(null, 0, 0, "");
        }

    }
}