/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/I_CmsImport.java,v $
 * Date   : $Date: 2005/04/24 11:20:30 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import java.io.File;
import java.security.MessageDigest;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.dom4j.Document;

/**
 * This interface describes a import class which is used to import resources into the VFS.<p>
 * 
 * OpenCms supports different import versions, for each version a own import class must be implemented.
 * A group of common used methods can be found in {@link org.opencms.importexport.A_CmsImport}.<p>
 * 
 * Implementations of this interface must be registered in the registry in the <code>importclasses</code> node,
 * e.g.:<br>
 * 
 * <code>
 * <importclasses>
 * <class>[your_complete_classname_incl._packages]</class>
 * </importclasses>
 * </code>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
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
     * <li>4 indicates an export file of OpenCms with a version after 5.1.6</li>
     * </ul>
     * 
     * @return the version of the import implementation
     */
    int getVersion();

    /**
     * Imports the resources.<p>
     * 
     * @param cms the current cms object
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * @param digest digest for taking a fingerprint of the files
     * @param importResource  the import-resource (folder) to load resources from
     * @param importZip the import-resource (zip) to load resources from
     * @param docXml the xml manifest-file
     * @param excludeList filenames of files and folders which should not be (over)written in the virtual file system (not used when null)
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written (not used when null)
     * @param fileCodes code of the written files (for the registry) (not used when null)
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @throws CmsException if something goes wrong
     */
    void importResources(
        CmsObject cms,
        String importPath,
        I_CmsReport report,
        MessageDigest digest,
        File importResource,
        ZipFile importZip,
        Document docXml,
        Vector excludeList,
        Vector writtenFilenames,
        Vector fileCodes,
        String propertyName,
        String propertyValue) throws CmsException;

}
