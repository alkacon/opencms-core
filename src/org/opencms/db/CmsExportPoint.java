/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsExportPoint.java,v $
 * Date   : $Date: 2004/03/07 19:22:41 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.db;

import org.opencms.main.OpenCms;

/**
 * Contains the data of a single export point.<p>
 *  
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.3
 */
public class CmsExportPoint {
    
    /** The destination path in the "real" file system, relative to the web application folder */
    private String m_destination;

    /** The URI of the OpenCms VFS resource (folder) of the export point */
    private String m_uri;
    
    /**
     * Creates a new export point.<p>
     * 
     * @param uri the folder in the OpenCms VFS to write as export point
     * @param destination the destination folder in the "real" file system, 
     *     relative to the web application root
     */
    public CmsExportPoint(String uri, String destination) {        
        m_uri = uri;
        m_destination = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(destination);
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (! (o instanceof CmsExportPoint)) {
            return false;
        }
        CmsExportPoint other = (CmsExportPoint)o;
        return getUri().equals(other.getUri());
    }
    
    /**
     * Returns the destination path in the "real" file system.<p>
     * 
     * @return the destination
     */
    public String getDestination() {
        return m_destination;
    }
    
    /**
     * Returns the uri of the OpenCms VFS folder to write as export point.<p>
     * 
     * @return the uri
     */
    public String getUri() {
        return m_uri;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getUri().hashCode();
    }
}
