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

import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Allows to have certain JSP code on a JSP not processed by OpenCms,
 * which can be useful in case you want to create a JSP page using the OpenCms static export.<p>
 *
 * Usually, if you want to create a JSP page using the OpenCms static export,
 * some parts of the page should be processed by OpenCms, while other parts of the JSP
 * are processed later when the exported JSP is deployed in another servlet container.
 * A typical use case is that you have a template applied to the page in OpenCms, but the body content of the page
 * is generated after the static export, for example by a database application.<p>
 *
 * <b>Please note:</b> In order to static export a JSP page with the <code>".jsp"</code> suffix, you need to add the
 * property <code>"exportsuffix"</code> with the value <code>".jsp"</code> to the OpenCms JSP file in the VFS.
 * Otherwise the static export will always add the suffix <code>".html"</code>.<p>
 *
 * @since 7.0.4
 */
public class CmsJspTagExport extends BodyTagSupport {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 6326430271724241959L;

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
     */
    @Override
    public int doStartTag() {

        return EVAL_BODY_INCLUDE;
    }
}