/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagInfo.java,v $
 * Date   : $Date: 2002/12/04 14:45:11 $
 * Version: $Revision: 1.3 $
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
import com.opencms.core.A_OpenCms;
import com.opencms.flex.cache.CmsFlexRequest;

/**
 * Provides access to some system information like OpenCms version,
 * JDK version etc.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsJspTagInfo extends javax.servlet.jsp.tagext.TagSupport {
    
    // member variables    
	private String m_property = null;

    /**
     * Sets the info property name.
     * 
     * @param name the info property name to set
     */
	public void setProperty(String name) {
		if (name != null) {
			m_property = name.toLowerCase();
		}
	}

    /**
     * Returns the selected info property.
     * 
     * @return the selected info property 
     */
	public String getProperty() {
		return m_property != null ? m_property : "";
	}

    /**
     * Releases the tag resources.
     */
	public void release() {
		super.release();
		m_property = null;
	}

    /** Static array with allowed info property values */
	private static final String[] m_systemProperties =
		{
			"opencms.version", // 0
            "opencms.url", // 1
            "opencms.uri", // 2
            "opencms.webapp", // 3
            "opencms.webbasepath", // 4
            "java.vm.name", // 5 
            "java.vm.version", // 6
            "java.vm.info", // 7
            "java.vm.vendor", // 8
            "os.name",  // 9
            "os.version", // 10
            "os.arch", // 11  
            "opencms.request.uri", // 12
            "opencms.request.element.uri", // 13
            "opencms.request.folder" // 14           
            };

    /** array list of allowed property values for more convenient lookup */
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

    /**
     * Returns the selected info property value based on the provided 
     * parameters.
     * 
     * @param property the info property to look up
     * @param req the currents request
     * @return the looked up property value 
     */    
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
            case 5: // system properties
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                result = System.getProperty(property);
                break;
            case 12: // opencms.request.uri
                result = req.getCmsRequestedResource();
                break;   
            case 13: // opencms.request.element.uri
                result = req.getCmsResource();
                break;                               
            case 14: // opencms.request.folder
                result = com.opencms.file.CmsResource.getParent(req.getCmsRequestedResource());
                break;            
            default :
				result =
					"+++ Invalid info property selected: " + property + " +++";
		}
        
        return result;
	}

}
