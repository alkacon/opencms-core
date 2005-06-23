/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsExplorerDialog.java,v $
 * Date   : $Date: 2005/06/23 08:12:45 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Dialog for explorer views in the administration view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerDialog extends CmsDialog {

    /** List of explorer tools. */
    public static final List C_EXPLORER_TOOLS = new ArrayList();

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsExplorerDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /* fill the explorer tools list */
    static {
        C_EXPLORER_TOOLS.add("/projects/files");
        C_EXPLORER_TOOLS.add("/galleryoverview/downloadgallery");
        C_EXPLORER_TOOLS.add("/galleryoverview/htmlgallery");
        C_EXPLORER_TOOLS.add("/galleryoverview/imagegallery");
        C_EXPLORER_TOOLS.add("/galleryoverview/linkgallery");
        C_EXPLORER_TOOLS.add("/galleryoverview/tablegallery");
    }

    /**
     * Generates the html code for the title frame.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayTitle() throws Exception {

        JspWriter out = getJsp().getJspContext().getOut();
        out.println(htmlStart());
        out.println(bodyStart(null));
        out.println(dialogStart());
        out.println(dialogContentStart(getParamTitle()));
        out.println(dialogContentEnd());
        out.println(dialogEnd());
        out.println(bodyEnd());
        out.println(htmlEnd());
    }
}
