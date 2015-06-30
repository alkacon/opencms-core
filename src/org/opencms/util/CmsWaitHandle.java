/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Low-level utility class used for waiting for an action performed by another thread.<p>
 *
 * This is really a thin wrapper around the wait() and notifyAll() methods.
 */
public class CmsWaitHandle {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWaitHandle.class);

    /**
     * Waits for a maximum of waitTime, but returns if another thread calls release().<p>
     *
     * @param waitTime the maximum wait time
     */
    public synchronized void enter(long waitTime) {

        try {
            wait(waitTime);
        } catch (InterruptedException e) {
            // should never happen, but log it just in case...
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Releases all currently waiting threads.<p>
     */
    public synchronized void release() {

        notifyAll();
    }

}
