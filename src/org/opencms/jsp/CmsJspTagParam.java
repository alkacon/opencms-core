/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagParam.java,v $
 * Date   : $Date: 2005/06/02 09:36:55 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * 
 * This file is based on:
 * org.apache.taglibs.standard.tag.common.core.ParamSupport
 * from the Apache JSTL 1.0 implmentation.
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.opencms.jsp;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 * A handler for &lt;param&gt; that accepts attributes as Strings
 * and evaluates them as expressions at runtime.<p>
 *
 * @author Shawn Bayern
 */
public class CmsJspTagParam extends BodyTagSupport {

    /**
     * There used to be an 'encode' attribute; I've left this as a
     * vestige in case custom subclasses want to use our functionality
     * but NOT encode parameters.
     */
    protected boolean m_encode;

    /** The name of the parameter. */
    protected String m_name;

    /** The value of the parameter. */
    protected String m_value;

    /**
     * Public constructor.<p>
     */
    public CmsJspTagParam() {

        super();
        init();
    }

    /**
     * Simply send our name and value to our appropriate ancestor.<p>
     * 
     * @throws JspException (never thrown, required by interface)
     * @return EVAL_PAGE
     */
    public int doEndTag() throws JspException {

        Tag t = findAncestorWithClass(this, I_CmsJspTagParamParent.class);
        if (t == null) {
            throw new JspTagException(Messages.get().key(pageContext.getRequest().getLocale(), 
                Messages.ERR_PARENTLESS_TAG_1, new Object[] {"param"}));
        }
        // take no action for null or empty names
        if (m_name == null || m_name.equals("")) {
            return EVAL_PAGE;
        }

        // send the parameter to the appropriate ancestor
        I_CmsJspTagParamParent parent = (I_CmsJspTagParamParent)t;
        String value = this.m_value;
        if (value == null) {
            if (bodyContent == null || bodyContent.getString() == null) {
                value = "";
            } else {
                value = bodyContent.getString().trim();
            }
        }
        if (m_encode) {
            parent.addParameter(CmsEncoder.encode(m_name, OpenCms.getSystemInfo().getDefaultEncoding()), CmsEncoder
                .encode(value, OpenCms.getSystemInfo().getDefaultEncoding()));
        } else {
            parent.addParameter(m_name, value);
        }

        return EVAL_PAGE;
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    public void release() {

        init();
    }

    /**
     * Sets the attribute name.<p>
     * 
     * @param name the name to set 
     */
    public void setName(String name) {

        this.m_name = name;
    }

    /**
     * Sets the attribute value.<p>
     * 
     * @param value the name to set 
     */
    public void setValue(String value) {

        this.m_value = value;
    }

    /**
     * Initializes the internal values.<p> 
     */
    private void init() {

        m_name = null;
        m_value = null;
    }
}
