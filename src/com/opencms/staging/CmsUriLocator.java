/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/staging/Attic/CmsUriLocator.java,v $
* Date   : $Date: 2001/04/27 15:21:48 $
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
     * A hashtable to store the uri's.
     */
    private Hashtable m_uris;

    /**
     * The default constructor for this locator.
     */
    CmsUriLocator() {
        m_uris = new Hashtable();
    }

    /**
     * Adds a new Uri to this locator.
     * @param descriptor - the UriDescriptor for this uri.
     * @param uri - the Uri to put in this locator.
     */
    public void put(CmsUriDescriptor desc, CmsUri uri) {
        m_uris.put(desc.getKey(), uri);
    }

    /**
     * Gets a uri from this locator.
     * @param desc - the descriptor to locate the uri.
     * @returns the uri that was found.
     */
    public CmsUri get(CmsUriDescriptor desc) {
        return (CmsUri) m_uris.get(desc.getKey());
    }
}