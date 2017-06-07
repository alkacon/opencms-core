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

package org.opencms.publish;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Performs some additional checks on publish lists to prevent inconsistent VFS states.<p>
 */
public class CmsPublishListVerifier {

    /**
     * Entry for parent folders in which it is not allowed to publish resources.<p>
     *
     * Also contains a field for the reason why publishing is not allowed.
     */
    private class ForbiddenFolderEntry {

        /** Reason why publishing is not allowed. */
        String m_reason;

        /** Root path of the forbidden parent folder. */
        String m_rootPath;

        /**
         * Creates a new entry.<p>
         *
         * @param rootPath the root path of the forbidden entry
         * @param reason the reason why publishing is not allowed
         */
        public ForbiddenFolderEntry(String rootPath, String reason) {
            m_rootPath = rootPath;
            m_reason = reason;
        }

        /**
         * Returns the reason.<p>
         *
         * @return the reason
         */
        public String getReason() {

            return m_reason;
        }

        /**
         * Returns the rootPath.<p>
         *
         * @return the rootPath
         */
        public String getRootPath() {

            return m_rootPath;
        }
    }

    /** Forbidden parent folders, with UUIDs as keys. */
    private ConcurrentHashMap<CmsUUID, ForbiddenFolderEntry> m_forbiddenParentFolders = new ConcurrentHashMap<CmsUUID, ForbiddenFolderEntry>();

    /**
     * Creates a new instance.<p>
     */
    public CmsPublishListVerifier() {
        // do nothing
    }

    /**
     * Adds a forbidden parent folder.<p>
     *
     * @param parentFolder the forbidden parent folder
     * @param reason the reason why publishing is not allowed
     * @return an ID which can be used later to remove the forbidden parent folder
     */
    public CmsUUID addForbiddenParentFolder(String parentFolder, String reason) {

        CmsUUID id = new CmsUUID();
        m_forbiddenParentFolders.put(id, new ForbiddenFolderEntry(parentFolder, reason));
        return id;
    }

    /**
     * Checks whether the given publish job is OK, and throws an exception otherwise.<p>
     *
     * @param publishList the publish list
     * @throws CmsException if there's something wrong with the publish job
     */
    public void checkPublishList(CmsPublishList publishList) throws CmsException {

        for (CmsResource resource : publishList.getAllResources()) {
            for (ForbiddenFolderEntry entry : m_forbiddenParentFolders.values()) {
                if (CmsStringUtil.isPrefixPath(entry.getRootPath(), resource.getRootPath())) {
                    throw new CmsPublishException(
                        Messages.get().container(
                            Messages.ERR_PUBLISH_FORBIDDEN_PARENT_FOLDER_3,
                            resource.getRootPath(),
                            entry.getRootPath(),
                            entry.getReason()));
                }
            }

        }
    }

    /**
     * Removes the forbidden parent folder using the id obtained while it was added.<p>
     *
     * @param id the id to remove the parent folder
     */
    public void removeForbiddenParentFolder(CmsUUID id) {

        m_forbiddenParentFolders.remove(id);
    }

}
