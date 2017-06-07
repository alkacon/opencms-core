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

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * HTML converters can be used to clean up HTML code and/or pretty print the code.<p>
 *
 * They can be configured in the configuration file <code>opencms-vfs.xml</code> and triggered when saving
 * XML contents or XML pages. Their behavior is configurable by setting the <code>content-conversion</code> property
 * value on VFS resources.<p>
 *
 * @since 7.5.1
 *
 */
public interface I_CmsHtmlConverter {

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * @param htmlInput the HTML input stored in a string
     * @return string containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    String convertToString(String htmlInput) throws UnsupportedEncodingException;

    /**
     * Returns the encoding used for the HTML code conversion.<p>
     *
     * @return the encoding used for the HTML code conversion
     */
    String getEncoding();

    /**
     * Returns the conversion modes to use as List of String parameters.<p>
     *
     * @return the conversion modes to use as List of String parameters
     */
    List<String> getModes();

    /**
     * Initializes the HTML converter instance.<p>
     *
     * Possible values for the conversion mode are dependent from the converter implementation.<p>
     *
     * @param encoding the encoding used for the HTML code conversion
     * @param modes the conversion modes to use
     */
    void init(String encoding, List<String> modes);

}
