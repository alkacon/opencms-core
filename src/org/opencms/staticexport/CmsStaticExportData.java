/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportData.java,v $
 * Date   : $Date: 2003/09/25 16:07:46 $
 * Version: $Revision: 1.3 $
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

package org.opencms.staticexport;

import com.opencms.file.CmsResource;

/**
 * Provides a data structure for the result of an export request.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 */
public class CmsStaticExportData {
    
    /** The resource to export */
    private CmsResource m_resource;    

    /** The uri to export in the rfs */
    private String m_rfsName;      
    
    /** The uri in the vfs */
    private String m_vfsName;

    /**
     * Creates a new static export data object.<p>
     * 
     * @param vfsName the vfs name of the resource
     * @param rfsName the rfs name of the resource
     * @param resource the resource object
     */
    public CmsStaticExportData(String vfsName, String rfsName, CmsResource resource) {
        m_vfsName = vfsName;
        m_rfsName = rfsName;
        m_resource = resource;
    }    

    /**
     * Returns the resource to export.<p>
     *  
     * @return the resource to export
     */
    public CmsResource getResource() {
        return m_resource;
    }
    
    /**
     * Returns the rfs name of the resource to export.<p>
     * 
     * @return the rfs name of the resource to export
     */
    public String getRfsName() {
        return m_rfsName;
    }

    /**
     * Returns the vfs name of the resource to export.<p>
     *  
     * @return the vfs name of the resource to export
     */
    public String getVfsName() {
        return m_vfsName;
    }
    
    /**
     * @see java.lang.Object#toString()
     */    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(this.getClass().getName());
        result.append("[vfsName=");
        result.append(m_vfsName);
        result.append(", rfsName=");
        result.append(m_rfsName);
        if (m_resource != null) {
            result.append(", resStructureId=");
            result.append(m_resource.getStructureId());
        }
        result.append("]");
        return result.toString();                
    }
}