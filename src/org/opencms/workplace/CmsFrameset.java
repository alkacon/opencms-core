/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsFrameset.java,v $
 * Date   : $Date: 2003/06/06 16:47:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1
 */
public class CmsFrameset extends CmsWorkplace {
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFrameset(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Builds the Javascript for the Workplace context menus.<p>
     * 
     * @return the Javascript for the Workplace context menus
     */
    public String buildContextMenues() {
        StringBuffer result = new StringBuffer();
        Vector resTypes;
        try {
            resTypes = getCms().getFilesInFolder("/system/workplace/restypes/");
        } catch (CmsException e) {
            resTypes = new Vector();
        }
        for(int i = 0; i < resTypes.size(); i++) {
            CmsFile resourceTyp = (CmsFile)resTypes.elementAt(i);
            try {
                int resId = getCms().getResourceType(resourceTyp.getName()).getResourceType();
                result.append(getResourceEntry(new String(getCms().readFile(resourceTyp.getAbsolutePath()).getContents()), resId));
            } catch(CmsException e) {
            }
        }  
        return result.toString();      
    }
    
    /**
     * Returns the file settings for the Workplace explorer view.<p>
     * 
     * @return the file settings for the Workplace explorer view
     */
    public int getExplorerSettings() {
        String explorerSettings = (String)getCms().getRequestContext().currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS);
        if(explorerSettings != null) {
            return new Integer(explorerSettings).intValue();
        } else {
            return I_CmsWpConstants.C_FILELIST_NAME + I_CmsWpConstants.C_FILELIST_TITLE + I_CmsWpConstants.C_FILELIST_TYPE + I_CmsWpConstants.C_FILELIST_CHANGED;
        }
    }
        
    /**
     * Builds the Javascript for the contents of a resource-type file.<p> 
     * 
     * @param data the content of the file
     * @param id the resource id of the file type
     * @return the Javascript for the contents of a resource-type file
     */
    private String getResourceEntry(String data, int id) {
        StringBuffer result = new StringBuffer();
        String resId = Integer.toString(id);

        // first replace "resource_id" with the id
        int index = 0;
        String myToken = "resource_id";
        int foundAt = data.indexOf(myToken, index);
        while(foundAt != -1) {
            result.append(data.substring(index, foundAt) + resId);
            index = foundAt + myToken.length();
            foundAt = data.indexOf(myToken, index);
        }
        result.append(data.substring(index));

        // now set in the language values
        data = result.toString();
        result = new StringBuffer();
        myToken = "language_key";
        index = 0;
        foundAt = data.indexOf(myToken, index);
        while(foundAt != -1) {
            int endIndex = data.indexOf(")", foundAt);
            String langKey = data.substring(data.indexOf("(", foundAt) + 1, endIndex);
            langKey = key(langKey);
            result.append(data.substring(index, foundAt) + langKey);
            index = endIndex + 1;
            foundAt = data.indexOf(myToken, index);
        }
        result.append(data.substring(index));

        // at last we have to remove the spaces in the rules parameter
        data = result.toString();
        result = new StringBuffer();
        myToken = "rules_key";
        index = 0;
        foundAt = data.indexOf(myToken, index);
        while(foundAt != -1) {
            int endIndex = data.indexOf(")", foundAt);
            String rulesKey = data.substring(data.indexOf("(", foundAt) + 1, endIndex).trim();
            int nextSpace = rulesKey.indexOf(" ");
            while(nextSpace > -1){
                rulesKey = rulesKey.substring(0, nextSpace)+rulesKey.substring(nextSpace+1);
                nextSpace = rulesKey.indexOf(" ");
            }
            result.append(data.substring(index, foundAt) + rulesKey);
            index = endIndex + 1;
            foundAt = data.indexOf(myToken, index);
        }
        result.append(data.substring(index));

        return result.toString();
    }
    
    /**
     * Returns a html select box filled with the current users accessible projects.<p>
     * 
     * @return a html select box filled with the current users accessible projects
     */
    public String getProjectSelect() {
        // Get all project information
        CmsRequestContext reqCont = getCms().getRequestContext();
        Vector allProjects;
        try {
            allProjects = getCms().getAllAccessibleProjects();
        } catch (CmsException e) {
            // should not happen
            allProjects = new Vector();
        }
        int currentProjectId = reqCont.currentProject().getId();

        // Now loop through all projects and fill the result vectors
        int numProjects = allProjects.size();
        int currentProjectNum = 0;
        int currentLength = 0;
        
        int maxPrjNameLen = 0;
        int selectedPrjIndex = 0;
        List projectIds = new ArrayList();
        List projectNames = new ArrayList();
        
        for (int i = 0; i < numProjects; i++) {
            CmsProject loopProject = (CmsProject) allProjects.elementAt(i);
            String loopProjectName = loopProject.getName();
            String loopProjectId = loopProject.getId() + "";
            
            projectIds.add(loopProjectId);
            projectNames.add(loopProjectName);
            
            if (loopProject.getId() == currentProjectId) {
                // Fine. The project of this loop is the user's current project. Save it!
                currentProjectNum = i;
            }
            
            currentLength = loopProjectName.length();
            if (currentLength>maxPrjNameLen) {
                maxPrjNameLen = currentLength;
            }
        }        
        selectedPrjIndex = currentProjectNum;
        
        if (maxPrjNameLen <= 20) {
            return buildSelect("class=\"textfeld\" name=\"wpProject\" style=\"width:150px\" onchange=\"document.forms.wpProjectSelect.submit()\"", projectNames, projectIds, selectedPrjIndex);
        } else {
            return buildSelect("class=\"textfeld\" name=\"wpProject\" onchange=\"document.forms.wpProjectSelect.submit()\"", projectNames, projectIds, selectedPrjIndex);
        }
    }    
    
    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     * 
     * @return a html select box filled with the views accessible by the current user
     */    
    public String getViewSelect() {

        CmsRequestContext reqCont = getCms().getRequestContext();   
        // let's check what view is selected in the session
        String currentView = (String)getSession().getAttribute(I_CmsWpConstants.C_PARA_VIEW);
        
        if (currentView == null) {
            // check out the user infor1ation if a default view is stored there
            Hashtable startSettings = (Hashtable) reqCont.currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);
            if (startSettings != null) {
                currentView = (String)startSettings.get(I_CmsConstants.C_START_VIEW);
            }
        }
        
        System.err.println("currentView: " + currentView);

        Vector viewNames = new Vector();
        Vector viewLinks = new Vector();
        
        List options = new ArrayList();
        List values = new ArrayList();
        int currentViewIndex = -1;
        int numViews = 0;

        // get the List of available views from the Registry
        try {
            numViews = (getCms().getRegistry()).getViews(viewNames, viewLinks);
        } catch (CmsException e) {
            // should not happen
        }

        // loop through the vectors and fill the result vectors
        for (int i = 0; i<numViews; i++) {
            String loopName = (String)viewNames.elementAt(i);
            String loopLink = (String)viewLinks.elementAt(i);
            
            boolean visible = true;
            try {
                getCms().readFileHeader(loopLink);
            } catch (CmsException e) {
                visible = false;
            }
            if (visible) {
                loopLink = getJsp().link(loopLink);
                if (loopLink.equals(currentView)) {
                    currentViewIndex = values.size();
                }
                options.add(key(loopName));
                values.add(loopLink);
            }
        }        
        return buildSelect("class=\"textfeld\" name=\"wpView\" style=\"width:150px\" onchange=\"document.forms.wpViewSelect.submit()\"", options, values, currentViewIndex);        
    }
    
    /**
     * Returns a html select box filled with groups of the current user.<p>
     * 
     * @return a html select box filled with groups of the current user
     */    
    public String getGroupSelect() {    

        // Get a vector of all of the user's groups by asking the request context
        CmsRequestContext reqCont = getCms().getRequestContext();
        CmsGroup currentGroup = reqCont.currentGroup();
        Vector allGroups = new Vector();
        try {
            allGroups = getCms().getGroupsOfUser(reqCont.currentUser().getName());
        } catch (CmsException e) {
            // should not happen
        }

        // loop through all groups and build the result vectors
        int numGroups = allGroups.size();
        int currentGroupNum = 0;
        
        List options = new ArrayList();
        List values = new ArrayList();        
        
        for (int i = 0; i<numGroups; i++) {
            CmsGroup loopGroup = (CmsGroup) allGroups.elementAt(i);
            String loopGroupName = loopGroup.getName();
            values.add(loopGroupName);
            options.add(loopGroupName);
            if (loopGroup.equals(currentGroup)) {
                currentGroupNum = i;
            }
        }
        
        return buildSelect("class=\"textfeld\" name=\"wpGroup\" style=\"width:150px\" onchange=\"document.forms.wpGroupSelect.submit()\"", options, values, currentGroupNum);                             
    }
}
