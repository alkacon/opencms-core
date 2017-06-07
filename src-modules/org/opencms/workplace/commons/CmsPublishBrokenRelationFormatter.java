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

package org.opencms.workplace.commons;

import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.Locale;

/**
 * This list item detail formatter for broken links.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishBrokenRelationFormatter implements I_CmsListFormatter {

    /** Resource name prefix for broken link sources. */
    public static final String PREFIX_SOURCES = "%";

    /** Resource name prefix for broken link targets. */
    public static final String PREFIX_TARGETS = "$";

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        String message = Messages.get().getBundle(locale).key(
            Messages.GUI_PUBLISH_BROKENRELATIONS_DETAIL_RELATION_SOURCES_0);
        String content = "";
        if ((data != null) && (data.toString().length() > 0)) {
            content = data.toString().substring(1);
            if (data.toString().startsWith(PREFIX_TARGETS)) {
                message = Messages.get().getBundle(locale).key(
                    Messages.GUI_PUBLISH_BROKENRELATIONS_DETAIL_RELATION_TARGETS_0);
            }
        }
        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' cellspacing='0' cellpadding='0'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='150' align='right' class='listdetailhead'>\n");
        html.append("\t\t\t");
        html.append(message);
        html.append("&nbsp;:&nbsp;\n");
        html.append("\t\t</td>\n");
        html.append("\t\t<td class='listdetailitem' style='white-space:normal;'>\n");
        html.append("\t\t\t");
        html.append(content);
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }
}