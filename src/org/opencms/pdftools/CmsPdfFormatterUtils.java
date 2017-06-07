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

package org.opencms.pdftools;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for PDF formatting.<p>
 */
public final class CmsPdfFormatterUtils {

    /**
     * Private constructor to prevent instantiation.<p>
     */
    private CmsPdfFormatterUtils() {

        // do nothing
    }

    /**
     * Executes a JSP with a given content as input and returns the output of the JSP.<p>
     *
     * @param cms the current CMS context
     * @param request the current request
     * @param response the current response
     * @param jsp the jsp resource to execute
     * @param content the content to render with the JSP
     *
     * @return the output of the JSP
     * @throws Exception if something goes wrong
     */
    public static byte[] executeJsp(
        CmsObject cms,
        HttpServletRequest request,
        HttpServletResponse response,
        CmsResource jsp,
        CmsResource content) throws Exception {

        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(
            OpenCms.getResourceManager().getLoader(jsp),
            content,
            jsp);
        CmsResource loaderRes = loaderFacade.getLoaderStartResource();
        request.setAttribute(CmsJspStandardContextBean.ATTRIBUTE_CMS_OBJECT, cms);
        CmsJspStandardContextBean context = CmsJspStandardContextBean.getInstance(request);
        CmsContainerElementBean element = new CmsContainerElementBean(
            content.getStructureId(),
            jsp.getStructureId(),
            Collections.<String, String> emptyMap(),
            false);
        context.setElement(element);
        return loaderFacade.getLoader().dump(
            cms,
            loaderRes,
            null,
            cms.getRequestContext().getLocale(),
            request,
            response);
    }

}
