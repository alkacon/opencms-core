/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListExplorerColumn.java,v $
 * Date   : $Date: 2005/12/14 10:36:37 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.util.CmsResourceUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * For adding text style to the columns in the explorer list.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListExplorerColumn extends CmsListColumnDefinition {

    /**
     * Default constructor.<p>
     * 
     * @param id the unique id
     */
    public CmsListExplorerColumn(String id) {

        super(id);
    }

    /**
     * @see org.opencms.workplace.list.CmsListColumnDefinition#htmlCell(org.opencms.workplace.list.CmsListItem, org.opencms.workplace.CmsWorkplace, boolean)
     */
    public String htmlCell(CmsListItem item, CmsWorkplace wp, boolean isPrintable) {

        if (isPrintable) {
            return super.htmlCell(item, wp, isPrintable);
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(
            wp.getCms(),
            ((A_CmsListExplorerDialog)wp).getResource(item.getId()));
        StringBuffer html = new StringBuffer(128);
        html.append("<table cellpadding='0' cellspacing='0' border='0'><tr><td class='");
        html.append(resUtil.getStyleClassName());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resUtil.getStyleRange())) {
            html.append(" ' style='");
            html.append(resUtil.getStyleRange());
            html.append("'");
        }
        html.append("'>");
        html.append(super.htmlCell(item, wp, isPrintable));
        html.append("</td></tr></table>");
        return html.toString();
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
        result.append(".fn, .fn .link a  { color: #0000aa; }\n");
        result.append(".fd, .fd .link a  { color: #000000; text-decoration: line-through; }\n");
        result.append(".fp, .fp .link a  { color: #888888; }\n");
        result.append(".nf, .nf .link a  { color:#000000; }\n");
        result.append("</style>");
        return result.toString();
    }
}
