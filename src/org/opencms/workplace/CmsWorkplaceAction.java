/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceAction.java,v $
 * Date   : $Date: 2003/06/12 16:32:26 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.workplace;

import com.opencms.file.CmsObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility class with static methods, mainly intended for the transition of
 * functionality from the old XML based workplace to the new JSP workplace.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceAction { 
    
    /** Path to the XML workplace */    
    public static final String C_XML_WORKPLACE_URI = "/system/workplace/action/index.html";
    
    /** Path to the JSP workplace */    
    public static final String C_JSP_WORKPLACE_URI = "/system/workplace/jsp/top.html";
    
    /**
     * Constructor is private since there must be no intances of this class.<p>
     */
    private CmsWorkplaceAction() {
    }
    
    /**
     * Updates the user preferences after changes have been made.<p>
     * 
     * @param cms the current cms context
     */
    public static void updatePreferences(CmsObject cms) {
        HttpSession session = extractSession(cms);
        if (session == null) return;
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) return;
        settings = CmsWorkplace.initWorkplaceSettings(cms, settings);
        // CmsWorkplace.storeSettings(session, settings);        
    }
    
    /**
     * Returns the uri of the top workplace frameset.<p>
     * 
     * @param cms the current cms context
     * @return the uri of the top workplace frameset
     */    
    public static String getWorkplaceUri(CmsObject cms) {              
        HttpSession session = extractSession(cms);
        if (session == null) return C_XML_WORKPLACE_URI;
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_SETTINGS);
        if (settings == null) return C_XML_WORKPLACE_URI;
        return C_JSP_WORKPLACE_URI;        
    }    
    
    /**
     * Extracts the HttpSession from the provided OpenCms context 
     * 
     * @param cms the current cms context
     * @return the HttpSession from the provided context, or null if no session could be extracted 
     */
    private static HttpSession extractSession(CmsObject cms) {
        HttpSession session = null;
        try {
            HttpServletRequest request = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
            session = request.getSession(false);
        } catch (Exception e) {
            // original request could not be extracted, use null return value 
        }
        return session;
    }

}
