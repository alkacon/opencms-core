/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterObjectReplaceStrings.java,v $
* Date   : $Date: 2004/06/14 16:04:30 $
* Version: $Revision: 1.3 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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

package com.opencms.htmlconverter;

/**
 * Object for replacing Strings. Contains 4 Strings with String to replace,
 * String with new content and prefix and suffix.
 * @author Andreas Zahner
 * @version 1.0
 */
final class CmsHtmlConverterObjectReplaceStrings extends CmsHtmlConverterObjectReplaceContent {

    /** the prefix will be placed in front of every replaced content. */
    private String m_prefix;
    /** the suffix will be placed behind every replaced content. */
    private String m_suffix;

    /**
     * default constructor creates object with empty Strings.<p>
     */
    protected CmsHtmlConverterObjectReplaceStrings () {
        super();
        m_prefix = "";
        m_suffix = "";
    }

    /**
     * constructor creates object with parameter values.<p>
     * @param sS String searchString
     * @param p String prefix
     * @param rS String replaceString
     * @param s String suffix
     */
    protected CmsHtmlConverterObjectReplaceStrings (String sS, String p, String rS, String s) {
        super(sS, rS);
        m_prefix = p;
        m_suffix = s;
    }

    /**
     * returns the new content.<p>
     * @return new String with prefix and suffix added
     */
    protected String getReplaceItem() {
        return m_prefix+super.getReplaceItem()+m_suffix;
    }

}
