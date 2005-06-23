/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEditable.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of editor tag used to provide settings to include tag.<p>
 * 
 * @version $Revision: 1.19 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagEditable extends BodyTagSupport {

    /** file with editable elements. */
    protected String m_file;

    /**
     * Returns an option String for the direct editor generated from the provided values.<p>
     * 
     * @param showEdit indicates that the edit button should be shown 
     * @param showDelete indicates that the delete button should be shown 
     * @param showNew indicates that the new button should be shown 
     * @return an option String for the direct editor generated from the provided values
     */
    public static String createEditOptions(boolean showEdit, boolean showDelete, boolean showNew) {

        StringBuffer result = new StringBuffer(32);
        if (showEdit) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_EDIT);
        }
        result.append('|');
        if (showDelete) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_DELETE);
        }
        result.append('|');
        if (showNew) {
            result.append(I_CmsEditorActionHandler.C_DIRECT_EDIT_OPTION_NEW);
        }
        return result.toString();
    }

    /**
     * Editable action method.<p>
     * 
     * @param context the current JSP page context
     * @param filename the source for direct edit elements
     * @param req the current request
     * @param res current response
     * @throws JspException never
     */
    public static void editableTagAction(PageContext context, String filename, ServletRequest req, ServletResponse res)
    throws JspException {

        try {
            CmsObject cms = CmsFlexController.getCmsObject(req);
            if (cms.getRequestContext().currentProject().getId() != I_CmsConstants.C_PROJECT_ONLINE_ID) {
                if (context.getRequest().getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI) == null) {
                    if (filename == null) {
                        filename = I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT;
                    }
                    context.getRequest().setAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI, filename);
                    CmsJspTagInclude.includeTagAction(
                        context,
                        filename,
                        I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDES,
                        false,
                        null,
                        req,
                        res);
                }
            }
        } catch (Throwable t) {
            // never thrown
            throw new JspException(t);
        }
    }

    /**
     * Includes the "direct edit" element that add HTML for the editable area to 
     * the output page.<p>
     * @param context the current JSP page context
     * @param element the editor element to include       
     * @param editTarget the direct edit target
     * @param editElement the direct edit element
     * @param editOptions the direct edit options
     * @param editPermissions the direct edit permissions
     * @param createLink the direct edit create link
     * 
     * @throws JspException in case something goes wrong
     * 
     * @return the direct edit permissions   
     */
    public static String includeDirectEditElement(
        PageContext context,
        String element,
        String editTarget,
        String editElement,
        String editOptions,
        String editPermissions,
        String createLink) throws JspException {

        ServletRequest req = context.getRequest();
        ServletResponse res = context.getResponse();
        CmsFlexController controller = CmsFlexController.getController(req);

        // check the "direct edit" mode
        String target = null;

        // get the include file where the direct edit HTML is stored in
        target = (String)req.getAttribute(I_CmsEditorActionHandler.C_DIRECT_EDIT_INCLUDE_FILE_URI);
        if ((target != null) && (editPermissions == null)) {
            // check the direct edit permissions of the current user if not provided                  
            editPermissions = OpenCms.getWorkplaceManager().getEditorActionHandler().getEditMode(
                controller.getCmsObject(),
                editTarget,
                null,
                req);
        }

        if (editPermissions == null) {
            return null;
        }
        // append "direct edit" permissions to element
        element = element + "_" + editPermissions;

        // set request parameters required by the included direct edit JSP 
        Map parameterMap = new HashMap();
        CmsJspTagInclude.addParameter(parameterMap, I_CmsConstants.C_PARAMETER_ELEMENT, element, true);
        CmsJspTagInclude.addParameter(
            parameterMap,
            I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_TARGET,
            editTarget,
            true);
        CmsJspTagInclude.addParameter(
            parameterMap,
            I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_LOCALE,
            controller.getCmsObject().getRequestContext().getLocale().toString(),
            true);
        CmsUserSettings settings = new CmsUserSettings(controller.getCmsObject().getRequestContext().currentUser());
        CmsJspTagInclude.addParameter(
            parameterMap,
            I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_BUTTONSTYLE,
            String.valueOf(settings.getDirectEditButtonStyle()),
            true);
        if (editElement != null) {
            CmsJspTagInclude.addParameter(
                parameterMap,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_ELEMENT,
                editElement,
                true);
        }
        if (editOptions != null) {
            CmsJspTagInclude.addParameter(
                parameterMap,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_OPTIONS,
                editOptions,
                true);
        }
        if (createLink != null) {
            CmsJspTagInclude.addParameter(
                parameterMap,
                I_CmsEditorActionHandler.C_DIRECT_EDIT_PARAM_NEWLINK,
                createLink,
                true);
        }

        // save old parameters from current request
        Map oldParameterMap = controller.getCurrentRequest().getParameterMap();

        try {

            controller.getCurrentRequest().addParameterMap(parameterMap);
            context.getOut().print(CmsFlexResponse.C_FLEX_CACHE_DELIMITER);
            controller.getCurrentResponse().addToIncludeList(target, parameterMap);
            controller.getCurrentRequest().getRequestDispatcher(target).include(req, res);

        } catch (ServletException e) {

            Throwable t;
            if (e.getRootCause() != null) {
                t = e.getRootCause();
            } else {
                t = e;
            }
            t = controller.setThrowable(t, target);
            throw new JspException(t);
        } catch (IOException e) {

            Throwable t = controller.setThrowable(e, target);
            throw new JspException(t);
        } finally {

            // restore old parameter map (if required)
            if (oldParameterMap != null) {
                controller.getCurrentRequest().setParameterMap(oldParameterMap);
            }
        }

        return editPermissions;
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
     * @return <code>EVAL_BODY_BUFFERED</code>
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Gets the file with elements for direct editing.<p>
     * 
     * @return the file
     */
    public String getFile() {

        return m_file != null ? m_file : "";
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    public void release() {

        super.release();
        m_file = null;
    }

    /**
     * Sets the file with elements for direct editing.<p>
     * 
     * @param file the file to set 
     */
    public void setFile(String file) {

        if (file != null) {
            m_file = file;
        }
    }
}