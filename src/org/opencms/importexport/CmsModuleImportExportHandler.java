/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsModuleImportExportHandler.java,v $
 * Date   : $Date: 2004/02/23 17:49:50 $
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
 
package org.opencms.importexport;

import java.util.Arrays;
import java.util.List;

/**
 * Import/export handler implementation for Cms modules.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/02/23 17:49:50 $
 * @since 5.3
 */
public class CmsModuleImportExportHandler extends Object implements I_CmsImportExportHandler {
    
    /** The type of this import/export handler.<p> */
    private String m_type;  
    
    /** The name of the export file in the real file system.<p> */
    private String m_fileName;
    
    /** The VFS resources to exported additionally with the module.<p> */
    private List m_resources; 
    
    /** The (package) name of the module to be exported.<p> */
    private String m_moduleName;

    /**
     * Creates a new Cms module import/export handler.<p>
     */
    public CmsModuleImportExportHandler() {
        super();
        m_type = C_TYPE_MODDATA;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getType()
     */
    public String getType() {
        return m_type;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData()
     */
    public void exportData() {
        // not yet implemented
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData()
     */
    public void importData() {
        // not yet implemented
    }
    
    /**
     * Returns the name of the export file in the real file system.<p>
     * 
     * @return the name of the export file in the real file system
     */
    public String getFileName() {
        return m_fileName;
    }
    
    /**
     * Sets the name of the export file in the real file system.<p>
     * 
     * @param fileName the name of the export file in the real file system
     */
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }    

    /**
     * Returns the (package) name of the module to be exported.<p>
     * 
     * @return the (package) name of the module to be exported
     */
    public String getModuleName() {
        return m_moduleName;
    }

    /**
     * Returns the VFS resources to exported additionally with the module.<p>
     * 
     * @return the VFS resources to exported additionally with the module
     */
    public String[] getResources() {
        return (String[]) m_resources.toArray();
    }
    
    /**
     * Returns the VFS resources to exported additionally with the module as a list.<p>
     * 
     * @return the VFS resources to exported additionally with the module as a list
     */
    public List getResourcesAsList() {
        return m_resources;
    }    

    /**
     * Sets the (package) name of the module to be exported.<p>
     * 
     * @param moduleName the (package) name of the module to be exported
     */
    public void setModuleName(String moduleName) {
        m_moduleName = moduleName;
    }

    /**
     * Sets the VFS resources to exported additionally with the module.<p>
     * 
     * @param resources the VFS resources to exported additionally with the module
     */
    public void setResources(String[] resources) {
        m_resources = Arrays.asList(resources);
    }

}
