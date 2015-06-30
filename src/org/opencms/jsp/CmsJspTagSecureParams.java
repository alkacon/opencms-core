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

import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This tag is used to enable parameter escaping for a single Flex Request.<p>
 */
public class CmsJspTagSecureParams extends TagSupport {

    /** Serial version id. */
    private static final long serialVersionUID = -3571347944585254L;

    /** The policy path. */
    private String m_policy;

    /** The comma-separated list of parameters for which XML characters will not be escaped. */
    private String m_allowXml;

    /** The comma-separated list of parameters for which HTML will be allowed, but be escaped. */
    private String m_allowHtml;

    /**
     * Static method which provides the actual functionality of this tag.<p>
     *
     * @param request the request for which the parameters should be escaped
     *
     * @param allowXml the comma-separated list of parameters for which XML characters will not be escaped
     * @param allowHtml the comma-separated list of parameters for which HTML will be allowed, but be escaped
     * @param policy  the site path of an AntiSamy policy file
     */
    public static void secureParamsTagAction(ServletRequest request, String allowXml, String allowHtml, String policy) {

        if (request instanceof CmsFlexRequest) {
            CmsFlexRequest flexRequest = (CmsFlexRequest)request;
            CmsObject cms = CmsFlexController.getCmsObject(flexRequest);
            List<String> exceptions = Collections.emptyList();
            if (allowXml != null) {
                exceptions = CmsStringUtil.splitAsList(allowXml, ",");
            }
            flexRequest.enableParameterEscaping();
            flexRequest.getParameterEscaper().setExceptions(exceptions);
            Set<String> allowHtmlSet = Collections.emptySet();
            if (allowHtml != null) {
                allowHtmlSet = new HashSet<String>(CmsStringUtil.splitAsList(allowHtml, ","));
                flexRequest.getParameterEscaper().enableAntiSamy(cms, policy, allowHtmlSet);
            }
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() {

        secureParamsTagAction(pageContext.getRequest(), m_allowXml, m_allowHtml, m_policy);
        return SKIP_BODY;
    }

    /**
     * Sets the 'allowHtml' parameter.<p>
     *
     * @param allowHtml the new 'allowHtml' parameter
     */
    public void setAllowHtml(String allowHtml) {

        m_allowHtml = allowHtml;
    }

    /**
     * Sets the 'allowXml' parameter.<p>
     *
     * @param allowXml the new 'allowXml' parameter
     */
    public void setAllowXml(String allowXml) {

        m_allowXml = allowXml;
    }

    /**
     * Sets the 'policy' parameter.<p>
     *
     * @param policy the new 'policy' parameter
     */
    public void setPolicy(String policy) {

        m_policy = policy;
    }

}
