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

import org.opencms.json.JSONTokener;
import org.opencms.main.CmsLog;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

/**
 * Adds a JSON value to the surrounding context and/or stores it as a variable in the page context.
 */
public class CmsJspTagJsonValue extends A_CmsJspJsonTag {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagJsonValue.class);

    /** Serial version id. */
    private static final long serialVersionUID = -8383685322762531356L;

    /** The value attribute. */
    private Object m_value;

    /** Keeps track of whether the value should be parsed as a JSON string. */
    private boolean m_parse;

    /** Keeps track if a value has been specified or the body should be evaluated. */
    private boolean m_valueSpecified;

    /** Name of variable to store errors under in the page scope. */
    private String m_errorVar;

    /**
     * Default constructor explicitly resetting all variables.
     */
    public CmsJspTagJsonValue() {

        init();
    }

    /**
     * @see org.opencms.jsp.A_CmsJspJsonTag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {

        setError(null);
        return super.doEndTag();
    }

    /**
     * @see org.opencms.jsp.A_CmsJspJsonTag#getJsonValue()
     */
    @Override
    public Object getJsonValue() {

        Object value = null;
        if (m_valueSpecified) {
            value = m_value;
        } else {
            if ((bodyContent == null) || (bodyContent.getString() == null)) {
                value = "";
            } else {
                value = bodyContent.getString().trim();
            }
        }
        if (m_parse) {
            String strValue = "" + value;
            strValue = strValue.trim();
            try {
                JSONTokener parser = new JSONTokener(strValue);
                value = parser.nextValue();
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
                String errorMessage = e.getLocalizedMessage();
                setError(errorMessage);
                return null;
            }
        }
        return value;
    }

    /**
     * Variable to store errors under in the page scope.
     *
     * @param errorVar the error variable
     */
    public void setErrorVar(String errorVar) {

        m_errorVar = errorVar;
    }

    /**
     * Sets the parse attribute.
     *
     * <p>If set to 'true', the value will be treated as a string and then parsed into JSON.
     *
     * @param parse the value being set
     */
    public void setParse(String parse) {

        m_parse = Boolean.valueOf(parse).booleanValue();

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
     * If an error variable has been specified, store the given error message in that variable.
     *
     * @param errorMessage the error message
     */
    protected void setError(String errorMessage) {

        try {
            if (m_errorVar != null) {
                pageContext.setAttribute(m_errorVar, errorMessage);
            }
        } catch (Exception e2) {
            LOG.error(e2.getLocalizedMessage(), e2);
        }
    }
}
