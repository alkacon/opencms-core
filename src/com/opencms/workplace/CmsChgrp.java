/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChgrp.java,v $
 * Date   : $Date: 2003/07/30 16:25:42 $
 * Version: $Revision: 1.38 $
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
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the chgrp screen of the OpenCms workplace.<p>
 *
 * @author Michael Emmerich
 * @version $Revision: 1.38 $ $Date: 2003/07/30 16:25:42 $
 */
public class CmsChgrp extends CmsWorkplaceDefault implements I_CmsWpConstants {

	/**
	 * Gets the content of the chgrp template and processes the data input.<p>
     * 
	 * @param cms The CmsObject.
	 * @param templateFile The chgrp template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearray containing the processed data of the template.
	 * @throws CmsException if something goes wrong.
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
		String newgroup = (String)parameters.get(C_PARA_NEWGROUP);
		String filename = (String)parameters.get(C_PARA_RESOURCE);
		String flags = (String)parameters.get(C_PARA_FLAGS);
		if(flags == null) flags = "false";
		if(filename != null) session.putValue(C_PARA_RESOURCE, filename);

		// check if the lock parameter was included in the request
		// if not, the lock page is shown for the first time
		filename = (String)session.getValue(C_PARA_RESOURCE);
		CmsResource file = cms.readFileHeader(filename);

		// select the template to be displayed
		if(file.isFile()) {
			template = "file";
        } else {
			template = "folder";
        }

		// a new owner was given in the request so try to change it
		if(newgroup != null) {
            
			// check if the current user has the right to change the group of the
			// resource. Only the owner of a file and the admin are allowed to do this.
			CmsRequestContext requestContext = cms.getRequestContext();
			if((requestContext.currentUser().equals(cms.readOwner(file)))
			|| (cms.userInGroup(requestContext.currentUser().getName(),
			C_GROUP_ADMIN))) {

				// if the resource is a folder, check if there is a corresponding
				// boolean rekursive = (file.isFolder() && flags.equals("true"));
				// cms.chgrp(cms.readAbsolutePath(file), newgroup, rekursive);

				session.removeValue(C_PARA_RESOURCE);

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
			} else {
				// the current user is not allowed to change the file owner
				xmlTemplateDocument.setData("details", "the current user is not allowed to change the file owner");
				xmlTemplateDocument.setData("lasturl", lasturl);
				template = "error";
				session.removeValue(C_PARA_RESOURCE);
			}
		}

		// set the required datablocks
		String title = cms.readProperty(cms.readAbsolutePath(file),C_PROPERTY_TITLE);
		if(title == null) title = "";
		CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
//		TODO fix this later
		//CmsUser owner = cms.readOwner(file);
        xmlTemplateDocument.setData("TITLE", Encoder.escapeXml(title));
		xmlTemplateDocument.setData("STATE", getState(cms, file, lang));
		xmlTemplateDocument.setData("OWNER", "" /* Utils.getFullName(owner) */);
		xmlTemplateDocument.setData("GROUP", "" /* cms.readGroup(file).getName() */);
		xmlTemplateDocument.setData("FILENAME", file.getResourceName());

		// process the selected template
		return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
	}

	/**
	 * Gets all groups that can be new group of the resource.<p>
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
	public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
	   Vector values, Hashtable parameters) 
    throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();
		int retValue = -1;
		I_CmsSession session = cms.getRequestContext().getSession(true);
		String filename = (String)session.getValue(C_PARA_RESOURCE);
		if(filename != null) {
			CmsResource file = cms.readFileHeader(filename);
			String group = cms.readGroup(file).getName();
			// fill the names and values
			for(int z = 0;z < groups.size();z++) {
				String name = ((CmsGroup)groups.elementAt(z)).getName();
				if(group.equals(name)) {
					retValue = z;
                }
				names.addElement(name);
				values.addElement(name);
			}
		}
		// no current user, set index to -1
		return new Integer(retValue);
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
		//if(file.inProject(cms.getRequestContext().currentProject())) {
        if (cms.isInsideCurrentProject(file)) {
			int state = file.getState();
			return lang.getLanguageValue("explorer.state" + state);
		}
		return lang.getLanguageValue("explorer.statenip");
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
