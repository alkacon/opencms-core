/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsTouch.java,v $
 * Date   : $Date: 2003/07/04 13:55:05 $
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

import com.opencms.core.CmsException;
import com.opencms.flex.jsp.CmsJspActionElement;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for the touch resource(s) dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/touch_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsTouch extends CmsDialog {
    
    /** Parameter name for the "change subfolders/files" checkbox */
    public static final String C_PARAM_RECURSIVE = "recursive";
    
    /** Parameter name for the new timestamp input field */
    public static final String C_PARAM_TS = "newtimestamp";
    
    /** Parameter name for the hidden field timestamp milliseconds */
    public static final String C_PARAM_TS_MILLIS = "newtimestampmillis";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsTouch(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // the file/folder which is copied
        String fileUri = (String)request.getParameter("file");
        if (fileUri != null) {
            settings.setFileUri(fileUri);
        }
    }
        
    /**
     * Performs the resource touching.<p>
     * 
     * @param request the http servlet request
     * 
     * @throws CmsException if touching is not successful
     * @throws IOException if redirecting is not successful
     */
    public void actionTouch(HttpServletRequest request) throws CmsException, IOException {
        String filename = getSettings().getFileUri();

        // get the new timestamp for the resource(s) from request parameter
        long timeStamp;
        try {
            timeStamp = Long.parseLong((String)request.getParameter(C_PARAM_TS_MILLIS));
        } catch (Exception e) {
            timeStamp = -1;
        }
        
        // get the flag if the touch is recursive from request parameter
        String touchRecString = (String)request.getParameter(C_PARAM_RECURSIVE);
        boolean touchRecursive = "true".equalsIgnoreCase(touchRecString);     
  
        // now touch the resource(s)
        if (timeStamp != -1) {
            getCms().touch(filename, timeStamp, touchRecursive);      
        }
    }   

}
