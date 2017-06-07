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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspTagInclude;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Direct edit provider that uses the same JSP include based logic that has been
 * the default before the 6.2.3 release.<p>
 *
 * Even though placing the HTML of the direct edit buttons appears to be more "flexible" at first,
 * there is a large overhead invloved using this provider as compared to an implementation
 * like {@link CmsDirectEditDefaultProvider}. For every direct edit button on a page,
 * a JSP include is processed <i>twice</i> using this provider,
 * one include for the opening and one for the closing HTML. A JSP include is a costly operation, which means
 * the performance of a website is be impacted if many content managers work on the system that makes great
 * use of direct edit with a lot of elements on a page. In order to avoid this performance impact,
 * OpenCms since version 6.2.3 uses the {@link CmsDirectEditDefaultProvider} by default.<p>
 *
 * This provider DOES NOT support {@link CmsDirectEditMode#MANUAL} mode.<p>
 *
 * @since 6.2.3
 */
public class CmsDirectEditJspIncludeProvider extends A_CmsDirectEditProvider {

    /** Prefix for direct edit end elements, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_AREA_END = "end_directedit";

    /** Prefix for direct edit start elements, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_AREA_START = "start_directedit";

    /** Default direct edit include file URI. */
    public static final String DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT = "/system/workplace/editors/direct_edit.jsp";

    /** Element name for direct edit includes. */
    public static final String DIRECT_EDIT_INCLUDES = "directedit_includes";

    /** Key to identify the edit button style, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_PARAM_BUTTONSTYLE = "__directEditButtonStyle";

    /** Key to identify the edit element, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_PARAM_ELEMENT = "__directEditElement";

    /** Key to identify the edit language, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_PARAM_LOCALE = "__directEditLocale";

    /** Key to identify the link to use for the "new" button (if enabled). */
    public static final String DIRECT_EDIT_PARAM_NEWLINK = "__directEditNewLink";

    /** Key to identify additional direct edit options, used e.g. to control which direct edit buttons are displayed */
    public static final String DIRECT_EDIT_PARAM_OPTIONS = "__directEditOptions";

    /** Key to identify the edit target, used on JPS pages that supply the direct edit html. */
    public static final String DIRECT_EDIT_PARAM_TARGET = "__directEditTarget";

    /** The last direct edit element. */
    protected String m_editElement;

    /** The last direct edit target. */
    protected String m_editTarget;

    /** The last calculated direct edit permissions. */
    protected String m_permissions;

    /**
     * Includes the "direct edit" element that adds HTML for the editable area to
     * the output page.<p>
     *
     * @param context the current JSP page context
     * @param jspIncludeFile the VFS path of the JSP that contains the direct edit HTML fragments
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
        String jspIncludeFile,
        String element,
        String editTarget,
        String editElement,
        String editOptions,
        String editPermissions,
        String createLink) throws JspException {

        if (editPermissions == null) {
            // we do not have direct edit permissions
            return null;
        }

        ServletRequest req = context.getRequest();
        ServletResponse res = context.getResponse();
        CmsFlexController controller = CmsFlexController.getController(req);

        // append "direct edit" permissions to element
        element = element + "_" + editPermissions;

        // set request parameters required by the included direct edit JSP
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        CmsJspTagInclude.addParameter(parameterMap, I_CmsResourceLoader.PARAMETER_ELEMENT, element, true);
        CmsJspTagInclude.addParameter(parameterMap, DIRECT_EDIT_PARAM_TARGET, editTarget, true);
        CmsJspTagInclude.addParameter(
            parameterMap,
            DIRECT_EDIT_PARAM_LOCALE,
            controller.getCmsObject().getRequestContext().getLocale().toString(),
            true);
        CmsUserSettings settings = new CmsUserSettings(controller.getCmsObject());
        CmsJspTagInclude.addParameter(
            parameterMap,
            DIRECT_EDIT_PARAM_BUTTONSTYLE,
            String.valueOf(settings.getDirectEditButtonStyle()),
            true);
        if (editElement != null) {
            CmsJspTagInclude.addParameter(parameterMap, DIRECT_EDIT_PARAM_ELEMENT, editElement, true);
        }
        if (editOptions != null) {
            CmsJspTagInclude.addParameter(parameterMap, DIRECT_EDIT_PARAM_OPTIONS, editOptions, true);
        }
        if (createLink != null) {
            CmsJspTagInclude.addParameter(parameterMap, DIRECT_EDIT_PARAM_NEWLINK, CmsEncoder.encode(createLink), true);
        }

        // save old parameters from current request
        Map<String, String[]> oldParameterMap = controller.getCurrentRequest().getParameterMap();

        try {
            controller.getCurrentRequest().addParameterMap(parameterMap);
            context.getOut().print(CmsFlexResponse.FLEX_CACHE_DELIMITER);
            controller.getCurrentResponse().addToIncludeList(
                jspIncludeFile,
                parameterMap,
                CmsRequestUtil.getAtrributeMap(req));
            controller.getCurrentRequest().getRequestDispatcher(jspIncludeFile).include(req, res);
        } catch (ServletException e) {
            Throwable t;
            if (e.getRootCause() != null) {
                t = e.getRootCause();
            } else {
                t = e;
            }
            t = controller.setThrowable(t, jspIncludeFile);
            throw new JspException(t);
        } catch (IOException e) {
            Throwable t = controller.setThrowable(e, jspIncludeFile);
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
     * @see org.opencms.workplace.editors.directedit.A_CmsDirectEditProvider#init(org.opencms.file.CmsObject, org.opencms.workplace.editors.directedit.CmsDirectEditMode, java.lang.String)
     */
    @Override
    public void init(CmsObject cms, CmsDirectEditMode mode, String fileName) {

        m_cms = cms;
        m_fileName = fileName;
        if (CmsStringUtil.isEmpty(m_fileName)) {
            m_fileName = DIRECT_EDIT_INCLUDE_FILE_URI_DEFAULT;
        }
        m_mode = mode != null ? mode : CmsDirectEditMode.AUTO;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditEnd(javax.servlet.jsp.PageContext)
     */
    public void insertDirectEditEnd(PageContext context) throws JspException {

        if (m_editTarget != null) {
            // otherwise no valid direct edit element has been opened
            includeDirectEditElement(
                context,
                m_fileName,
                DIRECT_EDIT_AREA_END,
                m_editTarget,
                m_editElement,
                null,
                m_permissions,
                null);
            m_editTarget = null;
            m_permissions = null;
            m_editElement = null;
        }
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditIncludes(javax.servlet.jsp.PageContext, org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    public void insertDirectEditIncludes(PageContext context, CmsDirectEditParams params) throws JspException {

        try {
            CmsJspTagInclude.includeTagAction(
                context,
                m_fileName,
                DIRECT_EDIT_INCLUDES,
                false,
                null,
                null,
                context.getRequest(),
                context.getResponse());
        } catch (Throwable t) {
            // should never happen
            throw new JspException(t);
        }
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#insertDirectEditStart(javax.servlet.jsp.PageContext, org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    public boolean insertDirectEditStart(PageContext context, CmsDirectEditParams params) throws JspException {

        String result = null;
        CmsDirectEditPermissions permissions = getResourceInfo(params.getResourceName()).getPermissions();
        if (permissions.getPermission() > 0) {
            // permission to direct edit is granted
            m_permissions = permissions.toString();
            m_editTarget = params.getResourceName();
            m_editElement = params.getElement();

            result = includeDirectEditElement(
                context,
                m_fileName,
                DIRECT_EDIT_AREA_START,
                m_editTarget,
                m_editElement,
                params.getButtonSelection().toString(),
                m_permissions,
                params.getLinkForNew());

        } else {
            // no direct edit permissions
            m_editTarget = null;
            m_permissions = null;
            m_editElement = null;
        }
        return result != null;
    }

    /**
     * Returns <code>false</code> because the JSP include provider does not support manual button placement.<p>
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

        CmsDirectEditJspIncludeProvider result = new CmsDirectEditJspIncludeProvider();
        result.m_configurationParameters = m_configurationParameters;
        return result;
    }
}