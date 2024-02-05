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

package org.opencms.ade.upload;

import org.opencms.main.CmsLog;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.logging.Log;

/**
 * A class which is executed in a new thread, so its able to detect
 * when an upload process is frozen and sets an exception in order to
 * be canceled. This doesn't work in Google application engine.<p>
 */
public class CmsUploadTimeoutWatcher extends Thread implements Serializable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUploadTimeoutWatcher.class);

    /** The serial version UID. */
    private static final long serialVersionUID = -649803529271569237L;

    /** The watchers interval. */
    private static final int WATCHER_INTERVAL = 5000;

    /** Last bytes read. */
    private long m_lastBytesRead;

    /** Timestamp for the last received data. */
    private long m_lastData = (new Date()).getTime();

    /** The listener to watch. */
    private CmsUploadListener m_listener;

    /**
     * A public constructor.<p>
     *
     * @param listener the listener to watch
     */
    public CmsUploadTimeoutWatcher(CmsUploadListener listener) {

        m_listener = listener;
    }

    /**
     * Cancels the watch process.<p>
     */
    public void cancel() {

        m_listener = null;
    }

    /**
     * The watching process.<p>
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        try {
            Thread.sleep(WATCHER_INTERVAL);
        } catch (InterruptedException e) {
            LOG.error(Messages.get().container(Messages.ERR_UPLOAD_INTERRUPT_WATCH_DOG_1, m_listener.toString()), e);
        }
        if (m_listener != null) {
            if (((m_listener.getBytesRead() > 0) && (m_listener.getPercent() >= 100)) || m_listener.isCanceled()) {
                LOG.debug(Messages.get().container(Messages.LOG_UPLOAD_FINISHED_WATCHER_1, m_listener.toString()));
                m_listener = null;
            } else {
                if (isFrozen()) {
                    m_listener.cancelUpload(new CmsUploadException(
                        Messages.get().getBundle().key(
                            Messages.ERR_UPLOAD_FROZEN_1,
                            Integer.valueOf(CmsUploadBean.DEFAULT_UPLOAD_TIMEOUT / 1000))));
                } else {
                    run();
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the upload process is frozen.<p>
     *
     * @return <code>true</code> if the upload process is frozen
     */
    private boolean isFrozen() {

        long now = (new Date()).getTime();

        if (m_listener.getBytesRead() > m_lastBytesRead) {
            m_lastData = now;
            m_lastBytesRead = m_listener.getBytesRead();
        } else if ((now - m_lastData) > CmsUploadBean.DEFAULT_UPLOAD_TIMEOUT) {
            return true;
        }
        return false;
    }
}
