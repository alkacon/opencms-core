/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/Attic/CmsContentCheckExplorerDialog.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.tools.CmsExplorerDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog for explorer views in the administration view.<p>
 * 
 * This special extension of the CmsExplorerDialog is required because of localisation issues.
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.1.2
 */
public class CmsContentCheckExplorerDialog extends CmsExplorerDialog {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsContentCheckExplorerDialog(CmsJspActionElement jsp) {

        super(jsp);
        setParamStyle(STYLE_NEW);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContentCheckExplorerDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(org.opencms.workplace.tools.content.Messages.get().getBundleName());
        addMessages("org.opencms.workplace.workplace");
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

}
