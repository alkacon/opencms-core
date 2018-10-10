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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Builds a <i>java.util.Map</i> isntance with string keys and values from nested param tags, then stores it in a page context variable whose name is supplied by the user.
 */
public class CmsJspTagMap extends BodyTagSupport implements I_CmsJspTagParamParent {

    /** Serial version id. */
    private static final long serialVersionUID = 3547998166985921533L;

    /** Map to save parameters to the include in. */
    private Map<String, String> m_content;

    /** The variable name used to store the map. */
    private String m_var;

    /**
     * Empty constructor, required for attribute value initialization.<p>
     */
    public CmsJspTagMap() {

        super();
    }

    /**
     * @see org.opencms.jsp.I_CmsJspTagParamParent#addParameter(java.lang.String, java.lang.String)
     */
    public void addParameter(String name, String value) {

        m_content.put(name, value);
    }

    /**
     * @return <code>EVAL_PAGE</code>
     *
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     *
     * @throws JspException by interface default
     */
    @Override
    public int doEndTag() throws JspException {

        pageContext.setAttribute(m_var, m_content);
        return EVAL_PAGE;
    }

    /**
     * Returns <code>{@link #EVAL_BODY_BUFFERED}</code>.<p>
     *
     * @return <code>{@link #EVAL_BODY_BUFFERED}</code>
     *
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        m_content = new HashMap<String, String>();
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Sets the variable name in which the map should be stored.<p>
     *
     * @param var the name of the variable
     */
    public void setVar(String var) {

        m_var = var;
    }

}