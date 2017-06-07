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
 * @since 6.0.0
 */
public class CmsExplorerDialog extends CmsDialog {

    /** List of explorer tools. */
    public static final List<String> EXPLORER_TOOLS = new ArrayList<String>();

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerDialog(CmsJspActionElement jsp) {

        super(jsp);
        setParamStyle(CmsToolDialog.STYLE_NEW);
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

    /* fill the explorer tools list, do not forget to add the message bundle also */
    static {
        EXPLORER_TOOLS.add("/galleryoverview/downloadgallery");
        EXPLORER_TOOLS.add("/galleryoverview/htmlgallery");
        EXPLORER_TOOLS.add("/galleryoverview/imagegallery");
        EXPLORER_TOOLS.add("/galleryoverview/linkgallery");
        EXPLORER_TOOLS.add("/galleryoverview/tablegallery");
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
