/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterObjectReplaceContent.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.6 $
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
 * Object to replace content in html code before
 * parsing it with JTidy.
 * @author Andreas Zahner
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
class CmsHtmlConverterObjectReplaceContent {

    /** String which will be replaced. */
    private String m_searchString;
    /** String which replaces old content. */
    private String m_replaceString;

    /**
     * Default constructor creates object with empty Strings.<p>
     */
    protected CmsHtmlConverterObjectReplaceContent () {
        m_searchString = "";
        m_replaceString = "";
    }

    /**
     * Constructor creates object with parameter values.<p>
     * 
     * @param sS String searchString
     * @param rS String replaceString
     */
    protected CmsHtmlConverterObjectReplaceContent (String sS, String rS) {
        m_searchString = sS;
        m_replaceString = rS;
    }

    /**
     * Returns the String to be found.<p>
     * 
     * @return String to be found
     */
    protected String getSearchString() {
        return m_searchString;
    }

    /**
     * Returns the new content.<p>
     * 
     * @return new content without prefix and suffix
     */
    protected String getReplaceItem() {
        return m_replaceString;
    }

}
