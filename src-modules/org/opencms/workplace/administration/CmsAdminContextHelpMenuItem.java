/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/administration/CmsAdminContextHelpMenuItem.java,v $
 * Date   : $Date: 2005/06/23 10:47:09 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.administration;

import org.opencms.workplace.CmsWorkplace;

/**
 * Menu item implementation that works as an context help text container.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminContextHelpMenuItem extends CmsAdminMenuItem {

    /**
     * Default Constructor.<p>
     */
    public CmsAdminContextHelpMenuItem() {

        super("conhelp", "Context Help", "", "", "", true, null);
    }

    /**
     * @see org.opencms.workplace.administration.CmsAdminMenuItem#itemHtml(CmsWorkplace)
     */
    public String itemHtml(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("<table border='0' cellspacing='0' cellpadding='0' width='100%' id='conhelp' class='node'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td width='100%'><div id='contexthelp'><div>\n");
        html.append("\t\t\t<span id='contexthelp_text' class='hint'></span>\n");
        html.append("\t\t</div></div></td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }

}