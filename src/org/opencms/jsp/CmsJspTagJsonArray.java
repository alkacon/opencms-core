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

import org.opencms.json.JSONArray;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Tag for defining a JSON array.
 *
 * Values created by nested JSON tags will be added to the array.
 */
public class CmsJspTagJsonArray extends A_CmsJspJsonTag implements I_CmsJspJsonContext {

    /** Serial version id. */
    private static final long serialVersionUID = -5609309021078612934L;

    /** The JSON array to build. */
    private JSONArray m_jsonArray;

    /**
     * Default constructor explicitly resetting all variables.
     */
    public CmsJspTagJsonArray() {

        init();
    }

    /**
     * @see org.opencms.jsp.I_CmsJspJsonContext#addValue(java.lang.String, java.lang.Object)
     */
    public void addValue(String key, Object val) throws JspException {

        if (key != null) {
            throw new JspTagException("Can not add value to JSONArray with a key (key:" + key + ", val:" + val + ")");
        }

        m_jsonArray.put(val);

    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
     */
    @Override
    public int doStartTag() {

        m_jsonArray = new JSONArray();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see org.opencms.jsp.A_CmsJspJsonTag#getJsonValue()
     */
    @Override
    public Object getJsonValue() {

        return m_jsonArray;
    }

    /**
     * Initializes / resets the internal values.<p>
     */
    @Override
    protected void init() {

        super.init();
        m_jsonArray = null;
    }
}
