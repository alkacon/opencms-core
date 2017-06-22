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

package org.opencms.ui;

import com.vaadin.server.FontIcon;

/**
 * An icon resource. Will be rendered as &lt;span class="v-icon styleName"&gt;&amp;bnsp;&lt;/span&gt;.<p>
 */
public class CmsCssIcon implements FontIcon {

    /** The serial version id. */
    private static final long serialVersionUID = -1013502165062622197L;

    /** The icon css class. */
    private String m_styleName;

    /**
     * Constructor.<p>
     *
     * @param styleName the style name to assign to the icon.<p>
     */
    public CmsCssIcon(String styleName) {
        m_styleName = styleName;
    }

    /**
     * @see com.vaadin.server.FontIcon#getCodepoint()
     */
    public int getCodepoint() {

        // this is the encoding for &nbsp;
        return 0x00A0;
    }

    /**
     * @see com.vaadin.server.FontIcon#getFontFamily()
     */
    public String getFontFamily() {

        return m_styleName;
    }

    /**
     * @see com.vaadin.server.FontIcon#getHtml()
     */
    @Override
    public String getHtml() {

        return "<span class=\"v-icon " + m_styleName + "\">&#x" + Integer.toHexString(getCodepoint()) + ";</span>";
    }

    public String getHtml(String title) {

        return "<span class=\"v-icon "
            + m_styleName
            + "\" title=\""
            + title
            + "\">&#x"
            + Integer.toHexString(getCodepoint())
            + ";</span>";
    }

    /**
     * @see com.vaadin.server.Resource#getMIMEType()
     */
    public String getMIMEType() {

        throw new UnsupportedOperationException(
            FontIcon.class.getSimpleName() + " should not be used where a MIME type is needed.");
    }
}
