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

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Tag for defining a JSON object.
 *
 * Key-value pairs created by nested JSON tags will be added to the object.
 */
public class CmsJspTagJsonObject extends A_CmsJspJsonTag implements I_CmsJspJsonContext {

    /** Serial version id. */
    private static final long serialVersionUID = -3054667197099417049L;

    /** The JSON object being constructed. */
    private JSONObject m_jsonObject;

    /**
     * Default constructor explicitly resetting all variables.
     */
    public CmsJspTagJsonObject() {

        init();
    }

    /**
     * @see org.opencms.jsp.I_CmsJspJsonContext#addValue(java.lang.String, java.lang.Object)
     */
    public void addValue(String key, Object val) throws JspException {

        if (key == null) {
            throw new JspTagException("Can not add value to JSONObject with no key (val:" + val + ")");
        }
        try {
            m_jsonObject.put(key, val);
        } catch (JSONException e) {
            throw new JspTagException("Could not add value to JSONObject", e);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
     */
    @Override
    public int doStartTag() {

        m_jsonObject = new JSONObject();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see org.opencms.jsp.A_CmsJspJsonTag#getJsonValue()
     */
    @Override
    public Object getJsonValue() {

        return m_jsonObject;
    }

    /**
     * Initializes / resets the internal values.<p>
     */
    @Override
    protected void init() {

        super.init();
        m_jsonObject = null;
    }
}
