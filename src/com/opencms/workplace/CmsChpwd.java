/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChpwd.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
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
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;

import java.util.Hashtable;

/**
 * Template class for displaying the chpwd screen of the OpenCms workplace.<p>
 *
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2004/02/13 13:41:44 $
 */

public class CmsChpwd extends CmsWorkplaceDefault {

	/**
	 * Gets the content of the chpwd template and processes the data input.<p>
     * 
	 * @param cms The CmsObject.
	 * @param templateFile The chpwd template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearray containing the processed data of the template.
	 * @throws CmsException if something goes wrong.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName,
	   Hashtable parameters, String templateSelector) 
    throws CmsException {
		// the template to be displayed
		String template = null;
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
		String oldpwd = (String)parameters.get(C_PARA_OLDPWD);
		String newpwd = (String)parameters.get(C_PARA_NEWPWD);
		String newpwdrepeat = (String)parameters.get(C_PARA_NEWPWDREPEAT);
		// a password was given in the request so try to change it
		if(oldpwd != null && newpwd != null && newpwdrepeat != null) {
			if("".equals(oldpwd) || "".equals(newpwd) || "".equals(newpwdrepeat)) {
				xmlTemplateDocument.setData("details", "All fields must be filled.");
				template = "error";
			} else {
				// check if the new password and its repetition are identical
				if(newpwd.equals(newpwdrepeat)) {
					// change the password
					try {
						CmsRequestContext requestContext = cms.getRequestContext();
						cms.setPassword(requestContext.currentUser().getName(),
						oldpwd, newpwd);
						// return to the parameter dialog
						try {
							requestContext.getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + C_WP_EXPLORER_PREFERENCES);
						}
						catch(Exception e) {
							throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
							+ C_WP_EXPLORER_PREFERENCES, CmsException.C_UNKNOWN_EXCEPTION, e);
						}

						// an error was thrown while setting the new password
					}
					catch(CmsException exp) {
						// check if the old password was not correct
						if(exp.getType() == CmsException.C_NO_USER) {
							xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(exp));
							template = "error2";
						}
						else {
							if (exp.getType() == 1){
								xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(exp));
								template = "error2";
							} else {
								throw exp;
                            }
						}
					}
				}
				else {
					// the new passwords do not match
					xmlTemplateDocument.setData("details", "The new passwords do not match.");
					template = "error";
				}
			}
		}

		// process the selected template
		return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
	}

    /**
     * Indicates if the results of this class are cacheable,
     * which is not the case for this class.<p>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code>
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
        Hashtable parameters, String templateSelector) {
        return false;
    }
}
