/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/jsp/Attic/CmsJspTagFileProperty.java,v $
* Date   : $Date: 2002/09/03 19:46:40 $
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

import com.opencms.flex.util.CmsPropertyLookup;

/**
 * This Tag provides access to the currently included files OpenCms properties.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsJspTagFileProperty extends javax.servlet.jsp.tagext.TagSupport implements I_CmsJspConstants {
    
    private String m_propertyName = null;
    
    private String m_propertyFile = null;
    
    private String m_defaultValue = null;
    
    public void setName(String name) {
        if (name != null) {
            m_propertyName = name;
        }
    }
    
    public String getName() {
        return m_propertyName!=null?m_propertyName:"";
    }

    public void setDefault(String def) {
        if (def != null) {
            m_defaultValue = def;
        }
    }
    
    public String getDefault() {
        return m_defaultValue!=null?m_defaultValue:"";
    }
    
    public void setFile(String file) {
        if (file != null) {
            m_propertyFile = file.toLowerCase();
        }
    }
    
    public String getFile() {
        return m_propertyFile!=null?m_propertyFile:"parent";
    }
    
    public void release() {
        super.release();
        m_propertyFile = null;
        m_propertyName = null;
    }    
    
    public int doStartTag() throws javax.servlet.jsp.JspException {
        
        javax.servlet.ServletRequest req = pageContext.getRequest();
        javax.servlet.ServletResponse res = pageContext.getResponse();
        
        // This will always be true if the page is called through OpenCms 
        if (req instanceof com.opencms.flex.cache.CmsFlexRequest) {

            com.opencms.flex.cache.CmsFlexRequest c_req = (com.opencms.flex.cache.CmsFlexRequest)req;

            try {       

                com.opencms.file.CmsObject cms = c_req.getCmsObject();

                javax.servlet.jsp.JspWriter out = pageContext.getOut();
                java.util.Hashtable props = null;

                String file = null;
                String prop = null;
                
                if ("parent".equals(getFile())) {                    
                    // Read properties of parent (i.e. top requested) file
                    prop = CmsPropertyLookup.lookupProperty(cms, cms.getRequestContext().getUri(), getName(), false);                  
                } else if ("this".equals(getFile())) {
                    // Read properties of this file
                    prop = CmsPropertyLookup.lookupProperty(cms, c_req.getCmsResource(), getName(), false);
                } else if ("search".equals(getFile())) {
                    // Try to find property on file and all parent folders
                    prop = CmsPropertyLookup.lookupProperty(cms, c_req.getCmsResource(), getName(), true);
                } else {
                    // Read properties of the file named in the attribute
                    prop = CmsPropertyLookup.lookupProperty(cms, c_req.toAbsolute(getFile()), getName(), false);                  
                }

                if ((m_defaultValue != null) && (prop == null)) {
                    prop = m_defaultValue;
                }
                out.print(prop);
                
            } catch (Exception ex) {
                System.err.println("Error in Jsp 'property' tag processing: " + ex);
                System.err.println(com.opencms.util.Utils.getStackTrace(ex));
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

}
