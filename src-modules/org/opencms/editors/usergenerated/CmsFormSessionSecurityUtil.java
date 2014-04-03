/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.editors.usergenerated;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPermissionViolationException;

import java.util.List;

/**
 * Helper class which implements some of the security checks for user generated content creation.<p>
 */
public class CmsFormSessionSecurityUtil {

    /** 
     * Hidden default constructor.<p>
     */
    private CmsFormSessionSecurityUtil() {

        // empty
    }

    /**
     * Checks whether a new XML content may be created and throws an exception if this is not the case.<p>
     * 
     * @param cms the current CMS context 
     * @param config the form configuration 
     * 
     * @throws CmsPermissionViolationException if creating a content would violate the limits set in the configuration
     * @throws CmsException for all other errors  
     */
    public static void checkCreateContent(CmsObject cms, CmsFormConfiguration config)
    throws CmsPermissionViolationException, CmsException {

        if (config.getMaxContentNumber().isPresent()) {
            int maxContents = config.getMaxContentNumber().get().intValue();
            String sitePath = cms.getSitePath(config.getContentParentFolder());
            if (cms.getFilesInFolder(sitePath).size() >= maxContents) {
                throw new CmsPermissionViolationException(Messages.get().container(
                    Messages.ERR_TOO_MANY_CONTENTS_1,
                    config.getContentParentFolder()));
            }
        }
    }

    /**
     * Checks whether an  uploaded file can be created in the VFS, and throws an exception otherwise.
     * 
     * @param cms the current CMS context 
     * @param config the form configuration 
     * @param name the file name of the uploaded file 
     * @param size the size of the uploaded file 
     * 
     * @throws CmsPermissionViolationException if creating the upload in the VFS would violate the limits set in the configuration
     * @throws CmsException for all other errors 
     */
    public static void checkCreateUpload(CmsObject cms, CmsFormConfiguration config, String name, long size)
    throws CmsPermissionViolationException, CmsException {

        if (!config.getUploadParentFolder().isPresent()) {
            throw new CmsPermissionViolationException(Messages.get().container(Messages.ERR_NO_UPLOADS_ALLOWED_0));
        }

        if (config.getMaxUploadSize().isPresent()) {
            if (config.getMaxUploadSize().get().longValue() < size) {
                throw new CmsPermissionViolationException(Messages.get().container(Messages.ERR_UPLOAD_TOO_BIG_1, name));
            }
        }

        if (config.getValidExtensions().isPresent()) {
            List<String> validExtensions = config.getValidExtensions().get();
            boolean foundExtension = false;
            for (String extension : validExtensions) {
                if (name.toLowerCase().endsWith(extension.toLowerCase())) {
                    foundExtension = true;
                    break;
                }
            }
            if (!foundExtension) {
                throw new CmsPermissionViolationException(Messages.get().container(
                    Messages.ERR_UPLOAD_FILE_EXTENSION_NOT_ALLOWED_1,
                    name));
            }
        }
    }
}
