/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInfo.java,v $
 * Date   : $Date: 2002/11/09 12:13:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


package com.opencms.flex.jsp;

import com.opencms.boot.CmsBase;
import com.opencms.flex.cache.CmsFlexRequest;

/**
 * Provides access to some system information like OpenCms version etc.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsJspTagInfo extends javax.servlet.jsp.tagext.TagSupport {
    
	private String m_property = null;

	public void setProperty(String name) {
		if (name != null) {
			m_property = name.toLowerCase();
		}
	}

	public String getProperty() {
		return m_property != null ? m_property : "";
	}

	public void release() {
		super.release();
		m_property = null;
	}

	private static final String[] m_systemProperties =
		{
			"opencms.version",
            "opencms.url",
            "opencms.uri",
            "opencms.webapp",
            "opencms.webbasepath" };

	private static final java.util.List m_userProperty =
		java.util.Arrays.asList(m_systemProperties);

	public int doStartTag() throws javax.servlet.jsp.JspException {

		javax.servlet.ServletRequest req = pageContext.getRequest();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;

            try {       
                String result = infoTagAction(m_property, c_req);
                // Return value of selected property
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                System.err.println("Error in Jsp 'user' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }
    
	public static String infoTagAction(String property, CmsFlexRequest req) {   
             
        com.opencms.file.CmsObject cms = req.getCmsObject();

		if (property == null)
			property = m_systemProperties[0];

		String result = null;
		switch (m_userProperty.indexOf(property)) {
			case 0 : // opencms.version
				result = cms.version();
				break;
			case 1 : // opencms.url
				result = req.getRequestURL().toString();
				break;
			case 2 : // opencms.uri
				result = req.getRequestURI();
				break;
			case 3 : // opencms.webapp
				result = CmsBase.getWebAppName();
				break;
			case 4 : // opencms.webbasepath
				result = CmsBase.getWebBasePath();
				break;
			default :
				result =
					"+++ Invalid info property selected: " + property + " +++";
		}
        
        return result;
	}

}
