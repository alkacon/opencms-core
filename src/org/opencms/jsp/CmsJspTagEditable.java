/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEditable.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.flex.CmsFlexController;
import org.opencms.main.I_CmsConstants;
import org.opencms.workplace.editor.I_CmsEditorActionHandler;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of editor tag used to provide settings to include tag.<p>
 * 
 * @version $Revision: 1.6 $ $Date: 2004/02/13 13:41:44 $
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
            if (controller.getCmsObject().getRequestContext().currentProject().getId() != I_CmsConstants.C_PROJECT_ONLINE_ID) {
                if (context.getRequest().getAttribute(I_CmsEditorActionHandler.C_EDIT_AREA) == null) {                
                    if (filename == null) {
                        filename = I_CmsEditorActionHandler.C_EDITAREA_DEFAULTS;
                    }
                    context.getRequest().setAttribute(I_CmsEditorActionHandler.C_EDIT_AREA, filename);            
                    CmsJspTagInclude.includeTagAction(context, filename, I_CmsEditorActionHandler.C_EDITAREA_INCLUDES, null, req, res);
                }
            }            
        } catch (Throwable t) {
            // never thrown
            throw new JspException(t);
        }
    }
}
