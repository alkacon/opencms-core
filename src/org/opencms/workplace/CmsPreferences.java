/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsPreferences.java,v $
 * Date   : $Date: 2004/02/04 10:48:13 $
 * Version: $Revision: 1.5 $
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
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsUser;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.workplace.CmsXmlLanguageFile;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.db.CmsUserSettings;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for the user preferences dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/dialogs/preferences_html
 * </ul>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.5 $
 * 
 * @since 5.1.12
 */
public class CmsPreferences extends CmsTabDialog {

    /** The dialog type */
    public static final String DIALOG_TYPE = "preferences";
    
    /** Request parameter value for the action: cancel button */
    public static final String DIALOG_CANCEL = "cancel";
    /** Request parameter value for the action: reload the workplace */
    public static final String DIALOG_RELOAD = "reload";
    /** Request parameter value for the action: change the password */
    public static final String DIALOG_CHPWD = "chpwd";
    
    /** Value for the action: cancel button */
    public static final int ACTION_CANCEL = 200;
    /** Value for the action: reload the workplace */
    public static final int ACTION_RELOAD = 201;
    /** Value for the action: change the password */
    public static final int ACTION_CHPWD = 202;
    /** Value for the action: show error screen */
    public static final int ACTION_ERROR = 203;
    
    private String m_paramNewPassword;
    private String m_paramOldPassword;

    /** User settings object used to store the dialog field values **/
    private CmsUserSettings m_userSettings;
    
    /** Constant for filter */
    private static final String C_SPACER = "------------------------------------------------";
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPreferences(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPreferences(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }  
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabs()
     */
    public List getTabs() {
        ArrayList tabList = new ArrayList(5);
        tabList.add(key("panel.workplace"));
        tabList.add(key("panel.explorer"));      
        tabList.add(key("panel.editors"));
        tabList.add(key("panel.task"));
        tabList.add(key("panel.user"));
        return tabList;
    }
    
    /**
     * @see org.opencms.workplace.CmsTabDialog#getTabParameterOrder()
     */
    public List getTabParameterOrder() {
        ArrayList orderList = new ArrayList(5);
        orderList.add("tabwp");
        orderList.add("tabex");
        orderList.add("tabed");
        orderList.add("tabwf");
        orderList.add("tabup");
        return orderList;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // create an empty user settings object
        m_userSettings = new CmsUserSettings(); 
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // get the active tab from request parameter or display first tab
        getActiveTab();
        
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_SET.equals(getParamAction())) {
            setAction(ACTION_SET);
        } else if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK); 
        } else if (DIALOG_RELOAD.equals(getParamAction())) {
            setAction(ACTION_RELOAD); 
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL); 
        } else if (DIALOG_CHPWD.equals(getParamAction())) {
            setAction(ACTION_CHPWD); 
        } else {
            if (!DIALOG_SWITCHTAB.equals(getParamAction())) {
                // first call of preferences dialog, fill param values with current settings
                fillUserSettings();
            }
            
            setAction(ACTION_DEFAULT);
            // build title for preferences dialog     
            setParamTitle(key("title.preferences"));
        }      
     
    } 
    
    /**
     * Performs the cancel operation of the settings dialog, i.e. redirecting to the right workplace view.<p>
     */
    public void actionCancel() {
        try {
            if (getSettings().isViewAdministration()) {                  
                String adminLink = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/administration.html";
                getCms().getRequestContext().getResponse().sendCmsRedirect(adminLink);
            } else {
                getCms().getRequestContext().getResponse().sendCmsRedirect(C_PATH_WORKPLACE + "/explorer_fs.html");
            }
        } catch (IOException e) {
            // do nothing
        }
    }
    
    /**
     * Performs the change password action.<p>
     * 
     * @throws JspException if inclusion of error element fails
     */
    public void actionChangePassword() throws JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        String oldPwd = getParamOldPassword();
        String newPwd = getParamNewPassword();
        // set the action parameter, reset the password parameters
        setAction(ACTION_DEFAULT);
        setParamOldPassword(null);
        setParamNewPassword(null);
        if (oldPwd != null && !"".equals(oldPwd.trim()) && newPwd != null && !"".equals(newPwd.trim())) {
            try {
                getCms().setPassword(getSettings().getUser().getName(), oldPwd, newPwd);
            } catch (CmsException e) {
                // failed setting the new password, show error dialog
                setAction(ACTION_ERROR);
                setParamErrorstack(e.getStackTraceAsString());
                setParamMessage(key("error.message.chpwd"));
                setParamReasonSuggestion(key("error.reason.chpwd2") + "<br>\n" + key("error.suggestion.chpwd2") + "\n");
                getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
            }
        } else {
            // form wasn't filled out correctly, show error dialog
            CmsException e = new CmsException("The password values you entered are not valid.");
            setAction(ACTION_ERROR);
            setParamErrorstack(e.getStackTraceAsString());
            setParamMessage(key("error.message.chpwd"));
            setParamReasonSuggestion(key("error.reason.chpwd2") + "<br>\n" + key("error.suggestion.chpwd2") + "\n");
            getJsp().include(C_FILE_DIALOG_SCREEN_ERROR);
        }
    }
    
    /**
     * Performs the save operation of the modified user settings.<p>
     */
    public void actionSave() {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        
        // set the current user in the settings object
        m_userSettings.setUser(getSettings().getUser());
        try {
            // write the user settings to the db
            m_userSettings.save(getCms());
        } catch (CmsException e) {
            // ignore this exception
        }
        
        // update the preferences and project after saving
        CmsWorkplaceAction.updatePreferences(getCms());
        try {
            int projectId = Integer.parseInt(m_userSettings.getStartProject());
            getCms().getRequestContext().setCurrentProject(projectId);
            getSettings().setProject(projectId);
        } catch (Exception e) {
            // ignore this exception
        }
        
        // now determine if the dialog has to be closed or not
        try {
            if (DIALOG_SET.equals(getParamAction())) {
                // after "set" action, leave dialog open 
                getCms().getRequestContext().getResponse().sendCmsRedirect(C_PATH_DIALOGS + "/preferences.html?" + PARAM_TAB + "=" + getActiveTab());
            } else {
                // after "ok" action, close dialog and reload the workplace view
                setParamOkFunctions("window.top.location.reload(true);");
                try {
                    closeDialog();
                } catch (JspException e) {
                    // closing dialog failed, redirect to dialog with action set to reload the workplace
                    getCms().getRequestContext().getResponse().sendCmsRedirect(C_PATH_DIALOGS + "/preferences.html?" + PARAM_ACTION + "=" + DIALOG_RELOAD);
                }    
            }
        } catch (IOException e) {
            // error during redirect, do nothing 
        }
    }
    
    /**
     * Builds the html for a common button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @param selectedIndex the index of the selected option
     * @return the html for the common button style select box
     */
    private String buildSelectButtonStyle(String htmlAttributes, int selectedIndex) {
        List options = new ArrayList(3);      
        options.add(key("preferences.buttonstyle.img"));
        options.add(key("preferences.buttonstyle.imgtxt"));
        options.add(key("preferences.buttonstyle.txt"));
        String [] vals = new String[] {"0", "1", "2"};
        List values = new ArrayList(java.util.Arrays.asList(vals));
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the editor button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the editor button style select box
     */
    public String buildSelectEditorButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabEdButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html for the explorer button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the explorer button style select box
     */
    public String buildSelectExplorerButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabExButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html for the explorer number of entries per page select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the explorer number of entries per page select box
     */
    public String buildSelectExplorerFileEntries(String htmlAttributes) {
        String [] opts = new String[] {"10", "25", "50", "100", key("preferences.fileentries.unlimited")};
        List options = new ArrayList(java.util.Arrays.asList(opts));
        String [] vals = new String[] {"10", "25", "50", "100", "" + Integer.MAX_VALUE};
        int selectedIndex = 2;
        for (int i=0; i<vals.length; i++) {
            if (vals[i].equals(getParamTabExFileEntries())) {
                selectedIndex = i;
            }
        }
        List values = new ArrayList(java.util.Arrays.asList(vals));
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Builds the html for the task startup filter select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the task startup filter select box
     */
    public String buildSelectFilter(String htmlAttributes) {
        List options = new ArrayList(16);
        List values = new ArrayList(16);
        int selectedIndex = -1;
        int counter = 0;
        
        for (int i=1; i<4; i++) {
            for (char k='a'; k<'d'; k++) {
                options.add(key(I_CmsConstants.C_TASK_FILTER + k + i));
                values.add("" + k + i);
                if (("" + k + i).equals(getParamTabWfFilter())) {
                    selectedIndex = counter;
                }
                counter++;
            }
            options.add(C_SPACER);
            values.add("");
            counter++;
        }
        for (int i=1; i<4; i++) {
            options.add(key(I_CmsConstants.C_TASK_FILTER + "d" + i));
            values.add("d" + i);
            if (("d" + i).equals(getParamTabWfFilter())) {
                selectedIndex = counter;
            }
            counter++;
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);  
    }
    
    /**
     * Builds the html for the language select box of the start settings.<p>
     *  
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the language select box
     */
    public String buildSelectLanguage(String htmlAttributes) {
        try {
            // get all folders with language files
            List allLangFolders = getCms().getSubFolders(I_CmsWpConstants.C_VFS_PATH_LOCALES);
            List options = new ArrayList(allLangFolders.size());
            List values = new ArrayList(allLangFolders.size());
            int checkedIndex = 0;
            for (int i=0; i<allLangFolders.size(); i++) {
                CmsFolder folder = (CmsFolder)allLangFolders.get(i);
                CmsXmlLanguageFile langFile = new CmsXmlLanguageFile(getCms(), folder.getName());
                options.add(langFile.getLanguageValue("name"));
                values.add(folder.getName());
                if (getParamTabWpLanguage().equals(folder.getName())) {
                    checkedIndex = i;
                }
            }
            return buildSelect(htmlAttributes, options, values, checkedIndex);
        } catch (CmsException e) {
            return getSettings().getLanguage();
        }
    }
    
    /**
     * Builds the html for the project select box of the start settings.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag 
     * @return the html for the project select box
     */
    public String buildSelectProject(String htmlAttributes) {
        try {
            Vector allProjects = getCms().getAllAccessibleProjects();
            List options = new ArrayList(allProjects.size());
            List values = new ArrayList(allProjects.size());
            int checkedIndex = 0;
            int startProjectId = -1;
            try {
                startProjectId = Integer.parseInt(getParamTabWpProject());
            } catch (NumberFormatException e) {
                // ignore this exception
            }
            
            for (int i=0; i<allProjects.size(); i++) {
                CmsProject project = (CmsProject)allProjects.get(i);
                options.add(project.getName());
                values.add("" + project.getId());
                if (startProjectId == project.getId()) {
                    checkedIndex = i;
                }
            }
            return buildSelect(htmlAttributes, options, values, checkedIndex);
        } catch (CmsException e) {
            return getSettings().getProject() + "";
        }
    }
    
    /**
     * Builds the html for the workplace report type select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace report type select box
     */
    public String buildSelectReportType(String htmlAttributes) {
        List options = new ArrayList(2);      
        options.add(key("preferences.report.simple"));
        options.add(key("preferences.report.extended"));
        String [] vals = new String[] {CmsReport.REPORT_TYPE_SIMPLE, CmsReport.REPORT_TYPE_EXTENDED};
        List values = new ArrayList(java.util.Arrays.asList(vals));
        int selectedIndex = 0;
        if (CmsReport.REPORT_TYPE_EXTENDED.equals(getParamTabWpReportType())) {
            selectedIndex = 1;
        }
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }
    
    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the views accessible by the current user
     */    
    public String buildSelectView(String htmlAttributes) {
        
        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        
        // get the Vector of available views from the registry      
        Vector viewNames = new Vector();
        Vector viewLinks = new Vector();
        getCms().getRegistry().getViews(viewNames, viewLinks);                       

        // loop through the vectors and fill the result vectors
        int numViews = viewNames.size();
        for (int i = 0; i<numViews; i++) {
            String loopName = (String)viewNames.get(i);
            String loopLink = (String)viewLinks.get(i);
            
            boolean visible = true;
            try {
                getCms().readFileHeader(loopLink);
            } catch (CmsException e) {
                visible = false;
            }
            if (visible) {
                //loopLink = getJsp().link(loopLink);
                options.add(key(loopName));
                values.add(loopLink);

                if (loopLink.equals(getParamTabWpView())) {
                    selectedIndex = i;
                }
            }
        }        
  
        return buildSelect(htmlAttributes, options, values, selectedIndex);        
    }
    
    /**
     * Builds the html for the workplace button style select box.<p>
     * 
     * @param htmlAttributes optional html attributes for the &lgt;select&gt; tag
     * @return the html for the workplace button style select box
     */
    public String buildSelectWorkplaceButtonStyle(String htmlAttributes) {
        int selectedIndex = Integer.parseInt(getParamTabWpButtonStyle());
        return buildSelectButtonStyle(htmlAttributes, selectedIndex);
    }
    
    /**
     * Builds the html code for the static user information table (tab 4).<p>
     * 
     * @return the html code for the static user information table
     */
    public String buildUserInformation() {
        StringBuffer result = new StringBuffer(512);
        CmsUser user = getSettings().getUser();
        CmsUserSettings settings = new CmsUserSettings(user);
        
        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"4\">\n");
        result.append("<tr>\n");
        result.append("\t<td style=\"width: 25%;\">" + key("input.user") + "</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">" + user.getName() + "</td>\n");
        result.append("\t<td style=\"width: 25%;\">" + key("input.email") + "</td>\n");
        result.append("\t<td class=\"textbold\" style=\"width: 25%;\">" + user.getEmail() + "</td>\n");        
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.lastname") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getLastname() + "</td>\n");
        result.append("\t<td rowspan=\"3\" style=\"vertical-align: top;\">" + key("input.adress") + "</td>\n");
        
        String address = user.getAddress();
        if ((settings.getAddressZip() != null && !"".equals(settings.getAddressZip())) 
                || (settings.getAddressTown() != null && !"".equals(settings.getAddressTown()))) {
            address += "<br>" + settings.getAddressZip();
            address += " " + settings.getAddressTown();
        }        
        result.append("\t<td rowspan=\"3\" class=\"textbold\" style=\"vertical-align: top;\">" + address + "</td>\n");        
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.firstname") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getFirstname() + "</td>\n");       
        result.append("</tr>\n");
        
        result.append("<tr>\n");
        result.append("\t<td>" + key("input.description") + "</td>\n");
        result.append("\t<td class=\"textbold\">" + user.getDescription() + "</td>\n");       
        result.append("</tr>\n");       
        result.append("</table>\n");
        
        return result.toString();
    }
    
    /**
     * Returns the action for the "cancel" button of the error dialog.<p>
     * 
     * This overwrites the cancel method of the CmsDialog class.<p>
     * 
     * Always use this value, do not write anything directly in the html page.<p>
     * 
     * @return the default action for a "cancel" button
     */
    public String buttonActionCancel() {
        String target = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplaceAction.C_JSP_WORKPLACE_URI);
        return "onClick=\"top.location.href='" + target + "';\"";
    }
    
    /**
     * Fills the parameter values according to the settings of the current user.<p>
     * 
     * This method is called once when first displaying the preferences dialog.<p>
     */
    private void fillUserSettings() {
        m_userSettings = new CmsUserSettings(getSettings().getUser());
    }
    
    /**
     * Helper method to add the "checked" attribute to an input field.<p>
     * 
     * @param paramValue the parameter value, if "true", the "checked" attribute will be returned 
     * @return the "checked" attribute or an empty String 
     */
    public String isChecked(String paramValue) {
        if ("true".equals(paramValue)) {
            return " checked=\"checked\"";
        }
        return "";
    }
    
    /**
     * Helper method for the request parameter methods to return a String depending on the boolean parameter.<p>
     * 
     * @param isEnabled the boolean variable to check
     * @return "true" if isEnabled is true, otherwise ""
     */
    private String isParamEnabled(boolean isEnabled) {
        if (isEnabled) {
            return "true";
        }
        return "";
    }
    
    /**
     * Returns the "editor button style" setting.<p>
     * 
     * @return the "editor button style" setting
     */
    public final String getParamTabEdButtonStyle() {
        return "" + m_userSettings.getEditorButtonStyle();
    }
    
    /**
     * Sets the "editor button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "editor button style" setting
     */
    public final void setParamTabEdButtonStyle(String value) {
        try {
            m_userSettings.setEditorButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Returns the "explorer button style" setting.<p>
     * 
     * @return the "explorer button style" setting
     */
    public final String getParamTabExButtonStyle() {
        return "" + m_userSettings.getExplorerButtonStyle();
    }
    
    /**
     * Sets the "explorer button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "explorer button style" setting
     */
    public final void setParamTabExButtonStyle(String value) {
        try {
            m_userSettings.setExplorerButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Returns the "display file creation date" setting.<p>
     * 
     * @return "true" if the file creation date input field is checked, otherwise ""
     */
    public final String getParamTabExFileDateCreated() {
        return isParamEnabled(m_userSettings.showExplorerFileDateCreated());
    }

    /**
     * Sets the "display file creation date" setting.<p>
     * 
     * @param value "true" to enable the "display file creation date" setting, all others to disable
     */
    public final void setParamTabExFileDateCreated(String value) {
        m_userSettings.setShowExplorerFileDateCreated("true".equals(value));
    }
    
    /**
     * Returns the "display file last modification date" setting.<p>
     * 
     * @return "true" if the file last modification date input field is checked, otherwise ""
     */
    public final String getParamTabExFileDateLastModified() {
        return isParamEnabled(m_userSettings.showExplorerFileDateLastModified());
    }

    /**
     * Sets the "display file last modification date" setting.<p>
     * 
     * @param value "true" to enable the "display file last modification date" setting, all others to disable
     */
    public final void setParamTabExFileDateLastModified(String value) {
        m_userSettings.setShowExplorerFileDateLastModified("true".equals(value));
    }
    
    /**
     * Returns the "explorer number of entries per page" setting.<p>
     * 
     * @return the "explorer number of entries per page" setting
     */
    public final String getParamTabExFileEntries() {
        return "" + m_userSettings.getExplorerFileEntries();
    }
    
    /**
     * Sets the "explorer number of entries per page" setting.<p>
     * 
     * @param value a String representation of an int value to set the "number of entries per page" setting
     */
    public final void setParamTabExFileEntries(String value) {
        try {
            m_userSettings.setExplorerFileEntries(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Returns the "display file locked by" setting.<p>
     * 
     * @return "true" if the file locked by input field is checked, otherwise ""
     */
    public final String getParamTabExFileLockedBy() {
        return isParamEnabled(m_userSettings.showExplorerFileLockedBy());
    }

    /**
     * Sets the "display file locked by" setting.<p>
     * 
     * @param value "true" to enable the "display file locked by" setting, all others to disable
     */
    public final void setParamTabExFileLockedBy(String value) {
        m_userSettings.setShowExplorerFileLockedBy("true".equals(value));
    }
    
    /**
     * Returns the "display file permissions" setting.<p>
     * 
     * @return "true" if the file permissions input field is checked, otherwise ""
     */
    public final String getParamTabExFilePermissions() {
        return isParamEnabled(m_userSettings.showExplorerFilePermissions());
    }

    /**
     * Sets the "display file permissions" setting.<p>
     * 
     * @param value "true" to enable the "display file permissions" setting, all others to disable
     */
    public final void setParamTabExFilePermissions(String value) {
        m_userSettings.setShowExplorerFilePermissions("true".equals(value));
    }
    
    /**
     * Returns the "display file size" setting.<p>
     * 
     * @return "true" if the file size input field is checked, otherwise ""
     */
    public final String getParamTabExFileSize() {
        return isParamEnabled(m_userSettings.showExplorerFileSize());
    }
    
    /**
     * Sets the "display file size" setting.<p>
     * 
     * @param value "true" to enable the "display file size" setting, all others to disable
     */
    public final void setParamTabExFileSize(String value) {
        m_userSettings.setShowExplorerFileSize("true".equals(value));
    }
    
    /**
     * Returns the "display file state" setting.<p>
     * 
     * @return "true" if the file state input field is checked, otherwise ""
     */
    public final String getParamTabExFileState() {
        return isParamEnabled(m_userSettings.showExplorerFileState());
    }

    /**
     * Sets the "display file state" setting.<p>
     * 
     * @param value "true" to enable the "display file state" setting, all others to disable
     */
    public final void setParamTabExFileState(String value) {
        m_userSettings.setShowExplorerFileState("true".equals(value));
    }
    
    /**
     * Returns the "display file title" setting.<p>
     * 
     * @return "true" if the file title input field is checked, otherwise ""
     */
    public final String getParamTabExFileTitle() {
        return isParamEnabled(m_userSettings.showExplorerFileTitle());
    }

    /**
     * Sets the "display file title" setting.<p>
     * 
     * @param value "true" to enable the "display file title" setting, all others to disable
     */
    public final void setParamTabExFileTitle(String value) {
        m_userSettings.setShowExplorerFileTitle("true".equals(value));
    }
    
    /**
     * Returns the "display file type" setting.<p>
     * 
     * @return "true" if the file type input field is checked, otherwise ""
     */
    public final String getParamTabExFileType() {
        return isParamEnabled(m_userSettings.showExplorerFileType());
    }

    /**
     * Sets the "display file type" setting.<p>
     * 
     * @param value "true" to enable the "display file type" setting, all others to disable
     */
    public final void setParamTabExFileType(String value) {
        m_userSettings.setShowExplorerFileType("true".equals(value));
    }
    
    /**
     * Returns the "display file created by" setting.<p>
     * 
     * @return "true" if the file created by input field is checked, otherwise ""
     */
    public final String getParamTabExFileUserCreated() {
        return isParamEnabled(m_userSettings.showExplorerFileUserCreated());
    }

    /**
     * Sets the "display file created by" setting.<p>
     * 
     * @param value "true" to enable the "display file created by" setting, all others to disable
     */
    public final void setParamTabExFileUserCreated(String value) {
        m_userSettings.setShowExplorerFileUserCreated("true".equals(value));
    }
    
    /**
     * Returns the "display file last modified by" setting.<p>
     * 
     * @return "true" if the file last modified by input field is checked, otherwise ""
     */
    public final String getParamTabExFileUserLastModified() {
        return isParamEnabled(m_userSettings.showExplorerFileUserLastModified());
    }

    /**
     * Sets the "display file last modified by" setting.<p>
     * 
     * @param value "true" to enable the "display file last modified by" setting, all others to disable
     */
    public final void setParamTabExFileUserLastModified(String value) {
        m_userSettings.setShowExplorerFileUserLastModified("true".equals(value));
    }
    
    /**
     * Returns the "task startup filter" setting.<p>
     * 
     * @return the "task startup filter" setting
     */
    public final String getParamTabWfFilter() {
        return m_userSettings.getTaskStartupFilter();
    }
    
    /**
     * Sets the "task startup filter" setting.<p>
     * 
     * @param filter the "task startup filter" setting
     */
    public final void setParamTabWfFilter(String filter) {
        if ("".equals(filter)) {
            filter = "a1";
        }
        m_userSettings.setTaskStartupFilter(filter);
    }
    
    /**
     * returns the "show all projects" setting.<p>
     * 
     * @return the "show all projects" setting
     */
    public final String getParamTabWfShowAllProjects() {
        return isParamEnabled(m_userSettings.taskShowAllProjects());
    }
    
    /**
     * Sets the "show all projects" setting.<p>
     * 
     * @param value the "show all projects" setting
     */
    public final void setParamTabWfShowAllProjects(String value) {
        m_userSettings.setTaskShowAllProjects("true".equals(value));
    }
    
    /**
     * Returns the "message when accepted" setting.<p>
     * 
     * @return the "message when accepted" setting
     */
    public final String getParamTabWfMessageAccepted() {
        return isParamEnabled(m_userSettings.taskMessageAccepted());
    }
    
    /**
     * Sets the "message when accepted" setting.<p>
     * 
     * @param value the "message when accepted" setting
     */
    public final void setParamTabWfMessageAccepted(String value) {
        m_userSettings.setTaskMessageAccepted("true".equals(value));
    }
    
    /**
     * Returns the "message when forwarded" setting.<p>
     * 
     * @return the "message when forwarded" setting
     */
    public final String getParamTabWfMessageForwarded() {
        return isParamEnabled(m_userSettings.taskMessageForwarded());
    }
    
    /**
     * Sets the "message when forwarded" setting.<p>
     * 
     * @param value the "message when forwarded" setting
     */
    public final void setParamTabWfMessageForwarded(String value) {
        m_userSettings.setTaskMessageForwarded("true".equals(value));
    }
    
    /**
     * Returns the "message when completed" setting.<p>
     * 
     * @return the "message when completed" setting
     */
    public final String getParamTabWfMessageCompleted() {
        return isParamEnabled(m_userSettings.taskMessageCompleted());
    }
    
    /**
     * Sets the "message when completed" setting.<p>
     * 
     * @param value the "message when completed" setting
     */
    public final void setParamTabWfMessageCompleted(String value) {
        m_userSettings.setTaskMessageCompleted("true".equals(value));
    }
    
    /**
     * Returns the "inform all role members" setting.<p>
     * 
     * @return "true" if the "inform all role members" input field is checked, otherwise ""
     */
    public final String getParamTabWfMessageMembers() {
        return isParamEnabled(m_userSettings.taskMessageMembers());
    }
    
    /**
     * Sets the "inform all role members" setting.<p>
     * 
     * @param value "true" to enable the "inform all role members" setting, all others to disable
     */
    public final void setParamTabWfMessageMembers(String value) {
        m_userSettings.setTaskMessageMembers("true".equals(value));
    }
    
    /**
     * Returns the "workplace button style" setting.<p>
     * 
     * @return the "workplace button style" setting
     */
    public final String getParamTabWpButtonStyle() {
        return "" + m_userSettings.getWorkplaceButtonStyle();
    }
    
    /**
     * Sets the "workplace button style" setting.<p>
     * 
     * @param value a String representation of an int value to set the "workplace button style" setting
     */
    public final void setParamTabWpButtonStyle(String value) {
        try {
            m_userSettings.setWorkplaceButtonStyle(Integer.parseInt(value));
        } catch (Throwable t) {
            // ignore this exception
        }
    }
    
    /**
     * Returns the "display lock dialog" setting.<p>
     * 
     * @return "true" if the "display lock dialog" input field is checked, otherwise ""
     */
    public final String getParamTabWpShowLock() {
        return isParamEnabled(m_userSettings.showLockDialog());
    }

    /**
     * Sets the "display lock dialog" setting.<p>
     * 
     * @param value "true" to enable the "display lock dialog" setting, all others to disable
     */
    public final void setParamTabWpShowLock(String value) {
        m_userSettings.setShowLockDialog("true".equals(value));
    }
    
    /**
     * Returns the "use upload applet" setting.<p>
     * 
     * @return "true" if the "use upload applet" input is checked, otherwise ""
     */
    public final String getParamTabWpUseUploadApplet() {
        return isParamEnabled(m_userSettings.useUploadApplet());
    }

    /**
     * Sets the "use upload applet" setting.<p>
     * 
     * @param value "true" to enable the "use upload applet" setting, all others to disable
     */
    public final void setParamTabWpUseUploadApplet(String value) {
        m_userSettings.setUseUploadApplet("true".equals(value));
    }
    
    /**
     * Returns the "workplace report type" setting.<p>
     * 
     * @return the "workplace report type" setting
     */
    public final String getParamTabWpReportType() {
        return m_userSettings.getWorkplaceReportType();
    }

    /**
     * Sets the "workplace report type" setting.<p>
     * 
     * @param value the "workplace report type" setting
     */
    public final void setParamTabWpReportType(String value) {
        if (CmsReport.REPORT_TYPE_SIMPLE.equals(value) || CmsReport.REPORT_TYPE_EXTENDED.equals(value)) {
            // set only if valid parameter value is found
            m_userSettings.setWorkplaceReportType(value);
        }
    }
    
    /**
     * Returns the start language setting.<p>
     * 
     * @return the start language setting
     */
    public final String getParamTabWpLanguage() {
        return m_userSettings.getStartLanguage();
    }

    /**
     * Sets the start language setting.<p>
     * 
     * @param value the start language setting
     */
    public final void setParamTabWpLanguage(String value) {
        m_userSettings.setStartLanguage(value);
    }
    
    /**
     * Returns the start project setting.<p>
     * 
     * @return the start project setting
     */
    public final String getParamTabWpProject() {
        return m_userSettings.getStartProject();
    }

    /**
     * Sets the start project setting.<p>
     * 
     * @param value the start project setting
     */
    public final void setParamTabWpProject(String value) {
        m_userSettings.setStartProject(value);
    }
    
    /**
     * Returns the start view setting.<p>
     * 
     * @return the start view setting
     */
    public final String getParamTabWpView() {
        return m_userSettings.getStartView();
    }

    /**
     * Sets the start view setting.<p>
     * 
     * @param value the start view setting
     */
    public final void setParamTabWpView(String value) {
        m_userSettings.setStartView(value);
    }
    
    /**
     * Returns the new password value.<p>
     * 
     * @return the new password value
     */
    public final String getParamNewPassword() {
        return m_paramNewPassword;
    }
    
    /**
     * Sets the new password value.<p>
     * 
     * @param newPwd the new password value
     */
    public final void setParamNewPassword(String newPwd) {
        m_paramNewPassword = newPwd;
    }
    
    /**
     * Returns the old password value.<p>
     * 
     * @return the old password value
     */
    public final String getParamOldPassword() {
        return m_paramOldPassword;
    }
    
    /**
     * Sets the old password value.<p>
     * 
     * @param oldPwd the old password value
     */
    public final void setParamOldPassword(String oldPwd) {
        m_paramOldPassword = oldPwd;
    }
    
}
