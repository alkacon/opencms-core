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

package org.opencms.jsp;

import org.opencms.ade.containerpage.CmsContainerpageActionElement;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;enable-ade/&gt;</code> tag.<p>
 *
 * @since 7.6
 */
public class CmsJspTagEnableAde extends BodyTagSupport {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEnableAde.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 8447599916548975733L;

    /**
     * Enable-ade action method.<p>
     *
     * @param context the current JSP page context
     *
     * @throws JspException in case something goes wrong
     */
    public static void enableAdeTagAction(PageContext context) throws JspException {

        ServletRequest req = context.getRequest();
        if (CmsHistoryResourceHandler.isHistoryRequest(req)) {
            // don't display advanced direct edit buttons on an historical resource
            return;
        }

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // advanced direct edit is never enabled in the online project
            return;
        }

        if (CmsResource.isTemporaryFileName(cms.getRequestContext().getUri())) {
            // don't display advanced direct edit buttons if a temporary file is displayed
            return;
        }
        updateDirectEditFlagInSession(req);
        if (isDirectEditDisabled(req)) {
            try {
                String buttonLeft = null;
                Integer left = (Integer)((HttpServletRequest)req).getSession().getAttribute(
                    CmsGwtConstants.PARAM_BUTTON_LEFT);

                if (left != null) {
                    buttonLeft = left.toString() + "px";
                } else {
                    buttonLeft = "20%";
                }
                String titleMessage = Messages.get().getBundle(
                    OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(Messages.GUI_TOOLBAR_ENABLE_EDIT_MODE_0);
                context.getOut().print(getPreviewInclude(buttonLeft, titleMessage));
            } catch (IOException e) {
                throw new JspException(e);
            }
        } else {

            I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
            eb.init(cms, CmsDirectEditMode.TRUE, "");
            CmsJspTagEditable.setDirectEditProvider(context, eb);

            try {
                CmsContainerpageActionElement actionEl = new CmsContainerpageActionElement(
                    context,
                    (HttpServletRequest)req,
                    (HttpServletResponse)context.getResponse());
                context.getOut().print(actionEl.exportAll());
            } catch (Exception e) {
                throw new JspException(e);
            }
        }
    }

    /**
     * Returns if direct edit is disabled for the current request.<p>
     *
     * @param request the servlet request
     *
     * @return <code>true</code> if direct edit is disabled for the current request
     */
    public static boolean isDirectEditDisabled(ServletRequest request) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(request.getParameter(CmsGwtConstants.PARAM_TEMPLATE_CONTEXT))) {
            return true;
        }
        String disabledParam = request.getParameter(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(disabledParam)) {
            return Boolean.parseBoolean(disabledParam);
        } else {
            HttpSession session = ((HttpServletRequest)request).getSession(false);
            Boolean disabledAttr = null == session
            ? null
            : (Boolean)session.getAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
            return (disabledAttr != null) && disabledAttr.booleanValue();
        }
    }

    /**
     * Removes the direct edit flag from session, turning the preview mode off.<p>
     *
     * @param session the session
     */
    public static void removeDirectEditFlagFromSession(HttpSession session) {

        session.removeAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
    }

    /**
     * Updates the direct edit flag in the session and also storing the button left info if available.<p>
     *
     * @param request the request
     */
    public static void updateDirectEditFlagInSession(ServletRequest request) {

        String disabledParam = request.getParameter(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(disabledParam)) {
            if (Boolean.parseBoolean(disabledParam)) {
                ((HttpServletRequest)request).getSession().setAttribute(
                    CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT,
                    Boolean.TRUE);
                String buttonLeft = request.getParameter(CmsGwtConstants.PARAM_BUTTON_LEFT);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(buttonLeft)) {
                    Integer left = null;
                    try {
                        left = Integer.valueOf(buttonLeft);
                        if (left.intValue() > 0) {
                            ((HttpServletRequest)request).getSession().setAttribute(
                                CmsGwtConstants.PARAM_BUTTON_LEFT,
                                left);
                        }
                    } catch (NumberFormatException e) {
                        // malformed parameter, ignore
                    }
                }
            } else {
                ((HttpServletRequest)request).getSession().removeAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
            }
        }
    }

    /**
     * Returns the preview mode include.<p>
     *
     * @param buttonLeft the button left parameter
     * @param titleMessage the title attribute of the "Editor mode" button rendered by the include
     *
     * @return the preview mode include
     */
    private static String getPreviewInclude(String buttonLeft, String titleMessage) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<style type=\"text/css\"> @import url(\"").append(
            CmsGwtActionElement.getFontIconCssLink()).append("\"); </style>\n");
        String heartbeatUrl = CmsStringUtil.joinPaths(
            OpenCms.getStaticExportManager().getVfsPrefix(),
            OpenCmsServlet.HANDLE_BUILTIN_SERVICE,
            CmsGwtConstants.HANDLER_UPDATE_SESSION);
        JSONObject previewSettings = new JSONObject();
        try {
            previewSettings.put("heartbeatUrl", heartbeatUrl);
            previewSettings.put("buttonLeft", buttonLeft);
            previewSettings.put("titleMessage", CmsEncoder.escapeXml(titleMessage));
        } catch (JSONException e) {
            // shouldn't happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        buffer.append("<script>\nvar previewSettings = " + previewSettings.toString() + ";\n</script>\n");
        buffer.append("<script src=\"" + CmsWorkplace.getStaticResourceUri("/ade/page-preview.js") + "\"></script>\n");
        return buffer.toString();
    }

    /**
     * Close the direct edit tag, also prints the direct edit HTML to the current page.<p>
     *
     * @return {@link #EVAL_PAGE}
     *
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doEndTag() throws JspException {

        // only execute action for the first "ade" tag on the page (include file)
        enableAdeTagAction(pageContext);

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }

        return EVAL_PAGE;
    }

    /**
     * Opens the direct edit tag, if manual mode is set then the next
     * start HTML for the direct edit buttons is printed to the page.<p>
     *
     * @return {@link #EVAL_BODY_INCLUDE}
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_INCLUDE;
    }
}