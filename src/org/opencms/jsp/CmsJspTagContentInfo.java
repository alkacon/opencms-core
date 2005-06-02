/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagContentInfo.java,v $
 * Date   : $Date: 2005/06/02 09:36:55 $
 * Version: $Revision: 1.10 $
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111R-1307  USA
 */

package org.opencms.jsp;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsMacroResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;

/**
 * Used to access and display XML content item information from the VFS.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.10 $
 * @since 6.0 alpha 3
 */
public class CmsJspTagContentInfo extends TagSupport implements I_CmsMacroResolver {

    /** The keys of the supported content info values. */
    private static final String[] KEYS = {
        "resultSize",
        "resultIndex",
        "pageCount",
        "pageIndex",
        "pageSize",
        "pageNavStartIndex",
        "pageNavEndIndex",
        "pageNavLength"};

    /** The keys of the supported content info values as a list. */
    private static final List KEYS_LIST = Collections.unmodifiableList(Arrays.asList(KEYS));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContentInfo.class);

    /** The scopes supported by the page context. */
    private static final String[] SCOPES = {"application", "session", "request", "page"};

    /** The scopes supported by the page context as a list. */
    private static final List SCOPES_LIST = Collections.unmodifiableList(Arrays.asList(SCOPES));

    /** The scope under which the content info is saved in the page context. */
    private String m_scope;

    /** The name of the content info's value that should be printed out. */
    private String m_value;

    /** The name of the variable under which the content info bean should be saved in the page context. */
    private String m_variable;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsJspTagContentContainer.class);
        if (ancestor == null) {
            // localize error message:
            // build a container:
            String msg;
            CmsMessageContainer container = Messages.get().container(Messages.ERR_PARENTLESS_TAG_1, "contentinfo");
            msg = Messages.getLocalizedMessage(container, pageContext);
            throw new JspTagException(msg);
        }

        I_CmsJspTagContentContainer contentContainer = (I_CmsJspTagContentContainer)ancestor;

        String tagContent = "";

        if (CmsStringUtil.isNotEmpty(m_variable)) {
            int scope = getScopeAsInt(m_scope);
            storeContentInfoBean((CmsJspTagContentLoad)contentContainer, m_variable, scope);
        }

        if (CmsStringUtil.isNotEmpty(m_value)) {
            // value is provided - resolve macros
            tagContent = resolveMacros(m_value);
        }

        try {
            pageContext.getOut().print(tagContent);
        } catch (IOException e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_PROCESS_TAG_1, "contentinfo");
            LOG.error(message.key(), e);
            throw new JspException(message.key(pageContext.getRequest().getLocale()));
        }

        return SKIP_BODY;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String macro) {

        int dotIndex = macro.indexOf('.');
        String beanName = null;

        if ((dotIndex > 1) && (dotIndex < (macro.length() - 1))) {
            beanName = macro.substring(0, dotIndex);
        } else {
            return null;
        }

        String variableName = macro.substring(dotIndex + 1, macro.length());

        if (CmsStringUtil.isEmpty(beanName) || CmsStringUtil.isEmpty(variableName)) {
            return null;
        }

        // extract bean from page context
        CmsContentInfoBean bean;
        int scope = pageContext.getAttributesScope(beanName);
        try {
            bean = (CmsContentInfoBean)pageContext.getAttribute(beanName, scope);
        } catch (ClassCastException e) {
            // attribute exists but is not of required class
            return null;
        }

        if (bean == null) {
            return null;
        }

        switch (KEYS_LIST.indexOf(variableName)) {
            case 0:
                // "resultSize"
                return Integer.toString(bean.getResultSize());
            case 1:
                // "resultIndex"
                return Integer.toString(bean.getResultIndex());
            case 2:
                // "pageCount"
                return Integer.toString(bean.getPageCount());
            case 3:
                // "pageIndex"
                return Integer.toString(bean.getPageIndex());
            case 4:
                // "pageSize"
                return Integer.toString(bean.getPageSize());
            case 5:
                // pageNavStartIndex
                return Integer.toString(bean.getPageNavStartIndex());
            case 6:
                // pageNavEndIndex
                return Integer.toString(bean.getPageNavEndIndex());
            case 7:
                // pageNavLength
                return Integer.toString(bean.getPageNavLength());
            default:
                // unknown value
                return null;
        }
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
     * Returns the name of the content info's value that should be printed out.<p>
     * 
     * @return the name of the content info's value that should be printed out
     */
    public String getValue() {

        return m_value;
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
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return true;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        return CmsMacroResolver.resolveMacros(input, this);
    }

    /**
     * Sets the scope under which the content info is saved in the page context.<p>
     * 
     * @param scope the scope under which the content info is saved in the page context
     */
    public void setScope(String scope) {

        m_scope = scope;
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
     * Sets the name of the variable under which the content info bean should be saved in the page context.<p>
     * 
     * @param var the name of the variable under which the content info bean should be saved in the page context
     */
    public void setVar(String var) {

        m_variable = var;
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
        switch (SCOPES_LIST.indexOf(scope)) {
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
}