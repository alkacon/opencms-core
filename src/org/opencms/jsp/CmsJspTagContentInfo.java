/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsMacroResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;

/**
 * Used to access and display XML content item information from the VFS.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagContentInfo extends CmsJspScopedVarBodyTagSuport implements I_CmsMacroResolver {

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
    private static final List<String> KEYS_LIST = Collections.unmodifiableList(Arrays.asList(KEYS));

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagContentInfo.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1955531050687258685L;

    /** The name of the content info's value that should be printed out. */
    private String m_value;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsResourceContainer.class);
        if (ancestor == null) {
            // build a container
            CmsMessageContainer container = Messages.get().container(Messages.ERR_PARENTLESS_TAG_1, "contentinfo");
            String msg = Messages.getLocalizedMessage(container, pageContext);
            throw new JspTagException(msg);
        }

        I_CmsResourceContainer contentContainer = (I_CmsResourceContainer)ancestor;

        String tagContent = "";

        if (isScopeVarSet()) {
            if (contentContainer instanceof CmsJspTagContentLoad) {
                storeContentInfoBean((CmsJspTagContentLoad)contentContainer);
            } else if (contentContainer instanceof CmsJspTagResourceLoad) {
                storeContentInfoBean((CmsJspTagResourceLoad)contentContainer);
            }
        }

        if (CmsStringUtil.isNotEmpty(m_value)) {
            // value is provided - resolve macros
            tagContent = resolveMacros(m_value);
        }

        try {
            pageContext.getOut().print(tagContent);
        } catch (IOException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_PROCESS_TAG_1, "contentinfo");
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
     * Returns the name of the content info's value that should be printed out.<p>
     *
     * @return the name of the content info's value that should be printed out
     */
    public String getValue() {

        return m_value;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return true;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_value = null;
        super.release();
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        return CmsMacroResolver.resolveMacros(input, this);
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
     * Stores the container's content info bean in the page context.<p>
     *
     * @param container the parent container
     */
    protected void storeContentInfoBean(CmsJspTagResourceLoad container) {

        CmsContentInfoBean contentInfoBean = container.getContentInfoBean();

        contentInfoBean.setPageSize(container.getContentInfoBean().getPageSize());
        contentInfoBean.setPageIndex(container.getContentInfoBean().getPageIndex());
        contentInfoBean.setResultSize(container.getContentInfoBean().getResultSize());

        storeAttribute(contentInfoBean);
    }
}