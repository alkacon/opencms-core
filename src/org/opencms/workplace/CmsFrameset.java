/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsFrameset.java,v $
 * Date   : $Date: 2003/07/22 00:29:22 $
 * Version: $Revision: 1.18 $
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

import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.LinkSubstitution;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/top.html
 * <li>/jsp/top_foot.html
 * <li>/jsp/top_head.html
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.18 $
 * 
 * @since 5.1
 */
public class CmsFrameset extends CmsWorkplace {
    
    private static Vector m_viewNames = null;
    private static Vector m_viewLinks = null;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFrameset(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // check if the user requested a group change
        String newGroup = request.getParameter("wpGroup");
        if (newGroup != null) {
            settings.setGroup(newGroup);
        }

        // check if the user requested a project change
        String newProject = request.getParameter("wpProject");
        if (newProject != null) {
            settings.setProject(Integer.parseInt(newProject));
        }                   

        // check if the user requested a view change
        String newView = request.getParameter("wpView");
        if (newView != null) {
            settings.setCurrentView(newView);
        }
        
        // check if the user requested a site change
        String newSite = request.getParameter("wpSite");
        if (newSite != null) {
            settings.setSite(newSite);
        }        
    } 
    
    /**
     * Builds the Javascript for the Workplace context menus.<p>
     * 
     * @return the Javascript for the Workplace context menus
     */
    public String buildContextMenues() {
        StringBuffer result = new StringBuffer();
        List resTypes = null;
        try {
            resTypes = getCms().getFilesInFolder("/system/workplace/restypes/");
        } catch (CmsException e) {
            resTypes = new Vector();
        }
        for (int i = 0; i < resTypes.size(); i++) {
            CmsFile resourceTyp = (CmsFile)resTypes.get(i);
            try {
                int resId = getCms().getResourceTypeId(resourceTyp.getResourceName());
                result.append(getResourceEntry(new String(getCms().readFile(getCms().readAbsolutePath(resourceTyp)).getContents()), resId));
            } catch (CmsException e) {
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
        if (explorerSettings != null) {
            return new Integer(explorerSettings).intValue();
        } else {
            return I_CmsWpConstants.C_FILELIST_NAME + I_CmsWpConstants.C_FILELIST_TITLE + I_CmsWpConstants.C_FILELIST_TYPE + I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED;
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
        
        StringBuffer result;
        int index = 0;
        String myToken;
        int foundAt;
        
        result = new StringBuffer(1024);
        
        // first append the current value of resource_id
        result.append("\nresource_id = ");
        result.append(id);
        result.append("\n");
        
        // set the language keys
        myToken = "language_key(";
        index = 0;
        foundAt = data.indexOf(myToken, index);
        while (foundAt != -1) {
            int endIndex = data.indexOf(")", foundAt);
            String langKey = data.substring(foundAt + 13, endIndex);
            langKey = key(langKey);
            result.append(data.substring(index, foundAt));
            result.append(langKey);
            index = endIndex + 1;
            foundAt = data.indexOf(myToken, index);
        }
        result.append(data.substring(index));
        data = result.toString();

        // remove the spaces in the rules parameter
        result = new StringBuffer(1024);
        myToken = "rules_key(";
        index = 0;
        foundAt = data.indexOf(myToken, index);
        while (foundAt != -1) {
            int endIndex = data.indexOf(")", foundAt);
            String rulesKey = data.substring(foundAt + 10, endIndex);
            result.append(data.substring(index, foundAt));
            int length = rulesKey.length();
            char c;
            for (int i=0; i<length; i++) {
                if ((c = rulesKey.charAt(i)) != ' ') {
                    result.append(c);
                }
            }
            index = endIndex + 1;
            foundAt = data.indexOf(myToken, index);
        }
        result.append(data.substring(index));
        
        String str = result.toString();        
        String jspWorkplaceUri = LinkSubstitution.getLinkSubstitution(getCms(), C_PATH_WORKPLACE);  
        String xmlWorkplaceUri = LinkSubstitution.getLinkSubstitution(getCms(), CmsWorkplaceAction.C_PATH_XML_WORKPLACE);          
        str = str.replaceAll("/JSPWORKPLACE/", jspWorkplaceUri);
        str = str.replaceAll("/XMLWORKPLACE/", xmlWorkplaceUri);
        
        return str;
    }
    
    /**
     * Returns a html select box filled with the current users accessible sites.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the current users accessible sites
     */
    public String getSiteSelect(String htmlAttributes) {

        List options = new ArrayList();
        List values = new ArrayList();    
        int selectedIndex = 0;                   

        List sites = CmsSiteManager.getAvailableSites(getCms());

        Iterator i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            values.add(site.getSiteRoot());
            options.add(site.getTitle());
            if (site.getSiteRoot().equals(getSettings().getSite())) { 
                // this is the user's current site
                selectedIndex = pos;
            }
            pos++;
        }
        
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }    
    
    /**
     * Returns a html select box filled with the current users accessible projects.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @param htmlWidth additional "width" html attributes
     * @return a html select box filled with the current users accessible projects
     */
    public String getProjectSelect(String htmlAttributes, String htmlWidth) {
        // get all project information
        Vector allProjects;
        try {
            allProjects = getCms().getAllAccessibleProjects();
        } catch (CmsException e) {
            // should not happen
            allProjects = new Vector();
        }

        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        int maxNameLength = 0;
        
        // now loop through all projects and fill the result vectors
        int numProjects = allProjects.size();
        for (int i = 0; i < numProjects; i++) {
            CmsProject loopProject = (CmsProject)allProjects.get(i);
            String loopProjectName = loopProject.getName();
            String loopProjectId = Integer.toString(loopProject.getId());
            
            values.add(loopProjectId);
            options.add(loopProjectName);
            
            if (loopProject.getId() == getSettings().getProject()) {
                // this is the user's current project
                selectedIndex = i;
            }            
            // check the length of the project name, to optionallly adjust the size of the selector
            maxNameLength = Math.max(loopProjectName.length(), maxNameLength);
        }        
        if (maxNameLength <= 20) {
            StringBuffer buf = new StringBuffer(htmlAttributes.length() + htmlWidth.length() + 5);
            buf.append(htmlAttributes);
            buf.append(" ");
            buf.append(htmlWidth);
            htmlAttributes = buf.toString();
        }
         
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }    
        
    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the views accessible by the current user
     */    
    public String getViewSelect(String htmlAttributes) {
        
        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        
        // get the Vector of available views from the Registry, but only once
        if (m_viewLinks == null) {
            synchronized (this) {
                try {
                    Vector viewNames = new Vector();
                    Vector viewLinks = new Vector();
                    getCms().getRegistry().getViews(viewNames, viewLinks);
                    m_viewNames = viewNames;
                    m_viewLinks = viewLinks;
                } catch (CmsException e) {
                    // should not happen
                }
            }
        }

        // loop through the vectors and fill the result vectors
        int numViews = m_viewNames.size();
        for (int i = 0; i<numViews; i++) {
            String loopName = (String)m_viewNames.get(i);
            String loopLink = (String)m_viewLinks.get(i);
            
            boolean visible = true;
            try {
                getCms().readFileHeader(loopLink);
            } catch (CmsException e) {
                visible = false;
            }
            if (visible) {
                loopLink = getJsp().link(loopLink);
                options.add(key(loopName));
                values.add(loopLink);

                if (loopLink.equals(getSettings().getCurrentView())) {
                    selectedIndex = i;
                }
            }
        }        
  
        return buildSelect(htmlAttributes, options, values, selectedIndex);        
    }
    
    /**
     * Returns a html select box filled with groups of the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with groups of the current user
     */    
    public String getGroupSelect(String htmlAttributes) {    

        // get the users groups from the request context
        Vector allGroups = new Vector();
        try {
            allGroups = getCms().getGroupsOfUser(getSettings().getUser().getName());
        } catch (CmsException e) {
            // should not happen
        }

        List options = new ArrayList();
        List values = new ArrayList();        
        int selectedIndex = 0;
        
        // loop through all groups and build the result vectors
        int numGroups = allGroups.size();
        for (int i = 0; i<numGroups; i++) {
            CmsGroup loopGroup = (CmsGroup)allGroups.get(i);
            String loopGroupName = loopGroup.getName();
            values.add(loopGroupName);
            options.add(loopGroupName);
            if (loopGroupName.equals(getSettings().getGroup())) {
                selectedIndex = i;
            }
        }

        return buildSelect(htmlAttributes, options, values, selectedIndex);                             
    }
    
    /**
     * Returns true if the user has publish permissions for the current project.<p>
     * 
     * @return true if the user has publish permissions for the current project
     */
    public boolean isPublishEnabled() {
        try {
            return ((getCms().isAdmin() || getCms().isManagerOfProject())  
                && (!getCms().getRequestContext().currentProject().isOnlineProject()));
        } catch (CmsException e) {
            return false;
        }        
    }
    
    /**
     * Returns true if the user has enabled synchronization.<p>
     * 
     * @return true if the user has enabled synchronization
     */
    public boolean isSyncEnabled() {
        try {
            return null != getCms().getRegistry().getSystemValue(I_CmsConstants.C_SYNCHRONISATION_PROJECT);
        } catch (CmsException e) {
            return false;
        }     
    }

    /**
     * Returns true if the online help for the users current language is installed.<p>
     * 
     * @return true if the online help for the users current language is installed
     */    
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getSettings().getLanguage() + "/");
            return true;
        } catch (CmsException e) {
            return false;
        }        
    }
    
    /**
     * Generates a button fot the OpenCms workplace.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * 
     * @return a button fot the OpenCms workplace
     */
    public String button(String href, String target, String image, String label, int type) {
        StringBuffer result = new StringBuffer(512); 
    
        result.append("<td>");      
        switch (type) {     
            case 1:
            // image and text
                if (href != null) {
                    result.append("<a href=\"");
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><span unselectable=\"on\" class=\"combobutton\" ");
                result.append("style=\"background-image: url('");
                result.append(getSkinUri());
                result.append("buttons/");
                result.append(image);
                result.append(".gif");
                result.append("');\">");
                result.append(key(label));
                result.append("</span></span>");
                if (href != null) {
                    result.append("</a>");
                }
                break;
            
            case 2:
            // text only
                if (href != null) {
                    result.append("<a href=\"");
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><span unselectable=\"on\" class=\"txtbutton\">");
                result.append(key(label));
                result.append("</span></span>");
                if (href != null) {
                    result.append("</a>");
                }       
                break;          
            
            default: 
            // only image
                if (href != null) {
                    result.append("<a href=\"");
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(" title=\"");
                    result.append(key(label));
                    result.append("\">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><img class=\"button\" src=\"");
                result.append(getSkinUri());
                result.append("buttons/");
                result.append(image);
                result.append(".gif");
                result.append("\">");
                result.append("</span>");
                if (href != null) {
                    result.append("</a>");
                }       
                break;
        }   
        result.append("</td>\n");
        return result.toString();   
    }
    
    /**
     * Generates a variable button bar separator line.<p>  
     * 
     * @param leftPixel the amount of pixel left to the line
     * @param rightPixel the amount of pixel right to the line
     * @param className the css class name for the formatting
     * 
     * @return  a variable button bar separator line
     */    
    public String buttonBarLine(int leftPixel, int rightPixel, String className) {
        StringBuffer result = new StringBuffer(512); 
        result.append("<td><span class=\"norm\"><span unselectable=\"on\" class=\"txtbutton\" style=\"padding-right: 0px; padding-left: "); 
        result.append(leftPixel);
        result.append("px;\"></span></span></td>\n<td><span class=\"");
        result.append(className);
        result.append("\"></span></td>\n");
        result.append("<td><span class=\"norm\"><span unselectable=\"on\" class=\"txtbutton\" style=\"padding-right: 0px; padding-left: ");
        result.append(rightPixel);
        result.append("px;\"></span></span></td>\n");
        return result.toString();        
    }
    
    /**
     * Generates a button bar starter tab.<p>  
     * 
     * @param leftPixel the amount of pixel left to the starter
     * @param rightPixel the amount of pixel right to the starter
     * 
     * @return a button bar starter tab
     */
    public String buttonBarStartTab(int leftPixel, int rightPixel) {
        return buttonBarLine(leftPixel, rightPixel, "starttab");
    }
    
    /**
     * Generates a button bar separator.<p>  
     * 
     * @param leftPixel the amount of pixel left to the separator
     * @param rightPixel the amount of pixel right to the separator
     * 
     * @return a button bar separator
     */
    public String buttonBarSeparator(int leftPixel, int rightPixel) {
        return buttonBarLine(leftPixel, rightPixel, "separator");
    }    
    
    /**
     * Generates a button bar label.<p>
     * 
     * @param label the label to show
     * 
     * @return a button bar label
     */
    public String buttonBarLabel(String label) {
        return buttonBarLabel(label, "norm");
    }  
    
    /**
     * Generates a button bar label.<p>
     * 
     * @param label the label to show
     * @param className the css class name for the formatting
     * 
     * @return a button bar label
     */    
    public String buttonBarLabel(String label, String className) {
        StringBuffer result = new StringBuffer(128); 
        result.append("<td><span class=\"");
        result.append(className);
        result.append("\"><span unselectable=\"on\" class=\"txtbutton\">");
        result.append(key(label));
        result.append("</span></span></td>\n");
        return result.toString();
    }
        
    /**
     * Returns the html for a button bar.<p>
     * 
     * @param segment the HTML segment (START / END)
     * 
     * @return a button bar html start / end segment 
     */
    public String buttonBar(int segment) {
        if (segment == HTML_START) {
            return "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr>\n";
        } else {
            return "</tr></table>";
        }
    }
}
