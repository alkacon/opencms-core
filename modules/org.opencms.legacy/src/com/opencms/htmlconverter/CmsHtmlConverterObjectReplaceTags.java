/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/htmlconverter/Attic/CmsHtmlConverterObjectReplaceTags.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
* Version: $Revision: 1.1 $
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
 * Object for replacing HTML tags.<p>
 * 
 * @author Andreas Zahner
 * @version 1.0
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
final class CmsHtmlConverterObjectReplaceTags {

    /** the prefix will be placed in front of every replaced tag. */
    private String m_prefix;
    /** name of tag which will be replaced. */
    private String m_tagName;
    /** Attribute of tag which will be replaced (optional). */
    private String m_tagAttrib;
    /** value of tag attribute which will be replaced (optional). */
    private String m_tagAttribValue;
    /** String which replaces the start tag. */
    private String m_replaceStartTag;
    /** String which replaces the end tag (optional). */
    private String m_replaceEndTag;
    /** the suffix will be placed behind every replaced tag. */
    private String m_suffix;
    /** if true, individual replaceStrings will be read from tag attributes. */
    private boolean m_getReplaceFromAttrs;
    /** tag attribute where the replaceStartTag String is stored. */
    private String m_startAttribute;
    /** tag attribute where the replaceEndTag String is stored. */
    private String m_endAttribute;
    /** tag attribute where the parameter String is stored. */
    private String m_parameter;
    /** if true, only tag attribute specified in parameter String is replaced. */
    private boolean m_replaceParamAttr;
    /** true, if replaceEndTag is empty. */
    private boolean m_inline;

    /**
     * Default constructor creates object with empty Strings.<p>
     */
    protected CmsHtmlConverterObjectReplaceTags () {
        m_prefix = "";
        m_tagName = "";
        m_tagAttrib = "";
        m_tagAttribValue = "";
        m_replaceStartTag = "";
        m_replaceEndTag = "";
        m_suffix = "";
        m_getReplaceFromAttrs = false;
        m_startAttribute = "";
        m_endAttribute = "";
        m_parameter = "";
        m_replaceParamAttr = false;
    }

    /**
     * Constructor creates object with parameters.<p>
     * 
     * @param prefix String prefix
     * @param tagName String tagName
     * @param tagAttrib String tagAttribute
     * @param tagAttribValue String tagAttributeValue
     * @param replaceStartTag String replaceStartTag
     * @param replaceEndTag String replaceEndTag
     * @param suffix String suffix
     * @param getReplaceFromAttrs boolean getReplaceFromAttrs
     * @param startAttribute String startAttribute
     * @param endAttribute String endAttribute
     * @param param String parameter
     * @param replaceParamAttr flag to indicate param replacement
     */
    protected CmsHtmlConverterObjectReplaceTags (String prefix, String tagName, String tagAttrib, String tagAttribValue, String replaceStartTag, String replaceEndTag, String suffix, boolean getReplaceFromAttrs, String startAttribute, String endAttribute, String param, boolean replaceParamAttr) {
        m_prefix = prefix;
        m_tagName = tagName;
        m_tagAttrib = tagAttrib;
        m_tagAttribValue = tagAttribValue;
        m_replaceStartTag = replaceStartTag;
        m_replaceEndTag = replaceEndTag;
        m_suffix = suffix;
        m_getReplaceFromAttrs = getReplaceFromAttrs;
        m_startAttribute = startAttribute;
        m_endAttribute = endAttribute;
        m_parameter = param;
        m_replaceParamAttr = replaceParamAttr;
        if (replaceEndTag.equals(null) || replaceEndTag.equals("")) {
            m_inline = true;
        }
    }

    /**
     * Returns prefix of actual object.<p>
     * 
     * @return String with prefix
     */
    protected String getPrefix() {
        return m_prefix;
    }

    /**
     * Returns suffix of actual object.<p>
     * 
     * @return String with suffix
     */
    protected String getSuffix() {
        return m_suffix;
    }

    /**
     * Returns tag name of tag which will be replaced.<p>
     * 
     * @return String with tag name
     */
    protected String getTagName() {
        return m_tagName;
    }

    /**
     * Returns attribute of tag which will be replaced.<p>
     * 
     * @return String with attribute name
     */
    protected String getTagAttrib() {
        return m_tagAttrib;
    }

    /**
     * Returns attribute value of tag which will be replaced.<p>
     * 
     * @return String with attribute value
     */
    protected String getTagAttribValue() {
        return m_tagAttribValue;
    }

    /**
     * Returns String which replaces the start tag.<p>
     * 
     * @return String with prefix, content and suffix
     */
    protected String getReplaceStartTag() {
        return m_prefix + m_replaceStartTag + m_suffix;
    }

    /**
     * Returns String which replaces the end tag.<p>
     * 
     * @return String with prefix, content and suffix
     */
    protected String getReplaceEndTag() {
        if (m_inline) {
            return "";
        }
        return m_prefix +m_replaceEndTag +m_suffix;
    }

    /**
     * Checks if end tag will be replaced or deleted.<p>
     * 
     * @return true if m_replaceEndTag is "", otherwise false
     */
    protected boolean getInline() {
        return m_inline;
    }

    /**
     * Checks if replacecontents are encoded in html code attributes.<p>
     * 
     * @return true if replacecontents are encoded, otherwise false
     */
    protected boolean getReplaceFromAttrs() {
        return m_getReplaceFromAttrs;
    }

    /**
     * Returns name of start attribute of encoded replacecontents.<p>
     * 
     * @return String with attribute name
     */
    protected String getStartAttribute() {
        return m_startAttribute;
    }

    /**
     * Returns name of end attribute of encoded replacecontents.<p>
     * 
     * @return String with attribute name
     */
    protected String getEndAttribute() {
        return m_endAttribute;
    }

    /**
     * Returns attribute name of parameter.<p>
     * 
     * @return String with attribute name
     */
    protected String getParameter() {
        return m_parameter;
    }

    /**
     * Returns if only an attribute must be replaced.<p>
     * 
     * @return true if only attribute has to be replaced
     */
    protected boolean getReplaceParamAttr() {
        return m_replaceParamAttr;
    }

}