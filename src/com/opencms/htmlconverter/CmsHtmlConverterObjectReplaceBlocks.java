/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterObjectReplaceBlocks.java,v $
* Date   : $Date: 2004/06/15 10:59:44 $
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
 * Object for replacing blocks of HTML code.
 * @author Andreas Zahner
 * @version 1.0
 */
final class CmsHtmlConverterObjectReplaceBlocks {

    /** the prefix will be placed in front of every replaced block. */
    private String m_prefix;
    /** name of tag which will be replaced. */
    private String m_tagName;
    /** Attribute of tag which will be replaced (optional). */
    private String m_tagAttrib;
    /** value of tag attribute which will be replaced (optional). */
    private String m_tagAttribValue;
    /** String which replaces the block. */
    private String m_replaceString;
    /** the suffix will be placed behind every replaced block. */
    private String m_suffix;
    /** if true, individual replaceString will be read from tag attribute. */
    private boolean m_getReplaceFromAttrs;
    /** tag attribute where the replaceString is stored. */
    private String m_replaceAttribute;
    /** tag attribute where the parameter String is stored. */
    private String m_parameter;

    /**
     * default constructor creates object with empty Strings.<p>
     */
    protected CmsHtmlConverterObjectReplaceBlocks () {
        m_prefix = "";
        m_tagName = "";
        m_tagAttrib = "";
        m_tagAttribValue = "";
        m_replaceString = "";
        m_suffix = "";
        m_getReplaceFromAttrs = false;
        m_replaceAttribute = "";
        m_parameter = "";
    }

    /**
     * constructor creates object with parameters.<p>
     * @param p String prefix
     * @param tN String tagName
     * @param tA String tagAttribute
     * @param tAV String tagAttributeValue
     * @param rS String replaceString
     * @param s String suffix
     * @param gRFA boolean getReplaceFromAttrs
     * @param rA String replaceAttribute
     * @param param String parameter
     */
    protected CmsHtmlConverterObjectReplaceBlocks (String p, String tN, String tA, String tAV, String rS, String s, boolean gRFA, String rA, String param) {
        m_prefix = p;
        m_tagName = tN;
        m_tagAttrib = tA;
        m_tagAttribValue = tAV;
        m_replaceString = rS;
        m_suffix = s;
        m_getReplaceFromAttrs = gRFA;
        m_replaceAttribute = rA;
        m_parameter = param;
    }

    /**
     * returns prefix of actual object.<p>
     * @return String with prefix
     */
    protected String getPrefix() {
        return m_prefix;
    }

    /**
     * returns suffix of actual object.<p>
     * @return String with suffix
     */
    protected String getSuffix() {
        return m_suffix;
    }

    /**
     * returns tag name of tag which will be replaced.<p>
     * @return String with tag name
     */
    protected String getTagName() {
        return m_tagName;
    }

    /**
     * returns attribute of tag which will be replaced.<p>
     * @return String with attribute name
     */
    protected String getTagAttrib() {
        return m_tagAttrib;
    }

    /**
     * returns attribute value of tag which will be replaced.<p>
     * @return String with attribute value
     */
    protected String getTagAttribValue() {
        return m_tagAttribValue;
    }

    /**
     * returns String which replaces the content between start and end tag.<p>
     * @return String with prefix, content and suffix
     */
    protected String getReplaceString() {
        return m_prefix + m_replaceString + m_suffix;
    }

    /**
     * checks if replacecontents are encoded in html code attributes.<p>
     * @return true if replacecontents are encoded, otherwise false
     */
    protected boolean getReplaceFromAttrs() {
        return m_getReplaceFromAttrs;
    }

    /**
     * returns name of attribute of encoded replacecontent.<p>
     * @return String with attribute name
     */
    protected String getReplaceAttribute() {
        return m_replaceAttribute;
    }

    /**
     * returns attribute name of parameter.<p>
     * @return String with attribute name
     */
    protected String getParameter() {
        return m_parameter;
    }

}