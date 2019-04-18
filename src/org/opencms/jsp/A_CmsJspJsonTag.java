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

import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Abstract superclass that handles the common behavior of the jsonarray/jsonobject/jsonvalue tags.
 *
 * <p>Each of these tags constructs a JSON value, adds it to the surrounding JSON context object if one exists,
 * and optionally stores the result in a page context variable, either as a JSON object or as formatted JSON text,
 * depending on the value of the mode attribute.
 */
public abstract class A_CmsJspJsonTag extends BodyTagSupport {

    /** JSON processing mode, decides what is stored in the variable given by var. */
    enum Mode {
    /** Store result of tag as an object. */
    object,

    /** Store result of tag as formatted JSON (i.e. a string) . */
    text;

    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The key attribute. */
    protected String m_key;

    /** The mode attribute. */
    protected String m_mode;

    /** The var attribute. */
    protected String m_var;

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() {

        I_CmsJspJsonContext context = (I_CmsJspJsonContext)findAncestorWithClass(this, I_CmsJspJsonContext.class);
        if (context != null) {
            context.addValue(m_key, getValue());
        }
        if (m_var != null) {
            Object obj = getValue();
            String modeStr = m_mode;
            Mode mode = Mode.text;
            if (modeStr != null) {
                try {
                    mode = Mode.valueOf(modeStr);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unknown mode for json tag: " + mode);
                }
            }

            switch (mode) {
                case object:
                    pageContext.setAttribute(m_var, obj);
                    break;
                case text:
                default:
                    try {
                        pageContext.setAttribute(m_var, JSONObject.valueToString(obj));
                    } catch (JSONException e) {
                        throw new IllegalArgumentException("Could not format JSON", e);
                    }
                    break;
            }
        }
        return EVAL_PAGE;

    }

    /**
     * Returns the JSON value that should be added to the surrounding context and/or stored in the variable
     * given by the var attribute.
     *
     * @return the value to add/store
     */
    public abstract Object getValue();

    /**
     * Sets the key attribute.
     *
     * @param key the key under which to store the value in the surrounding JSON object
     */
    public void setKey(String key) {

        m_key = key;
    }

    /**
     * Sets the mode attribute.
     *
     * @param mode the mode
     */
    public void setMode(String mode) {

        m_mode = mode;
    }

    /**
     * Sets the var attribute.
     *
     * @param var the name of the variable to store the result in
     */
    public void setVar(String var) {

        m_var = var;
    }

}
