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

import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import com.vaadin.server.FontIcon;

/**
 * An icon resource. Will be rendered as &lt;span class="v-icon styleName"&gt;&amp;bnsp;&lt;/span&gt;.<p>
 */
public class CmsCssIcon implements FontIcon {

    /** The serial version id. */
    private static final long serialVersionUID = -1013502165062622197L;

    /** The additional style to apply to buttons generated with this icon. */
    private String m_additionalButtonStyle;

    /** The unicode codepoint (character location) for this icon. */
    private int m_codePoint = 0x00A0;

    /** The icon css class. */
    private String m_styleName;

    /**The icon overlay css class.*/
    private String m_styleNameOverlay;

    /**
     * Cloning constructor.<p>
     *
     * @param icon the icon to clone
     */
    public CmsCssIcon(FontIcon icon) {

        m_styleName = icon.getFontFamily();
        m_codePoint = icon.getCodepoint();
        if (icon instanceof CmsCssIcon) {
            m_styleNameOverlay = ((CmsCssIcon)icon).m_styleNameOverlay;
            m_additionalButtonStyle = ((CmsCssIcon)icon).m_additionalButtonStyle;
        }
    }

    /**
     * Constructor.<p>
     *
     * @param styleName the style name to assign to the icon.<p>
     */
    public CmsCssIcon(String styleName) {

        m_styleName = styleName;
    }

    /**
     * Constructor.<p>
     *
     * @param styleName the style name to assign to the icon.<p>
     * @param additionalStyle the additional style to apply to buttons generated with this icon
     */
    public CmsCssIcon(String styleName, String additionalStyle) {

        m_styleName = styleName;
        m_additionalButtonStyle = additionalStyle;
    }

    /**
     * Returns the additional style name to use with OpenCms app buttons.<p>
     *
     * @return the additional style name
     */
    public String getAdditionalButtonStyle() {

        return m_additionalButtonStyle;
    }

    /**
     * @see com.vaadin.server.FontIcon#getCodepoint()
     */
    public int getCodepoint() {

        // this is the encoding for &nbsp;
        return m_codePoint;
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

        return "<span class=\"v-icon "
            + m_styleName
            + " "
            + m_additionalButtonStyle
            + "\">&#x"
            + Integer.toHexString(getCodepoint())
            + ";</span>";
    }

    /**
     * Returns the icon HTML with the title attribute set.<p>
     *
     * @param title the title to set
     *
     * @return the icon HTML
     */
    public String getHtml(String title) {

        return "<span class=\"v-icon "
            + m_styleName
            + " "
            + m_additionalButtonStyle
            + "\" title=\""
            + title
            + "\">&#x"
            + Integer.toHexString(getCodepoint())
            + ";</span>";
    }

    /**
     * Gets the html for the icon.<p>
     * If overlay is set, it will be shown.<p>
     *
     * @return html
     */
    public String getHtmlWithOverlay() {

        if (m_styleNameOverlay == null) {
            return getHtml();
        }
        return "<div class=\""
            + OpenCmsTheme.RESOURCE_ICON
            + " v-widget v-has-width\" style=\"width: 100%; padding-left: 0px !important;\">"
            + getHtml()
            + "<span class=\""
            + m_styleNameOverlay
            + "\" title=\"\"></span>"
            + "</div>";
    }

    /**
     * @see com.vaadin.server.Resource#getMIMEType()
     */
    public String getMIMEType() {

        throw new UnsupportedOperationException(
            FontIcon.class.getSimpleName() + " should not be used where a MIME type is needed.");
    }

    /**
     * Returns the style name of this icon.<p>
     *
     * @return the style name
     */
    public String getStyleName() {

        return m_styleName;
    }

    /**
     * Returns whether this icon has the additional button style set.<p>
     *
     * @return <code>true</code> in case the additional style is set
     */
    public boolean hasAdditionalButtonStyle() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_additionalButtonStyle);
    }

    /**
     * Sets the additional style to apply to buttons generated with this icon.<p>
     *
     * @param additionalStyle the additional style to apply to buttons generated with this icon
     */
    public void setAdditionalButtonStyle(String additionalStyle) {

        m_additionalButtonStyle = additionalStyle;
    }

    /**
     * Adds an overlay to icon. Overlay is only returned by getHtmlWithOverlay().<p>
     *
     * @param overlayStyle style to be added
     */
    public void setOverlay(String overlayStyle) {

        m_styleNameOverlay = overlayStyle;
    }
}
