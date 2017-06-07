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

package org.opencms.workplace.editors.directedit;

import org.opencms.cache.CmsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.Messages;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provider for the OpenCms default graphical "direct edit" buttons.<p>
 *
 * Since OpenCms version 6.2.3,
 * this provider is configured as the standard direct edit provider in a common OpenCms installation.<p>
 *
 * This provider DOES NOT support {@link CmsDirectEditMode#MANUAL} mode.<p>
 *
 * @since 6.2.3
 */
public class CmsDirectEditDefaultProvider extends A_CmsDirectEditProvider {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDirectEditDefaultProvider.class);

    /** Indicates the permissions for the last element the was opened. */
    protected int m_lastPermissionMode;

    /** The include file used by this provider. */
    private String m_headerInclude;

    /**
     * Returns the end HTML for a disabled direct edit button.<p>
     *
     * @return the end HTML for a disabled direct edit button
     */
    public String endDirectEditDisabled() {

        return "</div>\n<!-- EDIT BLOCK END (DISABLED) -->\n";
    }

    /**
     * Returns the end HTML for an enabled direct edit button.<p>
     *
     * @return the end HTML for an enabled direct edit button
     */
    public String endDirectEditEnabled() {

        return "</div>\n<!-- EDIT BLOCK END (ENABLED) -->\n";
    }

    /**
     * Returns the direct edit include HTML to insert in the page beginning.<p> t
     *
     * @param params the parameters for the direct edit includes
     *
     * @return the direct edit include HTML to insert in the page beginning
     */
    public String getDirectEditIncludes(CmsDirectEditParams params) {

        CmsMacroResolver resolver = prepareMacroResolverForIncludes(params);
        return resolver.resolveMacros(m_headerInclude);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.A_CmsDirectEditProvider#init(org.opencms.file.CmsObject, org.opencms.workplace.editors.directedit.CmsDirectEditMode, java.lang.String)
     */
    @Override
    public void init(CmsObject cms, CmsDirectEditMode mode, String fileName) {

        super.init(cms, mode, fileName);

        // check if the selected include file is available in the cache
        CmsMemoryObjectCache cache = CmsMemoryObjectCache.getInstance();
        m_headerInclude = (String)cache.getCachedObject(CmsDirectEditDefaultProvider.class, m_fileName);

        if (m_headerInclude == null) {
            // the file is not available in the cache
            try {
                CmsFile file = m_cms.readFile(m_fileName);
                // create a String with the right encoding
                m_headerInclude = getContentAsString(file);
                // store this in the cache
                cache.putCachedObject(CmsDirectEditDefaultProvider.class, m_fileName, m_headerInclude);

            } catch (CmsException e) {
                // this should better not happen
                m_headerInclude = "";
                LOG.error(Messages.get().getBundle().key(Messages.LOG_DIRECT_EDIT_NO_HEADER_1, fileName), e);
            }
        }
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
    public void insertDirectEditIncludes(PageContext context, CmsDirectEditParams params) throws JspException {

        print(context, getDirectEditIncludes(params));
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

        CmsDirectEditDefaultProvider result = new CmsDirectEditDefaultProvider();
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

        String editId = getNextDirectEditId();
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

        result.append("<script type=\"text/javascript\">\n");
        result.append("registerButtonOcms(\"").append(editId).append("\");\n");
        result.append("</script>\n");
        result.append("<div class=\"ocms_de_bt\" id=\"buttons_").append(editId).append("\">\n");
        result.append("<span onmouseover=\"activateOcms(\'").append(editId).append(
            "\');\" onmouseout=\"deactivateOcms(\'").append(editId).append("\');\">\n");
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"table_").append(editId).append(
            "\"><tr>\n");
        result.append("<td class=\"ocms_de\"><span class=\"ocms_disabled\">");
        if (m_editButtonStyle == 1) {
            result.append("<span class=\"ocms_combobutton\" style=\"background-image: url(\'").append(
                CmsWorkplace.getSkinUri()).append("buttons/directedit_in.png\');\">&nbsp;").append(
                    m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("</span>");
        } else if (m_editButtonStyle == 2) {
            result.append("<span class=\"ocms_combobutton\" style=\"padding-left: 4px;\">").append(
                m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("</span>");
        } else {
            result.append("<img border=\"0\" src=\"").append(CmsWorkplace.getSkinUri()).append(
                "buttons/directedit_in.png\" title=\"").append(
                    m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("\" alt=\"\"/>");
        }
        result.append("</span></td>\n");
        result.append("</tr></table>\n");
        result.append("</span>\n");
        result.append("</div>\n");
        result.append("<div id=\"").append(editId).append("\" class=\"ocms_de_norm\">\n");

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

        result.append("<script type=\"text/javascript\">\n");
        result.append("registerButtonOcms(\"").append(editId).append("\");\n");
        result.append("</script>\n");
        result.append("<div class=\"ocms_de_bt\" id=\"buttons_").append(editId).append("\">\n");
        result.append("<form name=\"form_").append(editId).append("\" id=\"form_").append(editId).append(
            "\" method=\"post\" action=\"").append(editLink).append("\" class=\"ocms_nomargin\" target=\"_top\">\n");
        result.append("<input type=\"hidden\" name=\"resource\" value=\"").append(params.getResourceName()).append(
            "\"/>\n");
        result.append("<input type=\"hidden\" name=\"directedit\" value=\"true\"/>\n");
        result.append("<input type=\"hidden\" name=\"elementlanguage\" value=\"").append(editLocale).append("\"/>\n");
        result.append("<input type=\"hidden\" name=\"elementname\" value=\"").append(params.getElement()).append(
            "\"/>\n");
        result.append("<input type=\"hidden\" name=\"backlink\" value=\"").append(uri).append("\"/>\n");
        result.append("<input type=\"hidden\" name=\"newlink\"/>\n");
        result.append("<input type=\"hidden\" name=\"closelink\"/>\n");
        result.append("<input type=\"hidden\" name=\"redirect\" value=\"true\"/>\n");
        result.append("<input type=\"hidden\" name=\"editortitle\"/>\n");
        result.append("</form>\n");
        result.append("<table onmouseover=\"activateOcms(\'").append(editId).append(
            "\');\" onmouseout=\"deactivateOcms(\'").append(editId).append("\');\"");
        result.append(" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"table_").append(editId).append(
            "\"><tr>\n");
        if (params.getButtonSelection().isShowEdit()) {
            result.append("<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:submitOcms(\'").append(
                editId).append("\', \'").append(CmsDirectEditButtonSelection.VALUE_EDIT).append(
                    "\');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className=\'ocms_over\'\" onmouseout=\"className=\'ocms_over\'\" onmousedown=\"className=\'ocms_push\'\" onmouseup=\"className=\'ocms_over\'\">");
            if (m_editButtonStyle == 1) {
                result.append("<span id=\"bt_").append(editId).append(
                    "\" class=\"ocms_combobutton\" style=\"background-image: url(\'").append(
                        CmsWorkplace.getSkinUri()).append("buttons/directedit_cl.png\');\">&nbsp;").append(
                            m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_EDIT_0)).append("</span>");
            } else if (m_editButtonStyle == 2) {
                result.append("<span class=\"ocms_combobutton\" style=\"padding-left: 4px;\">").append(
                    m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_EDIT_0)).append("</span>");
            } else {
                result.append("<span id=\"bt_").append(editId).append(
                    "\" class=\"ocms_combobutton\" style=\"padding-left: 15px; padding-right: 1px; background-image: url(\'").append(
                        CmsWorkplace.getSkinUri()).append(
                            "buttons/directedit_cl.png\'); background-position: 0px 0px;\" title=\"").append(
                                m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_EDIT_0)).append("\">&nbsp;</span>");
            }
            result.append("</span></a></td>\n");
        }
        if (params.getButtonSelection().isShowDelete()) {
            result.append("<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:submitOcms(\'").append(
                editId).append("\', \'").append(CmsDirectEditButtonSelection.VALUE_DELETE).append(
                    "\');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className=\'ocms_over\'\" onmouseout=\"className=\'ocms_over\'\" onmousedown=\"className=\'ocms_push\'\" onmouseup=\"className=\'ocms_over\'\">");
            if (m_editButtonStyle == 1) {
                result.append("<span id=\"del_").append(editId).append(
                    "\" class=\"ocms_combobutton\" style=\"background-image: url(\'").append(
                        CmsWorkplace.getSkinUri()).append("buttons/deletecontent.png\');\">&nbsp;").append(
                            m_messages.key(Messages.GUI_BUTTON_DELETE_0)).append("</span>");
            } else if (m_editButtonStyle == 2) {
                result.append("<span class=\"ocms_combobutton\" style=\"padding-left: 4px;\">").append(
                    m_messages.key(Messages.GUI_BUTTON_DELETE_0)).append("</span>");
            } else {
                result.append("<img border=\"0\" src=\"").append(CmsWorkplace.getSkinUri()).append(
                    "buttons/deletecontent.png\" title=\"").append(m_messages.key(Messages.GUI_BUTTON_DELETE_0)).append(
                        "\" alt=\"\"/>");
            }
            result.append("</span></a></td>\n");
        }
        if (params.getButtonSelection().isShowNew()) {
            result.append("<td class=\"ocms_de\"><a href=\"#\" onclick=\"javascript:submitOcms(\'").append(
                editId).append("\', \'").append(CmsDirectEditButtonSelection.VALUE_NEW).append("\', \'").append(
                    editNewLink).append(
                        "\');\" class=\"ocms_button\"><span class=\"ocms_over\" onmouseover=\"className=\'ocms_over\'\" onmouseout=\"className=\'ocms_over\'\" onmousedown=\"className=\'ocms_push\'\" onmouseup=\"className=\'ocms_over\'\">");
            if (m_editButtonStyle == 1) {
                result.append("<span id=\"new_").append(editId).append(
                    "\" class=\"ocms_combobutton\" style=\"background-image: url(\'").append(
                        CmsWorkplace.getSkinUri()).append("buttons/new.png\');\">&nbsp;").append(
                            m_messages.key(Messages.GUI_BUTTON_NEW_0)).append("</span>");
            } else if (m_editButtonStyle == 2) {
                result.append("<span class=\"ocms_combobutton\" style=\"padding-left: 4px;\">").append(
                    m_messages.key(Messages.GUI_BUTTON_NEW_0)).append("</span>");
            } else {
                result.append("<img border=\"0\" src=\"").append(CmsWorkplace.getSkinUri()).append(
                    "buttons/new.png\" title=\"").append(m_messages.key(Messages.GUI_BUTTON_NEW_0)).append(
                        "\" alt=\"\"/>");
            }
            result.append("</span></a></td>");
        }
        result.append("</tr></table>\n");
        result.append("</div>\n");
        result.append("<div id=\"").append(editId).append("\" class=\"ocms_de_norm\">");
        return result.toString();
    }

    /**
     * Helper method to convert the content of a resource to a string.<p>
     *
     * @param file the file
     * @return the file content as a string
     *
     * @throws CmsException if something goes wrong
     */
    protected String getContentAsString(CmsFile file) throws CmsException {

        CmsProperty p = m_cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true);
        String e = p.getValue();
        if (e == null) {
            e = OpenCms.getSystemInfo().getDefaultEncoding();
        }
        return CmsEncoder.createString(file.getContents(), e);
    }

    /**
     * Prepares the macro resolver which is used to process the included text file.<p>
     *
     * @param params the direct edit parameters
     *
     * @return the macro resolver
     */
    protected CmsMacroResolver prepareMacroResolverForIncludes(CmsDirectEditParams params) {

        String closeLink = getLink(params.getLinkForClose());
        String deleteLink = getLink(params.getLinkForDelete());
        String titleForNew = m_messages.key(Messages.GUI_EDITOR_TITLE_NEW_0);
        String skinUri = CmsWorkplace.getSkinUri();
        // resolve macros in include header
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.addMacro("closeLink", closeLink);
        resolver.addMacro("deleteLink", deleteLink);
        resolver.addMacro("titleForNew", titleForNew);
        resolver.addMacro("skinUri", skinUri);
        return resolver;
    }
}