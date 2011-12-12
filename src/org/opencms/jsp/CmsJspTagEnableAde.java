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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.OpenCms;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Implementation of the <code>&lt;enable-ade/&gt;</code> tag.<p>
 * 
 * @since 7.6 
 */
public class CmsJspTagEnableAde extends BodyTagSupport {

    /** The container-page editor jsp-include URI. */
    private static final String INCLUDE_JSP_URI = "/system/modules/org.opencms.ade.containerpage/containerpage_include.jsp";

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
            // direct edit has been disabled for this request
            return;
        }

        I_CmsDirectEditProvider eb = new CmsAdvancedDirectEditProvider();
        eb.init(cms, CmsDirectEditMode.TRUE, "");
        CmsJspTagEditable.setDirectEditProvider(context, eb);

        CmsJspTagInclude.includeTagAction(context, INCLUDE_JSP_URI, null, false, null, null, req, context.getResponse());
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