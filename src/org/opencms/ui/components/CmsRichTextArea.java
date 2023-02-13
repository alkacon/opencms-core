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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

import com.vaadin.ui.RichTextArea;

/**
 * Helper class for using rich text area in OpenCms.<p>
 *
 * <p>The client side implementation of this widget adds some special styles, see CmsRichTextAreaConnector
 */
public class CmsRichTextArea extends RichTextArea {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**CSS class name. */
    private static final String CSS_CLASSNAME = "richopensans";

    /**Style name. */
    private String m_styleName = "";

    /**
     * Public constructor.<p>
     */
    public CmsRichTextArea() {

        addStyleName("o-richtextarea-reduced");
    }

    /**
     * Cleans up the given HTML such that only elements which can be normally entered in the widget are left.
     *
     * @param html the HTML
     * @param allowLinks true if anchor elements / links  should be kept
     *
     * @return the cleaned up HTML
     */
    public static String cleanHtml(String html, boolean allowLinks) {

        if (html == null) {
            return null;
        }
        Safelist whitelist = new Safelist();
        whitelist.addTags("font", "b", "span", "i", "strong", "br", "u", "ul", "ol", "li", "div");
        whitelist.addAttributes("font", "size", "color", "face");
        if (allowLinks) {
            whitelist.addTags("a");
            whitelist.addAttributes("a", "href");
        }
        Cleaner cleaner = new Cleaner(whitelist);
        Document doc = Jsoup.parseBodyFragment(html);
        Document cleaned = cleaner.clean(doc);
        return cleaned.body().html();
    }

}
