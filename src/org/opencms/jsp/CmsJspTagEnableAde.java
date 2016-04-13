/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.CmsContainerpageActionElement;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of the <code>&lt;enable-ade/&gt;</code> tag.<p>
 *
 * @since 7.6
 */
public class CmsJspTagEnableAde extends BodyTagSupport {

    /** The preview mode JavaScript include. */
    private static final String PREVIEW_INCLUDE_SCRIPT = "<script type=\"text/javascript\">\n"
        + "function openEditor(){\n"
        + "var target=window.location.href;"
        + "target=target.replace(\""
        + CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT
        + "=true\",\"\");\n"
        + "window.location.href=target;\n"
        + "}\n"
        + "function injectButton(){\n"
        + "if (self === top){\n"
        + "var injectElement=document.createElement(\"div\");\n"
        + "injectElement.innerHTML=\"<button id='opencms-leave-preview' class='opencms-icon opencms-icon-edit-point cmsState-up' onClick='openEditor()'></button>\";\n"
        + "document.body.appendChild(injectElement);\n"
        + "}"
        + "}\n"
        + "document.addEventListener(\"DOMContentLoaded\",injectButton);\n"
        + "\n</script>\n";

    /** The preview mode CSS include. */
    private static final String PREVIEW_INCLUDE_STYLE = "\n<style type=\"text/css\">\n"
        + "button#opencms-leave-preview{\n"
        + "font-size:32px;\n"
        + "color:#474747;\n"
        + "border:none;\n"
        + "background:transparent;\n"
        + "position:fixed;\n"
        + "top:5px;\n"
        + "left:20%;\n"
        + "z-index:1000000;"
        + "padding: 4px;\n"
        + "}\n"
        + "button#opencms-leave-preview:hover{\n"
        + "color:#356EE1;\n"
        + "}\n"
        + "button#opencms-leave-preview:after{\n"
        + "content:\"\";"
        + "position:absolute;\n"
        + "z-index:-1;\n"
        + "background: #fff;\n"
        + "top:0;\n"
        + "left:0;\n"
        + "right:0;\n"
        + "bottom:0;\n"
        + "opacity:0.7;\n"
        + "border-radius:4px;\n"
        + "</style>\n";

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

        if (CmsJspTagEditable.isDirectEditDisabled(req)) {
            try {
                context.getResponse().getWriter().print(getPreviewInclude());
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
                context.getResponse().getWriter().print(actionEl.exportAll());
            } catch (Exception e) {
                throw new JspException(e);
            }
        }
    }

    /**
     * Returns the preview mode include.<p>
     *
     * @return the preview mode include
     */
    private static String getPreviewInclude() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("\n<style type=\"text/css\">\n @import url(\"").append(
            CmsGwtActionElement.getFontIconCssLink()).append("\");\n </style>\n");
        buffer.append(PREVIEW_INCLUDE_STYLE);
        buffer.append(PREVIEW_INCLUDE_SCRIPT);
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