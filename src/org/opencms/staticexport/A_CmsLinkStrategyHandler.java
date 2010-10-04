/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/A_CmsLinkStrategyHandler.java,v $
 * Date   : $Date: 2010/10/04 14:53:39 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsSecurityException;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;

/**
 * Abstract base implementation for the <code>{@link I_CmsLinkStrategyHandler}</code> interface.<p>
 * 
 * @author Ruediger Kurz 
 *
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public abstract class A_CmsLinkStrategyHandler implements I_CmsLinkStrategyHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsLinkStrategyHandler.class);

    /**
     * Implements the file filter used to guess the right suffix of a deleted jsp file.<p>
     */
    protected static class CmsPrefixFileFilter implements FileFilter {

        /** The base file. */
        private String m_baseName;

        /**
         * Creates a new instance of this filter.<p>
         * 
         * @param fileName the base file to compare with.
         */
        public CmsPrefixFileFilter(String fileName) {

            m_baseName = fileName + ".";
        }

        /**
         * Accepts the given file if its name starts with the name of of the base file (without extension) 
         * and ends with the extension.<p>
         * 
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File f) {

            return f.getName().startsWith(m_baseName)
                && (f.getName().length() > m_baseName.length())
                && (f.getName().indexOf('.', m_baseName.length()) < 0);
        }
    }

    /**
     * Returns the rfs name for a given vfs name with consideration of the export name.<p>
     * 
     * @param cms the cms obejct
     * @param vfsName the the name of the vfs resource
     *
     * @return the rfs name for a given vfs name with consideration of the export name
     */
    protected String getRfsNameWithExportName(CmsObject cms, String vfsName) {

        String rfsName = vfsName;

        try {
            // check if the resource folder (or a parent folder) has the "exportname" property set
            CmsProperty exportNameProperty = cms.readPropertyObject(
                CmsResource.getFolderPath(rfsName),
                CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                true);

            if (exportNameProperty.isNullProperty()) {
                // if "exportname" is not set we must add the site root 
                rfsName = cms.getRequestContext().addSiteRoot(rfsName);
            } else {
                // "exportname" property is set
                String exportname = exportNameProperty.getValue();
                if (exportname.charAt(0) != '/') {
                    exportname = '/' + exportname;
                }
                if (exportname.charAt(exportname.length() - 1) != '/') {
                    exportname = exportname + '/';
                }
                String value = null;
                boolean cont;
                String resourceName = rfsName;
                do {
                    // find out where the export name was set, to replace these parent folders in the RFS name
                    try {
                        CmsProperty prop = cms.readPropertyObject(
                            resourceName,
                            CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                            false);
                        if (prop.isIdentical(exportNameProperty)) {
                            // look for the right position in path 
                            value = prop.getValue();
                        }
                        cont = (value == null) && (resourceName.length() > 1);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // this is for publishing deleted resources 
                        cont = (resourceName.length() > 1);
                    } catch (CmsSecurityException se) {
                        // a security exception (probably no read permission) we return the current result                      
                        cont = false;
                    }
                    if (cont) {
                        resourceName = CmsResource.getParentFolder(resourceName);
                    }
                } while (cont);
                rfsName = exportname + rfsName.substring(resourceName.length());
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            // ignore exception, return vfsName as rfsName
            rfsName = vfsName;
        }
        return rfsName;
    }
}
