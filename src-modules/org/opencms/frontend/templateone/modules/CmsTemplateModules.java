/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/modules/CmsTemplateModules.java,v $
 * Date   : $Date: 2005/02/17 12:45:43 $
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

package org.opencms.frontend.templateone.modules;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A helper bean for the template one modules.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 6.0 alpha 2
 */
public class CmsTemplateModules extends CmsJspActionElement {
    
    /**
     * @see CmsJspActionElement#CmsJspActionElement(PageContext, HttpServletRequest, HttpServletResponse)
     */
    public CmsTemplateModules(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        
        super(context, req, res);
    }
    
    /**
     * Saves a {@link Date} object in the page context that was created from the value of
     * a specified page context attribute.<p>
     * 
     * @param dateAttrib the name of the page context attribute containing the date string
     */
    public void setDate(String dateAttrib) {
        
        long timestamp = (new Long((String)getJspContext().getAttribute(dateAttrib))).longValue();
        Date date = new Date(timestamp);
        getJspContext().setAttribute("date", date);
    }

    /**
     * Creates a HTML anchor from the values of three page context attribute names.
     * 
     * @param hrefAttrib the name of the page context attribute containing the link URL
     * @param descrAttrib the name of the page context attribute containing the link description
     * @param targetAttrib the name of the page context attribute containing the link target
     * @return an HTML anchor
     */
    public String getAnchor(String hrefAttrib, String descrAttrib, String targetAttrib) {

        String attribHref = (String)getJspContext().getAttribute(hrefAttrib);
        String attribDescr = (String)getJspContext().getAttribute(descrAttrib);
        boolean openBlank = Boolean.valueOf((String)getJspContext().getAttribute(targetAttrib)).booleanValue();

        String description = attribDescr;
        if (CmsStringUtil.isEmpty(attribDescr) || attribDescr.startsWith("???")) {
            description = attribHref;
        }

        String href = attribHref;
        if (!attribHref.toLowerCase().startsWith("http")) {
            href = link(attribHref);
        }

        String target = "";
        if (openBlank) {
            target = "_blank";
        }

        StringBuffer anchor = new StringBuffer();
        anchor.append("<a href=\"").append(href).append("\"");
        
        if (CmsStringUtil.isNotEmpty(description)) {
            anchor.append(" title=\"").append(description).append("\"");
        }
        
        if (CmsStringUtil.isNotEmpty(target)) {
            anchor.append(" target=\"").append(target).append("\"");
        }

        anchor.append(">").append(description).append("</a>");

        return anchor.toString();
    }

}