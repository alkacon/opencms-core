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

package org.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;

/**
 * The default upload folder handler.<p>
 */
public class CmsRepositoryFolderHandler implements I_CmsRepositoryFolderHandler {

    /**
     * @see org.opencms.workplace.I_CmsRepositoryFolderHandler#getRepositoryFolder(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getRepositoryFolder(CmsObject cms, String reference, String type) {

        String result = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(reference)) {
            try {
                CmsProperty prop = cms.readPropertyObject(reference, "repositoryfolder_" + type, true);
                // check if the resource exists, is a folder and the user has write permissions to it
                if (!prop.isNullProperty()
                    && cms.existsResource(prop.getValue(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    CmsResource uploadFolder = cms.readResource(
                        prop.getValue(),
                        CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    if (uploadFolder.isFolder()
                        && cms.hasPermissions(
                            uploadFolder,
                            CmsPermissionSet.ACCESS_WRITE,
                            false,
                            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                        result = cms.getSitePath(uploadFolder);
                    }

                }
            } catch (CmsException e) {
                // ignore
            }
        }
        return result;
    }
}
