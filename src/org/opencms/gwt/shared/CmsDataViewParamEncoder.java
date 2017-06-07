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

import java.io.UnsupportedEncodingException;

import com.google.common.io.BaseEncoding;

/**
 * Encodes/decodes the configuration passed to the data view popup as a request parameter.<p>
 */
public final class CmsDataViewParamEncoder {

    /** The encoding. */
    public static final BaseEncoding CODEC = BaseEncoding.base64Url().withPadChar('.');

    /**
     * Hidden default constructor.<p>
     */
    private CmsDataViewParamEncoder() {
        // do nothing
    }

    /**
     * Decodes a string.<p>
     *
     * @param s the encoded string
     * @return the decoded string
     */
    public static String decodeString(String s) {

        try {
            return new String(CODEC.decode(s), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen
            return null;
        }
    }

    /**
     * Encodes a string.<p>
     *
     * @param s the input string
     * @return the encoded string
     */
    public static String encodeString(String s) {

        try {
            return CODEC.encode(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen
            return null;
        }
    }

}
