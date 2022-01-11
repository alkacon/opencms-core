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

package org.opencms.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Provides Vfs utility functions.<p>
 *
 * @since 11.0.0
 */
public final class CmsVfsUtil {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsUtil.class);

    /**
     * Hides the public constructor.<p>
     */
    private CmsVfsUtil() {

        // empty
    }

    /**
     * Creates a folder and its parent folders if they don't exist.<p>
     *
     * @param cms the CMS context to use
     * @param rootPath the folder root path
     *
     * @throws CmsException if something goes wrong
     */
    public static void createFolder(CmsObject cms, String rootPath) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        List<String> parents = new ArrayList<String>();
        String currentPath = rootPath;
        while (currentPath != null) {
            if (rootCms.existsResource(currentPath)) {
                break;
            }
            parents.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        parents = Lists.reverse(parents);
        for (String parent : parents) {
            try {
                rootCms.createResource(
                    parent,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
                try {
                    rootCms.unlockResource(parent);
                } catch (CmsException e) {
                    // may happen if parent folder is locked also
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsVfsResourceAlreadyExistsException e) {
                // nop
            }
        }
    }

}
