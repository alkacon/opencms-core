/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleTemplate.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/03/24 09:39:14 $
 * Version: $Revision: 1.2 $
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
 * This class is used to display the application form of mindfact and makes it
 * possible to send the application form as a mail.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.2 $ $Date: 2000/03/24 09:39:14 $
 * @see com.opencms.template.CmsXmlTemplate
 */
public class CmsExampleTemplate extends CmsXmlTemplate {
	
	
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
     * Gets the content of a defined section in a given template file and its 
     * subtemplates with the given parameters. 
     * 
	 * @see getContent(A_CmsObject cms, String templateFile, String elementName,   
	 * Hashtable parameters)
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * 
     * @return It returns an array of bytes that contains the page.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String 
                             elementName, Hashtable parameters, String 
                             templateSelector) throws CmsException {
		// CententDefinition		
		CmsXmlTemplateFile datablock=(CmsXmlTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		if (parameters.get("type")!=null) {
			if (parameters.get("type").equals("plain")) {
				return startProcessing(cms, datablock, elementName, parameters, parameters.get("type").toString());	
			} else {
				if (parameters.get("name")!=null) {
					return startProcessing(cms, datablock, elementName, parameters, parameters.get("name").toString());
				}
			}
		}
		return startProcessing(cms, datablock, elementName, parameters, null);
	}
	
	
	/** 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFramePage(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		StringBuffer result = new StringBuffer();
		String href=cms.getRequestContext().getUri();
		int start=href.indexOf("?");
		if (start==-1) {
			href=href.substring(href.lastIndexOf("/")+1);
		} else {
			String tmpstr=href.substring(0,start);
			href=href.substring(tmpstr.lastIndexOf("/")+1);
		}
		result.append(href);
		return result.toString().getBytes();
	}
	
}