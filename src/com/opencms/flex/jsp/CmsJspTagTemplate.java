/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagTemplate.java,v $
* Date   : $Date: 2002/11/08 21:54:54 $
* Version: $Revision: 1.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2002  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.flex.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Used to select various template parts an a JSP template that
 * is included in another file.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsJspTagTemplate extends BodyTagSupport { 
    
    // Attribute member variables
    private String m_part = null;

    /** Template part identifier */
    public static final String C_TEMPLATE_PART = "_templatepart";
    
    /**
     * Sets the include page/file target.
     * @param target the target to set
     */
    public void setPart(String part) {
        if (part != null) {
            m_part = part.toLowerCase();
        }
    }
    
    /**
     * Returns the include page/file target.
     * @return String
     */
    public String getPart() {
        return m_part!=null?m_part:"";
    }

    /**
     * Release the resources of the tag.
     */    
    public void release() {
        super.release();
        m_part = null;
    }    

    public int doStartTag() throws JspException {
        String param =  pageContext.getRequest().getParameter(C_TEMPLATE_PART);
        
        if ((param == null) || (param.equals(m_part))) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }
 }