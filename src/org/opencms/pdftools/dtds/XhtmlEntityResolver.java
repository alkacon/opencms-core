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

package org.opencms.pdftools.dtds;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity resolver for XHTML entities.<p>
 */
public class XhtmlEntityResolver implements EntityResolver {

    /** The XHTML prefix. */
    static final String XHTML_PREFIX = "http://www.w3.org/TR/xhtml1/DTD/";

    /** The next entity resolver. */
    private final EntityResolver m_next;

    /**
     * Constructor.<p>
     **/
    public XhtmlEntityResolver() {
        this(null);
    }

    /**
     * Constructor.<p>
     *
     * @param next the next entity resolver.<p>
     */
    public XhtmlEntityResolver(EntityResolver next) {
        m_next = next;
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        if (systemId.startsWith(XHTML_PREFIX)) {
            String name = systemId.substring(XHTML_PREFIX.length());
            //final InputStream resourceAsStream = getClass().getResourceAsStream(name);
            URL resource = getClass().getResource(name);
            if (resource != null) {
                InputSource inputSource = new InputSource(resource.toExternalForm());
                inputSource.setPublicId(publicId);
                //inputSource.setByteStream(resourceAsStream);
                return inputSource;
            }
        }

        // Let file: URLs just get loaded using the default mechanism
        if (systemId.startsWith("file:") || systemId.startsWith("jar:")) {
            return null;
        }
        if (m_next != null) {
            return m_next.resolveEntity(publicId, systemId);
        } else {
            return null;
        }
    }
}
