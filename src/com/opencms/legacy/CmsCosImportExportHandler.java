/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsCosImportExportHandler.java,v $
 * Date   : $Date: 2004/02/23 17:40:58 $
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
 
package com.opencms.legacy;

import org.opencms.importexport.I_CmsImportExportHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Import/export handler implementation for COS data.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/02/23 17:40:58 $
 * @since 5.3
 */
public class CmsCosImportExportHandler extends Object implements I_CmsImportExportHandler {
    
    /** The type of this import/export handler.<p> */
    private String m_type; 
    
    /** The name of the export file in the real file system.<p> */
    private String m_fileName;
    
    /** The COS channels to be exported.<p> */
    private List m_exportChannels; 
    
    /** The COS modules to be exported.<p> */
    private List m_exportModules;    

    /**
     * Creates a new COS import/export handler.<p>
     */
    public CmsCosImportExportHandler() {
        super();
        m_type = C_TYPE_COSDATA;
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_exportChannels != null) {
                m_exportChannels.clear();
            }
            m_exportChannels = null;
            
            if (m_exportModules != null) {
                m_exportModules.clear();
            }
            m_exportModules = null;            
        } catch (Exception e) {
            // noop
        }
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
     * Returns the COS channels to be exported.<p>
     * 
     * @return the COS channels to be exported
     */
    public String[] getExportChannels() {
        return (String[]) m_exportChannels.toArray();
    }
    
    /**
     * Returns the COS channels to be exported as a list.<p>
     * 
     * @return the COS channels to be exported as a list
     */
    public List getExportChannelsAsList() {
        return m_exportChannels;
    }    

    /**
     * Returns the COS modules to be exported.<p>
     * 
     * @return the COS modules to be exported
     */
    public String[] getExportModules() {
        return (String[]) m_exportModules.toArray();
    }
    
    /**
     * Returns the COS modules to be exported as a list.<p>
     * 
     * @return the COS modules to be exported as a list
     */
    public List getExportModulesAsList() {
        return m_exportModules;
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
     * Sets the COS channels to be exported.<p>
     * 
     * @param exportChannels the COS channels to be exported
     */
    public void setExportChannels(String[] exportChannels) {
        m_exportChannels = Arrays.asList(exportChannels);
    }

    /**
     * Sets the COS modules to be exported.<p>
     * 
     * @param exportModules the COS modules to be exported
     */
    public void setExportModules(String[] exportModules) {
        m_exportModules = Arrays.asList(exportModules);
    }

    /**
     * Sets the name of the export file in the real file system.<p>
     * 
     * @param fileName the name of the export file in the real file system
     */
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

}
