/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleNavigation.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/03/27 13:19:17 $
 * Version: $Revision: 1.3 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Mindfact interaktive medien ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Mindfact.
 * In order to use this source code, you need written permission from Mindfact.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * MINDAFCT MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. MINDFACT SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.examples;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;
import java.io.*;

/**
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.3 $ $Date: 2000/03/27 13:19:17 $
 * @see com.opencms.template.CmsXmlTemplate
 */
public class CmsExampleNavigation extends CmsXmlTemplate implements I_CmsConstants {
	
	
	/**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }   
	
	
	/** 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNav(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
				
		CmsXmlTemplateFile xmlDataBlock=(CmsXmlTemplateFile)doc;		
		String frame="";
		if ((((Hashtable)userObject).get("cmsframe"))!=null) {
			frame=(((Hashtable)userObject).get("cmsframe")).toString();
		}
			
		String[] linkFile = {"neuigkeit1.html",
							 "neuigkeit2.html",
							 "index.html"
							};
		String[] linkText = { "Topthemen",
							  "Themen des Tages",
							  "Startseite"
							};
		
        StringBuffer result = new StringBuffer();

		for (int i=0;i<3;i++) {
			if (frame.equals("plain")) {
				xmlDataBlock.setData("linkTarget", xmlDataBlock.getDataValue("target.plain"));
			} else {
				xmlDataBlock.setData("linkTarget", xmlDataBlock.getDataValue("target.frame"));
			}
			xmlDataBlock.setData("linkUri", linkFile[i]);
			xmlDataBlock.setData("linkName", linkText[i]);
			result.append(xmlDataBlock.getProcessedDataValue("link") + "\n");

		}		
		return result.toString().getBytes();
	}
	
	/** 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getHead(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		CmsXmlTemplateFile xmlDataBlock=(CmsXmlTemplateFile)doc;
		String frame="";
		if ((((Hashtable)userObject).get("cmsframe"))!=null) {
			frame=(((Hashtable)userObject).get("cmsframe")).toString();
		}
		StringBuffer result = new StringBuffer();
		if (frame.equals("plain")) {
			result.append(xmlDataBlock.getDataValue("target.plain"));
			return result.toString().getBytes();
		}
        result.append(xmlDataBlock.getDataValue("target.frame"));
		return result.toString().getBytes();
	}
		
	/** 
     * @param cms A_CmsObject Object for accessing system resources.
     * @return String that contains Absolut path of internal files.
	 * @exception CmsException
     */
	public String getNavPath(A_CmsObject cms)
		throws CmsException {
		
		String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
		String path=servletPath+C_PATH_INTERNAL_TEMPLATES;
		
		return path;
	}
}