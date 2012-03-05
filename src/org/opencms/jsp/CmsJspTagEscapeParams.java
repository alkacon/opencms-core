/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.flex.CmsFlexRequest;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag is used to enable parameter escaping for a single Flex Request.<p>
 */
public class CmsJspTagEscapeParams extends TagSupport {

    /** Serial version id. */
    private static final long serialVersionUID = -3571347944585254L;

    /** A string containing a comma-separated list of exceptional parameters which shouldn't be escaped. */
    private String m_exceptions;

    /**
     * Static method which provides the actual functionality of this tag.<p>
     * 
     * @param request the request for which the parameters should be escaped 
     * @param paramExceptions the comma-separated exceptional parameter list 
     */
    public static void escapeParamsTagAction(ServletRequest request, String paramExceptions) {

        if (request instanceof CmsFlexRequest) {
            CmsFlexRequest flexRequest = (CmsFlexRequest)request;
            List<String> exceptions = Collections.emptyList();
            if (paramExceptions != null) {
                exceptions = CmsStringUtil.splitAsList(paramExceptions, ",");
            }
            flexRequest.enableParameterEscaping(exceptions);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        escapeParamsTagAction(pageContext.getRequest(), m_exceptions);
        return SKIP_BODY;
    }

    /**
     * Sets the parameters which shouldn't be escaped.<p>
     * 
     * @param exceptions a comma-separated list of parameters which shouldn't be escaped
     */
    public void setExceptions(String exceptions) {

        m_exceptions = exceptions;
    }

}
