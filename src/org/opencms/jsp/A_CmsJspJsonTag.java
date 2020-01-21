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
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspJsonWrapper;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
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
        /** Store result as wrapper object. */
        wrapper,
        /** Store result of tag as formatted JSON (i.e. a string) . */
        text;
    }

    /** String describing request scope. */
    private static final String SCOPE_REQUEST = "request";

    /** String describing session scope. */
    private static final String SCOPE_SESSION = "session";

    /** String describing application scope. */
    private static final String SCOPE_APPLICATION = "application";

    /** Serial version id. */
    private static final long serialVersionUID = -4536413263964718943L;

    /** The key attribute. */
    protected String m_key;

    /** The mode attribute. */
    protected String m_mode;

    /** The var attribute. */
    protected String m_var;

    /** The target attribute. */
    protected Object m_target;

    /** The scope attribute. */
    protected String m_scope;

    /**
     * Converts the given string description of a scope to the corresponding PageContext constant.
     *
     * @param scope String description of scope
     *
     * @return PageContext constant corresponding to given scope description
     */
    protected static int getScope(String scope) {

        int ret = PageContext.PAGE_SCOPE; // default
        if (SCOPE_REQUEST.equalsIgnoreCase(scope)) {
            ret = PageContext.REQUEST_SCOPE;
        } else if (SCOPE_SESSION.equalsIgnoreCase(scope)) {
            ret = PageContext.SESSION_SCOPE;
        } else if (SCOPE_APPLICATION.equalsIgnoreCase(scope)) {
            ret = PageContext.APPLICATION_SCOPE;
        }
        return ret;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        if (m_var != null) {
            // Export to a variable
            String modeStr = m_mode;
            Mode mode = Mode.wrapper;
            int scopeValue = getScope(m_scope);
            if (modeStr != null) {
                try {
                    mode = Mode.valueOf(modeStr);
                } catch (Exception e) {
                    throw new JspTagException("Unknown mode for json tag: " + modeStr);
                }
            }
            Object val;
            switch (mode) {
                case object:
                    val = getJsonValue();
                    break;
                case text:
                    try {
                        val = JSONObject.valueToString(getJsonValue());
                    } catch (JSONException e) {
                        throw new JspTagException("Could not format JSON", e);
                    }
                    break;
                case wrapper:
                default:
                    val = new CmsJspJsonWrapper(getJsonValue());
                    break;
            }
            pageContext.setAttribute(m_var, val, scopeValue);
        } else if (m_target != null) {
            // Export to a specified JSON object
            addToTarget(m_target, getJsonValue(), m_key);
        } else {
            I_CmsJspJsonContext context = (I_CmsJspJsonContext)findAncestorWithClass(this, I_CmsJspJsonContext.class);
            if (context != null) {
                context.addValue(m_key, getJsonValue());
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Returns the JSON value that should be added to the surrounding context and/or stored in the variable
     * given by the var attribute.
     *
     * @return the value to add/store
     * @throws JspTagException if getting the value fails
     */
    public abstract Object getJsonValue() throws JspTagException;

    /**
     * Releases the resources used by this tag.<p>
     */
    @Override
    public void release() {

        init();
        super.release();
    }

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
     * Sets the scope attribute.
     *
     * @param scope the variable scope
     */
    public void setScope(String scope) {

        m_scope = scope;
    }

    /**
     * Sets the target attribute.
     *
     * @param target the target Object to store the JSON in.
     *
     * This must be another JSON object or a JSON wrapper.
     */
    public void setTarget(Object target) {

        m_target = target;
    }

    /**
     * Sets the var attribute.
     *
     * @param var the name of the variable to store the result in
     */
    public void setVar(String var) {

        m_var = var;
    }

    /**
     * Adds the specified value to the selected target.<p>
     *
     * @param target the target object
     * @param val the value to set
     * @param key the key to set the value, required in case the target is a JSONObject
     *
     * @throws JspException in case the value could not be added to the target
     */
    protected void addToTarget(Object target, Object val, String key) throws JspException {

        if (target instanceof JSONObject) {
            if (key == null) {
                throw new JspTagException("Can not add to JSONObject target with no key (val:" + val + ")");
            }
            try {
                JSONObject jsonObj = (JSONObject)target;
                if (jsonObj.has(key)) {
                    jsonObj.append(key, val);
                } else {
                    jsonObj.put(key, val);
                }
            } catch (JSONException e) {
                throw new JspTagException("Could not add value to JSONObject target", e);
            }
        } else if (target instanceof JSONArray) {
            ((JSONArray)target).put(val);
        } else if (target instanceof CmsJspJsonWrapper) {
            Object wrappedTaget = ((CmsJspJsonWrapper)target).getObject();
            addToTarget(wrappedTaget, val, key);
        } else {
            throw new JspTagException("Invalid target specified (target:" + val + ")");
        }
    }

    /**
     * Initializes / resets the internal values.<p>
     */
    protected void init() {

        m_key = null;
        m_var = null;
        m_mode = null;
        m_target = null;
        m_scope = null;
    }
}
