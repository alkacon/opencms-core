/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsLock.java,v $
* Date   : $Date: 2004/02/21 17:11:42 $
* Version: $Revision: 1.55 $
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

import org.opencms.main.CmsException;
import org.opencms.security.CmsSecurityException;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.I_CmsSession;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.Hashtable;

/**
 * Template class for displaying the lock screen of the OpenCms workplace.<p>
 * 
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @author Alexander Lucas
 * @version $Revision: 1.55 $ $Date: 2004/02/21 17:11:42 $
 */

public class CmsLock extends CmsWorkplaceDefault {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the lock template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // the template to be displayed
        String template = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String lock = (String)parameters.get(C_PARA_LOCK);
        String filename = (String)parameters.get(C_PARA_RESOURCE);
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }

        //check if the user wants the lock dialog
        // if yes, the lock page is shown for the first time
        filename = (String)session.getValue(C_PARA_RESOURCE);
        CmsResource file = cms.readFileHeader(filename);

        // select the template to be displayed
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
            if (! filename.endsWith("/")) {
                filename += "/";
            }
        }
        Hashtable startSettings = (Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
        String showLockDialog = "off";
        if(startSettings!=null){
            showLockDialog = (String)startSettings.get(C_START_LOCKDIALOG);
        }
        if(lock == null && !"on".equals(showLockDialog)) {
            lock = "true";
        }
        if(lock != null) {
            if(lock.equals("true")) {
                session.removeValue(C_PARA_RESOURCE);
                try{
                    cms.lockResource(filename);  
                } catch(CmsException e){
                    if(e instanceof CmsSecurityException) {
                        template = "erroraccessdenied";
                        xmlTemplateDocument.setData("details", file.getName());
                    }else {
                        xmlTemplateDocument.setData("details", e.toString());
                        template = "error";
                    }
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    xmlTemplateDocument.setData("FILENAME", file.getName());
                    // process the selected template
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
                }

                // return to filelist
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                    + CmsWorkplaceAction.getExplorerFileUri(cms.getRequestContext().getRequest().getOriginalRequest()));
                    }
                    else {
                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                    }
                }
                catch(Exception e) {
                        throw new CmsException("Redirect fails :"
                                + getConfigFile(cms).getWorkplaceActionPath()
                                + CmsWorkplaceAction.getExplorerFileUri(cms.getRequestContext().getRequest().getOriginalRequest()), CmsException.C_UNKNOWN_EXCEPTION, e);
                }
                return null;
            }
        }
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

