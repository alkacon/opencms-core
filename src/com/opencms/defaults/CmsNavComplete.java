/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsNavComplete.java,v $
 * Date   : $Date: 2000/03/29 15:22:41 $
 * Version: $Revision: 1.1 $
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

package com.opencms.defaults;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;

/**
 * This class builds the default Navigation.
 * 
 * @author Alexander Kandzior
 * @author Waruschan Babachan
 * @version $Revision: 1.1 $ $Date: 2000/03/29 15:22:41 $
 */
public class CmsNavComplete extends A_CmsNavBase {
	
	/** 
	 * gets the root path.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getRootFolder(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		return (((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath()+"/");
	}
	
	
	/** 
	 * gets the root path.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getCurrentFolder(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		return (cms.getRequestContext().currentFolder().getAbsolutePath());
	}
	
	
	/** 
	 * gets the root path.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getCurrentPage(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		return (cms.getRequestContext().getUri());
	}
	
	
	/** 
	 * gets the navigation.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNav(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
		String requestedUri = cms.getRequestContext().getUri();
		String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
		CmsXmlTemplateFile xmlDataBlock=(CmsXmlTemplateFile)doc;		
		StringBuffer result = new StringBuffer();
				
		if (((Hashtable)userObject).get("nav.folder") != null) {
			if (((Hashtable)userObject).get("nav.folder").equals("root")) {
				currentFolder=cms.rootFolder().getAbsolutePath();
			}
			if (((Hashtable)userObject).get("nav.folder").equals("current")) {
				currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
			}
		}
					
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
				
		int size = resources.size();
        int max = 0;
		
		String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        
        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<size; i++) {
            A_CmsResource currentResource = (A_CmsResource)resources.elementAt(i);
            String path = currentResource.getAbsolutePath();
            String pos = cms.readMetainformation(path, C_METAINFO_NAVPOS);
            String text = cms.readMetainformation(path, C_METAINFO_NAVTITLE);     
			// Only list folders in the nav bar if they are not deleted!
            if (currentResource.getState() != C_STATE_DELETED) { 
                // don't list the temporary folders in the nav bar!
                if (pos != null && text != null && (!"".equals(pos)) && (!"".equals(text))
                     && ((!currentResource.getName().startsWith(C_TEMP_PREFIX)) || path.equals(requestedUri))) {
                    navLink[max] = path;
                    navText[max] = text;
                    navPos[max] = new Float(pos).floatValue();
                    max++;
                }
            }
        }
		
		// Sort the navigation
		sortNav(max, navLink, navText, navPos);
        
        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output
        for(int i=0; i<max; i++) {
			xmlDataBlock.setData("navText", navText[i]);
			xmlDataBlock.setData("count", new Integer(i+1).toString());
			xmlDataBlock.setData("navLink", servletPath + navLink[i] );
			// Check if nav is current nav
			if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
				result.append(xmlDataBlock.getProcessedDataValue("navCurrent"));
			} else {
				result.append(xmlDataBlock.getProcessedDataValue("navEntry"));
			}
        }

		return result.toString().getBytes();
	}
}