/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChown.java,v $
 * Date   : $Date: 2003/07/11 14:01:12 $
 * Version: $Revision: 1.35 $
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

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.util.Encoder;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the chown screen of the OpenCms workplace.<P>
 *
 * @author Michael Emmerich
 * @version $Revision: 1.35 $ $Date: 2003/07/11 14:01:12 $
 */
public class CmsChown extends CmsWorkplaceDefault implements I_CmsWpConstants {
    
	/**
	 * Overwrites the getContent method of the CmsWorkplaceDefault.<p>
     * 
	 * Gets the content of the chown template and processes the data input.<p>
     * 
	 * @param cms The CmsObject.
	 * @param templateFile The chown template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearre containgine the processed data of the template.
	 * @throws CmsException if something goes wrong.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName,
	Hashtable parameters, String templateSelector) throws CmsException {
		I_CmsSession session = cms.getRequestContext().getSession(true);

		// clear session values on first load
		String initial = (String)parameters.get(C_PARA_INITIAL);
		if(initial != null) {
			// remove all session values
			session.removeValue(C_PARA_FILE);
			session.removeValue("lasturl");
		}

		// get the lasturl parameter
		String lasturl = getLastUrl(cms, parameters);

		// the template to be displayed
		String template = null;
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
		String newowner = (String)parameters.get(C_PARA_NEWOWNER);
		String filename = (String)parameters.get(C_PARA_FILE);
		String flags = (String)parameters.get(C_PARA_FLAGS);
		if(flags == null) flags = "false";
		if(filename != null) session.putValue(C_PARA_FILE, filename);

		// check if the lock parameter was included in the request
		// if not, the lock page is shown for the first time
		filename = (String)session.getValue(C_PARA_FILE);
		CmsResource file = (CmsResource)cms.readFileHeader(filename);

		// select the template to be displayed
		if(file.isFile()) {
			template = "file";
        } else {
			template = "folder";
        }

		// a new owner was given in the request so try to change it
		if(newowner != null) {
			CmsRequestContext requestContext = cms.getRequestContext();
			// check if the current user has the right to change the owner of the
			// resource. Only the owner of a file and the admin are allowed to do this.
			if((requestContext.currentUser().equals(cms.readOwner(file)))
			|| (cms.userInGroup(requestContext.currentUser().getName(), C_GROUP_ADMIN))) {

				// boolean rekursive = false;
				// if the resource is a folder, check if there is a corresponding
				// if(file.isFolder() && flags.equals("true")) rekursive = true;
				// cms.chown(cms.readAbsolutePath(file), newowner, rekursive);
				session.removeValue(C_PARA_FILE);

				// return to filelist
				try {
					if(lasturl == null || "".equals(lasturl)) {
						requestContext.getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
						+ CmsWorkplaceAction.getExplorerFileUri(cms));
					} else {
						requestContext.getResponse().sendRedirect(lasturl);
                    }
				}
				catch(Exception e) {
					throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
					+ CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
				}
				return null;
			}
			else {

				// the current user is not allowed to change the file owner
				xmlTemplateDocument.setData("details", "the current user is not allowed to change the file owner");
				xmlTemplateDocument.setData("lasturl", lasturl);
				template = "error";
				session.removeValue(C_PARA_FILE);
			}
		}

		// set the required datablocks
		String title = cms.readProperty(cms.readAbsolutePath(file),
		C_PROPERTY_TITLE);
		if(title == null)
			title = "";
		CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
		// CmsUser owner = cms.readOwner(file);
        xmlTemplateDocument.setData("TITLE", Encoder.escapeXml(title));
		xmlTemplateDocument.setData("STATE", getState(cms, file, lang));
		xmlTemplateDocument.setData("OWNER", "" /* Utils.getFullName(owner) */);
		xmlTemplateDocument.setData("GROUP", "" /* cms.readGroup(file).getName() */);
		xmlTemplateDocument.setData("FILENAME", file.getName());

		// process the selected template
		return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
	}

	/**
	 * Gets a formatted file state string.<p>
     * 
	 * @param cms The CmsObject.
	 * @param file The CmsResource.
	 * @param lang The content definition language file.
	 * @return Formatted state string.
	 */
	private String getState(CmsObject cms, CmsResource file, CmsXmlLanguageFile lang)
	throws CmsException {
		if(file.inProject(cms.getRequestContext().currentProject())) {
			int state = file.getState();
			return lang.getLanguageValue("explorer.state" + state);
		}
		return lang.getLanguageValue("explorer.statenip");
	}
    
	/**
	 * Gets all users that can be new owner of the file.<p>
	 * 
	 * The given vectors <code>names</code> and <code>values</code> will
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 *
	 * @param cms CmsObject Object for accessing system resources.
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the current value in the vectors.
	 * @throws CmsException if an error occurred
	 */
	public Integer getUsers(CmsObject cms, CmsXmlLanguageFile lang, Vector names, 
        Vector values, Hashtable parameters) 
    throws CmsException {
		// get all groups
		Vector users = cms.getUsers();
		int retValue = -1;
		I_CmsSession session = cms.getRequestContext().getSession(true);
		String filename = (String)session.getValue(C_PARA_FILE);
		if(filename != null) {
			CmsResource file = (CmsResource)cms.readFileHeader(filename);
			String fileOwner = cms.readOwner(file).getName();

			// fill the names and values
			for(int z = 0;z < users.size();z++) {
				String name = ((CmsUser)users.elementAt(z)).getName();
				if(fileOwner.equals(name)) retValue = z;
				names.addElement(name);
				values.addElement(name);
			}
		}
		// no current user, set index to -1
		return new Integer(retValue);
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
