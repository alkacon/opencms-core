/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsMultipartInputStreamHandler.java,v $
* Date   : $Date: 2005/04/10 11:00:14 $
* Version: $Revision: 1.21 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * A class to aid in reading multipart/form-data from a ServletInputStream.
 * <p>
 * It keeps track of how many bytes have been read and detects when the
 * Content-Length limit has been reached.  This is necessary since some
 * servlet engines are slow to notice the end of stream.
 *
 * @author Michael Emmerich
 * @author Alexander Lucas
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
class CmsMultipartInputStreamHandler {
    
    private ServletInputStream m_in;
    private int m_totalExpected;
    private int m_totalRead;
    private int m_newLine;

    /**
     * Constructor, creates a new CmsMultipartInputStreamHandler.<p>
     *
     * @param in An input stream
     * @param totalExpected Number of bytes expected to be read
     */
    public CmsMultipartInputStreamHandler(ServletInputStream in, int totalExpected) {
        m_in = in;
        m_totalExpected = totalExpected;
    }

    /** A pass-through to ServletInputStream.read() that keeps track
     * how many bytes have been read and stops reading when the
     * Content-Length limit has been reached.
     *
     * @return value of the next byte or -1 if no byte could be read.
     * @throws IOException Throws IOException if any error with the input stream occurs.
     */
    public int read() throws IOException {
        if (m_totalRead >= m_totalExpected) {
            return -1;
        } else {
            int result = m_in.read();
            if (result > -1) {
                m_totalRead++;
            }
            return result;
        }
    }

    /** Reads the next line of input.  Returns null to indicate the end
     * of stream.
     *
     * @return Line of input.
     * @throws IOException Throws IOException if any error with the input stream occurs.
     */
    public String readLine() throws IOException {
        byte[] buf = new byte[64 * 1024];
        StringBuffer sbuf = new StringBuffer();
        int result;

        // loop only if the buffer was filled
        do {

            // this.readLine() does +=
            result = this.readLine(buf, 0, buf.length);
            if (result != -1) {
                sbuf.append(new String(buf, 0, result, "ISO-8859-1"));
            }
        } while(result == buf.length);
        if (sbuf.length() == 0) {
            // nothing read, must be at the end of stream
            return null;
        }
        // cut off the trailing newline
        if (m_newLine == 0) {
            m_newLine = (result > 1 && (buf[result - 2] == '\r' || buf[result - 2] == '\n')) ? 2 : 1;
        }
        buf = null;
        sbuf.setLength(sbuf.length() - m_newLine);
        return sbuf.toString();
    }

    /** A pass-through to ServletInputStream.readLine() that keeps track
     * how many bytes have been read and stops reading when the
     * Content-Length limit has been reached.
     *
     * @param b Array of bytes.
     * @param off Read offset.
     * @param len Length of byte buffer.
     * @return Number of bytes read.
     * @throws IOException Throws IOException if any error with the input stream occurs.
     */
    public int readLine(byte[] b, int off, int len) throws IOException {
        if (m_totalRead >= m_totalExpected) {
            return -1;
        } else {
            int result = m_in.readLine(b, off, len);
            if (result > 0) {
                m_totalRead += result;
            }
            return result;
        }
    }
}
