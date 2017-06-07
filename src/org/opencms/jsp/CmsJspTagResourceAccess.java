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

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.util.CmsJspResourceAccessBean;
import org.opencms.main.OpenCms;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Used to access resource information from the current open <code>&lt;cms:resourceload&gt;</code>
 * tag using JSP page context and the JSP EL.<p>
 *
 * The tag will create an instance of a {@link CmsJspResourceAccessBean} that is stored in the selected context.
 * Use the options provided by the bean to access the resource directly.<p>
 *
 * For example together with the JSTL, use this tag inside an open tag like this:<pre>
 * &lt;cms:resourceload ... &gt;
 *     &lt;cms:resourceaccess var="myVarName" scope="page" /&gt;
 *     ... other code ...
 * &lt;/cms:resourceload&gt;</pre>
 *
 * @since 8.0
 */
public class CmsJspTagResourceAccess extends CmsJspScopedVarBodyTagSuport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 2588220869205763894L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() {

        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        // get the current users OpenCms context
        CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());

        // get a reference to the parent "content container" class
        Tag ancestor = findAncestorWithClass(this, I_CmsResourceContainer.class);
        if (ancestor == null) {
            CmsMessageContainer errMsgContainer = Messages.get().container(
                Messages.ERR_PARENTLESS_TAG_1,
                "resourceaccess");
            String msg = Messages.getLocalizedMessage(errMsgContainer, pageContext);
            throw new JspTagException(msg);
        }
        // get the currently open resource container
        I_CmsResourceContainer resourceContainer = (I_CmsResourceContainer)ancestor;

        // initialize a new instance of a resource access bean
        CmsJspResourceAccessBean bean = new CmsJspResourceAccessBean(cms, resourceContainer.getResource());

        // store the resource in the selected page context scope
        storeAttribute(bean);

        return SKIP_BODY;
    }
}