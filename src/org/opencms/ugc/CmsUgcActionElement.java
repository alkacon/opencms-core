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

package org.opencms.ugc;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ugc.shared.CmsUgcException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Form action element class for use in rendering user-generated content forms.<p>
 */
public class CmsUgcActionElement extends CmsJspActionElement {

    /**
     * Creates a new instance.<p>
     *
     * @param pageContext the JSP page context
     * @param request the current request
     * @param response the current response
     */
    public CmsUgcActionElement(PageContext pageContext, HttpServletRequest request, HttpServletResponse response) {

        super(pageContext, request, response);
    }

    /**
     * Creates a new form session to edit the file with the given name using the given form configuration.
     *
     * @param configPath the site path of the form configuration
     * @param fileName the name (not path) of the XML content to edit
     * @return the id of the newly created form session
     *
     * @throws CmsUgcException if something goes wrong
     */
    public String createSessionForResource(String configPath, String fileName) throws CmsUgcException {

        CmsUgcSession formSession = CmsUgcSessionFactory.getInstance().createSessionForFile(
            getCmsObject(),
            getRequest(),
            configPath,
            fileName);
        return "" + formSession.getId();
    }

}
