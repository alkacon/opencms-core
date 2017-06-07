/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.report.I_CmsReport;
import org.opencms.xml.CmsXmlException;

import java.io.File;
import java.util.zip.ZipFile;

import org.dom4j.Document;

/**
 * This interface describes a import class which is used to import resources into the VFS.<p>
 *
 * OpenCms supports different import versions, for each version a own import class must be implemented.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsImport {

    /**
     * Returns the version of the import implementation.<p>
     *
     * <ul>
     * <li>0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms</li>
     * <li>1 indicates an export file of OpenCms with a version before 5.0.0</li>
     * <li>2 indicates an export file of OpenCms with a version before 5.1.2</li>
     * <li>3 indicates an export file of OpenCms with a version before 5.1.6</li>
     * <li>4 indicates an export file of OpenCms with a version before 6.3.0</li>
     * <li>5 indicates an export file of OpenCms with a version before 6.5.6</li>
     * <li>6 indicates an export file of OpenCms with a version before 7.0.4</li>
     * <li>7 indicates an export file of OpenCms with a version after 7.0.3</li>
     * </ul>
     *
     * @return the version of the import implementation
     */
    int getVersion();

    /**
     * Imports the data.<p>
     *
     * @param cms the current users OpenCms context
     * @param report a report object to output the progress information to
     * @param parameters the parameters to use during the import
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the manifest file could not be unmarshalled
     */
    void importData(CmsObject cms, I_CmsReport report, CmsImportParameters parameters)
    throws CmsImportExportException, CmsXmlException;

    /**
     * Imports the resources.<p>
     *
     * @param cms the current users OpenCms context
     * @param importPath the path in the OpenCms VFS to import into
     * @param report a report object to output the progress information to
     * @param importResource the import-resource (folder) to load resources from
     * @param importZip the import-resource (zip) to load resources from
     * @param docXml the <code>manifest.xml</code> file which contains the meta information of the imported files
     *
     * @throws CmsImportExportException if something goes wrong
     *
     * @deprecated use {@link #importData(CmsObject, I_CmsReport, CmsImportParameters)} instead
     */
    @Deprecated
    void importResources(
        CmsObject cms,
        String importPath,
        I_CmsReport report,
        File importResource,
        ZipFile importZip,
        Document docXml) throws CmsImportExportException;

    /**
     * Checks if the file given as parameter matches this import version implementation.<p>
     *
     * @param parameters the parameters to use during matching
     *
     * @return <code>true</code> if the file can be imported by this import version implementation
     *
     * @throws CmsImportExportException if something goes wrong
     */
    boolean matches(CmsImportParameters parameters) throws CmsImportExportException;
}