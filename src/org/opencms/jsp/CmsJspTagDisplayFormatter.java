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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * The tag 'displayFormatter' can be used to add a formatter resource type pairing to the surrounding 'display' tag.<p>
 */
public class CmsJspTagDisplayFormatter extends TagSupport {

    /** The serial version id. */
    private static final long serialVersionUID = -7268191204617852330L;

    /** The formatter key. */
    private String m_key;

    /** The path to the formatter configuration file. */
    private String m_path;

    /** The resource type name. */
    private String m_type;

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        Tag t = findAncestorWithClass(this, CmsJspTagDisplay.class);
        if (t == null) {
            throw new JspTagException(
                Messages.get().getBundle(pageContext.getRequest().getLocale()).key(
                    Messages.ERR_PARENTLESS_TAG_1,
                    new Object[] {"displayFormatter"}));
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_type) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_key)) {
            ((CmsJspTagDisplay)t).addDisplayFormatterKey(m_type, m_key);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_type)
            && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_path)) {
            // only act if type and path are set
            ((CmsJspTagDisplay)t).addDisplayFormatter(m_type, m_path);
        }

        return EVAL_PAGE;
    }

    /**
     * Sets the path to the formatter configuration file.<p>
     *
     * @param path the path to set
     */
    public void setFormatter(String path) {

        setPath(path);
    }

    /**
     * Sets the key of the formatter to use.
     *
     * @param key the formatter key
     */
    public void setFormatterKey(String key) {

        m_key = key;
    }

    /**
     * Sets the path to the formatter configuration file.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }

}
