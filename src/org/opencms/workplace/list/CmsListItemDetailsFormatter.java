/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListItemDetailsFormatter.java,v $
 * Date   : $Date: 2006/03/27 14:52:28 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;

import java.util.Locale;

/**
 * This list item detail formatter creates a two column table, in the first column
 * the message is put and in the second the content self.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListItemDetailsFormatter implements I_CmsListFormatter {

    /** Some message header. */
    private final CmsMessageContainer m_message;

    /**
     * Default constructor that sets the mask to use.<p>
     * 
     * @param message header for item detail
     */
    public CmsListItemDetailsFormatter(CmsMessageContainer message) {

        m_message = message;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
     */
    public String format(Object data, Locale locale) {

        String message = m_message.key(locale);
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
        html.append(data == null ? "" : data);
        html.append("\n");
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }
}