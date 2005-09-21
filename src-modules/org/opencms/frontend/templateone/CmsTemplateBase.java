/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/CmsTemplateBase.java,v $
 * Date   : $Date: 2005/09/21 08:02:57 $
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

package org.opencms.frontend.templateone;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Base action element for all template one beans.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.0
 */
public class CmsTemplateBase extends CmsJspActionElement {

    /** The old uri is stored in this parameter (if required). */
    public static final String ATTRIBUTE_ORIGINAL_URI = "__originalOpenCmsUri";

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsTemplateBase() {

        super();
    }

    /**
     * @see CmsJspActionElement#CmsJspActionElement(PageContext, HttpServletRequest, HttpServletResponse)
     */
    public CmsTemplateBase(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the original OpenCms request context URI thas has been changed, or <code>null</code>
     * if the URI was not changed.<p>
     * 
     * @return the original OpenCms request context URI thas has been changed, or <code>null</code>
     * if the URI was not changed
     */
    public String getOriginalUri() {

        return (String)getRequest().getAttribute(ATTRIBUTE_ORIGINAL_URI);
    }

    /**
     * Initializes the URI of the current template page.<p>
     * 
     * This checks for the presence of a special <code>uri</code> parameter.
     * If this parameter is found, the OpenCms request context URI is switched to this value.<p>
     */
    public void initUri() {

        String uri = getRequest().getParameter(CmsTemplateBean.PARAM_URI);
        if (CmsStringUtil.isNotEmpty(uri)) {
            getRequest().setAttribute(ATTRIBUTE_ORIGINAL_URI, getRequestContext().getUri());
            getRequestContext().setUri(uri);
        }
    }

    /**
     * Returns <code>true</code> if the OpenCms request context URI has been changed.<p>
     * 
     * @return <code>true</code> if the OpenCms request context URI has been changed
     */
    public boolean isUriChanged() {

        return null != getOriginalUri();
    }
}