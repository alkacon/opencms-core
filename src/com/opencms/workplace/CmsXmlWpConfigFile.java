/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpConfigFile.java,v $
* Date   : $Date: 2004/03/12 16:00:48 $
* Version: $Revision: 1.59 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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


package com.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Vector;

/**
 * Definition of some paths for the workplace. Currently some are static,
 * but can be switched to read from configurations like registry.
 *
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @author Andreas Schouten
 * @version $Revision: 1.59 $ $Date: 2004/03/12 16:00:48 $
 */

public class CmsXmlWpConfigFile {

    /** The cms object to get access to OpenCms */
    private CmsObject m_cms = null;

    /**
     * Default constructor.
     */

    public CmsXmlWpConfigFile() throws CmsException {
    }

    /**
     * Constructor for creating a new config file object.
     * The parameter cms is not needed at the moment.
     *
     * @param cms CmsObject object for accessing system resources.
     */
    public CmsXmlWpConfigFile(CmsObject cms) throws CmsException {
        m_cms = cms;
    }

    /**
     * Gets the path at which the folders with the download galleries are
     * @return Path for download galleries.
     */
    public String getDownGalleryPath(){
        return I_CmsWpConstants.C_VFS_GALLERY_DOWNLOAD;
    }

    /**
     * Gets the path at which the folders with the html galleries are
     * @return Path for html galleries.
     */
    public String getHtmlGalleryPath(){
        return I_CmsWpConstants.C_VFS_GALLERY_HTML;
    }

    /**
     * Gets the path at which the folders with the picture galleries are
     * @return Path for picture galleries.
     */
    public String getPicGalleryPath(){
        return I_CmsWpConstants.C_VFS_GALLERY_PICS;
    }

    /**
     * Gets the path at which the folders with the externallink galleries are
     * @return Path for externallink galleries.
     */
    public String getLinkGalleryPath() {
        return I_CmsWpConstants.C_VFS_GALLERY_EXTERNALLINKS;
    }
    
    /**
     * Gets the path for OpenCms language files.
     * @return Path for language files.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     * @deprecated Use the constant value I_CmsWpConstants.C_VFS_PATH_LOCALES instead
     */
    public String getLanguagePath() throws CmsException {
        return I_CmsWpConstants.C_VFS_PATH_LOCALES;
    }

    /**
     * Gets the path for OpenCms workplace action files.
     * @return Path for OpenCms workplace action files.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceActionPath() throws CmsException {
        return I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/";
    }

    /**
     * Gets the path for OpenCms workplace administration files.
     * @return Path for OpenCms workplace administration files.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceAdministrationPath() throws CmsException {
        return I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "administration/";
    }
    
    /** Flag to indicate what setting the value of "UseWpPicturesFromVFS" in the registry.xml is */    
    private static boolean m_useWpPicturesFromVFS = true;
    /** Path in the VFS if "UseWpPicturesFromVFS" is true */
    private static String m_useWpPicturesFromVFSPath = null;
    /** URI of the resources, including application context and servlet name (if required) */
    private static String m_resourceUri = null;

    /**
     * Gets the path for system picture files.
     * @return Path for picture files.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWpPicturePath() throws CmsException {
        if (m_useWpPicturesFromVFSPath == null) {
            // Check registry for setting of workplace images
            m_useWpPicturesFromVFS = false;
            m_useWpPicturesFromVFSPath = CmsXmlTemplateLoader.getRequest(m_cms.getRequestContext()).getServletUrl() + I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS;
            if (m_useWpPicturesFromVFS) {
                m_resourceUri = m_useWpPicturesFromVFSPath;
            } else {
                m_resourceUri = CmsXmlTemplateLoader.getRequest(m_cms.getRequestContext()).getWebAppUrl() + I_CmsWpConstants.C_SYSTEM_PICS_EXPORT_PATH;
            }
        }
        return m_resourceUri;
    }

    public void getWorkplaceIniData(Vector names, Vector values, String tag, String element) throws CmsException {
        if(tag.equals("NEWRESOURCES")) {
            names.add("folder");    values.add("explorer_files_new_folder.html");
            names.add("page");      values.add("explorer_files_new_page.html");
            // names.add("pdfpage");   values.add("explorer_files_new_pdfpage.html");
            names.add("link");      values.add("explorer_files_new_link.html");
            names.add("othertype"); values.add("explorer_files_new_othertype.html");
            names.add("upload");    values.add("explorer_files_new_upload.html");
        } else {
            names.add("plain");     values.add("plain");
            names.add("jsp");       values.add("jsp"); 
            names.add("newpage");       values.add("newpage");
            names.add("XMLTemplate");   values.add("XMLTemplate");            
            names.add("binary");    values.add("binary");
            names.add("image");     values.add("image");
        }        
    }

}
