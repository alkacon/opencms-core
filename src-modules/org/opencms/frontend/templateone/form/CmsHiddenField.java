/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsHiddenField.java,v $
 * Date   : $Date: 2005/09/06 09:26:15 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.frontend.templateone.form;

import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;

/**
 * Represents a hidden field.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsHiddenField extends A_CmsField {

    /** HTML field type: hidden field. */
    private static final String TYPE = "hidden";

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getType()
     */
    public String getType() {

        return TYPE;
    }
    
    /**
     * Returns the type of the input field, e.g. "text" or "select".<p>
     * 
     * @return the type of the input field
     */
    public static String getStaticType() {
        
        return TYPE;
    }
    
    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#buildHtml(CmsJspActionElement, CmsForm, org.opencms.i18n.CmsMessages, String)
     */
    public String buildHtml(CmsJspActionElement jsp, CmsForm formConfiguration, CmsMessages messages, String errorKey) {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<input type=\"hidden\" name=\"").append(getName()).append("\" value=\"").append(getValue()).append("\">\n");
        return buf.toString();
    }
    
}
