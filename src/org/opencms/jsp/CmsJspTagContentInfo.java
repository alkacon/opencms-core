/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentInfo.java,v $
 * Date   : $Date: 2005/01/13 12:44:56 $
 * Version: $Revision: 1.2 $
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

package org.opencms.jsp;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 6.0 alpha 3
 */
public class CmsJspTagContentInfo extends TagSupport {

    /** The name of the variable under which the content info bean should be saved in the page context. */
    private String m_variable;

    /** The name of the content info's value that should be printed out. */
    private String m_value;

    /** The scope under which the content info is saved in the page context. */
    private String m_scope;

    /** The keys of the supported content info values. */
    private static final String[] m_keys = {
        "resultSize",
        "resultIndex",
        "pageCount",
        "pageIndex",
        "pageSize",
        "pageNavStartIndex",
        "pageNavEndIndex",
        "pageNavLength"};

    /** The keys of the supported content info values as a list. */
    private static final List m_valueKeys = Collections.unmodifiableList(Arrays.asList(m_keys));

    /** The scopes supported by the page context. */
    private static final String[] m_scopes = {"application", "session", "request", "page"};

    /** The scopes supported by the page context as a list. */
    private static final List m_scopeKeys = Collections.unmodifiableList(Arrays.asList(m_scopes));

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsJspTagContentContainer.class);
        if (ancestor == null) {
            throw new JspTagException("Tag <contentinfo> without required parent tag found!");
        }

        I_CmsJspTagContentContainer contentContainer = (I_CmsJspTagContentContainer)ancestor;

        String tagContent = "";

        if (CmsStringUtil.isNotEmpty(m_variable)) {
            int scope = getScopeAsInt(m_scope);
            storeContentInfoBean((CmsJspTagContentLoad)contentContainer, m_variable, scope);
        }

        if (CmsStringUtil.isNotEmpty(m_value)) {
            CmsContentInfoBean contentInfoBean = readContentInfoBean(m_value);
            if (contentInfoBean != null) {
                tagContent = getTagContent(contentInfoBean, m_value);
            }
        }

        try {
            pageContext.getOut().print(tagContent);
        } catch (IOException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error in Jsp <contentshow> tag processing", e);
            }
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    /**
     * Returns the tag content for a specified content info bean and value.<p>
     * 
     * @param contentInfoBean a content info bean
     * @param value a string such as ${myinfo.resultSize}
     * @return the tag content
     * @throws JspException if something goes wrong
     */
    protected String getTagContent(CmsContentInfoBean contentInfoBean, String value) throws JspException {

        String variableValue = getVariableValue(value);
        String tagContent = "";

        switch (m_valueKeys.indexOf(variableValue)) {
            case 0:
                // "resultSize"
                tagContent = Integer.toString(contentInfoBean.getResultSize());
                break;
            case 1:
                // "resultIndex"
                tagContent = Integer.toString(contentInfoBean.getResultIndex());
                break;
            case 2:
                // "pageCount"
                tagContent = Integer.toString(contentInfoBean.getPageCount());
                break;
            case 3:
                // "pageIndex"
                tagContent = Integer.toString(contentInfoBean.getPageIndex());
                break;
            case 4:
                // "pageSize"
                tagContent = Integer.toString(contentInfoBean.getPageSize());
                break;
            case 5:
                // pageNavStartIndex
                tagContent = Integer.toString(contentInfoBean.getPageNavStartIndex());
                break;
            case 6:
                // pageNavEndIndex
                tagContent = Integer.toString(contentInfoBean.getPageNavEndIndex());
                break; 
            case 7:
                // pageNavLength
                tagContent = Integer.toString(contentInfoBean.getPageNavLength());
                break;                 
            default:
                throw new JspException("Unsupported content info value requested: " + value);
        }

        return tagContent;
    }

    /**
     * Returns a content info bean from the page context as specified by a string such as "${myinfo.resultSize}".<p>
     * 
     * @param value a string such as ${myinfo.resultSize}
     * @return the content info bean
     */
    protected CmsContentInfoBean readContentInfoBean(String value) {

        String variable = getVariableName(value);

        if (CmsStringUtil.isNotEmpty(variable)) {
            int scope = pageContext.getAttributesScope(variable);
            return (CmsContentInfoBean)pageContext.getAttribute(variable, scope);
        }

        return null;
    }

    /**
     * Extracts the variable name out of a string such as "${myinfo.resultSize}".<p>
     * 
     * @param value a string such as ${myinfo.resultSize}
     * @return the variable name, e.g. "myinfo"
     */
    protected String getVariableName(String value) {

        int dotIndex = -1;
        String variableName = null;

        if (value.startsWith(CmsStringUtil.C_MACRO_DELIMITER + CmsStringUtil.C_MACRO_START)
            && value.endsWith(CmsStringUtil.C_MACRO_END)
            && (dotIndex = value.indexOf(".")) > 0) {

            variableName = value.substring(2, dotIndex);
        }

        return variableName;
    }

    /**
     * Extracts the variable value out of a string such as "${myinfo.resultSize}".<p>
     * 
     * @param value a string such as ${myinfo.resultSize}
     * @return the variable name, e.g. "resultSize"
     */
    protected String getVariableValue(String value) {

        int dotIndex = -1;
        String variableValue = null;

        if (value.startsWith(CmsStringUtil.C_MACRO_DELIMITER + CmsStringUtil.C_MACRO_START)
            && value.endsWith(CmsStringUtil.C_MACRO_END)
            && (dotIndex = value.indexOf(".")) > 0) {

            variableValue = value.substring(dotIndex + 1, value.length() - 1);
        }

        return variableValue;
    }

    /**
     * Stores the container's content info bean under the specified scope in the page context.<p>
     * 
     * @param container the parent container
     * @param variable the variable under which the content info bean is saved
     * @param scope the scope under which the content info bean is saved
     */
    protected void storeContentInfoBean(CmsJspTagContentLoad container, String variable, int scope) {

        CmsContentInfoBean contentInfoBean = container.getContentInfoBean();

        contentInfoBean.setPageSize(container.getContentInfoBean().getPageSize());
        contentInfoBean.setPageIndex(container.getContentInfoBean().getPageIndex());
        contentInfoBean.setResultSize(container.getContentInfoBean().getResultSize());

        pageContext.setAttribute(variable, contentInfoBean, scope);
    }

    /**
     * Returns the int value of the specified scope string.<p>
     * 
     * The default value is {@link PageContext#PAGE_SCOPE}.<p>
     * 
     * @param scope the string name of the desired scope, e.g. "application", "request"
     * @return the int value of the specified scope string
     */
    protected int getScopeAsInt(String scope) {

        int scopeValue;
        switch (m_scopeKeys.indexOf(scope)) {
            case 0:
                // application
                scopeValue = PageContext.APPLICATION_SCOPE;
                break;
            case 1:
                // session
                scopeValue = PageContext.SESSION_SCOPE;
                break;
            case 2:
                // request
                scopeValue = PageContext.REQUEST_SCOPE;
                break;
            default:
                // page
                scopeValue = PageContext.PAGE_SCOPE;
                break;
        }

        return scopeValue;

    }

    /**
     * Sets the name of the variable under which the content info bean should be saved in the page context.<p>
     * 
     * @param var the name of the variable under which the content info bean should be saved in the page context
     */
    public void setVar(String var) {

        m_variable = var;
    }

    /**
     * Returns the name of the variable under which the content info bean should be saved in the page context.<p>
     * 
     * @return the name of the variable under which the content info bean should be saved in the page context
     */
    public String getVar() {

        return m_variable;
    }

    /**
     * Returns the name of the content info's value that should be printed out.<p>
     * 
     * @return the name of the content info's value that should be printed out
     */
    public String getValue() {

        return m_value;
    }

    /**
     * Sets the name of the content info's value that should be printed out.<p>
     * 
     * @param value the name of the content info's value that should be printed out
     */
    public void setValue(String value) {

        m_value = value;
    }

    /**
     * Returns the scope under which the content info is saved in the page context.<p>
     * 
     * @return the scope under which the content info is saved in the page context
     */
    public String getScope() {

        return m_scope;
    }

    /**
     * Sets the scope under which the content info is saved in the page context.<p>
     * 
     * @param scope the scope under which the content info is saved in the page context
     */
    public void setScope(String scope) {

        m_scope = scope;
    }

}