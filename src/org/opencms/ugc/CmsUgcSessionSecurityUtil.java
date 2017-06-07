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

package org.opencms.ugc;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ugc.shared.CmsUgcConstants;
import org.opencms.ugc.shared.CmsUgcException;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Helper class which implements some of the security checks for user generated content creation.<p>
 */
public class CmsUgcSessionSecurityUtil {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUgcSessionSecurityUtil.class);

    /**
     * Hidden default constructor.<p>
     */
    private CmsUgcSessionSecurityUtil() {

        // empty
    }

    /**
     * Checks whether a new XML content may be created and throws an exception if this is not the case.<p>
     *
     * @param cms the current CMS context
     * @param config the form configuration
     *
     *  @throws CmsUgcException if something goes wrong
     */
    public static void checkCreateContent(CmsObject cms, CmsUgcConfiguration config) throws CmsUgcException {

        if (config.getMaxContentNumber().isPresent()) {
            int maxContents = config.getMaxContentNumber().get().intValue();
            String sitePath = cms.getSitePath(config.getContentParentFolder());
            try {
                if (cms.getFilesInFolder(sitePath).size() >= maxContents) {

                    String message = Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                        Messages.ERR_TOO_MANY_CONTENTS_1,
                        config.getContentParentFolder());
                    throw new CmsUgcException(CmsUgcConstants.ErrorCode.errMaxContentsExceeded, message);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new CmsUgcException(e);
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
     *  @throws CmsUgcException if something goes wrong
     *
     */
    public static void checkCreateUpload(CmsObject cms, CmsUgcConfiguration config, String name, long size)
    throws CmsUgcException {

        if (!config.getUploadParentFolder().isPresent()) {
            String message = Messages.get().container(Messages.ERR_NO_UPLOADS_ALLOWED_0).key(
                cms.getRequestContext().getLocale());
            throw new CmsUgcException(CmsUgcConstants.ErrorCode.errNoUploadAllowed, message);
        }

        if (config.getMaxUploadSize().isPresent()) {
            if (config.getMaxUploadSize().get().longValue() < size) {
                String message = Messages.get().container(Messages.ERR_UPLOAD_TOO_BIG_1, name).key(
                    cms.getRequestContext().getLocale());
                throw new CmsUgcException(CmsUgcConstants.ErrorCode.errMaxUploadSizeExceeded, message);
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
                String message = Messages.get().container(Messages.ERR_UPLOAD_FILE_EXTENSION_NOT_ALLOWED_1, name).key(
                    cms.getRequestContext().getLocale());
                throw new CmsUgcException(CmsUgcConstants.ErrorCode.errInvalidExtension, message);
            }
        }
    }
}
