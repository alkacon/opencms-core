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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.test.OpenCmsTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @since 6.2.2
 */
public class TestCmsFileUtil extends OpenCmsTestCase {

    /**
     * An input stream that provides 24 bytes of data in two chunks, the first of 16 bytes,
     * the second of 8 bytes.<p>
     *
     * This simulates the behavior of calls to <code>available()</code> and <code>read()</code> on
     * buffered file or zip input streams, where the size of the underlying data slightly exceeds the size
     * of the stream's internal buffers.<p>
     */
    private static class TestInputStream extends InputStream {

        private boolean m_closed;
        private ByteArrayInputStream m_firstChunk;
        private ByteArrayInputStream m_secondChunk;

        /**
         * Input stream implementation that exploits an error with {@link CmsFileUtil#readFully(InputStream)}.<p>
         */
        TestInputStream() {

            byte[] first = new byte[16];
            byte[] second = new byte[8];

            Arrays.fill(first, (byte)1);
            Arrays.fill(second, (byte)2);

            m_firstChunk = new ByteArrayInputStream(first);
            m_secondChunk = new ByteArrayInputStream(second);
        }

        /**
         * @see java.io.InputStream#available()
         */
        @Override
        public int available() {

            if (m_closed) {
                return 0;
            }

            int first = m_firstChunk.available();
            return first > 0 ? first : m_secondChunk.available();
        }

        /**
         * @see java.io.InputStream#close()
         */
        @Override
        public void close() throws IOException {

            //mark that close was called for testing purposes.
            m_closed = true;
            super.close();
        }

        /**
         * Returns <code>true</code> if this stream was closed.<p>
         *
         * @return <code>true</code> if this stream was closed
         */
        public boolean isClosed() {

            return m_closed;
        }

        /**
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {

            if (m_closed) {
                throw new IOException("Stream was closed");
            }

            return m_firstChunk.available() > 0 ? m_firstChunk.read() : m_secondChunk.read();
        }

        /**
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {

            if (m_closed) {
                throw new IOException("Stream was closed");
            }

            int first = m_firstChunk.available();
            if (first > 0) {
                return m_firstChunk.read(b, off, len);
            } else {
                return m_secondChunk.read(b, off, len);
            }
        }
    }

    /**
     * Test the behavior of {@link CmsFileUtil#readFully(InputStream)}
     * when the read takes more than one iteration to complete.<p>
     *
     * @throws IOException in case the test fails
     */
    public void testMultiPassReadFully() throws IOException {

        // this test is written especially to exploit an array bounds bug (bug 1131)
        // if the first call to available() returns
        // a value greater than the second call to available(), an IndexOutOfBoundsException is thrown.
        TestInputStream is = new TestInputStream();
        byte[] data = CmsFileUtil.readFully(is);
        assertNotNull("new byte array returned by readFully", data);

        // test that all the data from both chunks of the input stream was read correctly.
        assertEquals("all data returned", 24, data.length);
        for (int i = 0; i < 16; ++i) {
            assertEquals("first chunk data[" + i + "] has correct value", 1, data[i]);
        }
        for (int i = 16; i < 24; ++i) {
            assertEquals("second chunk data[" + i + "] has correct value", 2, data[i]);
        }

        assertTrue("input stream was closed", is.isClosed());
    }
}