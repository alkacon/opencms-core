/*
* File   : $Source $
* Date   : $Date $
* Version: $Revision $
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

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

/**
 * This is a TagExtraInfo evaluation class that checks the attibutes of 
 * the <code>&lt;cms:include /&gt;</code> tag.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class CmsJspTagIncludeTEI extends TagExtraInfo {

    final private static String C_ATTR_PROPERTY = "property";
    final private static String C_ATTR_ATTRIBUTE = "attribute";
    final private static String C_ATTR_FILE = "file";
    final private static String C_ATTR_PAGE = "page";
    final private static String C_ATTR_SUFFIX = "suffix";
    final private static String C_ATTR_BODY = "body";
    final private static String C_ATTR_PART = "part";
    
    /**
     * Returns true if the given attribute name is specified, false otherwise.
     */
    public static boolean isSpecified(TagData data, String attributeName) {
        return (data.getAttribute(attributeName) != null);
    }
    
    /**
     * Checks the validity of the <code>&lt;cms:include /&gt;</code> attributs.<p>
     *
     * The logic used is:
     * <pre>
     * if (hasBody) {
     *       String type = (String)data.getAttribute(C_ATTR_BODY);
     *       if (! ("eval".equals(type) || "params".equals(type))) return false;
     * }
     * if (hasFile && (hasSuffix || hasProperty || hasAttribute)) return false;
     * if (hasProperty && hasAttribute) return false;
     * if (hasSuffix && !(hasProperty || hasAttribute)) return false;
     * if (! (hasProperty || hasFile || hasBody || hasAttribute)) return false;
     * </pre>
     * 
     * @param data the tag data
     * @return true if attributes are valid, false otherwise
     */
    public boolean isValid(TagData data) {
        
        boolean hasFile = isSpecified(data, C_ATTR_FILE) || isSpecified(data, C_ATTR_PAGE);
        boolean hasSuffix = isSpecified(data, C_ATTR_SUFFIX);
        boolean hasProperty = isSpecified(data, C_ATTR_PROPERTY);
        boolean hasBody = isSpecified(data, C_ATTR_BODY);
        boolean hasAttribute = isSpecified(data, C_ATTR_ATTRIBUTE);
        boolean hasPart = isSpecified(data, C_ATTR_PART);
        
        if (hasBody) {
            String type = (String)data.getAttribute(C_ATTR_BODY);
            if (! ("eval".equals(type) || "params".equals(type))) return false;
        }
        if (hasFile && (hasSuffix || hasProperty || hasAttribute)) return false;
        if (hasProperty && hasAttribute) return false;
        if (hasSuffix && !(hasProperty || hasAttribute)) return false;
        if (! (hasProperty || hasFile || hasBody || hasAttribute)) return false;

        return true;
    }        
}
