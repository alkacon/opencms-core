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

package org.opencms.lock;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.gwt.Messages;
import org.opencms.i18n.CmsEncoder;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.List;

/**
 * Locking utility class.<p>
 */
public final class CmsLockUtil {

    /** Helper to handle the lock reports together with the files. */
    public static final class LockedFile {

        /** The file that was read. */
        private CmsFile m_file;
        /** The lock action record from locking the file. */
        private CmsLockActionRecord m_lockRecord;
        /** Flag, indicating if the file was newly created. */
        private boolean m_new;
        /** The cms object used for locking, unlocking and encoding determination. */
        private CmsObject m_cms;

        /** Private constructor.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         * @throws CmsException thrown if locking fails.
         */
        private LockedFile(CmsObject cms, CmsResource resource)
        throws CmsException {
            m_lockRecord = CmsLockUtil.ensureLock(cms, resource);
            m_file = cms.readFile(resource);
            m_new = false;
            m_cms = cms;
        }

        /**
         * Lock and read a file.
         * @param cms the cms user context.
         * @param resource the resource to lock and read.
         * @return the read file with the lock action record.
         * @throws CmsException thrown if locking fails
         */
        public static LockedFile lockResource(CmsObject cms, CmsResource resource) throws CmsException {

            return new LockedFile(cms, resource);
        }

        /**
         * Returns the encoding used for the file.
         * @return the encoding used for the file.
         */
        public String getEncoding() {

            CmsProperty encodingProperty = CmsProperty.getNullProperty();
            try {
                encodingProperty = m_cms.readPropertyObject(
                    m_file,
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    true);
            } catch (CmsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return CmsEncoder.lookupEncoding(
                encodingProperty.getValue(""),
                OpenCms.getSystemInfo().getDefaultEncoding());

        }

        /** Returns the file.
         * @return the file.
         */
        public CmsFile getFile() {

            return m_file;
        }

        /** Returns the lock action record.
         * @return the lock action record.
         */
        public CmsLockActionRecord getLockActionRecord() {

            return m_lockRecord;
        }

        /**
         * Returns a flag, indicating if the file is newly created.
         * @return flag, indicating if the file is newly created.
         */
        public boolean isCreated() {

            return m_new;
        }

        /**
         * Set the flag, indicating if the file was newly created.
         * @param isNew flag, indicating if the file was newly created.
         */
        public void setCreated(boolean isNew) {

            m_new = isNew;

        }

        public boolean unlock() throws CmsException {

            if (!m_lockRecord.getChange().equals(LockChange.unchanged)) {
                m_cms.unlockResource(m_file);
                return true;
            } else {
                return false;
            }
        }

    }

    /**
     * Hidden constructor.
     */
    private CmsLockUtil() {
        // Hide constructor for util class
    }

    /**
     * Static helper method to lock a resource.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource to lock
     * @return the action that was taken
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsLockActionRecord ensureLock(CmsObject cms, CmsResource resource) throws CmsException {

        LockChange change = LockChange.unchanged;
        List<CmsResource> blockingResources = cms.getBlockingLockedResources(resource);
        if ((blockingResources != null) && !blockingResources.isEmpty()) {
            throw new CmsException(
                Messages.get().container(
                    Messages.ERR_RESOURCE_HAS_BLOCKING_LOCKED_CHILDREN_1,
                    cms.getSitePath(resource)));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
            change = LockChange.locked;
            lock = cms.getLock(resource);
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
            change = LockChange.changed;
            lock = cms.getLock(resource);
        }
        return new CmsLockActionRecord(lock, change);
    }

}
