/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsBackofficeHead.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.18 $
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

import com.opencms.core.CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the head frame of the generic backoffice input forms
 *
 * Creation date: (17.10.2001)
 * @author Michael Emmerich
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsBackofficeHead extends CmsWorkplaceDefault {

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsSession session = (CmsSession)CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Vector selector = (Vector)session.getValue("backofficeselectortransfer");
        if(selector == null || selector.size() < 2){
            //set the proccessTag = ""
            xmlTemplateDocument.setData("BOSELECTOR","");
            //remove the unused values from the session
            session.removeValue("backofficeselectortransfer");
            session.removeValue("backofficeselectedtransfer");
        }

        //care about the previewbutton
        String prevLink = (String)session.getValue("weShallDisplayThePreviewButton");
        if(prevLink != null && !prevLink.equals("") ){
            //we got a link so we display it
            xmlTemplateDocument.setData("link", prevLink);
            xmlTemplateDocument.setData("prevbut", xmlTemplateDocument.getProcessedDataValue("preview", this, parameters));
            session.removeValue("weShallDisplayThePreviewButton");
        }else{
            xmlTemplateDocument.setData("prevbut","");// xmlTemplateDocument.getDataValue("nopreview"));
        }


        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }


 /**
 * This method creates the selectbox with all avaiable Pages to select from.
 */
  public Integer getSelectedPage(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
                           Hashtable parameters) throws CmsException {
    // get the session
    CmsSession session = (CmsSession) CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
    // get all aviable template selectors
    Vector selector=(Vector)session.getValue("backofficeselectortransfer");

    // get the actual template selector
    Integer retValue =(Integer)session.getValue("backofficeselectedtransfer");
    // copy the data into the value and name vectors
    if(selector != null){
        for (int i = 0; i < selector.size(); i++) {
          String sel = (String) selector.elementAt(i);
          names.addElement(sel);
          values.addElement(sel);
        }
    }

    session.removeValue("backofficeselectortransfer");
    session.removeValue("backofficeselectedtransfer");
    if(retValue == null){
        retValue = new Integer(0);
    }
    return retValue;
  }

}