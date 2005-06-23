/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/I_CmsImportExportHandler.java,v $
 * Date   : $Date: 2005/06/23 10:47:09 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.xml.CmsXmlException;

import org.dom4j.Document;

/**
 * An import/export handler is an abstract layer to hide the logic how to import/export a specific 
 * type of Cms data.<p>
 * 
 * To export data, you would create an instance of a class implementing this interface, and call the
 * implementation's setter methods to arrange which data should be exported. To write the export,
 * call {@link org.opencms.importexport.CmsImportExportManager#exportData(CmsObject, I_CmsImportExportHandler, I_CmsReport)}.<p>
 * 
 * To import data, call {@link org.opencms.importexport.CmsImportExportManager#importData(CmsObject, String, String, I_CmsReport)}.
 * You don't have to worry about the contents of an imported a ZIP archive - 
 * the import/export manager finds the right import/export handler implementation 
 * to import the data. You can assign null to the importPath argument in case of a Cms module import.<p>
 * 
 * Use {@link org.opencms.main.OpenCms#getImportExportManager()} to get the Cms import/export manager.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsImportExportHandler {

    /**
     * Exports the data from the Cms.<p>
     * 
     * @param cms the current OpenCms context object
     * @param report a Cms report to print log messages
     * @throws CmsImportExportException if operation was not successful  
     * @throws CmsRoleViolationException if the current user has not the required role 
     * @throws CmsConfigurationException if a specified module to be exproted does not exist
     */
    void exportData(CmsObject cms, I_CmsReport report)
    throws CmsConfigurationException, CmsImportExportException, CmsRoleViolationException;

    /**
     * Returns the description of this import/export handler.<p>
     * The description is useful to print some info about the purpose of this handler.<p>
     * 
     * @return the description of this import/export handler
     */
    String getDescription();

    /**
     * Imports the data into the Cms.<p>
     * 
     * @param cms the current OpenCms context object
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @param importPath the name (absolute path) of the destination folder in the Cms (if required)
     * @param report a Cms report to print log messages
     * 
     * @throws CmsImportExportException if operation was not successful 
     * @throws CmsRoleViolationException if the current user has not the required role 
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     * @throws CmsException in case of errors accessing the VFS
     */
    void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsXmlException, CmsImportExportException, CmsRoleViolationException, CmsException;

    /**
     * Checks, if this import/export handler matches with a specified manifest document of an import,
     * so that it is able to import the data listed in the manifest document.<p>
     * 
     * @param manifest the manifest.xml of the import as a dom4j XML document
     * @return true, this handler is able to import the data listed in the manifest document
     */
    boolean matches(Document manifest);

    /**
     * Sets the description of this import/export handler.<p>
     * The description is useful to print some info about the purpose of this handler.<p>
     * 
     * @param description the description of this import/export handler
     */
    void setDescription(String description);

}
