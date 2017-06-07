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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Handles the actions that should be performed before opening the editor frameset.<p>
 *
 * For each resource type, a pre editor action class can be defined that is triggered in the workplace JSP
 * <code>/system/workplace/editors/editor.jsp</code> before the editor is initially opened.
 * If an action was performed, be sure to use the static method {@link #sendForwardToEditor(CmsDialog, Map)}
 * to open the editor after the action.<p>
 *
 * @since 6.5.4
 */
public class CmsPreEditorAction extends CmsDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPreEditorAction.class);

    /** The original request parameters passed to the editor. */
    private String m_originalParams;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPreEditorAction(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPreEditorAction(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns if the dialog is currently running in pre editor action mode,
     * depending on the presence of the original request parameters.<p>
     * @param dialog the dialog instance currently used
     * @return true if the dialog is currently running in pre editor action mode, otherwise false
     */
    public static boolean isPreEditorMode(CmsDialog dialog) {

        return CmsStringUtil.isNotEmpty(dialog.getParamOriginalParams());
    }

    /**
     * Forwards to the editor and opens it after the action was performed.<p>
     *
     * @param dialog the dialog instance forwarding to the editor
     */
    public static void sendForwardToEditor(CmsDialog dialog) {

        sendForwardToEditor(dialog, null);
    }

    /**
     * Forwards to the editor and opens it after the action was performed.<p>
     *
     * @param dialog the dialog instance forwarding to the editor
     * @param additionalParams eventual additional request parameters for the editor to use
     */
    public static void sendForwardToEditor(CmsDialog dialog, Map<String, String[]> additionalParams) {

        // create the Map of original request parameters
        Map<String, String[]> params = CmsRequestUtil.createParameterMap(dialog.getParamOriginalParams());
        // put the parameter indicating that the pre editor action was executed
        params.put(PARAM_PREACTIONDONE, new String[] {CmsStringUtil.TRUE});
        if (additionalParams != null) {
            // put the additional parameters to the Map
            params.putAll(additionalParams);
        }
        try {
            // now forward to the editor frameset
            dialog.sendForward(CmsWorkplace.VFS_PATH_EDITORS + "editor.jsp", params);
        } catch (Exception e) {
            // error forwarding, log the exception as error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns if an action has to be performed before opening the editor depending on the resource to edit
     * and request parameter values.<p>
     *
     * @return true if an action has to be performed, then the editor frameset is not generated
     */
    public boolean doPreAction() {

        String resourceName = getParamResource();
        try {
            boolean preActionDone = Boolean.valueOf(getParamPreActionDone()).booleanValue();
            if (!preActionDone) {
                // pre editor action not executed yet now check if a pre action class is given for the resource type
                CmsResource resource = getCms().readResource(resourceName, CmsResourceFilter.ALL);
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                I_CmsPreEditorActionDefinition preAction = OpenCms.getWorkplaceManager().getPreEditorConditionDefinition(
                    type);
                if (preAction != null) {
                    return preAction.doPreAction(resource, this, getOriginalParams());
                }
            }
        } catch (Exception e) {
            // log error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        // nothing to be done as pre action, open the editor
        return false;
    }

    /**
     * Returns the original request parameters for the editor to pass to the pre editor action dialog.<p>
     *
     * @return the original request parameters for the editor
     */
    public String getOriginalParams() {

        if (m_originalParams == null) {
            m_originalParams = CmsEncoder.decode(CmsRequestUtil.encodeParams(getJsp().getRequest()));
        }
        return m_originalParams;
    }

    /**
     * Checks that the current user is a workplace user.<p>
     *
     * @throws CmsRoleViolationException if the user does not have the required role
     */
    @Override
    protected void checkRole() throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.EDITOR);
    }

}
