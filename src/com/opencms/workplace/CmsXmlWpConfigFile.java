
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpConfigFile.java,v $
* Date   : $Date: 2001/07/26 13:49:51 $
* Version: $Revision: 1.32 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Definition of some paths for the workplace. Currently some are static,
 * but can be switched to read from configurations like registry.
 *
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @author Andreas Schouten
 * @version $Revision: 1.32 $ $Date: 2001/07/26 13:49:51 $
 */

public class CmsXmlWpConfigFile {

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
    }

    /**
     * Gets the path at which the folders with the download galleries are
     * @return Path for download galleries.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getDownGalleryPath() throws CmsException {
        return "/download/";
    }

    /**
     * Gets the path at which the folders with the picture galleries are
     * @return Path for picture galleries.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getPicGalleryPath() throws CmsException {
        return "/pics/";
    }

    /**
     * Gets the path for OpenCms language files.
     * @return Path for language files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */

    public String getLanguagePath() throws CmsException {
        return "/system/workplace/config/language/";
    }

    /**
     * Gets the path for OpenCms workplace action files.
     * @return Path for OpenCms workplace action files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceActionPath() throws CmsException {
        return "/system/workplace/action/";
    }

    /**
     * Gets the path for OpenCms workplace administration files.
     * @return Path for OpenCms workplace administration files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceAdministrationPath() throws CmsException {
        return "/system/workplace/administration/";
    }

    /**
     * Gets the path for system picture files.
     * @return Path for picture files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWpPicturePath() throws CmsException {
        return "/pics/system/";
    }

    public void getWorkplaceIniData(Vector names, Vector values, String tag, String element) throws CmsException {
        if(tag.equals("NEWRESOURCES")) {
            names.add("folder");    values.add("explorer_files_new_folder.html");
            names.add("page");      values.add("explorer_files_new_page.html");
            names.add("pdfpage");   values.add("explorer_files_new_pdfpage.html");
            names.add("link");      values.add("explorer_files_new_link.html");
            names.add("othertype"); values.add("explorer_files_new_othertype.html");
            names.add("upload");    values.add("explorer_files_new_upload.html");
        } else {
            names.add("binary");    values.add("binary");
            names.add("plain");     values.add("plain");
        }
    }

}
