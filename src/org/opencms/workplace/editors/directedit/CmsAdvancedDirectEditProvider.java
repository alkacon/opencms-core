/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/directedit/Attic/CmsAdvancedDirectEditProvider.java,v $
 * Date   : $Date: 2009/09/25 08:44:01 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.directedit;

import org.opencms.i18n.CmsEncoder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provider for the OpenCms AdvancedDirectEdit.<p>
 * 
 * Since OpenCms version 7.9.1.<p>
 * 
 * This provider DOES NOT support {@link CmsDirectEditMode#MANUAL} mode.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.9.1
 */
public class CmsAdvancedDirectEditProvider extends A_CmsDirectEditProvider {

    /** Indicates the permissions for the last element the was opened. */
    protected int m_lastPermissionMode;

    /**
     * Returns the end HTML for a disabled direct edit button.<p>
     * 
     * @return the end HTML for a disabled direct edit button
     */
    public String endDirectEditDisabled() {

        return "";
    }

    /**
     * Returns the end HTML for an enabled direct edit button.<p>
     * 
     * @return the end HTML for an enabled direct edit button
     */
    public String endDirectEditEnabled() {

        return "";
    }

    /**
     * Returns the direct edit include HTML to insert in the page beginning.<p> t
     * 
     * @param params the parameters for the direct edit includes
     *  
     * @return the direct edit include HTML to insert in the page beginning
     */
    public String getDirectEditIncludes(CmsDirectEditParams params) {

        // For Advanced Direct Edit all necessary js and css-code is included by the enableADE tag. Further includes in the head are not needed. 

        return "";
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditEnd(javax.servlet.jsp.PageContext)
     */
    public void insertDirectEditEnd(PageContext context) throws JspException {

        String content;
        switch (m_lastPermissionMode) {

            case 1: // disabled
                content = endDirectEditDisabled();
                break;
            case 2: // enabled
                content = endDirectEditEnabled();
                break;
            default: // inactive or undefined
                content = null;
        }
        m_lastPermissionMode = 0;
        print(context, content);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditIncludes(javax.servlet.jsp.PageContext, org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    public void insertDirectEditIncludes(PageContext context, CmsDirectEditParams params) {

        // For Advanced Direct Edit all necessary js and css-code is included by the enableADE tag. Further includes in the head are not needed.

    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditStart(javax.servlet.jsp.PageContext, org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    public boolean insertDirectEditStart(PageContext context, CmsDirectEditParams params) throws JspException {

        String content;
        // check the direct edit permissions of the current user          
        CmsDirectEditResourceInfo resourceInfo = getResourceInfo(params.getResourceName());
        // check the permission mode
        m_lastPermissionMode = resourceInfo.getPermissions().getPermission();
        switch (m_lastPermissionMode) {
            case 1: // disabled
                content = startDirectEditDisabled(params, resourceInfo);
                break;
            case 2: // enabled
                content = startDirectEditEnabled(params, resourceInfo);
                break;
            default: // inactive or undefined
                content = null;
        }
        print(context, content);
        return content != null;
    }

    /**
     * Returns <code>false</code> because the default provider does not support manual button placement.<p>
     * 
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#isManual(org.opencms.workplace.editors.directedit.CmsDirectEditMode)
     */
    @Override
    public boolean isManual(CmsDirectEditMode mode) {

        return false;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#newInstance()
     */
    public I_CmsDirectEditProvider newInstance() {

        CmsAdvancedDirectEditProvider result = new CmsAdvancedDirectEditProvider();
        result.m_configurationParameters = m_configurationParameters;
        return result;
    }

    /**
     * Returns the start HTML for a disabled direct edit button.<p>
     * 
     * @param params the direct edit parameters
     * @param resourceInfo contains information about the resource to edit
     * 
     * @return the start HTML for a disabled direct edit button
     */
    public String startDirectEditDisabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        StringBuffer result = new StringBuffer(256);

        result.append("<!-- EDIT BLOCK START (DISABLED): ");
        result.append(params.m_resourceName);
        result.append(" [");
        result.append(resourceInfo.getResource().getState());
        result.append("] ");
        if (!resourceInfo.getLock().isUnlocked()) {
            result.append(" locked ");
            result.append(resourceInfo.getLock().getProject().getName());
        }
        result.append(" -->\n");
        return result.toString();
    }

    /**
     * Returns the start HTML for an enabled direct edit button.<p>
     *
     * @param params the direct edit parameters
     * @param resourceInfo contains information about the resource to edit
     * 
     * @return the start HTML for an enabled direct edit button
     */
    public String startDirectEditEnabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        String editLocale = m_cms.getRequestContext().getLocale().toString();
        String editId = getNextDirectEditId();
        StringBuffer result = new StringBuffer(512);

        String uri = m_cms.getRequestContext().getUri();

        String editLink = getLink(params.getLinkForEdit());
        String editNewLink = CmsEncoder.encode(params.getLinkForNew());

        result.append("<!-- EDIT BLOCK START (ENABLED): ");
        result.append(params.m_resourceName);
        result.append(" [");
        result.append(resourceInfo.getResource().getState());
        result.append("]");
        if (!resourceInfo.getLock().isUnlocked()) {
            result.append(" locked ");
            result.append(resourceInfo.getLock().getProject().getName());
        }
        result.append(" -->\n");

        result.append("<div class='cms-editable' rel='ade_").append(resourceInfo.getResource().getStructureId()).append(
            "'>");
        result.append("<form name=\"form_").append(editId).append("\" id=\"form_").append(editId).append(
            "\" method=\"post\" action=\"").append(editLink).append(
            "\" class=\"cms-editable-form\" target=\"_top\" onsubmit=\"return false;\">\n");
        result.append("<input type=\"hidden\" name=\"resource\" value=\"").append(params.getResourceName()).append(
            "\"/>\n");
        result.append("<input type=\"hidden\" name=\"directedit\" value=\"true\"/>\n");
        result.append("<input type=\"hidden\" name=\"elementlanguage\" value=\"").append(editLocale).append("\"/>\n");
        result.append("<input type=\"hidden\" name=\"elementname\" value=\"").append(params.getElement()).append(
            "\"/>\n");
        result.append("<input type=\"hidden\" name=\"backlink\" value=\"").append(uri).append("\"/>\n");
        result.append("<input type=\"hidden\" name=\"newlink\" value=\"").append(editNewLink).append("\" />\n");
        result.append("<input type=\"hidden\" name=\"closelink\"/>\n");
        result.append("<input type=\"hidden\" name=\"redirect\" value=\"true\"/>\n");
        result.append("<input type=\"hidden\" name=\"editortitle\"/>\n");
        result.append("</form>\n");

        // append required buttons
        result.append("<div class='cms-directedit-buttons'>\n");
        if (params.getButtonSelection().isShowEdit()) {
            result.append("<a class='cms-edit-enabled'></a>\n");
        } else {
            result.append("<a class='cms-edit-disabled'></a>\n");
        }
        if (params.getButtonSelection().isShowDelete()) {
            result.append("<a class='cms-delete'></a>\n");
        }
        if (params.getButtonSelection().isShowNew()) {
            result.append("<a class='cms-new'></a>\n");
        }
        result.append("</div>\n");
        result.append("</div>\n");
        return result.toString();
    }
}