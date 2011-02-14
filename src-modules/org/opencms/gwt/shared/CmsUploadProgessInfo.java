/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsUploadProgessInfo.java,v $
 * Date   : $Date: 2011/02/14 13:05:55 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsUploadProgessInfo implements IsSerializable {

    /** Stores the bytes that are already read. */
    private long m_bytesRead;

    /** The total content length. */
    private long m_contentLength;

    /** The index of the current file. */
    private int m_currentFile;

    /** The current percentage. */
    private int m_percent;

    /** Signals whether the upload is running or not. */
    private boolean m_running;

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
     * @param running signals if the upload listener is available or not
     * @param contentLength the content length of the upload request
     * @param bytesRead the count of bytes read so far
     */
    public CmsUploadProgessInfo(int currentFile, int percent, boolean running, long contentLength, long bytesRead) {

        m_currentFile = currentFile;
        m_percent = percent;
        m_running = running;
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
     * Returns the running.<p>
     *
     * @return the running
     */
    public boolean isRunning() {

        return m_running;
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
     * Sets the running.<p>
     *
     * @param running the running to set
     */
    public void setRunning(boolean running) {

        m_running = running;
    }
}
