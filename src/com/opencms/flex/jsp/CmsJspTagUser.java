/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagUser.java,v $
* Date   : $Date: 2002/08/21 11:29:32 $
* Version: $Revision: 1.2 $
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

/**
 * This Tag provides access to the data of the currently logged in user.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsJspTagUser extends javax.servlet.jsp.tagext.TagSupport implements I_CmsJspConstants {
    
    private String m_property = null;
    
    public void setProperty(String name) {
        if (name != null) {
            m_property = name.toLowerCase();
        }
    }
    
    public String getProperty() {
        return m_property!=null?m_property:"";
    }
    
    public void release() {
        super.release();
        m_property = null;
    }
    
    private static String[] m_userProperties = {"name", 
                                              "firstname", 
                                              "lastname", 
                                              "email", 
                                              "street", 
                                              "zip",
                                              "city",
                                              "description", 
                                              "group",
                                              "currentgroup", 
                                              "defaultgroup",
                                              "otherstuff"};
                                              
                                              private java.util.List m_userProperty = java.util.Arrays.asList(m_userProperties);
    
                                              public int doStartTag() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        javax.servlet.ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;

            try {       

                com.opencms.file.CmsObject cms = c_req.getCmsObject();
                com.opencms.file.CmsUser user = cms.getRequestContext().currentUser();
                com.opencms.file.CmsGroup group = cms.getRequestContext().currentGroup();

                javax.servlet.jsp.JspWriter out = pageContext.getOut();

                if (m_property == null) m_property = m_userProperties[0];
                
                String result = null;
                switch (m_userProperty.indexOf(m_property)) {
                    case 0: // name
                        result = user.getName();
                        break;
                    case 1: // firstname
                        result = user.getFirstname();
                        break;
                    case 2: // lastname
                        result = user.getLastname();
                        break;                        
                    case 3: // email
                        result = user.getEmail();
                        break;
                    case 4: // street
                        result = user.getAddress();
                        break;
                    case 5: // zip
                        result = (String)user.getAdditionalInfo(com.opencms.core.I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE);                        
                        break;
                    case 6: // city
                        result = (String)user.getAdditionalInfo(com.opencms.core.I_CmsConstants.C_ADDITIONAL_INFO_TOWN);
                        break;
                    case 7: // description
                        result = user.getDescription();
                        break;
                    case 8: // group
                    case 9: // currentgroup
                        result = group.getName();
                        break;
                    case 10: // defaultgroup
                        result = user.getDefaultGroup().getName();
                        break;
                    case 11: // otherstuff
                        java.util.Enumeration e = user.getAdditionalInfo().keys();
                        result = "";
                        while (e.hasMoreElements()) {
                            Object o = e.nextElement();
                            result += "AddInfo " + o + "=" + user.getAdditionalInfo((String)o);
                        }
                        result += " Section=" + user.getSection();
                        break;
                    default:
                        result = "+++ Invalid property selected: " + m_property + " +++";    
                }
                    
                // Return value of selected property
                out.print(result);
            } catch (Exception ex) {
                System.err.println("Error in Jsp 'user' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

}
