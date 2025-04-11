/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsTextMetrics;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Element;

/**
 * Single line label implementation that acts like doing CSS 'text-overflow:ellipsis;', but cutting text on the left-hand side.<p>
 *
 * @since 8.0.0
 */
public class CmsLabelLeftTruncating extends CmsLabel {

    /**
     * Creates an empty label.<p>
     */
    public CmsLabelLeftTruncating() {

        super();
    }

    /**
     * Creates an empty label using the given element.<p>
     *
     * @param element the element to use
     */
    public CmsLabelLeftTruncating(Element element) {

        super(element);
    }

    /**
     * Creates a label with the specified text.<p>
     *
     * @param text the new label's text
     */
    public CmsLabelLeftTruncating(String text) {

        super(text);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    @Override
    public void truncate(String textMetricsKey, int labelWidth) {

        super.setText(m_originalText);

        // measure the actual text width
        Element element = getElement();
        CmsTextMetrics tm = CmsTextMetrics.get(element, textMetricsKey);
        String text = element.getInnerText();
        int textWidth = tm.getWidth(text);
        tm.release();

        if (labelWidth >= textWidth) {
            updateTitle(false);
            return;
        }
        updateTitle(true);

        text = (new StringBuilder(text)).reverse().toString();
        // if the text does not have enough space, fix it
        int maxChars = (int)(((float)labelWidth / (float)textWidth) * text.length());
        if (maxChars < 1) {
            maxChars = 1;
        }
        text = text.substring(0, maxChars - 1);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(text)) {
            // if empty, it could break the layout
            text = CmsDomUtil.Entity.nbsp.html();
        }
        // text = CmsClientStringUtil.shortenString(text, maxChars);
        text = (new StringBuilder(text)).reverse().toString();
        if (maxChars > 2) {
            // enough space for ellipsis?
            text = CmsDomUtil.Entity.hellip.html() + text;
        }
        // use html instead of text because of the entities
        setHTML(text);
    }

}
