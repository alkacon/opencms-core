/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagUser.java,v $
 * Date   : $Date: 2004/12/15 12:29:46 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.flex.CmsFlexController;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import java.util.Iterator;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Provides access to the data of the currently logged in user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 */
public class CmsJspTagUser extends TagSupport {
    
    // internal member variables
    private String m_property;
    
    /** Static array of the possible user properties. */
    private static final String[] m_userProperties =
        {
            "name",
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
            "otherstuff" };

    /** Array list for fast lookup. */
    private static final java.util.List m_userProperty =
        java.util.Arrays.asList(m_userProperties);
        
    /**
     * Sets the property name.<p>
     * 
     * @param name the property name
     */
    public void setProperty(String name) {
        if (name != null) {
            m_property = name.toLowerCase();
        }
    }

    /**
     * Returns the property name.<p>
     * 
     * @return String the property name
     */
    public String getProperty() {
        return m_property != null ? m_property : "";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        m_property = null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws javax.servlet.jsp.JspException {

        javax.servlet.ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {

            try {
                String result = userTagAction(m_property, req);
                // Return value of selected property
                pageContext.getOut().print(result);
            } catch (Exception ex) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error in Jsp 'user' tag processing", ex);
                }                    
                throw new javax.servlet.jsp.JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * Internal action method.<p>
     * 
     * @param property the selected user property
     * @param req the current request
     * @return String the value of the selected user property
     */
    public static String userTagAction(String property, ServletRequest req) {

        CmsFlexController controller = (CmsFlexController)req.getAttribute(CmsFlexController.ATTRIBUTE_NAME);

        CmsObject cms = controller.getCmsObject();
        CmsUser user = cms.getRequestContext().currentUser();

        if (property == null) {
            property = m_userProperties[0];
        }

        String result = null;
        switch (m_userProperty.indexOf(property)) {
            case 0 : // name
                result = user.getName();
                break;
            case 1 : // firstname
                result = user.getFirstname();
                break;
            case 2 : // lastname
                result = user.getLastname();
                break;
            case 3 : // email
                result = user.getEmail();
                break;
            case 4 : // street
                result = user.getAddress();
                break;
            case 5 : // zip
                result = (String)user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE);
                break;
            case 6 : // city
                result = (String)user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TOWN);
                break;
            case 7 : // description
                result = user.getDescription();
                break;
            case 8 : // group
            case 9 : // currentgroup
            case 10 : // defaultgroup
                result = "";
                break;
            case 11 : // otherstuff
                Iterator it = user.getAdditionalInfo().keySet().iterator();
                result = "AdditionalInfo:";
                while (it.hasNext()) {
                    Object o = it.next();
                    result += " " + o + "=" + user.getAdditionalInfo((String)o);
                }
                break;
            default :
                result = "+++ Invalid user property selected: " + property + " +++";
        }

        return result;
    }

}
