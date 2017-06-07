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

package org.opencms.jlan;

import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Byte buffer class which expands dynamically if bytes are written to its end.<p>
 */
public class CmsByteBuffer {

    /** The internal byte buffer. */
    private byte[] m_buffer;

    /** The current size (may be less than the length of the byte buffer). */
    private int m_size;

    /**
     * Creates a new instance.<p>
     */
    public CmsByteBuffer() {

        this(5);
    }

    /**
     * Creates a new instance with a given initial capacity.<p>
     *
     * @param capacity the initial capacity
     */
    public CmsByteBuffer(int capacity) {

        m_buffer = new byte[Math.max(capacity, 5)];
        m_size = 0;
    }

    /**
     * Gets the current length of the internal byte buffer, which is the number of bytes this buffer
     * can contain before a new buffer is allocated.<p>
     *
     * @return the current length of the internal buffer
     */
    public int getCapacity() {

        return m_buffer.length;
    }

    /**
     * Transfers bytes from this buffer to a target byte array.<p>
     *
     * @param dest the byte array to which the bytes should be transferred
     * @param srcStart the start index in this buffer
     * @param destStart the start index in the destination buffer
     *
     * @param len  the number of bytes to transfer
     */
    public void readBytes(byte[] dest, int srcStart, int destStart, int len) {

        System.arraycopy(m_buffer, srcStart, dest, destStart, len);

    }

    /**
     * Returns the logical size of this buffer (which may be less than its capacity).<p>
     *
     * @return the buffer size
     */
    public int size() {

        return m_size;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        List<String> fragments = new ArrayList<String>();
        fragments.add("[");
        int i = 0;
        for (byte b : m_buffer) {
            if (i == m_size) {
                fragments.add("|");
            }
            fragments.add("" + b);
            i += 1;
        }
        fragments.add("]");
        return CmsStringUtil.listAsString(fragments, " ");
    }

    /**
     * Changes the logical size of this buffer.<p>
     *
     * If the size is larger than the current size, the new space will be filled with 0s.
     * Note that the internal byte buffer will not be shrunk if the size is smaller than the current
     * size.<p>
     *
     * @param size the new size
     */
    public void truncate(int size) {

        ensureCapacity(size);
        int minSize = Math.min(size, m_size);
        int maxSize = Math.max(size, m_size);
        Arrays.fill(m_buffer, minSize, maxSize, (byte)0);
        m_size = size;
    }

    /**
     * Writes some bytes to this buffer, expanding the buffer if necessary.<p>
     *
     * @param src the source from which to write the bytes
     * @param srcStart the start index in the source array
     * @param destStart the start index in this buffer
     *
     * @param len the number of bytes to write
     */
    public void writeBytes(byte[] src, int srcStart, int destStart, int len) {

        int newEnd = destStart + len;
        ensureCapacity(newEnd);
        if (newEnd > m_size) {
            m_size = newEnd;
        }
        System.arraycopy(src, srcStart, m_buffer, destStart, len);
    }

    /**
     * Make sure that the internal byte buffer can store at least requestedCapacity of bytes
     * by reallocating it if necessary.<p>
     *
     * @param requestedCapacity the requested capacity
     */
    private void ensureCapacity(int requestedCapacity) {

        if (requestedCapacity > getCapacity()) {
            int newCapacity = getCapacity();
            while (requestedCapacity > newCapacity) {
                newCapacity = (newCapacity * 3) / 2;
            }
            reallocateBuffer(newCapacity);
        }
    }

    /**
     * Reallocates the internal byte buffer with a new capacity.<p>
     *
     * @param newCapacity the new capacity
     */
    private void reallocateBuffer(int newCapacity) {

        byte[] newBuffer = new byte[newCapacity];
        System.arraycopy(m_buffer, 0, newBuffer, 0, Math.min(m_size, newCapacity));
        m_buffer = newBuffer;
    }

}
