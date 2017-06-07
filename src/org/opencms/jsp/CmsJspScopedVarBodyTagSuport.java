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

import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Parent for body tags that require support for setting scoped variables to the JSP page context.<p>
 *
 * @since 7.0.2
 */
public class CmsJspScopedVarBodyTagSuport extends BodyTagSupport {

    /** The scopes supported by the page context. */
    private static final String[] SCOPES = {"page", "request", "session", "application"};

    /** The scopes supported by the page context as a list. */
    private static final List<String> SCOPES_LIST = Collections.unmodifiableList(Arrays.asList(SCOPES));

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 6521418315921327927L;

    /** The scope under which the content info is saved in the page context. */
    private int m_scope = PageContext.PAGE_SCOPE;

    /** The name of the variable under which the content info bean should be saved in the page context. */
    private String m_variable;

    /**
     * Returns the int value of the specified scope string.<p>
     *
     * The default value is {@link PageContext#PAGE_SCOPE}.<p>
     *
     * @param scope the string name of the desired scope, e.g. "application", "request"
     * @return the int value of the specified scope string
     */
    protected static int getScopeAsInt(String scope) {

        int scopeValue;
        switch (SCOPES_LIST.indexOf(scope)) {
            case 3:
                // application
                scopeValue = PageContext.APPLICATION_SCOPE;
                break;
            case 2:
                // session
                scopeValue = PageContext.SESSION_SCOPE;
                break;
            case 1:
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
     * Returns the String value of the specified scope integer.<p>
     *
     * Valid values for the scope int parameter are 1 to 4 only.<p>
     *
     * @param scope integer that describes the scope according to {@link #getScopeInt()}.<p>
     *
     * @return the String value of the specified scope integer
     */
    protected static String getScopeAsString(int scope) {

        if ((scope <= 0) || (scope > 4)) {
            // return default scope if int is outside valid rage
            return SCOPES[0];
        }
        return SCOPES[scope - 1];
    }

    /**
     * Returns the scope under which the content access bean is saved in the page context.<p>
     *
     * @return the scope under which the content access bean is saved in the page context
     */
    public String getScope() {

        return getScopeAsString(m_scope);
    }

    /**
     * Returns the name of the variable under which the content access bean is saved in the page context.<p>
     *
     * @return the name of the variable under which the content access bean is saved in the page context
     */
    public String getVar() {

        return m_variable;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {

        m_variable = null;
        m_scope = PageContext.PAGE_SCOPE;
        super.release();
    }

    /**
     * Sets the scope under which the content access bean is saved in the page context.<p>
     *
     * @param scope the scope under which the content access bean is saved in the page context
     */
    public void setScope(String scope) {

        if (CmsStringUtil.isNotEmpty(scope)) {
            scope = scope.trim().toLowerCase();
            m_scope = getScopeAsInt(scope);
        } else {
            // empty scope, use default "page" scope
            m_scope = PageContext.PAGE_SCOPE;
        }
    }

    /**
     * Sets the name of the variable under which the content access bean is saved in the page context.<p>
     *
     * @param var the name of the variable under which the content access bean is saved in the page context
     */
    public void setVar(String var) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(var)) {
            m_variable = var.trim();
        }
    }

    /**
     * Returns the scope as int usable for setting the JSP page context
     * with {@link PageContext#setAttribute(String, Object, int)}.<p>
     *
     * @return the scope as int usable for setting the JSP page context
     */
    protected int getScopeInt() {

        return m_scope;
    }

    /**
     * Returns <code>true</code> in case the scoped variable has been set for this Tag.<p>
     *
     * @return <code>true</code> in case the scoped variable has been set for this Tag
     */
    protected boolean isScopeVarSet() {

        return CmsStringUtil.isNotEmpty(getVar());
    }

    /**
     * Stores the provided Object as attribute in the JSP page context.<p>
     *
     * The values of {@link  #getVar()} and {@link #getScope()} are used to determine how the Object is stored.<p>
     *
     * @param obj the Object to store in the JSP page context
     */
    protected void storeAttribute(Object obj) {

        storeAttribute(getVar(), obj);
    }

    /**
     * Stores the provided Object as attribute with the provided name in the JSP page context.<p>
     *
     * The value of {@link #getScope()} is used to determine how the Object is stored.<p>
     *
     * @param name the name of the attribute to store the Object in
     * @param obj the Object to store in the JSP page context
     */
    protected void storeAttribute(String name, Object obj) {

        pageContext.setAttribute(name, obj, getScopeInt());
    }
}