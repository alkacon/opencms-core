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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class A_CmsHtmlConverter implements I_CmsHtmlConverter {

    /** The encoding used for the HTML code conversion. */
    private String m_encoding;

    /** The conversion modes to use as List of String parameters. */
    private List<String> m_modes;

    /**
     * Empty constructor.<p>
     *
     * Initializes with encoding {@link CmsEncoder#ENCODING_UTF_8} and with an empty String as mode.<p>
     */
    public A_CmsHtmlConverter() {

        init(null, null);
    }

    /**
     * Constructor, with parameters.<p>
     *
     * @param encoding the encoding used for the HTML code conversion
     * @param modes the conversion modes to use
     */
    public A_CmsHtmlConverter(String encoding, List<String> modes) {

        init(encoding, modes);
    }

    /**
     *
     * @see org.opencms.util.I_CmsHtmlConverter#convertToString(java.lang.String)
     */
    public abstract String convertToString(String htmlInput) throws UnsupportedEncodingException;

    /**
     * @see org.opencms.util.I_CmsHtmlConverter#getEncoding()
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * @see org.opencms.util.I_CmsHtmlConverter#getModes()
     */
    public List<String> getModes() {

        return m_modes;
    }

    /**
     * @see org.opencms.util.I_CmsHtmlConverter#init(java.lang.String, java.util.List)
     */
    public void init(String encoding, List<String> modes) {

        if (encoding == null) {
            m_encoding = CmsEncoder.ENCODING_UTF_8;
        } else {
            m_encoding = encoding;
        }
        if (modes == null) {
            m_modes = new ArrayList<String>();
        } else {
            m_modes = modes;
        }
    }
}
