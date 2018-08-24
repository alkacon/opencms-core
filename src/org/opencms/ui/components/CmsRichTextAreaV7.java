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

package org.opencms.ui.components;

import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import com.vaadin.ui.JavaScript;
import com.vaadin.v7.ui.RichTextArea;

/**
 * Helper class for using rich text area in OpenCms.<p>
 */
public class CmsRichTextAreaV7 extends RichTextArea {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**CSS class name. */
    private static final String CSS_CLASSNAME = "richopensans";

    /**Style name. */
    private String m_styleName = "";

    /**
     * Public constructor.<p>
     */
    public CmsRichTextAreaV7() {

        addStyleName("o-richtextarea-reduced");
        setFontStyle();
    }

    /**
     * Sets the font stype to Open Sans.<p>
     * Has to be called after every refresh of the UI. For example after switching tabs on tab-sheets..<p>
     */
    public void setFontStyle() {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_styleName)) {
            removeStyleName(m_styleName);
        }
        String id = new CmsUUID().getStringValue();
        m_styleName = CSS_CLASSNAME + "_" + id;
        addStyleName(m_styleName);
        String js = "var elements = document.getElementsByClassName('"
            + m_styleName
            + "');"
            + "var iframeContainer = elements[0];"
            + "var iframe = iframeContainer.getElementsByTagName('iframe')[0];"
            + "var iframeBody = iframe.contentDocument.getElementsByTagName('body')[0];"
            + "iframeBody.style.fontFamily='\"Open Sans\", sans-serif';";

        JavaScript.getCurrent().execute(js);

    }
}
