/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/administration/Attic/CmsAdminContextHelpMenuItem.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

/**
 * This class is a menu item that works as an context help text container.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.2 $
 * @since 6.0
 */
public class CmsAdminContextHelpMenuItem extends CmsAdminMenuItem {

    /**
     * Default Ctor.<p>
     */
    public CmsAdminContextHelpMenuItem() {

        super("Context Help", "", "", "", true, null);
    }

    /**
     * @see org.opencms.workplace.administration.CmsAdminMenuItem#itemHtml(CmsWorkplace)
     */
    public String itemHtml(CmsWorkplace page) {

        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil
            .code("<table border='0' cellspacing='0' cellpadding='0' width='100%' id='conhelp' class='node'>"));
        html.append(CmsStringUtil.code(1, "<tr>"));
        html.append(CmsStringUtil.code(2, "<td width='100%'><div id='contexthelp'><div>"));
        html.append(CmsStringUtil.code(3, "<span id='contexthelp_text' class='hint'></span>"));
        html.append(CmsStringUtil.code(2, "</div></div></td>"));
        html.append(CmsStringUtil.code(1, "</tr>"));
        html.append(CmsStringUtil.code("</table>"));
        return html.toString();
    }

}