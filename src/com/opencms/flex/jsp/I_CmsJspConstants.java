/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/I_CmsJspConstants.java,v $
* Date   : $Date: 2002/08/21 11:29:32 $
* Version: $Revision: 1.2 $
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

/**
 * This interface is a collection of JSP constants used in the OpenCms Flex implementation.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public interface I_CmsJspConstants {
    
    /** Attribute to store all properties of a CmsFile in */
    static final String C_JSP_ATTR_TAGFILEPROPERTY = "com.opencms.flex.CmsJspTagFileProperty";
    
    /** Attribute to store link substituions on a page in */
    static final String C_JSP_ATTR_TAGLINK = "com.opencms.flex.CmsJspTagLink";

    /** Attribute to store WP label definition file */
    static final String C_JSP_ATTR_TAGLABEL_DEF = "com.opencms.flex.CmsJspTagWpLabel.labeldeffile";
    
    /** Attribute to store WP language file */
    static final String C_JSP_ATTR_TAGLABEL_LANG = "com.opencms.flex.CmsJspTagWpLabel.langfile";
    
    /** Extension for JSP managed by OpenCms */
    static final String C_JSP_EXTENSION = ".jsp";    
    
}
