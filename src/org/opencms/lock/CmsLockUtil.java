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

package org.opencms.lock;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.gwt.Messages;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Locking utility class.<p>
 */
public final class CmsLockUtil {

    /** Helper to handle the lock reports together with the files. */
    public static final class LockedFile {

        /** The cms object used for locking, unlocking and encoding determination. */
        private CmsObject m_cms;
        /** The file that was read. */
        private CmsFile m_file;
        /** The lock action record from locking the file. */
        private CmsLockActionRecord m_lockRecord;
        /** Flag, indicating if the file was newly created. */
        private boolean m_new;

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
         *
         * @see CmsFileUtil#getEncoding(CmsObject, CmsResource)
         *
         * @return the encoding used for the file.
         */
        public String getEncoding() {

            return CmsFileUtil.getEncoding(m_cms, m_file);

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

            if (!m_lockRecord.getChange().equals(LockChange.unchanged) || m_new) {
                m_cms.unlockResource(m_file);
                return true;
            } else {
                return false;
            }
        }

    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLockUtil.class);

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

    /**
     * Utility method for locking and unlocking a set of resources conveniently with the try-with syntax
     * from Java 1.7.<p>
     *
     * This method locks a set of resources and returns a Closeable instance that will unlock the locked resources
     * when its close() method is called.
     *
     * @param cms the CMS context
     * @param resources the resources to lock
     *
     * @return the Closeable used to unlock the resources
     * @throws Exception if something goes wrong
     */
    public static Closeable withLockedResources(final CmsObject cms, CmsResource... resources) throws Exception {

        final Map<CmsResource, CmsLockActionRecord> lockMap = Maps.newHashMap();
        Closeable result = new Closeable() {

            @SuppressWarnings("synthetic-access")
            public void close() {

                for (Map.Entry<CmsResource, CmsLockActionRecord> entry : lockMap.entrySet()) {
                    if (entry.getValue().getChange() == LockChange.locked) {
                        CmsResource resourceToUnlock = entry.getKey();
                        // the resource may have been moved, so we read it again to get the correct path
                        try {
                            resourceToUnlock = cms.readResource(entry.getKey().getStructureId(), CmsResourceFilter.ALL);
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                        try {
                            cms.unlockResource(resourceToUnlock);
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }

                }
            }
        };
        try {
            for (CmsResource resource : resources) {
                CmsLockActionRecord record = ensureLock(cms, resource);
                lockMap.put(resource, record);
            }
        } catch (CmsException e) {
            result.close();
            throw e;
        }
        return result;
    }

}
