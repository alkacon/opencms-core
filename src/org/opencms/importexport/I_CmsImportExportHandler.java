/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/I_CmsImportExportHandler.java,v $
 * Date   : $Date: 2004/02/25 14:12:43 $
 * Version: $Revision: 1.3 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;

/**
 * An import/export handler is an abstract layer to hide the logic how to import/export a specific 
 * type of Cms data.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2004/02/25 14:12:43 $
 * @since 5.3
 */
public interface I_CmsImportExportHandler {

    /** The default description being used in all handler implementations.<p> */
    String C_DEFAULT_DESCRIPTION = "No description available for this handler";
    
    /**
     * Returns the type of this import/export handler.<p>
     * 
     * @return the type of this import/export handler
     */
    String getType();
    
    /**
     * Sets the description of this import/export handler.<p>
     * The description is useful to print some info about the purpose of this handler.<p>
     * 
     * @param description the description of this import/export handler
     */
    void setDescription(String description);
    
    /**
     * Returns the description of this import/export handler.<p>
     * The description is useful to print some info about the purpose of this handler.<p>
     * 
     * @return the description of this import/export handler
     */
    String getDescription();
    
    /**
     * Exports the data from the Cms.<p>
     * 
     * @param cms the current OpenCms context object
     * @param report a Cms report to print log messages
     * @throws CmsException if operation was not successful   
     */
    void exportData(CmsObject cms, I_CmsReport report) throws CmsException;
    
    /**
     * Imports the data into the Cms.<p>
     * 
     * @param cms the current OpenCms context object
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @param importPath the name (absolute path) of the destination folder in the Cms in case of a COS/VFS import
     * @param report a Cms report to print log messages
     * @throws CmsException if operation was not successful 
     */
    void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException;
    
}
