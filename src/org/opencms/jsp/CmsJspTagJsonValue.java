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

import javax.servlet.jsp.JspException;

/**
 * Adds a JSON value to the surrounding context and/or stores it as a variable in the page context.
 */
public class CmsJspTagJsonValue extends A_CmsJspJsonTag {

    /** Serial version id. */
    private static final long serialVersionUID = -8383685322762531356L;

    /** The value attribute. */
    private Object m_value;

    /** Keeps track if a value has been specified or the body should be evaluated. */
    private boolean m_valueSpecified;

    /**
     * Default constructor explicitly resetting all variables.
     */
    public CmsJspTagJsonValue() {

        init();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        if (!m_valueSpecified) {
            if ((bodyContent == null) || (bodyContent.getString() == null)) {
                m_value = "";
            } else {
                m_value = bodyContent.getString().trim();
            }
        }

        return super.doEndTag();
    }

    /**
     * @see org.opencms.jsp.A_CmsJspJsonTag#getValue()
     */
    @Override
    public Object getValue() {

        return m_value;
    }

    /**
     * Initializes / resets the internal values.<p>
     */
    @Override
    protected void init() {

        super.init();
        m_value = null;
        m_valueSpecified = false;
    }

    /**
     * Sets the value attribute.
     *
     * @param value the JSON value
     */
    public void setValue(Object value) {

        m_value = value;
        m_valueSpecified = true;
    }
}
