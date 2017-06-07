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

package org.opencms.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 *
 * DataSource implementation that may be filled with content from an {@link java.io.InputStream}.
 * <p>
 * It's intended use is for creation of mail attachments from strings without having to create RFS
 * or VFS resources. Note that this data source will only support read operations and operations
 * related to writing will throw an {@link java.lang.UnsupportedOperationException}.
 * <p>
 *
 * @since 6.1.7
 */
public class CmsInputStreamDataSource implements DataSource {

    /** The content type to use for the data source. */
    private String m_contentType;

    /** The underlying input stream of this data source. */
    private InputStream m_inputStream;

    /** The name of this data source. */
    private String m_name;

    /**
     * Constructor with mandatory input stream, content type and name.
     * <p>
     * Note that the given input stream has to be resettable. During a mail creation and
     * transmission cycle it is potentially read twice (commons-email-1.0.jar in combination with
     * activation.jar) and the 2nd time the actual transport to the serial data to transmit is done.
     * So a reset will be made here whenever the internal input stream is retrieved to avoid that
     * the attachments remain empty.
     * <p>
     * The contentType argument should always be a valid MIME type. It is suggested that it is
     * "application/octet-stream" if the DataSource implementation can not determine the data type.
     * For textual data it should be "text/&lt;subtype&gt;; charset=&lt;encoding&gt;" to give a hint
     * about the ecoding. Note that some textual documents like xml have their own encoding
     * directive contained and the charset given here (for the mail part header) should not be
     * different from the contained one.
     *
     *
     * @param in the underlying source of data.
     *
     * @param contentType the correct MIME type of the data along with the charset in the form of a
     *            string (see comment above).
     *
     * @param name the name that describes the data in the underyling input stream. E.g. the name of
     *            a file that the input stream reads from.
     *
     */
    public CmsInputStreamDataSource(InputStream in, String contentType, String name) {

        m_inputStream = in;
        m_contentType = contentType;
        m_name = name;
    }

    /**
     * @see javax.activation.DataSource#getContentType()
     */
    public String getContentType() {

        return m_contentType;
    }

    /**
     * Retunrs the underlying input stream of this data source.
     *
     * @return the underlying input stream of this data source.
     *
     * @throws IOException if the constructor-given input stream is not "resettable" ({@link InputStream#reset()}).
     *
     * @see javax.activation.DataSource#getInputStream()
     *
     */
    public InputStream getInputStream() throws IOException {

        m_inputStream.reset();
        return m_inputStream;
    }

    /**
     * @see javax.activation.DataSource#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Don't use this method, VFS resources can't be written using this datasource class.
     * <p>
     *
     * This method will just return a new <code>{@link ByteArrayOutputStream}</code>.
     * <p>
     *
     * @see javax.activation.DataSource#getOutputStream()
     */
    public OutputStream getOutputStream() {

        // maybe throw an Exception here to avoid errors
        return new ByteArrayOutputStream();
    }

}
