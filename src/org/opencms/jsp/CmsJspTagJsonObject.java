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

/**
 * Tag for defining a JSON object.
 *
 * Key-value pairs created by nested JSON tags will be added to the object.
 */
public class CmsJspTagJsonObject extends A_CmsJspJsonTag implements I_CmsJspJsonContext {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The JSON object being constructed. */
    private JSONObject m_jsonObject;

    /**
     * @see org.opencms.jsp.I_CmsJspJsonContext#addValue(java.lang.String, java.lang.Object)
     */
    public void addValue(String key, Object val) {

        if (key == null) {
            throw new UnsupportedOperationException("Can not add value to JSONArray with no key (val:" + val + ")");
        }
        try {
            m_jsonObject.put(key, val);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Can not add JSON value", e);
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
     * @see org.opencms.jsp.A_CmsJspJsonTag#getValue()
     */
    @Override
    public Object getValue() {

        return m_jsonObject;
    }

}
