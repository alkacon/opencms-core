/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExportPoint.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.4 $
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

package com.opencms.file;

import com.opencms.core.*;

/**
 * Describes a exportpoint. A exportpoint defines the type of a filesystem
 * containing data used my the Cms and where it is mounted in to the logical
 * filesystem of  the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.4 $ $Date: 2003/04/01 15:20:18 $
 */

public class CmsExportPoint implements I_CmsConstants {
    /**
     * Definition of the location of this exportpoint in the logical Cms fielsystem.
     */
    private String m_exportpoint = null;
    
    /**
     * The the exportpath of a exportpoint in a real filesystem.
     *  
     */
    private String m_exportpath = null;
    

     /**
     * Constructs a new CmsExportPoint. 
     * This constructor creates a new exportpoint for the disc filesystem
     * 
     * @param exportpoint The export point in the Cms filesystem.
     * @param expottpath The physical location this export point directs to. 
     */
    public CmsExportPoint(String exportpoint, String exportpath) {
        m_exportpoint = exportpoint;
        m_exportpath = exportpath;
    }
    /**
     * Returns the exportpath of a CmsExportPoint Object.
     * 
     * @return The physical location this exportpoint directs to or null.
     */
    public String getExportPath() {
        return m_exportpath;
    }
    /**
     * Returns the exportpoint of a CmsExportPoint Object.
     * @return The exportpoint in the OpenCms filesystem.
     */
    public String getExportpoint() {
      return m_exportpoint;
    }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     * 
     * @return string-representation for this object.
     */
    public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[ExportPoint]:");
        output.append(m_exportpoint);
        output.append(" , Exportpath=");
        output.append(m_exportpath);
        return output.toString();
    }
}
