/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEditable.java,v $
 * Date   : $Date: 2004/01/07 15:27:00 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
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

import org.opencms.flex.CmsFlexController;
import org.opencms.workplace.editor.I_CmsEditorActionHandler;

import com.opencms.core.I_CmsConstants;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of editor tag used to provide settings to include tag.<p>
 * 
 * @version $Revision: 1.3 $ $Date: 2004/01/07 15:27:00 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsJspTagEditable extends BodyTagSupport {
    
    /** file with editing elements */
    protected String m_file = null;        

    /**
     * Sets the file with elements for edit areas.<p>
     * 
     * @param file the file to set 
     */
    public void setFile(String file) {
        if (file != null) {
            m_file = file;
        }
    }

    /**
     * Gets the file with elements for edit areas.<p>
     * 
     * @return the file
     */
    public String getFile() {
        return m_file!=null?m_file:"";
    }

    /**
     * @return <code>EVAL_BODY_BUFFERED</code>
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */    
    public int doStartTag() {
        return EVAL_BODY_BUFFERED;
    } 
    
    /**
     * Simply send our name and value to our appropriate ancestor.<p>
     * 
     * @throws JspException (never thrown, required by interface)
     * @return EVAL_PAGE
     */
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();
        ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            
            editableTagAction(pageContext, m_file, req, res);

            release();
        }
        
        return EVAL_PAGE;
    }

    /**
     * Releases any resources we may have (or inherit)
     */
    public void release() {
        super.release();
        m_file = null;
    }

    /**
     * Editable action method.<p>
     * 
     * @param context the current JSP page context
     * @param filename the source for editarea elements
     * @param req the current request
     * @param res current response
     * @throws JspException never
     */
    public static void editableTagAction (PageContext context, String filename, ServletRequest req, ServletResponse res) 
    throws JspException {

        try {
            CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME); 
            
            if (filename == null) {
                filename = I_CmsEditorActionHandler.C_EDITAREA_DEFAULTS;
            }
            
            if (controller.getCmsObject().getRequestContext().currentProject().getId() != I_CmsConstants.C_PROJECT_ONLINE_ID) {
                context.setAttribute(I_CmsEditorActionHandler.C_EDIT_AREA, filename);
            
                CmsJspTagInclude.includeTagAction(context, filename, I_CmsEditorActionHandler.C_EDITAREA_INCLUDES, null, req, res);
            }
            
        } catch (Exception exc) {
            // never thrown
            if (res != null) {
                // noop 
            }
            throw new JspException(exc);
        }
    }
}
