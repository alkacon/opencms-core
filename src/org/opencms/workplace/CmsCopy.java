/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsCopy.java,v $
 * Date   : $Date: 2003/07/04 07:25:16 $
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
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for the copy resources dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/copy_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsCopy extends CmsDialog {

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsCopy(CmsJspActionElement jsp) {
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
     * Performs the resource copying.<p>
     * 
     * @param request the http servlet request
     * @return true, if the resource was copied, otherwise false
     * 
     * @throws CmsException if copying is not successful
     */
    public boolean actionCopy(HttpServletRequest request) throws CmsException {
        String filename = getSettings().getFileUri();
        
        // read the file header
        CmsResource file = null;
        file = (CmsResource)getCms().readFileHeader(filename);          
             
        // read all request parameters
        String wholePath = (String)request.getParameter("folder");
        String flags = (String)request.getParameter("keeprights");
        String action = (String)request.getParameter("action");
        
        // the wholePath includes the folder and/or the filename
        String newFolder = getNewFolder(wholePath, file);
        String newFile = getNewFile(wholePath, file);
        
        // first delete the target resource when confirmed by the user
        if ("confirmoverwrite".equals(action)) {
            getCms().deleteResource(newFolder+newFile);
        }
            
        // copy the resource       
        getCms().copyResource(filename, newFolder+newFile, "true".equals(flags));
      
        return true;
    }
    
    private String getNewFolder(String wholePath, CmsResource file) {
        if(wholePath != null && !("".equals(wholePath))){
            if(wholePath.startsWith("/")){
                // get the foldername
                return wholePath.substring(0, wholePath.lastIndexOf("/")+1);
            
            } else {
                return file.getParent();
            }
        }
        return new String();
    }
    
    private String getNewFile(String wholePath, CmsResource file) {   
        String newFile = new String();
        if(wholePath != null && !("".equals(wholePath))){
            if(wholePath.startsWith("/")){
                // get the foldername
                newFile = wholePath.substring(wholePath.lastIndexOf("/")+1);
                if (newFile == null || "".equals(newFile)){
                    newFile = file.getName();
                }
            } else {
                newFile = wholePath;
            }
        }
        return newFile;
    }

}
