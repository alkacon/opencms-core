/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsLockChange.java,v $
* Date   : $Date: 2001/06/29 13:44:06 $
* Version: $Revision: 1.29 $
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
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the lockchange screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.29 $ $Date: 2001/06/29 13:44:06 $
 */

public class CmsLockChange extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants{

    /**
     * method to check get the real body path from the content file
     *
     * @param cms The CmsObject, to access the XML read file.
     * @param file File in which the body path is stored.
     */

    private String getBodyPath(CmsObject cms, CmsFile file) throws CmsException {
        file = cms.readFile(file.getAbsolutePath());
        CmsXmlControlFile hXml = new CmsXmlControlFile(cms, file);
        return hXml.getElementTemplate("body");
    }

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the lockchange template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lockchange template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String lock = (String)parameters.get(C_PARA_LOCK);
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }

        //check if the lock parameter was included in the request
        // if not, the lock page is shown for the first time
        filename = (String)session.getValue(C_PARA_FILE);
        CmsResource file = (CmsResource)cms.readFileHeader(filename);

        // select the template to be displayed
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
        }
        if(lock == null && checkJavaProperty("opencms.dialog", "hide")) {
            lock = "true";
        }
        if(lock != null) {
            if(lock.equals("true")) {
                cms.lockResource(filename, true);
                session.removeValue(C_PARA_FILE);
            }

            // TODO: ErrorHandling

            // return to filelist
            try {
                if(lasturl == null || "".equals(lasturl)) {
                    cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                            + C_WP_EXPLORER_FILELIST);
                }
                else {
                    cms.getRequestContext().getResponse().sendRedirect(lasturl);
                }
            }
            catch(Exception e) {
                throw new CmsException("Redirect fails :"
                        + getConfigFile(cms).getWorkplaceActionPath()
                        + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
            }
            return null;
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,
                templateFile);
        xmlTemplateDocument.setData("FILENAME", file.getName());

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
}

