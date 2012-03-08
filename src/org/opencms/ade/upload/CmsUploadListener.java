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

package org.opencms.ade.upload;

import org.opencms.ade.upload.shared.CmsUploadProgessInfo;
import org.opencms.ade.upload.shared.CmsUploadProgessInfo.UPLOAD_STATE;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.logging.Log;

/**
 * Provides the upload listener for the upload widget.<p>
 * 
 * @since 8.0.0 
 */
public class CmsUploadListener implements ProgressListener, Serializable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUploadListener.class);

    /** The serial version id. */
    private static final long serialVersionUID = -6431275569719042836L;

    /** The content length of the request (larger than the sum of file sizes). */
    protected long m_contentLength;

    /** Stores the exception if one has been occurred. */
    protected RuntimeException m_exception;

    /** Signals that there occurred an exception before. */
    protected boolean m_exceptionTrhown;

    /** The bytes read so far. */
    private long m_bytesRead;

    /** The upload delay. */
    private int m_delay;

    /** A flag that signals if the upload is finished. */
    private boolean m_finished;

    /** The UUID for this listener. */
    private CmsUUID m_id;

    /** Stores the current item. */
    private int m_item;

    /** The timeout watch dog for this listener. */
    private CmsUploadTimeoutWatcher m_watcher;

    /**
     * The public constructor for the listener.<p>
     * 
     * @param requestSize content length of the request (larger than the sum of file sizes)
     */
    public CmsUploadListener(int requestSize) {

        m_id = new CmsUUID();
        m_contentLength = new Long(requestSize).longValue();
        startWatcher();
    }

    /**
     * Sets the exception that should cancel the upload on the next update.<p>
     * 
     * @param e the exception 
     */
    public void cancelUpload(CmsUploadException e) {

        m_exception = e;
    }

    /**
     * Returns the bytes transfered so far.<p>
     * 
     * @return the bytes transfered so far
     */
    public long getBytesRead() {

        return m_bytesRead;
    }

    /**
     * Returns the content length of the request (larger than the sum of file sizes).<p>
     * 
     * @return the content length of the request (larger than the sum of file sizes)
     */
    public long getContentLength() {

        return m_contentLength;
    }

    /**
     * Returns the exception.<p>
     *
     * @return the exception
     */
    public RuntimeException getException() {

        return m_exception;
    }

    /**
     * Returns the listeners UUID.<p>
     * 
     * @return the listeners UUID
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the current progress info of the upload.<p>
     * 
     * @return the progress info
     */
    public CmsUploadProgessInfo getInfo() {

        if (m_finished) {
            return new CmsUploadProgessInfo(
                getItem(),
                (int)getPercent(),
                UPLOAD_STATE.finished,
                getContentLength(),
                getBytesRead());
        }
        return new CmsUploadProgessInfo(
            getItem(),
            (int)getPercent(),
            UPLOAD_STATE.running,
            getContentLength(),
            getBytesRead());
    }

    /**
     * Returns the number of the field, which is currently being read.<p>
     * <ul>
     * <li>0 = no item so far
     * <li>1 = first item is being read, ...
     * </ul>

     * @return the number of the field, which is currently being read.
     */
    public int getItem() {

        return m_item;
    }

    /**
     * Returns the percent done of the current upload.<p>
     * 
     * @return the percent done of the current upload
     */
    public long getPercent() {

        return m_contentLength != 0 ? (m_bytesRead * 100) / m_contentLength : 0;
    }

    /**
     * Returns <code>true</code> if the process has been canceled due to an error or by the user.<p>
     * 
     * @return boolean<code>true</code> if the process has been canceled due to an error or by the user
     */
    public boolean isCanceled() {

        return m_exception != null;
    }

    /**
     * Returns the finished.<p>
     *
     * @return the finished
     */
    public boolean isFinished() {

        return m_finished;
    }

    /**
     * Sets the delay.<p>
     *
     * @param delay the delay to set
     */
    public void setDelay(int delay) {

        m_delay = delay;
    }

    /**
     * Sets the finished.<p>
     *
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {

        m_finished = finished;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "UUID="
            + getId()
            + " total="
            + getContentLength()
            + " done="
            + getBytesRead()
            + " cancelled="
            + isCanceled();
    }

    /**
     * Updates the listeners status information and does the following steps:
     * <ul>
     * <li> returns if there was already thrown an exception before
     * <li> sets the local variables to the current upload state
     * <li> throws an RuntimeException if it was set in the meanwhile (by another request e.g. user has canceled)
     * <li> slows down the upload process if it's configured
     * <li> stops the watcher if the upload has reached more than 100 percent
     * </ul>
     * 
     * @see org.apache.commons.fileupload.ProgressListener#update(long, long, int)
     */
    public void update(long done, long total, int item) {

        if (m_exceptionTrhown) {
            return;
        }

        m_bytesRead = done;
        m_contentLength = total;
        m_item = item;

        // If an other request has set an exception, it is thrown so the commons-fileupload's 
        // parser stops and the connection is closed.
        if (isCanceled()) {
            m_exceptionTrhown = true;
            throw m_exception;
        }

        // Just a way to slow down the upload process and see the progress bar in fast networks.
        if ((m_delay > 0) && (done < total)) {
            try {
                Thread.sleep(m_delay);
            } catch (Exception e) {
                m_exception = new RuntimeException(e);
            }
        }
        if (getPercent() >= 100) {
            stopWatcher();
        }
    }

    /**
     * Starts the watcher.<p>
     */
    private void startWatcher() {

        if (m_watcher == null) {
            try {
                m_watcher = new CmsUploadTimeoutWatcher(this);
                m_watcher.start();
            } catch (Exception e) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_UPLOAD_CREATE_WATCH_DOG_2,
                    getId(),
                    e.getMessage()));
            }
        }
    }

    /**
     * Stops the watcher.<p>
     */
    private void stopWatcher() {

        if (m_watcher != null) {
            m_watcher.cancel();
        }
    }
}
