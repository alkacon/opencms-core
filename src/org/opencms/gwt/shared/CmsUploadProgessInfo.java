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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean that holds the upload progress infos.<p>
 *
 * @since 8.0.0
 */
public class CmsUploadProgessInfo implements IsSerializable {

    /** A enum for the upload state. */
    public static enum UPLOAD_STATE {

        /** Upload is finished. */
        finished, /** Upload not started. */
        notStarted, /** Upload is running. */
        running
    }

    /** Stores the bytes that are already read. */
    private long m_bytesRead;

    /** The total content length. */
    private long m_contentLength;

    /** The index of the current file. */
    private int m_currentFile;

    /** The current percentage. */
    private int m_percent;

    /** Stores the state. */
    private UPLOAD_STATE m_state;

    /**
     * Default constructor.<p>
     */
    public CmsUploadProgessInfo() {

        // noop
    }

    /**
     * Constructor with parameters.<p>
     *
     * @param currentFile the current file count
     * @param percent the progress in percent
     * @param state the state
     * @param contentLength the content length of the upload request
     * @param bytesRead the count of bytes read so far
     */
    public CmsUploadProgessInfo(int currentFile, int percent, UPLOAD_STATE state, long contentLength, long bytesRead) {

        m_currentFile = currentFile;
        m_percent = percent;
        m_state = state;
        m_contentLength = contentLength;
        m_bytesRead = bytesRead;
    }

    /**
     * Returns the bytesRead.<p>
     *
     * @return the bytesRead
     */
    public long getBytesRead() {

        return m_bytesRead;
    }

    /**
     * Returns the contentLength.<p>
     *
     * @return the contentLength
     */
    public long getContentLength() {

        return m_contentLength;
    }

    /**
     * Returns the currentFile.<p>
     *
     * @return the currentFile
     */
    public int getCurrentFile() {

        return m_currentFile;
    }

    /**
     * Returns the percent.<p>
     *
     * @return the percent
     */
    public int getPercent() {

        return m_percent;
    }

    /**
     * Returns the state.<p>
     *
     * @return the state
     */
    public UPLOAD_STATE getState() {

        return m_state;
    }

    /**
     * Sets the bytesRead.<p>
     *
     * @param bytesRead the bytesRead to set
     */
    public void setBytesRead(long bytesRead) {

        m_bytesRead = bytesRead;
    }

    /**
     * Sets the contentLength.<p>
     *
     * @param contentLength the contentLength to set
     */
    public void setContentLength(long contentLength) {

        m_contentLength = contentLength;
    }

    /**
     * Sets the currentFile.<p>
     *
     * @param currentFile the currentFile to set
     */
    public void setCurrentFile(int currentFile) {

        m_currentFile = currentFile;
    }

    /**
     * Sets the percent.<p>
     *
     * @param percent the percent to set
     */
    public void setPercent(int percent) {

        m_percent = percent;
    }

    /**
     * Sets the state.<p>
     *
     * @param state the state to set
     */
    public void setState(UPLOAD_STATE state) {

        m_state = state;
    }
}
