/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/administration/CmsAdminDialog.java,v $
 * Date   : $Date: 2005/06/27 23:22:23 $
 * Version: $Revision: 1.9 $
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

package org.opencms.workplace.administration;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.tools.CmsExplorerDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Workplace class for /system/workplace/views/admin/admin-main.html .<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAdminDialog extends CmsDialog {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        Map params = initAdminTool();

        // explorer view dialogs
        if (CmsExplorerDialog.EXPLORER_TOOLS.contains(getCurrentToolPath())) {
            if (getAction() == CmsDialog.ACTION_CANCEL) {
                actionCloseDialog();
                return;
            }
            getToolManager().jspForwardPage(this, CmsToolManager.ADMINVIEW_ROOT_LOCATION + "/tool-fs.html", params);
            return;
        }

        // real tool
        if (!getAdminTool().getHandler().getLink().equals(getCms().getRequestContext().getUri())) {
            getToolManager().jspForwardPage(this, getAdminTool().getHandler().getLink(), params);
            return;
        }

        // just grouping 
        if (getAction() == CmsDialog.ACTION_CANCEL) {
            actionCloseDialog();
            return;
        }

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(htmlStart());
        out.print(bodyStart(null));
        out.print(dialogStart());
        out.print(dialogContentStart(getParamTitle()));
        out.print(dialogContentEnd());
        out.print(dialogEnd());
        out.print(bodyEnd());
        out.print(htmlEnd());
    }
}
