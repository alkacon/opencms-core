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

package org.opencms.jlan;

/**
 * Buffer class which holds file contents for JLAN file access in memory before they are written to the VFS.<p>
 * 
 * This is implemented as a CmsByteBuffer instance together with a 'position' index which marks the next write position
 */
public class CmsFileBuffer {

    /** The buffer used to store the file contents. */
    CmsByteBuffer m_buffer = new CmsByteBuffer(8192);

    /** The current write position. */
    long m_position;

    /**
     * Gets the contents of this buffer as a byte array.<p>
     * 
     * @return the file content 
     */
    public byte[] getContents() {

        byte[] contents = new byte[m_buffer.size()];
        m_buffer.readBytes(contents, 0, 0, m_buffer.size());
        return contents;
    }

    /**
     * Gets the length of the file content.<p>
     * 
     * @return the content length 
     *  
     */
    public long getLength() {

        return m_buffer.size();
    }

    /** 
     * Gets the current write position.<p>
     * 
     * @return the current write position 
     */
    public long getPosition() {

        return m_position;
    }

    /** 
     * Initializes the file content data.<p>
     * 
     * @param data the file content data 
     */
    public void init(byte[] data) {

        m_position = 0;
        m_buffer.writeBytes(data, 0, 0, data.length);
    }

    /**
     * Transfers data from this buffer to a byte array.<p>
     * 
     * @param dest the target byte array 
     * 
     * @param length the number of bytes to transfer
     * @param bufferOffset the start index for the target buffer 
     * @param fileOffset the start index for this instance 
     * 
     * @return the number of bytes read, or -1 if we are at the end of the file 
     */
    public int read(byte[] dest, int length, int bufferOffset, int fileOffset) {

        if (fileOffset >= m_buffer.size()) {
            return -1;
        }
        int readEnd = fileOffset + length;
        if (readEnd > m_buffer.size()) {
            length = length - (readEnd - m_buffer.size());
        }
        m_buffer.readBytes(dest, fileOffset, bufferOffset, length);
        return length;
    }

    /**
     * Changes the write position.<p>
     * 
     * @param newPos the new write position 
     */
    public void seek(long newPos) {

        m_position = newPos;
    }

    /**
     * Changes the size of this buffer.<p>
     * 
     * @param size the new size 
     */
    public void truncate(int size) {

        m_buffer.truncate(size);
        m_position = Math.min(size, m_position);
    }

    /**
     * Writes the data to the internal buffer.<p>
     * 
     * @param data the data to write 
     */
    public void write(byte[] data) {

        m_buffer.writeBytes(data, 0, (int)m_position, data.length);
    }
}
