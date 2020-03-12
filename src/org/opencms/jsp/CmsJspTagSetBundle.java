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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.apache.taglibs.standard.tag.el.fmt.SetBundleTag;

/** Set bundle tag using OpenCms' bundle loader mechanism. */
public class CmsJspTagSetBundle extends SetBundleTag {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 3260512204391338383L;
    /** Scope attribute. */
    private int m_scope;
    /** Variable attribute. */
    private String m_var;

    /**
     * Public constructor doing the initialization.
     */
    public CmsJspTagSetBundle() {

        super();
        init();
    }

    /**
     * @see org.apache.taglibs.standard.tag.common.fmt.SetBundleSupport#doEndTag()
     */
    @Override
    public int doEndTag() {

        LocalizationContext locCtxt = CmsJspTagBundle.getLocalizationContext(pageContext, basename);

        if (m_var != null) {
            pageContext.setAttribute(m_var, locCtxt, m_scope);
        } else {
            Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, m_scope);
        }

        return EVAL_PAGE;
    }

    /**
     * @see org.apache.taglibs.standard.tag.el.fmt.SetBundleTag#release()
     */
    @Override
    public void release() {

        super.release();
        init();
    }

    /**
     * @see org.apache.taglibs.standard.tag.common.fmt.SetBundleSupport#setScope(java.lang.String)
     */
    @Override
    public void setScope(String scope) {

        this.m_scope = Util.getScope(scope);
        super.setScope(scope);
    }

    /**
     * @see org.apache.taglibs.standard.tag.common.fmt.SetBundleSupport#setVar(java.lang.String)
     */
    @Override
    public void setVar(String var) {

        this.m_var = var;
        super.setVar(var);
    }

    /**
     * Sets the initial state.
     */
    private void init() {

        m_scope = PageContext.PAGE_SCOPE;
        m_var = null;
    }

}
