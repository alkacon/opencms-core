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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * A DataSource backed by a byte array. The byte array may be passed in directly, or may be initialized from an InputStream or a String.
 *
 * @since 6.3.0
 */
public class CmsByteArrayDataSource implements DataSource {

    /** The MIME content type of the data. */
    private String m_contentType;

    /** The data. */
    private byte[] m_data;

    /** The name of the data. */
    private String m_name;

    /**
     * Creates a ByteArrayDataSource with data from the specified byte array and with the specified MIME type.
     *
     * @param name the name of the data
     * @param data the data
     * @param contentType the MIME content type of the data
     */
    public CmsByteArrayDataSource(String name, byte[] data, String contentType) {

        m_name = name;
        m_data = data;
        m_contentType = contentType;
    }

    /**
     *
     * @see javax.activation.DataSource#getContentType()
     */
    public String getContentType() {

        return m_contentType;
    }

    /**
     *
     * @see javax.activation.DataSource#getInputStream()
     */
    public InputStream getInputStream() {

        return new ByteArrayInputStream(m_data);
    }

    /**
     *
     * @see javax.activation.DataSource#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     *
     * @see javax.activation.DataSource#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {

        throw new IOException("ByteArrayDataSource cannot support getOutputStream( )");
    }
}
