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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;

/**
 * For adding text style to the columns in the explorer list.<p>
 *
 * @since 6.9.1
 */
public class CmsListResourceLinkRelationExplorerColumn extends CmsListColumnDefinition {

    /**
     * Default constructor.<p>
     *
     * @param id the unique id
     */
    public CmsListResourceLinkRelationExplorerColumn(String id) {

        super(id);
    }

    /**
     * Generates the needed style sheet definitions.<p>
     *
     * @return html code
     */
    public static String getExplorerStyleDef() {

        StringBuffer result = new StringBuffer(256);
        result.append("<style type='text/css'>\n");
        result.append(".fc, .fc .link a { color: #b40000; }\n");
        result.append(".fn, .fn .link a { color: #0000aa; }\n");
        result.append(".fd, .fd .link a { color: #000000; text-decoration: line-through; }\n");
        result.append(".fp, .fp .link a { color: #888888; }\n");
        result.append(".nf, .nf .link a { color:#000000; }\n");
        result.append("</style>");
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.CmsListColumnDefinition#htmlCell(org.opencms.workplace.list.CmsListItem, boolean)
     */
    @Override
    public String htmlCell(CmsListItem item, boolean isPrintable) {

        if (isPrintable) {
            return super.htmlCell(item, isPrintable);
        }
        CmsResourceUtil resUtil = ((A_CmsListExplorerDialog)getWp()).getResourceUtil(item);
        StringBuffer html = new StringBuffer(128);
        html.append("<table cellpadding='0' cellspacing='0' border='0'><tr><td class='");
        String styleClass = resUtil.getStyleClassName();
        if (styleClass.equals("fp") && resUtil.getResource().getState().isDeleted()) {
            styleClass = "fd";
        }
        html.append(styleClass);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resUtil.getTimeWindowLayoutStyle())) {
            html.append(" ' style='");
            html.append(resUtil.getTimeWindowLayoutStyle());
            html.append("'");
        }
        html.append("'>");
        html.append(super.htmlCell(item, isPrintable));
        html.append("</td></tr></table>");
        return html.toString();
    }
}
